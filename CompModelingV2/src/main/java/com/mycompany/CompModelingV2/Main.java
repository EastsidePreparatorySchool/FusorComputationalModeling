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
import fusorvis.FusorVis;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        App.main(args);
        /*double x = 0.01;
        ArrayList<Charge> charges = new ArrayList();
        Charge L = new Charge(0.0, -56.44137907002903, -0.3066462775348471, -1);
        //Charge bL = new Charge(0.0, -1.0, 0.0, -1);
        //Charge tL = new Charge(0.0, 1.0, 0.0, -1);
        Charge R = new Charge(10.024655176723272, -200.0, -1.0364055361947608, 1);
        //Charge bR = new Charge(x, -1.0, 0.0, 1);
        //Charge tR = new Charge(x, 1.0, 0.0, 1);
        charges.add(L);
        //charges.add(bL);
        //charges.add(tL);
        charges.add(R);
        //charges.add(bR);
        //charges.add(tR);
        Charge testTL = new Charge(10.0, -10.0, -1.37, 1);

        //System.out.println("eP: " + electricPotentialEnergy(tL, charges, tL));
        System.out.println("eP: " + electricPotentialEnergy(L, charges, L));
        //System.out.println("eP: "+electricPotential(bL, charges, bL));
        //System.out.println("eP: " + electricPotentialEnergy(tR, charges, tR));
        System.out.println("eP: " + electricPotentialEnergy(R, charges, R));
        //System.out.println("eP: "+electricPotential(bR, charges, bR));
        System.out.println("eP: " + electricPotentialEnergy(testTL, charges, L));
        System.out.println("eP: " + electricPotentialEnergy(testTL, charges, R));
        */
    }

    /**
     * The electric potential energy of a given charge without the inclusion of
     * a specified charge
     *
     * @param c Selected Charge
     * @param ignore charge to be ignored in calculations
     * @return electric potential of a given charge
     */
    public static double electricPotentialEnergy(Charge c, List<Charge> charges, Charge ignore) {
        double ePotential = 0;
        for (Charge t : charges) {
            if (t != ignore) {
                ePotential += (t.polarity / (c.distanceTo(t))) * c.polarity;
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
    public static double electricPotential(Vector v, List<Charge> charges, Charge ignore) {
        double ePotential = 0;
        for (Charge t : charges) {
            if (t != ignore) {
                ePotential += t.polarity / (v.distanceTo(t));
            }
        }
        return ePotential;

    }
}
