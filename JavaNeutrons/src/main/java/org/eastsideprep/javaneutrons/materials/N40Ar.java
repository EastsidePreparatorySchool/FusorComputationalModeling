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
public class N40Ar extends Nuclide {

    private static N40Ar instance;

    N40Ar() {
        super("40 Argon", 18, 22, 39.948*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N40Ar getInstance() {
        if (instance == null) {
            N40Ar.instance = new N40Ar();
        }
        return instance;
    }

}
