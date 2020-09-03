/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.shapes;

import org.eastsideprep.javaneutrons.core.Shape;

public class Cuboid extends Shape {

    // create with side lengths
    public Cuboid(float dx, float dy, float dz) {
        create(dx, dy, dz);
    }

    // can also make a cube
    public Cuboid(float s) {
        create(s, s, s);
    }

    // create with side lengths
    public Cuboid(double dx, double dy, double dz) {
        create((float) dx, (float) dy, (float) dz);
    }

    // can also make a cube
    public Cuboid(double s) {
        this(s, s, s);
    }

    // to learn more about triangle meshes, 
    // go to https://docs.oracle.com/javase/8/javafx/api/javafx/scene/shape/TriangleMesh.html
    // we only use vertices and faces here,
    // no normals or texcoords
    private void create(float dx, float dy, float dz) {
        dx /= 2;
        dy /= 2;
        dz /= 2;
        mesh.getPoints().addAll(
                // these are all the points in our mesh
                // one row per point
                // the faces array points to them
                -dx, -dy, dz,
                dx, -dy, dz,
                dx, dy, dz,
                -dx, dy, dz,
                -dx, -dy, -dz,
                dx, -dy, -dz,
                dx, dy, -dz,
                -dx, dy, -dz
        );

        // we don't use this, but we have to have one at least
        mesh.getTexCoords().addAll(0, 0);

        mesh.getFaces().addAll(
                // these are interleaved with texCoords (all 0)
                // face index 1, tex coord index 1, face index 2, tex coord index 2 etc.
                // one row = one face (triangle)
                // all index numbers are indexing the vertices array (index * 3)
                // top
                0, 0, 1, 0, 2, 0,
                0, 0, 2, 0, 3, 0,
                //front
                0, 0, 4, 0, 1, 0,
                4, 0, 5, 0, 1, 0,
                //right
                1, 0, 5, 0, 2, 0,
                5, 0, 6, 0, 2, 0,
                //back
                2, 0, 6, 0, 3, 0,
                6, 0, 7, 0, 3, 0,
                //left
                3, 0, 7, 0, 0, 0,
                7, 0, 4, 0, 0, 0,
                //bottom
                4, 0, 6, 0, 5, 0,
                4, 0, 7, 0, 6, 0
        );
    }

}
