package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class Nuclide {

    private class ValueEntry {

        double energy;
        double value;

        private ValueEntry(double energy, double area) {
            this.energy = energy;
            this.value = area;
        }
    }

    private class NeutronPhotonDistribution {

        double energy;
        ArrayList<DistributionLine> discrete;
        ArrayList<DistributionLine> continuous;

        NeutronPhotonDistribution(double e, ArrayList<DistributionLine> d, ArrayList<DistributionLine> c) {
            this.energy = e;
            this.discrete = d;
            this.continuous = c;
        }
    }

    private class DistributionEntry {

        double energy;
        int count;
        double[] pdf;
        double[] cdf;
        double[] value;

        private DistributionEntry(double e, int c, String pdf, String cdf, String cos) {
            this.energy = e;
            this.count = c;
            try {
                this.pdf = Arrays.stream(pdf.substring(1, pdf.length() - 2).trim().split(" +"))
                        .mapToDouble(s -> Double.parseDouble(s + (s.endsWith(".") ? "0" : ""))).toArray();
                this.cdf = Arrays.stream(cdf.substring(1, cdf.length() - 2).trim().split(" +"))
                        .mapToDouble(s -> Double.parseDouble(s + (s.endsWith(".") ? "0" : ""))).toArray();
                this.value = Arrays.stream(cos.substring(1, cos.length() - 2).split(" +"))
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
            double cos = this.value[bin - 1] + t * (this.value[bin] - this.value[bin - 1]);
            //System.out.println("rand "+rand+": cos is " + cos + " array " + Arrays.toString(this.cos));

            return cos;
        }

        private int findBin(double rand) {
            int bin = Arrays.binarySearch(cdf, rand);
            bin = bin < 0 ? -bin - 1 : bin;
            return bin - 1;
        }
    }

    private class DistributionLine {

        double pdf;
        double cdf;
        double value;

        private DistributionLine(double pdf, double cdf, double value) {
            this.pdf = pdf;
            this.cdf = cdf;
            this.value = value;
        }
    }

    public static HashMap<String, Nuclide> elements = new HashMap<>();

    public String name;
    public double mass; // g
    public int atomicNumber;
    protected int neutrons;

    private double[] energies;
    private double[] elastic;
    private double[] capture;
    private double[] total;
    private double[] angEnergies;

    private double[] yieldEnergies;
    private double[] yields;

    private double[] ppnEnergies[];
    private double[] pppEnergies[];
    private double[][] pppPDF[];
    private double[][] pppCDF[];

    public ArrayList<DistributionEntry> angles;

    // for when you are too lazy to look up the correct mass
    public Nuclide(String name, int atomicNumber, int neutrons) {
        this(name, atomicNumber, neutrons, atomicNumber * Util.Physics.protonMass + neutrons * Neutron.mass);
    }

    // use this when you know the mass in kg
    public Nuclide(String name, int atomicNumber, int neutrons, double mass) {
        Nuclide.elements.put(name, this);

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

    public static Nuclide getByName(String name) {
        return Nuclide.elements.get(name);
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
        readPhotonFile(filename);
    }

    private void fillEntries(String fileName) {
        double epsilon = 0.1;
        fileName = "/data/ace/" + fileName + ".800nc.ace.csv";
        // read xyz.csv from resources/data
        InputStream is = Nuclide.class.getResourceAsStream(fileName);
        if (is == null) {
            System.err.println("Data file " + fileName + " not found for element " + this.name);
            return;
        }
        Scanner sc = new Scanner(is);
        sc.nextLine(); // skip header

        ArrayList<ValueEntry> newScatter = new ArrayList<>(); //reset
        ArrayList<ValueEntry> newCapture = new ArrayList<>(); //reset
        ArrayList<ValueEntry> newTotal = new ArrayList<>(); //reset

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
            newScatter.add(new ValueEntry(energy, scatter));
            newCapture.add(new ValueEntry(energy, capture));
            newTotal.add(new ValueEntry(energy, total));
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
        this.elastic = newScatter.stream().mapToDouble(e -> e.value).toArray();
        this.capture = newCapture.stream().mapToDouble(e -> e.value).toArray();
        this.total = newTotal.stream().mapToDouble(e -> e.value).toArray();
    }

    private void fillAngleEntries(String fileName) {
        double epsilon = 0.1;

        // read xyz.csv from resources/data
        InputStream is = Nuclide.class.getResourceAsStream("/data/ace/" + fileName + ".800nc.ace_angle.csv");
        if (is == null) {
            System.out.println("angle Data file " + fileName + " not found for element " + this.name);
            System.out.println("Using isotropic scattering instead");
            return;
        }
        Scanner sc = new Scanner(is);
        sc.nextLine(); // skip header

        ArrayList<DistributionEntry> newAngles = new ArrayList<>(); //reset

        String line = null;
        try {
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                String[] split = line.split(",");
                double energy = Double.parseDouble(split[0]);
                int count = Integer.parseInt(split[1]);
                String pdf = split[2];
                String cdf = split[3];
                String cos = split[4];
                newAngles.add(new DistributionEntry(energy, count, pdf, cdf, cos));
            }
        } catch (Exception ex) {
            System.out.println("ex " + ex);
        }

        this.angles = newAngles;

        this.angEnergies = newAngles.stream().mapToDouble(e -> e.energy).toArray();

    }

    public double getScatterCosTheta(double energy) {
        double mu;
        // locate energy bin
        int binAbove = Arrays.binarySearch(this.angEnergies, energy);
        if (binAbove < 0) {
            binAbove = -binAbove - 1;
            int bin = binAbove - 1;
            // compute how far we are into the energy bin
            double f = (energy - this.angEnergies[bin]) / (this.angEnergies[binAbove] - this.angEnergies[bin]);
            double eta1 = Util.Math.random();
            int l = eta1 > f ? bin : binAbove;

            double eta2 = Util.Math.random();
            int j = this.angles.get(bin).findBin(eta2);

            // l = energy bin
            // j = cos bin
            // todo : mu = mu(l,j)+ (eta2-cdf(l,j))/p(l,j)
            //or the more complicated linear-linear scheme
            // the rest here is old
            // get cos values for low and high bin, then interpolate
            double cos_theta_low = this.angles.get(bin).lookupCos(eta1);
            double cos_theta_high = this.angles.get(binAbove).lookupCos(eta2);
            // then interpolate
            mu = cos_theta_high * f + cos_theta_low * (1 - f);
        } else {
            // found it exactly
            mu = this.angles.get(binAbove).lookupCos(Util.Math.random());
        }
        return mu;
    }

//    public Vector3D getRandomVeclocity(double energy, Vector3D other) {
//        do {
//            // get correctly distributed speed
//            getRandomSpeed(energy)
//        }
//        Vector3D v = Util.Math.randomDir(e.cos_theta, neutronSpeed);
//        // random vector was scattered around Z, rotate to match axis of incoming neutron
//        Rotation r = new Rotation(Vector3D.PLUS_K, neutronVelocity);
//        v = r.applyTo(v);
//
//        //System.out.println("v: " + v);
//        return v;
//
//    }
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

    void readPhotonFile(String fileName) {
        double epsilon = 0.1;
        String line;
        String word;
        int number;
        int l = 0;

        // read xyz.csv from resources/data
        InputStream is = Nuclide.class.getResourceAsStream("/data/ace/" + fileName + ".800nc.txt");
        if (is == null) {
            System.out.println("Photon Data file " + fileName + " not found for element " + this.name);
            System.out.println("Using isotropic scattering instead");
            return;
        }
        Scanner sc = new Scanner(is);
        sc.nextLine(); // skip "Reading data from ..."
        sc.nextLine(); // skip ""
        sc.nextLine(); // skip "MT 102 Photon report for ..."
        sc.nextLine(); // skip ""
        line = sc.nextLine();
        l += 5;
        if (!line.endsWith(" photon distributions follow")) {
            throw new IllegalArgumentException("Number of distributions missing in line "+l);
        }
        word = line.split(" ")[0];
        number = Integer.parseInt(word);

        for (int n = 0; n < number; n++) {
            line = sc.nextLine(); // "Yield Interpolations"
            l++;
            // todo: interpret interpolation sections
            // assert "     1     2  (linear-linear)"
            line = sc.nextLine().trim();
            l++;
            if (line.equals(angles)) {
                if (!line.split(" +")[1].equals("2")) {
                    throw new IllegalArgumentException("Photon yield interpolation not linear-linear in " + fileName +" line "+l);
                }
            }

            sc.nextLine(); // skip " n-Energy      Yield"
            l++;

            ArrayList<ValueEntry> newYields = new ArrayList<>(); //reset

            try {
                while (sc.hasNextLine()) {
                    line = sc.nextLine().trim();
                    l++;
                    if (line.startsWith("Distribution Law: ")) {
                        break;
                    }
                    String[] split = line.split(" +");
                    double energy = Double.parseDouble(split[0]);
                    double yield = Double.parseDouble(split[1]);
                    newYields.add(new ValueEntry(energy, yield));
                }
            } catch (Exception ex) {
                System.out.println("ex " + ex);
            }
            // todo : record distribution law (in "line" at this point)

            line = sc.nextLine();
            l++;
            if (!line.startsWith("Neutron Energy Interpolation:")) {
                throw new IllegalArgumentException("Neutron energy interpolation expected in " + fileName +" line "+l);
            }

            line = sc.nextLine().trim(); // assert "     1     2  (linear-linear)"
            l++;
            if (!line.split(" +")[1].equals("2")) {
                // todo: accept other interpolations
                throw new IllegalArgumentException("Neutron energy interpolation not linear-linear in " + fileName +" line "+l);
            }

            // parse number of neutron energy photon distribution tables
            line = sc.nextLine();
            l++;
            line = line.substring(0, line.indexOf(" "));
            int numEnergies = Integer.parseInt(line);

            ArrayList<NeutronPhotonDistribution> npds = new ArrayList<>();
            // parse neutron energy in MeV
            line = sc.nextLine();
            l++;
            try {
                for (int iNE = 0; iNE < numEnergies; iNE++) {
                    line = line.substring("Neutron E = ".length());
                    line = line.substring(0, line.indexOf(" "));
                    double neutronEnergy = Double.parseDouble(line.trim());

                    line = sc.nextLine(); // assert "Distribution interpolation:   1  (histogram) (or 0)"
                    line = line.substring("Distribution interpolation:".length()).trim();
                    line = line.substring(0, line.indexOf(" ")).trim();
                    if (!line.equals("1") && !line.equals("0")) {
                        throw new IllegalArgumentException("Photon energy interpolation not 0 or histogram in " + fileName +" line "+l);
                    }

                    line = sc.nextLine();
                    line = line.substring("Number of Discrete Energies: ".length());
                    int numDiscretes = Integer.parseInt(line.trim());
                    sc.nextLine(); // skip "   EOUT           PDF              CDF"
                    l++;

                    ArrayList<DistributionLine> discretePhotonDistributions = new ArrayList<>();
                    ArrayList<DistributionLine> continuousPhotonDistributions = new ArrayList<>();

                    for (int iND = 0; iND < numDiscretes; iND++) {
                        line = sc.nextLine().trim();
                        if (line.equals("")) {
                            break;
                        }
                        String[] split = line.split(" +");
                        double energy = Double.parseDouble(split[0]);
                        double pdf = Double.parseDouble(split[1]);
                        double cdf = Double.parseDouble(split[2]);
                        discretePhotonDistributions.add(new DistributionLine(pdf, cdf, energy));
                    }
                    if (sc.hasNextLine()) {
                        line = sc.nextLine().trim(); // skip blank line
                        l++;
                        if (!line.startsWith("Neutron E = ")) {

                            while (sc.hasNext()) {
                                line = sc.nextLine().trim();
                                l++;
                                if (line.startsWith("Neutron E = ")) {
                                    break;
                                }
                                String[] split = line.split(" +");
                                double energy = Double.parseDouble(split[0]);
                                double pdf = Double.parseDouble(split[1]);
                                double cdf = Double.parseDouble(split[2]);
                                continuousPhotonDistributions.add(new DistributionLine(pdf, cdf, energy));
                            }
                        }
                    }

                    npds.add(new NeutronPhotonDistribution(neutronEnergy, discretePhotonDistributions, continuousPhotonDistributions));
                }

                /*
            System.out.println("Nuclide: " + this.name);
            System.out.println("  Yields: ");
            for (ValueEntry y : newYields) {
                System.out.println("    E: " + y.energy + ", yield: " + y.value);
            }
            System.out.println("");
            System.out.println("  Photon energy distributions:");
            for (NeutronPhotonDistribution npd : npds) {
                System.out.println("    Neutron energy: " + npd.energy);
                if (npd.discrete.size() > 0) {
                    System.out.println("      Discrete photon energies:");
                    for (DistributionLine dl : npd.discrete) {
                        System.out.println("      E photon: " + dl.value + ", pdf: " + dl.pdf + ", cdf: " + dl.cdf);
                    }
                }
                if (npd.continuous.size() > 0) {
                    System.out.println("      Continuous photon energies:");
                    for (DistributionLine dl : npd.continuous) {
                        System.out.println("      E photon: " + dl.value + ", pdf: " + dl.pdf + ", cdf: " + dl.cdf);
                    }
                }
            }
            System.out.println("");
                 */
            } catch (Exception ex) {
                System.out.println("readPhotonFile exception: " + ex);
                ex.printStackTrace();
            }
        }
        //this.yieldEnergies = 
        System.out.println("");

    }

}
