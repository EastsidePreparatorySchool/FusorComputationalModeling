/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Isotope;

//
// 
//
public class E1H extends Isotope {

    private static E1H instance;

    public E1H() {
        super("1H", 1, 0);
        //System.out.println("in h constructor");
    }

    // we only need one of these objects
    public static synchronized E1H getInstance() {
        //System.out.println("in h getinstance");
        if (instance == null) {
            E1H.instance = new E1H();
        }
        return instance;
    }

}
