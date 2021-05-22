/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

// paraffin wax material 358
public class Paraffin extends Material {

    private static Paraffin instance;

    Paraffin() {
        super("Paraffin");
        this.addComponent(N1H.getInstance(), 0.675311);
        this.addComponent(N12C.getInstance(), 0.324689);
        this.calculateAtomicDensities(930.0);
    }

    Paraffin(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Paraffin getInstance() {
        if (instance == null) {
            Paraffin.instance = new Paraffin();
        }
        return instance;
    }
}
