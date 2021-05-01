package org.eastsideprep.javaneutrons.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import javafx.scene.Node;
import org.apache.commons.math3.geometry.euclidean.threed.*;

abstract public class Particle {

    public String type;
    public double energy; // unit: SI for cm
    public Vector3D direction; // no units
    public Vector3D position; // unit: (cm,cm,cm)
    public double entryEnergy = 0;
    public double totalPath = 0;
    public MonteCarloSimulation mcs;
    public Set<CorrelatedTallyOverEV> fluences;

    ArrayList<Event> history = new ArrayList<>();

    public Particle(Vector3D position, Vector3D direction, double energy, MonteCarloSimulation mcs) {
        this.position = position;
        this.direction = direction;
        this.energy = energy;
        this.mcs = mcs;
        this.fluences = new HashSet<>();
    }

    public void setPosition(LinkedTransferQueue<Node> q, Vector3D position) {
        Vector3D oldPosition = this.position;
        this.position = position;

        this.totalPath += position.subtract(oldPosition).getNorm();
        if (this.mcs.traceLevel >= 1) {
            Util.Graphics.drawLine(q, oldPosition, position, 0.1, this.energy);
        }
    }

    public void setPosition(Vector3D newPosition) {
        if (this.position != null) {
            this.totalPath += newPosition.subtract(this.position).getNorm();
        }
        this.position = newPosition;
    }

    public void setDirectionAndEnergy(Vector3D direction, double energy) {
        this.direction = direction.normalize();
        this.energy = energy;
    }

    public void processEvent(Event event) {
    }
    
    abstract Event nextPoint(Material m);

    //replace parameters with 1 Neutron object??
    public boolean record(Event e) {
        //System.out.println("Neutron"+this.hashCode()+" recording event "+e);
        history.add(e);
        if (history.size() > 3000) {
            dumpEvents("More than 3000 events for particle:");
            return false;
        }
        return true;
    }

    public void dumpEvents(String reason) {
        synchronized (Event.class) {
//            System.out.println("");
//            System.out.println(reason);
//            System.out.println("-- start of particle events:");
//            //history.stream().forEach(event -> System.out.println(event));
//            System.out.println("-- done");
//            System.out.println("");
        }
    }
    
    public void tally() {
        for (CorrelatedTallyOverEV h:fluences) {
            h.tally(this);
        }
    }
}
