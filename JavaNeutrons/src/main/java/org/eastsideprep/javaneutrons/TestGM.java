/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.EnergyHistogram;
import org.eastsideprep.javaneutrons.core.Event;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.Isotope;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.shapes.Cuboid;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.materials.E12C;
import org.eastsideprep.javaneutrons.materials.Vacuum;
import org.fxyz3d.shapes.primitives.CuboidMesh;

/**
 *
 * @author gmein
 */
public class TestGM {

    private static void histTest() {
        EnergyHistogram test = new EnergyHistogram();
        test.record(1000, 0.5e-4*Util.Physics.eV);
        test.record(2000, 1e-3*Util.Physics.eV);
        test.record(2100, 1.1e-3*Util.Physics.eV);
        test.record(2500, 1.5e-3*Util.Physics.eV);

        test.record(3333, 2.51e6*Util.Physics.eV);

        test.record(3000, 0.5e7*Util.Physics.eV);
        test.record(4000, 1e7*Util.Physics.eV);
        test.record(5000, 2e7*Util.Physics.eV);
        
        XYChart.Series<String, Number> xy = test.makeSeries("", 1, "Linear (thermal)");
        for (XYChart.Data<String, Number> d : xy.getData()) {
            System.out.println(d.getXValue() + "," + d.getYValue());
        }
        System.exit(0);
    }

    public static MonteCarloSimulation simulationTest(Group visualizations) {
        //histTest();
//        Isotope i = E1H.getInstance();
//        System.out.println("1H mass: " + i.mass);
//        double sd = Math.sqrt(Util.Physics.boltzmann * Util.Physics.roomTemp / i.mass);
//        System.out.println("SD for hydrogen: " + sd);
//        double e = (3.0/2.0*Util.Physics.boltzmann * Util.Physics.roomTemp/Util.Physics.eV);
//        System.out.println("Room temp avg energy in eV: " + e);
//        System.exit(0);

//        histTest(new Histogram(false));
//        System.out.println("");
//        histTest(new Histogram(true));
//        System.exit(0);
        return spherical(visualizations);
    }

    public static XYChart.Series customTest(String scale, boolean xOnly) {
        EnergyHistogram test = new EnergyHistogram();
        //Histogram test = new Histogram(-102,102,104, false);
        MonteCarloSimulation mcs = new MonteCarloSimulation(null, null, null);
        //mcs.trace= true;
        Neutron n = new Neutron(Vector3D.ZERO, Vector3D.PLUS_I, Neutron.startingEnergyDD, mcs);
        Isotope is = E12C.getInstance();
        Event e = new Event(Vector3D.ZERO, Event.Code.Scatter, 0, is, n);

        for (int i = 0; i < 1000000; i++) {
            n.setDirectionAndEnergy(Vector3D.PLUS_I, 0.037 * Util.Physics.eV);
            n.processEvent(e);
            if (n.direction.getX() > 0 && Math.abs(n.direction.getY()) < 0.7 && Math.abs(n.direction.getZ()) < 0.7) {
                test.record(1, e.energyOut);
            }
        }

        return test.makeSeries("test", 1, scale);
    }

    public static MonteCarloSimulation simulationTestSmoosh(Group visualizations) {

        double gap = 1;

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class.getResource("/meshes/vac_chamber.obj")), "Steel");
        vacChamber.setColor("lightgreen");

        Part igloo = new Part("Igloo", new Shape(TestGM.class.getResource("/smoosh.obj"), "cm"), "Paraffin");
        igloo.getTransforms().add(0, new Rotate(90, new Point3D(1, 0, 0)));
        igloo.getTransforms().add(0, new Translate(0, 63, 0));
        igloo.setColor("ivory");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Left detector", detectorShape, "Vacuum");
        detector1.getTransforms().add(0, new Translate(-(100 + 1), 0, 0));
        detector1.setColor("pink");

        Part detector2 = new Part("Bottom detector", detectorShape, "Vacuum");
        detector2.getTransforms().add(0, new Rotate(90, new Point3D(0, 0, 1)));
        detector2.getTransforms().add(0, new Translate(0, 100 + 1, 0));
        detector2.setColor("pink");

        Part detector3 = new Part("Back detector", detectorShape, "Vacuum");
        detector3.getTransforms().add(0, new Rotate(90, new Point3D(0, 1, 0)));
        detector3.getTransforms().add(0, new Translate(0, 0, 100 + 1));
        detector3.setColor("pink");

        // assemble the Fusor out of the other stuff
        Assembly smoosh = new Assembly("Smooshed igloo");
        smoosh.addAll(igloo, vacChamber, detector1, detector2, detector3);
        smoosh.containsMaterialAt("Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(smoosh, null, visualizations);
    }

    public static MonteCarloSimulation spherical(Group visualizations) {
        double thickness = 0.025; //block thickness in cm
        String m = "HydrogenWax";
        //String m = "CarbonWax";
        //String m = "Paraffin";

        Part wall = new Part("Block: " + m, new Shape(new CuboidMesh(thickness, thickness, thickness)), m);
        wall.getTransforms().add(new Translate(50, 0, 0));
        wall.setColor("silver");

        Part detector = new Part("Spherical detector", new Shape(TestGM.class.getResource("/meshes/spherical_detector.stl"), "cm"), "HighVacuum");
        detector.getTransforms().add(new Translate(50, 0, 0));
        detector.setColor("pink");

        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall, detector);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer,
                null, Vector3D.PLUS_I, 0.0253 * Util.Physics.eV, // origin = (0,0,0), random dir, default DD-neutron energy+1 KeV
                "Vacuum", null, visualizations); // interstitial, initial
        return mcs;
    }

    public static MonteCarloSimulation simulationTestWhitmerCarbon(Group visualizations) {
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
                null, Vector3D.PLUS_I, 2.451e6 * Util.Physics.eV, // origin = (0,0,0), random dir, default DD-neutron energy+1 KeV
                "Vacuum", null, visualizations); // interstitial, initial
        return mcs;
    }

    public static MonteCarloSimulation simulationTestWhitmerParaffin(Group visualizations) {
        double thickness = 0.025; //block thickness in cm
        Shape blockShape = new Shape(new CuboidMesh(thickness, 100, 100));
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

    public static MonteCarloSimulation simulationTestSettle(Group visualizations) {
        double gap = 0.025; //block thickness in cm

        Part block1 = new Part("block", new Shape(TestGM.class.getResource("/meshes/baseparaffinblock.obj"), "cm"), "Paraffin");
        block1.getTransforms().add(0, new Rotate(90, new Point3D(1, 0, 0)));
        block1.getTransforms().add(0, new Translate(50, 50, -25));
        block1.setColor("lightblue");

        Part block2 = new Part("block", new Shape(TestGM.class.getResource("/meshes/baseparaffinblock.obj"), "cm"), "Paraffin");
        block2.getTransforms().add(0, new Rotate(90, new Point3D(1, 0, 0)));
        block2.getTransforms().add(0, new Translate(50, -100, -25));
        block2.setColor("lightgreen");

        System.out.println("");
        System.out.println("Distance Y-axis (before settle): " + block2.distance(block1, Vector3D.PLUS_J));
        Translate tx = block2.settleAgainst(block1, Vector3D.PLUS_J);
        System.out.println("Transform: " + tx);
        block2.getTransforms().add(0, tx);
        System.out.println("Distance X-axis: " + block2.distance(block1, Vector3D.PLUS_I));
        System.out.println("Distance Y-axis: " + block2.distance(block1, Vector3D.PLUS_J));
        System.out.println("Distance Z-axis: " + block2.distance(block1, Vector3D.PLUS_K));
        System.out.println("");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));
        Part detector1 = new Part("Detector behind wall", detectorShape, "HighVacuum");
        detector1.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1.setColor("pink");

        Assembly a = new Assembly("Blocks");
        a.addAll(block1, block2, detector1);

        MonteCarloSimulation mcs = new MonteCarloSimulation(a,
                null, null, 0, // origin = (0,0,0), random dir, default DD-neutron energy
                null, null, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation simulationTestWhitmer(Group visualizations) {

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
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall, detector1, detector2, vacChamber);
        //whitmer.addTransform(new Translate(0, -100, 0));

        whitmer.containsMaterialAt(Vacuum.getInstance(), Vector3D.ZERO);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer, Vector3D.ZERO, visualizations);
        return mcs;
    }

    public static MonteCarloSimulation simulationTest2(Group visualizations) {
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
        //System.out.println("Macroscopic total cross-section for paraffin: "+Paraffin.getInstance().getSigma(1*Util.Physics.eV));
        //
        // The detector is made from a stock - FXyz CuboidMesh
        //
        double s = 20;
        Shape detectorShape = new Shape(new CuboidMesh(s, 3 * s, 5 * s));
        // move detector behind cube wall
        detectorShape.getTransforms().add(new Translate(200, 0, 0));
        Part detector = new Part("Detector 1", detectorShape, "Vacuum");

        //
        // body
        //
        Shape bodyShape = new HumanBody();
        //bodyShape.getTransforms().add(0,new Rotate(90, new Point3D(1,0,0)));
        bodyShape.getTransforms().add(0, new Translate(0, 0, -200));
        Part body = new Part("Body", bodyShape, "HumanBodyMaterial");

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(igloo, detector, body, vacChamber);
        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);
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

        Isotope is = E12C.getInstance();

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

        Shape cube3 = new Shape(TestGM.class.getResource("/meshes/cube.obj"));
        cube3.setTranslateX(100);
        cube3.setTranslateY(100);
        cube3.setTranslateZ(100);
        cube3.setRotationAxis(new Point3D(-1, -1, 1));

        Shape body = new Shape(TestGM.class.getResource("/meshes/body.obj"));
        body.setScaleX(100);
        body.setScaleY(100);
        body.setScaleZ(100);

        body.setTranslateX(200);
        body.setTranslateY(200);
        body.setTranslateZ(200);
        body.setRotationAxis(new Point3D(-1, -1, -1));
        System.out.println("Volume:" + body.getVolume());
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
}
