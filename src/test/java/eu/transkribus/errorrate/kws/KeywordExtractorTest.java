/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

}
