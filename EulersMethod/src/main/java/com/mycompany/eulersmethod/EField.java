/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// PRAVEER AND MAGGIE, the package below is something you should definitely change to whatever you want your package name to be
// other notes
// you will most likely want the electric potential energy, not the electric potential
// as for the 'ignore charge', that's saying don't include the charge that we're finding the electric potential of
package com.mycompany.EulersMethod;

/**
 *
 * @author subif
 */
import java.util.ArrayList;
import java.util.List;

public class EField {

    final Double e = 1.602176634e-19; // elementary charge 
    // also the conversion between 1 electron volt and Joules.
    double chargeFactor = 1.0;
    double vAnnode;
    double vCathode;
    double scaleDistance;
    Charge[] charges;
    final Double k = 8.9875517923E9;  // Coulombs Constant

    public EField() {

    }

    /**
     *
     * @param charges a list of charges generating the electric field
     * @param vAnnode the + side voltage
     * @param vCathode the - side voltage
     * @param scaleDistance the distance in meters of one unit
     * @param centerOfGrid a vector pointing to the center of the wire grid,
     * defaults to origin
     */
    public EField(Charge[] charges, double vAnnode, double vCathode, double scaleDistance, Vector centerOfGrid) {
        this.vAnnode = vAnnode;
        this.vCathode = vCathode;
        this.charges = charges;
        this.scaleDistance = scaleDistance;
        for (Charge c : charges) {
            c.scale(scaleDistance);
        }
        if (centerOfGrid == null) {
            centerOfGrid = new Vector(0.0, 0.0, 0.0);
        }
        double ep = electricPotential(centerOfGrid);
        System.out.println();
        System.out.println("ep: " + ep);
        chargeFactor = Math.abs((vAnnode - vCathode) / ep);
        System.out.println("charge Factor: " + chargeFactor);

    }

    /**
     * This is the electric field at a given point assuming a positive charge of
     * 1
     *
     * @param v is the location (as a vector)
     * @return a force vector
     */
    public Vector fieldAtPoint(Vector v) {
        Vector sumOfField = new Vector(0.0, 0.0, 0.0);
        double vol;
        double distanceSquared;

        for (Charge t : charges) {

            vol = t.polarity;

            distanceSquared = t.distanceSquared(v);
            Vector effectOnPoint;
            effectOnPoint = t.thisToThat(v).normalized();
            //effectOnPoint.scale(-1.0);
            effectOnPoint.x *= vol / distanceSquared;
            effectOnPoint.y *= vol / distanceSquared;
            effectOnPoint.z *= vol / distanceSquared;
            sumOfField.plusEquals(effectOnPoint);
        }
        sumOfField.scale(chargeFactor);
        return sumOfField;
    }

    /**
     * This is the electric field on a charge.
     *
     * @param c is the charge
     * @return A force vector
     */
    public Vector forceOnCharge(Charge c) {
        Vector sumOfField = new Vector(0.0, 0.0, 0.0);
        double voltage;
        double vol;
        double distanceSquared;
        for (Charge t : charges) {
            vol = c.polarity * t.polarity;
            distanceSquared = t.distanceSquared(c);
            Vector effectOnPoint;
            effectOnPoint = t.thisToThat(c).normalized();
            effectOnPoint.x *= vol / distanceSquared;
            effectOnPoint.y *= vol / distanceSquared;
            effectOnPoint.z *= vol / distanceSquared;
            sumOfField.plusEquals(effectOnPoint);
        }
        sumOfField.scale(chargeFactor);
        return sumOfField;
    }

    /**
     * This is the electric field on a particle.
     *
     * @param p is the particle
     * @return A force vector
     */
    public Vector forceOnCharge(Particle p) {
        Vector sumOfField = new Vector(0.0, 0.0, 0.0);
        double vol;
        double distanceSquared;
        for (Charge t : charges) {
            vol = p.charge * t.polarity;
            distanceSquared = t.distanceSquared(p);
            Vector effectOnPoint;
            effectOnPoint = t.thisToThat(p).normalized();
            effectOnPoint.x *= vol / distanceSquared;
            effectOnPoint.y *= vol / distanceSquared;
            effectOnPoint.z *= vol / distanceSquared;
            sumOfField.plusEquals(effectOnPoint);
        }
        sumOfField.scale(chargeFactor);
        return sumOfField;
    }

    /**
     * The electric potential of a specific charge this is NOT the electric
     * potential ENERGY
     *
     * @param c Selected charge
     * @return electric potential of a given charge
     */
    public double electricPotential(Charge c) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity/ (c.distanceTo(t)));
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential of a specific point
     *
     * @param v Selected point
     * @return electric potential of a given point
     */
    public Double electricPotential(Vector v) {
        Double ePotential = 0.0;
        for (Charge t : charges) {
            ePotential += (t.polarity) / (v.distanceTo(t));
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential of a given charge without the inclusion of a
     * specified charge
     *
     * @param c Selected Charge
     * @param ignore charge to be ignored in calculations
     * @return electric potential of a given charge
     */
    public double electricPotential(Charge c, Charge ignore) {
        double ePotential = 0;
        for (Charge t : charges) {
            if (t != ignore) {
                ePotential += (t.polarity) / (c.distanceTo(t));
            }
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential of a given point without the inclusion of a
     * specified charge
     *
     * @param v Selected point
     * @param ignore charge to be ignored in calculations
     * @return electric potential of a given point
     */
    public double electricPotential(Vector v, Charge ignore) {
        double ePotential = 0;
        for (Charge t : charges) {
            if (t != ignore) {
                ePotential += (t.polarity) / (v.distanceTo(t));
            }
        }
        return ePotential * chargeFactor;

    }

    /**
     * The electric potential ENERGY of a given particle
     *
     * @param p Selected Particle
     * @return electric potential energy of a given particle
     */
    public double electricPotentialEnergy(Particle p) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity/ (p.distanceTo(t))) * p.charge /e;
            // does not apply elementary electric charge because
            // particles SHOULD already do this themselves
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential ENERGY of a given charge Recommended for use with
     * charges not on the grid
     *
     * @param c Selected Charge
     * @return electric potential of a given charge
     */
    public double electricPotentialEnergy(Charge c) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity/ (c.distanceTo(t))) * c.polarity;
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential ENERGY of a given charge without the inclusion of
     * a specified charge
     *
     * @param c Selected Charge
     * @param ignore charge to be ignored in calculations
     * @return electric potential of a given charge
     */
    public double electricPotentialEnergy(Charge c, Charge ignore) {
        double ePotential = 0;
        for (Charge t : charges) {
            if (t != ignore) {
                ePotential += (t.polarity/ (c.distanceTo(t))) * c.polarity;
            }
        }
        return ePotential * chargeFactor;
    }

    public double kineticEnergy(Particle p) {
        Vector zero = new Vector(0.0, 0.0, 0.0);
        return 0.5 * p.mass * p.vel.distanceSquared(zero);
    }

    public double totalEnergy(Particle p) {
        double ePotentialEnergy = electricPotentialEnergy(p);
        double kineticEnergy = kineticEnergy(p);
        return ePotentialEnergy + kineticEnergy;
    }
}
