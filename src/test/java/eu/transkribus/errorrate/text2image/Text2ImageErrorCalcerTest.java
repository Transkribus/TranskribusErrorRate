/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.text2image;

import eu.transkribus.errorrate.ErrorRateCalcer;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gundram
 */
public class Text2ImageErrorCalcerTest {

    private static final File folderGT = new File("src/test/resources/gt");
    private static final File folderBot = new File("src/test/resources/hyp_bot");
    private static final File folderErr = new File("src/test/resources/hyp_err");

    private static File[] listGT;
    private static File[] listBot;
    private static File[] listErr;

    public Text2ImageErrorCalcerTest() {
    }

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

    @AfterClass
    public static void tearDown() {
    }

    private void printResult(ErrorRateCalcer.Result result) {
        System.out.println(result.getMethod() + ":" + result.getCounts());
        System.out.println(result.getMethod() + ":" + result.getMetrics());
        System.out.println("");
    }

    @Test
    public void testHTR() {
        System.out.println("testHTR");
        Text2ImageErrorParser instance = new Text2ImageErrorParser();
        double[] res = instance.run("-p -t 0.9".split(" "), listGT, listErr);
        System.out.println("P-Value (text): " + res[0]);
        System.out.println("R-Value:(text): " + res[1]);
        System.out.println("R-Value (geom): " + res[2]);
    }

//    @Test
//    public void testBestCase() {
//        System.out.println("testBestCase");
//        ErrorRateCalcer instance = new ErrorRateCalcer();
//        Map<Method, ErrorRateCalcer.Result> results = instance.process(listGT, listGT, Method.values());
//        for (ErrorRateCalcer.Result value : results.values()) {
//            Map<Metric, Double> metrics = value.getMetrics();
//            for (Metric metric : metrics.keySet()) {
//                double val = metrics.get(metric);
//                switch (metric) {
//                    case ACC:
//                    case F:
//                    case PREC:
//                    case REC:
//                        assertEquals("wrong value of metric " + metric + " for a perfect system.", 1.0, val, 0.0);
//                        break;
//                    default:
//                        assertEquals("wrong value of metric " + metric + " for a perfect system.", 0.0, val, 0.0);
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testPagewise() {
//        System.out.println("testPagewise");
//        ErrorRateCalcer instance = new ErrorRateCalcer();
//        Map<Method, ErrorRateCalcer.ResultPagewise> results = instance.processPagewise(listErr, listGT, Method.values());
//        for (ErrorRateCalcer.ResultPagewise result : results.values()) {
//            ObjectCounter<Count> counts = result.getCounts();
//            ObjectCounter<Count> countsPagewise = new ObjectCounter<>();
//            for (ErrorRateCalcer.Result resultPagewise : result.getPageResults()) {
//                countsPagewise.addAll(resultPagewise.getCounts());
//            }
//            for (Count count : counts.getResult()) {
//                assertEquals("sum of pagecounts have to be same as overall result", counts.get(count), countsPagewise.get(count));
//            }
//        }
//    }
}
