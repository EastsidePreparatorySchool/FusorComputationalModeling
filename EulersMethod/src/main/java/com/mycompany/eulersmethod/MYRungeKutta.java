/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

/**
 *
 * @author myan
 */
public class MYRungeKutta implements Solution {

    EField eField;

    MYRungeKutta(EField eField) {
        this.eField = eField;
    }

    public Particle f(Particle p) {
        Vector first3 = p.vel;
        Vector last3 = eField.forceOnCharge(p).scale(1 / p.mass);
        Particle p1 = p.clone();
        p1.pos = first3;
        p1.vel = last3;
        return p1;
    }

    @Override
    public Particle step(Particle p, Double stepSize) {
        p.totalEnergy(eField);

        Particle k1 = f(p.clone());
        //System.out.println(k1);
        Particle p1 = p.clone();
        p1.plusEquals(k1.clone().multiply(stepSize / 2));

        Particle k2 = f(p1);
        //System.out.println(k2);
        Particle p2 = p.clone();
        p2.plusEquals(k2.clone().multiply(stepSize / 2));
        Particle k3 = f(p2);
        //System.out.println(k3);
        Particle p3 = p.clone();
        p3.plusEquals(k3.clone().multiply(stepSize));
        Particle k4 = f(p3);
        //System.out.println(k4);

        Particle p4 = p.clone();
        k2.multiply(2.0);
        k3.multiply(2.0);

        Particle[] k = {k1, k2, k3, k4};
        p4.plusEquals(k);
        p4.multiply(stepSize / 6);

        p4.time += stepSize;
        //System.out.println(p4);
        p4.totalEnergy(eField);
        return p4;
    }

    public Particle[] epoch(Particle p, Double stepSize, Double numberOfSteps, int batchSize) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
        int totalBatches = (int) (Math.round(numberOfSteps / batchSize));
        int batchCount = 0;
        int i = 0;
        Particle[] particles = new Particle[totalBatches];

        while (batchCount != totalBatches) {

            if (i < batchSize) {
                p = step(p, stepSize);
                i++;
                System.out.println("Number of steps complete: " + i + "/" + batchSize);
                //System.out.println("Time: " + System.currentTimeMillis());

            } else if (i == batchSize) {
                i = 0;
                particles[batchCount] = p.clone();
                //System.out.println("Particle: " + particles[batchCount]);
                batchCount++;
                System.out.println("Number of Batches complete: " + batchCount + "/" + totalBatches);
                //System.out.println("Time: " + System.currentTimeMillis());

            }

        }
        return particles;
    }
}
