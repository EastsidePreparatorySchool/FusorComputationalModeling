/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.assemblies.Element;

//
// 
//
public class Hydrogen extends Element {

    private static Hydrogen instance;

    Hydrogen() {
        super("Hydrogen", 1, 0);
        //System.out.println("in h constructor");
    }

    // we only need one of these objects
    public static synchronized Hydrogen getInstance() {
        //System.out.println("in h getinstance");
        if (instance == null) {
            Hydrogen.instance = new Hydrogen();
        }
        return instance;
    }

}
