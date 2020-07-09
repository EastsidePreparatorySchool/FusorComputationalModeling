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
public class E208Pb extends Isotope {

    private static E208Pb instance;

    E208Pb() {
        super("208 Pb", 82, 208-82, -1);
    }

    // we only need one of these objects
    public static synchronized E208Pb getInstance() {
        if (instance == null) {
            E208Pb.instance = new E208Pb();
        }
        return instance;
    }

}
