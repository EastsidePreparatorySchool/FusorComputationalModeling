/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.*;

/**
 *
 * @author guberti
 */

// I've finished:
// 1. the triangle generator etc.
// 2. Researching how the electric field works (not how the deuterons will react to it though) 
// 3. been rewritting the electric field generation part
public class Cylinder extends GridComponent {

    public Cylinder(VectorB pos, double radius, double height, int charge, boolean fV) {
        this.pos = pos;
        this.radius = radius;
        this.height = height;
        this.charge = charge;
        this.flipVertical = fV;
        this.type = ComponentType.Cylinder;
        this.surfaceArea = getSurfaceArea();
    }

    public double getSurfaceArea() {
        return (Math.PI * radius * 2 * height);
    }

    public Point getRandomPoint(Random rand) {
        double pointPhi = rand.nextDouble() * Math.PI * 2;
        
        Point p = new Point();
        p.y = pos.y + rand.nextDouble() * height;
        p.x = pos.x + radius * Math.cos(pointPhi);
        p.z = pos.z + radius * Math.sin(pointPhi);
        p.charge = charge;
        p = p.rotateAroundVector(pos);
        
        if (flipVertical) {p.y *= -1;}
        
        return p;
    }
    
    public Cylinder() {
    }
}
