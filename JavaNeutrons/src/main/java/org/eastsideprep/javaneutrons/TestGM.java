/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.assemblies.Assembly;
import org.eastsideprep.javaneutrons.assemblies.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.shapes.Cuboid;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.eastsideprep.javaneutrons.shapes.Shape;
import org.fxyz3d.shapes.primitives.CuboidMesh;

/**
 *
 * @author gmein
 */
public class TestGM {

    public static MonteCarloSimulation simulationTest(Group visualizations) {
        return simulationTestWhitmer(visualizations);
    }

    public static MonteCarloSimulation simulationTestSmoosh(Group visualizations) {

        double gap = 1;

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        Shape iglooShape = new Shape(TestGM.class.getResource("/smoosh.obj"), "cm");
        iglooShape.getTransforms().add(0, new Rotate(90, new Point3D(1, 0, 0)));
        iglooShape.getTransforms().add(0, new Translate(0, 63, 0));
        iglooShape.setColor("silver");
        Part igloo = new Part("Wall1", iglooShape, "Paraffin");

        Shape detector1Shape = new Shape(new CuboidMesh(2, 100, 100));
        detector1Shape.getTransforms().add(0, new Translate(-(100 + 1), 0, 0));
        detector1Shape.setColor("pink");
        Part detector1 = new Part("Left detector", detector1Shape, "Vacuum");

        Shape detector2Shape = new Shape(new CuboidMesh(2, 100, 100));
        detector2Shape.getTransforms().add(0, new Rotate(90, new Point3D(0, 0, 1)));
        detector2Shape.getTransforms().add(0, new Translate(0, 100 + 1, 0));
        detector2Shape.setColor("pink");
        Part detector2 = new Part("Bottom detector", detector2Shape, "Vacuum");

        Shape detector3Shape = new Shape(new CuboidMesh(2, 100, 100));
        detector3Shape.getTransforms().add(0, new Rotate(90, new Point3D(0, 1, 0)));
        detector3Shape.getTransforms().add(0, new Translate(0, 0, 100 + 1));
        detector3Shape.setColor("pink");
        Part detector3 = new Part("Back detector", detector3Shape, "Vacuum");

        // assemble the Fusor out of the other stuff
        Assembly smoosh = new Assembly("Smooshed igloo");
        smoosh.addAll(igloo, vacChamber, detector1, detector2, detector3);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(smoosh, Vector3D.ZERO, visualizations);
    }

    public static MonteCarloSimulation simulationTestWhitmer(Group visualizations) {

        Shape blockShape = new Shape(new CuboidMesh(25, 100, 100));
        blockShape.getTransforms().add(new Translate(50 + 12.5, 0, 0));
        blockShape.setColor("silver");
        Part wall = new Part("Wall", blockShape, "Paraffin");

        Shape detector1Shape = new Shape(new CuboidMesh(2, 100, 100));
        detector1Shape.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1Shape.setColor("pink");
        Part detector1 = new Part("Detector behind paraffin block", detector1Shape, "Vacuum");

        Shape detector2Shape = new Shape(new CuboidMesh(2, 100, 100));
        detector2Shape.getTransforms().add(new Translate(-(100 + 1), 0, 0));
        detector2Shape.setColor("pink");
        Part detector2 = new Part("Detector opposite paraffin block", detector2Shape, "Vacuum");

        // assemble the Fusor out of the other stuff
        Assembly whitmer = new Assembly("Whitmer");
        whitmer.addAll(wall, detector1, detector2/*, p*/);

        return new MonteCarloSimulation(whitmer, Vector3D.ZERO, visualizations);
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

        Sphere s = new Sphere(100);
        Util.Graphics.setColor(s, "gray");

        for (int i = 0; i < 10000; i++) {
            Vector3D v = Util.Math.randomDir().scalarMultiply(100);
            Sphere p = new Sphere(1);
            Util.Graphics.setColor(p, "red");
            p.getTransforms().add(new Translate(v.getX(), v.getY(), v.getZ()));
            g.getChildren().add(p);
            Util.Graphics.drawCoordSystem(g);
        }

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
