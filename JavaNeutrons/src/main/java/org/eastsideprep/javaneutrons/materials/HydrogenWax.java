/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Gas;

public class HydrogenWax extends Gas {

    private static HydrogenWax instance;

    HydrogenWax(double pressure) {
        this("HydrogenWax", pressure);
    }

    HydrogenWax(String name, double pressure) {
        super(name, pressure);
        
        double massDensitySTP = 138.1; //930.0*2.0/3.0/4.4894;  // to equal 1H density in paraffin wax

        this.addComponent(E1H.getInstance(), 1);

        // that's 100 Pa for STP
        this.calculateAtomicDensities(massDensitySTP * pressure / 100);
    }


    // we only need one of these objects
    public static synchronized HydrogenWax getInstance() {
        if (instance == null) {
            HydrogenWax.instance = new HydrogenWax(100);
        }
        return instance;
    }
  

}
