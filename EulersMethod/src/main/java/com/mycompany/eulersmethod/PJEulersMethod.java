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

    public Particle step(Particle p, Double stepSize) {

        p.totalEnergy(eField);
        Vector acceleration = eField.forceOnCharge(p).scale(1/p.mass);
        Vector velocity = acceleration.scale(stepSize).sum(p.vel);
        Vector position = p.pos.sum(velocity);
        
        p.vel = velocity;
        p.time += stepSize;

        return p;
    }

}