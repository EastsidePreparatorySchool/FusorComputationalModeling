/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class CorrelatedTallyOverEV extends TallyOverEV {

    final Map<Particle, TallyOverEV> hMap = new HashMap<>();
    TallyOverEV sumSquares;
    double[][] covLog;
    double[][] covFlat;
    double[][] covLow;
    Util.LogLogTable sievertConversionTable;
    double totalSieverts;

    public CorrelatedTallyOverEV(Util.LogLogTable sievertConversionTable) {
        sumSquares = new TallyOverEV();
        covLog = new double[sumSquares.bins.length][sumSquares.bins.length];
        covFlat = new double[sumSquares.hFlat.bins.length][sumSquares.hFlat.bins.length];
        covLow = new double[sumSquares.hLow.bins.length][sumSquares.hLow.bins.length];
        this.sievertConversionTable = sievertConversionTable;
    }

    public CorrelatedTallyOverEV(double e, int bins, Util.LogLogTable sievertConversionTable) {
        super(e, bins);
        sumSquares = new TallyOverEV(e, bins);
        covLog = new double[sumSquares.bins.length][sumSquares.bins.length];
        covFlat = new double[sumSquares.hFlat.bins.length][sumSquares.hFlat.bins.length];
        covLow = new double[sumSquares.hLow.bins.length][sumSquares.hLow.bins.length];
        this.sievertConversionTable = sievertConversionTable;
    }

    public void record(Particle p, double value, double energy) {
        TallyOverEV h;
        synchronized (hMap) {
            h = hMap.get(p);
        }

        if (h == null) {
            synchronized (hMap) {
                h = new TallyOverEV();
                hMap.put(p, h);
            }
        }

        h.record(value, energy);
        p.fluences.add(this);
        
        // record sieverts
        double sieverts = this.sievertConversionTable.lookup(energy)*value;
        synchronized (this) {
            this.totalSieverts += sieverts;
        }
    }

    public void tally(Particle p) {
        TallyOverEV h;
        synchronized (hMap) {
            h = hMap.get(p);
        }

        if (h == null) {
            return;
        }

        synchronized (this) {
            this.add(h);
            this.hLow.add(h.hLow);
            this.hFlat.add(h.hFlat);
            sumSquares.addSquares(h);
            sumSquares.hLow.addSquares(h.hLow);
            sumSquares.hFlat.addSquares(h.hFlat);

            for (int i = 0; i < bins.length; i++) {
                for (int j = 0; j < bins.length; j++) {
                    covLog[i][j] += bins[i] * bins[j];
                }
            }

            for (int i = 0; i < hFlat.bins.length; i++) {
                for (int j = 0; j < hFlat.bins.length; j++) {
                    covFlat[i][j] += hFlat.bins[i] * hFlat.bins[j];
                }
            }

            for (int i = 0; i < hLow.bins.length; i++) {
                for (int j = 0; j < hLow.bins.length; j++) {
                    covLow[i][j] += hLow.bins[i] * hLow.bins[j];
                }
            }
        }

        synchronized (hMap) {
            hMap.remove(p);
        }
    }

    public XYChart.Series makeErrorSeries(String seriesName, double count, String scale) {
        XYChart.Series<String, Number> smu;
        XYChart.Series<String, Number> sss;
        switch (scale) {
            case "Log":
                smu = makeSeries(seriesName, count);
                sss = sumSquares.makeSeries(seriesName, count);
                break;
            case "Linear (all)":
                smu = hFlat.makeSeries(seriesName, count);
                sss = sumSquares.hFlat.makeSeries(seriesName, count);
                break;
            case "Linear (thermal)":
                smu = hLow.makeSeries(seriesName, count);
                sss = sumSquares.hLow.makeSeries(seriesName, count, LOW_VISUAL_LIMIT);
                break;
            default:
                //return hFlat.makeSeries(seriesName, count);
                return null;
        }

        // take the square root of every item and put it back in
        for (int i = 0; i < sss.getData().size(); i++) {
            Data<String, Number> dmu = smu.getData().get(i);
            Data<String, Number> dss = sss.getData().get(i);
            double mu = dmu.getYValue().doubleValue();
            double ss = dss.getYValue().doubleValue();
            //System.out.print(""+mu+"("+ss +","+count+") -> ");
            dss.setYValue(mu > 0 ? Math.sqrt((ss - (mu * mu)) / count) / mu : 0);
            //System.out.println(""+dss.getYValue());
        }

        return sss;
    }

    @Override
    public boolean equals(Object other) {
        // not null

        // instanceof CTOEV
        // compute chi^2
        return false;
    }

    public static String compareToRef(String input) {
        CorrelatedTallyOverEV hist2 = parseFromString(input);

        return "";
    }

    public static CorrelatedTallyOverEV parseFromString(String s) {
        CorrelatedTallyOverEV output = new CorrelatedTallyOverEV(null); //to fill
        String[] lines = s.split("\n"); //
        ArrayList<String> collection = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            collection.add(lines[i]);
        }
        String Title = collection.remove(0);
        System.out.println(Title);
        String NeutronCount = collection.remove(0);
        String BinCount = collection.remove(0);
        // assert that bincount is 250
        String BinSize = collection.remove(0);
        // assert that binsize if 0.001
        String FluencesCount = collection.remove(0);
        String Fluences = collection.remove(0);
        String CovarianceMatrixDesc = collection.remove(0);
        System.out.println(CovarianceMatrixDesc);
        //the rest of collection should be the Covariance Matrix

        output.covLog = null;
        output.covFlat = null;

        // put values into low tally
        String[] fluencesStrings = Fluences.split(" ");
        for (int i = 0; i < output.hLow.bins.length; i++) {
            double value = Double.parseDouble(fluencesStrings[i]);
            output.hLow.bins[i] = value;
        }

        // construct covariance matrix
        // for every every row, parse it
        for (int i = 0; i < output.hLow.bins.length; i++) {
            String[] covStrings = collection.remove(0).split(" ");
            // for this row, put all the values into the matrix
            for (int j = 0; j < output.hLow.bins.length; j++) {
                double value = Double.parseDouble(fluencesStrings[i]);
                output.covLow[i][j] = value;
            }

        }

        System.gc();
        return output;
    }

    public static void works() {
        System.out.println("YES");
    }
}
