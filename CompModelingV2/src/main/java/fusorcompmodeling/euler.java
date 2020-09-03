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
public class euler implements Solver{

    @Override
    public Atom moveForward(Point[] points, double TS, Atom a, EFieldB e) {
        VectorB v0 = a.Velocity;
        VectorB g = StatsGen.FToAcc(points, a.position,a.mass,e.EFieldSum(points, a.position));
        
        a.position.x += (v0.x*TS) + ((g.x*TS*TS)/2);
        a.position.y += (v0.y*TS) + ((g.y*TS*TS)/2);
        a.position.z += (v0.z*TS) + ((g.z*TS*TS)/2);
        
        a.Velocity.x = v0.x + (g.x*TS);
        a.Velocity.y = v0.y + (g.y*TS);
        a.Velocity.z = v0.z + (g.z*TS);
        
    return a;
        
    }
    
}
