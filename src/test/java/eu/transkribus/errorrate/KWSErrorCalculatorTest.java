/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.kws.measures.IRankingMeasure;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gundram
 */
public class KWSErrorCalculatorTest {

    public static final File folderTmp = new File("src/test/resources/test_kws_tmp");
    private static final File folderGT = new File("src/test/resources/gt");

    public KWSErrorCalculatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        folderTmp.mkdirs();
    }

    @AfterClass
    public static void tearDownClass() {
        FileUtils.deleteQuietly(folderTmp);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class KwsError.
     */
    @Test
    public void testRun() throws IOException {
        System.out.println("testRun");
        Collection<File> listFiles = FileUtils.listFiles(folderGT, "xml".split(" "), true);
        List<File> tmp = new LinkedList<File>(listFiles);
        Collections.sort(tmp);
        listFiles = tmp;
        File gtList = new File(folderTmp, "gtList.txt");
        File kwFile = new File("src/test/resources/kw.txt");
        File resFile = new File("src/test/resources/kws_htr/out_20.json");
        FileUtils.writeLines(gtList, listFiles);
        KwsError calculator = new KwsError();
        Map<IRankingMeasure.Measure, Double> exp = new LinkedHashMap<>();
        exp.put(IRankingMeasure.Measure.R_PRECISION, 0.8551187196641742);
        exp.put(IRankingMeasure.Measure.G_NCDG, -0.014978690675125528);
        exp.put(IRankingMeasure.Measure.MAP, 0.9011214914516754);
        exp.put(IRankingMeasure.Measure.GAP, 0.9006038330890166);
        exp.put(IRankingMeasure.Measure.RECALL, 0.9798657718120806);
        exp.put(IRankingMeasure.Measure.PRECISION_AT_10, 0.5054545454545453);
        exp.put(IRankingMeasure.Measure.PRECISION, 0.4165477888730385);
        exp.put(IRankingMeasure.Measure.M_NCDG, 0.38384328267815787);
        exp.put(IRankingMeasure.Measure.WMAP, 0.915926584620434);
        Map<IRankingMeasure.Measure, Double> run = calculator.run(new String[]{resFile.getPath(), kwFile.getPath(), "-p", gtList.getPath(), "-i"});
        for (IRankingMeasure.Measure measure : run.keySet()) {
//            System.out.println(measure + " = " + run.get(measure));
            Assert.assertEquals("measure changed", exp.get(measure), run.get(measure), 0.000001);
        }
        // TODO review the generated test code and remove the default call to fail.
    }

    public static void main(String[] args) throws IOException {
        KWSErrorCalculatorTest.setUpClass();
        KWSErrorCalculatorTest t = new KWSErrorCalculatorTest();
        t.testRun();
        KWSErrorCalculatorTest.tearDownClass();
    }

}
