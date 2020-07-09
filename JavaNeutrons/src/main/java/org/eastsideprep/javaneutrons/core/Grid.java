package org.eastsideprep.javaneutrons.core;

import java.util.HashMap;
import java.util.LinkedList;

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

        double minX;
        double minY;
        double minZ;

        CellID(double x, double y, double z) {
            minX = x;
            minY = y;
            minZ = z;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(minX) ^ Double.hashCode(minY) ^ Double.hashCode(minZ);
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
    double side;
    double gminx = Double.POSITIVE_INFINITY;
    double gminy = Double.POSITIVE_INFINITY;
    double gminz = Double.POSITIVE_INFINITY;
    double gmaxx = Double.NEGATIVE_INFINITY;
    double gmaxy = Double.NEGATIVE_INFINITY;
    double gmaxz = Double.NEGATIVE_INFINITY;
    
    
    public void addAssembly(Assembly a) {
        a.getParts().stream().forEach(p->addPart(p));
    }

    public void addPart(Part p) {
        Shape s = p.shape;
        s.cacheVerticesAndFaces();
        for (int f = 0; f < s.faces.length; f += 6) {
            addTriangle(new Triangle(p, f));
        }
    }

    void addTriangle(Triangle t) {
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
            minx = Math.min(v[f[t.face + k] + 0], minx);
            maxx = Math.min(v[f[t.face + k] + 0], maxx);

            // vertex y values
            miny = Math.min(v[f[t.face + k] + 1], miny);
            maxy = Math.min(v[f[t.face + k] + 1], maxy);

            // vertex x values
            minz = Math.min(v[f[t.face + k] + 2], minz);
            maxz = Math.min(v[f[t.face + k] + 2], maxz);
        }
        minx = Math.floor(minx / side) * side;
        miny = Math.floor(miny / side) * side;
        minz = Math.floor(minz / side) * side;
        maxx = Math.ceil(maxx / side) * side;
        maxy = Math.ceil(maxy / side) * side;
        maxz = Math.ceil(maxz / side) * side;

        for (double x = minx; x < maxx; x += side) {
            for (double y = miny; y < maxy; y += side) {
                for (double z = minz; z < maxz; z += side) {
                    CellID cid = new CellID(x, y, z);
                    Cell cell = cells.get(cid);
                    if (cell == null) {
                        cell = cells.putIfAbsent(cid, new Cell(cid));
                        gminx = Math.min(x, gminx);
                        gminy = Math.min(y, gminy);
                        gminz = Math.min(z, gminz);
                        gmaxx = Math.max(x, gmaxx);
                        gmaxy = Math.max(y, gmaxy);
                        gmaxz = Math.max(z, gmaxz);
                    }
                    cell.addTriangle(t);
                }
            }
        }
    }

    private boolean inBoundingBox(double x, double y, double z) {
        return x >= gminx && x <= gmaxx
                && y >= gminy && y <= gmaxy
                && z >= gminz && z <= gmaxz;
    }

    Triangle rayIntersect(
            double ox, double oy, double oz,
            double dx, double dy, double dz,
            double v0x, double v0y, double v0z,
            double v1x, double v1y, double v1z,
            double v2x, double v2y, double v2z,
            boolean goingOut
    ) {
        // establish origin cell
        double cx = Math.ceil(ox / side) * side;
        double cy = Math.ceil(oy / side) * side;
        double cz = Math.ceil(oz / side) * side;

        // loop:
        while (inBoundingBox(cx, cy, cz)) {
            // see whether we have a cell with triangles at that position
            CellID id = new CellID(cx, cy, cz);
            Cell cell = cells.get(id);
            if (cell != null) {
                //   intersect all triangles in cell
                for (Triangle tr : cell.triangles) {
                    double t = tr.part.shape.rayTriangleIntersect(ox, oy, oz, dx, dy, dz, goingOut, tr.face);
                    if (t != -1) {
                        // found? leave.
                        return tr;
                    }
                }
            }
            // "how long" to exit in dimension?
            // i.e. calculate tx, ty, tz for leaving the cell for x, y, z
            double tx = Math.ceil(ox * Math.signum(dx) / side) * side / dx;
            double ty = Math.ceil(oy * Math.signum(dy) / side) * side / dy;
            double tz = Math.ceil(ox * Math.signum(dz) / side) * side / dz;

            double tmin = Math.min(Math.min(tx, ty), tz);
            if (tx == tmin) {
                // exit in x direction
                cx += Math.signum(dx);
            }
            if (ty == tmin) {
                // exit in y direction
                cy += Math.signum(dy);
            }
            if (tz == tmin) {
                // exit in y direction
                cz += Math.signum(dz);
            }
        }
        return null;
    }
}
