package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class Nuclide {

    private class ValueEntry {

        double energy;
        double value;

        private ValueEntry(double energy, double v) {
            this.energy = energy;
            this.value = v;
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

    private class NeutronPhotonDistribution {

        double energy;
        String photonLaw;
        ArrayList<DistributionLine> discrete;
        ArrayList<DistributionLine> continuous;

        NeutronPhotonDistribution(double e, String law, ArrayList<DistributionLine> d, ArrayList<DistributionLine> c) {
            this.energy = e;
            this.discrete = d;
            this.continuous = c;
            this.photonLaw = law;
        }
    }

    private abstract class PhotonData {
    }

    private class PhotonData2 extends PhotonData {

        double ePhoton;

        PhotonData2(double e) {
            ePhoton = e;
        }
    }

    private class PhotonData2a extends PhotonData {

        double ePhoton;
        double eIn;

        PhotonData2a(double e, double eIn) {
            this.ePhoton = e;
            this.eIn = eIn;
        }
    }

    private class PhotonDataTable extends PhotonData {

        String interpolationLaw;
        List<NeutronPhotonDistribution> npds;

        PhotonDataTable(String law, List<NeutronPhotonDistribution> npds) {
            this.interpolationLaw = law;
            this.npds = npds;
        }
    }

    private class PhotonDistribution {

        String prodDistLaw;
        String yieldLaw;
        List<ValueEntry> yields;
        PhotonData data;

        PhotonDistribution(String distLaw, String yieldLaw, List<ValueEntry> yields, PhotonData data) {
            this.prodDistLaw = distLaw;
            this.yieldLaw = yieldLaw;
            this.yields = yields;
            this.data = data;
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

    // actual Nuclide member variables
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
    public List<PhotonDistribution> pDistList;

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

        // some info at https://permalink.lanl.gov/object/tr?what=info:lanl-repo/lareport/LA-UR-19-29016
        String[] knownYieldInterpolationLaws = new String[]{"1", "2", "5"}; // histogram, linear-linear, log-log
        String[] knownNeutronEnergyInterpolationLaws = new String[]{"2", "22"}; // linear-linear, ??
        String[] knownPhotonEnergyDescriptionLaws = new String[]{"2", "2a", "4"}; // single value, EIn+offset, tabular
        String[] knownPhotonEnergyInterpolationLaws = new String[]{"0", "1", "2"}; // ?, histogram, linear-linear

        // read xyz.csv from resources/data
        fileName = "/data/ace/" + fileName + ".800nc.txt";
        InputStream is = Nuclide.class.getResourceAsStream(fileName);
        if (is == null) {
            System.out.println("Photon Data file " + fileName + " not found for element " + this.name);
            return;
        }
        SimpleParser sp = new SimpleParser(fileName, is);

        // read header
        sp.assertAndSkipLineStart("Reading data from");
        sp.skipLine();
        sp.assertAndSkipLineStart("MT 102 Photon report for");
        sp.skipLine();

        // number of photon distributions, then loop over them
        int pDists = sp.getInteger("$i photon distributions follow");
        ArrayList<PhotonDistribution> pDistList = new ArrayList<>();

        for (int n = 0; n < pDists; n++) {
            // get the number of yield interpolation sections and interpolation laws
            int yieldSections = sp.getInteger("Yield Interpolations *$i lines");
            // todo: multiple yield interpolation sections
            sp.assertEqual(yieldSections, new Integer[]{1}, "Cannot handle multiple yield sections yet");

            // for section 1 only
            int sectionStart = sp.getInteger(" *$i *([0-9]*a?) *(.*)"); // $i is section start, group 2 is law, group 3 is friendly name
            String yieldInterpolationLaw = sp.getString(2);
            sp.assertEqual(yieldInterpolationLaw, knownYieldInterpolationLaws, "Photon yield interpolation not known");
            //System.out.println("Yield dist " + yieldInterpolationLaw);
            // get number of energy buckets
            int eBuckets = sp.getInteger(" n-Energy      Yield *$i lines");
            ArrayList<ValueEntry> newYields = new ArrayList<>(); //reset

            for (int j = 0; j < eBuckets; j++) {
                double energy = sp.getDouble();
                double yield = sp.getDouble(2);
                newYields.add(new ValueEntry(energy, yield));
            }

            String distLaw = sp.getString("Distribution Law: (.*)");
            sp.assertEqual(distLaw, knownPhotonEnergyDescriptionLaws, "Photon energy description law not known");
            //System.out.println("Photon energy method law " + distLaw);
            ArrayList<NeutronPhotonDistribution> npds;
            PhotonData data;

            //System.out.println(distLaw);
            switch (distLaw) {
                case "2":
                    double e = sp.getDouble("Photon energy $d");
                    data = new PhotonData2(e);
                    break;
                case "2a":
                    double e2a1 = sp.getDouble("Photon energy $d \\+ $d Ein");
                    double e2a2 = sp.getDouble(2);
                    data = new PhotonData2a(e2a1, e2a2);
                    break;
                case "0":
                case "1":
                case "4":
                case "22":
                case "5":
                default:
                    // read the interpolation sections
                    int interpolationSections = sp.getInteger("Neutron Energy Interpolation: *$i lines");
                    // todo: handle multiple sections
                    sp.assertEqual(interpolationSections, new Integer[]{1}, "Cannot handle multiple interpolation sections yet");
                    sectionStart = sp.getInteger(" *$i *$i *(.*)");
                    String neInterpolationLaw = sp.getString(2);
                    sp.assertEqual(neInterpolationLaw, knownNeutronEnergyInterpolationLaws, "Neutron energy interpolation not known");
                    //System.out.println("NE dist " + neInterpolationLaw);
                    // parse number of neutron energy photon distribution tables
                    int numEnergies = sp.getInteger("$i neutron energy bins and distributions follow");
                    npds = new ArrayList<>();
                    // parse neutron energy in MeV
                    for (int iNE = 0; iNE < numEnergies; iNE++) {
                        double neutronEnergy = sp.getDouble("Neutron E = $d *\\(");

                        String distIntLaw = sp.getString("Distribution interpolation: *([^ ]*) *\\(");
                        sp.assertEqual(distIntLaw, knownPhotonEnergyInterpolationLaws, "Distribution interpolation not known");
                        //System.out.println("PE dist " + distIntLaw);
                        int discreteEnergies = sp.getInteger("Number of Discrete Energies: *$i");
                        int totalEnergies = sp.getInteger("   EOUT *PDF *CDF *$i lines");

                        ArrayList<DistributionLine> discretePhotonDistributions = new ArrayList<>();
                        ArrayList<DistributionLine> continuousPhotonDistributions = new ArrayList<>();

                        for (int iND = 0; iND < totalEnergies; iND++) {
                            double energy = sp.getDouble();
                            double pdf = sp.getDouble(2);
                            double cdf = sp.getDouble(3);
                            if (iND >= discreteEnergies) {
                                continuousPhotonDistributions.add(new DistributionLine(pdf, cdf, energy));
                            } else {
                                discretePhotonDistributions.add(new DistributionLine(pdf, cdf, energy));
                            }
                        }

                        npds.add(new NeutronPhotonDistribution(neutronEnergy, distIntLaw, discretePhotonDistributions, continuousPhotonDistributions));
                    }
                    data = new PhotonDataTable(neInterpolationLaw, npds);
                    break;
            }
            PhotonDistribution pdist = new PhotonDistribution(distLaw, yieldInterpolationLaw, newYields, data);
            pDistList.add(pdist);
        }
        this.pDistList = pDistList;
    }

    public void printPhotonData() {
        System.out.println("Nuclide: " + this.name);
        for (PhotonDistribution pdist : this.pDistList) {
            System.out.println("<Start of photon distribution>");
            System.out.println("  Yield distribution law: " + pdist.yieldLaw);
            System.out.println("  Yields: ");
            for (ValueEntry y : pdist.yields) {
                System.out.println("    E: " + y.energy + ", yield: " + y.value);
            }

            System.out.println("  Production distribution law: " + pdist.prodDistLaw);
            switch (pdist.prodDistLaw) {
                case "2":
                    System.out.println("  (law == constant");
                    System.out.println("  Photon energy " + ((PhotonData2) pdist.data).ePhoton);
                    break;

                case "2a":
                    System.out.println("  (law == offset from eIn");
                    System.out.println("  Photon energy " + ((PhotonData2a) pdist.data).ePhoton + " EIn " + ((PhotonData2a) pdist.data).eIn);
                    break;

                default:
                    System.out.println("  (law == function of eIn");
                    System.out.println("  Photon energy distributions:");
                    for (NeutronPhotonDistribution npd : ((PhotonDataTable) pdist.data).npds) {
                        System.out.println("    Neutron energy: " + npd.energy);
                        System.out.println("      Photon energy distribution law: " + npd.photonLaw);
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
                    break;
            }
            System.out.println("<End of photon distribution>");
        }
        System.out.println("");
    }

}
