package org.eastsideprep.javaneutrons.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
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
            public boolean hasNext() {
                synchronized (mcs) {
                    if (mcs.stop) {
                        produced = count;
                        mcs.lastCount = count;
                        return false;
                    }
                    return (produced < count || count == 0);
                }
            }

            @Override
            public Neutron next() {
                synchronized (mcs) {
                    if (mcs.stop) {
                        produced = count;
                        mcs.lastCount = count;
                        return null;
                    }

                    if (produced < count) {
                        produced++;
                        return new Neutron(position, direction == null ? Util.Math.randomDir() : direction, energy, mcs);
                    }
                }
                return null;
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
            return (int) count;
        }

    }

    public interface ProgressLambda {

        void reportProgress(int p);
    }

    public HashMap<String, Part> namedParts = new HashMap<>();
    public HashMap<String, Material> materials = new HashMap<>();

    public final Assembly assembly;
    private final Vector3D origin;
    private Vector3D direction;
    protected final AtomicLong completed;
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
    public Grid grid;
    public volatile boolean stop;
    public boolean whitmer = false;
    public long suggestedCount = -1;
    public double suggestedGrid = 5;
    public boolean fit = false;
    public Part designatedDetector = null;
    public double srcRate = 1E8;
    public String name;

    public static boolean visualLimitReached = false;

    protected MonteCarloSimulation() {
        this(null/*new Assembly("pseudo")*/, null, null);
    }

    public MonteCarloSimulation(Assembly assembly, Vector3D origin, Group g) {
        this(assembly, origin, null, Neutron.startingEnergyDD, null, null, g, false);
    }

    public MonteCarloSimulation(Assembly assembly, Vector3D origin, Vector3D direction, double initialEnergy,
            Object interstitialMaterial, Object initialMaterial, Group g, boolean verifyMesh) {

        // convert String class names to real objects
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

        if (this.assembly == null) {
            return;
        }

        if (this.interstitialMaterial == null) {
            this.interstitialMaterial = Air.getInstance("Interstitial air");
        }

        // add all named parts and materials
        this.addNamedPartsAndMaterials(this.assembly);

        if (this.initialMaterial == null) {
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

        if (verifyMesh) {
            // check mesh integrity
            Set<String> conflicts = assembly.verifyMeshIntegrity();
            for (String conflict : conflicts) {
                System.out.println("Failes mesh integrity test: " + conflict);
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
        this.visualizations.clear();

        return completed.get();
    }

    public void simulateParticles(int count, int visualObjectLimit, boolean textTrace) {
        preProcess();
        this.lastCount = count;
        this.traceLevel = count <= 10 ? (1 + (textTrace ? 1 : 0)) : 0;
        this.visualObjectLimit = visualObjectLimit;
        MonteCarloSimulation.visualLimitReached = false;
        this.completed.set(0);

        if (this.viewGroup != null) {
            this.viewGroup.getChildren().remove(this.dynamicGroup);
            this.dynamicGroup.getChildren().clear();
            this.viewGroup.getChildren().add(this.dynamicGroup);
        }

        System.out.println("");
        System.out.println("");
        System.out.println("Running new MC simulation for " + count + " neutrons ...");

        this.start = System.currentTimeMillis();

        if (assembly != null) {
            assembly.resetDetectors();
        }
        Collection<Material> c = this.materials.values();
        c.stream().forEach(m -> m.resetDetector());

        // and enviroment (will count escaped neutrons)
        Environment.getInstance().reset();

        // collect any gammas produced
        NeutronCollection neutrons = new NeutronCollection(count, this.origin, this.direction, this.initialEnergy, this);
        if (count > 0) {
            if (!MonteCarloSimulation.parallel || this.lastCount <= 10) {
                // simulate up to 10 in a single thread for visualization
                neutrons.stream().forEach(
                        n -> {
                            if (!stop) {
                                simulateParticle(n, null, true);
                            }
                        }
                );
            } else {
                // simulate lots in parallel
                for (int i = 1; i < Runtime.getRuntime().availableProcessors(); i++) {
                    new Thread(() -> {
                        for (Neutron n : neutrons) {
                            if (!stop) {
                                simulateParticle(n, null, true);
                            }
                        }
                    }).start();
                }
            }
        } else {
            // simulate one by one until one scatters
            this.scatter = false;
            for (Neutron n : neutrons) {
                simulateParticle(n, null, true);
                if (this.scatter || this.stop) {
                    break;
                }
            }
        }

    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void simulateParticle(Particle p, Material medium, boolean count) {
        if (p == null && count) {
            completed.incrementAndGet();
            return;
        }

        Event e = this.assembly.evolveParticlePath(p, this.visualizations, true, this.grid, medium);
        p.tally();

        if (!(p instanceof Gamma) && e.code == Event.Code.Capture) {
            if (this.traceLevel > 0) {
                System.out.println("Captured by: " + e.nuclide.name);
            }
            List<Gamma> gammas = e.nuclide.generateGammasForCapture(p.position, p.energy);
            if (!gammas.isEmpty()) {
                if (this.traceLevel > 0) {
                    System.out.println("Processing " + gammas.size() + " photons");
                }
                for (Gamma g : gammas) {
                    g.mcs = this;
                    simulateParticle(g, e.exitMaterial, false);
                }
            }
        } else {
            if (this.traceLevel > 0) {
                System.out.println("Particle complete, event code " + e.code);
            }
        }

        if (count) {
            completed.incrementAndGet();
            if (traceLevel >= 2) {
                System.out.println("");
            }
        }
    }

    private void addNamedPartsAndMaterials(Assembly a) {
        for (Part p : this.assembly.parts) {
            if (p instanceof Assembly) {
                Assembly a2 = (Assembly) p;
                addNamedPartsAndMaterials(a2);
            } else {
                if (p.name != null) {
                    this.namedParts.put(p.name, p);
                }
                if (p.material.name != null) {
                    this.materials.put(p.material.name, p.material);
                }
            }
        }
    }

    public Material getMaterialByName(String name) {
        return materials.get(name);
    }

    public Part getPartByName(String name) {
        return namedParts.get(name);
    }

    public void prepareGrid(double side, Group vis) {
        if (!(this instanceof MC0D)) {
            this.grid = new Grid(side, assembly, origin, vis);
        }
    }

    public void preProcess() {

    }

    public void postProcess() {
    }

    public class Formatter extends StringConverter<Number> {

        @Override
        public String toString(Number n) {
            return String.format("%6.3e", n.doubleValue());
        }

        @Override
        public Double fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public Chart makeChart(String detector, String series, String scale, String type) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        XYChart<String, Number> c;
        Part p;
        Material m;
        DecimalFormat f;
        String e;
        double factor;
        String chartData = "";
        Series<String, Number> sErrors = null;

        if (detector != null) {
            switch (series) {
                case "Entry counts":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    f = new DecimalFormat("0.###E0");
                    e = f.format(p.getTotalDepositedEnergy() * 1e-4);
                    c.setTitle("Part \"" + p.name + "\", total deposited energy: " + e + " J"
                            + ", src = " + this.lastCount
                            + p.entriesOverEnergy.getStatsString(scale, false, this.lastCount)
                    );
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Count/src");
                    c.getData().add(p.entriesOverEnergy.makeSeries(series, this.lastCount, scale));
                    if (fit && scale.equals("Linear (thermal)")) {
                        c.getData().add(p.entriesOverEnergy.makeFittedSeries("Energy fit", this.lastCount));
                    }
                    break;

                case "Exit counts":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    f = new DecimalFormat("0.###E0");
                    e = f.format(p.getTotalDepositedEnergy() * 1e-4);
                    c.setTitle("Part \"" + p.name + "\", total deposited energy: " + e + " J"
                            + ", src = " + this.lastCount
                            + p.exitsOverEnergy.getStatsString(scale, false, this.lastCount)
                    );
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Count/src");
                    c.getData().add(p.exitsOverEnergy.makeSeries(series, this.lastCount, scale));
                    if (fit && scale.equals("Linear (thermal)")) {
                        c.getData().add(p.exitsOverEnergy.makeFittedSeries("Energy fit", this.lastCount));
                    }
                    break;

                case "Fluence":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    if (p != null) {
                        f = new DecimalFormat("0.###E0");
                        double sv = p.getSieverts(type);
                        e = f.format(p.getTotalFluence(type) / this.lastCount);
                        c.setTitle("Part \"" + p.name + "\" (" + p.material.name + ")"
                                + "\nTotal fluence = " + e + " (n/cm^2)/src"
                                + ", "
                                + f.format(sv) + " Sv, "
                                + "src = " + this.lastCount
                        );
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Fluence (particles/cm^2)/src");
                        yAxis.setTickLabelFormatter(new Formatter());
                        Map<String, CorrelatedTallyOverEV> map = p.fluenceMap;
                        for (String kind : map.keySet()) {
                            if (kind.equals(type)) {
                                CorrelatedTallyOverEV h = map.get(kind);
                                c.getData().add(h.makeSeries("Fluence (" + type + ")", this.lastCount, scale));
                                sErrors = h.makeErrorSeries("Relative Error", this.lastCount, scale);
                            }
                        }
                        if (fit && scale.equals("Linear (thermal)")) {
                            c.getData().add(map.get("neutron").makeFittedSeries("Flux fit", this.lastCount));
                        }
                        //c.getData().add(p.capturesOverEnergy.makeSeries("Capture", log));
                    } else {
                        // this is only for the interstitial medium
                        factor = (4.0 / 3.0 * Math.PI * Math.pow(Environment.limit, 3) - this.assembly.getVolume());
                        m = this.getMaterialByName(detector);
                        f = new DecimalFormat("0.###E0");
                        e = f.format(m.totalFreePath / (this.lastCount * factor));
                        c.setTitle("Interstitial medium"
                                + "\nTotal fluence = " + e + " (n/cm^2)/src"
                                + ", src = " + this.lastCount);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Fluence (n/cm^2)/src");
                        yAxis.setTickLabelFormatter(new Formatter());
                        c.getData().add(m.lengthOverEnergy.makeSeries("Fluence", this.lastCount * factor, scale));
                    }
                    break;

                case "Scatter counts":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    if (p != null) {
                        c.setTitle("Part \"" + p.name + "\", "
                                + ", src = " + this.lastCount);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count/src");
                        c.getData().add(p.scattersOverEnergyBefore.makeSeries("Scatter (before)", this.lastCount, scale));
                        c.getData().add(p.scattersOverEnergyAfter.makeSeries("Scatter (after)", this.lastCount, scale));
                    } else {
                        m = this.getMaterialByName(detector);
                        c.setTitle("Material \"" + m.name + "\", src = " + this.lastCount);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        c.getData().add(m.scattersOverEnergyBefore.makeSeries("Scatter (before)", scale));
                        c.getData().add(m.scattersOverEnergyAfter.makeSeries("Scatter (after)", scale));
                    }
                    break;

                case "Capture counts":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    if (p != null) {
                        c.setTitle("Part \"" + p.name + "\""
                                + ", src = " + this.lastCount
                                + ", total events: " + p.getTotalEvents()
                                + p.capturesOverEnergy.getStatsString(scale, false, this.lastCount)
                        );
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count/src");
                        c.getData().add(p.capturesOverEnergy.makeSeries("Capture", this.lastCount, scale));
                    } else {
                        m = this.getMaterialByName(detector);
                        c.setTitle("Material \"" + m.name + "\""
                                + ", src = " + this.lastCount
                                + m.capturesOverEnergy.getStatsString(scale, false, this.lastCount));
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        c.getData().add(m.capturesOverEnergy.makeSeries("Capture", scale));
                    }
                    break;

                case "Path lengths":
                    factor = detector.equals("Air") ? (4.0 / 3.0 * Math.PI * Math.pow(1000, 3) - this.assembly.getVolume()) : 1;
                    c = new LineChart<>(xAxis, yAxis);
                    m = this.getMaterialByName(detector);
                    TallyOverEV h = m.lengthOverEnergy.normalizeBy(m.pathCounts);
                    c.setTitle("Material \"" + m.name + "\"\nMean free path: "
                            + (Math.round(100 * m.totalFreePath / m.pathCount.get()) / 100.0) + " cm, "
                            + "src = " + this.lastCount
                    );
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Count");
                    c.getData().add(h.makeSeries("Length", scale));
                    break;

                case "Scatter angles":
                    c = new LineChart<>(xAxis, yAxis);
                    p = this.getPartByName(detector);
                    c.setTitle("Part \"" + p.name + "\""
                            + "src = " + this.lastCount
                    );
                    xAxis.setLabel("cos(angle)");
                    yAxis.setLabel("Count");
                    c.getData().add(p.angles.makeSeries("Length", this.lastCount, 1.0));
                    break;

                case "Cross-sections":
                    c = new LineChart<>(xAxis, yAxis);
                    Nuclide element = Nuclide.getByName(detector);
                    c.setTitle("Microscopic cross-sections for element " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("log10(cross-section/barn)");
                    c.getData().add(element.makeCSSeries("Scatter"));
                    c.getData().add(element.makeCSSeries("Capture"));
                    c.getData().add(element.makeCSSeries("Total"));
                    break;

                case "Sigmas":
                    c = new LineChart<>(xAxis, yAxis);
                    m = this.getMaterialByName(detector);
                    c.setTitle("Macroscopic cross-sections for material " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("log(Sigma (cm^-1))");
                    c.getData().add(m.makeSigmaSeries("Sigma (" + detector + ")"));
                    break;

                default:
                    return null;
            }
        } else {
            // Enviroment chart
            c = new LineChart<>(xAxis, yAxis);
            c.setTitle("Environment:\nP(escape)="
                    + (Math.round(10000 * Environment.getEscapeProbability()) / 10000.0)
                    + ", P(capture)="
                    + (Math.round(10000 * (1 - Environment.getEscapeProbability())) / 10000.0)
                    + ", Total neutrons: " + this.lastCount
            );
            xAxis.setLabel("Energy (eV)");
            yAxis.setLabel("Count/src");

            c.getData().add(Environment.getInstance().counts.makeSeries("Escape counts", this.lastCount, scale));
        }
        copyChartCSV(c, sErrors);
        return c;
    }

    public static XYChart.Series makeThermalSeriesFromCSV(String name, URL url) {
        XYChart.Series s = new XYChart.Series();
        ObservableList data = s.getData();
        s.setName(name);

        try {
            InputStream ist = new FileInputStream(new File(url.toURI()));
            Scanner sc = new Scanner(ist);
            while (sc.hasNextLine()) {
                String[] numbers = sc.nextLine().split(",");
                try {
                    double x = Double.parseDouble(numbers[0]);
                    String tick = String.format("%6.3e", x);
                    if (x > TallyOverEV.LOW_VISUAL_LIMIT) {
                        break;
                    }
                    data.add(new XYChart.Data(tick, Double.parseDouble(numbers[1])));
                } catch (Exception e) {
                    // skip headers, underflow, overflow
                }
            }
        } catch (Exception e) {
            System.out.println("CSV not found for series: " + url);
            return null;
        }
        return s;
    }

    public static String makeChartCSV(XYChart<String, Number> c, Series<String, Number> sErrors) {
        String chartData = "'" + c.getXAxis().getLabel() + "'";
        try {
            // make header

            for (Series<String, Number> s : c.getData()) {
                chartData += " '" + s.getName() + "'";
            }
            if (sErrors != null) {
                chartData += " '" + sErrors.getName() + "'";
            }
            chartData += "\n";

            // check if we need to insert a 0 row
            Series<String, Number> s0 = c.getData().get(0);
            String firstBin = s0.getData().get(0).getXValue();
            if (firstBin.startsWith("<")) {
                firstBin = s0.getData().get(1).getXValue();
            }
            if (Double.parseDouble(firstBin) > 0) {
                // make row for 0s
                chartData += "0";
                for (Series<String, Number> s : c.getData()) {
                    chartData += " 0";
                }
                if (sErrors != null) {
                    chartData += " 0";
                }
                chartData += "\n";
            }

            // go through x-values
            for (int i = 0; i < s0.getData().size(); i++) {
                chartData += s0.getData().get(i).getXValue();
                // go through the y-values
                for (Series<String, Number> s : c.getData()) {
                    if (s.getData().size() > i) {
                        Data<String, Number> d2 = s.getData().get(i);
                        chartData += " " + d2.getYValue();
                    } else {
                        chartData += " NA";
                    }
                }
                if (sErrors != null) {
                    if (sErrors.getData().size() > i) {
                        Data<String, Number> d2 = sErrors.getData().get(i);
                        chartData += " " + d2.getYValue();
                    } else {
                        chartData += " NA";
                    }
                }
                chartData += "\n";
            }
        } catch (Exception e) {
            System.out.println("Exception while trying to create chart CSV: " + e);
        }
        return chartData;
    }

    public static void copyChartCSV(XYChart<String, Number> c, Series<String, Number> sErrors) {
        String chartData = makeChartCSV(c, sErrors);

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(chartData);
        clipboard.setContent(content);
    }

}
