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
public class E40Ar extends Isotope {

    private static E40Ar instance;

    E40Ar() {
        super("40 Argon", 18, 22, 39.948*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized E40Ar getInstance() {
        if (instance == null) {
            E40Ar.instance = new E40Ar();
        }
        return instance;
    }

}
