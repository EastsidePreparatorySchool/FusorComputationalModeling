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

import com.mycompany.EulersMethod.EField;
import com.mycompany.EulersMethod.InputHandler;

public class PJRungeKutta implements Solution {

    EField e;

    PJRungeKutta(EField e) {
        this.e = e;
    }

    public Particle f(Particle p){
        
        Vector first3 = p.vel.clone();
        Vector last3 = e.forceOnCharge(p).scale(1/p.mass);
        Particle j = p.clone();
        j.pos = first3;
        j.vel = last3;
        
        return j;

    }
    public Particle multiplyAndAdd(Particle a, Particle b, Double c){
        Particle bc = b.clone();
        bc.multiply(c);
        bc.plusEquals(a.clone());
        return bc;
    }
    @Override
    public Particle step(Particle p, Double stepSize) {
                           
        p.totalEnergy(e);
        Particle k1 = f(p.clone());
        Particle k1_2 = multiplyAndAdd(p.clone(), k1.clone(), stepSize/2);
        /*Particle k1_2 = p.clone();
        Particle x = k1.clone();
        x.multiply(stepSize/2);
        k1_2.plusEquals(x);*/
        Particle k2 = f(k1_2.clone());
        
        Particle k2_3 = multiplyAndAdd(p.clone(), k2.clone(), stepSize/2);
        /*Particle y = k2.clone();
        y.multiply(stepSize/2);
        k2_3.plusEquals(y);*/
        Particle k3 = f(k2_3.clone());
        
        Particle k3_4 = multiplyAndAdd(p.clone(), k3.clone(), stepSize);
        /*Particle z = k3.clone();
        z.multiply(stepSize);
        k3_4.plusEquals(z);*/
        Particle k4 = f(k3_4.clone());
                
        Particle p2 = p.clone();
        
       /* k2_3.multiply(2.0);
        k3_4.multiply(2.0);*/
        
        Particle[] k5 = {k2.multiply(2),k3.multiply(2),k4};
        k1.plusEquals(k5);
        Particle pFinal = k1.multiply(stepSize/6);
        p2.plusEquals(pFinal);
        p2.time += stepSize;
        p2.totalEnergy(e);

        return p2;
    }
        

    @Override
    public Particle[] epoch(Particle p, Double stepSize, Double numberOfSteps, int batchSize) {
       
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   
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


