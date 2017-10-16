/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import static eu.transkribus.errorrate.KWSErrorCalculatorTest.folderTmp;
import eu.transkribus.errorrate.kws.measures.IRankingMeasure;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
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
public class KwsErrorTest {

    public KwsErrorTest() {
    }

    public static final File folderTmp = new File("src/test/resources/test_kws2_tmp");
    private static final File folderGT = new File("src/test/resources/gt");

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
     * Test of run method, of class KwsError.
     */
    @Test
    public void testRun() throws IOException {
        Collection<File> listFiles = FileUtils.listFiles(folderGT, "xml".split(" "), true);
        List<File> tmp = new LinkedList<File>(listFiles);
        Collections.sort(tmp);
        listFiles = tmp;
        File gtList = new File(folderTmp, "gtList.txt");
        File resFile = new File("src/test/resources/kws_htr/out_20.json");
        FileUtils.writeLines(gtList, listFiles);
        KwsError calculator = new KwsError();
        Map<IRankingMeasure.Measure, Double> run = calculator.run(new String[]{resFile.getPath(), "-p", gtList.getPath(), "-i","-k","out.txt"});
        System.out.println(run);
    }
}
