/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.javaneutrons.assemblies;

import org.eastsideprep.javaneutrons.core.LogEnergyEVHistogram;

/**
 *
 * @author gunnar
 */
public class Environment {

    public static final double limit = 1000; // 1000cm = 10m
    private static Environment instance;
    public LogEnergyEVHistogram counts;
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
        counts = new LogEnergyEVHistogram();
        totalEscapes = 0;
        totalCaptures = 0;
    }

    public static double getEscapeProbability() {
        Environment e = Environment.getInstance();
        return e.totalEscapes / (double) (e.totalEscapes + e.totalCaptures);
    }

 
}
