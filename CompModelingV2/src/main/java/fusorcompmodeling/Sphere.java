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
public class Sphere extends GridComponent {

    public Sphere(VectorB pos, double radius, int charge) {
        this.pos = pos;
        this.radius = radius;
        this.charge = charge;
        this.type = ComponentType.Sphere;
    }

    public double getSurfaceArea() {
        return (Math.PI * 4 *(radius*radius));
    }

    public Point getRandomPoint(Random rand) {
        double pointPhi = rand.nextDouble() * Math.PI * 2;
        double pointTheta = rand.nextDouble() * Math.PI;
        
        Point p = new Point();
        p.y = pos.y + radius * Math.sin(pointPhi)*Math.sin(pointTheta);
        p.x = pos.x + radius * Math.sin(pointPhi)*Math.cos(pointTheta);
        p.z = pos.z + radius * Math.cos(pointPhi);
        p.charge = charge;
        
        return p.rotateAroundVector(pos);
    }
    
    public Sphere() {
    }
}
