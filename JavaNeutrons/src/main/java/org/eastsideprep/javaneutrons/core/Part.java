package org.eastsideprep.javaneutrons.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Part {

    public static HashMap<String, Part> namedParts = new HashMap<>();
    public Shape shape;
    public Material material;
    public String name;

    // universal detector functionality
    public EnergyHistogram entriesOverEnergy;
    public EnergyHistogram fluenceOverEnergy;
    public EnergyHistogram scattersOverEnergyBefore;
    public EnergyHistogram scattersOverEnergyAfter;
    public EnergyHistogram capturesOverEnergy;
    private double volume = 0;
    private double totalDepositedEnergy = 0;
    private double totalFluence = 0;
    private int totalEvents = 0;

    public Part(String name, Shape s, Object material) {
        if (s != null) {
            this.shape = new Shape(s);
        }
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
        this.totalDepositedEnergy = 0;
        this.totalFluence = 0;
        this.totalEvents = 0;
        this.entriesOverEnergy = new EnergyHistogram();
        this.fluenceOverEnergy = new EnergyHistogram();
        this.scattersOverEnergyBefore = new EnergyHistogram();
        this.capturesOverEnergy = new EnergyHistogram();
        this.scattersOverEnergyAfter = new EnergyHistogram();
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
        double epsilon = 1e-15; // 1 nm (in cm) 

        // entry into part - advance neutron ever so slightly
        // so that when something else happens, we will be firmly inside
        n.setPosition(visualizations, Util.Math.rayPoint(n.position, n.direction, epsilon));
        if (n.mcs.traceLevel >= 2) {
            System.out.println("Neutron " + n.hashCode() + " entry into part " + this.name);
            System.out.println(" Neutron energy in: " + String.format("%6.3e eV", n.energy / Util.Physics.eV));
        }
        this.processEntry(n);

        do {
            double currentEnergy = n.energy;

            exitEvent = this.rayIntersect(n.position, n.direction, true, visualizations);

            if (exitEvent == null) {
                //throw new IllegalArgumentException();
                exitEvent = new Event(n.position.add(n.direction.scalarMultiply(10)), Event.Code.EmergencyExit, 10, 0);
                Util.Graphics.visualizeEvent(exitEvent, n.direction, visualizations);
                if (n.mcs.traceLevel >= 2) {
                    n.dumpEvents("--no way out of part, emergency exit, dumping events" + this.name);
                }
                event = exitEvent;
            } else {
                // this next line will figure out where to scatter/absorb
                interactionEvent = material.nextPoint(n);
                if (exitEvent.t > interactionEvent.t) {
                    // scattering / absorption did really happen, process it
                    event = interactionEvent;
                    Util.Graphics.visualizeEvent(event, visualizations);
                    n.setPosition(visualizations, event.position);
                    this.processEvent(event);
                } else {
                    // we exit the part before stuff happens
                    event = exitEvent;
                    n.setPosition(visualizations, event.position);
                    Util.Graphics.visualizeEvent(event, n.direction, visualizations);
                    this.processExit(n);
                    event.neutron = n;
                    event.exitMaterial = this.shape.getContactMaterial(event.face);
                }
                // call for Detector parts to record
                this.material.processEvent(event, false);
                this.processPathLength(event.t, n);
            }

            // also record event for the individual neutron
            if (!n.record(event)) {
                // too many events, get out
                return event;
            }
        } while (event.code != Event.Code.Exit && event.code != Event.Code.Capture);

        return event;
    }

    //
    // detector functionality
    //
    void processPathLength(double length, Neutron n) {
        this.fluenceOverEnergy.record(length / volume, n.energy);
        synchronized (this) {
            this.totalFluence += length / volume;
        }
//        if (name.equals("Detector opposite block")) {
//            System.out.println("Entry into detector path length log: " + length / volume
//                    + ", new total: " + this.totalFluence
//            );
//        }
    }

    void processEntry(Neutron n) {
        this.entriesOverEnergy.record(1, n.energy);
        n.entryEnergy = n.energy;

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
        if (event.code == Event.Code.Scatter || event.code == Event.Code.Capture) {
            synchronized (this) {
                this.totalEvents++;
            }
        }

        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyAfter.record(1, event.neutron.energy);
        }
    }

    synchronized void processExit(Neutron n) {
        this.totalDepositedEnergy += (n.entryEnergy - n.energy);
    }

    public double getTotalDepositedEnergy() {
        return this.totalDepositedEnergy;
    }

    public double getTotalFluence() {
        return this.totalFluence;
    }

    public double getTotalPath() {
        return this.totalFluence * this.volume;
    }

    public int getTotalEvents() {
        return this.totalEvents;
    }

    public ObservableList<Transform> getTransforms() {
        //System.out.println("part "+this+" Tx "+this.shape.getTransforms());
        return this.shape.getTransforms();
    }

    public void addTransform(Transform t) {
        this.shape.getTransforms().add(t);
    }

    public void addTransform(int i, Transform t) {
        this.shape.getTransforms().add(i, t);
    }

    public void setColor(String color) {
        this.shape.setColor(color);
    }

    public Translate settleAgainst(Part other, Vector3D f) {
    
        return shape.settleAgainst(other.shape, f);
    }

    // what is the distance from our vertices to the other thing, 
    // and vice-versa?
    public double distance(Part other, Vector3D direction) {
        return shape.distance(other.shape, direction);
    }

}
