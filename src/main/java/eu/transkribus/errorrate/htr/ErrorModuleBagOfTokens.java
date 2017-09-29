/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.htr;

import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.util.ObjectCounter;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;
import eu.transkribus.tokenizer.TokenizerCategorizer;

import eu.transkribus.tokenizer.interfaces.ICategorizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private final ObjectCounter<Count> counter = new ObjectCounter<>();
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
     * @param reco hypothesis
     * @param ref reference
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
                counter.add(Count.TP);
                idxReco++;
                idxRef++;
                if (detailed != null && detailed) {
                    counterIntersect.add(refToken);
                }
            } else if (cmp > 0) {
                counter.add(Count.FP);
                idxReco++;
                if (detailed == null || detailed) {
                    counterOnlyReco.add(recoToken);
                }
            } else {
                counter.add(Count.FN);
                idxRef++;
                if (detailed == null || detailed) {
                    counterOnlyRef.add(refToken);
                }
            }
        }
        for (; idxRef < refs.size(); idxRef++) {
            String refToken = refs.get(idxRef);
            counter.add(Count.FN);
            if (detailed == null || detailed) {
                counterOnlyRef.add(refToken);
            }
        }
        for (; idxReco < recos.size(); idxReco++) {
            String recoToken = recos.get(idxReco);
            counter.add(Count.FP);
            if (detailed == null || detailed) {
                counterOnlyReco.add(recoToken);
            }
        }
        counter.add(Count.HYP, recos.size());
        counter.add(Count.GT, refs.size());
    }

    @Override
    public void reset() {
        counter.reset();
        counterIntersect.reset();
        counterOnlyReco.reset();
        counterOnlyRef.reset();
    }

    /**
     * returns the absolute and relative frequency of manipulation. If
     * detailed==null or detailed==True, the confusion map is added in before
     * the basic statistic.
     *
     * @return human readable results
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
        Map<Count, Long> map = getCounter().getMap();
//        List<Pair<PathCalculatorExpanded.Manipulation, Long>> resultOccurrence = counter.getResultOccurrence();
        double tp = map.get(Count.TP);
        double fn = map.get(Count.FN);
        double fp = map.get(Count.FP);
        double recall = tp / (tp + fn);
        double precision = tp / (tp + fp);
//        int length = Math.max(Math.max(String.valueOf(fn).length(), String.valueOf(fp).length()), String.valueOf(intersect).length());
        res.add(String.format("%6s =%6.4f%%", "RECALL", recall));
        res.add(String.format("%6s =%6.4f%%", "PRECISION", precision));
        if (tp == 0) {
            res.add(String.format("%6s =%6.4f%%", "F-MEASURE", 0.0));
        } else {
            res.add(String.format("%6s =%6.4f%%", "F-MEASURE", (recall * precision) / (recall + precision) * 2.0));
        }
        res.add(getCounter().toString());
        return res;
    }

    @Override
    public ObjectCounter<Count> getCounter() {
        return counter;
    }

    private String toOneLine(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        if (lines.size() == 1) {
            return lines.get(0);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            sb.append('\n').append(lines.get(i));
        }
        return sb.toString();
    }

    @Override
    public void calculate(List<String> reco, List<String> ref) {
        calculate(toOneLine(reco), toOneLine(ref));
    }

    private static class RecoRef {

        private String[] recos;
        private String[] refs;

        public RecoRef(String[] recos, String[] refs) {
            this.recos = recos;
            this.refs = refs;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Arrays.deepHashCode(this.recos);
            hash = 29 * hash + Arrays.deepHashCode(this.refs);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RecoRef other = (RecoRef) obj;
            if (!Arrays.deepEquals(this.recos, other.recos)) {
                return false;
            }
            if (!Arrays.deepEquals(this.refs, other.refs)) {
                return false;
            }
            return true;
        }

    }

}
