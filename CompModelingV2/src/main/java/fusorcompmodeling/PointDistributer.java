/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author guberti
 */
public class PointDistributer {
    static Map<Integer, Double> totalSurfaceArea;
    static Map<Integer, double[][]> cumulativeSurfaceAreas;
    // cumulative surface areas
    // is actually two double[][]
    // one is for a charge of 1, another for a charge of -1
    // then the double[][] is two arrays
    // the first is the cumulative surface areas (for that charge)
    // the second is the indexes of the parts in question
    
    public PointDistributer() {
    }
    
    public static Point[] shakeUpPoints(List<GridComponent> parts, int pointsPerCharge, int endCon) {
        totalSurfaceArea = new HashMap<>();
        totalSurfaceArea.put(-1, totalSurfaceArea(parts, -1));
        totalSurfaceArea.put(1, totalSurfaceArea(parts, 1));
        System.out.println("-1 tot surface area: " + totalSurfaceArea.get(-1));
        System.out.println("+1 tot surface area: " + totalSurfaceArea.get(1));
        calculateCumSurfaceAreas(parts);
        // why is it points per charge when its possible there are no shapes with a charge of -1
        //nevermind that's impossible
        // the calculate surface areas must be wrong for the negatives at least
        // or putting positives with negatives
        // or who knows what
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
        cumulativeSurfaceAreas = new HashMap<>();
        
        for (int charge : charges) {
            int partsIndex = 0;
            double cSAs [][] = new double[2][countPointsForCharge(parts, charge)];
            for (int i = 0; i < cSAs[0].length; i++) {
                // First, we must get the first part with the correct charge
                while (parts.get(partsIndex).charge != charge) {
                    partsIndex++;
                }
                cSAs[0][i] = parts.get(partsIndex).surfaceArea;
                
                if (i != 0) {
                    cSAs[0][i] += cSAs[0][i - 1];
                }
                
                cSAs[1][i] = partsIndex;
                
                partsIndex++;
            }
            cumulativeSurfaceAreas.put(charge, cSAs);
        }
    }
    
    private static int chargeToIndex(int charge) {
        if (charge == -1) {return 0;}
        return charge;
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
        // for reference, THIS ISN'T BROKEN
        // It's just that for some reason
        // a charge of -1
        // has an empty array (at the time)
        // and by extension, no total surface area
        // I wonder why there are no surfaces with a charge of -1
        int partIndex = getValAbove(rand, charge); // This is broken
        
        GridComponent g = parts.get(partIndex);
        Point p = g.getRandomPoint(generator);
        return p;
    }
    //returns the index in the cumulative surface areas of
    //the grid component that is just above the random number
    // simply, this is a binary search that returns the number above rand
    private static int getValAbove(double rand, int charge) {
        // array of the surface areas of charge "charge"
        double[] arr = cumulativeSurfaceAreas.get(charge)[0];

        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = (high + low) / 2;
            
            if (arr[mid] >= rand && (mid == 0 || arr[mid-1] < rand)) { // If we're at the right location
                return (int) cumulativeSurfaceAreas.get(charge)[1][mid]; 
            } else if (arr[mid] < rand) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        
        System.out.println("hoodsijfoi");
        return -1; // Should never get here
    }
    
    public static double totalSurfaceArea(List<GridComponent> parts, int charge) {
        double surfaceArea = 0;
        
        System.out.println("Supposed size: " + parts.size());
        double testSize = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).charge == charge) {
                testSize++;
                surfaceArea += parts.get(i).surfaceArea;
            }
        }
        System.out.println("\n");
        System.out.print(charge);
        System.out.println(" Charge tot parts: " + testSize);

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
            Point newPoint = getRandomPoint(parts, points[i].charge);
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
}
