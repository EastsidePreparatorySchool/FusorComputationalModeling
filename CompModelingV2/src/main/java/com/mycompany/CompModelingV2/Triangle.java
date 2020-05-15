/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.CompModelingV2;

import java.util.Random;

/**
 *
 * @author subif
 */
public class Triangle {
 
    public Vector[] points;
    public int polarity;
    public Double surfaceArea;
    
    Triangle(){}
    Triangle(Vector A, Vector B, Vector C, int polarity){
        this.points = new Vector[]{A, B, C};
        this.polarity = polarity;
        this.surfaceArea = getSurfaceArea();
    }
    Triangle(Vector[] points, int polarity){
        this.points = points;
        this.polarity = polarity;
        this.surfaceArea = getSurfaceArea();
    }
    public int getPolarity(){
        return polarity;
    }
    
    public void translate(Vector offset){
        for(Vector point: points){
            point.plusEquals(offset);
        }
    }
    
    public Double getSurfaceArea(){
        // according to stack exchange
        // https://math.stackexchange.com/questions/128991/how-to-calculate-the-area-of-a-3d-triangle
        // three points in 3d A, B, and C
        // first find angle theta between AB and AC
        // then find area of triangle using
        // Area= 1/2|AB||AC|sin(theta)
       
        Vector AB = points[0].thisToThat(points[1]);

        if(AB.norm().isNaN()){
            System.out.println("problem AB");
        }
        Vector AC = points[0].thisToThat(points[2]);
        if(AC.norm().isNaN()){
            System.out.println("Problem AC");
        }
        if(points[0].equals(points[1])){
            System.out.println("hoijo");
        }
        if(points[1].equals(points[2])){
            System.out.println("ueoi");
        }
        Double theta = AB.angleBetween(AC);
        if(theta.isNaN()){
            System.out.println("problem theta");
        }
        surfaceArea = 0.5*AB.norm()*AC.norm()*(Math.sin(theta));
        return surfaceArea;
    }
    /**
     * Generates a random vector on this triangle
     * Then creates a charge at that point with the polarity of the triangle
     * @return A random charge on this triangle 
     */
    public Charge genRandCharge(){
        //generate a random point p uniformly from within triangle ABC
        //https://math.stackexchange.com/questions/18686/uniform-random-point-in-triangle
        Charge charge = new Charge();
        Random randGen = new Random();
        double r1 = randGen.nextDouble(); // generates a double between 0.0 and 1.0
        double r2 = randGen.nextDouble();
        double sqr1 = Math.sqrt(r1);
        charge.x = (points[0].x*(1-sqr1))+(points[1].x*(sqr1*(1-r2)))+(points[2].x*r2*sqr1);
        charge.y = (points[0].y*(1-sqr1))+(points[1].y*(sqr1*(1-r2)))+(points[2].y*r2*sqr1);
        charge.z = (points[0].z*(1-sqr1))+(points[1].z*(sqr1*(1-r2)))+(points[2].z*r2*sqr1);
        charge.polarity = polarity;
        Float f = 0.0f;
        float fe = 0.0f;
        return charge;
    }   
    @Override
    public String toString(){
        String triangle = "";
        triangle += "Point A: " +points[0] + " \n";
        triangle += "Point B: " +points[1] + " \n";
        triangle += "Point C: " +points[2] + " \n";
        return triangle;
    }
    
}