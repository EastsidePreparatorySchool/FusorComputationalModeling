package org.eastsideprep.javaneutrons.materials;

import org.eastsideprep.javaneutrons.core.Material;

//
// Human tissue
// source:https://en.wikipedia.org/wiki/Composition_of_the_human_body
//
public class HumanBodyMaterial extends Material {

    static HumanBodyMaterial instance;

    HumanBodyMaterial() {
        super("HumanBodyMaterial");
        this.addComponent(N1H.getInstance(), 0.62);
        this.addComponent(N16O.getInstance(), 0.24);
        this.addComponent(N12C.getInstance(), 0.12);
        this.addComponent(N14N.getInstance(), 0.11);
        this.calculateAtomicDensities(1000);
    }

    HumanBodyMaterial(String name) {
        super(name);
    }

    // we only need one of these objects
    public static synchronized HumanBodyMaterial getInstance() {
        if (instance == null) {
            HumanBodyMaterial.instance = new HumanBodyMaterial();
        }
        return instance;
    }

}
