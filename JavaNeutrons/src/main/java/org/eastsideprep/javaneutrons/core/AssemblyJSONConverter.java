/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import com.google.gson.Gson;
import java.io.File;
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
////            parts.add(new BuilderComp());
////            parts.add(new BuilderComp()); //just testing
////            parts.add(new BuilderComp());
//        }

        public String name;
        public ArrayList<BuilderComp> parts = new ArrayList<BuilderComp>();

        AssemblyBuilder(String name) {
            this.name = name;
        }

        public void add(BuilderComp b) {
            parts.add(b);
        }

        public class BuilderComp {

            String material_name = "Copper";
            String name = "LondonCalling";
            String filename = "/meshes/filename.obj";
            String directorylocation = "SpecifyIfYouWant";
            String color = "blue";
            int x_rotation = 0;
            int y_rotation = 0;
            int z_rotation = 0;
            int x_translate = 0;
            int y_translate = 0;
            int z_translate = 0;
            int x_scale = 1;
            int y_scale = 1;
            int z_scale = 1;

            public BuilderComp(String a, String b, String c) {
                material_name = a;
                name = b;
                filename = c;
            }

            public BuilderComp(String a, String b, String c, int xr, int yr, int zr, int xtran, int ytran, int ztran, int q, int w, int e, String ggh) {
                material_name = a;
                name = b;
                filename = c;
                directorylocation = ggh;
                x_rotation = xr;
                y_rotation = yr;
                z_rotation = zr;
                x_translate = xtran;
                y_translate = ytran;
                z_translate = ztran;
                x_scale = q;
                y_scale = w;
                z_scale = e;
            }

            public BuilderComp() {

            }
        }

        AssemblyBuilder(String s, Boolean x) {
            this.name = s;
            BuilderComp b = new BuilderComp("d", "sdf", "sdf");
            parts.add(b);
            BuilderComp c = new BuilderComp();
            parts.add(c);
        }
    }

    Assembly BuildertoAssembly(AssemblyBuilder b) {
        Assembly a = new Assembly(b.name);
        for (BuilderComp i : b.parts) {
            if (i.directorylocation.equals("SpecifyIfYouWant")) {
                Material m = Material.getRealMaterial(i.material_name);

                URL u = TestET.class.getResource(i.filename);
                String appen = "file:/";
                String may = System.getProperty("user.dir");
                File temp = new File(may); // Specify the filename
                String newdir = temp.getParent() + "\\" + i.filename + ".obj";
               
                Shape s = new Shape(u);
                Part p = new Part(i.name, s, m);

                p.getTransforms().add(0, new Translate(i.x_translate, 0 - i.y_translate, i.z_translate));
                p.getTransforms().add(0, new Rotate(i.x_rotation, new Point3D(1, 0, 0)));
                p.getTransforms().add(0, new Rotate(i.y_rotation, new Point3D(0, 1, 0)));
                p.getTransforms().add(0, new Rotate(i.z_rotation, new Point3D(0, 0, 1)));
                p.getTransforms().add(0, new Scale(i.x_scale, i.y_scale, i.z_scale));
                p.setColor(i.color);
                System.out.println(p.name + " is made of " + p.material);
                a.add(p);
            } else {

            }
        }
//        for (int i=0; i<b.parts.size(); i++) {
//            Material m = Material.getRealMaterial(b.parts.get(i).material_name);
//            URL u = TestET.class.getResource(b.parts.get(i).filelocation);
//            Shape s = new Shape(u);
//            
//            s.getTransforms().add(0, new Rotate(b.parts.get(i).x_rotation, new Point3D(1, 0, 0)));
//            s.getTransforms().add(0, new Rotate(b.parts.get(i).y_rotation, new Point3D(0, 1, 0)));
//            s.getTransforms().add(0, new Rotate(b.parts.get(i).z_rotation, new Point3D(0, 0, 1)));
//            s.getTransforms().add(0, new Translate(b.parts.get(i).x_transform, b.parts.get(i).y_transform, b.parts.get(i).z_transform));
//          //  s.getTransforms().add(0, new Translate(200,0,0));
//            s.setColor(b.parts.get(i).color);
//            
//            Part p = new Part(b.parts.get(i).name, s, m);
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
        //  String x = may + "\\src\\main\\resources\\JSONassemblies\\" + filename + ".json";
        File temp = new File(may); // Specify the filename
        String newdir = temp.getParent() + "\\" + filename + ".json";

        String input = "";
        try {
            FileReader f = new FileReader(newdir);
            int i;
            while ((i = f.read()) != -1) {
                input += (char) i;
            }
            // System.out.println(input);
            f.close();
        } catch (IOException e) {
            System.err.println("Error reading JSON file \n" + newdir + "\n" + e);
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
