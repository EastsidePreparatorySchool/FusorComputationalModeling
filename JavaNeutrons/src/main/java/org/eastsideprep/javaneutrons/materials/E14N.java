/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Isotope;

/**
 *
 * @author gunnar
 */
public class E14N extends Isotope {

    private static E14N instance;

    E14N() {
        super("14N", 7, 7, 2.3258671e-26);
    }

    // we only need one of these objects
    public static synchronized E14N getInstance() {
        if (instance == null) {
            E14N.instance = new E14N();
        }
        return instance;
    }

}
