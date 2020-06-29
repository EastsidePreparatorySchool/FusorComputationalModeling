/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import com.google.gson.Gson;
import java.net.URL;
import java.io.IOException;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.assemblies.Assembly;
import org.eastsideprep.javaneutrons.assemblies.Element;
import org.eastsideprep.javaneutrons.shapes.Shape;
import org.eastsideprep.javaneutrons.assemblies.Part;
import org.eastsideprep.javaneutrons.core.MonteCarloSimulation;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.materials.HumanBodyMaterial;
import org.eastsideprep.javaneutrons.materials.Hydrogen;
import org.eastsideprep.javaneutrons.materials.Paraffin;
import org.eastsideprep.javaneutrons.materials.Steel;
import org.eastsideprep.javaneutrons.materials.Vacuum;
import org.eastsideprep.javaneutrons.shapes.Cuboid;
import org.eastsideprep.javaneutrons.shapes.HumanBody;
import org.eastsideprep.javaneutrons.shapes.Shape;
import org.fxyz3d.shapes.primitives.CuboidMesh;
import java.io.FileWriter;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
import org.eastsideprep.javaneutrons.Test;
import org.eastsideprep.javaneutrons.assemblies.AssemblyBuilder;

/**
 *
 * @author etardif
 */
public class AssemblyJSONConverter {
    
    final static private Gson gson = new Gson();
    
    void saveToJSON(Assembly a){
        String may=System.getProperty("user.dir");
        String x = may+"/src/main/resources/JSONassemblies";
        try {
            FileWriter f =new FileWriter(x);
            AssemblyBuilder temp = jsonbuild(a);
            gson.toJson(2123, f); //should be temp
        } catch (IOException e) {
            System.err.println("Error reading saving JSON file \n" + x+"\n"+e);
        }
    }
    
    AssemblyBuilder jsonbuild(Assembly a){
        return null;
    }
    
    Assembly assemblybuild(AssemblyBuilder b){
        return null;
    }
    
//    Assembly convertFromJSON(String name){
//     //   AssemblyObject=rendered=gson.;
//        URL u = Test.class.getResource("/JSONassemblies/+"+name+".json");
//        
//        gson.fromJson(, classOfT)
//        AssemblyBuilder input = gson.fromJson(u.toString(), AssemblyBuilder);
//        return null;
//    }
}
