/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.Random;

/**
 *
 * @author guberti
 */
public class TorusSegment extends GridComponent {
    final double phi2;
    double phi3;
    double radius2;
    
    public TorusSegment (VectorB pos, double radius, double phi2, double phi3, double radius2, int charge, boolean fV) {
        System.out.println(pos.toString() + ", radius1: " + radius + ", radius2: " + radius2 + ", phi2: " + phi2 + ", phi3: " + phi3 + ", charge: " + charge);
        this.pos = pos;
        this.radius = radius;
        this.phi2 = phi2;
        this.phi3 = phi3;
        this.radius2 = radius2; // Width of the torus
        this.charge = charge;
        this.flipVertical = fV;
        this.type = ComponentType.TorusSegment;
        this.surfaceArea = getSurfaceArea();
    }
    
    @Override
    public double getSurfaceArea() {
        return (4 * Math.pow(Math.PI, 2) * radius * radius2) * (Math.abs(phi3)/Math.PI*2);
    }
    
    @Override
    public Point getRandomPoint(Random r) {
        double pointPhi = phi2 + (phi3) * r.nextDouble();
        double pointTheta = r.nextDouble() * Math.PI * 2;
        
        // Points without rotation
        Point p = new Point();
        
        p.y = pos.y + radius2 * Math.sin(pointTheta);
        
        double a = radius2 * Math.cos(pointTheta);
        p.x = pos.x + (radius + a) * Math.cos(pointPhi);
        p.z = pos.z + (radius + a) * Math.sin(pointPhi);
        p.charge = charge;
        
        p = p.rotateAroundVector(pos);
        
        if (flipVertical) {p.y *= -1;}
        
        return p;
    }
    
    @Override
    public String toString() {
        return "Torus with radii " + radius + ", " + radius2 +
                " and position [" + pos.x + ", " + pos.y + ", " + pos.z;
    }
}
