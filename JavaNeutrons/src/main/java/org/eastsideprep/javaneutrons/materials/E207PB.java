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
public class E207PB extends Isotope {

    private static E207PB instance;

    E207PB() {
        super("207 Pb", 82, 207-82, -1);
    }

    // we only need one of these objects
    public static synchronized E207PB getInstance() {
        if (instance == null) {
            E207PB.instance = new E207PB();
        }
        return instance;
    }

}
