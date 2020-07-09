/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.materials;

public class Vacuum extends Air {

    private static Vacuum instance;

    public Vacuum() {
        // Vacuum is just air at really low pressure
        // 1 micron = 0.0001 kPa
        super("Vacuum", 1e-4);
    }

    public Vacuum(String name) {
        // Vacuum is just air at really low pressure
        // 1 micron = 0.0001 kPa
        super(name, 1e-4);
    }

    // we only need one of these objects
    public static synchronized Vacuum getInstance() {
        if (instance == null) {
            Vacuum.instance = new Vacuum();
        }
        return (Vacuum) instance;
    }
}
