package org.eastsideprep.javaneutrons.core;

import org.apache.commons.math3.geometry.euclidean.threed.*;

public final class Neutron extends Particle {

    public static double totalPE = 0.0;
    public static int countPE = 0;
    public static double totalNE = 0.0;
    public static int countNE = 0;
    public static double totalNE2 = 0.0;
    public static int countNE2 = 0;

    final public static double mass = 1.67492749804e-27; // SI
    final public static double startingEnergyDD = 3.925333e-13 * 1e4; //SI for cm
    // factor 1e4 is from using cm, not m here - 100^2

    public Vector3D velocity; // kept in parallel with energy and direction, see set() methods

    public Neutron(Vector3D position, Vector3D direction, double energy, MonteCarloSimulation mcs) {
        super(position, direction, energy, mcs);
        setDirectionAndEnergy(direction, energy);
        type = "neutron";
    }

    public final void setVelocity(Vector3D velocity) {
        double speed = velocity.getNorm();
        this.direction = velocity.normalize();

        // should not have to do relativistic calculation since rest energy = 939MeV
        this.energy = speed * speed * Neutron.mass / 2;
        this.velocity = velocity;
    }

    @Override
    final public void setDirectionAndEnergy(Vector3D direction, double energy) {
        this.direction = direction.normalize();
        this.energy = energy;
        this.velocity = this.direction.scalarMultiply(Math.sqrt(energy * 2 / Neutron.mass));
    }

    @Override
    Event nextPoint(Material m) {
        return m.nextPoint(this);
    }

    public Vector3D getScatteredVelocity(Event e, Vector3D neutronVelocity) {
        Nuclide i = e.nuclide;
        double cos_theta;
        /*
        if (i.angles != null) {
            double neutronSpeed = neutronVelocity.getNorm();
            double energyEV = 0.5 * Neutron.mass * neutronSpeed * neutronSpeed / Util.Physics.eV;

            // get the scattering angle from a random lookup in the tables
            cos_theta = i.getScatterCosTheta(energyEV);

            // construct vector and return
            Vector3D v = Util.Math.randomDir(cos_theta, neutronSpeed);
            // random vector was scattered around Z, rotate to match axis of incoming neutron
            Rotation r = new Rotation(Vector3D.PLUS_K, neutronVelocity);
            v = r.applyTo(v);

            //System.out.println("v: " + v);
            return v;

        } else {
        */
            return getIsotropicScatteredVelocity(e, neutronVelocity);
        /*}*/
    }

    public Vector3D getIsotropicScatteredVelocity(Event e, Vector3D neutronVelocity) {
        Vector3D v;
        Nuclide i = e.nuclide;
        double vneutron = neutronVelocity.getNorm();

        v = Util.Math.randomDir();
        e.cos_theta = v.dotProduct(neutronVelocity.scalarMultiply(1 / vneutron));
        v = v.scalarMultiply(vneutron);
        return v;
    }

    private double pickTargetSpeed(double vneutron, Nuclide i) {
        // according to OpenMC documentation chapter 5.10.1
        double beta = Math.sqrt(i.mass / (2 * Util.Physics.kB * Util.Physics.T));
        double y = beta * vneutron;
        double u;

        double eta1 = Util.Math.random();
        if (eta1 < 2 / (Math.sqrt(Math.PI) * y + 2)) {
            // sample by C49 with n=2
            u = Math.sqrt(-Math.log(Util.Math.random() * Util.Math.random()));
        } else {
            // by WhitmerMath(tm)
            double tau = Util.Math.randomGaussian();
            // now complete rule C61
            u = Math.sqrt(-Math.log(Util.Math.random()) + 0.5 * tau * tau);
        }

        return u / beta;
    }

    public boolean acceptCosTheta(double vneutron, double vtarget, double ct) {
        return Util.Math.random() < Math.sqrt(vneutron * vneutron + vtarget * vtarget - 2 * vneutron * vtarget * ct) / (vneutron + vtarget);
    }

    @Override
    public void processEvent(Event event) {
        if (event.code == Event.Code.Scatter) {

            Vector3D particleVelocityIn;
            double particleSpeedIn;
            Vector3D neutronVelocityCMin;
            Vector3D neutronVelocityCMout;
            Vector3D velocityCM;

            double energyIn = this.energy;
            double vneutron = this.velocity.getNorm();
            Vector3D neutronVelocityIn = this.velocity;
            Vector3D neutronVelocityOut;
            double vtarget;

            do {
                vtarget = pickTargetSpeed(vneutron, event.nuclide);
                particleVelocityIn = Util.Math.randomDir().scalarMultiply(vtarget);
            } while (!acceptCosTheta(vneutron, vtarget, particleVelocityIn.normalize().dotProduct(neutronVelocityIn.normalize())));

            event.nuclideEnergyIn = event.nuclide.mass * vtarget * vtarget / 2;
            //establish center of mass
            //add velocity vectors / total mass 
            velocityCM = (this.velocity.scalarMultiply(Neutron.mass)
                    .add(particleVelocityIn.scalarMultiply(event.nuclide.mass)))
                    .scalarMultiply(1 / (Neutron.mass + event.nuclide.mass));

            //convert neutron and particle --> center of mass frame
            neutronVelocityCMin = this.velocity.subtract(velocityCM);
            //calculate elastic collision: entry speed = exit speed, random direction
            neutronVelocityCMout = this.getScatteredVelocity(event, neutronVelocityCMin);
            //convert neutron back into lab frame
            neutronVelocityOut = neutronVelocityCMout.add(velocityCM);

            // update neutron energy and direction
            this.setVelocity(neutronVelocityOut);

            event.cos_theta = neutronVelocityIn.normalize().dotProduct(this.direction);
            event.energyOut = this.energy;

            if (this.mcs != null && this.mcs.traceLevel >= 2) {
                this.mcs.scatter = true;
//                double neutronSpeed = velocityIn.getNorm();
                double speedCM = velocityCM.getNorm();
//                double speedNCM = neutronVelocityCM.getNorm();
//                double neutronSpeedCM = neutronVelocityCM.getNorm();
                double angle = Math.acos(neutronVelocityOut.normalize().dotProduct(neutronVelocityIn.normalize())) / Math.PI * 180;
                double angleWithX = Math.acos(neutronVelocityOut.dotProduct(Vector3D.PLUS_I.normalize())) / Math.PI * 180;
                synchronized (Neutron.class) {
                    System.out.println("Neutron: " + this.hashCode());
                    System.out.println(" Nuclide: " + event.nuclide.name);
                    System.out.println(" Nuclide energy: " + String.format("%6.3e eV", event.nuclideEnergyIn / Util.Physics.eV));
                    System.out.println(" Neutron energy in: " + String.format("%6.3e eV", energyIn / Util.Physics.eV));
                    System.out.println(" Speed of Neutron: " + String.format("%6.3e cm/s", vneutron));
                    System.out.println(" Speed of Particle: " + String.format("%6.3e cm/s", vtarget));
                    System.out.println(" Speed of CM: " + String.format("%6.3e cm/s", speedCM));
                    System.out.println(" Neutron energy out: " + String.format("%6.3e eV", this.energy / Util.Physics.eV));
                    System.out.println(" Scattering angle: " + angle);
                    System.out.println(" Scattering angle with x-axis: " + angleWithX);
                    System.out.println(" Velocity unit dir " + this.velocity.normalize());
                    //Util.Graphics.drawLine(this.mcs.visualizations, position, position.add(velocity.normalize().scalarMultiply(45)), 1, Color.BLACK);
                }
            }
        } else if (event.code == Event.Code.Capture) {
            // capture
            Environment.recordCapture();
        }
    }
}
