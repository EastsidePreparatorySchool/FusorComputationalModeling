/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.efieldgeneration;

/**
 *
 * @author subif
 */
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import javafx.scene.shape.TriangleMesh;

public class Geometry {

    public Triangle[] positiveTriangles;
    public Triangle[] negativeTriangles;
    public Triangle[] triangles;
    TriangleMesh positiveMesh;
    TriangleMesh negativeMesh;
    Double[] positiveSumSA;
    Double[] negativeSumSA;
    Double[] triangleSumSA;
    public Double positiveSA;
    public Double negativeSA;
    public Double totalSA;

    public Geometry(String positiveStl, Double positiveCharge, String negativeStl, Double negativeCharge) {
        positiveMesh = importObject(positiveStl);
        negativeMesh = importObject(negativeStl);
        getTriangles();
        triangleSumSA = new Double[triangles.length];
        positiveSumSA = new Double[positiveTriangles.length];
        negativeSumSA = new Double[negativeTriangles.length];
    }

    public TriangleMesh importObject(String fileName) {
        Path path = Paths.get(fileName);
        StlMeshImporter meshImporter = new StlMeshImporter();
        File file = path.toFile();
        meshImporter.read(file);
        TriangleMesh mesh = meshImporter.getImport();
        meshImporter.close();
        return mesh;
    }

    public void getTriangles() {

        // .getfaces/.getPoints/whatever all return Observable(Object)Arrays
        // to convert them into normal arrays
        // you pass them an array
        // then they return an array for you to use
        float[] positivePoints = null;
        positivePoints = positiveMesh.getPoints().toArray(positivePoints);
        float[] negativePoints = null;
        negativePoints = negativeMesh.getPoints().toArray(negativePoints);
        int[] positiveFaceIndeces = null;
        int[] negativeFaceIndeces = null;

        positiveFaceIndeces = positiveMesh.getFaces().toArray(positiveFaceIndeces);
        System.out.println("Points mod 3: " + positivePoints.length % 3);
        System.out.println("Faces mod 6: " + positiveFaceIndeces.length % 6);
        System.out.println("Triangle Mesh Vertex Format: " + positiveMesh.getVertexFormat());
        System.out.println("point size: " + positivePoints.length);
        negativeFaceIndeces = negativeMesh.getFaces().toArray(negativeFaceIndeces);
        System.out.println("Points mod 3: " + negativePoints.length % 3);
        System.out.println("Faces mod 6: " + negativeFaceIndeces.length % 6);
        System.out.println("Triangle Mesh Vertex Format: " + negativeMesh.getVertexFormat());
        System.out.println("point size: " + negativePoints.length);
        triangles = new Triangle[(positiveFaceIndeces.length + negativeFaceIndeces.length) / 6];
        positiveTriangles = new Triangle[(positiveFaceIndeces.length) / 6];
        negativeTriangles = new Triangle[(negativeFaceIndeces.length) / 6];

        /*
        The Triangle Mesh Vertex Format is POINT_TEXCOORD in this case
        .getFaces returns an array of indeces into arrays
        .getPoints returns an array of floats, each one representing the x, y, or z of a point
        the array looks like this:
        [x1, y1, z1, x2, y2, z2,...]
        the .getFaces, however, is completely different
        what the indexes of .getFaces returns depends on the Vertex Format of your triangle mesh
        
        For example, the faces with VertexFormat.POINT_TEXCOORD that represent a single textured rectangle, using 2 triangles, have the following data order: [ 
        p0, t0, p1, t1, p3, t3, // First triangle of a textured rectangle 
        p1, t1, p2, t2, p3, t3 // Second triangle of a textured rectangle 
        ]
        where p0, p1, p2 and p3 are indices into the points array, n0, n1, n2 and
        n3 are indices into the normals array, and t0, t1, t2 and t3 are indices
        into the texCoords array. 
        
        so if you want a triangle, and our triangle meshes are in TEXCOORD,
        you move in sets of 6
        you get the first item in the faces array
        then use that index in the point array for x
        to get the y, you add one ot that index
        and z you add two
        then you get the third item in the faces array
        then the fifth item in the faces array;
        

        // this means poitns mod 9 should return 
         */
        for (int i = 0; i < positiveFaceIndeces.length; i += 6) {
            // three values make up a vector
            // three vectors (9 values) for a triangle
            Vector A = new Vector((double) positivePoints[positiveFaceIndeces[i] * 3], (double) positivePoints[positiveFaceIndeces[i] * 3 + 1], (double) positivePoints[positiveFaceIndeces[i] * 3 + 2]);
            Vector B = new Vector((double) positivePoints[positiveFaceIndeces[i + 2] * 3], (double) positivePoints[positiveFaceIndeces[i + 2] * 3 + 1], (double) positivePoints[positiveFaceIndeces[i + 2] * 3 + 2]);
            Vector C = new Vector((double) positivePoints[positiveFaceIndeces[i + 4] * 3], (double) positivePoints[positiveFaceIndeces[i + 4] * 3 + 1], (double) positivePoints[positiveFaceIndeces[i + 4] * 3 + 2]);

            Triangle t = new Triangle(A, B, C, 1);
            triangles[i / 6] = t;
            positiveTriangles[i / 6] = t;

        }

        for (int i = 0; i < negativeFaceIndeces.length; i += 6) {
            // three values make up a vector
            // three vectors (9 values) for a triangle
            Vector A = new Vector((double) negativePoints[negativeFaceIndeces[i] * 3], (double) negativePoints[negativeFaceIndeces[i] * 3 + 1], (double) negativePoints[negativeFaceIndeces[i] * 3 + 2]);
            Vector B = new Vector((double) negativePoints[negativeFaceIndeces[i + 2] * 3], (double) negativePoints[negativeFaceIndeces[i + 2] * 3 + 1], (double) negativePoints[negativeFaceIndeces[i + 2] * 3 + 2]);
            Vector C = new Vector((double) negativePoints[negativeFaceIndeces[i + 4] * 3], (double) negativePoints[negativeFaceIndeces[i + 4] * 3 + 1], (double) negativePoints[negativeFaceIndeces[i + 4] * 3 + 2]);

            Triangle t = new Triangle(A, B, C, -1);
            triangles[(i+positiveFaceIndeces.length) / 6] = t;
            negativeTriangles[i / 6] = t;

        }

    }

    public void translatePositiveTriangles(Vector offset) {
        for (Triangle t : triangles) {
            if (t.polarity > 0) {
                t.translate(offset);
            }
        }
    }

    public void translateNegativeTriangles(Vector offset) {
        for (Triangle t : triangles) {
            if (t.polarity < 0) {
                t.translate(offset);
            }
        }
    }

    public void translateTriangles(Vector offset) {
        for (Triangle t : triangles) {
            t.translate(offset);
        }
    }

    // for each polarity
    // make two lists
    // one with triangles, which you add to for each triangle of that polarity
    // another with the total surface area up to that point
    // so if you binary search for a surface area at reandom
    // your chance of gettign any triangle is proportional to that triangle's surface area
    public void sumUpSurfaceArea() {
        positiveSA = 0.0;
        negativeSA = 0.0;
        for (int i = 0; i < triangles.length; i++) {
            if (triangles[i].polarity > 0) {
                positiveSA += triangles[i].surfaceArea;
                positiveTriangles[i] = triangles[i];
                positiveSumSA[i] = positiveSA;

            } else if (triangles[i].polarity < 0) {
                negativeSA += triangles[i].surfaceArea;
                negativeTriangles[i-positiveTriangles.length] = triangles[i];
                negativeSumSA[i-positiveTriangles.length] = negativeSA;
            }
            triangleSumSA[i] = triangles[i].surfaceArea;
        }
        Double previous = positiveSumSA[0];
        Double current;
        for (int i = 1; i < positiveSumSA.length; i++) {

            current = positiveSumSA[i];
            //System.out.println("current: " + current);
            if (previous > current) {
                System.out.println("oijfodsijf;ldskf");
                break;
            } else {
                previous = current;
            }
        }
        totalSA = positiveSA + negativeSA;
        /*for(Triangle t: triangles){
            System.out.print(t);
            System.out.println("SA: " + t.getSurfaceArea());
        }
         */
        Arrays.sort(positiveSumSA);
        System.out.println("arrays binary search: " + Arrays.binarySearch(positiveSumSA, 5.0));
        System.out.println("my binary search: " + binarySearch(positiveSumSA, 5.0));
    }

    public int binarySearch(Double[] d, Double target) {
        Boolean first = true;
        Boolean done = false;
        int leftEdge = 0;
        int rightEdge = d.length - 1;
        int middle = (rightEdge + leftEdge) / 2;

        while (!done) {
            if (middle == rightEdge) {
                if (first) {
                    first = false;
                    if (d[leftEdge] > target) {
                        return rightEdge;
                    }
                }
                done = true;
                return rightEdge;
            } else if (middle == leftEdge) {
                if (first) {
                    first = false;
                    if (d[leftEdge] < target) {
                        return rightEdge;
                    }
                }
                done = true;
                return leftEdge;
            }
            Double m = d[middle];
            if (m > target) {
                rightEdge = middle;
            } else if (m < target) {
                leftEdge = middle;
            } else {
                done = true;
                return middle;
            }
            if (leftEdge + 1 == rightEdge) {
                if (target > d[leftEdge]) {
                    middle = rightEdge;
                } else {
                    middle = leftEdge;
                }
            } else {
                middle = (rightEdge + leftEdge) / 2;
            }

            first = false;
        }
        return -1;
    }

    public Charge getRandomPositiveCharge() {
        Random SAGen = new Random();
        double SA;
        int index;
        Triangle t;
        SA = SAGen.nextDouble();
        //System.out.println("SA: " + SA);
        SA *= positiveSA;
        index = binarySearch(positiveSumSA, SA);

        t = positiveTriangles[index];

        return t.genRandCharge();

    }

    public Charge getRandomNegativeCharge() {
        Random SAGen = new Random();
        double SA;
        int index;
        Triangle t;
        SA = SAGen.nextDouble();
        SA *= negativeSA;
        index = binarySearch(negativeSumSA, SA);
        t = negativeTriangles[index];
        return t.genRandCharge();
    }

    public Charge getRandomCharge() {
        Random SAGen = new Random();
        int index;
        double SA = totalSA;
        Triangle t;
        SA *= SAGen.nextDouble();
        index = binarySearch(triangleSumSA, SA);
        t = triangles[index];
        return t.genRandCharge();
    }

}
