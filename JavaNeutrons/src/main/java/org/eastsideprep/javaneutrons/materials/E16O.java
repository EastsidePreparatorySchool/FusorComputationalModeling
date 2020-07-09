/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Isotope;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class E16O extends Isotope {

    private static E16O instance;

    E16O() {
        super("16O", 8, 8, 2.6566962e-26);
    }

    // we only need one of these objects
    public static synchronized E16O getInstance() {
        if (instance == null) {
            E16O.instance = new E16O();
        }
        return instance;
    }

}
