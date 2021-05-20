package org.eastsideprep.javaneutrons.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Part {

    public Shape shape;
    public Material material;
    public String name;

    // universal detector functionality
    public TallyOverEV entriesOverEnergy;
    public TallyOverEV exitsOverEnergy;
    public Map<String, CorrelatedTallyOverEV> fluenceMap;
    public TallyOverEV scattersOverEnergyBefore;
    public TallyOverEV scattersOverEnergyAfter;
    public TallyOverEV capturesOverEnergy;
    public Tally angles;
    private double volume = 0;
    private double totalDepositedEnergy = 0;
    private AtomicLong totalEvents;
    final private static Integer tableCreationLock = 0;
    private static Util.LogLogTable neutronConversionTable = null;
    private static Util.LogLogTable photonConversionTable = null;

    public Part(String name, Shape s, Object material, String color) {
        this(name, s, material);
        this.setColor(color);
    }
 
    
    public Part(String name, Shape s, Object material) {
        if (s != null) {
            this.shape = new Shape(s);
        }
        this.name = name;
        if (this.shape != null) {
            this.shape.part = this;
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

    public void resetDetectors() {
        resetDetector();
    }

    public final void resetDetector() {
        this.totalDepositedEnergy = 0;
        this.totalEvents = new AtomicLong(0);
        this.exitsOverEnergy = new TallyOverEV(1e7,100);
        this.entriesOverEnergy = new TallyOverEV(1e7,100);

        synchronized (Part.tableCreationLock) {
            if (Part.neutronConversionTable == null) {
                Part.neutronConversionTable = new Util.LogLogTable("/sieverts/neutron_conversion.csv");
                Part.photonConversionTable = new Util.LogLogTable("/sieverts/photon_conversion.csv");
            }
        }

        CorrelatedTallyOverEV neutronFluence = new CorrelatedTallyOverEV(5e6,100, Part.neutronConversionTable);
        CorrelatedTallyOverEV gammaFluence = new CorrelatedTallyOverEV(12e6, 100, Part.photonConversionTable);
        this.fluenceMap = new HashMap<>();
        this.fluenceMap.put("neutron", neutronFluence);
        this.fluenceMap.put("gamma", gammaFluence);

        this.scattersOverEnergyBefore = new TallyOverEV(1e7,100);
        this.capturesOverEnergy = new TallyOverEV(1e7,100);
        this.scattersOverEnergyAfter = new TallyOverEV(1e7,100);
        this.angles = new Tally(-1, 1, 100, false);
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
    Event evolveParticlePath(Particle p, LinkedTransferQueue<Node> visualizations, boolean outermost, Grid grid, Material initialMedium) {
        double t;
        Event exitEvent;
        Event interactionEvent;
        Event event = null;
        double epsilon = 1e-10; // 10^-12m (in cm) 

        // entry into part - advance neutron ever so slightly
        // so that when something else happens, we will be firmly inside
//        p.setPosition(visualizations, Util.Math.rayPoint(p.position, p.direction, epsilon));
        if (p.mcs.traceLevel >= 2) {
            System.out.println(p.getClass().getSimpleName() + " " + p.hashCode() + " entry into part " + this.name);
            System.out.println(" " + p.getClass().getSimpleName() + " energy in: " + String.format("%6.3e eV", p.energy / Util.Physics.eV));
        }
        this.processEntry(p);

        do {
            double currentEnergy = p.energy;
            // this next line will figure out where to scatter/absorb
            interactionEvent = p.nextPoint(material);

            if (grid != null) {
                exitEvent = grid.rayIntersect(p.position, p.direction, true, p.mcs.traceLevel >= 1 ? visualizations : null, interactionEvent.t);
            } else {
                exitEvent = this.rayIntersect(p.position, p.direction, true, visualizations);
            }

            if (exitEvent == null) {
                //throw new IllegalArgumentException();
                // repeat for debugging
                exitEvent = grid.rayIntersect(p.position, p.direction, true, p.mcs.traceLevel >= 1 ? visualizations : null, interactionEvent.t);
                if (exitEvent == null) {
                    exitEvent = new Event(p.position.add(p.direction.scalarMultiply(10)), Event.Code.EmergencyExit, 10, 0);
                    Util.Graphics.visualizeEvent(exitEvent, p.direction, visualizations);
                    if (p.mcs.traceLevel >= 2) {
                        p.dumpEvents("--no way out of part, emergency exit, dumping events" + this.name);
                    }
                    event = exitEvent;
                }
            }

            if (exitEvent != null) {
                if (exitEvent.t > interactionEvent.t) {
                    // scattering / absorption did really happen, process it
                    event = interactionEvent;
                    Util.Graphics.visualizeEvent(event, visualizations);
                    p.setPosition(visualizations, event.position);
                    this.processEvent(event);
                } else {
                    // we exit the part before stuff happens
                    event = exitEvent;
                    p.setPosition(visualizations, event.position);
                    Util.Graphics.visualizeEvent(event, p.direction, visualizations);
                    this.processExit(p);
                    event.particle = p;
                    event.exitMaterial = this.shape.getContactMaterial(event.face);
                }
            }
            // call for Detector parts to record
            this.material.processEvent(event, false);
            this.processPathLength(p, event.t, currentEnergy);

            // also record event for the individual neutron
            if (!p.record(event)) {
                // too many events, get out
                return event;
            }
        } while (event.code != Event.Code.Exit && event.code != Event.Code.ExitEntry && event.code != Event.Code.Capture);
        if (event.code == Event.Code.Capture) {
            if (p.mcs.traceLevel >= 2) {
                System.out.println(p.getClass().getSimpleName() + " " + p.hashCode() + " captured in part " + this.name);
                System.out.println(" " + p.getClass().getSimpleName() + " energy final: " + String.format("%6.3e eV", p.energy / Util.Physics.eV));
            }
            event.exitMaterial = this.material;
        } else {
//            // advance the neutron a bit to the outside
//            p.setPosition(visualizations, Util.Math.rayPoint(p.position, p.direction, epsilon));
            if (p.mcs.traceLevel >= 2) {
                System.out.println(p.getClass().getSimpleName() + " " + p.hashCode() + " exit from part " + this.name);
                System.out.println(" " + p.getClass().getSimpleName() + " energy out: " + String.format("%6.3e eV", p.energy / Util.Physics.eV));
            }
        }

        return event;
    }

    //
    // detector functionality
    //
    void processPathLength(Particle particle, double length, double energy) {
        this.fluenceMap.get(particle.type).record(particle, length / volume, energy);
    }

    void processEntry(Particle p
    ) {
        this.entriesOverEnergy.record(1, p.energy);
        p.entryEnergy = p.energy;

    }

    public void processEvent(Event event) {
        // record stats for part
        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyBefore.record(1, event.particle.energy);
        } else {
            this.capturesOverEnergy.record(1, event.particle.energy);
        }

        // let the neutron do its thing
        event.particle.processEvent(event);

        // record more stats for part
        if (event.code == Event.Code.Scatter || event.code == Event.Code.Capture) {
            this.totalEvents.incrementAndGet();
        }

        if (event.code == Event.Code.Scatter) {
            this.scattersOverEnergyAfter.record(1, event.particle.energy);
            this.angles.record(1, event.cos_theta);
        }
    }

    void processExit(Particle p) {
        this.exitsOverEnergy.record(1, p.energy);
        synchronized (this) {
            this.totalDepositedEnergy += (p.entryEnergy - p.energy);
        }
    }

    public double getTotalDepositedEnergy() {
        return this.totalDepositedEnergy;
    }

    public double getTotalFluence(String kind) {
        return fluenceMap.get(kind).getTotal();
    }

    public double getTotalPath(String kind) {
        return this.getTotalFluence(kind) * this.volume;
    }

    public long getTotalEvents() {
        return this.totalEvents.get();
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

    public Part setColor(String color) {
        this.shape.setColor(color);
        return this;
    }

    public Translate settleAgainst(Part other, Vector3D f) {

        return shape.settleAgainst(other.shape, f);
    }

    // what is the distance from our vertices to the other thing, 
    // and vice-versa?
    public double distance(Part other, Vector3D direction) {
        return shape.distance(other.shape, direction);
    }

    public boolean contains(Vector3D point) {
        //System.out.println("Does part "+this+" contain point "+point + "?");
        return shape.contains(point);
    }

    @Override
    public String toString() {
        return "Part '" + this.name + "' (" + (this.material != null ? this.material.name : "unknown material") + ")";
    }

    //
    // exposure measurements in sieverts
    //
    public double getSieverts(String type) {
        return this.fluenceMap.get(type).totalSievert.doubleValue();
    }
    
    public Part translate(double x,double y,double z) {
        this.getTransforms().add(0, new Translate(x, y, z));
        return this;
    }
    
   public Part rotate(double angle, double x,double y,double z) {
        this.getTransforms().add(0, new Rotate(angle, new Point3D(x, y, z)));
        return this;
    }                 

}
