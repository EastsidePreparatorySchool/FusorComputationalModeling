/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author guberti
 */
public class PointDistributer {
    static Map<Integer, List<GridComponent>> reachableShapes;
    static Map<Integer, Double> totalSurfaceArea;
    static Map<Integer, double[][]> cumulativeSurfaceAreas;
    
    public PointDistributer() {
    }

    public static Point[] shakeUpPoints(List<GridComponent> parts, int pointsPerCharge, int endCon) {
        reachableShapes = new HashMap<>();
        reachableShapes.put(-1, getReachableShapes(-1, parts));
        reachableShapes.put(1, getReachableShapes(1, parts));

        totalSurfaceArea = new HashMap<>();
        totalSurfaceArea.put(-1, totalSurfaceArea(parts, -1));
        totalSurfaceArea.put(1, totalSurfaceArea(parts, 1));
        
        calculateCumSurfaceAreas(parts);
        
        Point[] points = distributePoints(parts, pointsPerCharge);
        int reps = 0;
        while (reps < endCon) {
            int changesMade = balanceCharges(points, parts);
            reps++;
            System.out.println("Shook up points, " + reps + "/" + endCon + " shakes complete, made " + changesMade + " changes");
        }
        return points;
    }
    public static void calculateCumSurfaceAreas(List<GridComponent> parts) {
        int[] charges = {-1, 1};
        int partsIndex = 0;
        
        for (int charge : charges) {
            double cSAs [][] = new double[2][countPointsForCharge(parts, charge)];
            
            for (int i = 0; i < cSAs.length; i++) {
                // First, we must get the first part with the correct charge
                while (parts.get(partsIndex).charge != charge) {
                    partsIndex++;
                }
                cSAs[0][i] = parts.get(partsIndex).surfaceArea;
                
                if (i != 0) {
                    cSAs[0][i] += cSAs[0][i - 1];
                }
                cSAs[1][i] = partsIndex;
            }
            cumulativeSurfaceAreas.put(charge, cSAs);
        }
    }
    
    private static int countPointsForCharge(List<GridComponent> parts, int charge) {
        int count = 0;
        for (GridComponent p : parts) {
            if (p.charge == charge) {count++;}
        }
        return count;
    }
    
    public static Point[] distributePoints(List<GridComponent> parts, int pointsForEachCharge) {
        Point[] totalPoints = new Point[pointsForEachCharge*2];
        Random newRand = new Random();
        for (int i = 0; i < pointsForEachCharge*2; i += 2) {
            totalPoints[i] = getRandomPoint(parts, -1);
            totalPoints[i + 1] = getRandomPoint(parts, 1);
        }
        return totalPoints;
    }

    public static Point getRandomPoint(List<GridComponent> parts, int charge) {
        double area = totalSurfaceArea.get(charge);
        Random generator = new Random();

        double rand = generator.nextDouble() * area;
        int partIndex = getValAbove(rand, charge);
        return parts.get(partIndex).getRandomPoint(generator);
    }

    private static int getValAbove(double rand, int charge) {
        // Basic binary search, modified from algoritm for efield generation
        int low = 0;
        int high = cumulativeSurfaceAreas.get(charge)[0].length - 1;
        while (low < high) {
            int mid = (high + low) / 2;
            double val = cumulativeSurfaceAreas.get(charge)[0][mid];
            if (rand > val) {
                low = mid;
            } else if (rand < val) {
                high = mid - 1;
            }
        }
        return low;
    }
    
    public static double totalSurfaceArea(List<GridComponent> parts, int charge) {
        double surfaceArea = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).charge == charge) {
                surfaceArea += parts.get(i).surfaceArea;
            }
        }
        return surfaceArea;
    }

 
   
    public static double electricPotential(Point[] points, Point comparePoint) {
        double potential = 0;
        for (int i = 0; i < points.length; i++) {
            if (!points[i].equals(comparePoint)) {

                potential += (points[i].charge * (1 / distanceCalculator(points[i], comparePoint)));

            }
        }
        return potential;
    }

    public static double distanceCalculator(Point a, Point b) {

        // I'm using the slower math.pow here, because it doesn't seem to make a big impact on performance
        // However, a more thourough investigation is required here
        double distSquared = (Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2));
        return Math.sqrt(distSquared);
    }

    public static int balanceCharges(Point[] points, List<GridComponent> parts) {
        int changes = 0;
        
        for (int i = 0; i < points.length; i++) {            
            Point newPoint = getRandomPoint(reachableShapes.get(points[i].charge), points[i].charge);
            double currentEP = electricPotential(points, points[i]);

            double newEP = electricPotential(points, newPoint);
            if (points[i].charge == 1) {
                if (newEP < currentEP) {
                    changes++;
                    points[i] = newPoint;
                } else if (newEP > currentEP) {
                    points[i].EP = currentEP;
                }

            } else if (newEP > currentEP) {
                changes++;
                points[i] = newPoint;
            } else if (newEP < currentEP) {
                points[i].EP = currentEP;
            }
        }
        return changes;
    }

    public static List<GridComponent> getReachableShapes(int charge, List<GridComponent> parts) {
        List<GridComponent> ReachableShapes = new ArrayList<>();
        int i = 0;
        while (i < parts.size()) {
            if (charge == parts.get(i).charge) {
                ReachableShapes.add(parts.get(i));
            }
            i++;
        }
        return ReachableShapes;
    }
}
