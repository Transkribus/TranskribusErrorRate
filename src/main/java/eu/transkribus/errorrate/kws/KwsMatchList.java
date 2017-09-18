/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.aligner.BaseLineAligner;
import eu.transkribus.errorrate.aligner.IBaseLineAligner;
import eu.transkribus.errorrate.types.KwsEntry;
import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.types.KwsWord;
import java.awt.Polygon;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author tobias
 */
public class KwsMatchList {

    public List<KwsMatch> matches;
    public int ref_size;
    private static Logger log = Logger.getLogger(KwsMatchList.class);

    public KwsMatchList() {
        matches = new LinkedList<>();
    }

    KwsMatchList(List<KwsMatch> matches, int ref_size) {
        this.matches = matches;
        this.ref_size = ref_size;

    }

    KwsMatchList(KwsWord hypos, KwsWord refs, KwsGroundTruth ref) {
        this(match(hypos, refs, ref), refs.size());
    }

    private static List<KwsMatch> match(KwsWord hypos, KwsWord refs, KwsGroundTruth ref) {
        IBaseLineAligner aligner = new BaseLineAligner();

        String keyWord = hypos.getKeyWord();

        HashMap<String, List<Polygon>> page2PolyHypos = generatePloys(hypos);
        HashMap<String, List<Polygon>> page2PolyRefs = generatePloys(refs);

        HashMap<String, List<Polygon>> page2AllBaselines = getAllLines(ref);
        LinkedList<KwsMatch> ret = new LinkedList<>();

        for (String pageID : page2AllBaselines.keySet()) {

            List<Polygon> allLines = page2AllBaselines.get(pageID);
            List<Polygon> polyHypos = page2PolyHypos.get(pageID);
            List<Polygon> polyRefs = page2PolyRefs.get(pageID);
            if (polyHypos == null) {
                polyHypos = new LinkedList<>();
            }
            if (polyRefs == null) {
                polyRefs = new LinkedList<>();
            }

            int[][] idcs = aligner.getAlignment(polyHypos, polyRefs, allLines);

            LinkedList<Integer> idsNotFound = new LinkedList<Integer>();
            for (int i = 0; i < polyRefs.size(); i++) {
                idsNotFound.add(i);
            }
            for (int i = 0; i < idcs.length; i++) {
                int[] idsPerHypo = idcs[i];
                KwsEntry hypo = hypos.getPos().get(i);
                if (idsPerHypo.length > 2) {
                    log.log(Level.ERROR, "two ground truch baselines match to one querry baseline. Schould not happen. Don't know what to do, so I ignore it!");
                    continue;
                }
                if (idsPerHypo.length > 0) {
                    for (int id : idsPerHypo) {
                        idsNotFound.remove(id);
                        KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.match, hypo, keyWord);
                        ret.add(kwsMatch);
                    }
                } else {
                    KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.falsePositve, hypo, keyWord);
                    ret.add(kwsMatch);
                }
            }
            for (Integer integer : idsNotFound) {
                Polygon get = polyRefs.get(integer);
                KwsMatch kwsMatch = new KwsMatch(KwsMatch.Type.falsePositve, Double.NEGATIVE_INFINITY, get, pageID, keyWord);
                ret.add(kwsMatch);
            }
        }
        return ret;

    }

    public void sort() {
        Collections.sort(matches);
    }

    private static HashMap<String, List<Polygon>> generatePloys(KwsWord kwsWords) {
        HashMap<String, List<Polygon>> ret = new HashMap<>();
        for (KwsEntry pos : kwsWords.getPos()) {
            String pageID = pos.getImage();
            List<Polygon> get = ret.get(pageID);
            if (get == null) {
                get = new LinkedList<>();
                ret.put(pageID, get);
            }
            get.add(pos.getPoly());

        }
        return ret;
    }

    private static HashMap<String, List<Polygon>> getAllLines(KwsGroundTruth ref) {
        HashMap<String, List<Polygon>> ret = new HashMap<>();
        for (KwsPage page : ref.getPages()) {
            LinkedList<Polygon> pagePolys = new LinkedList<Polygon>();
            ret.put(page.getPageID(), pagePolys);
            for (KwsLine line : page.getLines()) {
                pagePolys.add(line.getBaseline());
            }
        }
        return ret;
    }

}
