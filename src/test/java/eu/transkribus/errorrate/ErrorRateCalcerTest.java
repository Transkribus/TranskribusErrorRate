/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.types.Method;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gundram
 */
public class ErrorRateCalcerTest {

    private static final File folderGT = new File("src/test/resources/gt");
    private static final File folderBot = new File("src/test/resources/hyp_bot");
    private static final File folderErr = new File("src/test/resources/hyp_err");

    private static File[] listGT;
    private static File[] listBot;
    private static File[] listErr;

    public ErrorRateCalcerTest() {
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
        System.out.println(result.getMethod());
        System.out.println(result.getCounts());
        System.out.println(result.getMetrics());
        System.out.println("");
    }

    @Test
    public void testBot() {
        System.out.println("testBot");
        boolean pagewise = false;
        ErrorRateCalcer instance = new ErrorRateCalcer();
        List<ErrorRateCalcer.Result> results = instance.process(listBot, listGT, pagewise, Method.BOT, Method.BOT_ALNUM);
        for (ErrorRateCalcer.Result result : results) {
            printResult(result);
            if (result instanceof ErrorRateCalcer.ResultOverall) {
                ErrorRateCalcer.ResultOverall res = (ErrorRateCalcer.ResultOverall) result;
                for (ErrorRateCalcer.Result pageResult : res.getPageResults()) {
                    printResult(pageResult);
                }
            }
        }
    }

    @Test
    public void testErr() {
        System.out.println("testErr");
        boolean pagewise = false;
        ErrorRateCalcer instance = new ErrorRateCalcer();
        List<ErrorRateCalcer.Result> results = instance.process(listErr, listGT, pagewise, Method.WER, Method.BOT, Method.WER_ALNUM, Method.CER, Method.CER_ALNUM);
        for (ErrorRateCalcer.Result result : results) {
            printResult(result);
            if (result instanceof ErrorRateCalcer.ResultOverall) {
                ErrorRateCalcer.ResultOverall res = (ErrorRateCalcer.ResultOverall) result;
                for (ErrorRateCalcer.Result pageResult : res.getPageResults()) {
                    printResult(pageResult);
                }
            }
        }
    }

}
