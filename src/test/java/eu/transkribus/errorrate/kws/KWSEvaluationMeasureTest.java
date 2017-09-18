/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsResult;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
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

    private static final File folderGT = new File("src/test/resources/gt");
    private static final File folderBot = new File("src/test/resources/hyp_bot");
    private static final File folderErr = new File("src/test/resources/hyp_err");

    private static File[] listGT;
    private static File[] listBot;
    private static File[] listErr;

    @BeforeClass
    public static void setUp() {
        listGT = setUpFolder(folderGT);
        listErr = setUpFolder(folderErr);
        listBot = setUpFolder(folderBot);
    }

    private static File[] setUpFolder(File folder) {
        assertTrue("cannot find resources in " + folder.getPath(), folder.exists());
        File[] res = FileUtils.listFiles(folder, "xml".split(" "), true).toArray(new File[0]);
        Arrays.sort(res);
        return res;
    }

    public KWSEvaluationMeasureTest() {
    }

    @AfterClass
    public static void tearDownClass() {
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
        KWSEvaluationMeasure measure = new KWSEvaluationMeasure(new AveragePrecision());
        System.out.println("main");
        String[] args = null;
        KWSEvaluationMeasure.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
