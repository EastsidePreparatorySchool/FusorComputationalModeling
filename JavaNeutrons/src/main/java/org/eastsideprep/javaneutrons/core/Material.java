package org.eastsideprep.javaneutrons.core;

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

/**
 *
 * @author gunnar
 */
public class Material {

    public static HashMap<String, Material> materials = new HashMap<>();

    public class Component {

        Isotope e;
        double density; // atoms/(barn*cm)
        double proportion;

        Component(Isotope e, double proportion) {
            this.e = e;
            this.density = 0;
            this.proportion = proportion;
        }
    }

    public String name;
    ArrayList<Component> components;
    public Histogram lengths;
    public EnergyHistogram scattersOverEnergyBefore;
    public EnergyHistogram scattersOverEnergyAfter;
    public EnergyHistogram capturesOverEnergy;
    public EnergyHistogram lengthOverEnergy;
    public double totalEvents;
    public double totalFreePath;
    public long pathCount;

    //public int temp;
    public Material(String name) {
        materials.put(name, this);
        components = new ArrayList<>();
        this.name = name;
        resetDetector();
    }

    public static Material getByName(String name) {
        return materials.get(name);
    }

    public final void addComponent(Isotope element, double proportion) {
        components.add(new Component(element, proportion));
    }

    public final void calculateAtomicDensities(double densityMass) {
        // input is mass per cubic meter! not centimeter!
        // see volume below
        //
        // first, how much mass in one of these units 
        // (proportion units are arbitrary)
        double massMolecule = 0;
        for (Component c : components) {
            massMolecule += c.e.mass * c.proportion;
        }

        // how many of these make up a cubic centimeter?
        double volume = 1e-6; // 1 cubic centimeter in m
        double densityAtoms = densityMass * volume / massMolecule * Util.Physics.barn;

        // if there is n units per volume, 
        // then are are n*proportion units of the component
        System.out.println("Material " + this.name + " mass density " + densityMass + " Kg / m^3");
        System.out.println(" atomic density " + String.format("%6.3e", densityAtoms) + " / b-cm");
        for (Component c : components) {
            c.density = densityAtoms * c.proportion;
            System.out.println(" Element " + c.e.name + ": atomic density: " + String.format("%6.3e", c.density)
                    + ", microscopic cs (s,c) sigma at 2.45 MeV: " + String.format("%6.3e", c.e.getScatterCrossSection(2.45e6)) + " barn"
                    + ", " + String.format("%6.3e", c.e.getCaptureCrossSection(2.45e6)) + " barn");
        }
        System.out.println(" Macroscopic cross-section Sigma at 2.45 MeV: " + String.format("%6.3e", getSigma(2.45e6)) + " 1/cm");
        System.out.println(" 1/Sigma(2.45 MeV): " + String.format("%6.3e", (1 / getSigma(2.45e6))) + " cm");
        System.out.println(" Macroscopic cross-section Sigma at 0.025 eV: "
                + String.format("%6.3e", getSigma(Util.Physics.thermalEnergy / Util.Physics.eV)) + " 1/cm");
        System.out.println(" 1/Sigma(0.025 eV): "
                + String.format("%6.3e", (1 / getSigma(Util.Physics.thermalEnergy / Util.Physics.eV))) + " cm");
        System.out.println("");
    }

    public final void resetDetector() {
        this.totalEvents = 0;
        this.scattersOverEnergyBefore = new EnergyHistogram();
        this.scattersOverEnergyAfter = new EnergyHistogram();
        this.capturesOverEnergy = new EnergyHistogram();
        this.lengthOverEnergy = new EnergyHistogram();
        this.lengths = new Histogram(-5, 7, 120, false);
        this.totalEvents = 0;
        this.totalFreePath = 0;
        this.pathCount = 0;

    }

    // compute macroscopic cross-section
    // input: eV
    public double getSigma(double energy) {
        double sigma = 0;
        for (Component c : components) {
            sigma += c.e.getTotalCrossSection(energy) * c.density;
        }
        return sigma;
    }

    // input: SI(cm)
    public double randomPathLength(double energy) {
        double length = -Math.log(ThreadLocalRandom.current().nextDouble()) / getSigma(energy / Util.Physics.eV);
        return length;
    }

    public void recordLength(double length, double energy) {
        this.lengths.record(1, length);
        this.lengthOverEnergy.record(length, energy);
        synchronized (this) {
            this.totalFreePath += length;
        }
    }

    public void recordCollision() {
        synchronized (this) {
            this.pathCount++;
        }
    }

    public Event nextPoint(Neutron n) {
        double energy = n.energy;
        double t = randomPathLength(energy);
        
        Vector3D location = n.position.add(n.direction.scalarMultiply(t));

        if (t > 2*Environment.limit) {
            // we don't go that far
            return new Event(location, Event.Code.Gone, t);
        }
        
        if (n.mcs.traceLevel >= 2) {
            //System.out.println("");
            //System.out.println("Neutron at " + n.energy + " in " + this.name + ", t: " + t);
        }
        // make array of cumulative sums of sigmas
        double[] sigmas = new double[2 * components.size()];
        double sum = 0;
        for (int i = 0; i < sigmas.length; i += 2) {
            Component c = components.get(i / 2);
            sum += c.e.getScatterCrossSection(energy / Util.Physics.eV) * c.density;
            if (n.mcs.traceLevel >= 2) {
                //System.out.println(" e " + c.e.name + " s to " + sum);
            }

            sigmas[i] = sum;
            sum += c.e.getCaptureCrossSection(energy / Util.Physics.eV) * c.density;
            if (n.mcs.traceLevel >= 2) {
                //System.out.println(" e " + c.e.name + " c to " + sum);
            }

            sigmas[i + 1] = sum;
        }

        // random draw from across the combined distribution
        double rand = ThreadLocalRandom.current().nextDouble() * sum;
        if (n.mcs.traceLevel >= 2) {
            //System.out.println("sum: " + sum + ", draw: " + rand);
        }

        // now find the component index
        int slot = Arrays.binarySearch(sigmas, rand);
        // if not found, will be negative slot -1
        if (slot < 0) {
            slot = -slot - 1;
        }
        if (n.mcs.traceLevel >= 2) {
            //System.out.println("Slot " + slot);
        }

        Isotope e = components.get(slot / 2).e;
        Event.Code code = (slot % 2 == 0) ? Event.Code.Scatter : Event.Code.Capture;
        if (n.mcs.traceLevel >= 2) {
            //System.out.println("Component: " + e.name + ", code: " + code);
        }

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

    public void processEvent(Event event, boolean processNeutron) {
        if (null != event.code) // record stats for material
        {
            switch (event.code) {
                case Scatter:
                    this.scattersOverEnergyBefore.record(1, event.neutron.energy);
                    this.recordCollision();
                    // record more stats for material
                    synchronized (this) {
                        this.totalEvents++;
                    }
                    break;
                case Capture:
                    this.capturesOverEnergy.record(1, event.neutron.energy);
                    this.recordCollision();
                    // record more stats for material
                    synchronized (this) {
                        this.totalEvents++;
                    }
                    break;
                case Gone:
                    Environment.recordEscape(event.neutron.energy);
                    break;
                default:
                    break;
            }
        }

        //if (event.neutron.energy > 2.44e6 * Util.Physics.eV) {
        this.recordLength(event.t, event.neutron.energy);
//        if (this.name.equals("Air"))
//        synchronized (this) {
//            if (event.t > 300) {
//                temp++;
//                System.out.println("recorded " + temp);
//            }
//        }
        //}

        if (event.neutron != null && processNeutron) {
            // let the neutron do its thing
            event.neutron.processEvent(event);
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

            data.add(new XYChart.Data(tick, getSigma(energy)));
            //System.out.println(tick + " " + getSigma(energy));
        }

        return series;
    }

}
