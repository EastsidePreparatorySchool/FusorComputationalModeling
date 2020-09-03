/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Nuclide;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class N56Fe extends Nuclide {

    private static N56Fe instance;

    N56Fe() {
        super("56 Iron", 26, 30, 9.2732796e-26);
    }

    // we only need one of these objects
    public static synchronized N56Fe getInstance() {
        if (instance == null) {
            N56Fe.instance = new N56Fe();
        }
        return instance;
    }

}
