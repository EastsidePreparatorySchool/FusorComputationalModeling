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
public interface Solution {
    // your solution should probably have a method like "step" in it
    // so you can inherit this interface

    public Particle step(Particle p, Double stepSize);

    //the Epoch Method should take in all of the parameters
    // and return an array of particles, the size of numberofSteps/batchsize
    // full of particles completed in batches of size batchSize
    // Every batch is simply stepping a particle through batchSize times
    // and then saving only the batchSize-th particle to the array
    // And repeating; for a batchSize of 1,000 and # of steps 10M
    // that looks like an array of 10,000 particles
    public Particle[] epoch(Particle p, Double stepSize, Double numberOfSteps, int batchSize);

}
