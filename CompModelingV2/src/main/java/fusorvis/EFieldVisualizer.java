/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorvis;

import fusorcompmodeling.*;
import java.util.Arrays;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

/**
 *
 * @author guberti
 */
public class EFieldVisualizer {
    int aW; // Array width
    int aH; // Array height
    
    double wU; // Width unit
    double hU; // Height unit
    
    double sW; // Slice width
    double sH; // Slice height
    
    int blockSideLength;
    PixelWriter w;
    Point[] points;
        
    final int[] xRange = {-20, 20};
    final int[] yRange = {-20, 20};
    final int[] zRange = {-5, 5};
    
    final int rangeStep = 1;
    
    double[] refVals;
    
    int l = ((xRange[1] - xRange[0])*(yRange[1] - yRange[0])*(zRange[1] - zRange[0])) / ((int) Math.pow(rangeStep, 3));
    
    public EFieldVisualizer(double sliceWidth, double sliceHeight, double cF, int blockSideLength, PixelWriter w, Point[] points) {
        System.out.println(EFieldB.kQ);
        this.aW = (int) ((int) sliceWidth * cF / blockSideLength);
        this.aH = (int) ((int) sliceHeight * cF / blockSideLength);

        this.wU = sliceWidth / this.aW;
        this.hU = sliceHeight / this.aH;
        
        this.sW = sliceWidth;
        this.sH = sliceHeight;
        
        this.points = points;
        this.blockSideLength = blockSideLength;
        this.w = w;
        
        refVals = new double[((xRange[1] - xRange[0])*(yRange[1] - yRange[0])*(zRange[1] - zRange[0])) / ((int) Math.pow(rangeStep, 3))];
        
        getEFieldRange();
        int foo = 1;
    }
    
    
    
    public double[][][] calcSlice(Rotate[] eFieldTransforms, Box eFieldSlice) {

        double[][][] fieldGrid = new double[aW][aH][3];
        
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                Point p = new Point((-(sW / 2) + i * wU), (-(sH / 2) + k * hU), 0);
                p = translateEFieldPixel(p, eFieldTransforms, eFieldSlice);
                
                VectorB efield = EFieldB.EFieldSum(points, p);
                
                fieldGrid[i][k][0] = efield.x;
                fieldGrid[i][k][1] = efield.y;
                fieldGrid[i][k][2] = efield.z;
            }
        }
        
        return fieldGrid;
    }
    
    public void getEFieldRange() {
        int jMult = (zRange[1] - zRange[0]) / rangeStep;
        int iMult = ((yRange[1] - yRange[0]) / rangeStep) * jMult;
        for (int i = xRange[0]; i < xRange[1]; i += rangeStep) {
            
            for (int j = yRange[0]; j < yRange[1]; j += rangeStep) {
                for (int k = zRange[0]; k < zRange[1]; k += rangeStep) {
                    int ind = (k - zRange[0]) / rangeStep;
                    ind += jMult * ((j - yRange[0]) / rangeStep);
                    ind += iMult * ((i - xRange[0]) / rangeStep);
                    
                    refVals[ind] = PointDistributer.electricPotential(points, new Point(i, j, k));
                }
            }
        }
        Arrays.sort(refVals);
    }
    
    public void renderRGBCodedVectors(Rotate[] eFieldTransforms, Box eFieldSlice) {
        
        double[][][] fieldGrid = calcSlice(eFieldTransforms, eFieldSlice);
        
        double[][] sorted = new double[3][aW * aH];
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                for (int j = 0; j < 3; j++) {
                    sorted[j][k * aW + i] = fieldGrid[i][k][j];
                    
                }
            }
        }
        
        for (int j = 0; j < 3; j++) {
            Arrays.sort(sorted[j]);
        }

        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                Color c = new Color(
                        toColor(indexOf(sorted[0], fieldGrid[i][k][0]), sorted[0].length),
                        toColor(indexOf(sorted[1], fieldGrid[i][k][1]), sorted[1].length),
                        toColor(indexOf(sorted[2], fieldGrid[i][k][2]), sorted[2].length),
                        1.0);
                
                drawBlock(i, k, c);
            }
        }
    }
    
    public void renderIntensities(Rotate[] eFieldTransforms, Box eFieldSlice) {
        double[][][] fG = calcSlice(eFieldTransforms, eFieldSlice);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        double[][] intens = new double[aW][aH];
        
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                intens[i][k] = Math.sqrt(Math.pow(fG[i][k][0], 2) + Math.pow(fG[i][k][1], 2) + Math.pow(fG[i][k][2], 2));
                min = Math.min(min, intens[i][k]);
                max = Math.max(max, intens[i][k]);
            }
        }
        
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                double gV = (intens[i][k] - min) / (max - min);
                
                Color c = new Color(gV, gV, gV, 1.0);
                
                drawBlock(i, k, c);
            }
        }   
    }
    
    public void renderIntensitiesContrasted(Rotate[] eFieldTransforms, Box eFieldSlice) {
        double[][][] fG = calcSlice(eFieldTransforms, eFieldSlice);
        
        double[] sorted = new double[aW * aH];
        double[][] monocolored = new double[aW][aH];
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                for (int j = 0; j < 3; j++) {
                    double val = Math.sqrt(Math.pow(fG[i][k][0], 2) + Math.pow(fG[i][k][1], 2) + Math.pow(fG[i][k][2], 2));
                    sorted[k * aW + i] = val;
                    monocolored[i][k] = val;                    
                }
            }
        }
        for (double item : sorted) {
            if (item < 0) {
                System.out.println("Negative!");
            }
        }
        Arrays.sort(sorted);
                
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                double gV = toColor(indexOf(sorted, monocolored[i][k]), sorted.length);
                
                Color c = new Color(gV, gV, gV, 1.0);
                
                drawBlock(i, k, c);
            }
        }
    }
    
    public void renderElectricPotential(Rotate[] eFieldTransforms, Box eFieldSlice) {
        double[][] fieldGrid = new double[aW][aH];

        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                Point p = new Point((-(sW / 2) + i * wU), (-(sH / 2) + k * hU), 0);
                p = translateEFieldPixel(p, eFieldTransforms, eFieldSlice);
                
                fieldGrid[i][k] = PointDistributer.electricPotential(points, p);
            }
        }
                
        for (int i = 0; i < aW; i++) {
            for (int k = 0; k < aH; k++) {
                double gV = toColor(closestIndex(refVals, fieldGrid[i][k]), refVals.length);
                
                Color c = new Color(gV, 0.0, 1.0 - gV, 1.0);
                
                drawBlock(i, k, c);
            }
        }
    }
    
    private void drawBlock(int i, int k, Color c) {
        for (int j = 0; j < blockSideLength; j++) {
            for (int l = 0; l < blockSideLength; l++) {
                w.setColor(i * blockSideLength + j, k * blockSideLength + l, c);
            }
        }
    }
    private double toColor(int index, int length) {
        // Return a val between 0.0 and 1.0
        return (double) index / (double) (length - 1);
    }
    
    private void reverse2DArr (double[][] arrs) {
        for (double[] arr : arrs) {
            revSingleArr(arr);
        }
        for (int i = 0; i < (int) arrs.length/2; i++) {
            double[] t = arrs[i];
            arrs[i] = arrs[(arrs.length - 1) - i];
            arrs[(arrs.length - 1) - i] = t;
        }
    }
    
    private void revSingleArr (double[] d) {
        for (int i = 0; i < (int) d.length/2; i++) {
            double t = d[i];
            d[i] = d[(d.length - 1) - i];
            d[(d.length - 1) - i] = t;
        }
    }
    
    private int indexOf(double[] arr, double item) {
        // Simple binary search
        int low = 0;
        int high = arr.length - 1;
        
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (item < arr[mid]) {
                high = mid - 1;
            } else if (item > arr[mid]) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1; // Will never get here
    }
    
    private int closestIndex(double[] arr, double item) {
        int low = 0;
        int high = arr.length - 1;
        
        double lastVal;
        int mid = -1;

        while (low <= high) {
        
            mid = (low + high) / 2;
            lastVal = arr[mid];
            
            if (item < lastVal) {
                high = mid - 1;
            } else if (item > lastVal) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return mid;
    }
    public Point translateEFieldPixel(Point p, Rotate[] eFieldTransforms, Box eFieldSlice) {
        Point r = new Point(p.x, p.y, p.z);
        double c = 1;

        r.y = r.y * Math.cos(eFieldTransforms[0].getAngle() * c) - r.z * Math.sin(eFieldTransforms[0].getAngle() * c);
        r.z = r.y * Math.sin(eFieldTransforms[0].getAngle() * c) + r.z * Math.cos(eFieldTransforms[0].getAngle() * c);

        r.z = r.z * Math.cos(eFieldTransforms[1].getAngle() * c) - r.x * Math.sin(eFieldTransforms[1].getAngle() * c);
        r.x = r.z * Math.sin(eFieldTransforms[1].getAngle() * c) + r.x * Math.cos(eFieldTransforms[1].getAngle() * c);

        r.x = r.x * Math.cos(eFieldTransforms[2].getAngle() * c) - r.y * Math.sin(eFieldTransforms[2].getAngle() * c);
        r.y = r.x * Math.sin(eFieldTransforms[2].getAngle() * c) + r.y * Math.cos(eFieldTransforms[2].getAngle() * c);

        assert !Double.isNaN(r.x);

        VectorB v = r.convertToSphericalCoordsExc();
        v.length *= eFieldSlice.getScaleX();

        Point t = v.convertRayToCartesian(v.length);

        assert !Double.isNaN(r.x);

        t.x += eFieldSlice.getTranslateX();
        t.y += eFieldSlice.getTranslateY();
        t.z += eFieldSlice.getTranslateZ();

        return t;
    }
    
}