/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

//
// "Lead" material #171
//
//
public class Lead extends Material {

    static Lead instance;

    public Lead() {
        super("Lead");
        this.addComponent(N207Pb.getInstance(), 1.0);
        this.calculateAtomicDensities(11.350);
    }
    
    // we only need one of these objects
    public static synchronized Lead getInstance() {
        if (instance == null) {
            Lead.instance = new Lead();
        }
        return instance;
    }

}
