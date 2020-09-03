package fusorcompmodeling;

import fusorcompmodeling.Atom;
import fusorcompmodeling.EFieldB;
import fusorcompmodeling.Point;
import fusorcompmodeling.Solver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author daman
 */
public class RungeKutta implements Solver {

    @Override
    public Atom moveForward(Point[] points, double timeStep, Atom a, EFieldB e) {
        
        VectorB g = StatsGen.FToAcc(points, a.position, a.mass, e.EFieldSum(points, a.position));
        double[] k1 = k1(timeStep, a, g);
        for (int i = 0; i < k1.length; i++) {
            k1[i] = k1[i] * (timeStep / 2);
        }
        double[] k2 = k2(k1, a);
        for (int i = 0; i < k2.length; i++) {
            k2[i] = k2[i] * (timeStep / 2);
        }
        double[] k3 = k3(k2, a);
        for (int i = 0; i < k2.length; i++) {
            k2[i] = k2[i] * timeStep;
        }
        double[] k4 = k4(k3, a);

        double[] finalCalc = calculateAverage(k1, k2, k3, k4, timeStep, a);

        a.position.x = finalCalc[0];
        a.position.y = finalCalc[1];
        a.position.z = finalCalc[2];
        a.Velocity.x = finalCalc[3];
        a.Velocity.y = finalCalc[4];
        a.Velocity.z = finalCalc[5];
        return a;
    }

    public double[] k1(double TS, Atom a, VectorB g) {
        double k1[] = {position(a.position.x, a.Velocity.x, g.x, TS),
            position(a.position.y, a.Velocity.y, g.y, TS),
            position(a.position.z, a.Velocity.z, g.z, TS),
            velocity(a.Velocity.x, g.x, TS),
            velocity(a.Velocity.y, g.y, TS),
            velocity(a.Velocity.z, g.z, TS)};
        return k1;
    }

    public double[] k2(double[] k1, Atom a) {
        double k2[] = {(a.position.x + k1[0]),
            (a.position.y + k1[1]),
            (a.position.z + k1[2]),
            (a.Velocity.x + k1[3]),
            (a.Velocity.y + k1[4]),
            (a.Velocity.x + k1[5])};
        return k2;
    }

    public double[] k3(double[] k2, Atom a) {
        double k3[] = {(a.position.x + k2[0]),
            (a.position.y + k2[1]),
            (a.position.z + k2[2]),
            (a.Velocity.x + k2[3]),
            (a.Velocity.y + k2[4]),
            (a.Velocity.x + k2[5])};
        return k3;

    }

    public double[] k4(double[] k3, Atom a) {
        double k4[] = {(a.position.x + k3[0]),
            (a.position.y + k3[1]),
            (a.position.z + k3[2]),
            (a.Velocity.x + k3[3]),
            (a.Velocity.y + k3[4]),
            (a.Velocity.x + k3[5])};
        return k4;

    }

    public double[] calculateAverage(double[] k1, double[] k2, double[] k3, double[] k4, double TS, Atom a) {
        for (int i = 0; i < k2.length; i++) {
            k2[i] = k2[i] * 2;
            k3[i] = k3[i] * 2;
        }
        double[] finalCalc = new double[6];
        for (int i = 0; i < k1.length; i++) {
            finalCalc[i] = k1[i] + k2[i] + k3[i] + k4[i];
        }
        for (int i = 0; i < finalCalc.length; i++) {
            finalCalc[i] = finalCalc[i] * (TS / 6);
        }
        finalCalc[0] = finalCalc[0] + a.position.x;
        finalCalc[1] = finalCalc[1] + a.position.y;
        finalCalc[2] = finalCalc[2] + a.position.z;
        finalCalc[3] = finalCalc[3] + a.Velocity.x;
        finalCalc[4] = finalCalc[4] + a.Velocity.y;
        finalCalc[5] = finalCalc[5] + a.Velocity.z;
        return finalCalc;
    }

    public static double position(double initialPos, double initialVel, double acc, double time) {
        return initialPos + initialVel * time + (0.5 * acc * (time * time));
    }

    public static double velocity(double initialVel, double acc, double time) {
        return initialVel + acc * time;
    }

}
