/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.kws.measures.IRankingMeasure;
import eu.transkribus.errorrate.kws.measures.IRankingStatistic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.transkribus.errorrate.aligner.IBaseLineAligner;
import eu.transkribus.errorrate.types.KWS;
import eu.transkribus.errorrate.types.KWS.GroundTruth;
import eu.transkribus.errorrate.types.KWS.Line;
import eu.transkribus.errorrate.types.KWS.Page;
import eu.transkribus.errorrate.types.KWS.Word;
import eu.transkribus.errorrate.util.PolygonUtil;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tobias
 */
public class KWSEvaluationMeasure {

    private final IBaseLineAligner aligner;
    private KWS.Result hypo;
    private KWS.GroundTruth ref;
    List<KWS.MatchList> matchLists;
    private double thresh = 0.5;
    private double toleranceDefault = 20.0;
    private boolean useLineBaseline = true;
    private static Logger LOG = LoggerFactory.getLogger(KWSEvaluationMeasure.class);
    private MatchObserver mo = null;

    public KWSEvaluationMeasure(IBaseLineAligner aligner) {
        this.aligner = aligner;
    }

    public void setResults(KWS.Result hypo) {
        this.hypo = hypo;
        matchLists = null;
    }

    public void setMatchObserver(MatchObserver mo) {
        this.mo = mo;
    }

    public static interface MatchObserver {

        public void evalMatch(KWS.MatchList list);
    }

    public void setGroundtruth(KWS.GroundTruth ref) {
        this.ref = ref;
        calcTolerances();
        if (useLineBaseline) {
            setLineBaseline();
        }
        matchLists = null;
    }

    private void setLineBaseline() {
        for (Page page : ref.getPages()) {
            for (Line line : page.getLines()) {
                Polygon baseline = line.getBaseline();
                if (baseline == null) {
                    continue;
                }
                for (List<Polygon> value : line.getKeyword2Baseline().values()) {
                    for (int i = 0; i < value.size(); i++) {
                        value.set(i, baseline);
                    }
                }
            }
        }
    }

    private void calcTolerances() {
        for (Page page : ref.getPages()) {
            List<KWS.Line> lineList = page.getLines();
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

    private void calcMatchLists() {
        if (matchLists == null) {
            List<Pair<KWS.Word, KWS.Word>> l = alignWords(hypo.getKeywords(), ref);
            LinkedList<KWS.MatchList> ml = new LinkedList<>();
            HashMap<String, Page> refPagewise = new HashMap<>();
            for (Page page : ref.getPages()) {
                refPagewise.put(page.getPageID(), page);
            }
            for (Pair<KWS.Word, KWS.Word> pair : l) {
                KWS.Word refs = pair.getSecond();
                KWS.Word hypos = pair.getFirst();
                KWS.MatchList matchList = new KWS.MatchList(hypos, refs, aligner, toleranceDefault, thresh);
                if (mo != null) {
                    mo.evalMatch(matchList);
                }
                ml.add(matchList);
                LOG.trace("for keyword '{}' found {} gt and {} hyp", refs.getKeyWord(), refs.getPos().size(), hypos.getPos().size());
            }
            this.matchLists = ml;
        }
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
    public Map<IRankingMeasure.Measure, Double> getMeasure(IRankingMeasure.Measure... ms) {
        return getMeasure(Arrays.asList(ms));
    }
//    }

    public List<KWS.MatchList> getMatchList() {
        calcMatchLists();
        return matchLists;
    }

    public Map<IRankingMeasure.Measure, Double> getMeasure(Collection<IRankingMeasure.Measure> ms) {
        calcMatchLists();
        HashMap<IRankingMeasure.Measure, Double> ret = new HashMap<>();
        for (IRankingMeasure.Measure m : ms) {
            ret.put(m, m.getMethod().calcMeasure(matchLists));
        }
        return ret;
    }

    public Map<IRankingStatistic.Statistic, double[]> getStats(Collection<IRankingStatistic.Statistic> ss) {
        calcMatchLists();
        HashMap<IRankingStatistic.Statistic, double[]> ret = new HashMap<>();
        for (IRankingStatistic.Statistic s : ss) {
            ret.put(s, s.getMethod().calcStatistic(matchLists));
        }
        return ret;

    }

    private List<Pair<KWS.Word, KWS.Word>> alignWords(Set<Word> keywords_hypo, GroundTruth keywords_ref) {
        HashMap<String, KWS.Word> wordsRef = generateMap(keywords_ref);
        HashMap<String, KWS.Word> wordsHyp = generateMap(keywords_hypo);

        Set<String> queryWords = new HashSet<>();
        queryWords.addAll(wordsHyp.keySet());
        queryWords.addAll(wordsRef.keySet());

        LinkedList<Pair<KWS.Word, KWS.Word>> ret = new LinkedList<>();
        for (String queryWord : queryWords) {
            Word wordRef = wordsRef.get(queryWord);
            Word wordHyp = wordsHyp.get(queryWord);
            if (wordHyp == null) {
                wordHyp = new Word(queryWord);
            }
            if (wordRef == null) {
                wordRef = new Word(queryWord);
            }
            ret.add(new Pair<>(wordHyp, wordRef));
        }
        return ret;
    }

    private HashMap<String, KWS.Word> generateMap(Set<KWS.Word> keywords) {
        HashMap<String, KWS.Word> words = new HashMap<>();
        for (KWS.Word kwsWord : keywords) {
            words.put(kwsWord.getKeyWord(), kwsWord);
        }
        return words;
    }

    private HashMap<String, KWS.Word> generateMap(GroundTruth keywords_ref) {
        HashMap<String, KWS.Word> ret = new HashMap<>();
        for (Page page : keywords_ref.getPages()) {
            for (Line line : page.getLines()) {
                for (Map.Entry<String, List<Polygon>> entry : line.getKeyword2Baseline().entrySet()) {
                    KWS.Word word = ret.get(entry.getKey());
                    if (word == null) {
                        word = new Word(entry.getKey());
                        ret.put(entry.getKey(), word);
                    }
                    for (Polygon polygon : entry.getValue()) {
                        KWS.Entry ent = new KWS.Entry(Double.NaN, null, polygon, page.getPageID());
                        ent.setParentLine(line);
                        word.add(ent);
                    }
                }
            }
        }
        LOG.info("groundtruth map has {} keywords", ret.size());
        return ret;
    }

    public static void main(String[] args) {
        GroundTruth gt = new GroundTruth();
        Page page = new Page("page1");
        Line line = new Line("");
        line.addKeyword("AA", PolygonUtil.string2Polygon("0,0 1,1"));
        line.addKeyword("AA", PolygonUtil.string2Polygon("0,0 2,2"));
        line.addKeyword("BB", PolygonUtil.string2Polygon("1,1 2,2"));
        page.addLine(line);
        gt.addPages(page);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        System.out.println(gson.toJson(gt));

    }
}
