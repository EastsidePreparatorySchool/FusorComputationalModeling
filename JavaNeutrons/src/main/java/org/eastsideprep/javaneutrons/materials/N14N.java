/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Nuclide;

/**
 *
 * @author gunnar
 */
public class N14N extends Nuclide {

    private static N14N instance;

    N14N() {
        super("14N", 7, 7, 2.3258671e-26);
    }

    // we only need one of these objects
    public static synchronized N14N getInstance() {
        if (instance == null) {
            N14N.instance = new N14N();
        }
        return instance;
    }

}
