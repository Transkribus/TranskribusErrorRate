/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsResult;
import java.util.LinkedList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gundram
 */
public class KWSEvaluationMeasureTest {
    
    public KWSEvaluationMeasureTest() {
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
     * Test of setResults method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testSetResults() {
        System.out.println("setResults");
        KwsResult hypo = null;
        KWSEvaluationMeasure instance = null;
        instance.setResults(hypo);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGroundtruth method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testSetGroundtruth() {
        System.out.println("setGroundtruth");
        KwsGroundTruth ref = null;
        KWSEvaluationMeasure instance = null;
        instance.setGroundtruth(ref);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMatchLists method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testSetMatchLists() {
        System.out.println("setMatchLists");
        LinkedList<KwsMatchList> matchLists = null;
        KWSEvaluationMeasure instance = null;
        instance.setMatchLists(matchLists);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMeanMearsure method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testGetMeanMearsure() {
        System.out.println("getMeanMearsure");
        KWSEvaluationMeasure instance = null;
        double expResult = 0.0;
        double result = instance.getMeanMearsure();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGlobalMearsure method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testGetGlobalMearsure() {
        System.out.println("getGlobalMearsure");
        KWSEvaluationMeasure instance = null;
        double expResult = 0.0;
        double result = instance.getGlobalMearsure();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testMain() {
        KWSEvaluationMeasure measure = new KWSEvaluationMeasure(new IRankingMeasure() {
            @Override
            public double measure(KwsMatchList matches) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        System.out.println("main");
        String[] args = null;
        KWSEvaluationMeasure.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
