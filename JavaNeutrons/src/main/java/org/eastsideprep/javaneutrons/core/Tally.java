package org.eastsideprep.javaneutrons.core;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.MultivariateVectorOptimizer;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

public class Tally {

    int binsPerDecade = 10;
    double min;
    double max;
    double[] bins;
    boolean log;

    public Tally(double min, double max, int bins, boolean log) {
        this.min = min;
        this.max = max;
        this.bins = new double[bins + 2];
        this.log = log;
    }

    public Tally() {
        this(-3,7,100, true);
    }

    public void record(double value, double x) {
        int bin;

        if (this.log) {
            // take log in chosen base
            x = Math.log10(x);
        }

        if (x < min) {
            bin = 0;
        } else if (x > max) {
            bin = bins.length - 1;
        } else {
            x -= min;

            // find bin
            bin = (int) Math.ceil(x / (max - min) * (this.bins.length - 2));
        }

        synchronized (this) {
            this.bins[bin] += value;
//            if (this.bins[bin] > 1.e7) {
//                System.out.println("big guy at energy "+x);
//            }
        }
    }

    public XYChart.Series makeSeries(String seriesName) {

        return this.makeSeries(seriesName, 1.0);
    }

    public XYChart.Series makeSeries(String seriesName, double count) {

        return this.makeSeries(seriesName, count, 1e12);
    }

    public XYChart.Series makeSeries(String seriesName, double count, double limit) {
        //System.out.println("Retrieving series "+seriesName+":");
        XYChart.Series series = new XYChart.Series();
        ObservableList data = series.getData();
        series.setName(seriesName);
        String tick ="";

        // put in all the data
        double[] counts = new double[this.bins.length];

        synchronized (this) {
            System.arraycopy(this.bins, 0, counts, 0, counts.length);
        }

        //System.out.println("");
        //System.out.println(""+this.hashCode()+Arrays.toString(bins));
        if (counts[0] > 0) {
            tick = String.format("%6.3e", counts[0]);
            data.add(new XYChart.Data("< "+tick, counts[0] / count));
        }

        for (int i = 1; i < bins.length - 1; i++) {
            double x = min + i / ((double) bins.length - 2) * (max - min);

            if (this.log) {
                x = Math.pow(10, x);
            }

            tick = String.format("%6.3e", x);
            data.add(new XYChart.Data(tick, counts[i] / count));
            //System.out.println(tick + " " + String.format("%6.3e", counts[i] / count));
        }
        data.add(new XYChart.Data("> "+tick, counts[bins.length - 1] / count));
        //System.out.println("");

        return series;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean all) {
        StringBuilder sb = new StringBuilder(20000);
        // put in all the data
        double[] counts = new double[this.bins.length];

        synchronized (this) {
            System.arraycopy(this.bins, 0, counts, 0, counts.length);
        }

        //System.out.println("");
        //System.out.println(""+this.hashCode()+Arrays.toString(bins));
        if (counts[0] > 0 || all) {
            sb.append("<," + counts[0] + "\n");
        }

        for (int i = 1; i < bins.length - 1; i++) {
            double x = min + i / ((double) bins.length - 2) * (max - min);

            if (this.log) {
                x = Math.pow(10, x);
            }

            if (counts[i] > 0 || all) {
                String tick = String.format("%6.3e", x);
                sb.append(tick + "," + counts[i] + "\n");
            }

        }
        if (counts[bins.length - 1] > 0 || all) {
            sb.append(">," + counts[bins.length - 1] + "\n");
        }
        //System.out.println("");

        return sb.toString();
    }

    public XYChart.Series makeFittedSeries(String seriesName, ParametricUnivariateFunction f, double[] params, double count, double limit) {
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
            double x = (min + i / ((double) bins.length) * (max - min));
            if (x > limit) {
                break;
            }
            double yPred = f.value(x, params);
            String tick = String.format("%6.3e", x);
            data.add(new XYChart.Data(tick, yPred / count));
//            if (x > 2e-2 && x < 3e-2) {
//                System.out.println("predicted value x=" + x + " y=" + yPred);
//            }
            //System.out.println(tick + " " + String.format("%6.3e", counts[i] / count));
        }
        //System.out.println("");
        return series;
    }

    public double[] fitCurve(ParametricUnivariateFunction f, double[] guesses, double count) {

        MultivariateVectorOptimizer opt = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> cf = new CurveFitter<>(opt);

        ArrayList<Double> values = new ArrayList<>();

        // skip last bucket since it has the overflow
        for (int i = 0; i < bins.length - 1; i++) {
            double x = (min + i / ((double) bins.length) * (max - min));
            double y = bins[i];
            if (y == 0 || x == 0) {
                continue;
            }

            cf.addObservedPoint(x, y);
//            if (x > 2e-2 && x < 3e-2) {
//                System.out.println("observed value x=" + x + " y=" + y);
//            }

        }

        double[] beta = cf.fit(f, guesses);
        return beta;
    }

    public void mutateNormalizeBy(Tally other) {
        for (int i = 0; i < this.bins.length; i++) {
            if (other.bins[i] != 0) {
                bins[i] /= other.bins[i];
            }
        }
    }

    public void mutateClone(Tally other) {
        System.arraycopy(other.bins, 0, bins, 0, bins.length);
    }

    public Tally normalizeBy(Tally other) {
        Tally h = new Tally(this.min, this.max, this.bins.length, false);
        h.mutateClone(this);
        h.mutateNormalizeBy(other);
        return h;
    }

    public double getTotal() {
        synchronized (this) {
            return Arrays.stream(bins).sum();
        }
    }

    public void add(Tally hSource) {
        synchronized (this) {
            for (int i = 0; i < this.bins.length; i++) {
                this.bins[i] += hSource.bins[i];
            }
        }
    }

    public void addSquares(Tally hSource) {
        synchronized (this) {
            for (int i = 0; i < this.bins.length; i++) {
                this.bins[i] += hSource.bins[i] * hSource.bins[i];
            }
        }
    }
}
