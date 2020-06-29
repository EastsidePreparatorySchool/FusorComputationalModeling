/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.assemblies.Material;

//
// Steel
//
//
public class Steel extends Material {

    static Steel instance;

    Steel() {
        super("Steel");
        this.addComponent(Carbon.getInstance(), 0.022831);
        this.addComponent(Iron.getInstance(), 0.977169);
        this.calculateAtomicDensities(7820);
    }

    Steel(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Steel getInstance() {
        if (instance == null) {
            Steel.instance = new Steel();
        }
        return instance;
    }

}
