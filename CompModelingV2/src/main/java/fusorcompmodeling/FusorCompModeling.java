/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;
import Jama.*;

/**
 *
 * @author guberti
 */
public class FusorCompModeling {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) {
        VectorB v = new VectorB(0, 0, 0, Math.PI/2, -Math.PI/2);
        double[][] rotatable  = {{0}, {5}, {0}};
        Matrix m = new Matrix(rotatable);
        double[][] rotated = v.rotateAroundVector(Math.PI/2, m);
        
    }

}
