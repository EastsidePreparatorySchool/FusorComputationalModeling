/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Gas;
import org.eastsideprep.javaneutrons.core.Event;
import org.eastsideprep.javaneutrons.core.Material;

// material 4 - Air near sea-level
public class Air extends Gas {

    private static Air instance;

    // use this for Air (or getInstance()
    // pressure is in kPa
    Air(double pressure) {
        this("Air", pressure);

    }

    // use this for air at different pressure
    // give it a different name, of course
    Air(String name, double pressure) {
        super(name, pressure);

        double massDensitySTP = 1.205;

        this.addComponent(E12C.getInstance(), 0.000150);
        this.addComponent(E14N.getInstance(), 0.784431);
        this.addComponent(E16O.getInstance(), 0.210748);
        this.addComponent(E40Ar.getInstance(), 0.004671);

        // that's 100 Pa for STP
        this.calculateAtomicDensities(massDensitySTP * pressure / 100);
    }

    // we only need one of these objects
    public static synchronized Air getInstance(String name) {
        // find or make the named one
        if (name != null) {
            Material m = Material.getByName(name);
            if (m == null) {
                return new Air(name, 100);
            }
        }
        // or dish out the shared anonymous instance
        if (instance == null) {
            Air.instance = new Air(100);
        }
        return instance;
    }

    @Override
    public void processEvent(Event e) {
//        if (e.code == Event.Code.Capture && e.neutron.energy >= 2*Util.Physics.eV){
//            System.out.println("hah!");
//        }
        super.processEvent(e);
    }

}
