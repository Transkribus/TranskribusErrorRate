/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsResult;
import eu.transkribus.errorrate.types.KwsWord;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
     * Test of getMeanMearsure method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testGetMeanMearsure() {
        System.out.println("getMeanMearsure");
        AveragePrecision rank = new AveragePrecision();
        KWSEvaluationMeasure measure = new KWSEvaluationMeasure(rank);

        test(measure, 1.0, 0.0);
        test(measure, 1.0, 0.0, 0.0);

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

    private void test(KWSEvaluationMeasure measure, double corrRatio, double fpRatio) {
        HashSet<KwsWord> words = new HashSet<>();
        List<KwsPage> pages = new LinkedList<>();
        int numOfPages = 1;
        int numOfQuerries = 10;
        for (int querryId = 0; querryId < numOfQuerries; querryId++) {
            String querryWord = "word" + querryId;
            KwsWord word = new KwsWord(querryWord);
            for (int pageId = 0; pageId < numOfPages; pageId++) {
                LinkedList<KwsLine> lines = new LinkedList<>();
                int numOfLines = 10;
                for (int lineId = 0; lineId < numOfLines; lineId++) {
                    int numOfMatches = 100;
                    int numOfcorr = (int) Math.ceil(corrRatio * numOfMatches);
                    int numOfFp = numOfMatches - numOfcorr;
                    KwsLine line = new KwsLine("line" + lineId);
                    Polygon p = new Polygon(new int[]{0, lineId * 50}, new int[]{100, lineId * 50}, 2);
                    addMatches(word, numOfcorr, numOfFp, 0, p,"page" + pageId);
                    addWords(line, querryWord, numOfcorr, 0, p);
                    lines.add(line);
                }
                pages.add(new KwsPage("page" + pageId, lines));
            }
        }

        KwsResult res = new KwsResult(words);
        KwsGroundTruth gt = new KwsGroundTruth(pages);

        measure.setGroundtruth(gt);
        measure.setResults(res);

        double meanMearsure = measure.getMeanMearsure();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void test(KWSEvaluationMeasure measure, double corrRatio, double fpRatio, double missedRatio) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addMatches(KwsWord word, int numOfcorr, int numOfFp, int numOfFn, Polygon p, String pageId) {
        for (int i = 0; i < numOfcorr; i++) {
//            match =new KwsMatch(KwsMatch.Type.match,numOfcorr+numOfFn+numOfFp-i,p, pageId, word);
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addWords(KwsLine line, String string, int numOfcorr, int i, Polygon p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
