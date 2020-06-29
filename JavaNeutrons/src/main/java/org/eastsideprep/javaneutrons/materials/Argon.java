/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.assemblies.Element;
import org.eastsideprep.javaneutrons.core.Util;

/**
 *
 * @author gunnar
 */
public class Argon extends Element {

    private static Argon instance;

    Argon() {
        super("Argon", 18, 22, 39.948*Util.Physics.Da);
    }

    // we only need one of these objects
    public static synchronized Argon getInstance() {
        if (instance == null) {
            Argon.instance = new Argon();
        }
        return instance;
    }

}
