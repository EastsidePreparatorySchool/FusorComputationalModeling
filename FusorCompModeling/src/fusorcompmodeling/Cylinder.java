/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

/**
 *
 * @author guberti
 */
public class Cylinder extends GridComponent {
    double height;
    
    public Cylinder (Vector pos, double radius, double height) {
        this.pos = pos;
        this.radius = radius;
        this.height = height;
    }
    
    public Cylinder () {}
}
