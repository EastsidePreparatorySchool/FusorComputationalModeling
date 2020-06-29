/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;
import javafx.scene.Node;
import org.apache.commons.math3.geometry.euclidean.threed.*;

/**
 *
 * @author gunnar
 */
public class Neutron {

    final public static double mass = 1.67492749804e-27; // SI
    final public static double startingEnergyDD = 3.925333e-13 * 1e4; //SI for cm
    // factor 1e4 is from using cm, not m here - 100^2

    public double energy; // unit: SI for cm
    public Vector3D direction; // no units
    public Vector3D position; // unit: (cm,cm,cm)
    public Vector3D velocity; // kept in parallel with energy and direction, see set() methods
    public boolean trace = false;

    ArrayList<Event> history = new ArrayList<>();

    Neutron(Vector3D position, Vector3D direction, double energy, boolean trace) {
        setPosition(position);
        setDirectionAndEnergy(direction, energy);
        this.trace = trace;
    }

    public final void setPosition(LinkedTransferQueue<Node> q, Vector3D position) {
        Vector3D oldPosition = this.position;
        this.position = position;

        if (this.trace) {
            Util.Graphics.drawLine(q, oldPosition, position, 0.1, this.energy);
        }
    }

    public final void setPosition(Vector3D position) {
        this.position = position;
    }

    public final void setVelocity(Vector3D velocity) {
        double speed = velocity.getNorm();
        this.direction = velocity.normalize();

        // should not have to do relativistic calculation since rest energy = 939MeV
        this.energy = speed * speed * Neutron.mass / 2;
        this.velocity = velocity;
    }

    public final void setDirectionAndEnergy(Vector3D direction, double energy) {
        this.direction = direction.normalize();
        this.energy = energy;
        this.velocity = this.direction.scalarMultiply(Math.sqrt(energy * 2 / Neutron.mass));
    }

    public final void randomizeDirection() {
        this.direction = Util.Math.randomDir();
        this.velocity = this.direction.scalarMultiply(Math.sqrt(energy * 2 / Neutron.mass));
    }

    public void processEvent(Event event) {
        if (event.code == Event.Code.Scatter) {
            // other particle, velocity following Maxwell-Boltzmann speed distribution
            // derived through: <Ex> = 0.5*kB*T (x component takes a third of the kinetic energy)
            // => 0.5*m*<vx^2> = 0.5*kB*T
            // with <vx^2> = var(vx)
            // => m*sd(vx) = sqrt(kB*T)
            // => sd(vx) = sd(vy) = sd(vz) = sqrt(kB*T/m)
            double particleSpeedComponentSD = Math.sqrt(Util.Physics.boltzmann * Util.Physics.roomTemp / event.element.mass);
            Vector3D particleVelocity = Util.Math.randomGaussianComponentVector(particleSpeedComponentSD);

            // making these for later debug out
            double particleSpeed = particleVelocity.getNorm();
            double particleEnergy = event.element.mass * particleSpeed * particleSpeed / 2;
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
            velocityNCM = Util.Math.randomDir().scalarMultiply(velocityNCM.getNorm());
            double neutronSpeedCM = velocityNCM.getNorm();
            //convert back into lab frame
            Vector3D velocityNLab = velocityNCM.add(velocityCM);

            // update myself (energy and direction)
            this.setVelocity(velocityNLab);
            event.energyOut = this.energy;
            if (this.trace) {
                synchronized (Neutron.class) {
                    System.out.println("Particle: " + event.element.name);
                    System.out.println("Particle energy: " + String.format("%6.3e eV", particleEnergy / Util.Physics.eV));
                    System.out.println("Neutron energy in: " + String.format("%6.3e eV", neutronEnergyIn / Util.Physics.eV));
                    System.out.println("Speed of Neutron: " + String.format("%6.3e cm/s", neutronSpeed));
                    System.out.println("Speed of Particle: " + String.format("%6.3e cm/s", particleSpeed));
                    System.out.println("Speed of CM: " + String.format("%6.3e cm/s", speedCM));
                    System.out.println("Neutron energy out: " + String.format("%6.3e eV", this.energy / Util.Physics.eV));
                    System.out.println("");
                }
            }
        } else {
            // capture
            Environment.recordCapture();
        }
    }

    //replace parameters with 1 Neutron object??
    public void record(Event e) {
        //System.out.println("Neutron"+this.hashCode()+" recording event "+e);
        if (this.trace) {
            history.add(e);
        }
    }

    public void dumpEvents() {
        if (this.trace) {
            synchronized (Event.class) {
                history.stream().forEach(event -> System.out.println(event));
            }
        }
    }
}
