/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.aligner.BaseLineAligner;
import eu.transkribus.errorrate.aligner.IBaseLineAligner;
import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.GroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsWord;
import eu.transkribus.errorrate.util.ObjectCounter;
import java.awt.Polygon;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author tobias
 */
public class KwsMatchList {

    public List<KwsMatch> matches;
    public ObjectCounter<KwsMatch.Type> counter = new ObjectCounter<>();
    private static Logger log = Logger.getLogger(KwsMatchList.class);
//    private final IBaseLineAligner aligner;

    public KwsMatchList(List<KwsMatch> matches) {
//        this(aligner);
        setMatches(matches);
    }

//    private KwsMatchList(IBaseLineAligner aligner) {
//        this.aligner = aligner;
//    }
    public KwsMatchList(KwsWord hypos, KwsWord refs, IBaseLineAligner aligner, double toleranceDefault, double thresh) {
//        this(aligner);
        setMatches(match(hypos, refs, aligner, toleranceDefault, thresh));
    }

    private void setMatches(List<KwsMatch> matches) {
        this.matches = matches;
        counter.reset();
        for (KwsMatch matche : matches) {
            counter.add(matche.type);
        }
    }

    private int getCount(KwsMatch.Type type) {
        return (int) counter.get(type);
    }

    public int getRefSize() {
        return getCount(KwsMatch.Type.TRUE_POSITIVE) + getCount(KwsMatch.Type.FALSE_NEGATIVE);
    }

    public int getHypSize() {
        return getCount(KwsMatch.Type.TRUE_POSITIVE) + getCount(KwsMatch.Type.FALSE_POSITIVE);
    }
//
//    public IBaseLineAligner getAligner() {
//        return aligner;
//    }

    private List<Polygon> getPolys(List<KwsEntry> entries) {
        List<Polygon> res = new LinkedList<>();
        for (KwsEntry entry : entries) {
            res.add(entry.getBaseLineKeyword());
        }
        return res;
    }

    private List<KwsMatch> match(KwsWord hypos, KwsWord refs, IBaseLineAligner aligner, double toleranceDefault, double thresh) {

        String keyWord = hypos.getKeyWord();

        HashMap<String, List<KwsEntry>> page2PolyHypos = generatePloys(hypos);
        HashMap<String, List<KwsEntry>> page2PolyRefs = generatePloys(refs);
        HashMap<String, List<Double>> page2Tolerance = getTolerances(refs);
//        HashMap<String, List<Polygon>> page2AllBaselines = getAllLines(ref);
        LinkedList<KwsMatch> ret = new LinkedList<>();

        Set<String> pages = new HashSet<String>(page2PolyRefs.keySet());
        pages.addAll(page2PolyHypos.keySet());

        for (String pageID : pages) {
//            List<Polygon> allLines = page2AllBaselines.get(pageID);
            List<KwsEntry> polyHypos = page2PolyHypos.get(pageID);
            List<KwsEntry> polyRefs = page2PolyRefs.get(pageID);
            List<Double> toleranceRefs = page2Tolerance.get(pageID);
            if (polyHypos == null) {
                for (KwsEntry polyRef : polyRefs) {
                    KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.FALSE_NEGATIVE, Double.NEGATIVE_INFINITY, polyRef.getBaseLineKeyword(), pageID, keyWord);
                    ret.add(kwsMatch);
                }
                continue;
            }
            if (polyRefs == null) {
                for (KwsEntry polyHypo : polyHypos) {
                    KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.FALSE_POSITIVE, polyHypo, keyWord);
                    ret.add(kwsMatch);
                }
                continue;
            }
            double[] tolerancesVec = new double[toleranceRefs.size()];
            for (int i = 0; i < tolerancesVec.length; i++) {
                Double d = toleranceRefs.get(i);
                if (d == null || Double.isNaN(d) || Double.isInfinite(d)) {
                    tolerancesVec[i] = toleranceDefault;
                } else {
                    tolerancesVec[i] = d;
                }
            }

            int[][] idcs = uniqueAlignment(
                    aligner.getGTLists(
                            getPolys(polyRefs).toArray(new Polygon[0]),
                            tolerancesVec,
                            getPolys(polyHypos).toArray(new Polygon[0]),
                            thresh
                    )
            );

            Set<Integer> idsNotFound = new HashSet<Integer>();
            for (int i = 0; i < polyRefs.size(); i++) {
                idsNotFound.add(i);
            }
            for (int i = 0; i < idcs.length; i++) {
                int[] idsPerHypo = idcs[i];
                KwsEntry hypo = polyHypos.get(i);
                if (idsPerHypo.length > 1) {
                    log.log(Level.ERROR, "two ground truch baselines match to one querry baseline. Schould not happen. Don't know what to do, so I ignore it!");
                    continue;
                }
                if (idsPerHypo.length > 0) {
//                    for (int id : idsPerHypo) {
                    idsNotFound.remove(idsPerHypo[0]);
                    KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.TRUE_POSITIVE, hypo, keyWord);
                    ret.add(kwsMatch);
//                    }
                } else {
                    KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.FALSE_POSITIVE, hypo, keyWord);
                    ret.add(kwsMatch);
                }
            }
            for (Integer integer : idsNotFound) {
                KwsEntry get = polyRefs.get(integer);
                KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.FALSE_NEGATIVE, Double.NEGATIVE_INFINITY, get.getBaseLineKeyword(), pageID, keyWord);
                ret.add(kwsMatch);
            }
        }
        return ret;

    }

    public void sort() {
        Collections.sort(matches);
    }

    private static HashMap<String, List<KwsEntry>> generatePloys(KwsWord kwsWord) {
        HashMap<String, List<KwsEntry>> ret = new HashMap<>();
        List<KwsEntry> poss = kwsWord.getPos();
        Collections.sort(poss, new Comparator<KwsEntry>() {
            @Override
            public int compare(KwsEntry o1, KwsEntry o2) {
                return -Double.compare(o1.getConf(), o2.getConf());
            }
        }
        );
        for (KwsEntry pos : poss) {
            String pageID = pos.getImage();
            List<KwsEntry> get = ret.get(pageID);
            if (get == null) {
                get = new LinkedList<>();
                ret.put(pageID, get);
            }
            get.add(pos);

        }
        return ret;
    }

    private static HashMap<String, List<Double>> getTolerances(KwsWord kwsWords) {
        HashMap<String, List<Double>> ret = new HashMap<>();
        for (KwsEntry pos : kwsWords.getPos()) {
            String pageID = pos.getImage();
            List<Double> get = ret.get(pageID);
            if (get == null) {
                get = new LinkedList<>();
                ret.put(pageID, get);
            }
            KwsLine parentLine = pos.getParentLine();
            if (parentLine == null) {
                throw new NullPointerException("for keywords no parent lines are set.");
            }
            get.add(parentLine.getTolerance());

        }
        return ret;
    }

    private static HashMap<String, List<Polygon>> getAllLines(GroundTruth ref) {
        HashMap<String, List<Polygon>> ret = new HashMap<>();
        for (KwsPage page : ref.getPages()) {
            LinkedList<Polygon> pagePolys = new LinkedList<>();
            ret.put(page.getPageID(), pagePolys);
            for (KwsLine line : page.getLines()) {
                pagePolys.add(line.getBaseline());
            }
        }
        return ret;
    }

    /**
     * Align indices uniquely!
     *
     * ToDo: can produce a false alignment. Das Problem: Aus der gtList wird
     * nach und nach einfach der erstbeste Index entnommen. Das kann aber zu
     * Problemen führen, wenn später ein anderer Match genau diesen schon
     * entnommenen gtList-Index matched, den ignorierten aber nicht mehr. Dann
     * müsste man eigentlich beim ersten Mal einen hinteren Index verwenden und
     * den ersten für den späteren Match aufsparen. Nach hintengeschoben, weil
     * kombinatorisch aufwendig.
     *
     * @param gtLists
     * @return
     */
    private static int[][] uniqueAlignment(int[][] gtLists) {

        int[][] ret = new int[gtLists.length][];
        LinkedList<Integer> refIdcsUsed = new LinkedList<>();
        loop:
        for (int i = 0; i < gtLists.length; i++) {
            int[] gtList = gtLists[i];
            for (int j : gtList) {
                if (!refIdcsUsed.contains(j)) {
                    ret[i] = new int[]{j};
                    refIdcsUsed.add(j);
                    continue loop;
                }
            }
            ret[i] = new int[0];
        }
        return ret;
    }
}
