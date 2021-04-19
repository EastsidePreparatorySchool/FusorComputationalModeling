/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

//
// Concrete, regular (material 99)
//
//
public class Concrete extends Material {

    static Concrete instance;

    Concrete() {
        super("Concrete");
        this.addComponent(N1H.getInstance(), 0.168038);
        this.addComponent(N16O.getInstance(), 0.563183);
        this.addComponent(N28Si.getInstance(), 0.203231);
        this.addComponent(N56Fe.getInstance(), 0.004246);
        this.calculateAtomicDensities(203.231);
    }

    Concrete(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Concrete getInstance() {
        if (instance == null) {
            Concrete.instance = new Concrete();
        }
        return instance;
    }

}
