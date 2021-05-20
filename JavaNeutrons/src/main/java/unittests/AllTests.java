/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unittests;

/**
 *
 * @author gunnar
 */
public class AllTests {

    static boolean test = false;

    public static void run() {
        if (test) {
            UnitTest ut = new TallyUnitTest();
            boolean success = ut.test();
            System.out.println("Tally test: " + (success ? "success" : "failed"));
            
            System.exit(0);
        }
    }

}
