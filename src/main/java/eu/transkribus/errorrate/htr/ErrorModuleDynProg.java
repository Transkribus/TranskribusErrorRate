/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.htr;

import eu.transkribus.errorrate.interfaces.ICostCalculator;
import eu.transkribus.errorrate.interfaces.IErrorModule;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.types.PathCalculatorGraph;
import eu.transkribus.errorrate.util.ObjectCounter;
import eu.transkribus.interfaces.IStringNormalizer;
import eu.transkribus.interfaces.ITokenizer;
import eu.transkribus.tokenizer.TokenizerCategorizer;
import eu.transkribus.tokenizer.interfaces.ICategorizer;
import java.util.Arrays;
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

    private final PathCalculatorGraph<String, String> pathCalculator = new PathCalculatorGraph<>();
    private final ObjectCounter<Count> counter = new ObjectCounter<>();
    private final ObjectCounter<RecoRef> counterSub = new ObjectCounter<>();
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
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, "SUB"));
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, "INS"));
        pathCalculator.addCostCalculator(new CostCalculatorIntern(costCalculator, "DEL"));
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
        String[] recos = tokenizer.tokenize(reco).toArray(new String[0]);
        String[] refs = tokenizer.tokenize(ref).toArray(new String[0]);
        //use dynamic programming to calculate the cheapest path through the dynamic programming tabular
        List<PathCalculatorGraph.IDistance<String, String>> calcBestPath = pathCalculator.calcBestPath(recos, refs);
        //go through the path...
        for (PathCalculatorGraph.IDistance<String, String> iDistance : calcBestPath) {
            //count the manipulation, which have to be done at the specific position (Insertion, Deletion, Substitution, Correct)
            Count manipulation = Count.valueOf(iDistance.getManipulation());
            counter.add(manipulation);
            boolean isCorrect = manipulation.equals(Count.COR);
            if (!isCorrect) {
                counter.add(Count.ERR);
            }
            //for a detailed output, add tokens to the substitution/confusion map
            if (detailed == null && !isCorrect) {
                //if only errors should be put into the confusion map
                counterSub.add(new RecoRef(iDistance.getRecos(), iDistance.getReferences()));
                if (iDistance.getRecos().length == 0 && iDistance.getReferences().length == 0) {
                    throw new RuntimeException("error here in the normal mode");
                }
            } else if (detailed != null && detailed) {
                //if everything should be put in the substitution map (also correct manipulation)
                counterSub.add(new RecoRef(iDistance.getRecos(), iDistance.getReferences()));
                if (iDistance.getRecos().length == 0 && iDistance.getReferences().length == 0) {
                    throw new RuntimeException("error here in the other mode");
                }
            }
        }
        counter.add(Count.HYP, recos.length);
        counter.add(Count.GT, refs.length);
    }

    @Override
    public void reset() {
        counter.reset();
        counterSub.reset();
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
            for (Pair<RecoRef, Long> pair : counterSub.getResultOccurrence()) {
                RecoRef first = pair.getFirst();
                String key1;
                switch (first.recos.length) {
                    case 0:
                        key1 = "";
                        break;
                    case 1:
                        key1 = first.recos[0];
                        break;
                    default:
                        key1 = Arrays.toString(first.recos);
                }
                String key2;
                switch (first.refs.length) {
                    case 0:
                        key2 = "";
                        break;
                    case 1:
                        key2 = first.refs[0];
                        break;
                    default:
                        key2 = Arrays.toString(first.refs);
                }
                res.addFirst("[" + key1 + "=>" + key2 + "]=" + pair.getSecond());
            }
        }
        List<Pair<Count, Long>> resultOccurrence = getCounter().getResultOccurrence();
//        long sum = 0;
//        int length = 1;
//        for (Pair<Count, Long> pair : resultOccurrence) {
//            sum += pair.getSecond();
//            length = Math.max(length, pair.getFirst().toString().length());
//        }
//        int length2 = String.valueOf(sum).length();
//        res.add(String.format("%" + length + "s =%6.2f%% ; %" + length2 + "d", "ALL", 100.0, sum));
//        for (Pair<Count, Long> pair : resultOccurrence) {
//            res.add(String.format("%" + length + "s =%6.2f%% ; %" + length2 + "d", pair.toString(), (((double) pair.getSecond()) / sum * 100), pair.getSecond()));
//        }
        res.add(resultOccurrence.toString());
        return res;
    }

    private static class CostCalculatorIntern implements PathCalculatorGraph.ICostCalculator<String, String> {

        private List<String> recos;
        private List<String> refs;
        private PathCalculatorGraph.DistanceMat<String, String> mat;
        private final ICostCalculator cc;
        private final String manipulation;
        private final String[] emptyList = new String[0];

        public CostCalculatorIntern(ICostCalculator cc, String manipulation) {
            this.cc = cc;
            this.manipulation = manipulation;
        }

        @Override
        public PathCalculatorGraph.IDistance<String, String> getNeighbour(int[] point) {
            final int y = point[0];
            final int x = point[1];
            switch (manipulation) {
                case "SUB": {
                    int xx = x + 1;
                    int yy = y + 1;
                    if (yy >= recos.size() || xx >= refs.size()) {
                        return null;
                    }
                    final double cost = cc.getCostSubstitution(recos.get(yy), refs.get(xx));
                    return new PathCalculatorGraph.Distance<>(cost == 0 ? "COR" : "SUB",
                            cost, mat.get(point).getCostsAcc() + cost,
                            new int[]{yy, xx},
                            point, new String[]{recos.get(yy)},
                            new String[]{refs.get(xx)});
                }
                case "INS": {
                    int xx = x + 1;
                    if (xx >= refs.size()) {
                        return null;
                    }
                    final double cost = cc.getCostInsertion(refs.get(xx));
                    return new PathCalculatorGraph.Distance<>("INS",
                            cost, mat.get(point).getCostsAcc() + cost,
                            new int[]{y, xx},
                            point, emptyList,
                            new String[]{refs.get(xx)});
                }
                case "DEL": {
                    int yy = y + 1;
                    if (yy >= recos.size()) {
                        return null;
                    }
                    final double cost = cc.getCostDeletion(recos.get(yy));
                    return new PathCalculatorGraph.Distance<>("DEL",
                            cost, mat.get(point).getCostsAcc() + cost,
                            new int[]{yy, x},
                            point, new String[]{recos.get(yy)},
                            emptyList);
                }
                default:
                    throw new RuntimeException("not expected manipulation " + manipulation);
            }
        }

        @Override
        public void init(PathCalculatorGraph.DistanceMat<String, String> mat, List<String> recos, List<String> refs) {
            this.mat = mat;
            this.recos = recos;
            this.refs = refs;
        }

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
        if (reco.size() == ref.size()) {
            for (int i = 0; i < ref.size(); i++) {
                calculate(reco.get(i), ref.get(i));
            }
        } else {
            calculate(toOneLine(reco), toOneLine(ref));
        }
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
