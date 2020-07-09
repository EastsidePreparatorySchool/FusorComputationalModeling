package org.eastsideprep.javaneutrons.core;

import java.text.DecimalFormat;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.StringConverter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.TestGM;
import org.eastsideprep.javaneutrons.materials.Air;

public class MonteCarloSimulation {

    static boolean parallel = true;

    private class NeutronCollection extends AbstractCollection<Neutron> {

        int count;
        int produced;
        Vector3D position;
        Vector3D direction;
        double energy;
        MonteCarloSimulation mcs;

        private class NeutronIterator implements Iterator<Neutron> {

            @Override
            synchronized public boolean hasNext() {
                return produced < count || count == 0;
            }

            @Override
            synchronized public Neutron next() {
                produced++;
                return new Neutron(position, direction == null ? Util.Math.randomDir() : direction, energy, mcs);
            }

        }

        NeutronCollection(int count, Vector3D position, Vector3D direction, double energy, MonteCarloSimulation mcs) {
            this.count = count;
            this.produced = 0;
            this.position = position;
            this.direction = direction;
            this.energy = energy;
            this.mcs = mcs;
        }

        @Override
        public Iterator<Neutron> iterator() {
            return new NeutronIterator();
        }

        @Override
        public int size() {
            return count;
        }

    }

    public interface ProgressLambda {

        void reportProgress(int p);
    }

    public final Assembly assembly;
    private final Vector3D origin;
    private Vector3D direction;
    private final AtomicLong completed;
    public final LinkedTransferQueue visualizations;
    private final Group viewGroup;
    private final Group dynamicGroup;
    private Air air;
    public long lastCount;
    private int visualObjectLimit;
    private long start;
    public Material interstitialMaterial;
    public Material initialMaterial;
    public double initialEnergy;
    public int traceLevel;
    public boolean scatter;
    public String lastChartData = "";

    public static boolean visualLimitReached = false;

    public MonteCarloSimulation(Assembly assembly, Vector3D origin, Group g) {
        this(assembly, origin, null, Neutron.startingEnergyDD, null, null, g);
    }

    public MonteCarloSimulation(Assembly assembly, Vector3D origin, Vector3D direction, double initialEnergy,
            Object interstitialMaterial, Object initialMaterial, Group g) {
        interstitialMaterial = Material.getRealMaterial(interstitialMaterial);
        initialMaterial = Material.getRealMaterial(initialMaterial);

        this.assembly = assembly;
        this.origin = origin == null ? Vector3D.ZERO : origin;
        this.visualizations = new LinkedTransferQueue<Node>();
        this.completed = new AtomicLong(0);
        this.viewGroup = g;
        this.dynamicGroup = new Group();
        if (this.viewGroup != null) {
            this.viewGroup.getChildren().clear();
            // make some axes
            Util.Graphics.drawCoordSystem(g);
            // add the assembly objects
            this.viewGroup.getChildren().add(this.assembly.getGroup());
            // and a group for event visualiations
            this.viewGroup.getChildren().add(dynamicGroup);
        }

        this.interstitialMaterial = (Material) interstitialMaterial;
        this.initialMaterial = (Material) initialMaterial;
        this.initialEnergy = initialEnergy == 0 ? Neutron.startingEnergyDD : initialEnergy;
        this.direction = direction;

        if (this.interstitialMaterial == null) {
            this.interstitialMaterial = Air.getInstance("Interstitial air");
        }

        if (this.assembly == null) {
            return;
        }

        if (this.initialMaterial == null) {
            // todo : find initial material from origin
            Event e = this.assembly.rayIntersect(this.origin, Vector3D.PLUS_I, false, visualizations);
            if (e != null && e.code == Event.Code.Entry) {
                this.initialMaterial = e.part.shape.getContactMaterial(e.face);
                if (this.initialMaterial != null) {
                    System.out.println("Determined initial medium to be " + this.initialMaterial.name);
                }
            }
            if (this.initialMaterial == null) {
                this.initialMaterial = this.interstitialMaterial;
                System.out.println("No initial medium found, defaulting to " + this.initialMaterial.name);
            }
        }
    }

//    public void checkTallies() {
//        double totalNeutronPath = this.neutrons.stream().mapToDouble(n -> n.totalPath).sum();
//        double totalPartsPath = this.assembly.getParts().stream().mapToDouble(p -> p.getTotalPath()).sum();
//        Set<Material> materials = this.assembly.getMaterials();
//        double totalMaterialsPath = materials.stream().mapToDouble(m -> m == null ? 0.0 : m.totalFreePath).sum();
//        Set<Material> interstitials = this.assembly.getContainedMaterials();
//        interstitials.add(initialMaterial);
//        interstitials.add(interstitialMaterial);
//        double totalInterstitialsPath = interstitials.stream().mapToDouble(m -> m.totalFreePath).sum();
//
//        System.out.println("Total neutron path: " + String.format("%6.3e", totalNeutronPath));
//        System.out.println("Total parts path: " + String.format("%6.3e", totalPartsPath));
//        System.out.println("Total materials path: " + String.format("%6.3e", totalMaterialsPath));
//        System.out.println("Total interstitials path: " + String.format("%6.3e", totalInterstitialsPath));
//        System.out.println("");
//        System.out.println("n: " + String.format("%10.7e", totalNeutronPath) + " = p+i: " + String.format("%10.7e", totalPartsPath + totalInterstitialsPath));
//        System.out.println("n: " + String.format("%10.7e", totalNeutronPath) + " = m+i: " + String.format("%10.7e", totalMaterialsPath + totalInterstitialsPath));
//    }
// this will be called from UI thread
    public long update() {
        //viewGroup.getChildren().remove(this.dynamicGroup);
        int size = this.dynamicGroup.getChildren().size();
        if (size < this.visualObjectLimit) {
            this.visualizations.drainTo(this.dynamicGroup.getChildren(), this.visualObjectLimit - size);
        } else {
            MonteCarloSimulation.visualLimitReached = true;
        }

        // drain the rest and forget about it
        LinkedList<Node> list = new LinkedList<>();
        this.visualizations.drainTo(list);

        return completed.get();
    }

    public void clearVisuals() {
        this.viewGroup.getChildren().remove(this.dynamicGroup);
        this.viewGroup.getChildren().remove(this.assembly.getGroup());
    }

    public void simulateNeutrons(int count, int visualObjectLimit, boolean textTrace) {
        this.lastCount = count;
        this.traceLevel = count <= 10 ? (1+(textTrace?1:0)):0;
        this.visualObjectLimit = visualObjectLimit;
        MonteCarloSimulation.visualLimitReached = false;

        this.viewGroup.getChildren().remove(this.dynamicGroup);
        this.dynamicGroup.getChildren().clear();
        this.viewGroup.getChildren().add(this.dynamicGroup);

        System.out.println("");
        System.out.println("");
        System.out.println("Running new MC simulation for " + count + " neutrons ...");

        this.start = System.currentTimeMillis();

        assembly.resetDetectors();
        Collection<Material> c = Material.materials.values();
        c.stream().forEach(m -> m.resetDetector());

        // and enviroment (will count escaped neutrons)
        Environment.getInstance().reset();

        NeutronCollection neutrons = new NeutronCollection(count, this.origin, this.direction, this.initialEnergy, this);
        if (count > 0) {
            if (!MonteCarloSimulation.parallel || this.lastCount < 10) {
                neutrons.stream().forEach(n -> simulateNeutron(n));
            } else {
                for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                    new Thread(() -> {
                        for (Neutron n : neutrons) {
                            simulateNeutron(n);
                        }
                    }).start();
                }
            }
        } else {
            this.scatter = false;
            for (Neutron n : neutrons) {
                simulateNeutron(n);
                if (this.scatter) {
                    break;
                }
            }
        }
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void simulateNeutron(Neutron n) {
        this.assembly.evolveNeutronPath(n, this.visualizations, true);
        completed.incrementAndGet();
        if (traceLevel >= 2) {
            System.out.println("");

        }
    }

    private class Formatter extends StringConverter<Number> {

        @Override
        public String toString(Number n) {
            return String.format("%6.3e", n.doubleValue());
        }

        @Override
        public Double fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public Chart makeChart(String detector, String series, String scale) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        XYChart<String, Number> c;
        Part p;
        Material m;
        DecimalFormat f;
        String e;
        double factor;
        String chartData = "";

        if (detector != null) {
            switch (series) {
                case "Entry counts":
                    c = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    f = new DecimalFormat("0.###E0");
                    e = f.format(p.getTotalDepositedEnergy() * 1e-4);
                    c.setTitle("Part \"" + p.name + "\", total deposited energy: " + e + " J");
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Count");
                    c.getData().add(p.entriesOverEnergy.makeSeries("Entry counts", scale));
                    chartData = "Energy,Entry Count";
                    break;

                case "Fluence":
                    c = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    if (p != null) {
                        f = new DecimalFormat("0.###E0");
                        e = f.format(p.getTotalFluence() / this.lastCount);
                        c.setTitle("Part \"" + p.name + "\" (" + p.material.name + ")"
                                + "\nTotal fluence = " + e + " (n/cm^2)/src"
                                + ", src = " + this.lastCount
                                + " \nThermal energy stats:\n"
                                + p.fluenceOverEnergy.getStatsString(scale)
                        );
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Fluence (n/cm^2)/src");
                        yAxis.setTickLabelFormatter(new Formatter());
                        c.getData().add(p.fluenceOverEnergy.makeSeries("Fluence", this.lastCount, scale));
                        //c.getData().add(p.capturesOverEnergy.makeSeries("Capture", log));
                        chartData = "Energy,Fluence and Captures";
                    } else {
                        factor = (4.0 / 3.0 * Math.PI * Math.pow(1000, 3) - this.assembly.getVolume());
                        m = Material.getByName("Air");
                        f = new DecimalFormat("0.###E0");
                        e = f.format(m.totalFreePath / (this.lastCount * factor));
                        c.setTitle("Interstitial air"
                                + "\nTotal fluence = " + e + " (n/cm^2)/src"
                                + ", src = " + this.lastCount);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Fluence (n/cm^2)/src");
                        yAxis.setTickLabelFormatter(new Formatter());
                        c.getData().add(m.lengthOverEnergy.makeSeries("Fluence", this.lastCount * factor, scale));
                        //c.getData().add(m.capturesOverEnergy.makeSeries("Capture", log));
                        chartData = "Energy,Fluence";
                    }
                    break;

                case "Scatter counts":
                    c = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    if (p != null) {
                        c.setTitle("Part \"" + p.name + "\", "
                                + ", src = " + this.lastCount
                                +", total events: " + p.getTotalEvents());
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count/src");
                        c.getData().add(p.scattersOverEnergyBefore.makeSeries("Scatter (before)", this.lastCount, scale));
                        c.getData().add(p.scattersOverEnergyAfter.makeSeries("Scatter (after)", this.lastCount, scale));
                        chartData = "Energy,Event Count";
                    } else {
                        m = Material.getByName(detector);
                        c.setTitle("Material \"" + m.name + "\", total events: " + m.totalEvents);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        c.getData().add(m.scattersOverEnergyBefore.makeSeries("Scatter (before)", scale));
                        c.getData().add(m.scattersOverEnergyAfter.makeSeries("Scatter (after)", scale));
                        chartData = "Energy,Event Count";
                    }
                    break;

           case "Capture counts":
                    c = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    if (p != null) {
                        c.setTitle("Part \"" + p.name + "\""
                                + ", src = " + this.lastCount
                                +", total events: " + p.getTotalEvents());
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count/src");
                        c.getData().add(p.capturesOverEnergy.makeSeries("Capture", this.lastCount, scale));
                        chartData = "Energy,Event Count";
                    } else {
                        m = Material.getByName(detector);
                        c.setTitle("Material \"" + m.name + "\", total events: " + m.totalEvents);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        c.getData().add(m.capturesOverEnergy.makeSeries("Capture", scale));
                        chartData = "Energy,Event Count";
                    }
                    break;
                    
                case "Path lengths":
                    factor = detector.equals("Air") ? (4.0 / 3.0 * Math.PI * Math.pow(1000, 3) - this.assembly.getVolume()) : 1;
                    c = new BarChart<>(xAxis, yAxis);
                    m = Material.getByName(detector);
                    c.setTitle("Material \"" + m.name + "\"\nMean free path: "
                            + (Math.round(100 * m.totalFreePath / m.pathCount) / 100.0) + " cm, "
                            + "Fluence: " + String.format("%6.3e", m.totalFreePath / (this.lastCount * factor))
                            + " (n/cm^2)/src = " + this.lastCount
                    );
                    xAxis.setLabel("Length (cm)");
                    yAxis.setLabel("Count");
                    c.getData().add(m.lengths.makeSeries("Length"));
                    chartData = "Length,Count";
                    break;

                case "Cross-sections":
                    c = new LineChart<>(xAxis, yAxis);
                    Isotope element = Isotope.getByName(detector);
                    c.setTitle("Microscopic ross-sections for element " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("log10(cross-section/barn)");
                    c.getData().add(element.makeCSSeries("Scatter"));
                    c.getData().add(element.makeCSSeries("Capture"));
                    c.getData().add(element.makeCSSeries("Total"));
                    chartData = "Energy,Total Crosssection";
                    break;

                case "Sigmas":
                    c = new LineChart<>(xAxis, yAxis);
                    m = Material.getByName(detector);
                    c.setTitle("Macroscopic cross-sections for material " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Sigma (cm^-1)");
                    c.getData().add(m.makeSigmaSeries("Sigma (" + detector + ")"));
                    chartData = "Energy,Sigma";
                    break;

                case "Custom test":
                    c = new BarChart<>(xAxis, yAxis);
                    c.setTitle("Custom test");
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("counts");
                    c.getData().add(TestGM.customTest(scale, detector.equals("X-axis only")));
                    chartData = "Energy,Count";
                    break;

                default:
                    return null;
            }
        } else {
            // Enviroment chart
            c = new BarChart<>(xAxis, yAxis);
            c.setTitle("Environment:\nP(escape)="
                    + (Math.round(10000 * Environment.getEscapeProbability()) / 10000.0)
                    + ", P(capture)="
                    + (Math.round(10000 * (1 - Environment.getEscapeProbability())) / 10000.0)
                    + ", Total neutrons: " + this.lastCount
            );
            xAxis.setLabel("Energy (eV)");
            yAxis.setLabel("Count");

            c.getData().add(Environment.getInstance().counts.makeSeries("Escape counts", scale));
            chartData = "Energy,Count";
        }
        // place all series into clipboard
        chartData += "\n";
        for (Series<String, Number> s : c.getData()) {
            chartData += "Series: " + s.getName() + "\n";
            for (Data<String, Number> d : s.getData()) {
                chartData += d.getXValue() + "," + d.getYValue() + "\n";
            }
            chartData += "\n";
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(chartData);
        clipboard.setContent(content);
        return c;
    }

}
