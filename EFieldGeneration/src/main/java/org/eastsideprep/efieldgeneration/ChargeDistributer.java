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
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class ChargeDistributer {

    Charge[] charges;
    double changes = 0;
    final Double k;  // Coulombs Constant
    double scaleDistance;
    Geometry geometry;

    public ChargeDistributer() {
        this.k = 8.9875517923E9;
    }

    public ChargeDistributer(Geometry geometry, Double scaleDistance, int charges) {
        this.k = 8.9875517923E9;
        this.geometry = geometry;
        this.scaleDistance = scaleDistance;
        distributeCharges(charges, charges);
    }

    // distributes charges randomly and uniformly;
    public void distributeCharges(int posCharges, int negCharges) {
        charges = new Charge[posCharges+negCharges];
        //System.out.println("Charges: " + posSumSA.get(2));
        //System.out.println("Charges: " + posSumSA.get(3));

        for (int i = 0; i < posCharges; i++) {
            charges[i] =geometry.getRandomPositiveCharge();
        }
        for (int i = 0; i < negCharges; i++) {
            charges[i+posCharges] = (geometry.getRandomNegativeCharge());
        }

    }

    public void balanceCharges(int shakes) {
        double shakeUps = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < shakes; i++) {
            chargeShakeUp(charges, geometry);
            Double[] averageEP = averageElectricPotentials(charges);
            System.out.println("Shakes Completed: " + (i + 1) + "/" + shakes);
            System.out.println("Average Electric Potential: " + averageEP[0]);
            System.out.println("Average Positive Electric Potential: " + averageEP[1]);
            System.out.println("Average Negative Electric Potential: " + averageEP[2]);
            shakeUps++;
        }
        System.out.println("Shake Ups Completed: " + shakeUps);
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime));
        System.out.println("changes: " + changes);

    }

    public void balanceCharges(long ms) {
        long startTime = System.currentTimeMillis();
        double shakeUps = 0;
        while (System.currentTimeMillis() - startTime < ms) {
            chargeShakeUp(charges, geometry);
            shakeUps++;
            System.out.println("Shake Ups Completed: " + shakeUps);
        }
        System.out.println("changes: " + changes);
    }

    public void chargeShakeUp(Charge[] charges, Geometry geometry) {
        for (Charge c : charges) {
            if (c.polarity < 0) {
                Charge t = geometry.getRandomNegativeCharge();

                if (electricPotentialEnergy(t, c) < electricPotentialEnergy(c, c)) {
                    c.setPosition(t);
                    changes++;
                }

            } else {
                Charge t = geometry.getRandomPositiveCharge();

                if (electricPotentialEnergy(t, c) < electricPotentialEnergy(c, c)) {
                    c.setPosition(t);
                    changes++;
                }

            }
        }

    }

    /**
     *
     * @param charges
     * @return the average overall, average positive and average negative EP
     */
    public Double[] averageElectricPotentials(Charge[] charges) {
        Double[] averageEP = {0.0, 0.0, 0.0};
        int total = 0;
        int positive = 0;
        int negative = 0;
        for (Charge c : charges) {
            Double ep = electricPotential(c, c);
            averageEP[0] += ep;
            if (c.polarity > 0) {
                averageEP[1] += ep;
                positive++;
            } else {
                averageEP[2] += ep;
                negative++;
            }
            total++;
        }
        averageEP[0] /= total;
        averageEP[1] /= positive;
        averageEP[2] /= negative;
        return averageEP;
    }

    /**
     * The electric potential of a specific charge
     *
     * @param c Selected charge
     * @return electric potential of a given charge
     */
    public double electricPotential(Charge c) {
        double ePotential = 0;
        for (Charge t : charges) {
            ePotential += (t.polarity * k / (c.distanceTo(t))) * c.polarity;
        }
        return ePotential;
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
            ePotential += (t.polarity * k) / (v.distanceTo(t) * scaleDistance);
        }
        return ePotential;
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
                ePotential += (t.polarity * k) / (c.distanceTo(t) * scaleDistance);
            }
        }
        return ePotential;
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
                ePotential += (t.polarity * k) / (v.distanceTo(t) * scaleDistance);
            }
        }
        return ePotential;

    }

    /**
     * The electric potential energy of a given charge without the inclusion of
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
                ePotential += (t.polarity * k / (c.distanceTo(t) * scaleDistance)) * c.polarity;
            }
        }
        return ePotential;
    }

}
