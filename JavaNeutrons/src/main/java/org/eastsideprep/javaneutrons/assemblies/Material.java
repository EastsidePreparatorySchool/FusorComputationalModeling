/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Event;
import org.eastsideprep.javaneutrons.core.LogEnergyEVHistogram;
import org.eastsideprep.javaneutrons.core.LogHistogram;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class Material {

    public static HashMap<String, Material> materials = new HashMap<>();

    public class Component {

        Element e;
        double density; // atoms/(barn*cm)
        double proportion;

        Component(Element e, double proportion) {
            this.e = e;
            this.density = 0;
            this.proportion = proportion;
        }
    }

    public String name;
    ArrayList<Component> components;
    public LogHistogram lengths;
    public LogEnergyEVHistogram scattersOverEnergyBefore;
    public LogEnergyEVHistogram scattersOverEnergyAfter;
    public LogEnergyEVHistogram capturesOverEnergy;
    public double totalEvents;
    public double totalFreePath;
    public long pathCount;

    public Material(String name) {
        materials.put(name, this);
        components = new ArrayList<>();
        this.name = name;
        resetDetector();
    }

    public static Material getByName(String name) {
        return materials.get(name);
    }

    public final void addComponent(Element element, double proportion) {
        components.add(new Component(element, proportion));
    }

    public final void calculateAtomicDensities(double densityMass) {
        // input is mass per cubic meter! not centimeter!
        // see volume below
        //
        // first, how much mass in one of these units 
        // (proportion units are arbitrary)
        double massMolecule = 0;
        double sumProps = 0;
        for (Component c : components) {
            massMolecule += c.e.mass * c.proportion;
            sumProps += c.proportion;
        }

        // how many of these make up a cubic centimeter?
        double volume = 1e-6; // 1 cubic centimeter in m
        double n = densityMass * volume / massMolecule;

        // if there is n units per volume, 
        // then are are n*proportion units of the component
        for (Component c : components) {
            c.density = n * c.proportion;
        }
//        System.out.println("Macroscopic cross-section for "
//                + (this instanceof Element ? "(element) " : "")
//                + this.name + " at 1 eV: " + getSigma(1 * Util.Physics.eV));
    }

    public final void resetDetector() {
        this.totalEvents = 0;
        this.scattersOverEnergyBefore = new LogEnergyEVHistogram();
        this.scattersOverEnergyAfter = new LogEnergyEVHistogram();
        this.capturesOverEnergy = new LogEnergyEVHistogram();
        this.lengths = new LogHistogram(-5, 5, 70);
        this.totalEvents = 0;
        this.totalFreePath = 0;
        this.pathCount = 0;
    }

    // compute macroscopic cross-section
    public double getSigma(double energy) {
        double sigma = 0;
        for (Component c : components) {
            sigma += c.e.getTotalCrossSection(energy) * c.density;
        }
        return sigma;
    }

    private double randomPathLength(double energy) {
        double length = -Math.log(ThreadLocalRandom.current().nextDouble()) / getSigma(energy);
        this.lengths.record(1, length);
        synchronized (this) {
            this.totalFreePath += length;
            this.pathCount++;
        }
        return length;
    }

    public Event nextPoint(Neutron n) {
        double energy = n.energy;
        double t = randomPathLength(energy);
        Vector3D location = n.position.add(n.direction.scalarMultiply(t));

        // make array of cumulative sums of sigmas
        double[] sigmas = new double[2 * components.size()];
        double sum = 0;
        for (int i = 0; i < sigmas.length; i += 2) {
            Component c = components.get(i / 2);
            sum += c.e.getScatterCrossSection(energy) * c.density;
            sigmas[i] = sum;
            sum += c.e.getCaptureCrossSection(energy) * c.density;
            sigmas[i + 1] = sum;
        }

        // random draw from across the combined distribution
        double rand = ThreadLocalRandom.current().nextDouble() * sum;
        //System.out.println("sum: "+sum+"draw: "+rand);

        // now find the component index
        int slot = Arrays.binarySearch(sigmas, rand);
        // if not found, will be negative slot -1
        if (slot < 0) {
            slot = -slot - 1;
        }

        Element e = components.get(slot / 2).e;
        Event.Code code = (slot % 2 == 0) ? Event.Code.Scatter : Event.Code.Capture;

        return new Event(location, code, t, e, n);
    }

    public static Material getRealMaterial(Object material) {

        // try named material instance
        if (material instanceof String) {
            String name = (String) material;
            material = Material.getByName(name);
            if (material != null) {
                return (Material) material;
            }

            // if not named, try the class
            try {
                material = Class.forName("org.eastsideprep.javaneutrons.materials." + name);
            } catch (ClassNotFoundException ex) {
            }
            // now we have a class and can resolve in the next step
        }

        // if it was a class, call the "getInstance" method
        if (material instanceof Class) {
            try {
                Method method = ((Class) material).getDeclaredMethod("getInstance");
                method.setAccessible(true);
                material = method.invoke(null, new Object[]{});
            } catch (IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException x) {
                x.printStackTrace();
            }
        }

        // if it is a part, extract the material
        if (material instanceof Part) {
            material = ((Part) material).material;
        }

        return (Material) material;
    }

    public void processEvent(Event event) {
        // record stats for material
        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyBefore.record(1, event.neutron.energy);
        } else {
            this.capturesOverEnergy.record(1, event.neutron.energy);
        }

        // let the neutron do its thing
        event.neutron.processEvent(event);

        // record more stats for material
        synchronized (this) {
            this.totalEvents++;
        }
        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyAfter.record(1, event.neutron.energy);
        }
    }

    public XYChart.Series makeSigmaSeries(String seriesName) {
        XYChart.Series series = new XYChart.Series();
        ObservableList data = series.getData();
        series.setName(seriesName);

        for (double energy = 1e-3; energy < 1e7; energy *= 1.1) {
            DecimalFormat f = new DecimalFormat("0.##E0");
            String tick = f.format(energy);

            data.add(new XYChart.Data(tick, getSigma(energy * Util.Physics.eV)));
        }

        return series;
    }

}
