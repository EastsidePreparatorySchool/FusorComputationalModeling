/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

public class CarbonWax extends Material {

    private static CarbonWax instance;

    CarbonWax() {
        this("CarbonWax");
    }

    CarbonWax(String name) {
        super(name);
        
        double massDensity = 930-138.1; 
        this.addComponent(E12C.getInstance(), 1);
        this.calculateAtomicDensities(massDensity);
    }


    // we only need one of these objects
    public static synchronized CarbonWax getInstance() {
        if (instance == null) {
            CarbonWax.instance = new CarbonWax();
        }
        return instance;
    }
  

}
