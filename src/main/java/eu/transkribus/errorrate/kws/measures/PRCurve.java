/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.types.KWS.Match;
import eu.transkribus.errorrate.types.KWS.MatchList;
import static eu.transkribus.errorrate.types.KWS.Type.FALSE_NEGATIVE;
import static eu.transkribus.errorrate.types.KWS.Type.FALSE_POSITIVE;
import static eu.transkribus.errorrate.types.KWS.Type.TRUE_POSITIVE;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gundram
 */
public class PRCurve implements IRankingStatistic {

    private static Logger LOG = LoggerFactory.getLogger(PRCurve.class);

    public static double[] getStat(MatchList matchList) {
        if (matchList.getRefSize() == 0) {
            LOG.warn("count of gt == 0, count of matches is {} return double[matches.size()+1] with 1.0 on first index and 0.0 everywhere else", matchList.matches.size());
            double[] res = new double[matchList.matches.size() + 1];
            res[0] = 1.0;
            return res;
        }
        List<Double> precs = new LinkedList<>();
//        double[] res = new double[matches.size()];
        int fp = 0;
        int fn = 0;
        int tp = 0;
        int idx = 0;
        int gt = matchList.getRefSize();
        for (Match match : matchList.matches) {
            switch (match.type) {
                case FALSE_NEGATIVE:
                    fn++;
                    break;
                case FALSE_POSITIVE:
                    fp++;
                    break;
                case TRUE_POSITIVE:
                    tp++;
                    precs.add(((double) tp) / (tp + fp));
            }
        }
        if (gt != tp + fn) {
            LOG.warn("number of gt = {} is not the same as the sum of tp = {} + fn = {}.", gt, tp, fn);
        }
        double[] res = new double[tp + fn + 1];
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
