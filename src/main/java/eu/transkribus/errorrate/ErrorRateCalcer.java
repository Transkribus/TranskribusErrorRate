/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.costcalculator.CostCalculatorDft;
import eu.transkribus.errorrate.interfaces.ICostCalculator;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.normalizer.StringNormalizerLetterNumber;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.types.Method;
import eu.transkribus.errorrate.types.Metric;
import eu.transkribus.errorrate.util.ObjectCounter;
import eu.transkribus.errorrate.util.TextLineUtil;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;
import eu.transkribus.tokenizer.TokenizerCategorizer;
import eu.transkribus.tokenizer.categorizer.CategorizerCharacterDft;
import eu.transkribus.tokenizer.categorizer.CategorizerWordMergeGroups;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.util.Pair;

/**
 * Class to calculate different metrics on documents
 *
 * @author gundram
 */
public class ErrorRateCalcer {

    public class ResultOverall extends Result {

        List<Result> pageResults = new LinkedList<>();

        public ResultOverall(Method method) {
            super(method);
        }

        @Override
        public void addCounts(ObjectCounter<Count> counts) {
            super.addCounts(counts);
            Result result = new Result(method);
            result.addCounts(counts);
            pageResults.add(result);
        }

        public List<Result> getPageResults() {
            return pageResults;
        }

    }

    public static class Result {

        final Method method;
        final ObjectCounter<Count> counts = new ObjectCounter<>();
        Map<Metric, Double> metrics;
        boolean isCalculated = false;

        public Result(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public ObjectCounter<Count> getCounts() {
            return counts;
        }

        private static boolean isRetrieval(ObjectCounter<Count> counts) {
            List<Count> list = counts.getResult();
            boolean isDynProg = list.contains(Count.COR) || list.contains(Count.DEL) || list.contains(Count.INS) || list.contains(Count.SUB);
            boolean isRetrieval = list.contains(Count.TP) || list.contains(Count.FN) || list.contains(Count.FP);
            if (isDynProg != isRetrieval) {
                return isRetrieval;
            }
            if (counts.getMap().getOrDefault(Count.GT, 0L) == 0L) {
                return false;
            }
            throw new RuntimeException("cannot find out if task is retrieval task or dynamic programming task.");
        }

        private static Map<Metric, Double> getMetrics(ObjectCounter<Count> counts) {
            Map<Metric, Double> res = new HashMap<>();
            if (isRetrieval(counts)) {
                double tp = counts.get(Count.TP);
                double fp = counts.get(Count.FP);
                double fn = counts.get(Count.FN);
                double prec = (fp + tp) == 0 ? 1.0 : tp / (fp + tp);
                double rec = (fn + tp) == 0 ? 1.0 : tp / (fn + tp);
                double f = 2 * prec * rec / (prec + rec);
                res.put(Metric.REC, rec);
                res.put(Metric.PREC, prec);
                res.put(Metric.F, f);
            } else {
                double err = counts.get(Count.ERR);
                double cor = counts.get(Count.COR);
                double gt = counts.get(Count.GT);
                res.put(Metric.ACC, gt == 0 ? 1.0 : cor / gt);
                res.put(Metric.ERR, gt == 0 ? 0.0 : err / gt);
            }
            return res;
        }

        public void addCounts(ObjectCounter<Count> counts) {
            this.counts.addAll(counts);
        }

        private void calculate() {
            if (!isCalculated) {
                metrics = getMetrics(counts);
                isCalculated = true;
            }
        }

        public Map<Metric, Double> getMetrics() {
            calculate();
            return metrics;
        }

        public double getMetric(Metric metric) {
            calculate();
            return metrics.get(metric);
        }

    }

    public class Request {

        private boolean pagewise; //also Command line tool
        private Method methods[];

        public Request(boolean pagewise, Method... methods) {
            this.pagewise = pagewise;
            this.methods = methods;
        }

    }

    private IErrorModule getErrorModule(Method method) {
        Boolean detailed = Boolean.FALSE;
        ICostCalculator cc = new CostCalculatorDft();

        IStringNormalizer sn = null;
        switch (method) {
            case BOT:
            case WER:
            case CER:
                break;
            case BOT_ALNUM:
            case WER_ALNUM:
            case CER_ALNUM:
                sn = new StringNormalizerLetterNumber(sn);
                break;
            default:
                throw new RuntimeException("unexpected method '" + method + "'.");
        }

        ITokenizer tok = null;
        switch (method) {
            case BOT:
            case BOT_ALNUM:
            case WER:
            case WER_ALNUM:
                tok = new TokenizerCategorizer(new CategorizerWordMergeGroups());
                break;
            case CER:
            case CER_ALNUM:
                tok = new TokenizerCategorizer(new CategorizerCharacterDft());
                break;
            default:
                throw new RuntimeException("unexpected method '" + method + "'.");
        }
        switch (method) {
            case CER:
            case WER:
            case CER_ALNUM:
            case WER_ALNUM:
                return new ErrorModuleDynProg(cc, tok, sn, detailed);
            case BOT:
            case BOT_ALNUM:
                return new ErrorModuleBagOfTokens(tok, sn, detailed);
            default:
                throw new RuntimeException("unexpected method '" + method + "'.");
        }
    }

    private static Pair<List<String>, List<String>> reshape(List<Pair<String, String>> data) {
        List<String> l1 = new LinkedList<>();
        List<String> l2 = new LinkedList<>();
        for (Pair<String, String> pair : data) {
            l1.add(pair.getFirst());
            l2.add(pair.getSecond());
        }
        return new Pair<>(l1, l2);
    }

    public List<Result> process(File[] hyp, File[] gt, boolean pagewise, Method... methods) {
        List<IErrorModule> modules = new ArrayList<>(methods.length);
        List<Result> results = new ArrayList<>(methods.length);
        for (Method method : methods) {
            modules.add(getErrorModule(method));
            results.add(pagewise ? new ResultOverall(method) : new Result(method));
        }
        for (int i = 0; i < gt.length; i++) {
            File fileGT = gt[i];
            File fileHYP = hyp[i];
            Pair<List<String>, List<String>> textlines = reshape(TextLineUtil.getTextFromPageDom(fileHYP, fileGT));
            for (int j = 0; j < modules.size(); j++) {
                IErrorModule errorModule = modules.get(j);
                errorModule.calculate(textlines.getFirst(), textlines.getSecond());
                Result result = results.get(j);
                result.addCounts(errorModule.getCounter());
                errorModule.reset();
            }

        }
        return results;
    }

}
