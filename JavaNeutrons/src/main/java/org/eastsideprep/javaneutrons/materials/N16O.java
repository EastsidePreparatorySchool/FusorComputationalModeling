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
public class N16O extends Nuclide {

    private static N16O instance;

    N16O() {
        super("16O", 8, 8, 2.6566962e-26);
    }

    // we only need one of these objects
    public static synchronized N16O getInstance() {
        if (instance == null) {
            N16O.instance = new N16O();
        }
        return instance;
    }

}
