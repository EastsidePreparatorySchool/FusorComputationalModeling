/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import com.google.gson.Gson;
import java.io.FileReader;
import java.net.URL;
import java.io.IOException;
import org.eastsideprep.javaneutrons.core.Assembly;
import org.eastsideprep.javaneutrons.core.Shape;
import org.eastsideprep.javaneutrons.core.Part;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.eastsideprep.javaneutrons.TestET;
import org.eastsideprep.javaneutrons.core.AssemblyJSONConverter.AssemblyBuilder.BuilderComp;
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

    public void saveToJSON(Assembly a, String filename) {
        String may = System.getProperty("user.dir");
        String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";
        try {
            FileWriter f = new FileWriter(x);
            //   AssemblyBuilder temp = AssemblytoBuilder(a);

            //   f.write(gson.toJson(temp));
            f.close();
        } catch (IOException e) {
            System.err.println("Error saving JSON file \n" + x + "\n" + e);
        }
    }
//    AssemblyBuilder AssemblytoBuilder (Assembly a) {
//        AssemblyBuilder b= new AssemblyBuilder(a.name);
//        for(int i =0; i< a.getGroup().getChildren().size(); i++){
//            Part p = (Part) a.getGroup().getChildren().get(i);
//            BuilderComp c = new BuilderComp();
//        }
//        return null;
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
//    public AssemblyBuilder assemblybuildtest(int w, int s, String d, boolean e) {
//        AssemblyBuilder a = new AssemblyBuilder(w, s, d, e);
//        return a;
//    }

    public class AssemblyBuilder {
//        public int sure;
//        public int what;
//        public String no;
//        public Boolean rivers;
//        
        //        AssemblyBuilder(int w, int s, String d, boolean e) {
//            sure = w;
//            what = s;
//            no = d;
//            rivers = e;
////            components.add(new BuilderComp());
////            components.add(new BuilderComp()); //just testing
////            components.add(new BuilderComp());
//        }

        public String name;
        public ArrayList<BuilderComp> components = new ArrayList<BuilderComp>();

        AssemblyBuilder(String name) {
            this.name = name;
        }

        public void add(BuilderComp b) {
            components.add(b);
        }

        public class BuilderComp {

            String material_name = "Copper";
            String name = "LondonCalling";
            String filelocation = "/meshes/filename.obj";
            String color = "blue";
            int x_rotation = 0;
            int y_rotation = 0;
            int z_rotation = 0;
            int x_transform = 0;
            int y_transform = 0;
            int z_transform = 0;
            int x_scale=1;
            int y_scale=1;
            int z_scale=1;

            public BuilderComp(String a, String b, String c) {
                material_name = a;
                name = b;
                filelocation = c;
            }
            public BuilderComp(String a, String b, String c, int xr, int yr, int zr, int xtran, int ytran, int ztran, int q, int w, int e) {
                material_name = a;
                name = b;
                filelocation = c;
                x_rotation = xr;
                y_rotation = yr;
                z_rotation = zr;
                x_transform = xtran;
                y_transform = ytran;
                z_transform = ztran;
                x_scale=q;
                y_scale=w;
                z_scale=e;
            }
            public BuilderComp(){
            
            }
        }
        AssemblyBuilder(String s, Boolean x) {
            this.name = s;
            BuilderComp b = new BuilderComp("d", "sdf", "sdf");
            components.add(b);
            BuilderComp c = new BuilderComp();
            components.add(c);
        }
    }

    Assembly BuildertoAssembly(AssemblyBuilder b) {
        Assembly a = new Assembly(b.name);
        for (BuilderComp i : b.components) {
            Material m = Material.getRealMaterial(i.material_name);
            URL u = TestET.class.getResource(i.filelocation);
            Shape s = new Shape(u);
            Part p = new Part(i.name, s, m);
            
            p.getTransforms().add(0, new Rotate(i.x_rotation, new Point3D(1, 0, 0)));
            p.getTransforms().add(0, new Rotate(i.y_rotation, new Point3D(0, 1, 0)));
            p.getTransforms().add(0, new Rotate(i.z_rotation, new Point3D(0, 0, 1)));
            p.getTransforms().add(0, new Translate(i.x_transform, 0-i.y_transform, i.z_transform));
            p.getTransforms().add(0, new Scale(i.x_scale, i.y_scale, i.z_scale));
            p.setColor(i.color);
            
            a.add(p);
        }
//        for (int i=0; i<b.components.size(); i++) {
//            Material m = Material.getRealMaterial(b.components.get(i).material_name);
//            URL u = TestET.class.getResource(b.components.get(i).filelocation);
//            Shape s = new Shape(u);
//            
//            s.getTransforms().add(0, new Rotate(b.components.get(i).x_rotation, new Point3D(1, 0, 0)));
//            s.getTransforms().add(0, new Rotate(b.components.get(i).y_rotation, new Point3D(0, 1, 0)));
//            s.getTransforms().add(0, new Rotate(b.components.get(i).z_rotation, new Point3D(0, 0, 1)));
//            s.getTransforms().add(0, new Translate(b.components.get(i).x_transform, b.components.get(i).y_transform, b.components.get(i).z_transform));
//          //  s.getTransforms().add(0, new Translate(200,0,0));
//            s.setColor(b.components.get(i).color);
//            
//            Part p = new Part(b.components.get(i).name, s, m);
//            a.add(p);
//        }
        return a;

    }

    public AssemblyBuilder assemblybuildtest2() {
        AssemblyBuilder a = new AssemblyBuilder("Requiem", true);

        return a;
    }

    public Assembly convertFromJSON(String filename) {
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

        Assembly b = BuildertoAssembly(a);

        //only works with obj
        return b;
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
