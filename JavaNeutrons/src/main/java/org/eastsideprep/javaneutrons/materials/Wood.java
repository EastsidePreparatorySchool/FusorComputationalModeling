/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

//
// Wood (Material 359, Southern Pine, with omissions)
//
//
public class Wood extends Material {

    static Wood instance;

    Wood() {
        super("Wood");
        this.addComponent(E1H.getInstance(), 0.462423);
        this.addComponent(E12C.getInstance(), 0.323389);
        this.addComponent(E14N.getInstance(), 0.002773);
        this.addComponent(E16O.getInstance(), 0.208779);
        this.calculateAtomicDensities(640);
    }

    Wood(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Wood getInstance() {
        if (instance == null) {
            Wood.instance = new Wood();
        }
        return instance;
    }

}