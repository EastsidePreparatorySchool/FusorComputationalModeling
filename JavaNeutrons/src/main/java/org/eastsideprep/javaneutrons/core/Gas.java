package org.eastsideprep.javaneutrons.core;

//
// air is the material for NegativeSpace - everything around the parts 
// of an Assembly
public abstract class Gas extends Material {

    protected double pressure;

    // pressure is in kPa

    public Gas(String name, double pressure) {
        super(name);
        this.pressure = pressure;
    }
    
    // use this for a single-element gas
    public Gas(String name, Nuclide element, double pressure, double massDensitySTP){
        super(name);
        this.pressure = pressure;
        
        this.addComponent(element, 1);
        this.calculateAtomicDensities(massDensitySTP * pressure);
    }

}
