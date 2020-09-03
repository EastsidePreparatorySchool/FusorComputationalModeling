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
public class SLRungeKutta {

    EField e;
    SLRungeKutta(EField e){
        this.e = e;
    }
    public Particle f(Particle p) {
        Particle p1 = p.clone();
        p1.pos = p.vel.clone();
        p1.vel = e.forceOnCharge(p).scale(1/p.mass);
        return p1;
    }

    public Particle scaleAdd(Particle y, Particle k, Double h) {
        Particle hk = k.clone();
        hk.multiply(h);
        hk.plusEquals(y.clone());
        return hk;

    }

    public Particle step(Particle p, Double stepSize) {
        p.totalEnergy(e);
        Particle k1 = f(p.clone());
        Particle hk1_2 = scaleAdd(p.clone(), k1.clone(), stepSize / 2);
        Particle k2 = f(hk1_2.clone());
        Particle hk2_2 = scaleAdd(p.clone(), k2.clone(), stepSize / 2);
        Particle k3 = f(hk2_2.clone());
        Particle hk3 = scaleAdd(p.clone(), k3.clone(), stepSize);
        Particle k4 = f(hk3.clone());
        Particle p2 = p.clone();
        Particle[] k_2_4 = {k2.multiply(2), k3.multiply(2), k4};
        k1.plusEquals(k_2_4);
        Particle k = k1.multiply(stepSize / 6);
        p2.plusEquals(k);
        p2.time +=stepSize;
        p2.totalEnergy(e);
        return p2;

    }

    //the Epoch Method should take in all of the parameters
    // and return an array of particles, the size of numberofSteps/batchsize
    // full of particles completed in batches of size batchSize
    // Every batch is simply stepping a particle through batchSize times
    // and then saving only the batchSize-th particle to the array
    // And repeating; for a batchSize of 1,000 and # of steps 10M
    // that looks like an array of 10,000 particles
    public Particle[] epoch(Particle p, Double stepSize, Double numberOfSteps, int batchSize) {
        int batchNumber = (int) (numberOfSteps / batchSize);
        Particle[] particles = new Particle[batchNumber];
        long epochStart = System.currentTimeMillis();
        for (int i = 0; i < batchNumber; i++) {
            long batchStart = System.currentTimeMillis();
            for (int j = 0; j < batchSize; j++) {

                p = step(p, stepSize);
            }
            particles[i] = p.clone();
            System.out.println("Batches Completed: " + i + "/" + batchNumber);
            System.out.println("Percentage: " + (i / batchNumber)*100 + "%");
            long currentTime = System.currentTimeMillis();
            System.out.println("Time this Batch: " + (currentTime - batchStart));
        }
        System.out.println("Epoch Time: " + (System.currentTimeMillis() - epochStart));
        
        return particles;
    }

}
