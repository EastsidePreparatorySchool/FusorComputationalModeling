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
public class N23Na extends Nuclide {

    private static N23Na instance;

    N23Na() {
        super("23Na", 11, 12, 22.98976928*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N23Na getInstance() {
        if (instance == null) {
            N23Na.instance = new N23Na();
        }
        return instance;
    }

}
