/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.types.KWS;
import eu.transkribus.errorrate.types.KWS.Match;
import eu.transkribus.errorrate.types.KWS.MatchList;
import static eu.transkribus.errorrate.types.KWS.Type.FALSE_NEGATIVE;
import static eu.transkribus.errorrate.types.KWS.Type.FALSE_POSITIVE;
import static eu.transkribus.errorrate.types.KWS.Type.TRUE_POSITIVE;
import eu.transkribus.errorrate.util.ObjectCounter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gundram
 */
public class WPRCurve implements IRankingStatistic {

    private static Logger LOG = LoggerFactory.getLogger(WPRCurve.class);

    public static double[] getStat(MatchList matchList) {
        if (matchList.getRefSize() == 0) {
            LOG.warn("count of gt == 0, count of matches is {} return double[matches.size()+1] with 1.0 on first index and 0.0 everywhere else", matchList.matches.size());
            double[] res = new double[matchList.matches.size() + 1];
            res[0] = 1.0;
            return res;
        }
        ObjectCounter<String> countGT = new ObjectCounter<>();
        for (Match matche : matchList.matches) {
            if (!matche.type.equals(KWS.Type.FALSE_NEGATIVE)) {
                //then it is tp or fp - wie have the GT word!
                countGT.add(matche.getWord());
            }
        }
        List<Double> precs = new LinkedList<>();
//        double[] res = new double[matches.size()];
        double fp = 0;
        double fn = 0;
        double tp = 0;
        int idx = 0;
        int gt = matchList.getRefSize();
        for (Match match : matchList.matches) {
            switch (match.type) {
                case FALSE_NEGATIVE:
                    fn += 1.0 / countGT.get(match.getWord());
                    break;
                case FALSE_POSITIVE:
                    fp += 1.0 / countGT.get(match.getWord());
                    break;
                case TRUE_POSITIVE:
                    tp += 1.0 / countGT.get(match.getWord());
                    precs.add(tp / (tp + fp));
            }
        }
        if (gt != tp + fn) {
            LOG.warn("number of gt = {} is not the same as the sum of tp = {} + fn = {}.", gt, tp, fn);
        }
        double[] res = new double[gt + 1];
        res[0] = 1.0;
        for (int i = 0; i < precs.size(); i++) {
            res[i + 1] = precs.get(i);
        }
        return res;

    }

    @Override
    public double[] calcStatistic(List<MatchList> matchlists) {
        if (matchlists == null || matchlists.isEmpty()) {
            LOG.error("input is null or empty - return null");
            return null;
        }
        LinkedList<Match> matches = new LinkedList<>();
        for (MatchList match : matchlists) {
            matches.addAll(match.matches);
        }
        Collections.sort(matches);
        return getStat(new MatchList(matches));
    }

}
