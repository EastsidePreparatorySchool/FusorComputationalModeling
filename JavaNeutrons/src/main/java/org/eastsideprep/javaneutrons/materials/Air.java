/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Event;

/**
 *
 * @author gmein
 */
//
// air is the material for NegativeSpace - everything around the parts 
// of an Assembly
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
        
        // Google answer "density of air"
        double massDensitySTP = 1.205;

        this.addComponent(Carbon.getInstance(), 0.000150);
        this.addComponent(Nitrogen.getInstance(), 0.784431);
        this.addComponent(Oxygen.getInstance(), 0.210748);
        this.addComponent(Argon.getInstance(), 0.004671);

        // that's 100 Pa for STP
        this.calculateAtomicDensities(massDensitySTP * pressure / 100);
    }


    // we only need one of these objects
    public static synchronized Air getInstance() {
        if (instance == null) {
            Air.instance = new Air(100);
        }
        return instance;
    }
    
    @Override
    public void processEvent(Event e){
//        if (e.code == Event.Code.Capture && e.neutron.energy >= 2*Util.Physics.eV){
//            System.out.println("hah!");
//        }
        super.processEvent(e);
    }

}
