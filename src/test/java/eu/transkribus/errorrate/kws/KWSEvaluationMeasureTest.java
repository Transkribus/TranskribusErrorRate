/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.aligner.BaseLineAligner;
import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsResult;
import eu.transkribus.errorrate.types.KwsWord;
import java.awt.Polygon;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

//        test(measure, 1.0, 0.0, 0.0);
    }

    /**
     * Test of getGlobalMearsure method, of class KWSEvaluationMeasure.
     */
    @Test
    public void testGetGlobalMearsure() {
        System.out.println("getGlobalMearsure");
        KWSEvaluationMeasure measure = new KWSEvaluationMeasure(new BaseLineAligner());

        test(measure, 1.0, 0.0);
        test(measure, 0.8, 0.0);
        test(measure, 0.5, 0.0);
        test(measure, 0.0, 0.0);

        test(measure, 1.0, 0.2);
        test(measure, 1.0, 1.0);
    }

    private void test(KWSEvaluationMeasure measure, double corrRatio, double fnRatio) {
        HashMap<String, KwsWord> words = new HashMap<>();
        List<KwsPage> pages = new LinkedList<>();
        int numOfPages = 1;
        int numOfQuerries = 10;
        int totalFn = 0;
        int totalcorr = 0;
        Random rnd = new Random(1234);

        LinkedList<String> keys = new LinkedList<String>();

        for (int querryId = 0; querryId < numOfQuerries; querryId++) {
            String querryWord = "word" + querryId;
            KwsWord word = new KwsWord(querryWord);
            words.put(word.getKeyWord(), word);
        }

        LinkedList<Map.Entry<String, KwsWord>> keyAndWord = new LinkedList<>(words.entrySet());

        for (int pageId = 0; pageId < numOfPages; pageId++) {
            LinkedList<KwsLine> lines = new LinkedList<>();
            int numOfLines = 10;
            for (int lineId = 0; lineId < numOfLines; lineId++) {
                int numOfMatches = 5;// muss kleiner sein als numOfQuerries
                int numOfcorr = (int) Math.ceil(corrRatio * numOfMatches);
                int numOfFp = numOfMatches - numOfcorr;
                int numOfFn = (int) Math.ceil(numOfMatches * fnRatio);
//                totalFp += numOfFp;
                totalcorr += numOfcorr;
                totalFn += numOfFn;
                Polygon p = new Polygon(new int[]{lineId * 50, lineId * 50}, new int[]{0, 100}, 2);
                KwsLine line = new KwsLine(p);

                int cnt = -1;
                Collections.shuffle(keyAndWord, rnd);
                for (Map.Entry<String, KwsWord> entry : keyAndWord) {
                    if (cnt < 0) {
                        // false negatives 
                        for (int i = 0; i < numOfFn; i++) {
                            addGtWord(line, entry.getKey(), p, "page" + pageId);
                        }
                    } else {
                        // false positives and corrects
                        addMatch(entry.getValue(), cnt < numOfFp ? 0.0 : 1.0, p, "page" + pageId);
                        if (cnt >= numOfFp) {
                            addGtWord(line, entry.getKey(), p, "page" + pageId);
                        }
                    }
                    cnt++;
                    if (cnt >= numOfMatches) {
                        break;
                    }
                }

                lines.add(line);
            }
            pages.add(new KwsPage("page" + pageId, lines));
        }

        KwsResult res = new KwsResult(new HashSet<>(words.values()));
        KwsGroundTruth gt = new KwsGroundTruth(pages);

        measure.setGroundtruth(gt);
        measure.setResults(res);
        LinkedList<IRankingMeasure.Measure> ms = new LinkedList<>();
        ms.add(IRankingMeasure.Measure.GAP);

        double globalMearsure = measure.getMeasure(ms).get(IRankingMeasure.Measure.GAP);
        assertEquals(corrRatio == 0.0 ? 1.0 : (double) totalcorr / (totalcorr + totalFn), globalMearsure, 1e-5);
        System.out.println("measure: " + globalMearsure);
    }

    private void addMatch(KwsWord word, double conf, Polygon p, String pageId) {
        word.add(new KwsEntry(conf, "", p, pageId));
    }

    private void addGtWord(KwsLine line, String keyword, Polygon p, String pageId) {
        line.addKeyword(keyword, p);
    }

}
