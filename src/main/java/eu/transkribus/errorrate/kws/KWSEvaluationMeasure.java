/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.transkribus.errorrate.aligner.BaseLineAligner;
import eu.transkribus.errorrate.aligner.IBaseLineAligner;
import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsResult;
import eu.transkribus.errorrate.types.KwsWord;
import eu.transkribus.errorrate.util.PolygonUtil;
import java.awt.Polygon;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.util.Pair;

/**
 *
 * @author tobias
 */
public class KWSEvaluationMeasure {

    private final IBaseLineAligner aligner;
    private KwsResult hypo;
    private KwsGroundTruth ref;
    List<KwsMatchList> matchLists;
    private double thresh = 0.01;
    private double toleranceDefault = 20.0;

    public KWSEvaluationMeasure(IBaseLineAligner aligner) {
        this.aligner = aligner;
    }

    public void setResults(KwsResult hypo) {
        this.hypo = hypo;
        matchLists = null;
    }

    public void setGroundtruth(KwsGroundTruth ref) {
        this.ref = ref;
        calcTolerances();
        matchLists = null;
    }

    private void calcTolerances() {
        for (KwsPage page : ref.getPages()) {
            List<KwsLine> lineList = page.getLines();
            Polygon[] lines = new Polygon[lineList.size()];
            for (int i = 0; i < lineList.size(); i++) {
                lines[i] = lineList.get(i).getBaseline();
            }
            double[] calcTols = aligner.calcTolerances(lines);
            for (int i = 0; i < lineList.size(); i++) {
                lineList.get(i).setTolerance(calcTols[i]);
            }
        }
    }

    private void createMatchLists() {
        List<Pair<KwsWord, KwsWord>> l = alignWords(hypo.getKeywords(), ref);
        LinkedList<KwsMatchList> ml = new LinkedList<>();
        HashMap<String, KwsPage> refPagewise = new HashMap<>();
        for (KwsPage page : ref.getPages()) {
            refPagewise.put(page.getPageID(), page);
        }
        for (Pair<KwsWord, KwsWord> pair : l) {
            KwsWord refs = pair.getSecond();
            KwsWord hypos = pair.getFirst();
            KwsMatchList matchList = new KwsMatchList(hypos, refs, aligner, toleranceDefault, thresh);
            ml.add(matchList);

        }
        this.matchLists = ml;
    }

//    public String getStats() {
//        if (meanStats == null && globalStats == null) {
//            getGlobalMearsure();
//            return globalStats.toString();
//        }
//        if (meanStats == null) {
//            return globalStats.toString();
//        } else {
//            return meanStats.toString();
//        }
//    }
    public Map<IRankingMeasure.Measure, Double> getMeasure(Collection<IRankingMeasure.Measure> ms) {
        if (matchLists == null) {
            createMatchLists();
        }
        HashMap<IRankingMeasure.Measure, Double> ret = new HashMap<>();
        IRankingMeasure measure;
        for (IRankingMeasure.Measure m : ms) {
            switch (m) {
                case GAP:
                    measure = new GlobalAveragePrecision();
                    ret.put(m, measure.calcMeasure(matchLists));
                    break;
                case MAP:
                    measure = new MeanAveragePrecision();
                    ret.put(m, measure.calcMeasure(matchLists));
                    break;
                case PRECISION:
                case RECALL:
                case R_PRECISION:
                case G_NCDG:
                case M_NCDG:
                case PRECISION_AT_10:
                default:
                    throw new UnsupportedOperationException("Not supported yet");
            }

        }

        return ret;
    }

    public Map<IRankingStatistic.Statistic, double[]> getStats(Collection<IRankingStatistic.Statistic> ss) {
        HashMap<IRankingStatistic.Statistic, double[]> ret = new HashMap<IRankingStatistic.Statistic, double[]>();
        IRankingStatistic stats;
        for (IRankingStatistic.Statistic s : ss) {
            switch (s) {
                case PR_CURVE:
                    stats = new PRCurve();
                    ret.put(s, stats.calcStatistic(matchLists));
                    break;
                case M_PR_CURVE:
                    throw new UnsupportedOperationException("Not yet supported");
            }

        }
        return ret;

    }

    private List<Pair<KwsWord, KwsWord>> alignWords(Set<KwsWord> keywords_hypo, KwsGroundTruth keywords_ref) {
        HashMap<String, KwsWord> wordsRef = generateMap(keywords_ref);
        HashMap<String, KwsWord> wordsHyp = generateMap(keywords_hypo);

        Set<String> queryWords = new HashSet<>();
        queryWords.addAll(wordsHyp.keySet());
        queryWords.addAll(wordsRef.keySet());

        LinkedList<Pair<KwsWord, KwsWord>> ret = new LinkedList<>();
        for (String queryWord : queryWords) {
            KwsWord wordRef = wordsRef.get(queryWord);
            KwsWord wordHyp = wordsHyp.get(queryWord);
            if (wordHyp == null) {
                wordHyp = new KwsWord(queryWord);
            }
            if (wordRef == null) {
                wordRef = new KwsWord(queryWord);
            }
            ret.add(new Pair<>(wordHyp, wordRef));
        }
        return ret;
    }

    private HashMap<String, KwsWord> generateMap(Set<KwsWord> keywords) {
        HashMap<String, KwsWord> words = new HashMap<>();
        for (KwsWord kwsWord : keywords) {
            words.put(kwsWord.getKeyWord(), kwsWord);
        }
        return words;
    }

    private HashMap<String, KwsWord> generateMap(KwsGroundTruth keywords_ref) {
        HashMap<String, KwsWord> ret = new HashMap<>();
        for (KwsPage page : keywords_ref.getPages()) {
            for (KwsLine line : page.getLines()) {
                for (Map.Entry<String, List<Polygon>> entry : line.getKeyword2Baseline().entrySet()) {
                    KwsWord word = ret.get(entry.getKey());
                    if (word == null) {
                        word = new KwsWord(entry.getKey());
                        ret.put(entry.getKey(), word);
                    }
                    for (Polygon polygon : entry.getValue()) {
                        KwsEntry ent = new KwsEntry(Double.NaN, null, polygon, page.getPageID());
                        ent.setParentLine(line);
                        word.add(ent);
                    }
                }
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        KwsGroundTruth gt = new KwsGroundTruth();
        KwsPage page = new KwsPage("page1");
        KwsLine line = new KwsLine("");
        line.addKeyword("AA", PolygonUtil.string2Polygon("0,0 1,1"));
        line.addKeyword("AA", PolygonUtil.string2Polygon("0,0 2,2"));
        line.addKeyword("BB", PolygonUtil.string2Polygon("1,1 2,2"));
        page.addLine(line);
        gt.addPages(page);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        System.out.println(gson.toJson(gt));

    }
}
