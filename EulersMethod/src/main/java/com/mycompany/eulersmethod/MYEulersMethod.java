/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.EulersMethod;

/**
 *
 * @author pjain
 */
public class MYEulersMethod implements Solution {

    EField eField;

    MYEulersMethod(EField eField) {
        this.eField = eField;
    }

    public Particle step(Particle p, Double stepSize) {

        //Particle pReturn = new Particle(p.pos, p.vel, p.polarity, p.time, p.mass);
        p.totalEnergy(eField);

        Vector acceleration = eField.forceOnCharge(p).scale(1 / p.mass);
        //System.out.println("Acceleration: " + acceleration);
        Vector velocity = p.vel.sum(acceleration.scale(stepSize));
        //System.out.println("Velocity: " + p.vel);
        p.pos.plusEquals(p.vel.scale(stepSize));
        //System.out.println("Position: " + p.pos);  

        p.vel = velocity;
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
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
        int totalBatches = (int)(Math.round(numberOfSteps / batchSize));
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
