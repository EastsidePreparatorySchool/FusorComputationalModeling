/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

//
// Water (H2O) Material # 354
//
public class Water extends Material {

    static Water instance;

    Water() {
        super("Water");
        this.addComponent(N1H.getInstance(), 0.666657);
        this.addComponent(N16O.getInstance(), 0.333343);
        this.calculateAtomicDensities(998);
    }

    Water(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized Water getInstance() {
        if (instance == null) {
            Water.instance = new Water();
        }
        return instance;
    }

}
