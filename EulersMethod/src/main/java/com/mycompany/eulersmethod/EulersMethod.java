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
public class EulersMethod implements Solution {

    EField eField;

    EulersMethod(EField eField) {
        this.eField = eField;
    }


    @Override
    public Particle step(Particle p, Double stepSize) {
        Particle testParticle = new Particle(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0);
        return testParticle;
    }
}