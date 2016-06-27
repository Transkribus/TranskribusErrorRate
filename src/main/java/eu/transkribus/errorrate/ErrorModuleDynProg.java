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
import eu.transkribus.errorrate.util.ObjectCounter;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/**
 * Module, which uses the {@link PathCalculatorExpanded} to calculate the error
 * rates between tokens. Some other classes are needed, to calculate the error
 * rate. See {@link ICostCalculator}, {@link ITokenizer} and
 * {@link IStringNormalizer} for more details.
 *
 * @author gundram
 */
public class ErrorModuleDynProg implements IErrorModule {

    private final PathCalculatorExpanded<String, String> pathCalculator = new PathCalculatorExpanded<>();
    private final ObjectCounter<String> counter = new ObjectCounter<>();
    private final ObjectCounter<Pair<List<String>, List<String>>> counterSub = new ObjectCounter<>();
//    private final ICostCalculator costCalculatorCharacter;
//    private final ICategorizer categorizer;
    private final ITokenizer tokenizer;
    private final Boolean detailed;
    private final IStringNormalizer stringNormalizer;

    public ErrorModuleDynProg(ICostCalculator costCalculatorCharacter, ICategorizer categorizer, IStringNormalizer stringNormalizer, Boolean detailed) {
        this(costCalculatorCharacter, new TokenizerCategorizer(categorizer), stringNormalizer, detailed);
        if (costCalculatorCharacter instanceof ICostCalculator.CategoryDependent) {
            ((ICostCalculator.CategoryDependent) costCalculatorCharacter).setCategorizer(categorizer);
        }

    }

    public ErrorModuleDynProg(ICostCalculator costCalculator, ITokenizer tokenizer, IStringNormalizer stringNormalizer, Boolean detailed) {
        this.detailed = detailed;
//        this.categorizer = cactegorizer;
        if (tokenizer == null) {
            throw new RuntimeException("no tokenizer given (is null)");
        }
        this.tokenizer = tokenizer;
        this.stringNormalizer = stringNormalizer;
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, PathCalculatorExpanded.Manipulation.SUB));
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, PathCalculatorExpanded.Manipulation.INS));
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, PathCalculatorExpanded.Manipulation.DEL));
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
        //use dynamic programming to calculate the cheapest path through the dynamic programming tabular
        List<PathCalculatorExpanded.IDistance<String, String>> calcBestPath = pathCalculator.calcBestPath(recos, refs);
        //go through the path...
        for (PathCalculatorExpanded.IDistance<String, String> iDistance : calcBestPath) {
            //count the manipulation, which have to be done at the specific position (Insertion, Deletion, Substitution, Correct)
            PathCalculatorExpanded.Manipulation manipulation = iDistance.getManipulation();
            counter.add(manipulation.toString());
            //for a detailed output, add tokens to the substitution/confusion map
            if (detailed == null && !manipulation.equals(PathCalculatorExpanded.Manipulation.COR)) {
                //if only errors should be put into the confusion map
                counterSub.add(new Pair<>(iDistance.getRecos(), iDistance.getReferences()));
                if (iDistance.getRecos().isEmpty() && iDistance.getReferences().isEmpty()) {
                    throw new RuntimeException("error here in the normal mode");
                }
            } else if (detailed != null && detailed) {
                //if everything should be put in the substitution map (also correct manipulation)
                counterSub.add(new Pair<>(iDistance.getRecos(), iDistance.getReferences()));
                if (iDistance.getRecos().isEmpty() && iDistance.getReferences().isEmpty()) {
                    throw new RuntimeException("error here in the other mode");
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
     * @return human readable result
     */
    @Override
    public List<String> getResults() {
        LinkedList<String> res = new LinkedList<>();
        if (detailed == null || detailed) {
            for (Pair<Pair<List<String>, List<String>>, Long> pair : counterSub.getResultOccurrence()) {
                Pair<List<String>, List<String>> first = pair.getFirst();
                String key1;
                switch (first.getFirst().size()) {
                    case 0:
                        key1 = "";
                        break;
                    case 1:
                        key1 = first.getFirst().get(0);
                        break;
                    default:
                        key1 = first.getFirst().toString();
                }
                String key2;
                switch (first.getSecond().size()) {
                    case 0:
                        key2 = "";
                        break;
                    case 1:
                        key2 = first.getSecond().get(0);
                        break;
                    default:
                        key2 = first.getSecond().toString();
                }
                res.addFirst("[" + key1 + "=>" + key2 + "]=" + pair.getSecond());
            }
        }
        List<Pair<String, Long>> resultOccurrence = getCounter().getResultOccurrence();
        long sum = 0;
        int length = 1;
        for (Pair<String, Long> pair : resultOccurrence) {
            sum += pair.getSecond();
            length = Math.max(length, pair.getFirst().length());
        }
        int length2 = String.valueOf(sum).length();
        res.add(String.format("%" + length + "s =%6.2f%% ; %" + length2 + "d", "ALL", 100.0, sum));
        for (Pair<String, Long> pair : resultOccurrence) {
            res.add(String.format("%" + length + "s =%6.2f%% ; %" + length2 + "d", pair.toString(), (((double) pair.getSecond()) / sum * 100), pair.getSecond()));
        }
        res.add(resultOccurrence.toString());
        return res;
    }

    private static class CostCalculatorIntern implements PathCalculatorExpanded.ICostCalculator<String, String> {

        private List<String> recos;
        private List<String> refs;
        private PathCalculatorExpanded.DistanceMat<String, String> mat;
        private final ICostCalculator cc;
        private final PathCalculatorExpanded.Manipulation manipulation;
        private final List<String> emptyList = new ArrayList<>(0);

        public CostCalculatorIntern(ICostCalculator cc, PathCalculatorExpanded.Manipulation manipulation) {
            this.cc = cc;
            this.manipulation = manipulation;
        }

        @Override
        public void init(PathCalculatorExpanded.DistanceMat<String, String> mat, List<String> recos, List<String> refs) {
            this.mat = mat;
            this.recos = recos;
            this.refs = refs;
        }

        @Override
        public boolean isValid(int y, int x) {
            switch (manipulation) {
                case SUB:
                    return (y > 0 && x > 0);
                case INS:
                    return x > 0;
                case DEL:
                    return y > 0;
                default:
                    throw new RuntimeException("unexpected state " + manipulation + ".");
            }
        }

        @Override
        public PathCalculatorExpanded.IDistance<String, String> getDistance(int y, int x) {
            switch (manipulation) {
                case SUB: {
                    final double cost = cc.getCostSubstitution(recos.get(y), refs.get(x));
                    return new PathCalculatorExpanded.Distance<>(cost == 0 ? PathCalculatorExpanded.Manipulation.COR : PathCalculatorExpanded.Manipulation.SUB,
                            this,
                            cost,
                            mat.get(y - 1, x - 1).getCostsAcc() + cost,
                            new int[]{y - 1, x - 1},
                            recos.get(y),
                            refs.get(x));
                }
                case INS: {
                    final double cost = cc.getCostInsertion(refs.get(x));
                    return new PathCalculatorExpanded.Distance<>(manipulation,
                            this,
                            cost,
                            mat.get(y, x - 1).getCostsAcc() + cost,
                            new int[]{y, x - 1},
                            emptyList,
                            refs.get(x));
                }
                case DEL: {
                    final double cost = cc.getCostDeletion(recos.get(y));
                    return new PathCalculatorExpanded.Distance<>(manipulation,
                            this,
                            cost,
                            mat.get(y - 1, x).getCostsAcc() + cost,
                            new int[]{y - 1, x},
                            recos.get(y),
                            emptyList);
                }
                default:
                    throw new RuntimeException("not expected manipulation " + manipulation);
            }
        }
    }

    @Override
    public ObjectCounter<String> getCounter() {
        ObjectCounter<String> objectCounter = new ObjectCounter<>();
        for (Pair<String, Long> pair : counter.getResultOccurrence()) {
            objectCounter.add(pair.getFirst(), pair.getSecond());
        }
        return objectCounter;
    }

}
