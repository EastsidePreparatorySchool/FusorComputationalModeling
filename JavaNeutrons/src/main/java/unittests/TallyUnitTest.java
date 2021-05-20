/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unittests;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eastsideprep.javaneutrons.core.CorrelatedTallyOverEV;
import org.eastsideprep.javaneutrons.core.Gamma;
import org.eastsideprep.javaneutrons.core.Tally;
import org.eastsideprep.javaneutrons.core.TallyOverEV;
import org.eastsideprep.javaneutrons.core.Util;
import org.eastsideprep.javaneutrons.materials.N1H;

/**
 *
 * @author gunnar
 */
public class TallyUnitTest implements UnitTest {

    @Override
    public boolean test() {

        Tally t = new Tally();
        TallyOverEV toe = new TallyOverEV(12e6, 100);
        CorrelatedTallyOverEV ct = new CorrelatedTallyOverEV(12e6, 100, null);

        Gamma g1 = new Gamma(new Vector3D(1, 1, 1), N1H.getInstance(), 3e6);
        Gamma g2 = new Gamma(new Vector3D(1, 1, 1), N1H.getInstance(), 3e6);
        Gamma g3 = new Gamma(new Vector3D(1, 1, 1), N1H.getInstance(), 3e6);

        t.record(1000, 1e-5);
        t.record(2000, 2.45e6);
        t.record(3000, 1e10);

        toe.record(1000, 1e-5 * Util.Physics.eV);
        toe.record(2000, 2.45e6 * Util.Physics.eV);
        toe.record(3000, 1e10 * Util.Physics.eV);

        ct.record(g1, 1000, 1e-5 * Util.Physics.eV);
        ct.record(g2, 2000, 2.45e6 * Util.Physics.eV);
        ct.record(g3, 3000, 1e10 * Util.Physics.eV);
        g1.tally();
        g2.tally();
        g3.tally();

        System.out.println("Simple:");
        System.out.println(t.toString(false));
        System.out.println("");

        System.out.println("Combined over eV:");
        System.out.println("Log");
        System.out.println(toe.toString());
        System.out.println("Flat");
        System.out.println(toe.hFlat.toString());
        System.out.println("Low");
        System.out.println(toe.hLow.toString());
        System.out.println("");

        System.out.println("Correlated over eV:");
        System.out.println("Log");
        System.out.println(ct.toString());
        System.out.println("Flat");
        System.out.println(ct.hFlat.toString());
        System.out.println("Low");
        System.out.println(ct.hLow.toString());

        return true;
    }

}
