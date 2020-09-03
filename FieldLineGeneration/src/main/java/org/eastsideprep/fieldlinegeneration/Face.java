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
public class Face {

    Vector equation;
    Double answer;
    Vector c1;
    Vector c2;

    /**
     * Making a square from opposite points
     *
     * @param p1
     * @param p2 must be opposite from p1 and on the same plane
     */
    Face(Vector p1, Vector p2) {
        Vector p3;
        if (p1.x.equals(p2.x)) {
            p3 = new Vector(p1.x, p1.y + p2.y, p1.z + p2.z);
        } else if (p1.y.equals(p2.y)) {
            p3 = new Vector(p1.x + p2.x, p1.y, p1.z + p2.z);
        } else {
            p3 = new Vector(p1.x + p2.x, p1.y + p2.y, p1.z);
        }
        Vector p12 = p1.thisToThat(p2);
        Vector p13 = p1.thisToThat(p3);
        this.equation = p12.crossProduct(p13);
        this.answer = (equation.x * p1.x) + (equation.y * p1.y) + (equation.z * p1.z);
        c1 = new Vector(p1);
        c2 = new Vector(p2);
        if (p1.x.compareTo(p2.x) > 0) {
            c2.x = p1.x;
            c1.x = p2.x;
        }
        if (p1.y.compareTo(p2.y) > 0) {
            c2.y = p1.y;
            c1.y = p2.y;
        }
        if (p1.z.compareTo(p2.z) > 0) {
            c2.z = p1.z;
            c1.z = p2.z;
        }

    }

    Vector intersectsLine(Vector a, Vector b) {
        Vector lineEquation = a.thisToThat(b);
        Vector v = equation.product(b);
        Vector u = equation.product(lineEquation);
        Double t = u.x + u.y + u.z;
        Double v2 = v.x + v.y + v.z;
        v2 = answer - v2;
        // mostly checking if hitting this plane
        if (v2 == 0) {
            Vector intersection = lineEquation.scale(t);
            intersection.plusEquals(b);
            // checks if it hits this box
            if (((c2.x.compareTo(intersection.x) >= 0) && (c1.x.compareTo(intersection.x)) <= 0)
                    && ((c2.y.compareTo(intersection.y) >= 0) && (c1.y.compareTo(intersection.y) <= 0))
                    && ((c2.z.compareTo(intersection.z) >= 0) && (c1.z.compareTo(intersection.z)) <= 0)) {
                return intersection;

            }
            return null;
        }

        if (t == 0) {
            return null;
        }
        t = v2 / t;
        Vector intersection = new Vector(lineEquation.scale(t));
        intersection.plusEquals(b);

        // checks if it hits this box
        if (((c2.x.compareTo(intersection.x) >= 0) && (c1.x.compareTo(intersection.x)) <= 0)
                && ((c2.y.compareTo(intersection.y) >= 0) && (c1.y.compareTo(intersection.y) <= 0))
                && ((c2.z.compareTo(intersection.z) >= 0) && (c1.z.compareTo(intersection.z)) <= 0)) {
            return intersection;

        }
        return null;

    }

    /**
     * Check if a line segment intersects with this face
     *
     * @param a
     * @param b
     * @return
     */
    public Vector intersectsLineSegment(Vector a, Vector b) {
        Vector i = intersectsLine(a, b);
        if (i == null) {
            return null;
        }
        // check if the intersection point is on the line segment specified
        if ((((a.x.compareTo(i.x)>=0) && (i.x.compareTo(b.x))>=0) || ((b.x.compareTo(i.x)>=0) && (i.x.compareTo(a.x)>=0)))
                && (((a.y.compareTo(i.y)>=0) && (i.y.compareTo(b.y)>=0)) || ((b.y.compareTo(i.y)>=0) && (i.y.compareTo(a.y)>=0)))
                && (((a.z.compareTo(i.z)>=0) && (i.z.compareTo(b.z)>=0)) || ((b.z.compareTo(i.z)>=0) && (i.z.compareTo(a.z)>=0)))) {
            return i;
        }
        return null;

    }

    public Face clone() {
        Face clone = new Face(c1, c2);

        return clone;

    }

    @Override
    public String toString() {
        String face = "";

        face += "Equation: " + equation;
        face += "answer: " + answer;
        face += "c1: " + c1;
        face += "c2: " + c2;
        return face;
    }

}
