package org.eastsideprep.javaneutrons.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Assembly extends Part {

    private AssemblyGroup g;
    public ArrayList<Part> parts = new ArrayList<>();

    public Assembly(String name) {
        super(name, null, null);
        this.g = new AssemblyGroup(this);
    }

    // use this constructor to construct an assembly from an OBJ file
    public Assembly(String name, URL url, Object material, String unit) {
        super(name, null, material);

        material = Material.getRealMaterial(material);

        this.g = new AssemblyGroup(this);

        ArrayList<Shape> shapes;

        if (url.toString().toLowerCase().endsWith("obj")) {
            shapes = Shape.loadOBJ(url, unit);
        } else if (url.toString().toLowerCase().endsWith("stl")) {
            shapes = Shape.loadSTL(url, unit);
        } else {
            throw new IllegalArgumentException("Assembly contructor: Not OBJ/STL file: " + url);
        }
        this.addAll(Part.NewPartsFromShapeList(name, shapes, (Material) material));
    }

    // use this constructor to construct an assembly from an OBJ file
    public Assembly(String name, URL url, Object material) {
        this(name, url, material, "cm");
    }

    public Group getGroup() {
        return g;
    }

    @Override
    public void resetDetectors() {
        for (Node n : g.getChildren()) {
            if (n instanceof AssemblyGroup) {
                ((AssemblyGroup) n).assembly.resetDetectors();
            } else if (n instanceof Shape) {
                ((Shape) n).part.resetDetectors();
            }
        }
    }

    @Override
    public Event evolveParticlePath(Particle p, LinkedTransferQueue visualizations, boolean outermost, Grid grid, Material initialMedium) {
        Event partEvent;
        Event interactionEvent;
        Event event = null;
        double t;
        boolean firstTime = true;

        // we start in a certain medium.
        Material medium = initialMedium;
        if (medium == null) {
            medium = p.mcs.initialMaterial;
        }
        do {
            if (event != null && event.code == Event.Code.ExitEntry) {
                // we have an exit/entry event from a prior iteration
                partEvent = event;
                partEvent.part = event.part2;
                partEvent.code = Event.Code.Entry;
                partEvent.position = event.position;
                partEvent.t = 0;
            } else {
                // find the closest part we intersect with
                if (grid != null) {
                    partEvent = grid.rayIntersect(p.position, p.direction, false, p.mcs.traceLevel >= 1 ? visualizations : null, Double.POSITIVE_INFINITY);
                } else {
                    partEvent = this.rayIntersect(p.position, p.direction, false, visualizations);
                }
            }
            if (partEvent == null && !outermost) {
                // we found nothing and we are not in the outermost assembly,
                // return to the containing assembly
                return new Event(p.position, Event.Code.Exit);
            }
            // find possible interactions along the way
            interactionEvent = p.nextPoint(medium);

            // did we not find a part, maybe we started inside a part?
            if (partEvent == null && outermost && firstTime) {
                // repeat search looking at triangle meshes from the inside
                if (grid != null) {
                    partEvent = grid.rayIntersect(p.position, p.direction, true, p.mcs.traceLevel >= 1 ? visualizations : null, Double.POSITIVE_INFINITY);
                } else {
                    partEvent = this.rayIntersect(p.position, p.direction, true, visualizations);
                }
                if (partEvent != null) {
                    // we are already inside the part. 
                    partEvent.t = 0;
                }
            }
            firstTime = false;

            // did we not find a part, or is it further than an air event?
            if (partEvent == null || partEvent.t > interactionEvent.t) {
                event = interactionEvent;
                if (event.position.getNorm() <= Environment.limit) {
                    // scattering / absorption in medium did really happen, process it
                    p.setPosition(visualizations, event.position);
                    medium.processEvent(event, true);
                    Util.Graphics.visualizeEvent(event, visualizations);
                }
            } else {
                // no interaction, we will just enter a new part
                Part part = partEvent.part;
                if (partEvent.t != 0) {
                    // we entered the part from the outside, visualize it
                    Util.Graphics.visualizeEvent(partEvent, p.direction, visualizations);
                    p.setPosition(visualizations, partEvent.position);
                }
                if (!p.record(partEvent)) {
                    // to many events, get out
                    return partEvent;
                }
                partEvent.particle = p;
                medium.processEvent(partEvent, true);
                //System.out.println("Entering part " + p.name);
                event = part.evolveParticlePath(p, visualizations, false, grid, null);
                // coming out, we might be in a new material
                medium = event.exitMaterial != null ? event.exitMaterial : medium;
                //System.out.println("Exit to material: "+medium.name);
            }
            // if things happened far enough from the origin, call it gone
            if (event.position.getNorm() > Environment.limit) {
                t = Util.Math.raySphereIntersect(p.position, p.direction, Vector3D.ZERO, Environment.limit);
                event.position = p.position.add(p.direction.scalarMultiply(t));
                event.t = t;
                event.code = Event.Code.Gone;
                event.particle = p;
                p.setPosition(visualizations, event.position);
                medium.processEvent(event, true);
                //n.setPosition(visualizations, event.position);
            }
            //visualizeEvent(event, visualizations);
        } while (event.code != Event.Code.Capture && event.code != Event.Code.Gone && event.code != Event.Code.EmergencyExit);
        return event;
    }

    //
    // rayIntersect
    //
    // intersect with all parts in assembly, return event with 
    // part or null
    // we need to ignore "going out" here, as we are not inside a simplepart
    //
    @Override
    Event rayIntersect(Vector3D rayOrigin, Vector3D rayDirection, boolean goingOut, LinkedTransferQueue vis) {
        double tmin = -1;
        Event closestEvent = null;
        Part p = null;

        for (Node node : this.g.getChildren()) {
            if (node instanceof Shape) {
                // link back to the Part that contains it
                p = ((Shape) node).part;
            } else if (node instanceof AssemblyGroup) {
                // link back to the Assembly that contains it
                p = ((AssemblyGroup) node).assembly;
            }
            if (p != null) {
                // intersect with that part, false means "going in"
                Event entryEvent = p.rayIntersect(rayOrigin, rayDirection, goingOut, vis);
                // event != null means we found a triangle
                if (entryEvent != null) {
                    if (tmin == -1 || entryEvent.t < tmin) {
                        tmin = entryEvent.t;
                        closestEvent = entryEvent;
                        closestEvent.part = p;
                    }
                }
            }
        }
        return closestEvent;

        //return (tmin == -1) ? null : new Event(rayOrigin.add(rayDirection.scalarMultiply(tmin)), closestPart, tmin);
    }

    public void add(Part part) {
        if (part instanceof Assembly) {
            Assembly a = (Assembly) part;
            this.addAll(a.parts);
            g.getChildren().add(a.getGroup());
        } else {
            this.parts.add(part);
            g.getChildren().add(part.shape);
        }
//        if (part.shape != null) {
//            System.out.println("Added part " + part.name + ", extent: " + part.shape.getExtent());
//        } 
    }

    public void addAll(Part... parts) {
        for (Part p : parts) {
            this.add(p);
        }
    }

    public void addAll(ArrayList<Part> parts) {
        for (Part p : parts) {
            this.add(p);
        }
    }

    public ObservableList<Transform> getTransforms() {
        return g.getTransforms();
    }

    //
    // vacuum face detection
    //
    //
    // marking parts containing vacuum
    //
    public boolean containsMaterialAt(Object material, Vector3D location) {
        // convert whatever we got here
        material = Material.getRealMaterial(material);

        // send out a random ray from location
        Vector3D direction = Util.Math.randomDir();
        Event e = this.rayIntersect(location, direction, false, null);
        if (e == null) {
            return false;
        }

        Part p = e.part;
        Shape s = p.shape;

        s.markSurfaceInContactWith(location, direction, (Material) material, null);
        return true;
    }

    public double getVolume() {
        double volume = 0;
        for (Node node : this.g.getChildren()) {
            if (node instanceof Shape) {
                // link back to the Part that contains it
                volume += ((Shape) node).getVolume();
            } else if (node instanceof AssemblyGroup) {
                // link back to the Assembly that contains it
                volume += ((AssemblyGroup) node).assembly.getVolume();
            }

        }
        return volume;
    }

    // recursive transform add
    public void addTransform(Transform t) {
        for (Node node : this.g.getChildren()) {
            if (node instanceof Shape) {
                // link back to the Part that contains it
                ((Shape) node).getTransforms().add(t);
            } else if (node instanceof AssemblyGroup) {
                // link back to the Assembly that contains it
                ((AssemblyGroup) node).assembly.addTransform(t);
            }

        }
    }

    // recursive transform insert
    public void addTransform(int i, Transform t) {
        for (Node node : this.g.getChildren()) {
            if (node instanceof Shape) {
                // link back to the Part that contains it
                ((Shape) node).getTransforms().add(i, t);
            } else if (node instanceof AssemblyGroup) {
                // link back to the Assembly that contains it
                ((AssemblyGroup) node).assembly.addTransform(i, t);
            }

        }
    }

    //  parts list
    public List<Part> getParts() {
        LinkedList<Part> parts = new LinkedList<>();
        for (Node node : this.g.getChildren()) {
            if (node instanceof Shape) {
                parts.add(((Shape) node).part);
            } else if (node instanceof AssemblyGroup) {
                // link back to the Assembly that contains it
                parts.addAll(((AssemblyGroup) node).assembly.getParts());
            }

        }
        return parts;
    }

    public Set<Material> getMaterials() {
        return getParts().stream().map(p -> p.material).filter(m -> m != null).collect(Collectors.toSet());
    }

    public Set<Material> getContainedMaterials() {
        return getParts().stream().map(p -> p.shape.containedMaterial).filter(m -> m != null).collect(Collectors.toSet());
    }

    public Set<Part> verifyPart(Part part) {

        HashSet<Part> intersectingParts = new HashSet<>();
        ArrayList<Vector3D> points = part.shape.getPoints();
        for (Part other : parts) {
            if (other != part) {
                for (Vector3D point : points) {
                    if (other.contains(point)) {
                        // repeat that test with another random ray
                        if (other.contains(point)) {
                            if (other.contains(point)) {
                                intersectingParts.add(other);
                            }
                        }
                    }
                }
            }
        }

        return intersectingParts;
    }

    public Set<String> verifyMeshIntegrity() {
        Set<String> intersectingPairs = new HashSet<>();
        for (Part p : this.parts) {
            Set<Part> intersectingParts = verifyPart(p);
            for (Part other : intersectingParts) {
                String conflict = "Parts of " + p.name + " are contained in " + other.name;
                intersectingPairs.add(conflict);
            }
        }
        return intersectingPairs;
    }
}
