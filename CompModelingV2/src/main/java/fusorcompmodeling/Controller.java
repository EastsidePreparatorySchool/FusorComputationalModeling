/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.util.List;

/**
 *
 * @author sfreisem-kirov
 */
public class Controller {
    public EFieldB e = new EFieldB();
    public Solver s = new RungeKutta();  
    public Atom[] atoms = new Atom[50];
    public int atomsInArray = 0;
    
    public Controller(Point[] points, double voltageAnnode, double voltageCathode){
        e.setkQ(voltageAnnode,voltageCathode,points);
    }
    
    public void stepAllForeward(Point[] points,double numPSecs){
        double ts = 0.000000000001*numPSecs;
        for(int i = 0; i < atomsInArray; i++){
            Atom newAtom = s.moveForward(points,ts, atoms[i],e);
            atoms[i] = newAtom;
            System.out.println("Loop just ran once, atoms in array is " + atomsInArray);
        }
    }
    public void addAtom(Point pos, double mass){
        VectorB v = new VectorB();
        v.x=0;
        v.y=0;
        v.z=0;
        Atom a = new Atom();
        a.position = pos;
        a.Velocity = v;
        a.mass = mass;
        atoms[atomsInArray] = a;
        atomsInArray = atomsInArray + 1;
    }
}
