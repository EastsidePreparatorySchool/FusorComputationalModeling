/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.Event;
import org.eastsideprep.javaneutrons.core.LogEnergyEVHistogram;
import org.eastsideprep.javaneutrons.core.Neutron;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.shapes.Shape;

/**
 *
 * @author gunnar
 */
public class Part {

    public static HashMap<String, Part> namedParts = new HashMap<>();
    public Shape shape;
    public Material material;
    public String name;

    // universal detector functionality
    public LogEnergyEVHistogram entriesOverEnergy;
    public LogEnergyEVHistogram fluenceOverEnergy;
    public LogEnergyEVHistogram scattersOverEnergyBefore;
    public LogEnergyEVHistogram scattersOverEnergyAfter;
    public LogEnergyEVHistogram capturesOverEnergy;
    private double volume = 0;
    private double currentEntryEnergy = 0;
    private double totalDepositedEnergy = 0;
    private double totalFluence = 0;
    private int totalEvents = 0;

    public Part(String name, Shape s, Object material) {
        this.shape = s;
        this.name = name;
        if (this.shape != null) {
            this.shape.part = this;
            namedParts.put(name, this);
        }
        if (material != null) {
            String mName = material.toString();
            this.material = Material.getRealMaterial(material);
            if (this.material == null) {
                throw new IllegalArgumentException("invalid material for part: " + mName);
            }
        }
        if (this.shape != null) {
            this.volume = this.shape.getVolume();
        }
        resetDetector();
    }

    public static Part getByName(String name) {
        return namedParts.get(name);
    }

    public void resetDetectors() {
        resetDetector();
    }

    public final void resetDetector() {
        this.currentEntryEnergy = 0;
        this.totalDepositedEnergy = 0;
        this.totalFluence = 0;
        this.totalEvents = 0;
        this.entriesOverEnergy = new LogEnergyEVHistogram();
        this.fluenceOverEnergy = new LogEnergyEVHistogram();
        this.scattersOverEnergyBefore = new LogEnergyEVHistogram();
        this.capturesOverEnergy = new LogEnergyEVHistogram();
        this.scattersOverEnergyAfter = new LogEnergyEVHistogram();
    }

    public static ArrayList<Part> NewPartsFromShapeList(String name, List<Shape> shapes, Material material) {
        ArrayList<Part> parts = new ArrayList<>();
        int i = 0;
        for (Shape s : shapes) {
            Part p = new Part(name + "." + (s.name != null ? s.name : String.format("%03d", i)), s, material);
            p.shape.setDrawMode(DrawMode.LINE);
            p.shape.setOpacity(0.5);
            p.shape.setColor("blue");
            parts.add(p);
            i++;
        }
        return parts;
    }

    //
    // rayIntersect
    //
    // intersects a ray with all triangles of this parts, returns the t-param of the closest
    // goingOut determines whether we are entering or leaving the part (true=test back faces)
    //
    Event rayIntersect(Vector3D rayOrigin, Vector3D rayDirection, boolean goingOut, LinkedTransferQueue<Node> g) {
        int[] face = new int[1];
        double t = shape.rayIntersect(rayOrigin, rayDirection, goingOut, face, g);
        // not found?
        if (t == -1) {
            return null;
        }
        // construct appropriate event
        return new Event(Util.Math.rayPoint(rayOrigin, rayDirection, t),
                goingOut ? Event.Code.Exit : Event.Code.Entry,
                t,
                face[0]);
    }

    //
    // evolveNeutronPathNoVacuum
    // 
    // follows the neutron around from entry to exit or absorption
    // outermost will be ignored, this is not an assembly
    // 
    Event evolveNeutronPath(Neutron n, LinkedTransferQueue<Node> visualizations, boolean outermost) {
        double t;
        Event exitEvent;
        Event interactionEvent;
        Event event;
        double epsilon = 1e-7; // 1 nm (in cm) 

        // entry into part - advance neutron ever so slightly
        // so that when something else happens, we will be firmly inside
        n.setPosition(visualizations, Util.Math.rayPoint(n.position, n.direction, epsilon));

        this.processEntryEnergy(n.energy);

        do {
            double currentEnergy = n.energy;

            exitEvent = this.rayIntersect(n.position, n.direction, true, visualizations);

            if (exitEvent == null) {

                //throw new IllegalArgumentException();
                exitEvent = new Event(n.position.add(n.direction.scalarMultiply(10)), Event.Code.EmergencyExit, 10, 0);
                n.record(exitEvent);
                Util.Graphics.visualizeEvent(exitEvent, n.direction, visualizations);
                if (n.trace) {
                    System.out.println("");
                    System.out.println("--no way out of part, emergency exit, dumping events" + this.name);
                    n.dumpEvents();
                    System.out.println("--end dump");
                }
                return exitEvent;
            }

            // this next line will figure out where to scatter/absorb
            interactionEvent = material.nextPoint(n);
            if (exitEvent.t > interactionEvent.t) {
                // scattering / absorption did really happen, process it
                event = interactionEvent;
                Util.Graphics.visualizeEvent(event, visualizations);
                n.setPosition(visualizations, event.position);
                this.processEvent(event);
            } else {
                event = exitEvent;
                n.setPosition(visualizations, event.position);
                Util.Graphics.visualizeEvent(event, n.direction, visualizations);
                this.processExitEnergy(n.energy);
                event.exitMaterial = this.shape.getContactMaterial(event.face);
            }
            // call for Detector parts to record
            this.processPathLength(event.t, currentEnergy);

            // also record event for the individual neutron
            n.record(event);
        } while (event.code != Event.Code.Exit && event.code != Event.Code.Capture);

        return event;
    }

    //
    // detector functionality
    //
    synchronized void processPathLength(double length, double energy) {
//        if (name.equals("Body")) {
//            System.out.println("Entry into detector path length log " + this.fluenceOverEnergy.hashCode());
//        }
        this.fluenceOverEnergy.record(length / volume, energy);
        this.totalFluence += length / volume;
    }

    synchronized void processEntryEnergy(double e) {
//        if (name.equals("Body")) {
//            //System.out.println("Entry into detector entry energy log " + this.entryOverEnergy.hashCode());
//        }
        this.entriesOverEnergy.record(1, e);
        this.currentEntryEnergy = e;
    }

    public void processEvent(Event event) {
        // record stats for part
        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyBefore.record(1, event.neutron.energy);
        } else {
            this.capturesOverEnergy.record(1, event.neutron.energy);

        }

        // let the neutron do its thing
        event.neutron.processEvent(event);

        // record more stats for part
        synchronized (this) {
            this.totalEvents++;
        }
        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyAfter.record(1, event.neutron.energy);
        }
    }

    synchronized void processExitEnergy(double e) {

        this.totalDepositedEnergy += (e - this.currentEntryEnergy);
    }

    public double getTotalDepositedEnergy() {
        return this.totalDepositedEnergy;
    }

    public double getTotalFluence() {
        return this.totalFluence;
    }

    public int getTotalEvents() {
        return this.totalEvents;
    }
}
