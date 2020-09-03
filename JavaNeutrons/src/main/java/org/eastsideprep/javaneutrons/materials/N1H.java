/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Nuclide;

//
// 
//
public class N1H extends Nuclide {

    private static N1H instance;

    public N1H() {
        super("1H", 1, 0);
        //System.out.println("in h constructor");
    }

    // we only need one of these objects
    public static synchronized N1H getInstance() {
        //System.out.println("in h getinstance");
        if (instance == null) {
            N1H.instance = new N1H();
        }
        return instance;
    }

}
