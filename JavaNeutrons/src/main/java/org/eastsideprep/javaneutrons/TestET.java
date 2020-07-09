/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.materials.HumanBodyMaterial;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.eastsideprep.javaneutrons.core.Shape;
import org.fxyz3d.shapes.primitives.CuboidMesh;
import com.google.gson.Gson;
import org.eastsideprep.javaneutrons.core.AssemblyJSONConverter;
import org.eastsideprep.javaneutrons.core.AssemblyJSONConverter.AssemblyBuilder;
import org.eastsideprep.javaneutrons.materials.Vacuum;

/**
 *
 * @author gunnar
 */
public class TestET {

    final static private Gson gson = new Gson();
    //   final static private GsonBuilder gdaughter = new GsonBuilder();

    public static MonteCarloSimulation simulationTest(Group visualizations) {
        AssemblyJSONConverter caravan = new AssemblyJSONConverter();


        Assembly basicbodies = caravan.convertFromJSON("AssembleTest3");

        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(basicbodies);
        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);
//        fusor.containsMaterialAt(Vacuum.getInstance(), Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);
       
        
//        String may=System.getProperty("user.dir");
//        String filename="Lets_do_this_one_last_time";
//        String x = may+"\\src\\main\\resources\\JSONassemblies\\"+filename+".json";
//        System.out.println(x);
//        try {
//            FileWriter f =new FileWriter(x); 
//            f.write(gson.toJson("I was beginning to get worried")); //should be temp
//            f.close();
//  //          f.write(AssemblyBuilder.toJSONString());
//        } catch (IOException e) {
//            System.err.println("Error saving JSON file \n" + x+"\n"+e);
//        }
//        AssemblyBuilder a = caravan.assemblybuildtest(6302020,100,"Remember this", true); // try saving arrayList 
//        caravan.saveToJSON(a, "Here");                                                    // of 
//        AssemblyBuilder fd=caravan.convertFromJSONtest("Here"); 
//        
//        System.out.println(fd.no);
//        System.out.println(fd.rivers);
//        System.out.println(fd.sure);
//        System.out.println(fd.what);
//        
//         AssemblyBuilder a = caravan.assemblybuildtest2();
//          caravan.saveToJSON(a, "BuildTest2");
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
        Assembly igloo = new Assembly("igloo", TestET.class.getResource("/meshes/igloo.obj"), "Paraffin");
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

        //
        //
        // extra bodies
        //
        Shape bodyShape2 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape2.getTransforms().add(new Translate(0, 0, -400));
        bodyShape2.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape2.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape2.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body2 = new Part("Body", bodyShape2, HumanBodyMaterial.getInstance());

        Shape bodyShape3 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape3.getTransforms().add(new Translate(-200, 0, 0));
        bodyShape3.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape3.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
        bodyShape3.getTransforms().add(new Rotate(180, new Point3D(0, 0, 1)));
        bodyShape3.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body3 = new Part("Body", bodyShape3, HumanBodyMaterial.getInstance());

        Shape bodyShape4 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape4.getTransforms().add(new Translate(0, -150, 0));
        bodyShape4.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape4.getTransforms().add(new Rotate(0, new Point3D(0, 1, 0)));
        bodyShape4.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape4.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body4 = new Part("Body", bodyShape4, HumanBodyMaterial.getInstance());

        Shape bodyShape5 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape5.getTransforms().add(new Translate(0, 0, 200));
        bodyShape5.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape5.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
        bodyShape5.getTransforms().add(new Rotate(180, new Point3D(0, 0, 1)));
        bodyShape5.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body5 = new Part("Body", bodyShape5, HumanBodyMaterial.getInstance());

        Shape bodyShape6 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape6.getTransforms().add(new Translate(-75, 0, -600));
        bodyShape6.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape6.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape6.getTransforms().add(new Scale(73, 73, 73));
        Part body6 = new Part("Body", bodyShape6, HumanBodyMaterial.getInstance());

        Shape bodyShape7 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape7.getTransforms().add(new Translate(-25, 0, -600));
        bodyShape7.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape7.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape7.getTransforms().add(new Scale(73, 73, 73));
        Part body7 = new Part("Body", bodyShape7, HumanBodyMaterial.getInstance());

        Shape bodyShape8 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape8.getTransforms().add(new Translate(25, 0, -600));
        bodyShape8.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape8.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape8.getTransforms().add(new Scale(73, 73, 73));
        Part body8 = new Part("Body", bodyShape8, HumanBodyMaterial.getInstance());

        Shape bodyShape9 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape9.getTransforms().add(new Translate(75, 0, -600));
        bodyShape9.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape9.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape9.getTransforms().add(new Scale(73, 73, 73));
        Part body9 = new Part("Body", bodyShape9, HumanBodyMaterial.getInstance());

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestET.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(igloo, detector, body, vacChamber, body2, body3, body4, body5, body6, body7, body8, body9);
        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);

        // vac chamber
    }

    public static MonteCarloSimulation simulationTest3(Group visualizations) {
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
        Assembly igloo = new Assembly("igloo", TestET.class.getResource("/meshes/igloo.obj"), "Paraffin");
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

        //
        //
        // extra bodies
        //
        Shape bodyShape2 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape2.getTransforms().add(new Translate(0, 0, -400));
        bodyShape2.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape2.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape2.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body2 = new Part("Body", bodyShape2, HumanBodyMaterial.getInstance());

        Shape bodyShape3 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape3.getTransforms().add(new Translate(-200, 0, 0));
        bodyShape3.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape3.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
        bodyShape3.getTransforms().add(new Rotate(180, new Point3D(0, 0, 1)));
        bodyShape3.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body3 = new Part("Body", bodyShape3, HumanBodyMaterial.getInstance());

        Shape bodyShape4 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape4.getTransforms().add(new Translate(0, -150, 0));
        bodyShape4.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape4.getTransforms().add(new Rotate(0, new Point3D(0, 1, 0)));
        bodyShape4.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape4.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body4 = new Part("Body", bodyShape4, HumanBodyMaterial.getInstance());

        Shape bodyShape5 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape5.getTransforms().add(new Translate(0, 0, 200));
        bodyShape5.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape5.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
        bodyShape5.getTransforms().add(new Rotate(180, new Point3D(0, 0, 1)));
        bodyShape5.getTransforms().add(new Scale(73, 73, 73));
        //System.out.println("Body volume in cm^3: " + bodyShape.getVolume());
        Part body5 = new Part("Body", bodyShape5, HumanBodyMaterial.getInstance());

        Shape bodyShape6 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape6.getTransforms().add(new Translate(-75, 0, -600));
        bodyShape6.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape6.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape6.getTransforms().add(new Scale(73, 73, 73));
        Part body6 = new Part("Body", bodyShape6, HumanBodyMaterial.getInstance());

        Shape bodyShape7 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape7.getTransforms().add(new Translate(-25, 0, -600));
        bodyShape7.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape7.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape7.getTransforms().add(new Scale(73, 73, 73));
        Part body7 = new Part("Body", bodyShape7, HumanBodyMaterial.getInstance());

        Shape bodyShape8 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape8.getTransforms().add(new Translate(25, 0, -600));
        bodyShape8.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape8.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape8.getTransforms().add(new Scale(73, 73, 73));
        Part body8 = new Part("Body", bodyShape8, HumanBodyMaterial.getInstance());

        Shape bodyShape9 = new Shape(TestET.class.getResource("/body.obj"));
        bodyShape9.getTransforms().add(new Translate(75, 0, -600));
        bodyShape9.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        bodyShape9.getTransforms().add(new Rotate(90, new Point3D(0, 0, 1)));
        bodyShape9.getTransforms().add(new Scale(73, 73, 73));
        Part body9 = new Part("Body", bodyShape9, HumanBodyMaterial.getInstance());

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestET.class.getResource("/meshes/vac_chamber.obj")), "Steel");

        // assemble the Fusor out of the other stuff
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(igloo, detector, body, vacChamber);
        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);

        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);

        // vac chamber
    }
}
