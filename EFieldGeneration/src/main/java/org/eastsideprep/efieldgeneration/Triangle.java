/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.efieldgeneration;

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
    /**
     * 
     * @return The polarity (-1 or +1) of this triangle
     */
    public int getPolarity(){
        return polarity;
    }
    
    /**
     * translates the triangle's coordinates by an offset vector
     * @param offset vector to add to the triangle's coordinates
     */
    public void translate(Vector offset){
        for(Vector point: points){
            point.plusEquals(offset);
        }
    }
    
    /**
     * multiplies each component of each point of a triangle by the
     * corresponding component of the factor vector
     * @param factor 
     */
    public void multiply(Vector factor){
        for(Vector point: points){
            point.multiply(factor);
        }
    }
    
    /**
     * Gets the surface area of this triangle
     * @return the surface area of the triangle as a double
     */
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
        charge.pos.x = (points[0].x*(1-sqr1))+(points[1].x*(sqr1*(1-r2)))+(points[2].x*r2*sqr1);
        charge.pos.y = (points[0].y*(1-sqr1))+(points[1].y*(sqr1*(1-r2)))+(points[2].y*r2*sqr1);
        charge.pos.z = (points[0].z*(1-sqr1))+(points[1].z*(sqr1*(1-r2)))+(points[2].z*r2*sqr1);
        charge.polarity = polarity;

        return charge;
    }   
    
    /**
     * Test generates a random charge on the surface of this triangle
     * @return random charge on this triangle
     */
    public Charge testGenRandCharge(){
        // doesn't work right now
        // if tested with the two files "ThinPlate.stl" and "ThinRightPlate.stl"
        // creates a really neat x pattern instead of the squares
        //https://jsfiddle.net/jniac/fmx8bz9y/
        Charge charge = new Charge();
        Random randGen = new Random();
        double r1 = randGen.nextDouble();
        double r2 = randGen.nextDouble();
        if(r1+r2>1){
            r1 = 1-r1;
            r2 = 1-r2;
        }
        
        charge.pos.x = points[0].x + points[0].thisToThat(points[1]).x*r1 + points[1].thisToThat(points[2]).x*r2;
        charge.pos.y = points[0].y + points[0].thisToThat(points[1]).y*r1 + points[1].thisToThat(points[2]).y*r2;
        charge.pos.z = points[0].z + points[0].thisToThat(points[1]).z*r1 + points[1].thisToThat(points[2]).z*r2;
        charge.polarity = polarity;
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
