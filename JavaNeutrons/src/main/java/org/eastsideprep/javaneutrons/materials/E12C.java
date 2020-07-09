/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Isotope;

/**
 *
 * @author gunnar
 */
public class E12C extends Isotope {

    private static E12C instance;

    E12C() {
        super("12C", 6, 6, 1.9944235e-26);
    }


    // we only need one of these objects
    public static synchronized E12C getInstance() {
        if (instance == null) {
            E12C.instance = new E12C();
        }
        return instance;
    }

}
