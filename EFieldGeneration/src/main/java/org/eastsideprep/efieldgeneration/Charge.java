/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.efieldgeneration;

/**
 *
 * @author subif
 */
public class Charge {

    
    public int polarity;
    public Double x;
    public Double y;
    public Double z;
    public Double EP;

    public Charge() {
    }

    public Charge(Double x, Double y, Double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Charge(Vector pos) {
        this(pos.x, pos.y, pos.z);
    }

    public Charge(Double x, Double y, Double z, int polarity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.polarity = polarity;
    }

    public Charge(Vector v, int polarity) {
        this(v.x, v.y, v.z, polarity);
    }

    public Charge(Double x, Double y, Double z, int polarity, Double EP) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.polarity = polarity;
        this.EP = EP;
    }

    public Charge(Vector v, int polarity, double EP) {
        this(v.x, v.y, v.z, polarity, EP);
    }
    
    public Charge(String[] s){
        this.x = Double.parseDouble(s[0]);
        this.y = Double.parseDouble(s[1]);
        this.z = Double.parseDouble(s[2]);
        this.polarity = Integer.parseInt(s[3]);
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
        return new Vector((x - c.x), (y - c.y), (z - c.z));
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
        return new Vector((c.x - x), (c.y - y), (c.z - z));
    }

    public double distanceTo(Vector v) {
        return Math.sqrt(Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2) + Math.pow(z - v.z, 2));
    }

    public double distanceTo(Charge c) {
        return Math.sqrt(Math.pow(x - c.x, 2) + Math.pow(y - c.y, 2) + Math.pow(z - c.z, 2));
    }

    public double distanceSquared(Vector v) {
        return Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2) + Math.pow(z - v.z, 2);
    }

    public double distanceSquared(Charge c) {
        return Math.pow(x - c.x, 2) + Math.pow(y - c.y, 2) + Math.pow(z - c.z, 2);
    }

    @Override
    public String toString() {
        String charge = "";
        charge += new Vector(x, y, z).toString();
        charge += "Polarity: " + polarity;
        return charge;
    }
    
    public String[] toCSVString(){
        String[] csvString = {""+this.x, ""+this.y, ""+this.z, ""+this.polarity};
        return csvString;
    }

    public void scale(Double s) {
        x *= s;
        y *= s;
        z *= s;
    }
    
    /**
     * Adds a scalar to this charge's position vector
     * (the += sign)
     * @param s the scalar
     */
    public void plusEquals(Double s){
        this.x += s;
        this.y += s;
        this.z += s;
    }
    /**
     * Sets the position of this charge to a vector
     * @param v the vector
     */
    public void setPosition(Vector v){
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }
    
    /**
     * Sets the position of this charge to a different charge's position
     * Use instead of this = charge;
     * @param c the other charge
     */
    public void setPosition(Charge c){
        this.x = c.x;
        this.y = c.y;
        this.z = c.z;
    }    
}
