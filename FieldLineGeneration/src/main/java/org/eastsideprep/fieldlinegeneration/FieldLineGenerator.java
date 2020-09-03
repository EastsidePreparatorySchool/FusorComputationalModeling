/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.fieldlinegeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 *
 * @author subif
 */
public class FieldLineGenerator {

    EField eField;

    LinkedList<LinkedList<Vector>> fieldLines;
    LinkedList<Vector> mostRecentPoints; // old points for termination rule 3
    GridBox[][] grid2D; // we'll be using binary searches to determine which box
    // something is in (like the triangles funnily enough).  One binary search
    // for each dimension, which is why
    // knowing from the getgo
    // if you're 2d or not is so helpful in terms of saving memory
    GridBox[][][] grid3D;
    List<GridBox> small2D;
    List<GridBox> small3D;
    Double threshold;
    int minimumIterations = 50; // the minimum number of iterations a field line gets
    // before it has to move threshold distance
    int minimumSteps = 5; // the minimum steps a field line gets before it is included
    // in the queue of most recent points
    int numberOfRecentPoints = 10000; // number of vectors stored in mostRecentPoints at a time
    Double maximumCloseness = 0.000001; // The closest a streamline can get to other points
    // on the most recent points list when crossing into a new grid box
    GridBox bounds;
    Double[] xRanges;// gives the lower bound of every grid box
    Double[] yRanges;
    Double[] zRanges;
    Double xGap;
    Double yGap;
    Double zGap;
    Double stepSize;
    int missingDimension; // which (if any) dimension is this field line not going into
    // 1 = x, 2=y, 3=z, 0= null
    int checkerBoard; // if it's 0, all are available from start,
    // if 1, then every other is available, 2 every 2, etc.

    /**
     *
     * @param eField
     * @param bounds
     * @param stepSize
     * @param threshold
     * @param numberOfGaps
     * @param checkerBoard
     */
    FieldLineGenerator(EField eField, GridBox bounds, Double stepSize,
            Double threshold, int numberOfGaps, int checkerBoard) {

        if (checkerBoard == 0) {
            checkerBoard++;
        }

        this.bounds = bounds;
        this.eField = eField;
        this.stepSize = stepSize;
        this.threshold = threshold;
        this.checkerBoard = checkerBoard;
        missingDimension = 0;
        if (bounds.c1.x.equals(bounds.c2.x)) {
            missingDimension = 1;
            yRanges = fillWithMultiples(numberOfGaps, bounds.c1.y, bounds.c2.y);
            zRanges = fillWithMultiples(numberOfGaps, bounds.c1.z, bounds.c2.z);
            yGap = (bounds.c2.y - bounds.c1.y) / numberOfGaps;
            zGap = (bounds.c2.z - bounds.c1.z) / numberOfGaps;
            Double x = bounds.c1.x + 0.0;
            Double y = bounds.c1.y + 0.0;
            Double z = bounds.c1.z + 0.0;
            grid2D = new GridBox[numberOfGaps][numberOfGaps];
            for (int i = 0; i < numberOfGaps; i++) {
                for (int j = 0; j < numberOfGaps; j++) {
                    grid2D[i][j] = new GridBox(new Vector(x, y, z), new Vector(x, y + yGap, z + zGap));
                    z += zGap;
                    if ((checkerBoard > 1) && ((i + (j % checkerBoard))) % checkerBoard == 1) {
                        grid2D[i][j].flag = true;
                    }
                }
                z = bounds.c1.z + 0.0;
                y += yGap;

            }

        } else if (bounds.c1.y.equals(bounds.c2.y)) {
            missingDimension = 2;
            xRanges = fillWithMultiples(numberOfGaps, bounds.c1.x, bounds.c2.x);
            zRanges = fillWithMultiples(numberOfGaps, bounds.c1.z, bounds.c2.z);
            xGap = (bounds.c2.x - bounds.c1.x) / numberOfGaps;
            zGap = (bounds.c2.z - bounds.c1.z) / numberOfGaps;
            Double x = bounds.c1.x + 0.0;
            Double y = bounds.c1.y + 0.0;
            Double z = bounds.c1.z + 0.0;
            grid2D = new GridBox[numberOfGaps][numberOfGaps];
            for (int i = 0; i < numberOfGaps; i++) {
                for (int j = 0; j < numberOfGaps; j++) {

                    grid2D[i][j] = new GridBox(new Vector(x, y, z), new Vector(x + xGap, y, z + zGap));
                    z += zGap;
                    if ((checkerBoard > 1) && ((i + (j % checkerBoard))) % checkerBoard == 1) {
                        grid2D[i][j].flag = true;
                    }
                }
                z = bounds.c1.z + 0.0;
                x += xGap;
            }
        } else if (bounds.c1.z.equals(bounds.c2.z)) {
            missingDimension = 3;
            yRanges = fillWithMultiples(numberOfGaps, bounds.c1.y, bounds.c2.y);
            xRanges = fillWithMultiples(numberOfGaps, bounds.c1.x, bounds.c2.x);
            xGap = (bounds.c2.x - bounds.c1.x) / numberOfGaps;
            yGap = (bounds.c2.y - bounds.c1.y) / numberOfGaps;
            Double x = bounds.c1.x + 0.0;
            Double y = bounds.c1.y + 0.0;
            Double z = bounds.c1.z + 0.0;
            grid2D = new GridBox[numberOfGaps][numberOfGaps];
            for (int i = 0; i < numberOfGaps; i++) {
                for (int j = 0; j < numberOfGaps; j++) {
                    grid2D[i][j] = new GridBox(new Vector(x, y, z), new Vector(x + xGap, y + yGap, z));
                    y += yGap;
                    if ((checkerBoard > 1) && ((i + (j % checkerBoard))) % checkerBoard == 1) {
                        grid2D[i][j].flag = true;
                    }
                }
                y = bounds.c1.y + 0.0;
                x += xGap;
            }
        } else {
            xRanges = fillWithMultiples(numberOfGaps, bounds.c1.x, bounds.c2.x);
            yRanges = fillWithMultiples(numberOfGaps, bounds.c1.y, bounds.c2.y);
            zRanges = fillWithMultiples(numberOfGaps, bounds.c1.z, bounds.c2.z);
            xGap = (bounds.c2.x - bounds.c1.x) / numberOfGaps;
            yGap = (bounds.c2.y - bounds.c1.y) / numberOfGaps;
            zGap = (bounds.c2.z - bounds.c1.z) / numberOfGaps;

            Double x = bounds.c1.x + 0.0;
            Double y = bounds.c1.y + 0.0;
            Double z = bounds.c1.z + 0.0;
            grid3D = new GridBox[numberOfGaps][numberOfGaps][numberOfGaps];
            for (int i = 0; i < numberOfGaps; i++) {
                for (int j = 0; j < numberOfGaps; j++) {
                    for (int k = 0; k < numberOfGaps; k++) {
                        grid3D[i][j][k] = new GridBox(new Vector(x, y, z), new Vector(x + xGap, y + yGap, z + zGap));
                        z += yGap;
                        if ((checkerBoard > 1) && ((i + (j % checkerBoard)
                                + (k % checkerBoard))) % checkerBoard == 1) {
                            grid3D[i][j][k].flag = true;
                        }
                    }
                    z = bounds.c1.z + 0.0;
                    y += yGap;
                }
                y = bounds.c1.y + 0.0;
                x += xGap;
            }
        }
    }

    public LinkedList<LinkedList<Vector>> drawFieldLines() {
        fieldLines = new LinkedList<>();
        mostRecentPoints = new LinkedList<>();
        if (missingDimension == 0) {
            for (int i = 0; i < grid3D.length; i++) {
                for (int j = 0; j < grid3D[i].length; j++) {
                    for (int k = 0; k < grid3D[i][j].length; k++) {
                        System.out.println("starting line");
                        LinkedList<Vector> fieldLine = drawLine(grid3D[i][j][k], stepSize);
                        if (fieldLine != null) {
                            System.out.println("line finished");
                            fieldLines.add(fieldLine);
                        } else {
                            System.out.println("line canceled");
                        }
                    }

                }

            }
        } else {
            for (int i = 0; i < grid2D.length; i++) {
                for (int j = 0; j < grid2D[i].length; j++) {
                    System.out.println("starting line");
                    LinkedList<Vector> fieldLine = drawLine(grid2D[i][j], stepSize);
                    if (fieldLine != null) {
                        System.out.println("line finished");
                        fieldLines.add(fieldLine);
                    } else {
                        System.out.println("line canceled");
                    }
                }

            }
        }
        return fieldLines;

    }

    // draw line draws a field line
    public LinkedList<Vector> drawLine(GridBox firstGridBox, Double stepSize) {
        // field lines are drawn in three stages
        // first, start: finding out if their start location is viable
        // second, loop: make incremental steps following the electric field
        // third, termination: if any of the termination conditions are met stop
        if (firstGridBox.flag || firstGridBox.fieldLine || firstGridBox.dArrow) {
            System.out.println("Box unavailable");
            return null;

        }
        Vector start = firstGridBox.findCenter();
        GridBox lastGridBox = firstGridBox.clone();

        LinkedList<Vector> fieldLine = new LinkedList<>();
        Queue<Vector> recentIterations = new LinkedList<>();
        Queue<Vector> recentSteps = new LinkedList<>(); // for the recent steps queue up top
        int steps = 0;
        int iterations = 0;
        fieldLine.add(start);
        lastGridBox.arrowInBounds(start);

        // step forward
        Vector next = step(start, stepSize);
        lastGridBox = terminationCheck(next, recentIterations, threshold, lastGridBox);

        while (lastGridBox != null) {
            steps++;
            iterations++;
            recentIterations.add(next);
            recentSteps.add(next);
            if (iterations > minimumIterations) {
                recentIterations.remove();
                iterations--;
            }
            if (steps > minimumSteps) {
                mostRecentPoints.add(recentSteps.remove());
                if (mostRecentPoints.size() > numberOfRecentPoints) {
                    mostRecentPoints.removeLast();
                }
                steps--;
            }
            fieldLine.add(next);
            next = step(next, stepSize);
            lastGridBox = terminationCheck(next, recentIterations, threshold, lastGridBox);
        }

        // step backward
        iterations = 0;
        lastGridBox = firstGridBox.clone();
        next = step(start, -stepSize);
        lastGridBox = terminationCheck(next, recentIterations, threshold, lastGridBox);
        while (lastGridBox != null) {
            iterations++;
            steps++;
            recentIterations.add(next);
            recentSteps.add(next);
            if (iterations > minimumIterations) {
                recentIterations.remove();
                iterations--;
            }
            if (steps > minimumSteps) {
                mostRecentPoints.add(recentSteps.remove());
                if (mostRecentPoints.size() > numberOfRecentPoints) {
                    mostRecentPoints.removeLast();
                }
                steps--;
            }
            fieldLine.addFirst(next);
            next = step(next, -stepSize);
            lastGridBox = terminationCheck(next, recentIterations, threshold, lastGridBox);
        }

        return fieldLine;

    }

// returns true if a location is "start-able" for a streamline
// Conditions include:
// 1. Location on the grid
// 2. Box of location doesn't have streamlines in it
// 3. Has not been flagged (miscellanious pre-calculation stuff)
// 3 is most ambiguous, but is generally something like
// "every other box is flagged" or something.
    public GridBox startAble(Vector start) {

        int[] theoretical = theoreticalBox(start);
        Queue<Vector> previous = new LinkedList<>();
        if (theoretical == null) {
            System.out.println("Not on grid");
            return null;
        }
        GridBox box;

        if (missingDimension == 1) {
            box = grid2D[theoretical[1]][theoretical[2]];
        } else if (missingDimension == 2) {
            box = grid2D[theoretical[0]][theoretical[2]];
        } else if (missingDimension == 3) {
            box = grid2D[theoretical[0]][theoretical[1]];
        } else {
            box = grid3D[theoretical[0]][theoretical[1]][theoretical[2]];
        }

        if (box.fieldLine || box.dArrow) {
            System.out.println("This box already has a streamline");
            return null;
        }

        if (box.flag) {
            System.out.println("This box has been flagged ahead of time");
            return null;
        }
        return box;
    }

    // returns true if a streamline should end
    public GridBox terminationCheck(Vector arrow, Queue<Vector> previous, Double threshold, GridBox lastGridBox) {
        // termination rule 1:
        // streamline leaves grid
        int[] theoretical = theoreticalBox(arrow);
        if (theoretical == null) {
            System.out.println("theoretical box failure; terminating");
            return null;
        }
        GridBox box;
        if (missingDimension == 1) {
            box = grid2D[theoretical[1]][theoretical[2]];
        } else if (missingDimension == 2) {
            box = grid2D[theoretical[0]][theoretical[2]];
        } else if (missingDimension == 3) {
            box = grid2D[theoretical[0]][theoretical[1]];
        } else {
            box = grid3D[theoretical[0]][theoretical[1]][theoretical[2]];
        }
        // termination rule 2:
        // streamline fails to move a minimum required distance
        // over a certain number of iterations
        if (previous.peek() != null) {
            if ((previous.peek().distanceTo(arrow) < threshold) && (previous.size() >= minimumIterations)) {
                System.out.println("Displacement too small: terminating");
                return null;
            }
        }

        if (previous.size() > minimumIterations) {
            previous.remove();
        }

        // termination rule 3:
        // Whenever a streamline enters a new box, coordinates of all other
        // streamlines, which are on a "circular list" (linked list?) are
        // compared to the present coordinates of the streamline (excluding
        // the previous x coordinates of this streamline) and if any point
        // is too close to this streamline, this streamline ends
        if (lastGridBox != box) { // different box entered
            for (Vector point : mostRecentPoints) {
                if (point.distanceTo(arrow) < maximumCloseness) {
                    System.out.println("arrow:" + arrow);
                    System.out.println("point: " + point);
                    System.out.println("distance: " + point.distanceTo(arrow));
                    System.out.println("too close to another streamline"
                            + " (possibly itself): terminating");
                    return null;
                }

            }
        }

        return box;

    }

    public Vector step(Vector start, Double stepSize) {

        if (missingDimension == 1) {
            return stepYZ(start, stepSize);
        } else if (missingDimension == 2) {
            return stepXZ(start, stepSize);
        } else if (missingDimension == 3) {
            return stepXY(start, stepSize);
        }
        return stepXYZ(start, stepSize);

    }

    // experimental method
    // may be prone to error
    public Vector stepXYZ(Vector start, Double stepSize) {
        Vector point = new Vector(start);
        Vector fPoint = eField.fieldAtPoint(start);
        Double norm = fPoint.norm();
        point.x += (fPoint.x / norm) * stepSize;
        point.y += (fPoint.y / norm) * stepSize;
        point.z += (fPoint.z / norm) * stepSize;
        return point;
    }

    public Vector stepXY(Vector start, Double stepSize) {
        Vector point = new Vector(start);
        Vector fPoint = eField.fieldAtPoint(start);
        // the reason why we use two seemingly different formulas for
        // looking at the math they end up the same formula (in 2D at least)
        Double dx = stepSize * (fPoint.x / fPoint.xyNorm());
        point.y += (fPoint.y / fPoint.x) * dx;
        point.x += dx;
        return point;
    }

    public Vector stepXZ(Vector start, Double stepSize) {
        Vector point = new Vector(start);
        Vector fPoint = eField.fieldAtPoint(start);
        Double dx = stepSize * (fPoint.x / fPoint.xzNorm());
        point.z += (fPoint.z / fPoint.x) * dx;
        point.x += dx;
        return point;

    }

    public Vector stepYZ(Vector start, Double stepSize) {

        Vector point = new Vector(start);
        Vector fPoint = eField.fieldAtPoint(start);
        Double dy = stepSize * (fPoint.y / fPoint.yzNorm());
        point.z += (fPoint.z / fPoint.x) * dy;
        point.y += dy;
        return point;

    }

    // gives the theoretical x y and z parameters in the 2d/3d array for grid box
    public int[] theoreticalBox(Vector location) {
        if (!bounds.arrowInBounds(location)) {
            return null;
        }
        int xSpot = 0;
        int ySpot = 0;
        int zSpot = 0;
        switch (missingDimension) {

            case 0:
                xSpot = binarySearch(xRanges, location.x);
                ySpot = binarySearch(yRanges, location.y);
                zSpot = binarySearch(zRanges, location.z);

                break;

            case 1:
                ySpot = binarySearch(yRanges, location.y);
                zSpot = binarySearch(zRanges, location.z);

                break;
            case 2:
                xSpot = binarySearch(xRanges, location.x);
                zSpot = binarySearch(zRanges, location.z);

                break;
            case 3:
                xSpot = binarySearch(xRanges, location.x);
                ySpot = binarySearch(yRanges, location.y);

                break;
            default:
                xSpot = binarySearch(xRanges, location.x);
                ySpot = binarySearch(yRanges, location.y);
                zSpot = binarySearch(zRanges, location.z);

                break;

        }

        int[] box = new int[3];
        box[0] = xSpot;
        box[1] = ySpot;
        box[2] = zSpot;
        return box;
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
                    if (d[leftEdge].compareTo(target) > 0) {
                        return rightEdge;
                    }
                }
                done = true;
                return rightEdge;
            } else if (middle == leftEdge) {
                if (first) {
                    first = false;
                    if (d[leftEdge].compareTo(target) < 0) {
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
                if (target.compareTo(d[leftEdge]) > 0) {
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

    /**
     *
     * @param numberOfMultiples
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public Double[] fillWithMultiples(int numberOfMultiples, Double lowerBound, Double upperBound) {
        Double[] m = new Double[numberOfMultiples];
        Double s = lowerBound;
        Double gap = (upperBound - lowerBound) / numberOfMultiples;
        for (int i = 0; i < numberOfMultiples; i++) {
            m[i] = s;
            s += gap;
        }
        return m;
    }
}
