/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

/**
 *
 * @author sfreisem-kirov
 */
public interface Solver {
    public Atom moveForward(Point[] points,double timeStep,Atom a, EFieldB e);
}
