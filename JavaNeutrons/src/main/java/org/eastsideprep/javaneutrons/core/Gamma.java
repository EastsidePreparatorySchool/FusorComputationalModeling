package org.eastsideprep.javaneutrons.core;

import org.apache.commons.math3.geometry.euclidean.threed.*;

public final class Gamma extends Particle {

    public static double totalPE = 0.0;
    public static int countPE = 0;
    public static double totalNE = 0.0;
    public static int countNE = 0;
    public static double totalNE2 = 0.0;
    public static int countNE2 = 0;

    // these are for debugging, mostly
    public Nuclide producer;
    public double neutronEnergy;
    
    public Gamma(Vector3D position, Nuclide producer, double ne) {
        this(position, null, 0, null, producer, ne);
    }

    public Gamma(Vector3D position, Vector3D direction, double energy, MonteCarloSimulation mcs, Nuclide producer, double ne) {
        super(position, direction, energy, mcs);
        this.type = "gamma";
        this.producer = producer;
        this.neutronEnergy = ne;
    }

    @Override
    Event nextPoint(Material m) {
        // return something far out of bounds
        double t = 10000000;
        return new Event(Util.Math.rayPoint(position, direction, t), Event.Code.Gone, t);
    }
}
