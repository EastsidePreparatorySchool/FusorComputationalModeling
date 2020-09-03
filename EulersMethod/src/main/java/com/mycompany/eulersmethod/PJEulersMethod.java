/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

import com.mycompany.EulersMethod.EField;
import com.mycompany.EulersMethod.InputHandler;
import com.mycompany.EulersMethod.Particle;
import com.mycompany.EulersMethod.Vector;

/**
 *
 * @author pjain
 */
public class PJEulersMethod implements Solution {

    EField eField;

    PJEulersMethod(EField eField) {
        this.eField = eField;
    }

    PJEulersMethod() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Particle step(Particle p, Double stepSize) {

        p.totalEnergy(eField);
        Vector acceleration = eField.forceOnCharge(p).scale(1 / p.mass);
        Vector velocity = acceleration.scale(stepSize).sum(p.vel);
        Vector position = p.pos.sum(p.vel.scale(stepSize));

        p.vel = velocity;
        p.pos = position;
        p.time += stepSize;

        return p;
    }

    //the Epoch Method should take in all of the parameters
    // and return an array of particles, the size of numberofSteps/batchsize
    // full of particles completed in batches of size batchSize
    // Every batch is simply stepping a particle through batchSize times
    // and then saving only the batchSize-th particle to the array
    // And repeating; for a batchSize of 1,000 and # of steps 10M
    // that looks like an array of 10,000 particles
    @Override
    public Particle[] epoch(Particle p, Double stepSize, Double numberOfSteps, int batchSize) {
        // throw new UnsupportedOperationException("Not supported yet.");//To change body of generated methods, choose Tools | Templates.

        int batchesNeeded = (int) (numberOfSteps/batchSize);
        int batchesCompleted = 0;
        int steps = 0;
        Particle[] j = new Particle[batchesNeeded];
        long startTime = System.currentTimeMillis();
        Double bcompleted = (double)batchesCompleted;
        Double bneeded = (double)batchesNeeded;
        

        while (batchesCompleted != batchesNeeded) {
            System.out.println("Batches Completed: " + batchesCompleted);
            System.out.println("Batches Needed: " + batchesNeeded);
            System.out.println("Overall Progress: " + ((bcompleted/bneeded)*100) + "%");
            System.out.println("This code has taken: " + ((System.currentTimeMillis() - startTime)/1000) + " seconds to process.");

            if (steps < batchSize) {
                step(p, stepSize);
                steps++;
            } else if (steps == batchSize) {
                steps = 0;
                j[batchesCompleted] = p.clone();
                batchesCompleted++;
                bcompleted++;
            }

        }
        return j;
    }
}