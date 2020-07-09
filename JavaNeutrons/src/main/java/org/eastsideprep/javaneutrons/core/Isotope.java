package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class Isotope {

    private class CSEntry {

        double energy;
        double area;

        private CSEntry(double energy, double area) {
            this.energy = energy;
            this.area = area;
        }
    }

    private class AngEntry {

        double energy;
        int count;
        double[] cdf;
        double[] cos;

        private AngEntry(double e, int c, String cdf, String cos) {
            this.energy = e;
            this.count = c;
            try {
                this.cdf = Arrays.stream(cdf.substring(1, cdf.length() - 2).trim().split(" +"))
                        .mapToDouble(s -> Double.parseDouble(s + (s.endsWith(".") ? "0" : ""))).toArray();
                this.cos = Arrays.stream(cos.substring(1, cos.length() - 2).split(" +"))
                        .mapToDouble(s -> Double.parseDouble(s + (s.endsWith(".") ? "0" : ""))).toArray();
//                if (this.cdf.length != c || this.cos.length != c) {
//                    System.out.println("problem with angle line: " + count + " " + cdf + " " + cos);
//                }
            } catch (Exception ex) {
                System.out.println(" ex '" + e + "' " + ex.getMessage());
                System.out.println(" ex problem with angle line: " + count + " " + cdf + " " + cos);

                //ex.printStackTrace();
            }
        }

        private double lookupCos(double rand) {
            int bin = Arrays.binarySearch(cdf, rand);
            bin = bin < 0 ? -bin - 1 : bin;
            //System.out.println("rand "+rand+": bin is " + bin + " array " + Arrays.toString(this.cdf));
            //System.out.println("");
            
            // how far are we into the bucket, in terms of 0-1:
            double t = (rand - this.cdf[bin - 1]) / (this.cdf[bin] - this.cdf[bin - 1]);
            //System.out.println("rand "+rand+": t is "+t);
            
            // let's go that far into the cos bucket
            double cos = this.cos[bin - 1] + t * (this.cos[bin] - this.cos[bin - 1]);
            //System.out.println("rand "+rand+": cos is " + cos + " array " + Arrays.toString(this.cos));
            
            return cos;
        }
    }

    public static HashMap<String, Isotope> elements = new HashMap<>();

    public String name;
    public double mass; // g
    public int atomicNumber;
    protected int neutrons;

    private double[] energies;
    private double[] elastic;
    private double[] capture;
    private double[] total;
    private double[] angEnergies;

    public  ArrayList<AngEntry> angles;

    // for when you are too lazy to look up the correct mass
    public Isotope(String name, int atomicNumber, int neutrons) {
        this(name, atomicNumber, neutrons, atomicNumber * Util.Physics.protonMass + neutrons * Neutron.mass);
    }

    // use this when you know the mass in kg
    public Isotope(String name, int atomicNumber, int neutrons, double mass) {
        Isotope.elements.put(name, this);

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

    public static Isotope getByName(String name) {
        return Isotope.elements.get(name);
    }

    public double getScatterCrossSection(double energy) {
        //return getArea2(elasticEntries, energy);
        return getArea(energies, elastic, energy);
    }

    public double getCaptureCrossSection(double energy) {
        //return getArea2(captureEntries, energy);
        return getArea(energies, capture, energy);
    }

    public double getTotalCrossSection(double energy) {
        //return getArea2(totalEntries, energy);
        return getArea(energies, total, energy);
    }

    protected final void readDataFiles(int atomicNumber) {
        String filename = Integer.toString(atomicNumber * 1000 + atomicNumber + neutrons);
        fillEntries(filename);
        //fillAngleEntries(filename);
    }

    private void fillEntries(String fileName) {
        double epsilon = 0.1;

        // read xyz.csv from resources/data
        InputStream is = Isotope.class.getResourceAsStream("/data/ace/" + fileName + ".800nc.ace.csv");
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
            if (Math.abs(total - (scatter + capture)) > total * epsilon
                    && energy < 2.6e6) {
//                System.out.println("Element " + this.name + ", energy " + energy
//                        + ": inelastic events other than capture make up more than "
//                        + (int) (epsilon * 100) + " % of cs: "
//                        + Math.round(100 * Math.abs(total - (scatter + capture)) / total * 100) / 100 + " %");
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

        this.energies = newScatter.stream().mapToDouble(e -> e.energy).toArray();
        this.elastic = newScatter.stream().mapToDouble(e -> e.area).toArray();
        this.capture = newCapture.stream().mapToDouble(e -> e.area).toArray();
        this.total = newTotal.stream().mapToDouble(e -> e.area).toArray();
    }

    private void fillAngleEntries(String fileName) {
        double epsilon = 0.1;

        // read xyz.csv from resources/data
        InputStream is = Isotope.class.getResourceAsStream("/data/ace/" + fileName + ".800nc.ace_angle.csv");
        if (is == null) {
            System.out.println("angle Data file " + fileName + " not found for element " + this.name);
            System.out.println("Using isotropic scattering instead");
            return;
        }
        Scanner sc = new Scanner(is);
        sc.nextLine(); // skip header

        ArrayList<AngEntry> newAngles = new ArrayList<>(); //reset

        String line = null;
        try {
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                String[] split = line.split(",");
                double energy = Double.parseDouble(split[0]);
                int count = Integer.parseInt(split[1]);
                String cdf = split[2];
                String cos = split[3];
                newAngles.add(new AngEntry(energy, count, cdf, cos));
            }
        } catch (Exception ex) {
            System.out.println("ex " + ex);
        }

        this.angles = newAngles;

        this.angEnergies = newAngles.stream().mapToDouble(e -> e.energy).toArray();

    }
    
    public double getScatterCosTheta(double energy) {
            double cos_theta;
            double rand = ThreadLocalRandom.current().nextDouble();
            // locate energy bin
            int bin = Arrays.binarySearch(this.angEnergies, energy);
            if (bin < 0) {
                bin = -bin - 1;
                int binBelow = bin - 1;
                // compute how far we are into the energy bin
                double t = (energy - this.angEnergies[binBelow]) / (this.angEnergies[bin] - this.angEnergies[binBelow]);

                // get cos values for low and high bin, then interpolate
                double cos_theta_low = this.angles.get(binBelow).lookupCos(rand);
                double cos_theta_high = this.angles.get(bin).lookupCos(rand);
                // then interpolate
                cos_theta = cos_theta_high * t + cos_theta_low * (1 - t);
            } else {
                // found it exactly
                cos_theta = this.angles.get(bin).lookupCos(rand);
            }
            return cos_theta;
    }

   

    //
    // input: eV, output: barn
    //
    private double getArea(double energies[], double[] area, double energy) {
        //System.out.println("Energy: "+energy+" eV");
        int index = Arrays.binarySearch(energies, energy);
        if (index >= 0) {
            return area[index];
        }
        //else, linear interpolate between two nearest points
        index = -index - 1;
        if (index == 0 || index >= area.length) {
            // todo: Our neutrons should not get this cold,
            // but if they do, deal with it properly
            // for now, just return the smallest cross-section
            //System.out.println("Not enough data to linear interpolate");
            return area[0];
        }
        double resultArea = area[index - 1] + (((energy - energies[index - 1]) / (energies[index] - energies[index - 1]))
                * (area[index] - area[index - 1])); //linear interpolation function

        return resultArea;
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

            double value = scatter ? getScatterCrossSection(energy)
                    : (total ? getTotalCrossSection(energy)
                            : getCaptureCrossSection(energy));

            value = Math.log10(value);

            data.add(new XYChart.Data(tick, value));
        }

        return series;
    }
}
