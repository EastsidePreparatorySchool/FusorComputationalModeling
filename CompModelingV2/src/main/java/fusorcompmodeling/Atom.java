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
public class Atom {
    public Atom(){
    
    }
    public double mass;
    public Point position;
    public VectorB Velocity;
    
    public String toString(){
        return ("Mass: " + mass + " Position: " + position + " Velocity: " + Velocity);
         
    }
}
