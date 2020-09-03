/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;

/**
 *
 * @author subif
 */
public class GridBox {

    Vector c1; // less than or equal to c2  in every dimension (x y and z)
    Vector c2; // comparison vectors (making the box opposite cornered positively)
    Boolean dArrow = false; // is a directional arrow present in this grid box?
    Boolean fieldLine = false; // is a field line present in this grid box?
    Boolean flag = false; // This flag is set by FieldLineGenerator arbitraritly
    // if only every other box should start a streamline, only half of these wil
    // be flagged
    Face[] faces;

    GridBox(Vector p1, Vector p2) {
        c1 = new Vector(p1);
        c2 = new Vector(p2);

        if (p1.x.compareTo(p2.x)>0) {
            c2.x = p1.x;
            c1.x = p2.x;
        }
        if (p1.y.compareTo(p2.y)>0) {
            c2.y = p1.y;
            c1.y = p2.y;
        }
        if (p1.z.compareTo(p2.z)>0) {
            c2.z = p1.z;
            c1.z = p2.z;
        }

        if ((c2.x.equals(c1.x)) || (c2.y.equals(c1.y)) || (c2.z.equals(c1.z))) {
            Face face = new Face(c1, c2);
            faces = new Face[1];
            faces[0] = face;
        } else {
            Vector c3 = new Vector(c2.x, c1.y, c1.z);
            Face f1 = new Face(c3, c2);
            c3 = new Vector(c1.x, c2.y, c1.z);
            Face f2 = new Face(c3, c2);
            c3 = new Vector(c1.x, c1.y, c2.z);
            Face f3 = new Face(c3, c2);

            c3 = new Vector(c1.x, c2.y, c2.z);
            Face f4 = new Face(c3, c1);
            c3 = new Vector(c2.x, c1.y, c2.z);
            Face f5 = new Face(c3, c1);
            c3 = new Vector(c2.x, c2.y, c1.z);
            Face f6 = new Face(c3, c1);

            faces = new Face[6];
            faces[0] = f1;
            faces[1] = f2;
            faces[2] = f3;
            faces[3] = f4;
            faces[4] = f5;
            faces[5] = f6;
        }
    }

    public Vector findCenter() {
        return new Vector((c1.x + c2.x) / 2, (c1.y + c2.y) / 2, (c1.z + c2.z) / 2);
    }

    public Boolean arrowInBounds(Vector arrow) {
        if (((c2.x.compareTo(arrow.x)>=0) && (c1.x.compareTo(arrow.x) <=0))
                &&((c2.y.compareTo(arrow.y) >= 0) && (c1.y.compareTo(arrow.y) <= 0))
                && ((c2.z.compareTo(arrow.z)>=0) && (c1.z.compareTo(arrow.z)<=0))) {
            dArrow = true;
            fieldLine = true;
            return true;
        }
        return false;

    }

    public Boolean straightLineInBounds(Vector a, Vector b) {
        Boolean inBounds = false;
        for (Face face : faces) {
            Vector ib = face.intersectsLineSegment(a, b);
            if (ib != null) {
                fieldLine = true;
                inBounds = true;
                return inBounds;
            }
        }
        return inBounds;
    }

    public GridBox clone() {
        GridBox clone = new GridBox(c1, c2);
        clone.dArrow = this.dArrow;
        clone.faces = new Face[this.faces.length];
        for (int i = 0; i < this.faces.length; i++) {
            clone.faces[i] = this.faces[i].clone();
        }
        clone.fieldLine = (this.fieldLine);
        clone.flag = (this.flag);
        return clone;
    }

    @Override
    public String toString() {
        String box = "";
        box += "c1: " + c1;
        box += "\n c2: " + c2;
        box += "\n dArrow: " + dArrow;
        box += "\n fieldLine: " + fieldLine;
        box += "\n flag: " + flag;
        int i = 1;
        for(Face face : faces){
            box += "\n face " + i + " " +face;
        }
        return box;
    }

}
