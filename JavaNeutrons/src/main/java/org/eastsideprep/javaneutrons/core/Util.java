package org.eastsideprep.javaneutrons.core;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Util {

    static public class Math {

        //
        // helping to keep a counter tmin valid through comparison with many t values
        // just hides a little ugliness
        //
        public static double minIfValid(double t, double tmin) {
            if (t != -1) {
                if (tmin != -1) {
                    tmin = java.lang.Math.min(t, tmin);
                } else {
                    tmin = t;
                }
            }
            return tmin;
        }

        //
        // jiggle vector
        //
        public static Vector3D jiggle(Vector3D v, double sd) {
            double epsilon = 1e-12;
            // pick a couple of normals to the vector
            Vector3D n1 = v.crossProduct(Vector3D.PLUS_I);
            if (n1.getNorm() < epsilon) {
                n1 = v.crossProduct(Vector3D.PLUS_J);
            }

            n1 = n1.normalize();
            Vector3D n2 = v.crossProduct(n1).normalize();

            return v.add(n1.scalarMultiply(ThreadLocalRandom.current().nextGaussian() * sd))
                    .add(n2.scalarMultiply(ThreadLocalRandom.current().nextGaussian() * sd))
                    .normalize();
        }

        public static double randomGaussian() {
            return ThreadLocalRandom.current().nextGaussian();
        }

        public static double random() {
            return ThreadLocalRandom.current().nextDouble();
        }

        public static Vector3D randomDir() {
//            double phi = random() * 2 * java.lang.Math.PI;
//            double z = random() * 2 - 1;
//            double theta = java.lang.Math.asin(z);
//            return new Vector3D(java.lang.Math.cos(theta) * java.lang.Math.cos(phi), java.lang.Math.cos(theta) * java.lang.Math.sin(phi), z);
            return randomDir(random() * 2 - 1, 1.0);
        }

        public static Vector3D randomDir(double cos_theta, double magnitude) {
            double phi = random() * 2 * java.lang.Math.PI;
            double sin_theta = java.lang.Math.sqrt(1 - cos_theta * cos_theta);
            return new Vector3D(magnitude * sin_theta * java.lang.Math.cos(phi), magnitude * sin_theta * java.lang.Math.sin(phi), magnitude * cos_theta);
        }

        public static Vector3D randomGaussianComponentVector(double componentSD) {
            return new Vector3D(ThreadLocalRandom.current().nextGaussian() * componentSD,
                    ThreadLocalRandom.current().nextGaussian() * componentSD,
                    ThreadLocalRandom.current().nextGaussian() * componentSD);
        }

        public static boolean solveQuadratic(double a, double b, double c, double[] result) {
            double discr = b * b - 4 * a * c;
            if (discr < 0) {
                return false;
            } else if (discr == 0) {
                result[0] = -0.5 * b / a;
                result[1] = result[0];
            } else {
                double q = (b > 0)
                        ? -0.5 * (b + java.lang.Math.sqrt(discr))
                        : -0.5 * (b - java.lang.Math.sqrt(discr));
                result[0] = q / a;
                result[1] = c / q;
            }
            if (result[0] > result[1]) {
                double temp = result[0];
                result[0] = result[1];
                result[1] = temp;
            }

            return true;
        }

        //
        // raySphereIntersect
        //
        // credit: 
        // https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
        //
        public static double raySphereIntersect(Vector3D orig, Vector3D dir, Vector3D center, double radius) {

            double t0, t1;

            Vector3D L = orig;
            if (center != Vector3D.ZERO) {
                L = L.subtract(center);
            }

            double a = dir.dotProduct(dir);
            double b = 2 * dir.dotProduct(L);
            double c = L.dotProduct(L) - radius * radius;
            double[] result = new double[]{0, 0};
            if (!solveQuadratic(a, b, c, result)) {
                return -1;
            }
            t0 = result[0];
            t1 = result[1];

            if (t0 < 0) {
                t0 = t1;
                if (t0 < 0) {
                    return -1;
                }
            }
            return t0;
        }

        //
        // rayTriangleIntersect
        //
        // static helper function
        // adapted from: 
        // https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm#Java_Implementation
        //
        public static double rayTriangleIntersect(
                double ox, double oy, double oz,
                double dx, double dy, double dz,
                double v0x, double v0y, double v0z,
                double v1x, double v1y, double v1z,
                double v2x, double v2y, double v2z) {
            final double kEpsilon = 1E-12; // constant for "close enough to 0"
            double a, f, u, v;

            double edge1x = v1x - v0x;
            double edge1y = v1y - v0y;
            double edge1z = v1z - v0z;

            double edge2x = v2x - v0x;
            double edge2y = v2y - v0y;
            double edge2z = v2z - v0z;

            double hx = dy * edge2z - dz * edge2y;
            double hy = dz * edge2x - dx * edge2z;
            double hz = dx * edge2y - dy * edge2x;

            a = edge1x * hx + edge1y * hy + edge1z * hz;

            if (a > -kEpsilon && a < kEpsilon) {
                return -1;    // This ray is parallel to this triangle.
            }

            double nx = edge1y * edge2z - edge1z * edge2y;
            double ny = edge1z * edge2x - edge1x * edge2z;
            double nz = edge1x * edge2y - edge1y * edge2x;

            double nDotDir = nx * dx + ny * dy + nz * dz;
            if (nDotDir > -kEpsilon) {
                return -1; // wrong direction or parallel 
            }

            f = 1.0 / a;
            double sx = ox - v0x;
            double sy = oy - v0y;
            double sz = oz - v0z;

            u = f * (sx * hx + sy * hy + sz * hz);
            if (u < 0.0 || u > 1.0) {
                return -1;
            }

            double qx = sy * edge1z - sz * edge1y;
            double qy = sz * edge1x - sx * edge1z;
            double qz = sx * edge1y - sy * edge1x;

            v = f * (dx * qx + dy * qy + dz * qz);
            if (v < 0.0 || u + v > 1.0) {
                return -1;
            }

            // At this stage we can compute t to find out where the intersection point is on the line.
            double t = f * (edge2x * qx + edge2y * qy + edge2z * qz);
//            if (t > kEpsilon) {
            if (t >= 0) {
                return t;
            } else {
                return -1;
            }
        }

        public static Vector3D rayPoint(Vector3D origin, Vector3D direction, double t) {
            return origin.add(direction.scalarMultiply(t));
        }

    }

    static public class Physics {

        //final public static double boltzmann = 8.61733333353e-5; //eV/K
        final public static double kB = 1.380649e-19; // SI with cm
        final public static double T = 293.0; // K
        final public static double protonMass = 1.67262192369e-27; // SI
        final public static double eV = 1.60218e-19 * 1e4; // 1 eV in SI with cm
        final public static double barn = 1e-24; // 1 barn in SI cm
        final public static double Da = 1.6605e-27; // Dalton amu in kg
        final public static double thermalEnergy = 4.0535154e-21 * 1e4; // room temp avg. energy in J (cm) eqv to 0.0253eV
        // factor 1e4 is from using cm, not m here - 100^2

    }

    static public class Graphics {

        //
        // setColor for JavaFX shapes
        //
        public static void setColor(Shape3D n, String webColor) {
            final PhongMaterial pm = new PhongMaterial();
            pm.setSpecularColor(Color.web(webColor));
            pm.setDiffuseColor(Color.web(webColor));
            n.setMaterial(pm);
        }

        public static void drawSphere(LinkedTransferQueue<Node> g, Vector3D position, float radius, String webColor) {
            Sphere s = new Sphere(radius, 32);
            s.setTranslateX(position.getX());
            s.setTranslateY(position.getY());
            s.setTranslateZ(position.getZ());
            final PhongMaterial pm = new PhongMaterial();
            pm.setSpecularColor(Color.web(webColor));
            pm.setDiffuseColor(Color.web(webColor));
            s.setMaterial(pm);

            g.add(s);
        }

        public static void drawCube(LinkedTransferQueue<Node> g, Vector3D position, float side, String webColor) {
            Box s = new Box(side, side, side);
            s.setTranslateX(position.getX());
            s.setTranslateY(position.getY());
            s.setTranslateZ(position.getZ());
            final PhongMaterial pm = new PhongMaterial();
            pm.setSpecularColor(Color.web(webColor));
            pm.setDiffuseColor(Color.web(webColor));
            s.setMaterial(pm);
            s.setDrawMode(DrawMode.LINE);

            g.add(s);
        }

        public static void drawLine(LinkedTransferQueue<Node> g, Vector3D p1, Vector3D p2, double size, Color c) {
            Vector3D v = p2.subtract(p1);

            Cylinder line = new Cylinder(size, v.getNorm(), 4);
            PhongMaterial pm = new PhongMaterial(c);
            line.setMaterial(pm);

            double phi = java.lang.Math.atan2(v.getX(), v.getY()) * 180 / java.lang.Math.PI;
            double theta = java.lang.Math.acos(v.getZ() / v.getNorm()) * 180 / java.lang.Math.PI;

            line.getTransforms().add(new Translate((p2.getX() + p1.getX()) / 2,
                    (p2.getY() + p1.getY()) / 2, (p2.getZ() + p1.getZ()) / 2));
            line.getTransforms().add(new Rotate(180 - phi, new Point3D(0, 0, 1)));
            line.getTransforms().add(new Rotate(theta - 90, new Point3D(1, 0, 0)));

            g.add(line);
        }

        public static void drawLine(LinkedTransferQueue<Node> g, Vector3D p1, Vector3D p2, double size, double energy) {
            drawLine(g, p1, p2, size, heatColor(energy));

        }

        public static Color heatColor(double energy) {
            double value = java.lang.Math.log10(energy / Util.Physics.eV);
            int min = -4;
            int max = 7;

            double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * (value - (min)) / (max - min);
            Color color = Color.hsb(hue, 1.0, 1.0);
            return color;
        }

        public static Image createHeatMap(int width, int height) {
            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();
            int min = -4;
            int max = 7;

            for (int y = 0; y < height; y++) {
                double value = max - (max - min) * y / (double) height;
                Color color = heatColor(java.lang.Math.pow(10, value) * Util.Physics.eV);
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, color);
                }
            }

            for (int i = min; i < max; i++) {
                int y = (int) ((i - min) / (double) (max - min) * height);
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
            return image;
        }

        //
        // visualizeEvent
        //
        // will make a small golden sphere at the event point
        //
        public static void visualizeEvent(Event event, Vector3D direction, LinkedTransferQueue<Node> g) {
            if (MonteCarloSimulation.visualLimitReached) {
                return;
            }

            //  if (event.code != Event.Code.Gone) {
            String color;
            float size = 0.2f;
            double jitter = 0.1;

            switch (event.code) {
                case ExitEntry:
                    Vector3D position = event.position;
                    Util.Graphics.drawSphere(g, position.add(direction.scalarMultiply(-jitter)), size, "red");
                    Util.Graphics.drawSphere(g, position.add(direction.scalarMultiply(jitter)), size, "green");

                    return;
                case Entry:
                    color = "green";
                    size *= 1;
                    break;
                case Exit:
                    color = "red";
                    size *= 1;
                    jitter *= -1;
                    break;
                case Scatter:
                    color = "yellow";
                    size *= 0.5;
                    jitter = 0;
                    break;
                case EmergencyExit:
                    color = "purple";
                    size *= 2;
                    jitter = 0;
                    break;
                case Capture:
                    color = "lightblue";
                    size *= 0.5;
                    jitter = 0;
                    break;
                default:
                    color = "black";
                    break;
            }
            Vector3D position = event.position;
            if (direction != null) {
                position = position.add(direction.scalarMultiply(-jitter));
            }

            Util.Graphics.drawSphere(g, position, size, color);
        }

        //
        // visualizeEvent
        //
        // same but without offset direction
        //
        public static void visualizeEvent(Event event, LinkedTransferQueue<Node> g) {
            visualizeEvent(event, null, g);
        }

        public static void drawCoordSystem(Group g) {
            LinkedTransferQueue<Node> q = new LinkedTransferQueue<>();
            Util.Graphics.drawSphere(q, Vector3D.ZERO, 1, "red");
            Util.Graphics.drawLine(q, new Vector3D(-2000, 0, 0), new Vector3D(2000, 0, 0), 0.1, Color.CYAN);
            Util.Graphics.drawLine(q, new Vector3D(0, -2000, 0), new Vector3D(0, 2000, 0), 0.1, Color.YELLOW);
            Util.Graphics.drawLine(q, new Vector3D(0, 0, -2000), new Vector3D(0, 0, 2000), 0.1, Color.RED);
            q.drainTo(g.getChildren());
        }

    }

}
