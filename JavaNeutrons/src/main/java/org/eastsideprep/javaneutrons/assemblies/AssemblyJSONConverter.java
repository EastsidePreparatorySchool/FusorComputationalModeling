/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import com.google.gson.Gson;
import java.io.FileReader;
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
import java.util.HashMap;
import org.eastsideprep.javaneutrons.core.LogEnergyEVHistogram;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.eastsideprep.javaneutrons.Test;

/**
 *
 * @author etardif
 */
public class AssemblyJSONConverter {

    final static private Gson gson = new Gson();

//    public void saveToJSON(Assembly a, String filename) {
//        String may = System.getProperty("user.dir");
//        String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";
//        try {
//            FileWriter f = new FileWriter(x);
//            AssemblyBuilder temp = jsonbuild(a);
//
//            f.write(gson.toJson(temp));
//            f.close();
//        } catch (IOException e) {
//            System.err.println("Error saving JSON file \n" + x + "\n" + e);
//        }
//    }

    public void saveToJSON(AssemblyBuilder a, String filename) {
        String may = System.getProperty("user.dir");
        String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";
        try {
            FileWriter f = new FileWriter(x);
            f.write(gson.toJson(a));
            f.close();
        } catch (IOException e) {
            System.err.println("Error saving JSON file \n" + x + "\n" + e);
        }
    }

    AssemblyBuilder jsonbuild(Assembly a) {
        AssemblyBuilder hope = new AssemblyBuilder(1, 2, "df", true);
        return null;
    }

    public class AssemblyBuilder {

        public int sure;
        public int what;
        public String no;
        public Boolean rivers;

        AssemblyBuilder(int w, int s, String d, boolean e) {
            sure = w;
            what = s;
            no = d;
            rivers = e;
        }

        String assembly_name;
        URL assembly_url;
        Object assembly_material;
        String assembly_unit;
        
        public HashMap<String, Part> part_namedParts = new HashMap<>();
        public Shape part_shape;
        public Material part_material;
        public String part_name;

        // universal detector functionality
        public LogEnergyEVHistogram entriesOverEnergy;
        public LogEnergyEVHistogram fluenceOverEnergy;
        public LogEnergyEVHistogram scattersOverEnergyBefore;
        public LogEnergyEVHistogram scattersOverEnergyAfter;
        public LogEnergyEVHistogram capturesOverEnergy;
        private double volume = 0;
        private double currentEntryEnergy = 0;
        private double totalDepositedEnergy = 0;
        private double totalFluence = 0;
        private int totalEvents = 0;

        AssemblyBuilder(String name, URL url, Object material) {
            assembly_name = name;
            assembly_url = url;
            assembly_material = material;
        }

    }

    Assembly assemblybuild(AssemblyBuilder b) {
        return null;
    }

    public AssemblyBuilder assemblybuildtest(int w, int s, String d, boolean e) {
        AssemblyBuilder a = new AssemblyBuilder(w, s, d, e);
        return a;
    }

    public AssemblyBuilder convertFromJSONtest(String filename) {
        String may = System.getProperty("user.dir");
        String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";

        String input = "";
        try {
            FileReader f = new FileReader(x);
            int i;
            while ((i = f.read()) != -1) {
                input += (char) i;
            }
            // System.out.println(input);
            f.close();
        } catch (IOException e) {
            System.err.println("Error reading JSON file \n" + x + "\n" + e);
        }

        AssemblyBuilder a = gson.fromJson(input, AssemblyBuilder.class);

        return a;
    }
}


//    public Assembly convertFromJSON(String filename) {
//        String may = System.getProperty("user.dir");
//        String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";
//
//        String input = "";
//        try {
//            FileReader f = new FileReader(x);
//            int i;
//            while ((i = f.read()) != -1) {
//                input += (char) i;
//            }
//            // System.out.println(input);
//            f.close();
//        } catch (IOException e) {
//            System.err.println("Error reading JSON file \n" + x + "\n" + e);
//        }
//
//        AssemblyBuilder a = gson.fromJson(input, AssemblyBuilder.class);
//        
//        String x1=a.assembly_name;
//        URL x2=a.assembly_url;
//        Material x3= Material.getRealMaterial(a.assembly_material);
//        
//        Assembly b = new Assembly(x1,x2,x3);
//        
//        //only works with obj
//        return b;
//    }

