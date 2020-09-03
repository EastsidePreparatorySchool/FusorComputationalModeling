package org.eastsideprep.javaneutrons.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedTransferQueue;
import javafx.scene.Group;
import javafx.scene.Node;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Grid {

    public class Triangle {

        Part part;
        int face;

        Triangle(Part p, int f) {
            this.part = p;
            this.face = f;
        }
    }

    private class CellID {

        int minX;
        int minY;
        int minZ;

        CellID(double x, double y, double z) {
            minX = (int) Math.round(x);
            minY = (int) Math.round(y);
            minZ = (int) Math.round(z);
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(minX) ^ Integer.hashCode(minY) ^ Integer.hashCode(minZ);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CellID other = (CellID) obj;
            return minX == other.minX && minY == other.minY && minZ == other.minZ;
        }

    }

    private class Cell {

        CellID id;
        LinkedList<Triangle> triangles = new LinkedList<>();

        Cell(CellID id) {
            this.id = id;
        }

        void addTriangle(Triangle t) {
            this.triangles.add(t);
        }
    }

    // member variables for Grid
    HashMap<CellID, Cell> cells = new HashMap<>();
    public double side;
    public double slice;
    double gminx = Double.POSITIVE_INFINITY;
    double gminy = Double.POSITIVE_INFINITY;
    double gminz = Double.POSITIVE_INFINITY;
    double gmaxx = Double.NEGATIVE_INFINITY;
    double gmaxy = Double.NEGATIVE_INFINITY;
    double gmaxz = Double.NEGATIVE_INFINITY;
    int totalTriangles = 0;
    int totalTrianglesTimesCells = 0;

    public Grid(double side, Assembly a, Vector3D origin, Group g) {
        LinkedTransferQueue<Node> q = new LinkedTransferQueue<>();
        this.side = side;
        this.slice = 0.01; // give triangles a bit of a third dimension
        this.totalTriangles = 0;
        this.totalTrianglesTimesCells = 0;
        addAssembly(a, q);

        gminx = Math.min(origin.getX(), gminx);
        gminy = Math.min(origin.getY(), gminy);
        gminz = Math.min(origin.getZ(), gminz);
        gmaxx = Math.max(origin.getX(), gmaxx);
        gmaxy = Math.max(origin.getY(), gmaxy);
        gmaxz = Math.max(origin.getZ(), gmaxz);

        q.drainTo(g.getChildren());
        System.out.println("Grid: added " + this.totalTriangles + "  tringles, t*c: " + this.totalTrianglesTimesCells);
    }

    public Grid(Assembly a) {
        LinkedTransferQueue<Node> q = new LinkedTransferQueue<>();
        this.side = 20.0;
        this.totalTriangles = 0;
        this.totalTrianglesTimesCells = 0;
        addAssembly(a, q);
        System.out.println("Grid: added " + this.totalTriangles + "  tringles, t*c: " + this.totalTrianglesTimesCells);
    }

    private void addAssembly(Assembly a, LinkedTransferQueue<Node> vis) {
        a.getParts().stream().forEach(p -> addPart(p, vis));
    }

    private void addPart(Part p, LinkedTransferQueue<Node> vis) {
        //ArrayList<Triangle> at = new ArrayList<>();
        Shape s = p.shape;
        s.cacheVerticesAndFaces();
        for (int f = 0; f < s.faces.length; f += 6) {
            //at.add(new Triangle(p, f));
            addTriangle(new Triangle(p, f), vis);
        }
        //at.parallelStream().forEach(t -> addTriangle(t,vis));
    }

    void addTriangle(Triangle t, LinkedTransferQueue<Node> vis) {
        double minx = Double.POSITIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double minz = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        double maxz = Double.NEGATIVE_INFINITY;
        int[] f = t.part.shape.faces;
        double[] v = t.part.shape.vertices;

        // for all vertices in triangle
        for (int k = 0; k < 6; k += 2) {
            // vertex x values
            minx = Math.min(v[3 * f[t.face + k] + 0], minx);
            maxx = Math.max(v[3 * f[t.face + k] + 0], maxx);

            // vertex y values
            miny = Math.min(v[3 * f[t.face + k] + 1], miny);
            maxy = Math.max(v[3 * f[t.face + k] + 1], maxy);

            // vertex x values
            minz = Math.min(v[3 * f[t.face + k] + 2], minz);
            maxz = Math.max(v[3 * f[t.face + k] + 2], maxz);
        }
        minx = Math.floor((minx-this.slice) / side) * side;
        miny = Math.floor((miny-this.slice) / side) * side;
        minz = Math.floor((minz-this.slice) / side) * side;
        maxx = Math.ceil((maxx+this.slice) / side) * side;
        maxy = Math.ceil((maxy+this.slice) / side) * side;
        maxz = Math.ceil((maxz+this.slice) / side) * side;

        for (double x = minx; x <= maxx; x += side) {
            for (double y = miny; y <= maxy; y += side) {
                for (double z = minz; z <= maxz; z += side) {
                    CellID cid = new CellID(x, y, z);
                    Cell cell;
                    synchronized (this) {
                        cell = cells.get(cid);
                    }
                    if (cell == null) {
                        cell = new Cell(cid);
                        synchronized (this) {
                            cells.put(cid, cell);
                            // draw cube

                            //Util.Graphics.drawCube(vis, new Vector3D(x+side/2, y+side/2, z+side/2), (float)side, "purple");
                            // update global bounds
                            gminx = Math.min(x, gminx);
                            gminy = Math.min(y, gminy);
                            gminz = Math.min(z, gminz);
                            gmaxx = Math.max(x, gmaxx);
                            gmaxy = Math.max(y, gmaxy);
                            gmaxz = Math.max(z, gmaxz);
                        }

                    }
                    synchronized (cell) {
                        cell.addTriangle(t);
                    }
                    synchronized (this) {
                        this.totalTrianglesTimesCells++;
                    }
                }
            }
        }
        synchronized (this) {
            this.totalTriangles++;
        }
    }

    private boolean inBoundingBox(double x, double y, double z) {
        return x >= gminx && x <= gmaxx
                && y >= gminy && y <= gmaxy
                && z >= gminz && z <= gmaxz;
    }

    Event rayIntersect(Vector3D origin, Vector3D direction, boolean goingOut, LinkedTransferQueue<Node> vis, double tmax) {
        double hs = side / 2.0;

        double ox = origin.getX();
        double oy = origin.getY();
        double oz = origin.getZ();

        double dx = direction.getX();
        double dy = direction.getY();
        double dz = direction.getZ();

        // establish origin cell
        double cx = Math.floor(ox / side) * side;
        double cy = Math.floor(oy / side) * side;
        double cz = Math.floor(oz / side) * side;

        double ttotal = 0;

        // loop:
        while (inBoundingBox(cx, cy, cz)) {
            // exceeded client's tmax?
            if (ttotal > tmax) {
                return new Event(Util.Math.rayPoint(origin, direction, ttotal), null, ttotal, goingOut, -1);
            }

            if (vis != null) {
                Util.Graphics.drawCube(vis, new Vector3D(cx + side / 2, cy + side / 2, cz + side / 2), (float) side, "orange");
            }

            // see whether we have a cell with triangles at that position
            CellID id = new CellID(cx, cy, cz);
            Cell cell = cells.get(id);
            if (cell != null) {

                //   intersect all triangles in cell
                double tmin = -1;
                double tmin2 = -1;
                Part p = null;
                Part p2 = null;
                int face = -1;
                int face2 = -1;

                for (Triangle tr : cell.triangles) {
                    // find intersecting triangles
                    double t = tr.part.shape.rayTriangleIntersect(ox, oy, oz, dx, dy, dz, goingOut, tr.face);
                    if (t == 0  && goingOut) {
                        // there won't be a zero-distance to exiting a part.
                        // instead, this is the exit face of the adjacent part
                        // we came out of last.
                            continue;
                    }
                    double tminNew = Util.Math.minIfValid(t, tmin);
                    if (tminNew < tmin || (tmin == -1.0 && tminNew != -1.0)) {
                        tmin = tminNew;
                        p = tr.part;
                        face = tr.face;
                    }

                    if (goingOut) {
                        // find triangles that intersect right into the next part, if we are on the way out
                        double t2 = tr.part.shape.rayTriangleIntersect(ox, oy, oz, dx, dy, dz, false, tr.face);
                        double tminNew2 = Util.Math.minIfValid(t2, tmin2);
                        if (tminNew2 < tmin2 || (tmin2 == -1.0 && tminNew2 != -1.0)) {
                            tmin2 = tminNew2;
                            p2 = tr.part;
                            face2 = tr.face;
                        }
                    }
                }
                if (tmin != -1) {
                    ttotal += tmin;
                    // found? Make event and return
                    if (tmin2 == tmin) {
                        // found 2 triangles, in and out, at same spot
                        return new Event(origin.add(direction.scalarMultiply(ttotal)), p, p2, ttotal);
                    }
                    return new Event(origin.add(direction.scalarMultiply(ttotal)), p, ttotal, goingOut, face);
                }
            }
            // "how long" to exit in dimension?
            // i.e. calculate tx, ty, tz for leaving the cell for x, y, z
            // divisions by zero are ok here, will lead to infinities
            double tx = (hs + hs * Math.signum(dx) - (ox - cx)) / dx;
            double ty = (hs + hs * Math.signum(dy) - (oy - cy)) / dy;
            double tz = (hs + hs * Math.signum(dz) - (oz - cz)) / dz;

            tx = Double.isNaN(tx) ? Double.POSITIVE_INFINITY : tx;
            ty = Double.isNaN(ty) ? Double.POSITIVE_INFINITY : ty;
            tz = Double.isNaN(tz) ? Double.POSITIVE_INFINITY : tz;

            // find out which dimension hits first
            // this part is ok I think
            double tmin = Math.min(Math.min(tx, ty), tz);
            ttotal += tmin;
            if (tx == tmin) {
                // exit in x direction
                cx += Math.signum(dx) * side;
            }
            if (ty == tmin) {
                // exit in y direction
                cy += Math.signum(dy) * side;
            }
            if (tz == tmin) {
                // exit in y direction
                cz += Math.signum(dz) * side;
            }

            ox += tmin * dx;
            oy += tmin * dy;
            oz += tmin * dz;
        }
        return null;
    }
}
