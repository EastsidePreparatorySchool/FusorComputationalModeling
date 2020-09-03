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
public class N207Pb extends Nuclide {

    private static N207Pb instance;

    N207Pb() {
        super("207 Pb", 82, 207-82, 206.9758969*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized N207Pb getInstance() {
        if (instance == null) {
            N207Pb.instance = new N207Pb();
        }
        return instance;
    }

}
