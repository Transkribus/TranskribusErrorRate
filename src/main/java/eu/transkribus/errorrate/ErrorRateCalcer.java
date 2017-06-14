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
import java.util.LinkedHashMap;
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

    public class ResultPagewise extends Result {

        private final List<Result> pageResults = new LinkedList<>();

        public ResultPagewise(Method method) {
            super(method);
        }

        @Override
        public void addCounts(ObjectCounter<Count> counts) {
            super.addCounts(counts);
            Result result = new Result(getMethod());
            result.addCounts(counts);
            pageResults.add(result);
        }

        public List<Result> getPageResults() {
            return pageResults;
        }

    }

    public static class Result {

        private final Method method;
        private final ObjectCounter<Count> counts = new ObjectCounter<>();
        private final Map<Metric, Double> metrics = new LinkedHashMap<>();
        private boolean isCalculated = false;

        public Result(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public ObjectCounter<Count> getCounts() {
            return counts;
        }

        public Long getCount(Count count) {
            return counts.get(count);
        }

        public void addCounts(ObjectCounter<Count> counts) {
            this.counts.addAll(counts);
            isCalculated = false;
        }

        private void calculate() {
            if (!isCalculated) {
                metrics.clear();
                switch (method) {
                    case BOT:
                    case BOT_ALNUM:
                        double tp = counts.get(Count.TP);
                        double fp = counts.get(Count.FP);
                        double fn = counts.get(Count.FN);
                        double prec = (fp + tp) == 0 ? 1.0 : tp / (fp + tp);
                        double rec = (fn + tp) == 0 ? 1.0 : tp / (fn + tp);
                        double f = (prec + rec) == 0 ? 0 : 2 * prec * rec / (prec + rec);
                        metrics.put(Metric.REC, rec);
                        metrics.put(Metric.PREC, prec);
                        metrics.put(Metric.F, f);
                        break;
                    case CER:
                    case CER_ALNUM:
                    case WER:
                    case WER_ALNUM:
                        double err = counts.get(Count.ERR);
                        double cor = counts.get(Count.COR);
                        double gt = counts.get(Count.GT);
                        metrics.put(Metric.ACC, gt == 0 ? 1.0 : cor / gt);
                        metrics.put(Metric.ERR, gt == 0 ? 0.0 : err / gt);
                        break;
                    default:
                        throw new RuntimeException("unknown method '" + method + "'.");
                }
                isCalculated = true;
            }
        }

        public Map<Metric, Double> getMetrics() {
            calculate();
            return metrics;
        }

        public double getMetric(Metric metric) {
            return getMetrics().get(metric);
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

    public Result process(File[] hyp, File[] gt, Method method) {
        return process(hyp, gt, new Method[]{method}).get(method);
    }

    public Map<Method, Result> process(File[] hyp, File[] gt, Method... methods) {
        return process(hyp, gt, false, methods);
    }

    public ResultPagewise processPagewise(File[] hyp, File[] gt, Method method) {
        return processPagewise(hyp, gt, new Method[]{method}).get(method);
    }

    public Map<Method, ResultPagewise> processPagewise(File[] hyp, File[] gt, Method... methods) {
        Map<Method, Result> results = process(hyp, gt, true, methods);
        Map<Method, ResultPagewise> res = new HashMap<>();
        for (Method method : results.keySet()) {
            res.put(method, (ResultPagewise) results.get(method));
        }
        return res;
    }

    private Map<Method, Result> process(File[] hyp, File[] gt, boolean pagewise, Method... methods) {
        HashMap<Method, IErrorModule> modules = new HashMap<>();
        HashMap<Method, Result> results = new HashMap<>();
        for (Method method : methods) {
            modules.put(method, getErrorModule(method));
            results.put(method, pagewise ? new ResultPagewise(method) : new Result(method));
        }
        for (int i = 0; i < gt.length; i++) {
            File fileGT = gt[i];
            File fileHYP = hyp[i];
            Pair<List<String>, List<String>> textlines = reshape(TextLineUtil.getTextFromPageDom(fileHYP, fileGT));
            for (Method method : methods) {
                IErrorModule errorModule = modules.get(method);
                errorModule.calculate(textlines.getFirst(), textlines.getSecond());
                results.get(method).addCounts(errorModule.getCounter());
                errorModule.reset();
            }

        }
        return results;
    }

}
