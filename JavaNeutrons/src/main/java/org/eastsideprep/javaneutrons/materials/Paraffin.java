/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.assemblies.Material;

//
// Paraffin
// Cn H(2n+2)
// one major component is hentriacontane n=31: C31 H64
// source: https://en.wikipedia.org/wiki/Paraffin_wax
// density 781 kg/m^3
// source: Google answer "hentriacontane density"
//
public class Paraffin extends Material {

    private static Paraffin instance;

    Paraffin() {
        super("Paraffin");
        this.addComponent(Carbon.getInstance(), 324689);
        //System.out.println("before adding h");
        this.addComponent(Hydrogen.getInstance(), 675311);
        //System.out.println("before adding h");
        this.calculateAtomicDensities(930);
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
