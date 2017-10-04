/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.kws.measures.AveragePrecision;
import eu.transkribus.errorrate.kws.measures.GlobalAveragePrecision;
import eu.transkribus.errorrate.types.KWS;
import eu.transkribus.errorrate.types.KWS.Match;
import eu.transkribus.errorrate.types.KWS.MatchList;
import java.util.LinkedList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tobias
 */
public class AveragePrecisionTest {

    public AveragePrecisionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of calcStat method, of class AveragePrecision.
     */
    @Test
    public void testCalcStat() {
        System.out.println("calcStat");

        AveragePrecision ap = new GlobalAveragePrecision();

        test(ap, 1.0);
        test(ap, 0.8);
        test(ap, 0.5);
        test(ap, 0.3);
        test(ap, 0.0);

    }

    private void test(AveragePrecision ap, double corrRatio) {
        int n = 100;
        LinkedList<Match> matches = new LinkedList<>();
        for (int i = n - 1; i >= 0; i--) {
            if (i < n * corrRatio) {
                matches.add(new Match(KWS.Type.TRUE_POSITIVE, n - i, null, "", ""));
            } else {
                matches.add(new Match(KWS.Type.FALSE_POSITIVE, n - i, null, "", ""));
            }
        }
        MatchList matchlist = new MatchList(matches);
        LinkedList<MatchList> list = new LinkedList<>();
        list.add(matchlist);
        double value = ap.calcMeasure(list);
//
//        assertEquals((int) Math.ceil(corrRatio * n), value);
//        assertEquals(n - (int) Math.ceil(corrRatio * n), calcStat.falsePositives);
//        assertEquals(0, calcStat.falseNegatives);
//        assertEquals(n, calcStat.gt_size);
        assertEquals(corrRatio * n < 1 ? 0.0 : 1.0, value, 1E-5);
    }

}
