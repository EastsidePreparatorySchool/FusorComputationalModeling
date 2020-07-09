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
public class E56Fe extends Isotope {

    private static E56Fe instance;

    E56Fe() {
        super("56 Iron", 26, 30, 9.2732796e-26);
    }

    // we only need one of these objects
    public static synchronized E56Fe getInstance() {
        if (instance == null) {
            E56Fe.instance = new E56Fe();
        }
        return instance;
    }

}
