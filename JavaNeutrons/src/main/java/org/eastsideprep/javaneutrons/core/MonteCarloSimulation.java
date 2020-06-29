package org.eastsideprep.javaneutrons.core;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.util.StringConverter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.assemblies.Assembly;
import org.eastsideprep.javaneutrons.assemblies.Element;
import org.eastsideprep.javaneutrons.assemblies.Material;
import org.eastsideprep.javaneutrons.assemblies.Part;
import org.eastsideprep.javaneutrons.materials.Air;

/**
 *
 * @author gunnar
 */
public class MonteCarloSimulation {

    static boolean parallel = true;

    public interface ProgressLambda {

        void reportProgress(int p);
    }

    private final Assembly assembly;
    private final Vector3D origin;
    private final AtomicLong completed;
    private final LinkedTransferQueue visualizations;
    private final Group viewGroup;
    private final Group dynamicGroup;
    private Air air;
    public long lastCount;
    private int visualObjectLimit;
    private long start;

    public MonteCarloSimulation(Assembly assembly, Vector3D origin, Group g) {
        this.assembly = assembly;
        this.origin = origin;
        this.visualizations = new LinkedTransferQueue<Node>();
        this.completed = new AtomicLong(0);
        this.viewGroup = g;
        this.dynamicGroup = new Group();
        this.viewGroup.getChildren().clear();
        // make some axes
        Util.Graphics.drawCoordSystem(g);
        // add the assembly objects
        this.viewGroup.getChildren().add(this.assembly.getGroup());
        // and a group for event visualiations
        this.viewGroup.getChildren().add(dynamicGroup);
        this.air = Air.getInstance();
    }

    // this will be called from UI thread
    public long update() {
        //viewGroup.getChildren().remove(this.dynamicGroup);
        int size = this.dynamicGroup.getChildren().size();
        if (size < this.visualObjectLimit) {
            this.visualizations.drainTo(this.dynamicGroup.getChildren(), this.visualObjectLimit - size);
        }
        //viewGroup.getChildren().add(this.dynamicGroup);
        return completed.get();
    }

    public void clearVisuals() {
        this.viewGroup.getChildren().remove(this.dynamicGroup);
        this.viewGroup.getChildren().remove(this.assembly.getGroup());
    }

    public void simulateNeutrons(long count, int visualObjectLimit) {
        this.lastCount = count;
        this.visualObjectLimit = visualObjectLimit;

//        Vector3D v0 = new Vector3D(200, 100, 100);
//        Vector3D v1 = new Vector3D(-100, 100, 100);
//        Vector3D v2 = new Vector3D(-100, 100, -200);
//        Util.Graphics.drawLine(simVis, v0, v1, Color.GREEN);
//        Util.Graphics.drawLine(simVis, v1, v2, Color.GREEN);
//        Util.Graphics.drawLine(simVis, v2, v0, Color.GREEN);
//
//        Vector3D start = new Vector3D(0,25,0);
//        double t = Util.Math.rayTriangleIntersect(start, Vector3D.PLUS_J, v0, v1, v2);
//        if (t != -1) {
//            System.out.println("t="+t);
//            Util.Graphics.drawSphere(simVis, Util.Math.rayPoint(start, Vector3D.PLUS_J, t), 5, "blue");
//        }
//        t = Util.Math.rayTriangleIntersect(Vector3D.ZERO, Vector3D.PLUS_I.add(Vector3D.PLUS_J.scalarMultiply(0.5)), v0, v1, v2);
//        Util.Graphics.drawSphere(simVis, Vector3D.ZERO.add(Vector3D.PLUS_I.add(Vector3D.PLUS_J.scalarMultiply(0.5)).scalarMultiply(t)), 5, "yellow");
//        
//        t = Util.Math.rayTriangleIntersect(Vector3D.ZERO, Vector3D.PLUS_I.add(Vector3D.PLUS_K.scalarMultiply(-0.5)), v0, v1, v2);
//        Util.Graphics.drawSphere(simVis, Vector3D.ZERO.add(Vector3D.PLUS_I.add(Vector3D.PLUS_K.scalarMultiply(-0.5)).scalarMultiply(t)), 5, "red");
//
//
//        for (int i = 0; i < 10; i++) {
//            Event e = assembly.rayIntersect(new Vector3D(25,0,0),
//                    Vector3D.PLUS_I.add(new Vector3D(0, Util.Math.random.nextDouble() * 2 - 1, Util.Math.random.nextDouble() * 2 - 1)),
//                    simVis);
//            System.out.println("Event: " + e);
//            Util.Graphics.visualizeEvent(e, simVis);
//        }
        // clear out the old simulation
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

        ArrayList<Neutron> neutrons = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            Vector3D direction = Util.Math.randomDir();
            Neutron n = new Neutron(this.origin, direction, Neutron.startingEnergyDD, count <= 10);
            neutrons.add(n);
        }

        Thread th = new Thread(() -> {
            if (!MonteCarloSimulation.parallel) {
                neutrons.stream().forEach(n -> simulateNeutron(n));
            } else {
                neutrons.parallelStream().forEach(n -> simulateNeutron(n));
            }
        });

        Platform.runLater(() -> th.start());
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void simulateNeutron(Neutron n) {
        this.assembly.evolveNeutronPath(n, this.visualizations, true);
        completed.incrementAndGet();
    }

    public Chart makeChart(String detector, String series) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bc;
        LineChart<String, Number> lc;
        Part p;
        Material m;
        DecimalFormat f;
        String e;

        if (detector != null) {
            switch (series) {
                case "Entry counts":
                    bc = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    f = new DecimalFormat("0.###E0");
                    e = f.format(p.getTotalDepositedEnergy() * 1e-4);
                    bc.setTitle("Part \"" + p.name + "\", total deposited energy: " + e + " J");
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Count");
                    bc.getData().add(p.entriesOverEnergy.makeSeries("Entry counts"));
                    break;

                case "Fluence":
                    bc = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    f = new DecimalFormat("0.###E0");
                    e = f.format(p.getTotalFluence() / this.lastCount);
                    bc.setTitle("Part \"" + p.name + "\" (" + p.material.name + ")"
                            + "\nTotal fluence = " + e + " (n/cm^2)/src"
                            + ", src = " + this.lastCount);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Fluence (n/cm^2)/src");
                    bc.getData().add(p.fluenceOverEnergy.makeSeries("Fluence", this.lastCount));
                    break;

                case "Event counts":
                    bc = new BarChart<>(xAxis, yAxis);
                    p = Part.getByName(detector);
                    if (p != null) {
                        bc.setTitle("Part \"" + p.name + "\", total events: " + p.getTotalEvents());
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        bc.getData().add(p.scattersOverEnergyBefore.makeSeries("Scatter (before)"));
                        bc.getData().add(p.scattersOverEnergyAfter.makeSeries("Scatter (after)"));
                        bc.getData().add(p.capturesOverEnergy.makeSeries("Capture"));
                    } else {
                        m = Material.getByName(detector.substring(detector.indexOf(' ') + 1));
                        bc.setTitle("Interstitial material \"" + m.name + "\", total events: " + m.totalEvents);
                        xAxis.setLabel("Energy (eV)");
                        yAxis.setLabel("Count");
                        bc.getData().add(m.scattersOverEnergyBefore.makeSeries("Scatter (before)"));
                        bc.getData().add(m.scattersOverEnergyAfter.makeSeries("Scatter (after)"));
                        bc.getData().add(m.capturesOverEnergy.makeSeries("Capture"));
                    }
                    break;

                case "Path lengths":
                    bc = new BarChart<>(xAxis, yAxis);
                    m = Material.getByName(detector);
                    bc.setTitle("Material \"" + m.name + "\"\nMean free path: "
                            + (Math.round(100 * m.totalFreePath / m.pathCount) / 100.0) + " cm (count: " + m.pathCount + ")");
                    xAxis.setLabel("Length (cm)");
                    yAxis.setLabel("Count");
                    bc.getData().add(m.lengths.makeSeries("Length"));
                    break;

                case "Cross-sections":
                    lc = new LineChart<>(xAxis, yAxis);
                    Element element = Element.getByName(detector);
                    lc.setTitle("Microscopic ross-sections for element " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Cross-section (barn)");
                    lc.getData().add(element.makeCSSeries("Scatter"));
                    lc.getData().add(element.makeCSSeries("Capture"));
                    lc.getData().add(element.makeCSSeries("Total"));
                    return lc;

                case "Sigmas":
                    lc = new LineChart<>(xAxis, yAxis);
                    m = Material.getByName(detector);
                    lc.setTitle("Macroscopic cross-sections for material " + detector);
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Sigma (cm^-1)");
                    lc.getData().add(m.makeSigmaSeries("Sigma (" + detector + ")"));
                    return lc;

                default:
                    return null;
            }
        } else {
            // Enviroment chart
            bc = new BarChart<>(xAxis, yAxis);
            bc.setTitle("Environment:\nP(escape)="
                    + (Math.round(10000 * Environment.getEscapeProbability()) / 10000.0)
                    + ", P(capture)="
                    + (Math.round(10000 * (1 - Environment.getEscapeProbability())) / 10000.0)
                    + ", Total neutrons: " + this.lastCount
            );
            xAxis.setLabel("Energy (eV)");
            yAxis.setLabel("Count");

            bc.getData().add(Environment.getInstance().counts.makeSeries("Escape counts"));
        }
        return bc;
    }

}
