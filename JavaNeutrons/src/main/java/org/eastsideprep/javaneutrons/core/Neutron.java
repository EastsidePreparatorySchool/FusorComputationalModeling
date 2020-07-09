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
    }

    public final void setVelocity(Vector3D velocity) {
        double speed = velocity.getNorm();
        this.direction = velocity.normalize();

        // should not have to do relativistic calculation since rest energy = 939MeV
        this.energy = speed * speed * Neutron.mass / 2;
        this.velocity = velocity;
    }

    @Override
    public void setDirectionAndEnergy(Vector3D direction, double energy) {
        this.direction = direction.normalize();
        this.energy = energy;
        this.velocity = this.direction.scalarMultiply(Math.sqrt(energy * 2 / Neutron.mass));
    }

    public Vector3D getScatteredVelocity(Event e, Vector3D neutronVelocity) {
        Isotope i = e.element;
        if (i.angles != null) {
            double neutronSpeed = neutronVelocity.getNorm();
            double energyEV = 0.5 * Neutron.mass * neutronSpeed * neutronSpeed / Util.Physics.eV;

            // get the scattering angle from a random lookup in the tables
            double cos_theta = i.getScatterCosTheta(energyEV);

            // construct vector and return
            Vector3D v = Util.Math.randomDir(cos_theta, neutronSpeed);
            Rotation r = new Rotation(Vector3D.PLUS_K, neutronVelocity);
            v = r.applyTo(v);

            e.cos_theta = cos_theta;

            //System.out.println("v: " + v);
            return v;

        } else {

            Vector3D v = Util.Math.randomDir().scalarMultiply(neutronVelocity.getNorm());
            double angleWithX = Math.acos(Vector3D.PLUS_I.dotProduct(v.normalize())) / Math.PI * 180;
            e.cos_theta = Math.cos(angleWithX);
            return v;
        }
    }

    @Override
    public void processEvent(Event event) {
        if (event.code == Event.Code.Scatter) {
            // other particle, velocity following Maxwell-Boltzmann speed distribution
            // derived through: <Ex> = 0.5*kB*T (x component takes a third of the kinetic energy)
            // => 0.5*m*<vx^2> = 0.5*kB*T
            // => m<vx^2> = kB*T
            // with <vx^2> = var(vx) because mean(vx) = 0
            // => m*var(vx) = kB*T
            // => var(vx) = kB*t/m
            // => sd(vx) = sd(vy) = sd(vz) = sqrt(kB*T/m)
            double particleSpeedComponentSD = Math.sqrt(Util.Physics.boltzmann * Util.Physics.roomTemp / event.element.mass);
            Vector3D particleVelocity = Util.Math.randomGaussianComponentVector(particleSpeedComponentSD);

            // making these for later debug out
            double particleSpeed = particleVelocity.getNorm();
            double particleEnergy = event.element.mass * particleSpeed * particleSpeed / 2;

            synchronized (Neutron.class) {
                Neutron.totalPE += particleEnergy;
                Neutron.countPE++;
                Neutron.totalNE += this.energy;
                Neutron.countNE++;
            }

            double neutronSpeed = this.velocity.getNorm();
            double neutronEnergyIn = this.energy;

            //establish center of mass
            //add velocity vectors / total mass 
            Vector3D velocityCM = (this.velocity.scalarMultiply(Neutron.mass)
                    .add(particleVelocity.scalarMultiply(event.element.mass)))
                    .scalarMultiply(1 / (Neutron.mass + event.element.mass));
            double speedCM = velocityCM.getNorm();

            //convert neutron and particle --> center of mass frame
            Vector3D velocityNCM = this.velocity.subtract(velocityCM);
            //calculate elastic collision: entry speed = exit speed, random direction
            //velocityNCM = Util.Math.randomDir().scalarMultiply(velocityNCM.getNorm());
            double speedNCM = velocityNCM.getNorm();
            velocityNCM = this.getScatteredVelocity(event, velocityNCM);
            //double neutronSpeedCM = velocityNCM.getNorm();
            //convert back into lab frame
            Vector3D velocityNLab = velocityNCM.add(velocityCM);

            // update myself (energy and direction)
            double angle = Math.acos(this.velocity.normalize().dotProduct(velocityNLab.normalize())) / Math.PI * 180;
            double angleWithX = Math.acos(Vector3D.PLUS_I.dotProduct(velocityNLab.normalize())) / Math.PI * 180;
            this.setVelocity(velocityNLab);
            event.energyOut = this.energy;

            synchronized (Neutron.class) {
                Neutron.totalNE2 += this.energy;
                Neutron.countNE2++;
            }

            if (this.mcs != null && this.mcs.traceLevel >= 2) {
                this.mcs.scatter = true;
                synchronized (Neutron.class) {
                    System.out.println("Neutron: " + this.hashCode());
                    System.out.println(" Particle: " + event.element.name);
                    System.out.println(" Particle energy: " + String.format("%6.3e eV", particleEnergy / Util.Physics.eV));
                    System.out.println(" Neutron energy in: " + String.format("%6.3e eV", neutronEnergyIn / Util.Physics.eV));
                    System.out.println(" Speed of Neutron: " + String.format("%6.3e cm/s", neutronSpeed));
                    System.out.println(" Speed of Particle: " + String.format("%6.3e cm/s", particleSpeed));
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
