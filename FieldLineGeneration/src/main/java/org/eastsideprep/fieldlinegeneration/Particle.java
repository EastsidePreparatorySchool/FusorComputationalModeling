/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;

/**
 *
 * @author subif
 */
public class Particle extends Charge {

    public Vector vel = new Vector(0.0, 0.0, 0.0);
    public Double charge = 1.602E-19;
    public Double time = 0.0;
    public Double mass = 2.014 * 1.66E-27;
    public Double ePotentialEnergy = 0.0;
    public Double kineticEnergy = 0.0;
    public Double totalEnergy = 0.0;
    public Double scaleDistance = 1.0;

    public Particle() {
    }

    public Particle(Double x, Double y, Double z) {

        this.pos = new Vector(x, y, z);
    }

    public Particle(Vector pos, Vector vel) {
        this(pos.x, pos.y, pos.z);
        this.vel = vel;
    }

    public Particle(Vector pos) {
        this(pos.x, pos.y, pos.z);
    }

    public Particle(Double x, Double y, Double z, int polarity, Double charge) {
        this.pos = new Vector(x, y, z);
        this.polarity = polarity;
        this.charge = charge;
    }

    public Particle(Vector p, int polarity, Double charge) {
        this(p.x, p.y, p.z, polarity, charge);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param vx
     * @param vy
     * @param vz
     * @param polarity
     * @param charge
     */
    public Particle(Double x, Double y, Double z, Double vx, Double vy,
            Double vz, int polarity, Double charge) {
        this.pos = new Vector(x, y, z);
        this.vel = new Vector(vx, vy, vz);
        this.polarity = polarity;
        this.charge = charge;
    }

    /**
     *
     * @param p
     * @param v
     * @param polarity
     * @param charge
     */
    public Particle(Vector p, Vector v, int polarity, Double charge) {
        this(p.x, p.y, p.z, v.x, v.y, v.z, polarity, charge);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param vx
     * @param vy
     * @param vz
     * @param polarity
     * @param charge
     * @param time
     */
    public Particle(Double x, Double y, Double z, Double vx, Double vy,
            Double vz, int polarity, Double charge, Double time) {
        this.pos = new Vector(x, y, z);
        this.vel = new Vector(vx, vy, vz);
        this.polarity = polarity;
        this.charge = charge;
        this.time = time;
    }

    /**
     *
     * @param p
     * @param v
     * @param polarity
     * @param time
     */
    public Particle(Vector p, Vector v, int polarity, Double charge, Double time) {
        this(p.x, p.y, p.z, v.x, v.y, v.z, polarity, charge, time);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param vx
     * @param vy
     * @param vz
     * @param polarity
     * @param time
     * @param mass
     */
    public Particle(Double x, Double y, Double z, Double vx, Double vy,
            Double vz, int polarity, Double charge, Double time, Double mass) {
        this.pos = new Vector(x, y, z);
        this.vel = new Vector(vx, vy, vz);
        this.polarity = polarity;
        this.charge = charge;
        this.time = time;
        this.mass = mass;
    }

    /**
     *
     * @param p
     * @param v
     * @param polarity
     * @param time
     * @param mass
     */
    public Particle(Vector p, Vector v, int polarity, Double charge, Double time,
            Double mass) {
        this(p.x, p.y, p.z, v.x, v.y, v.z, polarity, charge, time, mass);
    }

    public Particle(String[] s) {
        this.pos = new Vector(Double.parseDouble(s[0]),
                Double.parseDouble(s[1]), Double.parseDouble(s[2]));
        this.vel = new Vector(Double.parseDouble(s[3]),
                Double.parseDouble(s[4]), Double.parseDouble(s[5]));
        this.polarity = Integer.parseInt(s[6]);
        this.charge = Double.parseDouble(s[7]);
        this.time = Double.parseDouble(s[8]);
        this.mass = Double.parseDouble(s[9]);
    }

    void setScaleDistance(Double scaleDistance) {
        this.pos.scale(scaleDistance);
        this.vel.scale(scaleDistance);
        this.scaleDistance = scaleDistance;
    }

    @Override
    public String toString() {
        String particle = "Position Vector: " + pos.product(1 / scaleDistance).toString();
        particle += "Velocity Vector: " + vel.product(1 / scaleDistance).toString();
        particle += "Polarity: " + polarity;
        particle += "Charge: " + charge;
        particle += "Time: " + time;
        particle += "Mass: " + mass;
        particle += "Electric Potential Energy: " + ePotentialEnergy;
        particle += "Kinetic Energy: " + kineticEnergy;
        return particle;
    }

    public String[] toCSVString() {
        this.pos.scale(1 / scaleDistance);
        this.vel.scale(1 / scaleDistance);
        String[] csvString = {"" + this.pos.x, "" + this.pos.y, "" + this.pos.z,
            "" + this.vel.x, "" + this.vel.y, "" + this.vel.z,
            "" + this.polarity, "" + this.charge, "" + this.time, "" + this.mass,
            "" + this.ePotentialEnergy, "" + this.kineticEnergy,
            "" + this.totalEnergy};
        return csvString;
    }

    public Particle clone() {
        Particle clone = new Particle(pos, vel, polarity, charge, time, mass);
        clone.ePotentialEnergy = this.ePotentialEnergy;
        clone.kineticEnergy = this.kineticEnergy;
        clone.totalEnergy = this.totalEnergy;
        clone.scaleDistance = this.scaleDistance;
        return clone;
    }

    public double kineticEnergy() {
        Vector zero = new Vector(0.0, 0.0, 0.0);
        this.kineticEnergy = 0.5 * this.mass * this.vel.distanceSquared(zero);
        return this.kineticEnergy;
    }

    public double electricPotentialEnergy(EField e) {
        double ePotential = 0;
        for (Charge t : e.charges) {
            ePotential += (t.polarity * e.k / (this.distanceTo(t))) * this.charge;
        }

        this.ePotentialEnergy = ePotential * e.chargeFactor;
        return this.ePotentialEnergy;
    }

    public double totalEnergy(EField e) {
        this.totalEnergy = kineticEnergy() + electricPotentialEnergy(e);
        return this.totalEnergy;
    }
    
    public void plusEquals(Particle p){
        this.pos.plusEquals(p.pos);
        this.vel.plusEquals(p.vel);
    }
    
    public void plusEquals(Particle[] ps){
        for(Particle p: ps){
            plusEquals(p);
        }
    
    }
    
    public Particle sum(Particle p){
        Particle s = this.clone();
        s.plusEquals(p);
        return s;
    }
    
    public Particle sum(Particle[] ps){
        Particle s = this.clone();
        for(Particle p: ps){
            s.plusEquals(p);
        }
        return s;
    }
    
    public Particle multiply(Double s){
        this.pos.scale(s);
        this.vel.scale(s);
        return this;
        
    }
    
}
