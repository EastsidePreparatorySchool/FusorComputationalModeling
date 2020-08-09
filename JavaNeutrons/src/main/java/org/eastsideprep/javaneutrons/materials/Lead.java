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
        this.addComponent(E207Pb.getInstance(), 1.0);
        this.calculateAtomicDensities(11350);
    }

    public Lead(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Lead getInstance() {
        if (instance == null) {
            Lead.instance = new Lead();
        }
        return instance;
    }

}
