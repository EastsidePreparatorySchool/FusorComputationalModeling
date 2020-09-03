/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import static java.lang.Double.NaN;
import java.util.Comparator;

/**
 *
 * @author guberti
 */
public class Point {
    public double x;
    public double y;
    public double z;
    public int charge; // True is positive, false is negative
    public double EP;
    
    public Point(double x, double y, double z, int charge, double EP) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.charge = charge;
        this.EP = EP;
    }
    
    public Point(double x, double y, double z, int charge) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.charge = charge;
    }
    
    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point(VectorB v, double r) {
        this.x = r * Math.sin(v.theta) * Math.cos(v.phi);
        this.y = r * Math.cos(v.theta);
        this.z = r * Math.sin(v.theta) * Math.sin(v.phi);
    }
    
    public Point() {}
    
    public Point rotateAroundVector(VectorB v) {
        /* Note to future self: the code below is an abomination. It was
        written based on incorrect formulas, but instead of finding correct
        ones I chose to fix the errors generated here in later parts of the
        code. If this some day breaks, do not try to fix it - major
        restructuring will be needed.
        */
        
        // First move the two points so that the point can be rotated around
        // the origin
        
        v.phi = -v.phi;
        v.theta = -v.theta;
        
        Point mP = new Point(x - v.x, y - v.y, z - v.z);
        
        // Then rotate the point around the origin with the rotational formula
        Point rP = new Point(); // rP stands for rotatedPoint

        rP.z = Math.cos(v.phi) * mP.x + Math.sin(v.phi) * Math.sin(v.theta) * mP.y -
                Math.sin(v.phi) * Math.cos(v.theta) * mP.z;
        
        rP.y = Math.cos(v.theta) * mP.y + Math.sin(v.theta) * mP.z;
        
        rP.x = Math.sin(v.phi) * mP.x + Math.cos(v.phi) * -1 * Math.sin(v.theta) * 
                mP.y + Math.cos(v.phi) * Math.cos(v.theta) * mP.z;
        
        // Then add the coordinates the point is being rotated around
        
        v.phi = -v.phi;
        v.theta = -v.theta;
        
        rP.x += v.x;
        rP.y += v.y;
        rP.z += v.z;
        rP.charge = charge;
        // Return the rotated point
        
        return rP;
    }
    
    // VectorB v's phi and theta describe how much to rotate, and the vector's location
    // describes what to rotate around
    
    public Point rotateAroundPoint(VectorB v) {
        
        Point mP = new Point(x - v.x, y - v.y, z - v.z);
        
        VectorB sphericalCoords = mP.convertToSphericalCoords();
        sphericalCoords.phi += v.phi;
        sphericalCoords.theta += v.theta;
        double radius = Math.sqrt(mP.x*mP.x + mP.y*mP.y + mP.z*mP.z);
        Point rP = new Point(sphericalCoords, radius);
        
        rP.x += v.x;
        rP.y += v.y;
        rP.z += v.z;
        rP.charge = charge;
        
        return rP;
    }
    
    public Point crossProduct(Point p) {
        Point nP = new Point();
        
        nP.x = y*p.z - z*p.y;
        nP.y = z*p.x - x*p.z;
        nP.z = x*p.y - y*p.x;
        
        return nP;
    }
    
    public double dotProduct(Point p) {
        return x*p.x + y*p.y + z*p.z;
    }
    
    public VectorB convertToSphericalCoords() { // Faster, used by Wire class
        VectorB v = new VectorB();
        v.phi = Math.atan(z/x);
        v.theta = Math.atan(Math.sqrt(x*x+z*z)/y);
        v.length = Math.sqrt(x*x + y*y + z*z);
        return v;
    }
    
    public VectorB convertToSphericalCoordsExc() { // Slower, but works for all values
        VectorB v = new VectorB();
        
        if (x == 0 && y == 0 && z == 0) {
            return new VectorB(0, 0, 0);
        }
        
        if (x == 0 && z == 0) {
            v.phi = 0;
        } else {
            v.phi = Math.acos(x/Math.sqrt(x*x + z*z));
        }
        
        v.length = Math.sqrt(x*x + y*y + z*z);
        v.theta = Math.acos(y/v.length);
        
        return v;
    }
    
    public double getVectorLength() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }
    
    public double getAngleBetweenVectors(Point p) {
        double dotProduct = dotProduct(p);
        double divisor = getVectorLength() * p.getVectorLength();
        double cosAngle = dotProduct/divisor;
        return Math.acos(cosAngle);
    }

    public int compareTo(Point b, int axis) {
        if (axis == 0) {
            if (this.x > b.x) {
                return -1;
            } else if (this.x < b.x) {
                return 1;
            }
        } else if (axis == 1) {
            if (this.y > b.y) {
                return -1;
            } else if (this.y < b.y) {
                return 1;
            }
        } else if (axis == 2) {
            if (this.z > b.z) {
                return -1;
            } else if (this.z < b.z) {
                return 1;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "[" + Double.toString(x) + 
                ", " + Double.toString(y) + ", " + 
                Double.toString(z) + "]";
    }

    public void divideByLength(double len) {
        x /= len;
        y /= len;
        z /= len;
    }
    
    public void scale (double scaleFactor) { // Technically redundant (see previous method)
        // But is still a good thing to have (I think)
        x *= scaleFactor;
        y *= scaleFactor;
        z *= scaleFactor;
        
    }
    
    public void sum (Point addend) {
        x += addend.x;
        y += addend.y;
        z += addend.z;
    }
    
    double getLength() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }
    boolean testForNaN() {
        return x == NaN || y == NaN || z == NaN;
    }
}
