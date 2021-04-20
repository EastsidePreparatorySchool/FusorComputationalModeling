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
public class N27Al extends Nuclide {

    private static N27Al instance;

    N27Al() {
        super("27Al", 13, 14, 26.981539*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N27Al getInstance() {
        if (instance == null) {
            N27Al.instance = new N27Al();
        }
        return instance;
    }

}
