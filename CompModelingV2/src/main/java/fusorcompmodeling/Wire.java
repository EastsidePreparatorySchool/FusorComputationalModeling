/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import Jama.*;

/**
 *
 * @author guberti
 */

/*
    A wire is a method of turning a JSON string into a full grid setup.
    It consists of a single wire that has bent parts and straight parts.
    It starts at a vector and then has a ordered list of bends and straight
    pieces.
*/

public class Wire {
    // All units in mm
    double wireradius;
    VectorB start;
    JSONArray bends;
    int charge;
    VectorB originalPlane;
    boolean flipVertical;

    public Wire (String json) {
        MathJSONObject obj = new MathJSONObject(json);
        
        wireradius = obj.getMath("radius");
        charge = obj.getInt("charge");
        flipVertical = obj.getBoolean("flip_vertical");
        
        MathJSONObject jsonStart = new MathJSONObject(obj.getJSONObject("start"));
        start = new VectorB(
                jsonStart.getMath("x"),
                jsonStart.getMath("y"),
                jsonStart.getMath("z"),
                jsonStart.getMath("phi"),
                jsonStart.getMath("theta"));
        
        originalPlane = new VectorB(
                jsonStart.getMath("planePhi"), 
                jsonStart.getMath("planeTheta"));
        
        bends = obj.getJSONArray("components");       
    }

    public boolean isComponent(String type) {
        String[] componentTypes = {"straight", "bend"};
        for (String componentType : componentTypes) {
            if (type.equals(componentType)) {
                return true;
            }
        }
        return false;
    }

    public List<GridComponent> getAsGridComponents() {        
        List<GridComponent> parts = new ArrayList<>();
        
        MathJSONObject lastObj = new MathJSONObject(); // Needs to be initialized or Netbeans will be sad
        
        VectorB currentPlane = originalPlane;
        System.out.println("Current plane: " + originalPlane.toString());
        
        for (int i = 0; i < bends.length(); i++) {
            VectorB s;
            MathJSONObject currentObj = new MathJSONObject(bends.getJSONObject(i));
            
            if (i == 0) {
                // If component is the first component in the array,
                // its starting props should be the same as the wire's
                // starting props
                
                s = start;
            } else {
                // Otherwise, the starting props should be equal
                // to the ending props of the last component in the array
                GridComponent lastGC = parts.get(parts.size() - 1);
                
                s = new VectorB();
               
                // If it is a straight part
                if ("straight".equals(lastObj.getString("type"))) {
                    
                    double h = lastObj.getMath("height");
                    
                    s.phi = lastGC.pos.phi;
                    s.theta = lastGC.pos.theta;
                    System.out.println("Rotation: " + s.phi + ", " + s.theta);
                    s.x = h * Math.sin(s.theta) * Math.cos(s.phi) + lastGC.pos.x;
                    s.z = h * Math.sin(s.theta) * Math.sin(s.phi) + lastGC.pos.z;
                    
                    // Y is up/down
                    s.y = h * Math.cos(s.theta) + lastGC.pos.y;
                    
                // If it is a curved part
                } else if ("bend".equals(lastObj.getString("type"))) {
                    double initialPhi = ((TorusSegment)(lastGC)).phi2 + 
                            ((TorusSegment)(lastGC)).phi3;
                    
                    double prelimX = lastGC.pos.x + Math.cos(initialPhi) * lastGC.radius;
                    
                    double prelimZ = lastGC.pos.z + Math.sin(initialPhi) * lastGC.radius;
                    
                    Point rotatablePoint = new Point(prelimX, lastGC.pos.y, prelimZ);
                                        
                    Point finalPoint = rotatablePoint.rotateAroundVector(lastGC.pos);
                    
                   /* // Now we must calculate a ray coming off of the torus segment
                    
                    // To do this, we will calculate an initial point coming
                    // off our initally calculated ray, and then we
                    // will rotate the point around lastGC.pos and get a new
                    // point, from which we will calculate the ray again
                    
                    Point initialRayPoint = new Point(
                            Math.cos(initialPhi) + finalPoint.x, 
                            finalPoint.y,
                            Math.sin(initialPhi) + finalPoint.z);
                    
                    System.out.println("Initial point: " + rotatablePoint.toString());
                    Point finalRayPoint = initialRayPoint.rotateAroundVector(lastGC.pos);
                    System.out.println("Final ray point: " + finalPoint.toString());
          
                    double rayTheta = Math.atan2(
                            finalRayPoint.z - initialRayPoint.z, 
                            finalRayPoint.x - initialRayPoint.x);
                    
                    double rayPhi = Math.asin(
                            (finalRayPoint.x - initialRayPoint.x)/rayTheta);*/
                   
                   // To calculate the starting direction, we must take the
                   // cross product of the current torus's center (lastGC.pos)
                   // with the vector created from finalPoint with the torus's
                   // center.
                   
                    Point lastGCDirection = lastGC.pos.convertRayToCartesian(1);
                    Point sliceDirection = new Point(
                            finalPoint.x - lastGC.pos.x,
                            finalPoint.y - lastGC.pos.y,
                            finalPoint.z - lastGC.pos.z);

                    VectorB d = lastGCDirection.crossProduct(sliceDirection).convertToSphericalCoords();
                    System.out.println("Vector dir: " + d.toString());
                    try {
                         if (lastObj.getBoolean("invert")) {
                             d.phi *= -1;
                             d.theta *= -1;
                         }
                    } catch (Exception e) {} // Nothing will occur if invert was not set

                    boolean flipdir = false;
                    
                    try {
                         if (lastObj.getBoolean("invertangle")) {
                             flipdir = true;
                         }
                    } catch (Exception e) {} // Nothing will occur if invertangle was not set
                    try {
                        if (lastObj.getBoolean("smartinver")) {
                            d.theta = Math.PI - d.theta;
                            d.phi += Math.PI;
                            System.out.println("Performed a smart inversion!");
                        }
                    } catch (Exception e) {} // Nothing will occur if flipdir was not set
                    
                    if (flipdir) {
                        d.phi += Math.PI;
                        d.theta += Math.PI;
                    }

                    // We're done now, stick all our stuff into one ray
                    s = new VectorB(finalPoint, d.phi, d.theta);
                    
                }
            }
            // Now that we have the ray our part must stem from,
            // we need to again treat torus segments and cylinders differently
            
            if ("straight".equals(currentObj.getString("type"))) {
                Cylinder c = new Cylinder(s, wireradius, currentObj.getMath("height"), charge, flipVertical);
                parts.add(c);
                lastObj = currentObj;
            } else if ("bend".equals(currentObj.getString("type"))) {
                System.out.println("Current direction for torus: " + s.phi + ", " + s.theta);

                double r1 = currentObj.getMath("radius");
                
                /* To find the center of our torus, we need to take the cross
                product of the starting vector and a vector perpendicular to the
                current plane, as if these vectors were positionless rays. This
                will give us a vector that is perpendicular to these two vectors,
                */
                
                // Cross products can only be taken of vectors in cartesian form
                Point sRay = s.convertRayToCartesian(r1); 
                Point pRay = currentPlane.convertRayToCartesian(r1);
                
                Point tRay = sRay.crossProduct(pRay);
                
                /*Now we need to set the length of this ray to the radius of
                the torus (the inner radius, not the width (i.e. r1, not r2)).
                Thhere are plenty of ways to do this, but we're going to convert
                the vector into spherical form, change its radius, and then
                convert back.
                
                While it may seem at first glance that we can just use
                tRay, since the lengths of sRay and pRay are both what we
                need the length of tRay to be, this is not the case. When you
                take the cross product of two vectors with lengths a and b, 
                the cross product has a length of ab. This means that we need
                to divide tRay by the radius of the current torus.
                */
                
                // We need to include the minus sign to invert the vector
                tRay.divideByLength(-r1);

                /* I used to believe that a lot of complicated things had to
                be done in order to determine the direction the torus segment
                was facing, but this is completely false. Because the torus
                needs to lie inside the current plane, the vector that runs
                through the torus is the same as the vector that runs through
                the current plane. Therefore, we can just use the current plane
                as our torus's direction. Simple!
                
                */
                
                VectorB segmentStartSpherical = new VectorB(
                        currentPlane.phi + Math.PI/2,
                        currentPlane.theta + Math.PI/2);
                
                /* Now, we need to calculate the angle between this segment start
                and the vector "s". We have a function for this, but it requires
                that both vectors be in cartesian form
                */
                
                // Radius of course does not matter for this conversion
                Point segmentStart = segmentStartSpherical.convertRayToCartesian(r1); 
                
                double angleToStart = sRay.getAngleBetweenVectors(segmentStart);

                try {
                   double a = currentObj.getMath("angletostart");
                   angleToStart = a;
                   } catch (Exception e) {} // Nothing will occur if invert was not set
                
                try {
                   if (currentObj.getBoolean("invertdegree")) {
                        angleToStart = Math.PI + currentObj.getMath("angle");
                        System.out.println("Inverted angle!");
                    }
                   } catch (Exception e) {} // Nothing will occur if invert was not set

                try {
                   if (currentObj.getBoolean("transangle")) {
                        tRay.x -= 2 * currentObj.getMath("radius");
                        System.out.println("Translated angle!");
                    }
                   } catch (Exception e) {} // Nothing will occur if invert was not set
                // Combine all our data for the position into a single vector
                // This will be fed right into our torus segment untouched
                
                VectorB tP = new VectorB(
                        tRay.x + s.x,
                        tRay.y + s.y,
                        tRay.z + s.z,
                        currentPlane.phi,
                        currentPlane.theta);
                                
                final TorusSegment tS = new TorusSegment(tP, r1, angleToStart,
                        currentObj.getMath("angle"), wireradius, charge, flipVertical);
                System.out.println("Rotation attributes: " + tP.phi + ", " + angleToStart);
                parts.add(tS);
                
                lastObj = currentObj;
                
            } else if ("planeredef".equals(currentObj.getString("type"))) {
                currentPlane = new VectorB(currentObj.getMath("phi"), currentObj.getMath("theta"));
            }
        }
        
        return parts;
    }
}
