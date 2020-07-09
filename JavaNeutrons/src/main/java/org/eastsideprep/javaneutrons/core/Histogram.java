package org.eastsideprep.javaneutrons.core;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class Histogram {

    int binsPerDecade = 10;
    double min;
    double max;
    double[] bins;
    boolean log;

    public Histogram(double min, double max, int bins, boolean log) {
        this.min = min;
        this.max = max;
        this.bins = new double[bins];
        this.log = log;
        //this.bins = new double[logMax - logMin + 1];
    }

    public Histogram() {
        this.min = -3;
        this.max = 7;
        this.bins = new double[(int)Math.ceil((max - min) * this.binsPerDecade)];
        this.log = true;
        //this.bins = new double[logMax - logMin + 1];
    }

//    public Histogram(boolean log) {
//        this.log = log;
//
//        if (log) {
//            this.min = -3;
//            this.max = 7;
//            this.bins = new double[(max - min) * this.binsPerDecade];
//        } else {
//            this.min = 25000;
//            this.max = 2525000;
//            this.bins = new double[(int)(this.max - this.min) / 25000];
//
//        }
//    }

    public void record(double value, double x) {
        int bin;

        if (this.log) {
            // take log in chosen base
            x = Math.log10(x);
        }
        x -= min;

        // find bin
        bin = (int) Math.ceil(x / (max - min) * (this.bins.length));

        // cut of stuff that is too small
        bin = Math.max(bin, 0);
        // cut off stuff that is too big
        bin = Math.min(bin, this.bins.length - 1);

        //System.out.println(""+this.hashCode()+": recording "+(x+min)+":"+value);
        synchronized (this) {
            this.bins[bin] += value;
        }
    }

    public XYChart.Series makeSeries(String seriesName) {

        return this.makeSeries(seriesName, 1.0);
    }

    public XYChart.Series makeSeries(String seriesName, double count) {
        //System.out.println("Retrieving series "+seriesName+":");
        XYChart.Series series = new XYChart.Series();
        ObservableList data = series.getData();
        series.setName(seriesName);

        // put in all the data
        double[] counts = new double[this.bins.length];

        synchronized (this) {
            System.arraycopy(this.bins, 0, counts, 0, counts.length);
        }

        //System.out.println("");
        //System.out.println(""+this.hashCode()+Arrays.toString(bins));
        for (int i = 0; i < bins.length; i++) {
            double x = min + i / ((double) bins.length) * (max - min);
            if (this.log) {
                x = Math.pow(10, x);
            }
            String tick = String.format("%6.3e", x);
            data.add(new XYChart.Data(tick, counts[i] / count));
            //System.out.println(tick + " " + String.format("%6.3e", counts[i] / count));
        }
        //System.out.println("");

        return series;
    }

}
