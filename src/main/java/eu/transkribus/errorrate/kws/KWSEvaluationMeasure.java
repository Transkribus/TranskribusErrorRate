/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsResult;
import eu.transkribus.errorrate.types.KwsWord;
import java.awt.Polygon;
import java.util.HashMap;
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

    private final IRankingMeasure measure;
    private double meanMeasure;
    private KwsResult hypo;
    private KwsGroundTruth ref;
    List<KwsMatchList> matchLists;
    private double globalMeasure;

    public KWSEvaluationMeasure(IRankingMeasure measure) {
        this.measure = measure;
    }

    public void setResults(KwsResult hypo) {
        this.hypo = hypo;
        matchLists = null;
    }

    public void setGroundtruth(KwsGroundTruth ref) {
        this.ref = ref;
        matchLists = null;
    }

    private void createMatchLists() {
        List<Pair<KwsWord, KwsWord>> l = alignWords(hypo.getKeywords(), ref);
        LinkedList<KwsMatchList> ml = new LinkedList<>();

        for (Pair<KwsWord, KwsWord> pair : l) {
            KwsWord refs = pair.getSecond();
            KwsWord hypos = pair.getFirst();
            KwsMatchList matchList = new KwsMatchList(hypos, refs,ref);
            ml.add(matchList);

        }
        setMatchLists(ml);
    }

    public void setMatchLists(LinkedList<KwsMatchList> matchLists) {
        this.matchLists = matchLists;
        meanMeasure = Double.NaN;
        globalMeasure = Double.NaN;
    }

    public double getMeanMearsure() {
        if (matchLists == null) {
            createMatchLists();
        }
        if (Double.isNaN(meanMeasure)) {
            meanMeasure = 0.0;
            for (KwsMatchList matchList : matchLists) {
                matchList.sort();
                double value = measure.calcStat(matchList);
                meanMeasure += matchList.ref_size == 0
                        ? (matchList.matches.isEmpty() ? 1 : 0)
                        : value / matchList.ref_size;
            }
            meanMeasure /= matchLists.size();
        }

        return meanMeasure;
    }

    public double getGlobalMearsure() {
        if (matchLists == null) {
            createMatchLists();
        }
        if (Double.isNaN(globalMeasure)) {
            LinkedList<KwsMatch> list = new LinkedList<>();
            int ref_size = 0;
            for (KwsMatchList matchList : matchLists) {
                list.addAll(matchList.matches);
                ref_size += matchList.ref_size;
            }
            KwsMatchList kwsMatchList = new KwsMatchList(list, ref_size);
            kwsMatchList.sort();
            globalMeasure = measure.calcStat(kwsMatchList);
        }

        return globalMeasure;
    }

    private List<Pair<KwsWord, KwsWord>> alignWords(Set<KwsWord> keywords_hypo, KwsGroundTruth keywords_ref) {
        HashMap<String, KwsWord> wordsRef = generateMap(keywords_ref);
        HashMap<String, KwsWord> wordsHyp = generateMap(keywords_hypo);

        LinkedList<String> querryWords = new LinkedList<>();
        querryWords.addAll(wordsHyp.keySet());
        querryWords.addAll(wordsRef.keySet());

        LinkedList<Pair<KwsWord, KwsWord>> ret = new LinkedList<>();
        for (String querryWord : querryWords) {
            KwsWord wordRef = wordsRef.get(querryWord);
            KwsWord wordHyp = wordsHyp.get(querryWord);
            if (wordHyp == null) {
                wordHyp = new KwsWord(querryWord);
            }
            if (wordRef == null) {
                wordRef = new KwsWord(querryWord);
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
                        word.add(ent);
                    }
                }
            }
        }

        return ret;
    }

}
