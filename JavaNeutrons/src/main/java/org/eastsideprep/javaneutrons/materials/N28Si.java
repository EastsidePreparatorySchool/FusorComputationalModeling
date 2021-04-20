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
public class N28Si extends Nuclide {

    private static N28Si instance;

    N28Si() {
        super("14Si", 14, 14, 27.9769265*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N28Si getInstance() {
        if (instance == null) {
            N28Si.instance = new N28Si();
        }
        return instance;
    }

}
