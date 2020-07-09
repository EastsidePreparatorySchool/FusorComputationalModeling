/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

public class HighVacuum extends Air {

    private static HighVacuum instance;

    public HighVacuum() {
        // Vacuum is just air at really low pressure
        // 1 micron = 0.0001 kPa
        super("HighVacuum", 1e-8);
    }

    public HighVacuum(String name) {
        // Vacuum is just air at really low pressure
        // 1 micron = 0.0001 kPa
        super(name, 1e-8);
    }

    // we only need one of these objects
    public static synchronized HighVacuum getInstance() {
        if (instance == null) {
            HighVacuum.instance = new HighVacuum();
        }
        return (HighVacuum) instance;
    }
}
