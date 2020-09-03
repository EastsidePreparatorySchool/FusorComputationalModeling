/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Gas;
import org.eastsideprep.javaneutrons.core.Event;

public class HydrogenGas extends Gas {

    private static HydrogenGas instance;

    HydrogenGas(double pressure) {
        this("HydrogenGas", pressure);
    }

    HydrogenGas(String name, double pressure) {
        super(name, pressure);
        
        double massDensitySTP = 0.084;
        // to equal 1H density in paraffin wax: massDensitySTP = 930.0*2.0/3.0;

        this.addComponent(N1H.getInstance(), 1);

        // that's 100 Pa for STP
        this.calculateAtomicDensities(massDensitySTP * pressure / 100);
    }


    // we only need one of these objects
    public static synchronized HydrogenGas getInstance() {
        if (instance == null) {
            HydrogenGas.instance = new HydrogenGas(100);
        }
        return instance;
    }
    
 

}
