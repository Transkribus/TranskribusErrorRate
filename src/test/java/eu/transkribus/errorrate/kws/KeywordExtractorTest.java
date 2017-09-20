/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsResult;
import eu.transkribus.errorrate.types.KwsWord;
import java.awt.Polygon;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gundram
 */
public class KeywordExtractorTest {

    private static final File folderGT = new File("src/test/resources/gt");
    private static final File folderBot = new File("src/test/resources/hyp_bot");
    private static final File folderErr = new File("src/test/resources/hyp_err");

    private static File[] listGT;
    private static File[] listBot;
    private static File[] listErr;

    public KeywordExtractorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        cases.add(new TestCase("the the they athe the", "the", 3, 5));
        cases.add(new TestCase("abab ab -da", "ab", 1, 3));
        cases.add(new TestCase("wadde hadde du de da", "du de", 1, 1));
    }

    @BeforeClass
    public static void setUpFolder() {
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

    @After
    public void tearDown() {
    }

    private class TestCase {

        private String line;
        private String keyword;
        private int countSeparated;
        private int countIncl;

        public TestCase(String line, String keyword, int countSeparated, int countIncl) {
            this.line = line;
            this.keyword = keyword;
            this.countSeparated = countSeparated;
            this.countIncl = countIncl;
        }

    }

    List<TestCase> cases = new LinkedList<>();

    /**
     * Test of countKeyword method, of class KeywordExtractor.
     */
    @Test
    public void testCountKeyword() {
        System.out.println("CountKeyword");
        KeywordExtractor instanceTrue = new KeywordExtractor(true);
        KeywordExtractor instanceFalse = new KeywordExtractor(false);
        for (TestCase aCase : cases) {
            assertEquals(aCase.countSeparated, instanceTrue.countKeyword(aCase.keyword, aCase.line));
            assertEquals(aCase.countIncl, instanceFalse.countKeyword(aCase.keyword, aCase.line));

        }
    }

    /**
     * Test of countKeyword method, of class KeywordExtractor.
     */
    @Test
    public void testGetKeywordPosition() {
        System.out.println("getKeywordPosition");
        KeywordExtractor instanceTrue = new KeywordExtractor(true);
        KeywordExtractor instanceFalse = new KeywordExtractor(false);
        for (TestCase aCase : cases) {
            double[][] keywordPosition = instanceTrue.getKeywordPosition(aCase.keyword, aCase.line);
            for (double[] ds : keywordPosition) {
                Assert.assertEquals(aCase.keyword.length(), (ds[1] - ds[0]) * aCase.line.length(), 1e-4);
            }
            double[][] keywordPosition2 = instanceFalse.getKeywordPosition(aCase.keyword, aCase.line);
            for (double[] ds : keywordPosition2) {
                Assert.assertEquals(aCase.keyword.length(), (ds[1] - ds[0]) * aCase.line.length(), 1e-4);
            }

        }
    }

    private static KwsResult GT2Hyp(KwsGroundTruth gt) {
        HashMap<String, KwsWord> map = new HashMap<>();
        for (KwsPage page : gt.getPages()) {
            String pageID = page.getPageID();
            List<KwsLine> lines = page.getLines();
            for (KwsLine line : lines) {
                HashMap<String, List<Polygon>> keywords = line.getKeyword2Baseline();
                for (String kw : keywords.keySet()) {
                    List<Polygon> positions = keywords.get(kw);
                    if (positions == null || positions.isEmpty()) {
                        continue;
                    }
                    KwsWord kwsWord = map.get(kw);
                    if (kwsWord == null) {
                        kwsWord = new KwsWord(kw);
                        for (KwsEntry po : kwsWord.getPos()) {
                            po.setParentLine(line);
                        }
                        map.put(kw, kwsWord);
                    }
                    for (Polygon position : positions) {
                        kwsWord.add(new KwsEntry(1.0, null, position, pageID));
                    }
                }
            }
        }
        return new KwsResult(new HashSet<>(map.values()));
    }

    private String[] getStringList(File[] files) {
        String[] res = new String[files.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = files[i].getPath();
        }
        return res;
    }

    @Test
    public void testGenerateKWSGroundTruth() {
        System.out.println("generateKWSGroundTruth");
        KeywordExtractor kwe = new KeywordExtractor(true);
        List<String> keywords = Arrays.asList("der", "und");
        String[] idList = getStringList(listGT);
        KwsGroundTruth keywordGroundTruth = kwe.getKeywordGroundTruth(getStringList(listGT), idList, keywords);
        KwsResult keyWordErr = GT2Hyp(kwe.getKeywordGroundTruth(getStringList(listErr), idList, keywords));
        KWSEvaluationMeasure kem = new KWSEvaluationMeasure(new AveragePrecision());
        kem.setGroundtruth(keywordGroundTruth);
        kem.setResults(keyWordErr);
        System.out.println(kem.getGlobalMearsure());
        System.out.println(kem.getMeanMearsure());
        System.out.println(kem.getStats());
    }

    public static void main(String[] args) {
        KeywordExtractorTest.setUpClass();
        KeywordExtractorTest.setUpFolder();
        KeywordExtractorTest kwe = new KeywordExtractorTest();
        kwe.setUp();
        kwe.testGenerateKWSGroundTruth();
    }

}
