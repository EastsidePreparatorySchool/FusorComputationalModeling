/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

// paraffin wax material ???
public class Paraffin extends Material {

    private static Paraffin instance;

    Paraffin() {
        super("Paraffin");
        this.addComponent(E12C.getInstance(), 0.324689);
        this.addComponent(E1H.getInstance(), 0.675311);
        this.calculateAtomicDensities(930.0);
    }

    Paraffin(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Paraffin getInstance() {
        try {
        if (instance == null) {
            Paraffin.instance = new Paraffin();
        }
        } catch (Exception e) {
            System.out.println("in paraffin "+e);
        }
        return instance;
    }
}
