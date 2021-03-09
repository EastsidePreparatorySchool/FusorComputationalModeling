/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import static org.eastsideprep.javaneutrons.TestSV.detectorPeople;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.Environment;
import org.eastsideprep.javaneutrons.core.TallyOverEV;
import org.eastsideprep.javaneutrons.core.Event;
import org.eastsideprep.javaneutrons.core.Tally;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.Nuclide;
import org.eastsideprep.javaneutrons.core.MC0D;
import org.eastsideprep.javaneutrons.core.Material;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Particle;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.shapes.Cuboid;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.materials.N12C;
import org.eastsideprep.javaneutrons.materials.N1H;
import org.eastsideprep.javaneutrons.materials.HydrogenWax;
import org.eastsideprep.javaneutrons.materials.N14N;
import org.eastsideprep.javaneutrons.materials.N16O;
import org.eastsideprep.javaneutrons.materials.N206Pb;
import org.eastsideprep.javaneutrons.materials.N207Pb;
import org.eastsideprep.javaneutrons.materials.N208Pb;
import org.eastsideprep.javaneutrons.materials.N40Ar;
import org.eastsideprep.javaneutrons.materials.N56Fe;
import org.eastsideprep.javaneutrons.materials.Paraffin;
import org.eastsideprep.javaneutrons.materials.Vacuum;
import org.fxyz3d.shapes.primitives.CuboidMesh;

/**
 *
 * @author gmein
 */
public class TestGM {

    public static MonteCarloSimulation current(Group visualizations) {
        //return TestSV.october(visualizations);
        // return prison(visualizations);
         return bigBlock(visualizations);
    
        //System.exit(0);
        //return null;
    }

    public static MonteCarloSimulation MC0D_Scatter1(Group vis) {
        vis.getChildren().clear();

        MonteCarloSimulation mcs = new MC0D() {

            ArrayList<Vector2D> pairs = new ArrayList<>();
            Nuclide is = N1H.getInstance();
            Material hw = HydrogenWax.getInstance();
            Shape spherical = new Shape(TestGM.class.getResource("/meshes/spherical_detector.stl"));
            double vol = spherical.getVolume();
            TallyOverEV maxwell;
            TallyOverEV adjusted;
            Tally angles;

            @Override
            public void init() {
                System.out.println("Shell volume: " + vol);
                System.out.println("Util.Physics.thermalEnergy: " + Util.Physics.thermalEnergy / Util.Physics.eV + " eV");
                this.materials.put("Hydrogen Wax", hw);
                before();
            }

            @Override
            public void before() {
                maxwell = new TallyOverEV();
                adjusted = new TallyOverEV();
                angles = new Tally(-1.0, 1.0, 100, false);
                pairs = new ArrayList<>();
            }

            @Override
            public void run(Particle n) {
                // n is a fresh neutron

                // neutron comes from origin, shell is to the +x,
                // does not contain origin.
                // score fluence on the way into the shell
                n.setDirectionAndEnergy(Vector3D.PLUS_I, Util.Physics.thermalEnergy);
                maxwell.record(1.0 / vol, n.energy);
                adjusted.record(1.0 / vol, n.energy);
                // score angle
                angles.record(1, 1);

                // scatter test - will we scatter in the block?
                // technically, this includes captures, might need to differentiate
                Event e = hw.nextPoint(n);
                //e.code = Event.Code.Scatter;
                //if (hw.getPathLength(Util.Physics.thermalEnergy, Math.random()) < 0.025) {
                if (e.t < 0.025 && e.code == Event.Code.Scatter) {
                    // record energy for pair correlation
                    double before = n.energy;

                    n.processEvent(e);
                    double angle = n.direction.getX();
                    // score angle
                    angles.record(1, angle);
                    // score adjusted fluence
                    adjusted.record(1.0 / vol, n.energy);

                    // get second part of pair, record it
                    synchronized (pairs) {
                        pairs.add(new Vector2D(before, n.energy));
                    }
                } else {

                    // score the unscattered fluence
                    maxwell.record(1.0 / vol, n.energy);
                    adjusted.record(1.0 / vol, n.energy);
                    // score angle
                    angles.record(1, 1);
                }
            }

            @Override
            public void after() {
                PearsonsCorrelation pc = new PearsonsCorrelation();
                double[] x;
                double[] y;
                synchronized (pairs) {
                    x = pairs.stream().mapToDouble(v -> v.getX()).toArray();
                    y = pairs.stream().mapToDouble(v -> v.getY()).toArray();
                }
                double c = pc.correlation(x, y);
                System.out.println("Correlation of energies before and after scatter: " + c);
            }

            @Override
            public Chart makeCustomChart(String series, String scale) {
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                XYChart<String, Number> c = new LineChart<>(xAxis, yAxis);
                c.setTitle("MC0D tiny HydrogenWax cube in spherical shell"
                        + ", src = " + this.lastCount);
                if (series.equals("Fluence")) {
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Fluence (n/cm^2)/src");
                    yAxis.setTickLabelFormatter(new Formatter());
                    //c.getData().add(maxwell.makeSeries("Maxwell", this.lastCount, scale));
                    c.getData().add(adjusted.makeSeries("MC0D", this.lastCount, scale));
                    c.getData().add(makeThermalSeriesFromCSV("MCNP", TestGM.class.getResource("/whitmer/thermal_scatter_mcnp.csv")));
                    c.getData().add(makeThermalSeriesFromCSV("Whitmer MC0D 10m scatters", TestGM.class.getResource("/whitmer/thermal_scatter_mc0d.csv")));
                    c.getData().add(makeThermalSeriesFromCSV("MC3D 10m neutrons", TestGM.class.getResource("/whitmer/spherical_mc3d.csv")));
                } else if (series.equals("Scatter angles")) {
                    xAxis.setLabel("cos(angle)");
                    yAxis.setLabel("count/src");
                    c.getData().add(angles.makeSeries("count/src", this.lastCount, 1.0));
                } else {
                    return null;
                }
                copyChartCSV(c, null);
                return c;
            }

        };
        mcs.suggestedCount = 10000000;
        return mcs;
    }

    public static MonteCarloSimulation MC0D_Prison(Group vis) {
        vis.getChildren().clear();

        MonteCarloSimulation mcs = new MC0D() {

            ArrayList<Vector2D> pairs = new ArrayList<>();
            Nuclide is = N1H.getInstance();
            Material hw = HydrogenWax.getInstance();
            Shape prison = new Cuboid(200);
            double vol = prison.getVolume();
            TallyOverEV adjusted;
            Tally angles;

            @Override
            public void init() {
                System.out.println("Shell volume: " + vol);
                System.out.println("Util.Physics.thermalEnergy: " + Util.Physics.thermalEnergy / Util.Physics.eV + " eV");
                this.materials.put("Hydrogen Wax", hw);
                before();
            }

            @Override
            public void before() {
                adjusted = new TallyOverEV();
                angles = new Tally(-1.0, 1.0, 100, false);
                pairs = new ArrayList<>();
            }

            @Override
            public void run(Particle n) {
                Event e;
                // n is a fresh neutron

                n.setDirectionAndEnergy(Vector3D.PLUS_I, Util.Physics.thermalEnergy);

                // scatter test - will we scatter in the block?
                // technically, this includes captures, might need to differentiate
                //e.code = Event.Code.Scatter;
                //if (hw.getPathLength(Util.Physics.thermalEnergy, Math.random()) < 0.025) {
                do {
                    e = hw.nextPoint(n);
                    // record energy for pair correlation
                    double before = n.energy;
                    // score adjusted fluence
                    adjusted.record(e.t / vol, n.energy);

                    n.processEvent(e);
                    if (e.code == Event.Code.Scatter) {
                        // score angle
                        angles.record(1, e.cos_theta);
                        // get second part of pair, record it
                        synchronized (pairs) {
                            pairs.add(new Vector2D(before, n.energy));
                        }
                    }

                } while (e.code == Event.Code.Scatter);
            }

            @Override
            public void after() {
                PearsonsCorrelation pc = new PearsonsCorrelation();
                double[] x;
                double[] y;
                synchronized (pairs) {
                    x = pairs.stream().mapToDouble(v -> v.getX()).toArray();
                    y = pairs.stream().mapToDouble(v -> v.getY()).toArray();
                }
                double c = pc.correlation(x, y);
                System.out.println("Correlation of energies before and after scatter: " + c);
            }

            @Override
            public Chart makeCustomChart(String series, String scale) {
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                XYChart<String, Number> c = new LineChart<>(xAxis, yAxis);
                c.setTitle("MC0D prison, src = " + this.lastCount);
                if (series.equals("Fluence")) {
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Fluence (n/cm^2)/src");
                    yAxis.setTickLabelFormatter(new Formatter());
                    //c.getData().add(maxwell.makeSeries("Maxwell", this.lastCount, scale));
                    c.getData().add(adjusted.makeSeries("Fluence (n/cm^2)/src", this.lastCount, scale));
//                    c.getData().add(makeThermalSeriesFromCSV("MCNP", TestGM.class.getResource("/whitmer/thermal_scatter_mcnp.csv")));
//                    c.getData().add(makeThermalSeriesFromCSV("MC0D 10m scatters", TestGM.class.getResource("/whitmer/thermal_scatter_mc0d.csv")));
                } else if (series.equals("Scatter angles")) {
                    xAxis.setLabel("cos(angle)");
                    yAxis.setLabel("count/src");
                    c.getData().add(angles.makeSeries("count/src", this.lastCount, 1.0));
                }
                copyChartCSV(c, null);
                return c;
            }

        };
        mcs.suggestedCount = 100000;
        return mcs;
    }

    public static MonteCarloSimulation MC0D_CountryClubPrison(Group vis) {
        vis.getChildren().clear();

        MonteCarloSimulation mcs = new MC0D() {

            ArrayList<Vector2D> pairs = new ArrayList<>();
            Nuclide is = N1H.getInstance();
            Material pf = Paraffin.getInstance();
            Shape prison = new Cuboid(44);
            double vol = prison.getVolume();
            TallyOverEV adjusted;
            Tally angles;

            @Override
            public void init() {
                System.out.println("Shell volume: " + vol);
                this.materials.put("Paraffin", pf);
                before();
            }

            @Override
            public void before() {
                adjusted = new TallyOverEV();
                angles = new Tally(-1.0, 1.0, 100, false);
                pairs = new ArrayList<>();
            }

            @Override
            public void run(Particle n) {
                Event e;
                // n is a fresh neutron

                n.setPosition(Vector3D.ZERO);
                n.setDirectionAndEnergy(Util.Math.randomDir(), Neutron.startingEnergyDD);

                // scatter test - will we scatter in the block?
                // technically, this includes captures, might need to differentiate
                //e.code = Event.Code.Scatter;
                //if (hw.getPathLength(Util.Physics.thermalEnergy, Math.random()) < 0.025) {
                do {
                    e = pf.nextPoint(n);
                    if (e.position.getNormInf() > 22) {
                        Environment.getInstance().counts.record(1, n.energy);
                        break;
                    }
                    // record energy for pair correlation
                    double before = n.energy;
                    // score adjusted fluence
                    adjusted.record(e.t / vol, n.energy);

                    n.processEvent(e);
                    if (e.code == Event.Code.Scatter) {
                        // score angle
                        angles.record(1, e.cos_theta);
                        // get second part of pair, record it
                        synchronized (pairs) {
                            pairs.add(new Vector2D(before, n.energy));
                        }
                    }
                    n.setPosition(e.position);

                } while (e.code == Event.Code.Scatter);
            }

            @Override
            public void after() {
                PearsonsCorrelation pc = new PearsonsCorrelation();
                double[] x;
                double[] y;
                synchronized (pairs) {
                    x = pairs.stream().mapToDouble(v -> v.getX()).toArray();
                    y = pairs.stream().mapToDouble(v -> v.getY()).toArray();
                }
                double c = pc.correlation(x, y);
                System.out.println("Correlation of energies before and after scatter: " + c);
            }

            @Override
            public Chart makeCustomChart(String series, String scale) {
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                XYChart<String, Number> c = new LineChart<>(xAxis, yAxis);
                c.setTitle("MC0D prison, src = " + this.lastCount);
                if (series.equals("Fluence")) {
                    xAxis.setLabel("Energy (eV)");
                    yAxis.setLabel("Fluence (n/cm^2)/src");
                    yAxis.setTickLabelFormatter(new Formatter());
                    //c.getData().add(maxwell.makeSeries("Maxwell", this.lastCount, scale));
                    c.getData().add(adjusted.makeSeries("Fluence (n/cm^2)/src", this.lastCount, scale));
//                    c.getData().add(makeThermalSeriesFromCSV("MCNP", TestGM.class.getResource("/whitmer/thermal_scatter_mcnp.csv")));
//                    c.getData().add(makeThermalSeriesFromCSV("MC0D 10m scatters", TestGM.class.getResource("/whitmer/thermal_scatter_mc0d.csv")));
                } else if (series.equals("Scatter angles")) {
                    xAxis.setLabel("cos(angle)");
                    yAxis.setLabel("count/src");
                    c.getData().add(angles.makeSeries("count/src", this.lastCount, 1.0));
                }
                copyChartCSV(c, null);
                return c;
            }

        };
        mcs.suggestedCount = 100000;
        return mcs;
    }

    //
    // world simulations
    //
    public static MonteCarloSimulation bigBlock(Group visualizations) {
        double thickness = 25; //block thickness in cm
        Shape blockShape = new Shape(new CuboidMesh(thickness, 100, 100));
        String m = "HydrogenWax";
        //String m = "CarbonWax";
        //String m = "Paraffin";

        Part wall = new Part("Wall: " + m, blockShape, m);
        wall.getTransforms().add(new Translate(50 + thickness / 2, 0, 0));
        wall.setColor("silver");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Detector behind " + m + " wall", detectorShape, "HighVacuum");
        detector1.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1.setColor("pink");

        Part detector2 = new Part("Detector opposite " + m + " wall", detectorShape, "HighVacuum");
        detector2.getTransforms().add(new Translate(-(100 + 1), 0, 0));
        detector2.setColor("pink");

        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall, detector1, detector2);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, null, Neutron.startingEnergyDD,
                "Vacuum", null, visualizations); // interstitial, initial
        //mcs.prepareGrid(5.0, visualizations);
        mcs.suggestedCount = 100000;
        return mcs;
    }

    //
    // world simulations
    //
    public static MonteCarloSimulation sandwich(Group visualizations) {
        double thickness = 5; //block thickness in cm
        Shape blockShape = new Shape(new CuboidMesh(thickness, 100, 100));

        Part wall1 = new Part("Wall 1: Lead", blockShape, "Lead");
        wall1.getTransforms().add(new Translate(50, 0, 0));
        wall1.setColor("gray");

        Part wall2 = new Part("Wall 2: Wood", blockShape, "Wood");
        wall2.getTransforms().add(new Translate(50 + thickness, 0, 0));
        wall2.setColor("brown");

        Part wall3 = new Part("Wall 3: Paraffin", blockShape, "Paraffin");
        wall3.getTransforms().add(new Translate(50 + 2 * thickness, 0, 0));
        wall3.setColor("beige");

        Part detector = new Part("Detector", blockShape, "HighVacuum");
        detector.getTransforms().add(new Translate(3 * thickness + 60, 0, 0));
        detector.setColor("pink");

        Assembly whitmer = new Assembly("Sandwich");
        whitmer.addAll(wall1, wall2, wall3, detector);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, null, Neutron.startingEnergyDD,
                "Vacuum", null, visualizations); // interstitial, initial
        //mcs.prepareGrid(5.0, visualizations);
        mcs.suggestedCount = 1000000;
        return mcs;
    }

    public static MonteCarloSimulation prison(Group visualizations) {
        double thickness = 200; //block thickness in cm
        //String m = "HydrogenWax";
        String m = "CarbonWax";
        //String m = "Paraffin";

        //Part wall = new Part("Prison: " + m, new Shape(TestGM.class.getResource("/meshes/prison.stl"), "cm"), m);
        Part wall = new Part("Prison: " + m, new Cuboid(thickness), m);
        wall.setColor("silver");

        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, null, Neutron.startingEnergyDD, // origin = (0,0,0), random dir, default DD-neutron energy+1 KeV
                "Air", null, visualizations); // interstitial, initial
        mcs.suggestedCount = 100000;
        return mcs;
    }

    public static MonteCarloSimulation smoosh0(Group visualizations) {

        double gap = 1;

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "lightgreen");

        Part igloo = new Part("Igloo", new Shape(TestGM.class.getResource("/smoosh.obj"), "cm"), "Paraffin");

        igloo.getTransforms()
                .add(0, new Rotate(90, new Point3D(1, 0, 0)));
        igloo.getTransforms()
                .add(0, new Translate(0, 63, 0));
        igloo.setColor(
                "ivory");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Left detector", detectorShape, "Vacuum");

        detector1.getTransforms()
                .add(0, new Translate(-(100 + 1), 0, 0));
        detector1.setColor(
                "pink");

        Part detector2 = new Part("Bottom detector", detectorShape, "Vacuum");

        detector2.getTransforms()
                .add(0, new Rotate(90, new Point3D(0, 0, 1)));
        detector2.getTransforms()
                .add(0, new Translate(0, 100 + 1, 0));
        detector2.setColor(
                "pink");

        Part detector3 = new Part("Back detector", detectorShape, "Vacuum");

        detector3.getTransforms()
                .add(0, new Rotate(90, new Point3D(0, 1, 0)));
        detector3.getTransforms()
                .add(0, new Translate(0, 0, 100 + 1));
        detector3.setColor(
                "pink");

        // assemble the Fusor out of the other stuff
        Assembly smoosh = new Assembly("Smooshed igloo");

        smoosh.addAll(igloo, vacChamber, detector1, detector2, detector3);

        smoosh.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        MonteCarloSimulation mcs = new MonteCarloSimulation(smoosh, null, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation smoosh1(Group visualizations) {
        MonteCarloSimulation mcs = smoosh0(visualizations);
        mcs.prepareGrid(1.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation smoosh2(Group visualizations) {
        MonteCarloSimulation mcs = smoosh0(visualizations);
        mcs.prepareGrid(2.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation smoosh5(Group visualizations) {
        MonteCarloSimulation mcs = smoosh0(visualizations);
        mcs.prepareGrid(5.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation smoosh10(Group visualizations) {
        MonteCarloSimulation mcs = smoosh0(visualizations);
        mcs.prepareGrid(10.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation smoosh20(Group visualizations) {
        MonteCarloSimulation mcs = smoosh0(visualizations);
        mcs.prepareGrid(20.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation spherical(Group visualizations) {
        double thickness = 0.025; //block thickness in cm
        String m = "HydrogenWax";
        //String m = "CarbonWax";
        //String m = "Paraffin";

        Part wall = new Part("Block: " + m, new Cuboid(thickness), m);
        wall.getTransforms().add(new Translate(50, 0, 0));
        wall.setColor("silver");

        Part detector = new Part("Spherical detector", new Shape(TestGM.class
                .getResource("/meshes/spherical_detector.stl"), "cm"), "HighVacuum");
        detector.getTransforms()
                .add(new Translate(50, 0, 0));
        detector.setColor(
                "pink");

        Assembly whitmer = new Assembly("Whitmer");

        whitmer.addAll(wall, detector);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, Vector3D.PLUS_I, Util.Physics.thermalEnergy, // origin = (0,0,0), random dir, default DD-neutron energy+1 KeV
                "Vacuum", null, visualizations); // interstitial, initial

        mcs.prepareGrid(
                2.0, visualizations);
        mcs.suggestedCount = 10000;
        return mcs;
    }

    public static MonteCarloSimulation sphericalAdjusted(Group visualizations) {
        MonteCarloSimulation mcs = spherical(visualizations);
        mcs.prepareGrid(5.0, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation sphericalWhitmerAdjusted(Group visualizations) {
        MonteCarloSimulation mcs = spherical(visualizations);
        mcs.prepareGrid(5.0, visualizations);
        mcs.whitmer = true;
        return mcs;
    }

    public static MonteCarloSimulation thin(Group visualizations) {
        double thickness = 0.025; //block thickness in cm
        Shape blockShape = new Shape(new Cuboid(thickness, 100, 100));
        //Part wall = new Part("Wall", blockShape, "CarbonWax");
        Part wall = new Part("Block", blockShape, "Paraffin");
        wall.getTransforms().add(new Translate(50 + thickness / 2, 0, 0));
        wall.setColor("silver");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Detector behind wall", detectorShape, "HighVacuum");
        detector1.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1.setColor("pink");

        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall, detector1);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, null, 0, // origin = (0,0,0), random dir, default DD-neutron energy
                null, null, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation thinAdjusted(Group vis) {

        MonteCarloSimulation mcs = thin(vis);
        return mcs;
    }

    public static MonteCarloSimulation settle(Group visualizations) {
        double gap = 0.025; //block thickness in cm

        Part block1 = new Part("block", new Shape(TestGM.class
                .getResource("/meshes/baseparaffinblock.obj"), "cm"), "Paraffin");
        block1.getTransforms()
                .add(0, new Rotate(90, new Point3D(1, 0, 0)));
        block1.getTransforms()
                .add(0, new Translate(50, 50, -25));
        block1.setColor(
                "lightblue");

        Part block2 = new Part("block", new Shape(TestGM.class.getResource("/meshes/baseparaffinblock.obj"), "cm"), "Paraffin");

        block2.getTransforms()
                .add(0, new Rotate(90, new Point3D(1, 0, 0)));
        block2.getTransforms()
                .add(0, new Translate(50, -100, -25));
        block2.setColor(
                "lightgreen");

        System.out.println(
                "");
        System.out.println(
                "Distance Y-axis (before settle): " + block2.distance(block1, Vector3D.PLUS_J));
        Translate tx = block2.settleAgainst(block1, Vector3D.PLUS_J);

        System.out.println(
                "Transform: " + tx);
        block2.getTransforms()
                .add(0, tx);
        System.out.println(
                "Distance X-axis: " + block2.distance(block1, Vector3D.PLUS_I));
        System.out.println(
                "Distance Y-axis: " + block2.distance(block1, Vector3D.PLUS_J));
        System.out.println(
                "Distance Z-axis: " + block2.distance(block1, Vector3D.PLUS_K));
        System.out.println(
                "");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Detector behind wall", detectorShape, "HighVacuum");

        detector1.getTransforms()
                .add(new Translate(100 + 1, 0, 0));
        detector1.setColor(
                "pink");

        Assembly a = new Assembly("Blocks");

        a.addAll(block1, block2, detector1);

        MonteCarloSimulation mcs = new MonteCarloSimulation(a,
                null, null, 0, // origin = (0,0,0), random dir, default DD-neutron energy
                null, null, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation whitmer1(Group visualizations) {

        Shape blockShape = new Shape(new CuboidMesh(25, 100, 100));
        Part wall = new Part("Block", blockShape, "Paraffin");
        wall.getTransforms().add(new Translate(50 + 12.5, 0, 0));
        wall.setColor("silver");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));

        Part detector1 = new Part("Detector behind block", detectorShape, "HighVacuum");
        detector1.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1.setColor("pink");

        Part detector2 = new Part("Detector opposite block", detectorShape, "HighVacuum");
        detector2.getTransforms().add(new Translate(-(100 + 1), 0, 0));
        detector2.setColor("pink");

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly whitmer = new Assembly("Whitmer");

        whitmer.addAll(wall, detector1, detector2, vacChamber);
        //whitmer.addTransform(new Translate(0, -100, 0));

        whitmer.containsMaterialAt(Vacuum.getInstance(), Vector3D.ZERO);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer, Vector3D.ZERO, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation humanDetector(Group visualizations) {
        //
        // Wall1
        // this cube-shaped wall is loaded from an obj file in resources
        // any obj files need to live their (folder src/main/resources in folder view)
        //

        double gap = 3; // in cm
        double offset = 2 * gap; // in cm
        //
        // igloo
        //
        Assembly igloo = new Assembly("igloo", TestGM.class.getResource("/meshes/igloo.obj"), "Paraffin");

        double s = 20;
        // move detector behind cube wall
        Part detector = new Part("Detector 1", new Cuboid(s, 3 * s, 5 * s), "Vacuum");

        detector.getTransforms().add(new Translate(200, 0, 0));

        //
        // body
        //
        //bodyShape.getTransforms().add(0,new Rotate(90, new Point3D(1,0,0)));
        Part body = new Part("Body", new HumanBody(), "HumanBodyMaterial");

        body.getTransforms().add(0, new Translate(0, 0, -200));

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly fusor = new Assembly("Fusor");

        fusor.addAll(igloo, detector, body, vacChamber);

        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);

        mcs.prepareGrid(
                2.0, visualizations);
        return mcs;
    }

    public static Group testVisuals() {
        Group g = new Group();
        double radius = 200;

        Sphere s = new Sphere(radius);
        s.setRotationAxis(new Point3D(0, 0, 1));
        s.setRotate(90);
        Util.Graphics.setColor(s, "white");
        s.setOpacity(0.1);
        //s.setTranslateX(-500);
        //s.setDrawMode(DrawMode.LINE);
        Vector3D orig = new Vector3D(0, 0, 0);

        Nuclide is = N12C.getInstance();

        for (int i = 0; i < 5000; i++) {
            // Vector3D v = Util.Math.randomDir().scalarMultiply(100);
            Vector3D dir1 = Util.Math.randomDir();

            double t1 = Util.Math.raySphereIntersect(orig, dir1, Vector3D.ZERO, radius);
            if (t1 >= 0) {
                Vector3D v = orig.add(dir1.scalarMultiply(t1));
                Sphere p = new Sphere(1);
                Util.Graphics.setColor(p, "red");
                p.getTransforms().add(new Translate(v.getX(), v.getY(), v.getZ()));
                g.getChildren().add(p);
            }

            /*
            if (is.angles != null) {
                double cos_theta = is.getScatterCosTheta(2.44e6);
                System.out.println("cos_theta " + cos_theta);
                Vector3D dir2 = Util.Math.randomDir(cos_theta, 1.0);
                System.out.println("dir2 " + dir2);
                if (Math.abs(dir2.getNorm() - 1) > 1e-12) {
                    System.out.println("not normal");
                }

                //dir2 = Util.Math.randomDir();
                Rotation r = new Rotation(Vector3D.PLUS_K, Vector3D.PLUS_I);
                dir2 = r.applyTo(dir2);
                System.out.println("x1 " + dir2.getX());
                double t2 = Util.Math.raySphereIntersect(orig, dir2, Vector3D.ZERO, radius);
                if (t2 >= 0) {
                    System.out.println("dir2 " + dir2 + " t " + t2);
                    Vector3D v = orig.add(dir2.scalarMultiply(t2));
                    Sphere p = new Sphere(1);
                    Util.Graphics.setColor(p, "green");
                    System.out.println("v " + v);
                    p.getTransforms().add(new Translate(v.getX(), v.getY(), v.getZ()));
                    g.getChildren().add(p);
                }
            }
            */
        }
        Util.Graphics.drawCoordSystem(g);
        g.getChildren().add(s);
        return g;
    }

    //
    // old stuff - Sydney, this is mostly for visual testing. Return a group you can add in main.
    //
    public static Group testVisuals2() {
        Cuboid cube = new Cuboid(200);
        cube.setRotationAxis(new Point3D(1, 1, 1));
        cube.setTranslateX(100);
        cube.setTranslateY(100);
        cube.setTranslateZ(100);

        Shape cube2 = new Shape(new CuboidMesh(100, 100, 100));
        cube2.setTranslateX(100);
        cube2.setTranslateY(100);
        cube2.setTranslateZ(100);
        cube2.setRotationAxis(new Point3D(-1, 1, 1));

        Shape cube3 = new Shape(TestGM.class
                .getResource("/meshes/cube.obj"));
        cube3.setTranslateX(
                100);
        cube3.setTranslateY(
                100);
        cube3.setTranslateZ(
                100);
        cube3.setRotationAxis(
                new Point3D(-1, -1, 1));

        Shape body = new Shape(TestGM.class.getResource("/meshes/body.obj"));

        body.setScaleX(
                100);
        body.setScaleY(
                100);
        body.setScaleZ(
                100);

        body.setTranslateX(
                200);
        body.setTranslateY(
                200);
        body.setTranslateZ(
                200);
        body.setRotationAxis(
                new Point3D(-1, -1, -1));
        System.out.println(
                "Volume:" + body.getVolume());
        // little thread to keep rotating the cube
        Thread t = new Thread(() -> {
            try {
                for (;;) {
                    Thread.sleep(50);
                    // runLater() posts the argument lambda to the main event queue 
                    // really to run as soon as possible, but on the main thread.

                    Platform.runLater(() -> {
                        cube.setRotate(cube.getRotate() + 1);
                        cube2.setRotate(cube2.getRotate() + 1);
                        cube3.setRotate(cube3.getRotate() + 1);
                        body.setRotate(cube3.getRotate() + 1);
                    });
                }
            } catch (InterruptedException ex) {
            }
        });

        t.start();

        //Creating a Group object  
        Group g = new Group(cube, cube2, cube3, body);

        return g;
    }

    private static void histTest() {
        TallyOverEV test = new TallyOverEV();
        test.record(1000, 0.5e-4 * Util.Physics.eV);
        test.record(2000, 1e-3 * Util.Physics.eV);
        test.record(2100, 1.1e-3 * Util.Physics.eV);
        test.record(2500, 1.5e-3 * Util.Physics.eV);

        test.record(3333, 2.51e6 * Util.Physics.eV);

        test.record(3000, 0.5e7 * Util.Physics.eV);
        test.record(4000, 1e7 * Util.Physics.eV);
        test.record(5000, 2e7 * Util.Physics.eV);

        XYChart.Series<String, Number> xy = test.makeSeries("", 1, "Linear (thermal)");
        for (XYChart.Data<String, Number> d : xy.getData()) {
            System.out.println(d.getXValue() + "," + d.getYValue());
        }
        System.exit(0);
    }

    public static MonteCarloSimulation ROOM5mm(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1, 0, 0)));

        //room walls
        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/wfront.stl"), "cm"), "Vacuum");
        wfront.setColor("gray");
        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/wback.stl"), "cm"), "Vacuum");
        wback.setColor("gray");
        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/wfloor.stl"), "cm"), "Vacuum");
        wfloor.setColor("gray");
        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/wceiling.stl"), "cm"), "Vacuum");
        wceiling.setColor("gray");
        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/wleft.stl"), "cm"), "Vacuum");
        wleft.setColor("gray");
        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/wright.stl"), "cm"), "Vacuum");
        wright.setColor("gray");

        //important stuff
        Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/wood.stl"), "cm"), "Wood");
        wood.setColor("yellow");
        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/pipes.stl"), "cm"), "Steel");
        pipes.setColor("gray");
        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/leadbox.stl"), "cm"), "Lead");
        lead.setColor("gray");
        Part wax = new Part("Wax", new Shape(TestSV.class.getResource("/meshes/5mmcubes.stl"), "cm"), "Paraffin");
        wax.setColor("lightblue");
        Assembly fusor = new Assembly("Fusor");

        fusor.addAll(vacChamber);
        fusor.addAll(wood);
        fusor.addAll(pipes);
        fusor.addAll(lead);
        fusor.addAll(wax);
        fusor.addAll(wfront);
        fusor.addAll(wback);
        fusor.addAll(wfloor);
        fusor.addAll(wceiling);
        fusor.addAll(wleft);
        fusor.addAll(wright);
        fusor.addTransform(new Rotate(90, new Point3D(1, 0, 0)));

        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20, 30, -299), 180, 100);
        fusor.addAll(dp);

        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        mcs.suggestedCount = 10;
        mcs.suggestedGrid = 5.0;

        System.out.println("Specific tests:");
         return mcs;

    }
    
       public static MonteCarloSimulation ROOMnew(Group visualizations) {

        // vac chamber
//        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
//                .getResource("/meshes/vac_chamber.obj")), "Lead");
//        vacChamber.setColor(
//                "black");
//        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
//        
//        //room walls
//        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/hi/wfront.stl"), "cm"), "Vacuum");
//            wfront.setColor("gray");
//        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/hi/wback.stl"), "cm"), "Vacuum");
//            wback.setColor("gray");
//        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/hi/wfloor.stl"), "cm"), "Vacuum");
//            wfloor.setColor("gray");
//        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/hi/wceiling.stl"), "cm"), "Vacuum");
//            wceiling.setColor("gray");
//        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/hi/wleft.stl"), "cm"), "Vacuum");
//            wleft.setColor("gray");
//        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/hi/wright.stl"), "cm"), "Vacuum");
//            wright.setColor("gray");
            
        //important stuff
        Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/hi/wood.stl"), "cm"), "Wood");
            wood.setColor("yellow");
//        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/hi/newpipes.stl"), "cm"), "Steel");
//            pipes.setColor("gray");
//            pipes.getTransforms().add(new Translate(0,-0.5,0));
//        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/hi/leadbox.stl"), "cm"), "Lead");
//            lead.setColor("gray");
        Part wax = new Part("Wax", new Shape(TestSV.class.getResource("/meshes/hi/0mm.stl"), "cm"), "Paraffin");
            wax.setColor("lightblue");
            wax.getTransforms().add(0, new Translate(0,0.6,0));
        Assembly fusor = new Assembly("Fusor");

        //fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);
        fusor.addAll(/*wood,*/wax/*, lead*/);
       // fusor.addTransform(new Rotate(90, new Point3D(1,0,0)));
        
//        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,30,-299), 180, 100);
      //  fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        wood.shape.intersects(wax.shape, 5, visualizations);
       return mcs;
    }


}
