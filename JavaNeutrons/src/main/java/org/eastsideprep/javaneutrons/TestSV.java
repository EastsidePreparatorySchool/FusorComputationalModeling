/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.Material;
import org.eastsideprep.javaneutrons.core.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.fxyz3d.shapes.primitives.CuboidMesh;

/**
 *
 * @author gunnar
 */
public class TestSV {
    
    public static MonteCarloSimulation simulationTest(Group visualizations){
            return simpleTest(visualizations);
            //return simpleWaxBlockTest(visualizations);
            //return iglooTest(visualizations);
            //return peopleTest(visualizations);
            //return roomTest(visualizations);
        }
    
    
    
    public static MonteCarloSimulation iglooTest(Group visualizations){
        //shapes for d1 and d2
            Shape detectorShape = new Shape(new CuboidMesh(100, 100, 2));

            Part d1 = new Part("Detector", detectorShape, "Vacuum");
            d1.getTransforms().add(0,new Translate(0, 0, -101)); 
            d1.setColor("green");

            //Assembly d1 = detectorPeople(3,152.4); //3 people 5 feet
            //get paraffin blocks
            Assembly paraffin = new Assembly("Paraffin", TestSV.class.getResource("/meshes/5mmgaps.obj"), "Paraffin");
            
            Assembly fusor = new Assembly("Fusor");
            fusor.addAll(paraffin, d1);
        
            // make some axes
            // Util.Graphics.drawCoordSystem(visualizations);

           return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);
    }
       
    
    public static MonteCarloSimulation simpleTest(Group visualizations) {

        //shapes for d1, d2, and paraffin
        Shape detectorShape = new Shape(new CuboidMesh(100, 100, 2));

        Shape detectorShape2 = new Shape(new CuboidMesh(100, 100, 2));

        Shape blockShape = new Shape(new CuboidMesh(100, 100, 25));
        
        //build parts for d1, d2, and paraffin
        Part d1 = new Part("Detector No Shielding", detectorShape, "Vacuum");
        d1.setColor("green");
        d1.getTransforms().add(0,new Translate(0, 0, -101)); 
        Part d2 = new Part("Detector Post-Shielding", detectorShape2, "Vacuum");
        d2.setColor("green");
        d2.getTransforms().add(0,new Translate(0, 0, 101));
        Part paraffin = new Part("Paraffin Block", blockShape, "Paraffin");
        paraffin.setColor("blue");
        paraffin.getTransforms().add(0,new Translate(0, 0, 62.5));

        //Assembly people = detectorPeople1(3,152.4);       
        
        // assemble
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(paraffin, d1, d2);
        fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);
        
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);

        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);    
    }
        
    
    
     public static MonteCarloSimulation simpleWaxBlockTest(Group visualizations){
            //shapes for d1 and d2
            Shape detectorShape = new Shape(new CuboidMesh(100, 100, 2));       
            Shape detectorShape2 = new Shape(new CuboidMesh(100, 100, 2));
    
            //build d1 and d2
            Part d1 = new Part("Detector No Shielding", detectorShape, "Vacuum");
            d1.getTransforms().add(new Translate(0, 0, -101));  
            Part d2 = new Part("Detector Post-Shielding", detectorShape2, "Vacuum");
            d2.getTransforms().add(new Translate(0, 0, 101));
            
            //get paraffin block
            //Assembly paraffin = new Assembly("Paraffin Block", TestSV.class.getResource("/meshes/flatblock.obj"), "Paraffin");
            //paraffin.getTransforms().add(new Translate(0, 0, 62.5));
            Assembly paraffin = new Assembly("Paraffin Block", TestSV.class.getResource("/meshes/flatblock2.obj"), "Paraffin");
    
            Assembly fusor = new Assembly("Fusor");
           
            fusor.addAll(paraffin, d1, d2);
            //fusor.containsMaterialAt("Vacuum", Vector3D.ZERO);
        
            // make some axes
            Util.Graphics.drawCoordSystem(visualizations);

           return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);
    }
     
     public static MonteCarloSimulation peopleTest(Group visualizations){
                   
            Part paraffin = new Part("5mm gaps", new Shape(TestSV.class.getResource("/meshes/5mmgaps.obj"), "cm"), "Paraffin");
            paraffin.setColor("lightgray");
            
            Assembly fusor = new Assembly("Fusor");
            fusor.addAll(paraffin, detectorPeople(7, 152.4, new Vector3D(0,0,-299), 180, 100));  //n, radius, center point, full angle, angle adjust
        
            // make some axes
            Util.Graphics.drawCoordSystem(visualizations);

           MonteCarloSimulation mcs = new MonteCarloSimulation(fusor,
                null, /*Vector3D.PLUS_I*/ null, 2.45e6 * Util.Physics.eV, // origin = (0,0,0), random dir, default DD-neutron energy+1 KeV
                "Air", null, visualizations, false); // interstitial, initial
           return mcs;
    }
     
    public static Assembly detectorPeople(int n, double distance, Vector3D from, double around, double adjust){

        Assembly detectors = new Assembly("People"); 
        
        for(int i = 0; i<n; i++){
            Shape test = new Shape(new CuboidMesh(100, 100, 2));
            Shape person = new Shape(TestSV.class.getResource("/meshes/body.obj"));
            Part p = new Part("Person."+i,person,"HumanBodyMaterial");
            double angle = i/((double)n)*around + adjust;
            
            
            p.getTransforms().add(0, new Rotate(-90, new Point3D(1, 0, 0)));
            p.getTransforms().add(0, new Rotate(180-angle, new Point3D(0, 0, 1)));//new Rotate(angle-180, new Point3D(0, 1, 0)));
            //p.getTransforms().add(0, new Translate(distance*Math.sin(angle/360*Math.PI*2), 0, distance*Math.cos(angle/360*Math.PI*2)));
            p.getTransforms().add(0, new Translate(distance*Math.sin(angle/360*Math.PI*2), distance*Math.cos(angle/360*Math.PI*2), 0));
            p.getTransforms().add(0, new Translate(from.getX(), from.getY(), from.getZ()));
            p.setColor("lightblue");
            detectors.addAll(p);
        }     
        return detectors;
    }
    
    //some of the dimensions don't make sense, need to fix
//    public static MonteCarloSimulation roomTest(Group visualizations){
//        Part paraffin = new Part("5mmblank", new Shape(TestSV.class.getResource("/meshes/smoosh.obj"), "cm"), "Paraffin");
//        
//        paraffin.setColor("lightgray");
//        double h = 250; //floor to ceiling height of room
//        double roomwidth = 200;
//        
//        double wallwidth = 20;
//        double floorwidth = 20;
//        double ceilingwidth = 20;
//        
//        double floortoparaffin = 80;
//        double sourcetoparaffinbase = 25;
//        double sourcetoback = 70.5;
//        
//        Assembly walls = new Assembly("Walls");
//            Part front = new Part("Wall.front", new Shape(new CuboidMesh(roomwidth+2*wallwidth,h,wallwidth)), "Vacuum");
//                front.getTransforms().add(0, new Translate(0,-0.5*h+sourcetoparaffinbase+floortoparaffin,-299-0.5*wallwidth));
//                front.setColor("lightblue");
//            Part left = new Part("Wall.left", new Shape(new CuboidMesh(wallwidth, h, 299+sourcetoback)), "Vacuum");
//                left.getTransforms().add(0, new Translate(-0.5*roomwidth-0.5*wallwidth,-0.5*h+sourcetoparaffinbase+floortoparaffin,-299/2+0.5*sourcetoback));
//                left.setColor("lightblue");
//            Part right = new Part("Wall.right", new Shape(new CuboidMesh(wallwidth, h, 299+sourcetoback)), "Vacuum");
//                right.getTransforms().add(0, new Translate(0.5*roomwidth+0.5*wallwidth,-0.5*h+sourcetoparaffinbase+floortoparaffin,-299/2+0.5*sourcetoback));
//                right.setColor("lightblue");
//            Part back = new Part("Wall.back", new Shape(new CuboidMesh(roomwidth+2*wallwidth,h,wallwidth)), "Vacuum");
//                back.getTransforms().add(0, new Translate(0,-0.5*h+sourcetoparaffinbase+floortoparaffin,sourcetoback+0.5*wallwidth));
//                back.setColor("lightblue");
//            Part floor = new Part("Wall.floor", new Shape(new CuboidMesh(roomwidth+2*wallwidth,floorwidth,299+sourcetoback+2*wallwidth)), "Vacuum");
//                floor.getTransforms().add(0, new Translate(0,floortoparaffin+sourcetoparaffinbase+0.5*floorwidth,-299/2+0.5*sourcetoback));
//                floor.setColor("lightblue");
//            Part ceiling = new Part("Wall.ceiling", new Shape(new CuboidMesh(roomwidth+2*wallwidth,ceilingwidth,299+sourcetoback+2*wallwidth)), "Vacuum");
//                ceiling.getTransforms().add(0, new Translate(0,-h+sourcetoparaffinbase+floortoparaffin-0.5*ceilingwidth,-299/2+0.5*sourcetoback));
//                ceiling.setColor("lightblue");
//        walls.addAll(front, left, right, back, floor, ceiling);
//        
//        Assembly fusor = new Assembly("Fusor");
//        fusor.addAll(paraffin,walls);
//        
//        // make some axes
//        Util.Graphics.drawCoordSystem(visualizations);
//
//        System.out.println("fusor object: "+fusor);
//        System.out.println("what we will return: "+new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations));
//        return new MonteCarloSimulation(fusor, Vector3D.ZERO, visualizations);
//
//    }
    
    public static MonteCarloSimulation ROOM5mm(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
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

        fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);

        fusor.addTransform(new Rotate(90, new Point3D(1,0,0)));
        
        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,30,-299), 180, 100);
        fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        return mcs;
    }
    
    public static MonteCarloSimulation ROOM1mm(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
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
        Part wax = new Part("Wax", new Shape(TestSV.class.getResource("/meshes/1mmcubes.stl"), "cm"), "Paraffin");
            wax.setColor("lightblue");
        Assembly fusor = new Assembly("Fusor");

        fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);
        //fusor.addAll(wood);
        fusor.addTransform(new Rotate(90, new Point3D(1,0,0)));
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        return mcs;
    }
    

    public static MonteCarloSimulation ROOMnew(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
        //room walls
        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/hi/wfront.stl"), "cm"), "Vacuum");
            wfront.setColor("gray");
        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/hi/wback.stl"), "cm"), "Vacuum");
            wback.setColor("gray");
        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/hi/wfloor.stl"), "cm"), "Vacuum");
            wfloor.setColor("gray");
        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/hi/wceiling.stl"), "cm"), "Vacuum");
            wceiling.setColor("gray");
        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/hi/wleft.stl"), "cm"), "Vacuum");
            wleft.setColor("gray");
        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/hi/wright.stl"), "cm"), "Vacuum");
            wright.setColor("gray");
            
        //important stuff
        Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/hi/wood.stl"), "cm"), "Wood");
            wood.setColor("yellow");
        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/hi/newpipes.obj"), "cm"), "Steel");
            pipes.setColor("gray");
            pipes.getTransforms().add(new Translate(0,0.5,0));
        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/hi/leadbox.stl"), "cm"), "Lead");
            lead.setColor("gray");
        Part wax = new Part("Wax", new Shape(TestSV.class.getResource("/meshes/hi/0mm.stl"), "cm"), "Paraffin");
            wax.setColor("lightblue");
            wax.getTransforms().add(0, new Translate(0,0.5,0));
        Assembly fusor = new Assembly("Fusor");

        //fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);
        fusor.addAll(wood, wax, lead, pipes);
       // fusor.addTransform(new Rotate(90, new Point3D(1,0,0)));
        
        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,30,-299), 180, 100);
      //  fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        return mcs;
    }
    
    public static MonteCarloSimulation BROKENIGLOO(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Lead");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
        //room walls
        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/hi/wfront.stl"), "cm"), "Vacuum");
            wfront.setColor("gray");
        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/hi/wback.stl"), "cm"), "Vacuum");
            wback.setColor("gray");
        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/hi/wfloor.stl"), "cm"), "Vacuum");
            wfloor.setColor("gray");
        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/hi/wceiling.stl"), "cm"), "Vacuum");
            wceiling.setColor("gray");
        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/hi/wleft.stl"), "cm"), "Vacuum");
            wleft.setColor("gray");
        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/hi/wright.stl"), "cm"), "Vacuum");
            wright.setColor("gray");
            
        //metal
      //  Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/hi/wood.stl"), "cm"), "Wood");
      //      wood.setColor("yellow");
        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/hi/newpipes.obj"), "cm"), "Steel");
            pipes.setColor("gray");
            pipes.getTransforms().add(new Translate(0,0.5,0));
        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/hi/leadbox.stl"), "cm"), "Lead");
            lead.setColor("gray");

        Part ptop = new Part("P.Top", new Shape(TestSV.class.getResource("/meshes/broken/top.stl"), "cm"), "Paraffin");
        Part pbase = new Part("P.Base", new Shape(TestSV.class.getResource("/meshes/broken/base.stl"), "cm"), "Paraffin");
        Part pleft = new Part("P.Left", new Shape(TestSV.class.getResource("/meshes/broken/left.stl"), "cm"), "Paraffin");
        Part pright = new Part("P.Right", new Shape(TestSV.class.getResource("/meshes/broken/right.stl"), "cm"), "Paraffin");
        Part pfront = new Part("P.Front", new Shape(TestSV.class.getResource("/meshes/broken/front.stl"), "cm"), "Paraffin");
        Part pback = new Part("P.Back", new Shape(TestSV.class.getResource("/meshes/broken/back.stl"), "cm"), "Paraffin");
            ptop.setColor("lightblue");
            pbase.setColor("lightblue");
            pleft.setColor("lightblue");
            pright.setColor("lightblue");
            pfront.setColor("lightblue");
            pback.setColor("lightblue");
                pbase.getTransforms().add(0, new Translate(0,0.5,0));
                ptop.getTransforms().add(0, new Translate(0,0.5,0));
                pleft.getTransforms().add(0, new Translate(0,0.5,0));
                pright.getTransforms().add(0, new Translate(0,0.5,0));
                pfront.getTransforms().add(0, new Translate(0,0.5,0));
                pback.getTransforms().add(0, new Translate(0,0.5,0));

        //wood
        Part wood1 = new Part("Wood.outerSides", new Shape(TestSV.class.getResource("/meshes/broken/woodouter.stl"), "cm"), "Wood");
        Part wood2 = new Part("Wood.innerSides", new Shape(TestSV.class.getResource("/meshes/broken/woodinner.stl"), "cm"), "Wood");
        Part wood3 = new Part("Wood.base", new Shape(TestSV.class.getResource("/meshes/broken/woodbase.stl"), "cm"), "Wood");
        Part wood4 = new Part("Wood.topShelf", new Shape(TestSV.class.getResource("/meshes/broken/woodtopshelf.stl"), "cm"), "Wood");
        Part wood5 = new Part("Wood.frontDoor", new Shape(TestSV.class.getResource("/meshes/broken/woodfrontdoor.stl"), "cm"), "Wood");
        Part wood6 = new Part("Wood.innerBack", new Shape(TestSV.class.getResource("/meshes/broken/woodinnerback.stl"), "cm"), "Wood");
        Part wood7 = new Part("Wood.outerBack", new Shape(TestSV.class.getResource("/meshes/broken/woodouterback.stl"), "cm"), "Wood");
        
        //assembling and such  
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(vacChamber, pipes, lead, ptop, pbase, pleft, pright, pfront, pback, wfront, wback, wfloor, wceiling, wleft, wright, wood1, wood2, wood3, wood4, wood5, wood6, wood7);
        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,30,-299), 180, 100); //need to move these.
      //  fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        return mcs;
    }

    
    
public static MonteCarloSimulation ThisOneWorks0mm(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Steel");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
        //room walls
        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/hi/wfront.stl"), "cm"), "Vacuum");
            wfront.setColor("gray");
        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/hi/wback.stl"), "cm"), "Vacuum");
            wback.setColor("gray");
        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/hi/wfloor.stl"), "cm"), "Vacuum");
            wfloor.setColor("gray");
        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/hi/wceiling.stl"), "cm"), "Vacuum");
            wceiling.setColor("gray");
        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/hi/wleft.stl"), "cm"), "Vacuum");
            wleft.setColor("gray");
        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/hi/wright.stl"), "cm"), "Vacuum");
            wright.setColor("gray");
            
        //other stuff
        Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/920/plywood.stl"), "cm"), "Wood");
            wood.setColor("yellow");
           
        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/hi/newpipes.obj"), "cm"), "Steel");
            pipes.setColor("gray");
            pipes.getTransforms().add(new Translate(0,0.5,0));
        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/920/leadbox.stl"), "cm"), "Lead");
            lead.setColor("gray");

        Part wax = new Part("0mm wax", new Shape(TestSV.class.getResource("/meshes/920/0mmwax.stl"), "cm"), "Paraffin"); //alternate 0mmnewer.stl
            wax.setColor("lightblue");
                wax.getTransforms().add(0, new Translate(0,0.5,0));

          //assembling and such  
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);
        //fusor.addAll(lead, wax);

        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,-300,-30), 180, 100);//new Vector3D(-20,30,-299), 180, 100);
        fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
        return mcs;
    }
    
    
public static MonteCarloSimulation october(Group visualizations) {

        // vac chamber
        Part vacChamber = new Part("Vacuum chamber", new Shape(TestGM.class
                .getResource("/meshes/vac_chamber.obj")), "Steel");
        vacChamber.setColor(
                "black");
        vacChamber.getTransforms().add(0, new Rotate(90, new Point3D(1,0,0)));
        
        //room walls
        Part wfront = new Part("W.front", new Shape(TestSV.class.getResource("/meshes/hi/wfront.stl"), "cm"), "Vacuum");
            wfront.setColor("gray");
        Part wback = new Part("W.back", new Shape(TestSV.class.getResource("/meshes/hi/wback.stl"), "cm"), "Vacuum");
            wback.setColor("gray");
        Part wfloor = new Part("W.floor", new Shape(TestSV.class.getResource("/meshes/hi/wfloor.stl"), "cm"), "Vacuum");
            wfloor.setColor("gray");
        Part wceiling = new Part("W.ceiling", new Shape(TestSV.class.getResource("/meshes/hi/wceiling.stl"), "cm"), "Vacuum");
            wceiling.setColor("gray");
        Part wleft = new Part("W.left", new Shape(TestSV.class.getResource("/meshes/hi/wleft.stl"), "cm"), "Vacuum");
            wleft.setColor("gray");
        Part wright = new Part("W.right", new Shape(TestSV.class.getResource("/meshes/hi/wright.stl"), "cm"), "Vacuum");
            wright.setColor("gray");
            
        //other stuff
        Part wood = new Part("Wood", new Shape(TestSV.class.getResource("/meshes/920/plywood.stl"), "cm"), "Wood");
            wood.setColor("yellow");
        
//        Part wood1 = new Part("Wood.outerSides", new Shape(TestSV.class.getResource("/meshes/broken/woodouter.stl"), "cm"), "Wood");
//        Part wood2 = new Part("Wood.innerSides", new Shape(TestSV.class.getResource("/meshes/broken/woodinner.stl"), "cm"), "Wood");
//        Part wood3 = new Part("Wood.base", new Shape(TestSV.class.getResource("/meshes/broken/woodbase.stl"), "cm"), "Wood");
//        Part wood4 = new Part("Wood.topShelf", new Shape(TestSV.class.getResource("/meshes/broken/woodtopshelf.stl"), "cm"), "Wood");
//        Part wood5 = new Part("Wood.frontDoor", new Shape(TestSV.class.getResource("/meshes/broken/woodfrontdoor.stl"), "cm"), "Wood");
//        Part wood6 = new Part("Wood.innerBack", new Shape(TestSV.class.getResource("/meshes/broken/woodinnerback.stl"), "cm"), "Wood");
//        Part wood7 = new Part("Wood.outerBack", new Shape(TestSV.class.getResource("/meshes/broken/woodouterback.stl"), "cm"), "Wood");
        
        Part pipes = new Part("Steel Pipes", new Shape(TestSV.class.getResource("/meshes/hi/newpipes.obj"), "cm"), "Steel");
            pipes.setColor("gray");
            pipes.getTransforms().add(new Translate(0,0.5,0.65));
        Part lead = new Part("Lead Box", new Shape(TestSV.class.getResource("/meshes/920/leadbox.stl"), "cm"), "Lead");
            lead.setColor("gray");

        Part wax = new Part("5mm wax", new Shape(TestSV.class.getResource("/meshes/october/4mm.stl"), "cm"), "Paraffin"); //alternate 0mmnewer.stl
            wax.setColor("lightblue");
                wax.getTransforms().add(0, new Translate(0,0.5,0));
        
                //i was playing around with some things
        Part shield1 = new Part("Extra shield wood", new Shape(TestSV.class.getResource("/meshes/extrashield/shieldwood.stl"), "cm"), "Wood"); 
            shield1.setColor("yellow");
           // shield1.getTransforms().add(0, new Translate(0,0.5,0));
        Part shield2 = new Part("Extra shield steel", new Shape(TestSV.class.getResource("/meshes/extrashield/shield5mm.stl"), "cm"), "Steel"); 
            shield2.setColor("black");
        
        
        //assembling and such  
        Assembly fusor = new Assembly("Fusor");
        fusor.addAll(vacChamber, wood, pipes, lead, wax, wfront, wback, wfloor, wceiling, wleft, wright);
        //fusor.addAll(vacChamber, wax, wfront, wback);

        Assembly dp = detectorPeople(7, 152.4, new Vector3D(-20,-300,-30), 180, 100);//new Vector3D(-20,30,-299), 180, 100);
        fusor.addAll(dp);
        
        fusor.containsMaterialAt(
                "Vacuum", Vector3D.ZERO);
        // make some axes
        Util.Graphics.drawCoordSystem(visualizations);
        MonteCarloSimulation mcs = new MonteCarloSimulation(fusor, null, visualizations);
       // mcs.interstitialMaterial = Material.getRealMaterial("Air");
       // System.out.println("mcs.interstitialMaterial is "+mcs.interstitialMaterial.name);
        return mcs;
    }
 
}
