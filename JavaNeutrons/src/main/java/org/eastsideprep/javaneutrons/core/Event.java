package org.eastsideprep.javaneutrons.core;

import org.apache.commons.math3.geometry.euclidean.threed.*;

public class Event {

    public enum Code {
        Entry, Exit, Scatter, Capture, Gone, EmergencyDirectionChange, EmergencyExit
    };

    public Code code; // what kind of interesting thing happened here
    public Vector3D position;
    public Neutron neutron;

    // additional info - presence depends on event
    public Isotope element;
    public double energyOut;
    public double t; // how far along was this on the vector we took to get here
    public Part part;
    public int face;
    public Material exitMaterial;
    public double cos_theta;

//    public Event(double x, double y, double z, Event.Code c) {
//        this.position = new Vector3D(x, y, z);
//        this.code = c;
//    }
//
//    public Event(double x, double y, double z, Event.Code c, double t) {
//        this.position = new Vector3D(x, y, z);
//        this.code = c;
//        this.t = t;
//    }

    public Event(Vector3D position, Event.Code c) {
        this.position = new Vector3D(position.getX(), position.getY(), position.getZ());
        this.code = c;
    }

   public Event(Vector3D position, Event.Code c, double t) {
        this.position = new Vector3D(position.getX(), position.getY(), position.getZ());
        this.code = c;
        this.t = t;
    }
    public Event(Vector3D position, Event.Code c, double t, int face) {
        this.position = new Vector3D(position.getX(), position.getY(), position.getZ());
        this.code = c;
        this.t = t;
        this.face = face;
    }

    public Event(Vector3D position, Event.Code c, double t, Isotope e, Neutron n) {
        this.position = new Vector3D(position.getX(), position.getY(), position.getZ());
        this.code = c;
        this.t = t;
        this.element = e;
        this.neutron = n;
    }

    public Event(Vector3D position, Part p, double t) {
        this.position = new Vector3D(position.getX(), position.getY(), position.getZ());
        this.part = p;
        this.code = Event.Code.Entry;
        this.t = t;
    }

    @Override
    public String toString() {
        return "Event: " + this.hashCode() + ": " + this.code + ": "+this.position;
    }
}
