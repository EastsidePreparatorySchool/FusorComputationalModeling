/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class CorrelatedTallyOverEV extends TallyOverEV {

    final Map<Particle, TallyOverEV> hMap = new HashMap<>();
    final TallyOverEV sumSquares = new TallyOverEV();
    double[][] covLog;
    double[][] covFlat;
    double[][] covLow;

    public CorrelatedTallyOverEV() {
        covLog = new double[sumSquares.bins.length][sumSquares.bins.length];
        covFlat = new double[sumSquares.hFlat.bins.length][sumSquares.hFlat.bins.length];
        covLow = new double[sumSquares.hLow.bins.length][sumSquares.hLow.bins.length];
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
        CorrelatedTallyOverEV histogram2 = parseFromString(input);
        String x= compareToRef(histogram2);
        return x;
    }
    public static String compareToRef(CorrelatedTallyOverEV hist1){
        
        String x = "Comparing the fluences of the "+"currently selected part"+" and "+Title;
        return"";
    }
    
    static String Title;
    public static CorrelatedTallyOverEV parseFromString (String s){ 
        CorrelatedTallyOverEV output = new CorrelatedTallyOverEV(); //to fill
        String[] words = s.split("\n"); //
        ArrayList<String> collection = new ArrayList<> ();
        for (int i = 0; i < words.length; i++) {
            collection.add(words[i]);
        }
        Title = collection.remove(0);
        System.out.println(Title);
        
        String NeutronCount_pre = collection.remove(0);
        String[] NeutronCountArray= NeutronCount_pre.split(" ");
        String Neutron_String = NeutronCountArray[0];
        int neutronCount = Integer.valueOf(Neutron_String);
        
        
        String BinCount_pre = collection.remove(0);
        String[] BinCountArray= BinCount_pre.split(" ");
        String BinCount_String = BinCountArray[0];
        int binCount = Integer.valueOf(BinCount_String);
        
        
        String BinSizeandInfo = collection.remove(0);
        String[] BinInfo = BinSizeandInfo.split(" ");
        String BinSize_String = BinInfo[0];
        String BinType = BinInfo[1];
        double binSize = Double.parseDouble(BinSize_String);
        
        String fluencesCount = collection.remove(0);
        String[] fc = BinSizeandInfo.split(" ");
        String FluenceCount_String = fc[0];
        int fluenceCount = Integer.valueOf(FluenceCount_String);
        
        //conditions
//        if(binSize!=0.001||){
//        
//        }
//        
        
        String Fluences = collection.remove(0);
        String[] FluencesArray_String = Fluences.split(" ");
        double[] FluencesArray = new double[FluencesArray_String.length];
        for (int i = 0; i < FluencesArray.length; i++) {
            FluencesArray[i]= Double.parseDouble(FluencesArray_String[i]);
        }
        
        String CovarianceMatrixDesc = collection.remove(0);
        System.out.println(CovarianceMatrixDesc);
        System.gc();
        //the rest of collection should be the Covariance Matrix
        
        
        /** Convert String to Java Matrix here */
        
        
        
        
        /** ToDo: Parsing the individual Strings. Checking that it works. Making Matrix in optimal way */
        //Below we make the CorrelatedTallyOverEV
        
        
        return output;
    }
    public static void works(){
        System.out.println("YES");
    }
}
