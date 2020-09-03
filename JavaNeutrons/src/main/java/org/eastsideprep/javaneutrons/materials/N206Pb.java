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
public class N206Pb extends Nuclide {

    private static N206Pb instance;

    N206Pb() {
        super("206 Pb", 82, 206-82, 205.9744653*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N206Pb getInstance() {
        if (instance == null) {
            N206Pb.instance = new N206Pb();
        }
        return instance;
    }

}
