/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Nuclide;

/**
 *
 * @author gunnar
 */
public class N12C extends Nuclide {

    private static N12C instance;

    N12C() {
        super("12C", 6, 6, 1.9944235e-26);
    }


    // we only need one of these objects
    public static synchronized N12C getInstance() {
        if (instance == null) {
            N12C.instance = new N12C();
        }
        return instance;
    }

}
