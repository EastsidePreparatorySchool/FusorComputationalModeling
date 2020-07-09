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

    public class AssemblyBuilder {

        public int sure;
        public int what;
        public String no;
        public Boolean rivers;
        
        
        public String name;
        public ArrayList<BuilderComp> components = new ArrayList<BuilderComp>();
        
        AssemblyBuilder(String name){
            this.name=name;
        }

        public void add(BuilderComp b){
            components.add(b);
        }

        public class BuilderComp{
            String material_name="Copper";
            String name="Gold";
            String location="/meshes/igloo.obj";
            public BuilderComp(String a, String b, String c){
                material_name=a;
                name=b;
                location=c;
            }
            BuilderComp(){};
        }
        AssemblyBuilder(int w, int s, String d, boolean e) {
            sure = w;
            what = s;
            no = d;
            rivers = e;
//            components.add(new BuilderComp());
//            components.add(new BuilderComp()); //just testing
//            components.add(new BuilderComp());
        }
        AssemblyBuilder(String s,Boolean x) {
            this.name=s;
            BuilderComp b = new BuilderComp("d", "sdf", "sdf");
            components.add(b);
            BuilderComp c = new BuilderComp("new", "stuff", "yay");
            components.add(c);
//            components.add(new BuilderComp());
//            components.add(new BuilderComp()); //just testing
//            components.add(new BuilderComp());
        }
    }

    Assembly BuildertoAssembly(AssemblyBuilder b) {
        Assembly a = new Assembly(b.name);
        for(BuilderComp i:b.components){
            Material m = Material.getRealMaterial(i.material_name);
            URL u = TestET.class.getResource(i.location);
            Shape s = new Shape(u);
            Part p = new Part(i.name, s, m);
            a.add(p);
        }
        return a;
        
    }
//    AssemblyBuilder AssemblytoBuilder (Assembly a) {
//        AssemblyBuilder b= new AssemblyBuilder(a.name);
//        for(int i =0; i< a.getGroup().getChildren().size(); i++){
//            Part p = (Part) a.getGroup().getChildren().get(i);
//            BuilderComp c = new BuilderComp();
//        }
//        return null;
//    }
    public AssemblyBuilder assemblybuildtest(int w, int s, String d, boolean e) {
        AssemblyBuilder a = new AssemblyBuilder(w, s, d, e);
        return a;
    }
    public AssemblyBuilder assemblybuildtest2() {
        AssemblyBuilder a = new AssemblyBuilder("Requiem", true);
        
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

}