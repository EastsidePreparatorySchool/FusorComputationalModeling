/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class Element {

    private class CSEntry {

        double energy;
        double area;

        private CSEntry(double energy, double area) {
            this.energy = energy;
            this.area = area;
        }
    }

    public static HashMap<String, Element> elements = new HashMap<>();

    public String name;
    public double mass; // g
    public int atomicNumber;
    protected int neutrons;
    private ArrayList<CSEntry> elasticEntries;
    private ArrayList<CSEntry> captureEntries;
    private ArrayList<CSEntry> totalEntries;

    // for when you are too lazy to look up the correct mass
    public Element(String name, int atomicNumber, int neutrons) {
        this(name, atomicNumber, neutrons, atomicNumber * Util.Physics.protonMass + neutrons * Neutron.mass);
    }

    // use this when you know the mass in kg
    public Element(String name, int atomicNumber, int neutrons, double mass) {
        Element.elements.put(name, this);

        this.atomicNumber = atomicNumber;
        this.neutrons = neutrons;
        this.name = name;
        this.mass = mass;

        // read appropriate ENDF-derived data file
        // for the lightest stable isotope of the element
        readDataFiles(atomicNumber);
    }

    public String getName() {
        return this.name;
    }

    public static Element getByName(String name) {
        return Element.elements.get(name);
    }

    public double getScatterCrossSection(double energy) {
        return getArea(elasticEntries, energy);
    }

    public double getCaptureCrossSection(double energy) {
        return getArea(captureEntries, energy);
    }

    public double getTotalCrossSection(double energy) {
        return getArea(totalEntries, energy);
    }

    protected final void readDataFiles(int atomicNumber) {
        String filename = Integer.toString(atomicNumber*1000+atomicNumber+neutrons);
        fillEntries(filename);
    }

    //
    // kind is "elastic" or "total"
    //
    private void fillEntries(String fileName) {
        double epsilon = 0.1;

        // read xyz.csv from resources/data
        InputStream is = Element.class.getResourceAsStream("/data/ace/" + fileName + ".800nc.ace.csv");
        if (is == null) {
            System.err.println("Data file " + fileName + " not found for element " + this.name);
            return;
        }
        Scanner sc = new Scanner(is);
        sc.nextLine(); // skip header

        ArrayList<CSEntry> newScatter = new ArrayList<>(); //reset
        ArrayList<CSEntry> newCapture = new ArrayList<>(); //reset
        ArrayList<CSEntry> newTotal = new ArrayList<>(); //reset

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] split = line.split(",");
            double energy = Double.parseDouble(split[0]);
            double scatter = Double.parseDouble(split[1]);
            double capture = Double.parseDouble(split[2]);
            double total = Double.parseDouble(split[3]);
            if (Math.abs(total - (scatter + capture)) > total * epsilon &&
                    energy < 2.6e6) {
                System.out.println("Element " + this.name + ", energy " + energy + 
                        ": inelastic events other than capture make up more than " + 
                        (int) (epsilon * 100) + " % of cs: "+
                        Math.round(100*Math.abs(total - (scatter + capture))/total*100)/100+" %");
            }
            newScatter.add(new CSEntry(energy, scatter));
            newCapture.add(new CSEntry(energy, capture));
            newTotal.add(new CSEntry(energy, total));
        }
        Collections.sort(newScatter, (a, b) -> {
            return (int) Math.signum(a.energy - b.energy);
        });
        Collections.sort(newCapture, (a, b) -> {
            return (int) Math.signum(a.energy - b.energy);
        });
        Collections.sort(newTotal, (a, b) -> {
            return (int) Math.signum(a.energy - b.energy);
        });

        this.elasticEntries = newScatter;
        this.captureEntries = newCapture;
        this.totalEntries = newTotal;
    }

    private double getArea(ArrayList<CSEntry> data, double energy) {
        // table data is in eV, convert to SI (cm)
        energy /= Util.Physics.eV;
        //System.out.println("Energy: "+energy+" eV");
        int index = Collections.binarySearch(data, new CSEntry(energy, 0), (a, b) -> (int) Math.signum(a.energy - b.energy));
        if (index >= 0) {
            return data.get(index).area * Util.Physics.barn;
        }
        //else, linear interpolate between two nearest points
        index = -index - 1;
        if (index == 0 || index >= data.size()) {
            // todo: Our neutrons should not get this cold,
            // but if they do, deal with it properly
            // for now, just return the smallest cross-section
            //System.out.println("Not enough data to linear interpolate");
            return data.get(0).area * Util.Physics.barn;
        }
        CSEntry e1 = data.get(index - 1); //the one with just lower energy
        CSEntry e2 = data.get(index);   //the one with just higher energy
        double area = e1.area + (((energy - e1.energy) / (e2.energy - e1.energy)) * (e2.area - e1.area)); //linear interpolation function

        // convert back into SI (cm) and return
        return area * Util.Physics.barn;
    }

    public XYChart.Series<String, Number> makeCSSeries(String seriesName) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<XYChart.Data<String, Number>> data = series.getData();
        series.setName(seriesName);
        boolean scatter = seriesName.equals("Scatter");
        boolean total = seriesName.equals("Total");

        for (double energy = 1e-3; energy < 1e7; energy *= 1.1) {
            DecimalFormat f = new DecimalFormat("0.##E0");
            String tick = f.format(energy);

            data.add(new XYChart.Data(tick, scatter
                    ? getScatterCrossSection(energy * Util.Physics.eV) / Util.Physics.barn
                    : (total ? getTotalCrossSection(energy * Util.Physics.eV) / Util.Physics.barn
                            : getCaptureCrossSection(energy * Util.Physics.eV) / Util.Physics.barn)));
        }

        return series;
    }
}
