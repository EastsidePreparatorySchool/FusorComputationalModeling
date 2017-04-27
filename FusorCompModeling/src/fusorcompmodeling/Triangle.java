/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import static java.lang.Double.NaN;
import java.util.*;
import java.util.Random;

/**
 *
 * @author ethan
 */
public class Triangle extends GridComponent {
    
    Point[] points;
    
    
    public Triangle(Point[] points, int charge) {
        this.points = points;
        this.charge = charge;
        this.type = ComponentType.Triangle;
        this.surfaceArea = getSurfaceArea();
    }
    
    
    public Point getRandomPoint(Random r) {
        
        double r1 = Math.random(); 
        double r2 = Math.random();
        Point p = new Point();
        //Generate two random numbers and a new point
        
        p.x = (1 - Math.sqrt(r1)) * points[0].x + (Math.sqrt(r1) * (1 - r2)) * points[1].x + (Math.sqrt(r1) * r2) * points[2].x;
        p.y = (1 - Math.sqrt(r1)) * points[0].y + (Math.sqrt(r1) * (1 - r2)) * points[1].y + (Math.sqrt(r1) * r2) * points[2].y;
        p.z = (1 - Math.sqrt(r1)) * points[0].z + (Math.sqrt(r1) * (1 - r2)) * points[1].z + (Math.sqrt(r1) * r2) * points[2].z;
        p.charge = charge;
        //Use a formula to generate a random coordinate that is on triangle, and set it to the point
        
        return p;
    }

    @Override
    public double getSurfaceArea() {
        
        double[] v1 = new double[3];
        double[] v2 = new double[3];
        double[] cross = new double[3];
        double norm;
        double surfaceArea;
        
        v1[0] = points[1].x - points[0].x;
        v1[1] = points[1].y - points[0].y;
        v1[2] = points[1].z - points[0].z;
        //vector one is second point minus first point
        
        v2[0] = points[2].x - points[0].x;
        v2[1] = points[2].y - points[0].y;
        v2[2] = points[2].z - points[0].z;
        //vector two is third point minus first point
        
        cross[0] = v1[1]*v2[2] - v1[2]*v2[1]; 
        cross[1] = v1[0]*v2[2] - v1[2]*v2[0];
        cross[2] = v1[0]*v2[1] - v1[1]*v2[0];
        //use cross rule to multiply vector 1 and two, making the cross vector
        
        norm = Math.sqrt(Math.pow(cross[0], 2) + Math.pow(cross[1], 2) + Math.pow(cross[2], 2));
        //find magnitude of cross vector
        
        surfaceArea = norm/2;
        //because it is area of triangle, divide by 2

        if (surfaceArea == NaN) {
            System.out.println("getSurfaceArea returned a NAN value!");
        }
        return surfaceArea;
    }

    
}
