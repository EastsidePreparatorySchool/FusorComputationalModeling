/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import static fusorcompmodeling.PointDistributer.electricPotential;

/**
 *
 * @author sfreisem-kirov
 */
public class StatsGen {
    public static double avgPotential(Point[] points, int charge){
        double avgPotential;
        double totalPotential = 0;
        for(int i = 0; i < points.length; i++){
            if (points[i].charge == charge){
                totalPotential += electricPotential(points,points[i]);
            }
        }
        avgPotential = 2 * totalPotential/(points.length);
        return avgPotential;
        
    }
    public static double getDeltaPhi(double posAvgPot, double negAvgPot){
        double DeltaPhi;
        DeltaPhi = posAvgPot - negAvgPot;
        return DeltaPhi;
    }

    public static VectorB FToAcc(Point[] points, Point r, double mass, VectorB force){
        VectorB vAcc = new VectorB();
        vAcc.x = EFieldB.EFieldSum(points, r).x/mass;
        vAcc.y = EFieldB.EFieldSum(points, r).y/mass;
        vAcc.z = EFieldB.EFieldSum(points, r).z/mass;
        return vAcc;
    }

}
