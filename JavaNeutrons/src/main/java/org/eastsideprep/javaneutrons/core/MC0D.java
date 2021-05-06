package org.eastsideprep.javaneutrons.core;

import javafx.scene.chart.Chart;
import org.eastsideprep.javaneutrons.shapes.Cuboid;

abstract public class MC0D extends MonteCarloSimulation {

    abstract public void init();

    abstract public void before();

    abstract public void run(Particle n);

    abstract public void after();

    public MC0D() {
        super();
        Part pseudoPart = new Part("Custom", new Cuboid(1), "HighVacuum");
//        this.assembly.add(pseudoPart);
        this.namedParts.put("Custom", pseudoPart);
//        if (pseudoPart.material.name != null) {
//            this.materials.put(pseudoPart.material.name, pseudoPart.material);
//        }
    }

    @Override
    public void preProcess() {
        this.before();
    }

    @Override
    public void postProcess() {
        this.after();
    }

    @Override
    public void simulateParticle(Particle n, Material m, boolean count) {
        if (n == null) {
            return;
        }

        this.run(n);
        if (count) {
            completed.incrementAndGet();
        }
    }

    public Chart makeCustomChart(String series, String scale) {
        return null;
    }

    @Override
    public Chart makeChart(String detector, String series, String scale, String type) {
        if (detector != null && detector.equals("Custom")) {
            return makeCustomChart(series, scale);
        }
        return super.makeChart(detector, series, scale, type);

    }

}
