/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.types.Method;
import eu.transkribus.errorrate.types.Metric;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Class to calculate different metrics on documents
 *
 * @author gundram
 */
public class ErrorRateCalcer {

    public class PageResult {

        Map<Count, Integer> counts;
        Map<Metric, Double> metrics;

    }

    public class Result {

        Map<Count, Integer> counts;
        Map<Metric, Double> metrics;
        List<PageResult> pageResults;

    }

    public class Request {

        private boolean pagewise; //also Command line tool
        private Method methods[];

        public Request(boolean pagewise, Method... methods) {
            this.pagewise = pagewise;
            this.methods = methods;
        }

    }

    public Result process(File[] hyp, File[] gt, Request methods) {
        return new Result();
    }

}
