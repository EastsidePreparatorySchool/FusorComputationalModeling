/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pjain
 */
public class Main {

    public static void main(String[] args) {
        InputHandler ih = new InputHandler();
        ih.getInput();

        Vector test1 = new Vector(0.0, 0.0, 0.0);
        Vector test2 = new Vector(1000.0, 1000.0, 1000.0);
        Vector test3 = new Vector(0.07, 0.0, 0.0);
        Particle p = new Particle(test3, test1, 1, 1.0);
        SLRungeKutta slrk = new SLRungeKutta(ih.eField);
        MYEulersMethod myeu = new MYEulersMethod(ih.eField);
        /*System.out.println("Grid vector: " + ih.eField.electricPotential(test1));
        System.out.println("Vector outside the chamber: " + ih.eField.electricPotential(test2));
        System.out.println("At x=70mm: " + ih.eField.electricPotential(test3));
        System.out.println("More at x=70mm: " + ih.eField.fieldAtPoint(test3));
        System.out.println("last: " + ih.eField.fieldAtPoint(test3).norm());
        System.out.println("charge 1: " + ih.eField.charges[0]);
         */
        
        Particle step1 = myeu.step(p.clone(), 5e-11);
        System.out.println("euler 1 step: " + myeu.step(p.clone(), 5e-11));
        System.out.println("euler 2 step: " + myeu.step(step1, 5e-11));
        step1 = slrk.step(p, 5e-11);
        System.out.println("1 step: " + slrk.step(p, 5e-11));
        System.out.println("2 step: " + slrk.step(step1, 5e-11));
        System.out.println("P: " + ih.eField.forceOnCharge(p));
        System.out.println("P acc: " + ih.eField.forceOnCharge(p).scale(1 / p.mass));
        
    }

    public static void times2(Double d) {
        d = d * 2;
    }
}
