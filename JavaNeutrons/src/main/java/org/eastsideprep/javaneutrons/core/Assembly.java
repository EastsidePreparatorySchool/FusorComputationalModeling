package org.eastsideprep.javaneutrons.core;

import java.net.URL;
import java.util.ArrayList;
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
    // todo: acceleration structure

    private AssemblyGroup g;

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
    public Event evolveNeutronPath(Neutron n, LinkedTransferQueue visualizations, boolean outermost) {
        return this.evolveNeutronPathNoVacuum(n, visualizations, outermost);
    }

    public Event evolveNeutronPathNoVacuum(Neutron n, LinkedTransferQueue visualizations, boolean outermost) {
        Event partEvent;
        Event interactionEvent;
        Event event;
        double t;

        // we start in a certain medium. Todo: API for either figring that out or setting it
        Material medium = n.mcs.initialMaterial;

        do {
            // find the closest part we intersect with
            partEvent = this.rayIntersect(n.position, n.direction, false, visualizations);
            if (partEvent == null && !outermost) {
                // we found nothing and we are not in the outermost assembly,
                // return to the containing assembly
                return new Event(n.position, Event.Code.Exit);
            }
            // find possible interactions along the way
            interactionEvent = medium.nextPoint(n);

            // did we not find a part, or is it further than an air event?
            if (partEvent == null || partEvent.t > interactionEvent.t) {
                event = interactionEvent;
                if (event.position.getNorm() <= Environment.limit) {
                    // scattering / absorption in medium did really happen, process it
                    n.setPosition(visualizations, event.position);
                    medium.processEvent(event, true);
                    Util.Graphics.visualizeEvent(event, visualizations);
                }
            } else {
                // no interaction, we will just enter a new part
                Util.Graphics.visualizeEvent(partEvent, n.direction, visualizations);
                Part p = partEvent.part;
                n.setPosition(visualizations, partEvent.position);
                if (!n.record(partEvent)) {
                    // to many events, get out
                    return partEvent;
                }
                partEvent.neutron = n;
                medium.processEvent(partEvent, true);
                //System.out.println("Entering part " + p.name);
                event = p.evolveNeutronPath(n, visualizations, false);
                // coming out, we might be in a new material
                medium = event.exitMaterial != null ? event.exitMaterial : medium;
                //System.out.println("Exit to material: "+medium.name);
            }
            // if things happened far enough from the origin, call it gone
            if (event.position.getNorm() > Environment.limit) {
                t = Util.Math.raySphereIntersect(n.position, n.direction, Vector3D.ZERO, Environment.limit);
                event.position = n.position.add(n.direction.scalarMultiply(t));
                event.t = t;
                event.code = Event.Code.Gone;
                event.neutron = n;
                n.setPosition(visualizations, event.position);
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
        Part closestPart = null;
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
                Event entryEvent = p.rayIntersect(rayOrigin, rayDirection, false, vis);
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
            g.getChildren().add(a.getGroup());
        } else {
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

    ;
    
    
    public Set<Material> getMaterials() {
        return getParts().stream().map(p -> p.material).filter(m -> m != null).collect(Collectors.toSet());
    }

    public Set<Material> getContainedMaterials() {
        return getParts().stream().map(p -> p.shape.containedMaterial).filter(m -> m != null).collect(Collectors.toSet());
    }
}
