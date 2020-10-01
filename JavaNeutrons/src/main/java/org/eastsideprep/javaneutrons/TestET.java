/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import javafx.scene.Group;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.materials.Vacuum;
import org.fxyz3d.shapes.primitives.CuboidMesh;

/**
 *
 * @author gunnar
 */
public class TestET {

    public static MonteCarloSimulation simulationTest(Group visualizations) {
        //
        return null;
    }
     public static MonteCarloSimulation HWaxNeutronPrison_TwoMeter(Group visualizations) {

        Shape blockShape = new Shape(new CuboidMesh(25, 100, 100));
        Part wall = new Part("Block", blockShape, "Paraffin");
        wall.getTransforms().add(new Translate(50 + 12.5, 0, 0));
        wall.setColor("silver");

        Shape detectorShape = new Shape(new CuboidMesh(2, 100, 100));

        Part detector1 = new Part("Detector behind block", detectorShape, "HighVacuum");
        detector1.getTransforms().add(new Translate(100 + 1, 0, 0));
        detector1.setColor("pink");

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly whitmer = new Assembly("Whitmer");

        whitmer.addAll(wall, detector1, vacChamber);
        //whitmer.addTransform(new Translate(0, -100, 0));

        whitmer.containsMaterialAt(Vacuum.getInstance(), Vector3D.ZERO);

        MonteCarloSimulation mcs = new MonteCarloSimulation(whitmer, Vector3D.ZERO, visualizations);
        return mcs;
    }
}
