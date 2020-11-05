package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Nuclide {

    private static class ValueEntry implements Comparable<ValueEntry> {

        double energy;
        double value;

        private ValueEntry(double energy, double v) {
            this.energy = energy;
            this.value = v;
        }

        @Override
        public int compareTo(ValueEntry o) {
            ValueEntry other = (ValueEntry) o;
            return (int) Math.signum(this.energy - other.energy);
        }
    }

    private static class DistributionLine implements Comparable<DistributionLine> {

        double pdf;
        double cdf;
        double value;

        private DistributionLine(double pdf, double cdf, double value) {
            this.pdf = pdf;
            this.cdf = cdf;
            this.value = value;
        }

        private DistributionLine(double cdf) {
            // for search purposes only
            this.cdf = cdf;
        }

        @Override
        public int compareTo(DistributionLine o) {
            DistributionLine other = (DistributionLine) o;
            return (int) Math.signum(this.cdf - o.cdf);
        }

    }

    abstract private static class NeutronPhotonDistribution implements Comparable<NeutronPhotonDistribution> {

        double energy;
        String photonInterpolationLaw;  //{"0", "1", "2"}; ?, histogram, linear-linear

        double smallestContinuousCDF;
        ArrayList<DistributionLine> dist;

        NeutronPhotonDistribution(String law, double e) {
            this(e, law, null, null);
        }

        NeutronPhotonDistribution(double e, String law, ArrayList<DistributionLine> d, ArrayList<DistributionLine> c) {
            this.energy = e;
            this.dist = d;
            if (c != null) {
                this.dist.addAll(c);
                this.smallestContinuousCDF = (c.size() > 0 ? c.get(c.size() - 1).cdf : 1);
            }
            this.photonInterpolationLaw = law;
        }

        abstract double sampleContinuousPhotonEnergy(double eta);

        double samplePhotonEnergy() {
            double eta = Util.Math.random();
            if (eta >= smallestContinuousCDF) {
                // interpolate by scheme
                return sampleContinuousPhotonEnergy(eta);
            } else {
                // discrete energy, just return the nearest smaller discrete energy
                int index = Collections.binarySearch(dist, new DistributionLine(eta));
                index = index < 0 ? -index - 1 : index;

                return dist.get(index).value;
            }
        }

        NeutronPhotonDistribution interpolate(NeutronPhotonDistribution other, double eNeutron) {
            NeutronPhotonDistribution result;
            if (this instanceof NeutronPhotonDistribution1) {
                result = new NeutronPhotonDistribution1(this.photonInterpolationLaw, eNeutron);
            } else {
                result = new NeutronPhotonDistribution2(this.photonInterpolationLaw, eNeutron);
            }

            // list merge, interpolating CDF as we go
            ArrayList<DistributionLine> cont = new ArrayList<>();
            ArrayList<DistributionLine> left = this.dist;
            ArrayList<DistributionLine> right = other.dist;
            int size = Math.min(left.size(), right.size());
            for (int i = 0; i < size; i++) {
                cont.add(new DistributionLine(
                        0, // don't need pdf
                        Util.Math.interpolateLinearLinear(this.energy, left.get(i).cdf, other.energy, right.get(i).cdf, eNeutron),
                        left.get(i).value // should be equal values
                ));
            }
            // merge remaining tail
            if (left.size() > right.size()) {
                cont.addAll(left.subList(right.size(), left.size()));
            } else if (right.size() > left.size()) {
                cont.addAll(right.subList(left.size(), right.size()));
            }
            result.dist = cont;
            return result;
        }

        @Override
        public int compareTo(NeutronPhotonDistribution o) {
            return (int) Math.signum(this.energy - o.energy);
        }
    }

    // class for bSearch only
    private static class NeutronPhotonDistributionKey extends NeutronPhotonDistribution {

        NeutronPhotonDistributionKey(double e) {
            super (e, "", null, null);
        }

        @Override
        double sampleContinuousPhotonEnergy(double eta) {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    // for histogram photon energy interpolation
    private static class NeutronPhotonDistribution1 extends NeutronPhotonDistribution {

        NeutronPhotonDistribution1(double e, String law, ArrayList<DistributionLine> d, ArrayList<DistributionLine> c) {
            super(e, law, d, c);
        }

        NeutronPhotonDistribution1(String law, double e) {
            super(law, e);
        }

        @Override
        double sampleContinuousPhotonEnergy(double eta) {
            // hist: just return the nearest smaller discrete energy
            int index = Collections.binarySearch(dist, new DistributionLine(eta));
            index = index < 0 ? -index - 1 : index;
            return dist.get(index).value;
        }

    }

    // for linear-linear photon energy interpolation
    private static class NeutronPhotonDistribution2 extends NeutronPhotonDistribution {

        NeutronPhotonDistribution2(double e, String law, ArrayList<DistributionLine> d, ArrayList<DistributionLine> c) {
            super(e, law, d, c);
        }

        NeutronPhotonDistribution2(String law, double e) {
            super(law, e);
        }

        @Override
        double sampleContinuousPhotonEnergy(double eta) {
            // lin-lin
            int index = Collections.binarySearch(dist, new DistributionLine(eta));
            index = index < 0 ? -index - 1 : index;
            DistributionLine left = dist.get(index);
            DistributionLine right = dist.get(index + 1);
            return Util.Math.inverseInterpolateLinearLinear(left.value, left.cdf, right.value, right.cdf, eta);
        }

    }

    private abstract class PhotonData {

        abstract double getEnergy(double eNeutron);
    }

    private class PhotonData2 extends PhotonData {

        double ePhoton;

        PhotonData2(double e) {
            ePhoton = e;
        }

        @Override
        double getEnergy(double eNeutron) {
            return ePhoton;
        }
    }

    private class PhotonData2a extends PhotonData {

        double c0;
        double c1;

        PhotonData2a(double e, double eInCoef) {
            this.c0 = e;
            this.c1 = eInCoef;
        }

        @Override
        double getEnergy(double eNeutron) {
            return c0 + c1 * eNeutron;
        }
    }

    private class PhotonDataTable extends PhotonData {

        String interpolationLaw;
        List<NeutronPhotonDistribution> npds;

        PhotonDataTable(String law, List<NeutronPhotonDistribution> npds) {
            this.interpolationLaw = law;
            this.npds = npds;
        }

        @Override
        double getEnergy(double eNeutron) {
            int index = Collections.binarySearch(this.npds, new NeutronPhotonDistributionKey(eNeutron));
            NeutronPhotonDistribution npd;
            switch (this.interpolationLaw) {
                case "2":
                    // make an interpolated distribution for this neutron energy
                    npd = npds.get(index).interpolate(npds.get(index + 1), eNeutron);
                    break;
                case "22":
                default:
                    npd = npds.get(index);
                    break;
            }

            // sample a photon energy from the chosen distribution
            return npd.sampleContinuousPhotonEnergy(Util.Math.random());
        }
    }

    private class PhotonDistribution {

        String prodDistLaw;
        String yieldInterpolationLaw;
        List<ValueEntry> yields;
        PhotonData data;

        PhotonDistribution(String distLaw, String yieldInterpolationLaw, List<ValueEntry> yields, PhotonData data) {
            this.prodDistLaw = distLaw;
            this.yieldInterpolationLaw = yieldInterpolationLaw;
            this.yields = yields;
            this.data = data;
        }

        private List<Gamma> yieldGammas(Vector3D position, double yield) {
            Gamma g = null;
            LinkedList<Gamma> result = new LinkedList<>();

            // depending on the fractional value, add one more
            if (Util.Math.random() < (yield - Math.floor(yield))) {
                yield += 1;
            }

            // produce that many neutrons
            for (int i = 0; i < Math.floor(yield); i++) {
                g = new Gamma(position);
                result.add(g);
            }
            return result;
        }

        private List<Gamma> generateGammas(Vector3D position, double eNeutron) {
            // interpolates yield, calls yieldGammas()
            int index = Collections.binarySearch(this.yields, new ValueEntry(eNeutron, 0));
            double yield = 0;
            ValueEntry yield1;
            ValueEntry yield2;
            //String[] knownYieldInterpolationLaws = new String[]{"1", "2", "5"}; // histogram, linear-linear, log-log
            switch (yieldInterpolationLaw) {
                case "1":
                    // hist
                    yield = yields.get(index).value;
                    break;
                case "2":
                    //lin-lin
                    yield1 = yields.get(index);
                    yield2 = yields.get(index);
                    yield = Util.Math.interpolateLinearLinear(yield1.energy, yield1.value, yield2.energy, yield2.value, eNeutron);
                    break;
                case "5":
                    // log-log
                    yield1 = yields.get(index);
                    yield2 = yields.get(index);
                    yield = Util.Math.interpolateLogLog(yield1.energy, yield1.value, yield2.energy, yield2.value, eNeutron);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown interpolation law " + yieldInterpolationLaw);
            }
            List<Gamma> result = yieldGammas(position, yield);

            //String[] knownPhotonEnergyDistributionLaws = new String[]{"2", "2a", "4"}; // single value, c0 + c1*EIn, tabular
            // these laws are already encoded here as subclasses of PhotonData
            for (int i = 0; i < result.size(); i++) {
                Gamma g = result.get(i);
                g.setDirectionAndEnergy(Util.Math.randomDir(), data.getEnergy(eNeutron));
            }
            return result;
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
        InputStream is = Nuclide.class
                .getResourceAsStream(fileName);
        if (is
                == null) {
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

        Collections.sort(newScatter,
                (a, b) -> {
                    return (int) Math.signum(a.energy - b.energy);
                }
        );
        Collections.sort(newCapture,
                (a, b) -> {
                    return (int) Math.signum(a.energy - b.energy);
                }
        );
        Collections.sort(newTotal,
                (a, b) -> {
                    return (int) Math.signum(a.energy - b.energy);
                }
        );

        this.energies = newScatter.stream().mapToDouble(e -> e.energy).toArray();

        this.elastic = newScatter.stream().mapToDouble(e -> e.value).toArray();

        this.capture = newCapture.stream().mapToDouble(e -> e.value).toArray();

        this.total = newTotal.stream().mapToDouble(e -> e.value).toArray();
    }

 
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

    List<Gamma> generateGammasForCapture(Vector3D position, double eNeutron) {
        LinkedList<Gamma> list = new LinkedList();
        
        for (PhotonDistribution dist:this.pDistList) {
            list.addAll(dist.generateGammas(position, eNeutron));
        }

        return list;
    }

    void readPhotonFile(String fileName) {
        double epsilon = 0.1;
        String line;
        String word;
        int number;

        // some info at https://permalink.lanl.gov/object/tr?what=info:lanl-repo/lareport/LA-UR-19-29016
        String[] knownYieldInterpolationLaws = new String[]{"1", "2", "5"}; // histogram, linear-linear, log-log
        String[] knownNeutronEnergyInterpolationLaws = new String[]{"2", "22"}; // linear-linear, unit-base linear-linear which we will treat like a histogram
        String[] knownPhotonEnergyDistributionLaws = new String[]{"2", "2a", "4"}; // single value, EIn+offset, tabular
        String[] knownPhotonEnergyInterpolationLaws = new String[]{"0", "1", "2"}; // ?, histogram, linear-linear

        // read xyz.csv from resources/data
        fileName = "/data/ace/" + fileName + ".800nc.txt";
        InputStream is = Nuclide.class
                .getResourceAsStream(fileName);
        if (is
                == null) {
            System.out.println("Photon Data file " + fileName + " not found for element " + this.name);
            return;
        }
        SimpleParser sp = new SimpleParser(fileName, is);

        // read header
        sp.assertAndSkipLineStart(
                "Reading data from");
        sp.skipLine();

        sp.assertAndSkipLineStart(
                "MT 102 Photon report for");
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
            sp.assertEqual(distLaw, knownPhotonEnergyDistributionLaws, "Photon energy distribution law not known");
            //System.out.println("Photon energy distribution law " + distLaw);
            ArrayList<NeutronPhotonDistribution> npds;
            PhotonData data = null;

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
                case "4":
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

                        switch (distIntLaw) {
                            case "0": // todo: probably. but really?
                            case "1":
                                npds.add(new NeutronPhotonDistribution1(neutronEnergy, distIntLaw, discretePhotonDistributions, continuousPhotonDistributions));
                                break;
                            case "2":
                                npds.add(new NeutronPhotonDistribution1(neutronEnergy, distIntLaw, discretePhotonDistributions, continuousPhotonDistributions));
                                break;
                        }

                    }
                    data = new PhotonDataTable(neInterpolationLaw, npds);
                    break;
                default:
                    sp.error("We should not be here, we checked.");
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
            System.out.println("  Yield distribution law: " + pdist.yieldInterpolationLaw);
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
                    System.out.println("  Photon energy " + ((PhotonData2a) pdist.data).c0 + " + " + ((PhotonData2a) pdist.data).c1+" eIn");
                    break;

                default:
                    System.out.println("  (law == function of eIn");
                    System.out.println("  Photon energy distributions:");
                    for (NeutronPhotonDistribution npd : ((PhotonDataTable) pdist.data).npds) {
                        System.out.println("    Neutron energy: " + npd.energy);
                        System.out.println("      Photon energy distribution law: " + npd.photonInterpolationLaw);
                        System.out.println("      Smallest continuous CDF: "+npd.smallestContinuousCDF);
                        if (npd.dist.size() > 0) {
                            System.out.println("      Photon energies:");
                            for (DistributionLine dl : npd.dist) {
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
