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
public class Particle extends Charge {

    public Vector vel = new Vector(0.0, 0.0, 0.0);
    public Double charge = 1.0;
    public Double time = 0.0;
    public Double mass = 2.014102 * 1.66053906660E-27;
    final Double e = 1.602176634e-19; // elementary charge 
    // also the conversion between 1 electron volt and Joules
    public Double ePotentialEnergy = 0.0;
    public Double voltsPerCm = 0.0;
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
        this.charge = charge * e;
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
        this.charge = charge * e;
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
        this.charge = charge * e;
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
        this.charge = charge * e;
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

    Particle(int polarity, Double charge, Double time, Double mass) {
        this.polarity = polarity;
        this.charge = charge;
        this.time = time;
        this.mass = mass;
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
        System.out.println("pre scale pos:" + pos);
        this.pos.scale(scaleDistance);
        System.out.println("post-scal pos:" + pos);
        this.vel.scale(scaleDistance);
        this.scaleDistance = scaleDistance;
    }

    @Override
    public String toString() {
        String particle = "Position Vector: " + pos.product(1 / scaleDistance).toString();
        particle += " Velocity Vector: " + vel.product(1 / scaleDistance).toString();
        particle += " Polarity: " + polarity;
        particle += " Charge: " + charge;
        particle += " Time: " + time;
        particle += " Mass: " + mass;
        particle += " Electric Potential Energy: " + ePotentialEnergy;
        particle += " Kinetic Energy: " + kineticEnergy;
        particle += " Volts Per Cm: " + voltsPerCm;
        return particle;
    }

    /**
     * Returns a string version of this particle suitable for printing to a CSV
     * file
     *
     * @return CSV string
     */
    public String[] toCSVString() {
        this.pos.scale(1 / scaleDistance);
        this.vel.scale(1 / scaleDistance);
        String[] csvString = {"" + this.pos.x, "" + this.pos.y, "" + this.pos.z,
            "" + this.vel.x, "" + this.vel.y, "" + this.vel.z,
            "" + this.polarity, "" + this.charge, "" + this.time, "" + this.mass,
            "" + this.ePotentialEnergy, "" + this.kineticEnergy,
            "" + this.totalEnergy,  "" + this.voltsPerCm};
        return csvString;
    }

    /**
     * Creates a clone of this particle with a different memory address
     *
     * @return clone
     */
    public Particle clone() {
        Particle clone = new Particle(polarity, charge, time, mass);
        clone.pos = this.pos.clone();
        clone.vel = this.vel.clone();
        clone.ePotentialEnergy = this.ePotentialEnergy;
        clone.kineticEnergy = this.kineticEnergy;
        clone.totalEnergy = this.totalEnergy;
        clone.scaleDistance = this.scaleDistance;
        clone.charge = this.charge;
        clone.voltsPerCm=this.voltsPerCm;
        return clone;
    }

    /**
     * Finds the kinetic energy of this particle
     *
     * @return kinetic energy
     */
    public double kineticEnergy() {
        Vector zero = new Vector(0.0, 0.0, 0.0);
        this.kineticEnergy = 0.5 * this.mass * this.vel.distanceSquared(zero) / this.e;
        return this.kineticEnergy;
    }

    /**
     * Given an electric field, finds the electric potential energy of this
     * particle
     *
     * @param e electric field
     * @return electric potential energy
     */
    public double electricPotentialEnergy(EField e) {
        double ePotential = 0;
        for (Charge t : e.charges) {
            ePotential += (t.polarity / (this.distanceTo(t))) * this.charge / this.e;
        }

        this.ePotentialEnergy = ePotential * e.chargeFactor;
        return this.ePotentialEnergy;
    }

    /**
     * Given an electric field, finds the total energy of this particle Also
     * updates the kinetic and electric potential energy of this particle
     *
     * @param e electric field
     * @return total energy
     */
    public double totalEnergy(EField e) {
        this.totalEnergy = kineticEnergy() + electricPotentialEnergy(e);
        voltsPerCentimeter(e);
        return this.totalEnergy;
    }
    
    public double voltsPerCentimeter(EField e){
        this.voltsPerCm = e.forceOnCharge(this).norm()*100;
        return this.voltsPerCm;
    }

    /**
     * Adds another particle's position and velocity vectors to this particle's
     *
     * @param p input particle
     */
    public void plusEquals(Particle p) {
        this.pos.plusEquals(p.pos);
        this.vel.plusEquals(p.vel);
    }

    /**
     * Adds other particle's position and velocity vectors to this particle's
     *
     * @param ps input particles
     */
    public void plusEquals(Particle[] ps) {
        for (Particle p : ps) {
            plusEquals(p);
        }

    }

    /**
     * Returns the sum of this particle and another particle's position and
     * velocity vectors without editing this particle's vectors
     *
     * @param p input particle
     * @return sum particle
     */
    public Particle sum(Particle p) {
        Particle s = this.clone();
        s.plusEquals(p);
        return s;
    }

    /**
     * Returns the sum of this particle and other particle's position and
     * velocity vectors without editing this particle's vectors
     *
     * @param ps input particles
     * @return sum particle
     */
    public Particle sum(Particle[] ps) {
        Particle s = this.clone();
        for (Particle p : ps) {
            s.plusEquals(p);
        }
        return s;
    }

    /**
     * Scales this particle's position and velocity vectors by s, and returns
     * this particle
     *
     * @param s scalar
     * @return scaled particle
     */
    @Override
    public Particle scale(Double s) {
        this.pos.scale(s);
        this.vel.scale(s);
        return this;

    }

    /**
     * Scales this particle's position and velocity vectors by s, and returns
     * this particle
     *
     * @param s scalar
     * @return scaled particle
     */
    public Particle scale(int s) {
        this.pos.scale(s);
        this.vel.scale(s);
        return this;

    }

}
