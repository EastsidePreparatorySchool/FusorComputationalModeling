/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

/**
 *
 * @author subif
 */
import java.lang.Math;

public class Vector {
    
    Double x;
    Double y;
    Double z;

    public Vector() {
    }

    public Vector(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(String x, String y, String z) {
        this.x = Double.valueOf(x);
        this.y = Double.valueOf(y);
        this.z = Double.valueOf(z);
    }

    public Vector(Vector v) {
        this(v.x, v.y, v.z);
    }
    /**
     * 
     * @return magnitude of vector
     */
    public Double norm() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }
    /**
     * 
     * @return xy magnitude of vector
     */
    public Double xyNorm() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * 
     * @return xz magnitude of vector
     */
    public Double xzNorm() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
    
    /**
     * 
     * @return yz magnitude of vector
     */
    public Double yzNorm() {
        return Math.sqrt(Math.pow(z, 2) + Math.pow(y, 2));
    }
    /**
     * 
     * @param v input vector
     * @return distance between this vector and input vector
     */
    public Double distanceTo(Vector v) {
        return Math.sqrt(Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2) + Math.pow(z - v.z, 2));
    }
    /**
     * 
     * @param c input charge
     * @return distance between this vector and input charge
     */
    public Double distanceTo(Charge c) {
        return Math.sqrt(Math.pow(x - c.pos.x, 2) + Math.pow(y - c.pos.y, 2) + Math.pow(z - c.pos.z, 2));
    }

    /**
     * 
     * @param v input vector
     * @return distance squared between this vector and input vector
     */
    public Double distanceSquared(Vector v) {
        return Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2) + Math.pow(z - v.z, 2);
    }
    
    /**
     * 
     * @param c Input charge
     * @return distance squared between this vector and input charge
     */
    public Double distanceSquared(Charge c) {
        return Math.pow(x - c.pos.x, 2) + Math.pow(y - c.pos.y, 2) + Math.pow(z - c.pos.z, 2);
    }
    
    /**
     * Dot product: multiplying respective components and adding them together
     * (x1*x2)+(y1*y2)+(z1*z2)
     * @param v
     * @return dot
     */
    public Double dotProduct(Vector v) {
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    /**
     * Cross Product: outputs a vector, whose components are
     * the determinants of the other two components of two vectors
     * @param v
     * @return cross product
     */
    public Vector crossProduct(Vector v) {
        Vector c = new Vector();
        c.x = (y * v.z) - (z * v.y);
        c.y = (z * v.x) - (x * v.z);
        c.z = (x * v.y) - (y * v.x);
        return c;
    }
    
    /**
     * Finds the angle between two vectors
     * @param v input vector
     * @return angle between vectors
     */
    public Double angle(Vector v) {

        // ||a X b|| = ||a|||*|b||*sin(theta)
        Double theta = Math.acos(crossProduct(v).norm() / (norm() * v.norm()));
        return theta;
        // multiply brom of cross product by 1/2
    }
    /**
     * finds the angle in radians between this vector and another vector
     * @param v input vector
     * @return 
     */
    public Double angleBetween(Vector v) {
        // basic vector math
        // dot product can be found in two ways
        //1. Take the magnitudes of the two vectors 
        // (the norm), multiply them together, and multiply by cos(theta)
        // where theta is the angle between them
        //2. do the dot product (a.x*b.x)+(a.y*b.y)...
        // so if you divide the dot product by the magnitudes multiplied
        // you get cos(theta)
        // and then you're done
        Double dot = dotProduct(v);
        if (dot.isNaN()) {
            System.out.println("Dot is problem");
        }
        Double magnitudes = (this.norm() * v.norm());
        if (this.norm() == 0.0) {
            System.out.println("this length is problem");
        }

        if (v.norm() == 0.0) {
            System.out.println("v length is problem");
        }
        if (magnitudes == 0.0) {
            System.out.println("magnitudes is problem");
        }
        Double cosTheta = dot / magnitudes;
        if (cosTheta.isNaN()) {
            System.out.println("cosTheta is problem");
        }
        return Math.acos(cosTheta);
    }

    /**
     * this - v a vector from v to this Vector: VThis
     *
     * @param v the vector on the right of the minus sign
     * @return the result of this - v (a line from v to this)
     */
    public Vector thatToThis(Vector v) {
        return new Vector((x - v.x), (y - v.y), (z - v.z));
    }

    /**
     * this - c a vector from c to this Vector: CThis
     *
     * @param c the vector on the right of the minus sign
     * @return the result of this - c (a line from c to this)
     */
    public Vector thatToThis(Charge c) {
        return new Vector((x - c.pos.x), (y - c.pos.y), (z - c.pos.z));
    }

    /**
     * v-this draws a line from v to this Vector: ThisV
     *
     * @param v the vector on the left side of the minus sign
     * @return the result of v-this (a line from this to v)
     */
    public Vector thisToThat(Vector v) {
        return new Vector((v.x - x), (v.y - y), (v.z - z));
    }

    /**
     * c-this draws a line from c to this Vector: ThisC
     *
     * @param c the vector on the left side of the minus sign
     * @return the result of C-this (a line from this to C)
     */
    public Vector thisToThat(Charge c) {
        return new Vector((c.pos.x - x), (c.pos.y - y), (c.pos.z - z));
    }

    /**
     * A function for getting a version of this vector that is pointing in the
     * same direction, but with a length of 1
     *
     * @return the normalized version of this vector
     */
    public Vector normalized() {
        double length = this.norm();
        Vector norm = new Vector();
        norm.x = this.x / length;
        norm.y = this.y / length;
        norm.z = this.z / length;
        return norm;

    }

    /**
     * Adds another vector to this vector (the += sign)
     *
     * @param v other vector
     */
    public void plusEquals(Vector v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    /**
     * Adds a scalar to this vector (the += sign)
     *
     * @param s the scalar
     */
    public void plusEquals(Double s) {
        this.x += s;
        this.y += s;
        this.z += s;
    }

    /**
     *
     * @param v addend
     * @return the sum of this vector and another vector
     */
    public Vector sum(Vector v) {
        Vector s = new Vector();
        s.x = this.x + v.x;
        s.y = this.y + v.y;
        s.z = this.z + v.z;
        return s;
    }
    /**
     * Sum of this vector and other vectors
     * @param vectors addends
     * @return sum of this vector and other vectors
     */
    public Vector sum(Vector[] vectors) {
        Vector s = new Vector();
        s.x = this.x;
        s.y = this.y;
        s.z = this.z;
        for (Vector v : vectors) {
            s.x += v.x;
            s.y += v.y;
            s.z += v.z;
        }
        return s;
    }

    /**
     * Adds other vectors to this vector
     * @param vectors input vectors
     */
    public void plusEquals(Vector[] vectors) {
        for (Vector v : vectors) {
            this.x += v.x;
            this.y += v.y;
            this.z += v.z;
        }
    }

    public String toString() {
        String vector = "";
        vector += "x: " + x;
        vector += " y: " + y;
        vector += " z: " + z + " ";
        return vector;
    }

    /**
     * Checks if this vector is equivalent to the input vector
     * @param v input vector
     * @return true if this vector is equal to the input vector, false otherwise
     */
    public Boolean equals(Vector v) {
        Boolean same = true;
        if (!this.x.equals(v.x)) {
            same = false;
        }
        if (!this.y.equals(v.y)) {
            same = false;
        }
        if (!this.z.equals(v.z)) {
            same = false;
        }
        return same;
    }

    /**
     * Scales this vector by a factor s, and returns this vector
     * @param s scalar
     * @return scaled vector
     */
    public Vector scale(Double s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    /**
     * Scales this vector by a factor s, and returns this vector
     * @param s scalar
     * @return scaled vector
     */
    public Vector scale(int s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }
    
    /**
     * Multiplies each component of this vector with the corresponding 
     * component in the input vector
     * @param v input vector
     * @return this vector
     */
    public Vector multiply(Vector v){
        x*=v.x;
        y*=v.y;
        z*=v.z;
        return this;
    }
    
    /**
     * Returns the product of this vector and a scalar, without
     * editing this vectors values
     * @param s scalar
     * @return 
     */
    public Vector product(Double s) {
        return new Vector(x, y, z).scale(s);
    }

    /**
     * Creates an identical clone of this vector with a different memory address
     * @return clone
     */
    public Vector clone() {
        Vector v = new Vector();
        v.x = this.x + 0.0;
        v.y = this.y + 0.0;
        v.z = this.z + 0.0;
        return v;

    }
}
