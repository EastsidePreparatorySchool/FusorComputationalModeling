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
public class N208Pb extends Nuclide {

    private static N208Pb instance;

    N208Pb() {
        super("208 Pb", 82, 208-82, 207.9766521*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N208Pb getInstance() {
        if (instance == null) {
            N208Pb.instance = new N208Pb();
        }
        return instance;
    }

}
