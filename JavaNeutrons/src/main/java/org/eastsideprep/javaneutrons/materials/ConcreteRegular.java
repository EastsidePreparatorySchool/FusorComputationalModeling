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
public class ConcreteRegular extends Material {

    static ConcreteRegular instance;

    ConcreteRegular() {
        super("Concrete");
        this.addComponent(N1H.getInstance(), 0.168038);
        this.addComponent(N16O.getInstance(), 0.563183);
        this.addComponent(N23Na.getInstance(), 0.021365);
        this.addComponent(N27Al.getInstance(), 0.021343);
        this.addComponent(N28Si.getInstance(), 0.203231);
        this.addComponent(N56Fe.getInstance(), 0.004246);
        this.calculateAtomicDensities(2.300);
    }

    ConcreteRegular(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized ConcreteRegular getInstance() {
        if (instance == null) {
            ConcreteRegular.instance = new ConcreteRegular();
        }
        return instance;
    }

}
