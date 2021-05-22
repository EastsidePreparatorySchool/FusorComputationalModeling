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
public class ConcreteNIST extends Material {

    static ConcreteNIST instance;

    ConcreteNIST() {
        super("Concrete");
        this.addComponent(N1H.getInstance(), 0.305330);
        this.addComponent(N12C.getInstance(), 0.002880);
        this.addComponent(N16O.getInstance(), 0.500407);
        this.addComponent(N23Na.getInstance(), 0.009212);
        //this.addComponent(N__Mg.getInstance(), 0.000725);
        this.addComponent(N27Al.getInstance(), 0.010298);
        this.addComponent(N28Si.getInstance(), 0.151042);
        //this.addComponent(N__K.getInstance(), 0.003578);
        //this.addComponent(N__Ca.getInstance(), 0.014924);
        this.addComponent(N56Fe.getInstance(), 0.001605);
        this.calculateAtomicDensities(2.300);
    }

    ConcreteNIST(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized ConcreteNIST getInstance() {
        if (instance == null) {
            ConcreteNIST.instance = new ConcreteNIST();
        }
        return instance;
    }

}
