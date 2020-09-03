/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;
// temp edits made to field effect at point
//TEMPORARY FOR NOW

/**
 *
 * @author subif
 */
public class EField {
    final Double e = 1.602e-19; // elementary charge
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
        double voltage;
        double vol;
        double distanceSquared;

        for (Charge t : charges) {
            voltage = vAnnode - vCathode;

            vol = t.polarity * voltage;

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
     * The electric potential of a specific charge this is NOT the electric
     * potential ENERGY
     *
     * @param c Selected charge
     * @return electric potential of a given charge
     */
    public double electricPotential(Charge c) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity * k / (c.distanceTo(t)))*e;
        }
        return ePotential * chargeFactor;
    }

    /**
     * The electric potential of a specific point
     *
     * @param v Selected point
     * @return electric potential of a given point
     */
    public double electricPotential(Vector v) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity * k) / (v.distanceTo(t))*e;
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
                ePotential += (t.polarity * k) / (c.distanceTo(t))*e;
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
                ePotential += (t.polarity * k) / (v.distanceTo(t))*e;
            }
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
            ePotential += (t.polarity * k / (c.distanceTo(t))) * c.polarity*e;
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
                ePotential += (t.polarity * k / (c.distanceTo(t))) * c.polarity*e;
            }
        }
        return ePotential * chargeFactor;
    }

    public void deScale() {
        for (Charge c : charges) {
            c.scale(1 / scaleDistance);
        }
    }
}
