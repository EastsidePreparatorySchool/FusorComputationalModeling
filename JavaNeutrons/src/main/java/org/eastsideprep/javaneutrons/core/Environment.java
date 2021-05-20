package org.eastsideprep.javaneutrons.core;

public class Environment {

    public static final double limit = 2000; // 2000cm = 20m
    private static Environment instance;
    public TallyOverEV counts;
    private long totalEscapes;
    private long totalCaptures; // not really part of environment, but counted here

    public Environment() {
        reset();
    }

    public static Environment getInstance() {
        if (Environment.instance == null) {
            Environment.instance = new Environment();
        }
        return Environment.instance;
    }

    public static void recordEscape(double energy) {
        Environment e = Environment.getInstance();
        e.counts.record(1, energy);
        synchronized (e) {
            e.totalEscapes++;
        }
    }

    public static void recordCapture() {
        Environment e = Environment.getInstance();
        synchronized (e) {
            e.totalCaptures++;
        }
    }

    public void reset() {
        counts = new TallyOverEV(1e7, 100);
        totalEscapes = 0;
        totalCaptures = 0;
    }

    public static double getEscapeProbability() {
        Environment e = Environment.getInstance();
        return e.totalEscapes / (double) (e.totalEscapes + e.totalCaptures);
    }

 
}
