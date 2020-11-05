package org.eastsideprep.javaneutrons.core;

import org.apache.commons.math3.geometry.euclidean.threed.*;

public final class Gamma extends Particle {

    public static double totalPE = 0.0;
    public static int countPE = 0;
    public static double totalNE = 0.0;
    public static int countNE = 0;
    public static double totalNE2 = 0.0;
    public static int countNE2 = 0;
    
    public Gamma(Vector3D position) {
        this(position, null, 0, null);
    }

    public Gamma(Vector3D position, Vector3D direction, double energy, MonteCarloSimulation mcs) {
        super(position, direction, energy, mcs);
        type = "gamma";
    }

    @Override
    Event nextPoint(Material m) {
        // return something far out of bounds
        double t = 10000;
        return new Event(Util.Math.rayPoint(position, direction, t), Event.Code.Scatter, t);
    }
}
