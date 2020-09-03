/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.CompModelingV2;

/**
 *
 * @author subif
 */
public class Charge {

    public int polarity;
    public Vector pos; // position

    public Charge() {
    }

    public Charge(Double x, Double y, Double z) {
        this.pos = new Vector(x, y, z);

    }

    public Charge(Vector pos) {
        this.pos = pos;
    }

    public Charge(Double x, Double y, Double z, int polarity) {
        this.pos = new Vector(x, y, z);
        this.polarity = polarity;
    }

    public Charge(Vector v, int polarity) {
        this(v.x, v.y, v.z, polarity);
    }

    public Charge(String[] s) {
        this.pos = new Vector(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
        this.polarity = Integer.parseInt(s[3]);
    }

    /**
     * this - v a vector from v to this Vector: VThis
     *
     * @param v the vector on the right of the minus sign
     * @return the result of this - v (a line from v to this)
     */
    public Vector thatToThis(Vector v) {
        return new Vector((pos.x - v.x), (pos.y - v.y), (pos.z - v.z));
    }

    /**
     * this - c a vector from c to this Vector: CThis
     *
     * @param c the vector on the right of the minus sign
     * @return the result of this - c (a line from c to this)
     */
    public Vector thatToThis(Charge c) {
        return new Vector((pos.x - c.pos.x), (pos.y - c.pos.y), (pos.z - c.pos.z));
    }

    /**
     * v-this draws a line from v to this Vector: ThisV
     *
     * @param v the vector on the left side of the minus sign
     * @return the result of v-this (a line from this to v)
     */
    public Vector thisToThat(Vector v) {
        return new Vector((v.x - pos.x), (v.y - pos.y), (v.z - pos.z));
    }

    /**
     * c-this draws a line from c to this Vector: ThisC
     *
     * @param c the vector on the left side of the minus sign
     * @return the result of C-this (a line from this to C)
     */
    public Vector thisToThat(Charge c) {
        return new Vector((c.pos.x - pos.x), (c.pos.y - pos.y), (c.pos.z - pos.z));
    }

    public double distanceTo(Vector v) {
        return Math.sqrt(Math.pow(pos.x - v.x, 2) + Math.pow(pos.y - v.y, 2) + Math.pow(pos.z - v.z, 2));
    }

    public double distanceTo(Charge c) {
        return Math.sqrt(Math.pow(pos.x - c.pos.x, 2) + Math.pow(pos.y - c.pos.y, 2) + Math.pow(pos.z - c.pos.z, 2));
    }

    public double distanceSquared(Vector v) {
        return Math.pow(pos.x - v.x, 2) + Math.pow(pos.y - v.y, 2) + Math.pow(pos.z - v.z, 2);
    }

    public double distanceSquared(Charge c) {
        return Math.pow(pos.x - c.pos.x, 2) + Math.pow(pos.y - c.pos.y, 2) + Math.pow(pos.z - c.pos.z, 2);
    }

    @Override
    public String toString() {
        String charge = "";
        charge += pos.toString();
        charge += "Polarity: " + polarity;
        return charge;
    }

    public String[] toCSVString() {
        String[] csvString = {"" + this.pos.x, "" + this.pos.y, "" + this.pos.z, "" + this.polarity};
        return csvString;
    }

    public void scale(Double s) {
        pos.x *= s;
        pos.y *= s;
        pos.z *= s;
    }

    /**
     * Adds a scalar to this charge's position vector (the += sign)
     *
     * @param s the scalar
     */
    public void plusEquals(Double s) {
        this.pos.x += s;
        this.pos.y += s;
        this.pos.z += s;
    }

    /**
     * Sets the position of this charge to a vector
     *
     * @param v the vector
     */
    public void setPosition(Vector v) {
        this.pos.x = v.x;
        this.pos.y = v.y;
        this.pos.z = v.z;
    }

    /**
     * Sets the position of this charge to a different charge's position Use
     * instead of this = charge;
     *
     * @param c the other charge
     */
    public void setPosition(Charge c) {
        this.pos.x = c.pos.x;
        this.pos.y = c.pos.y;
        this.pos.z = c.pos.z;
    }    
}
