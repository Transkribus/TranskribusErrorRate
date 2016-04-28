/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate;

import eu.transkribus.errorrate.tokenizer.TokenizerCategorizer;
import eu.transkribus.errorrate.interfaces.ICategorizer;
import eu.transkribus.errorrate.interfaces.ICostCalculator;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.interfaces.IStringNormalizer;
import eu.transkribus.errorrate.interfaces.ITokenizer;
import eu.transkribus.errorrate.util.ObjectCounter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/**
 * Module, which counts the differences between two bag of tokens, namely the
 * bags of tokens from the recognition/hypothesis and the reference/ground
 * truth. uses the calculate the error rates between tokens. Some other classes
 * are needed, to calculate the error rate. Two tokens are the same, if their
 * unicode representation is the same. See {@link ITokenizer} and
 * {@link IStringNormalizer} for more details.
 *
 * @author gundram
 */
public class ErrorModuleBagOfTokens implements IErrorModule {

    private final ObjectCounter<String> counter = new ObjectCounter<>();
    private final ObjectCounter<String> counterIntersect = new ObjectCounter<>();
    private final ObjectCounter<String> counterOnlyReco = new ObjectCounter<>();
    private final ObjectCounter<String> counterOnlyRef = new ObjectCounter<>();
//    private final ICostCalculator costCalculatorCharacter;
//    private final ICategorizer categorizer;
    private final ITokenizer tokenizer;
    private final Boolean detailed;
    private final IStringNormalizer stringNormalizer;

    public ErrorModuleBagOfTokens(ICategorizer categorizer, IStringNormalizer stringNormalizer, Boolean detailed) {
        this(new TokenizerCategorizer(categorizer), stringNormalizer, detailed);
    }

    public ErrorModuleBagOfTokens(ITokenizer tokenizer, IStringNormalizer stringNormalizer, Boolean detailed) {
        this.detailed = detailed;
//        this.categorizer = cactegorizer;
        if (tokenizer == null) {
            throw new RuntimeException("no tokenizer given (is null)");
        }
        this.tokenizer = tokenizer;
        this.stringNormalizer = stringNormalizer;
    }

    /**
     * normalize and tokenize both inputs. Afterwards find the cheapest cost to
     * manipulate the recognition tokens to come to the reference tokens. Count
     * the manipulation which had to be done. If detailed==null or
     * detailed==True, confusion/substitution map is filled.
     *
     * @param reco
     * @param ref
     */
    @Override
    public void calculate(String reco, String ref) {
        //use string normalizer, if set
        if (stringNormalizer != null) {
            reco = stringNormalizer.normalize(reco);
            ref = stringNormalizer.normalize(ref);
        }
        //tokenize both strings
        List<String> recos = tokenizer.tokenize(reco);
        List<String> refs = tokenizer.tokenize(ref);

        Collections.sort(refs);
        Collections.sort(recos);
        int idxRef = 0;
        int idxReco = 0;
        while (idxReco < recos.size() && idxRef < refs.size()) {
            String recoToken = recos.get(idxReco);
            String refToken = refs.get(idxRef);
            int cmp = refToken.compareTo(recoToken);
            if (cmp == 0) {
                counter.add("TP");
                idxReco++;
                idxRef++;
                if (detailed != null && detailed) {
                    counterIntersect.add(refToken);
                }
            } else if (cmp > 0) {
                counter.add("FP");
                idxReco++;
                if (detailed == null || detailed) {
                    counterOnlyReco.add(recoToken);
                }
            } else {
                counter.add("FN");
                idxRef++;
                if (detailed == null || detailed) {
                    counterOnlyRef.add(refToken);
                }
            }
        }
        counter.add("HYP", recos.size());
        counter.add("GT", refs.size());
    }

    /**
     * returns the absolute and relative frequency of manipulation. If
     * detailed==null or detailed==True, the confusion map is added in before
     * the basic statistic.
     *
     * @return
     */
    @Override
    public List<String> getResults() {
        LinkedList<String> res = new LinkedList<>();
        if (detailed == null || detailed) {
            List<Pair<String, Long>> result = new LinkedList<>();
            for (Pair<String, Long> pair : counterIntersect.getResultOccurrence()) {
                result.add(new Pair<>("TP=" + pair.getFirst(), pair.getSecond()));
            }
            for (Pair<String, Long> pair : counterOnlyRef.getResultOccurrence()) {
                result.add(new Pair<>("FN=" + pair.getFirst(), pair.getSecond()));
            }
            for (Pair<String, Long> pair : counterOnlyReco.getResultOccurrence()) {
                result.add(new Pair<>("FP=" + pair.getFirst(), pair.getSecond()));
            }
            Collections.sort(result, new Comparator<Pair<String, Long>>() {
                @Override
                public int compare(Pair<String, Long> o1, Pair<String, Long> o2) {
                    return Long.compare(o1.getSecond(), o2.getSecond());
                }
            });
            int maxSize = String.valueOf(result.get(result.size() - 1).getSecond()).length();
            for (Pair<String, Long> pair : result) {
                res.add(String.format("%" + maxSize + "d=\"%s\"", pair.getSecond(), pair.getFirst()));
            }
        }
        List<Pair<String, Long>> resultOccurrence = getCounter().getResultOccurrence();
//        List<Pair<PathCalculatorExpanded.Manipulation, Long>> resultOccurrence = counter.getResultOccurrence();
        double intersect = 0, fp = 0, fn = 0;
        for (Pair<String, Long> pair : resultOccurrence) {
            switch (pair.getFirst()) {
                case "TP":
                    intersect = Double.valueOf(String.valueOf(pair.getSecond()));
                    break;
                case "FP":
                    fp = Double.valueOf(String.valueOf(pair.getSecond()));
//                    fp = pair.getSecond();
                    break;
                case "FN":
                    fn = Double.valueOf(String.valueOf(pair.getSecond()));
//                    fn = pair.getSecond();
                    break;
                default:
                    throw new RuntimeException("unexpected manipulation '" + pair.getFirst().toString() + "'");
            }
        }
        double recall = intersect / (intersect + fn);
        double precision = intersect / (intersect + fp);
//        int length = Math.max(Math.max(String.valueOf(fn).length(), String.valueOf(fp).length()), String.valueOf(intersect).length());
        res.add(String.format("%6s =%6.2f%%", "RECALL", recall * 100.0));
        res.add(String.format("%6s =%6.2f%%", "PRECISION", precision * 100.0));
        if (intersect == 0) {
            res.add(String.format("%6s =%6.2f%%", "F-MEASURE", 0.0));
        } else {
            res.add(String.format("%6s =%6.2f%%", "F-MEASURE", (recall * precision) / (recall + precision) * 200.0));
        }
        res.add(resultOccurrence.toString());
        return res;
    }

    @Override
    public ObjectCounter<String> getCounter() {
        List<Pair<String, Long>> resultOccurrence = counter.getResultOccurrence();
//        long intersect = 0, fp = 0, fn = 0;
        ObjectCounter<String> objectCounter = new ObjectCounter<>();
        for (Pair<String, Long> pair : resultOccurrence) {
            switch (pair.getFirst()) {
                case "COR":
                    objectCounter.add("TP", pair.getSecond());
                    break;
                case "DEL":
                    objectCounter.add("FP", pair.getSecond());
                    break;
                case "INS":
                    objectCounter.add("FN", pair.getSecond());
                    break;
                default:
                    throw new RuntimeException("unexpected manipulation '" + pair.getFirst().toString() + "'");
            }
        }
        return objectCounter;
    }

}
