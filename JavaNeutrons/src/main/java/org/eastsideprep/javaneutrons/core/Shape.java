package org.eastsideprep.javaneutrons.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.fxyz3d.importers.Importer3D;

public class Shape extends MeshView {

    public TriangleMesh mesh = null;
    double[] vertices = null;
    int[] faces = null;
    public Part part;
    public Material containedMaterial;
    public String name;
    private HashSet<Integer> facesContainingMaterial = new HashSet<>();

    // fresh
    public Shape() {
        this.mesh = new TriangleMesh();
        super.setMesh(this.mesh);
        setVisuals("gray");
    }

    // clone
    public Shape(Shape shape) {
        super(shape.mesh);
        this.mesh = shape.mesh;
        setVisuals("yellow");
    }

    // from existing triangle mesh
    public Shape(TriangleMesh mesh) {
        super(mesh);
        this.mesh = mesh;
        setVisuals("purple");
    }

    // use this constructor to construct a shape from an FXyz object 
    // (they extend MeshView)
    public Shape(MeshView mv) {
        Mesh m = mv.getMesh();

        if (m instanceof TriangleMesh) {
            this.mesh = (TriangleMesh) m;
            super.setMesh(m);
        } else {
            throw new IllegalArgumentException("Constructing Shape from invalid kind of mesh: " + m);
        }

        setVisuals("purple");
    }

    // use this constructor to construct a shape from an OBJ/STL file
    // will use only the first mesh in the group
    public Shape(URL url, String unit) {
        ArrayList<Shape> shapes;

        if (url.toString().toLowerCase().endsWith("obj")) {
            shapes = loadOBJ(url, unit);
        } else if (url.toString().toLowerCase().endsWith("stl")) {
            shapes = loadSTL(url, unit);
        } else {
            throw new IllegalArgumentException("Shape contructor: Not OBJ/STL file: " + url);
        }

        if (shapes.size() != 1) {
            throw new IllegalArgumentException("Contructing shape from OBJ/STL file containing more or fewer than one mesh: " + url);
        }
        this.mesh = (TriangleMesh) shapes.get(0).getMesh();
        super.setMesh(this.mesh);
        setVisuals("green");
    }

    // use this constructor to construct a shape from an OBJ/STL file
    // will use only the first mesh in the group
    public Shape(URL url) {
        this(url, "cm");
    }

    public void setName(String name) {
        this.name = name;
    }

    //
    // setVisuals
    // default vis attributes for shapes
    //
    private void setVisuals(String color) {
        this.setColor(color);
        this.setOpacity(0.5);
        this.setDrawMode(DrawMode.LINE);
    }

    //
    // loadOBJ
    //
    // static helper, can be used to load multi-shape OBJ files
    //
    public static ArrayList<Shape> loadOBJ(URL url, String unit) {
        ArrayList<Shape> shapes = new ArrayList<>();
        float factor = factorFromUnit(unit);

        Group g = null;
        try {
            g = Importer3D.load(url).getRoot();
        } catch (IOException e) {
            System.err.println("Error reading OBJ file " + url);
        }

        // go through the group of objects
        // add all triangle meshes to the list
        for (Node n : g.getChildren()) {
            if (n instanceof MeshView) {
                //System.out.println("Adding shape");
                MeshView mv = (MeshView) n;
                TriangleMesh m = (TriangleMesh) mv.getMesh();
                if (factor != 1.0f) {
                    float[] data = m.getPoints().toArray(null);
                    for (int i = 0; i < data.length; i++) {
                        data[i] *= factor;
                    }
                    m.getPoints().clear();
                    m.getPoints().addAll(data);
                }
                shapes.add(new Shape((MeshView) n));
            }
        }
        return shapes;
    }

    public static Scanner openSTL(URL url) {
        InputStream is;
        try {
            is = new FileInputStream(new File(url.toURI()));
        } catch (Exception e) {
            System.err.println("Error opening STL file " + url + ": " + e);
            return null;
        }
        Scanner sc = new Scanner(is);
        return sc;
    }

    private static String nextObject(Scanner sc) {
        if (!sc.hasNextLine()) {
            return null;
        }
        String object = sc.nextLine();
        if (!object.startsWith("solid ")) {
            return null;
        }
        return object.substring(6);
    }

    private static float[][] nextFacet(Scanner sc, float factor) {
        String line;
        String[] numbers;

        if (!sc.hasNextLine()) {
            return null;
        }
        line = sc.nextLine().trim();
        if (!line.startsWith("facet normal")) {
            return null;
        }

        float[] n = new float[3];
        line = line.substring(12).trim();
        numbers = line.split(" ");
        for (int j = 0; j < 3; j++) {
            n[j] = Float.valueOf(numbers[j]);
        }

        if (!sc.hasNextLine() || !sc.nextLine().trim().equals("outer loop")) {
            return null;
        }

        float[][] face = new float[3][3];
        for (int i = 0; i < 3; i++) {
            line = sc.nextLine();
            if (!line.trim().startsWith("vertex")) {
                return null;
            }
            line = line.trim().substring(7);
            numbers = line.split(" ");
            for (int j = 0; j < 3; j++) {
                face[i][j] = Float.valueOf(numbers[j]) * factor;
            }
            //System.out.println("Point " + Arrays.toString(face[i]));
        }

        if (!sc.hasNextLine() || !sc.nextLine().trim().equals("endloop")) {
            return null;
        }

        if (!sc.hasNextLine() || !sc.nextLine().trim().equals("endfacet")) {
            return null;
        }

//        // check order against normal vector
//        Vector3D v0 = new Vector3D(face[0][0], face[0][1], face[0][2]);
//        Vector3D v1 = new Vector3D(face[1][0], face[1][1], face[1][2]);
//        Vector3D v2 = new Vector3D(face[2][0], face[2][1], face[2][2]);
//        Vector3D edge1 = v1.subtract(v0);
//        Vector3D edge2 = v2.subtract(v0);
//        Vector3D n1 = new Vector3D(n[0], n[1], n[2]);
//        Vector3D n2 = edge1.crossProduct(edge2);
//        if (n1.dotProduct(n2) < 0) {
//            System.out.println("hah!");
//        }
        return face;
    }

    private static class Vertex {

        float[] coords;

        Vertex(float[] coords) {
            this.coords = new float[3];
            System.arraycopy(coords, 0, this.coords, 0, 3);
        }

        @Override
        public boolean equals(Object b) {
            if (b == null) {
                return false;
            }
            if (!(b instanceof Vertex)) {
                return false;
            }
            Vertex v = (Vertex) b;
            return (this.coords[0] == v.coords[0] && this.coords[1] == v.coords[1] && this.coords[2] == v.coords[2]);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.coords);
        }
    }

    private static float factorFromUnit(String unit) {
        float factor = 1.0f;
        switch (unit) {
            case "cm":
                break;
            case "mm":
                factor = 0.1f;
                break;
            case "m":
                factor = 100f;
                break;
            default:
                break;
        }
        return factor;
    }

    public static ArrayList<Shape> loadSTL(URL url, String unit) {

        Scanner sc = openSTL(url);
        if (sc == null) {
            return null;
        }

        float factor = factorFromUnit(unit);

        ArrayList<Shape> result = new ArrayList<>();

        String object = nextObject(sc);
        while (object != null) {
            HashMap<Vertex, Integer> vertexMap = new HashMap<>();
            ArrayList<int[]> facesList = new ArrayList<>();

            // read all face data
            float[][] face = nextFacet(sc, factor);
            while (face != null) {

                int[] faceData = new int[6];
                // for each vertex in the current face
                for (int k = 0; k < 3; k++) { // 3 points in a triangle
                    // convert it to float
                    // try to find it
                    Vertex v = new Vertex(face[k]);
                    int vertex = vertexMap.getOrDefault(v, -1);
                    if (vertex == -1) {
                        // if not found, insert
                        vertex = vertexMap.size();
                        vertexMap.put(v, vertex);
                        //System.out.println("point added  " + Arrays.toString(v.coords) + ", hash " + v.hashCode() + " " + vertexMap.containsKey(v));
                    }
                    faceData[2 * k] = vertex;
                }
                facesList.add(faceData);
                //System.out.println("");

                face = nextFacet(sc, factor);
            }

            // prepare shape
            Shape s = new Shape();

            // prepare mesh
            s.mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);

            // need to convert vertices 
            float[] vertices = new float[vertexMap.size() * 3];
            for (Entry<Vertex, Integer> e : vertexMap.entrySet()) {
                //System.out.println("" + e.getValue() + ": " + Arrays.toString(e.getKey().coords));
                System.arraycopy(e.getKey().coords, 0, vertices, e.getValue() * 3, 3);
            }
            // add converted vertices to mesh
            s.mesh.getPoints().addAll(vertices);
            for (int i = 0; i < vertices.length; i++) {
                //System.out.print(vertices[i] + " ");
                //if ((i + 1) % 3 == 0) {
                //    System.out.println("");
                //}
            }
            //System.out.println("");

            // set dummy texcoords
            s.mesh.getTexCoords().addAll(0, 0);

            // convert faces to flat array
            int[] faces = new int[facesList.size() * 6];
            for (int j = 0; j < facesList.size(); j++) {
                System.arraycopy(facesList.get(j), 0, faces, j * 6, 6);
            }
            // add faces to mesh
            s.mesh.getFaces().addAll(faces);
            for (int i = 0; i < faces.length; i++) {
                //System.out.print(faces[i] + " ");
                //if ((i + 1) % 6 == 0) {
                //    System.out.println("");
                //}
            }

            s.setName(object);
            result.add(s);

            // and next object until done with file
            object = nextObject(sc);
        }

        return result;
    }

    private double[] getVertices() {
        float[] floats = mesh.getPoints().toArray(null);
        double[] v = new double[floats.length];
        for (int i = 0; i < floats.length; i++) {
            v[i] = floats[i];
        }
        return v;
    }

    private double[] getTransformedVertices() {
        double[] v = getVertices();

        // transform all vertices by all transforrms
        double[] verticesTransformed = new double[v.length];
        for (int i = this.getTransforms().size() - 1; i >= 0; i--) {
            Transform t = this.getTransforms().get(i);
            //System.out.println("Shape "+this+", Transform: "+t);
            t.transform3DPoints(v, 0,
                    verticesTransformed, 0,
                    v.length / 3);
            double[] temp = v;
            v = verticesTransformed;
            verticesTransformed = temp;
        }
        return v;
    }

    private int[] getFaces() {
        return mesh.getFaces().toArray(null);
    }

    void cacheVerticesAndFaces() {
        synchronized (this) {
            if (vertices == null) {
                //System.out.println("Original vertices for " + this.part.name);
                //System.out.println(Arrays.toString(this.getVertices()));
                vertices = getTransformedVertices();
                //System.out.println("Vertices for " + this.part.name);
                //System.out.println(Arrays.toString(vertices));
            }

            if (faces == null) {
                faces = getFaces();
                //System.out.println("Faces for " + this.part.name);
                //System.out.println(Arrays.toString(faces));
            }
        }
    }

    //
    // rayIntersect
    //
    // goes through all the triangles in the shape to find the intersection
    // returns t-parameter for the ray, or 0 if not intersecting
    // todo: acceleration structure like hierarchy of volumes
    //
    public double rayIntersect(Vector3D rayOrigin, Vector3D rayDirection, boolean goingOut, int[] faceIndex, LinkedTransferQueue simVis) {
        double tmin = -1;
        int x = 0;
        int y = 1;
        int z = 2;
        int face = -1;
        if (java.lang.Math.abs(rayDirection.getNorm() - 1) > 1e-8) {
            System.out.println("direction not normalized in rayIntersect");
        }

        double ox = rayOrigin.getX();
        double oy = rayOrigin.getY();
        double oz = rayOrigin.getZ();

        double dx = rayDirection.getX();
        double dy = rayDirection.getY();
        double dz = rayDirection.getZ();

        cacheVerticesAndFaces();
//        System.out.println("Checking part" + this.part.name);
        int vis = this.mesh.getVertexFormat().getVertexIndexSize();

        for (int i = 0; i < faces.length; i += 6) {
            double t = Util.Math.rayTriangleIntersect(ox, oy, oz, dx, dy, dz,
                    vertices[3 * faces[i] + x], vertices[3 * faces[i] + y], vertices[3 * faces[i] + z],
                    goingOut ? vertices[3 * faces[i + 2 * vis] + x] : vertices[3 * faces[i + vis] + x],
                    goingOut ? vertices[3 * faces[i + 2 * vis] + y] : vertices[3 * faces[i + vis] + y],
                    goingOut ? vertices[3 * faces[i + 2 * vis] + z] : vertices[3 * faces[i + vis] + z],
                    goingOut ? vertices[3 * faces[i + vis] + x] : vertices[3 * faces[i + 2 * vis] + x],
                    goingOut ? vertices[3 * faces[i + vis] + y] : vertices[3 * faces[i + 2 * vis] + y],
                    goingOut ? vertices[3 * faces[i + vis] + z] : vertices[3 * faces[i + 2 * vis] + z]
            );
            if (t != -1) {
                if (tmin != -1) {
                    if (t < tmin) {
                        tmin = t;
                        face = i;
                    }
                } else {
                    tmin = t;
                    face = i;
                }
            }
        }
        // report back the face index if asked for
        if (faceIndex != null) {
            faceIndex[0] = face;
        }

        return tmin;
    }

    //
    // rayIntersect
    //
    // test one triangle in the shape to find the intersection
    // returns t-parameter for the ray, or -1 if not intersecting
    // used by acceleration structure "Grid"
    //
    public double rayTriangleIntersect(double ox, double oy, double oz, double dx, double dy, double dz, boolean goingOut, int face) {
        int x = 0;
        int y = 1;
        int z = 2;

      
        cacheVerticesAndFaces();
        int vis = this.mesh.getVertexFormat().getVertexIndexSize();

        return Util.Math.rayTriangleIntersect(ox, oy, oz, dx, dy, dz,
                vertices[3 * faces[face] + x], vertices[3 * faces[face] + y], vertices[3 * faces[face] + z],
                goingOut ? vertices[3 * faces[face + 2 * vis] + x] : vertices[3 * faces[face + vis] + x],
                goingOut ? vertices[3 * faces[face + 2 * vis] + y] : vertices[3 * faces[face + vis] + y],
                goingOut ? vertices[3 * faces[face + 2 * vis] + z] : vertices[3 * faces[face + vis] + z],
                goingOut ? vertices[3 * faces[face + vis] + x] : vertices[3 * faces[face + 2 * vis] + x],
                goingOut ? vertices[3 * faces[face + vis] + y] : vertices[3 * faces[face + 2 * vis] + y],
                goingOut ? vertices[3 * faces[face + vis] + z] : vertices[3 * faces[face + 2 * vis] + z]
        );
    }
    
    
    

    public Material getContactMaterial(int faceIndex) {
        if (this.facesContainingMaterial.contains(faceIndex)) {
            return this.containedMaterial;
        }
        return null;
    }

    //
    // getVolume
    //
    // calculates the volume of a mesh in O(N)
    // todo: since the math is so simple, this could be done without creating objects
    //
    public double getVolume() {

        double volume = 0;

        double[] v = this.getVertices();
        int[] f = this.getFaces();

        for (int i = 0; i < f.length; i += 6) {
            Vector3D v0 = new Vector3D(v[3 * f[i]], v[3 * f[i] + 1], v[3 * f[i] + 2]);
            Vector3D v1 = new Vector3D(v[3 * f[i + 2]], v[3 * f[i + 2] + 1], v[3 * f[i + 2] + 2]);
            Vector3D v2 = new Vector3D(v[3 * f[i + 4]], v[3 * f[i + 4] + 1], v[3 * f[i + 4] + 2]);

            v0 = v0.crossProduct(v1);
            volume += v0.dotProduct(v2);
        }

        return Math.abs(volume / 6.0);
    }

    //
    // getExtent
    //
    // calculates the max bondaries of a mesh in O(N)
    //
    public Vector3D getExtent() {
        double[] v = this.getVertices();

        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        double zmin = Double.POSITIVE_INFINITY;
        double zmax = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < v.length; i += 3) {
            xmin = Math.min(v[i], xmin);
            xmax = Math.max(v[i], xmax);
            ymin = Math.min(v[i + 1], ymin);
            ymax = Math.max(v[i + 1], ymax);
            zmin = Math.min(v[i + 2], zmin);
            zmax = Math.max(v[i + 2], zmax);
        }

        return new Vector3D(xmax - xmin, ymax - ymin, zmax - zmin);
    }

    //
    // setColor
    //
    public void setColor(String webColor) {
        final PhongMaterial pm = new PhongMaterial();
        pm.setSpecularColor(Color.web(webColor));
        pm.setDiffuseColor(Color.web(webColor));
        this.setMaterial(pm);
    }

    // 
    // for containing things other than air
    //
    public void markSurfaceInContactWith(Vector3D origin, Vector3D direction, Material material, LinkedTransferQueue g) {
        // find the triangle face
        int[] face = new int[1];
        this.rayIntersect(origin, direction, false, face, g);

        // set up a queue of triangles that share the points of this one
        LinkedList<Integer> facesQueue = new LinkedList<>();

        // and a set of processed faces so we don't visit one twice
        HashSet<Integer> facesAdded = new HashSet<>();

        // let's get started
        facesQueue.add(face[0]);
        facesAdded.add(0);

        while (!facesQueue.isEmpty()) {
            // get a face to process
            int nextFace = facesQueue.removeFirst();

            // mark it as "special"
            this.facesContainingMaterial.add(nextFace);

            // queue all faces that share points with this one
            addFacesForPoint(faces[nextFace], facesQueue, facesAdded);
            addFacesForPoint(faces[nextFace + 2], facesQueue, facesAdded);
            addFacesForPoint(faces[nextFace + 4], facesQueue, facesAdded);
        }

        this.containedMaterial = material;
        System.out.println("Part " + this.part.name + ": Marked " + facesAdded.size() + " faces as in contact with " + material.name);
    }

    private void addFacesForPoint(int pointIndex, LinkedList<Integer> facesQueue, HashSet<Integer> facesAdded) {
        for (int i = 0; i < faces.length; i += 6) {
            if (faces[i] == pointIndex || faces[i + 2] == pointIndex || faces[i + 4] == pointIndex) {
                if (!facesAdded.contains(i)) {
                    facesQueue.add(i);
                    facesAdded.add(i);
                }
            }
        }
    }

    Translate settleAgainst(Shape other, final Vector3D f) {
        // equality within epsilon
        double epsilon = 1e-12;
        int jiggleCount = 200;
        double sd = 1.0;

        // first, move it with the force vector, to up to within 1mm of the target
        double t = distance(other, f);
        if (t == -1) {
            return null;
        }
        this.getTransforms().add(0, new Translate(t * f.getX(), Math.min(t * f.getY(), t * f.getY() - 0.1), t * f.getZ()));

        // now, jiggle it
        List<Vector3D> jiggles = Stream
                .generate(() -> Util.Math.jiggle(f, sd))
                .limit(jiggleCount)
                .collect(Collectors.toList());

        double tmin = -1;
        Vector3D best = null;
        for (Vector3D j : jiggles) {
            t = distance(other, j);
            tmin = Util.Math.minIfValid(t, tmin);
            if (t != -1 && t == tmin) {
                best = j;
            }
        }

        if (best == null) {
            return null;
        }

        return new Translate(tmin * best.getX(), tmin * best.getY(), tmin * best.getZ());
    }

    // what is the distance from our vertices to the other thing, 
    // and vice-versa?
    double distance(Shape other, Vector3D direction) {
        double t1 = this.oneWayDistance(other, direction);
        double t2 = other.oneWayDistance(this, direction.negate());

        return Math.min(t1, t2);
    }

    // what is the distance of our vertices to 
    private double oneWayDistance(Shape other, Vector3D direction) {
        // for every vertex in this shape,
        // would it intersect the other shape if moved in direction dir?
        // if so, at what distance? Find the min.

        Vector3D d = direction.normalize();
        double[] v = this.getTransformedVertices();
        double tmin = -1;

        for (int i = 0; i < v.length; i += 3) {
            Vector3D vertex = new Vector3D(v[i], v[i + 1], v[i + 2]);
            double t = other.rayIntersect(vertex, d, false, null, null);
            tmin = Util.Math.minIfValid(t, tmin);
        }

        return tmin;
    }

}
