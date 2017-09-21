/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

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

    Logger LOG = LoggerFactory.getLogger(PRCurve.class);

    @Override
    public double[] calcStatistic(List<KwsMatchList> matchlists) {
        if (matchlists == null || matchlists.isEmpty()) {
            LOG.error("input is null or empty - return null");
            return null;
        }
        LinkedList<KwsMatch> matches = new LinkedList<>();
        int cntRefs = 0;
        for (KwsMatchList match : matchlists) {
            cntRefs += match.getRefSize();
            matches.addAll(match.matches);
        }
        int gt = cntRefs;
        if (gt == 0) {
            if (matches.isEmpty()) {
                LOG.warn("count of gt and matches is 0 - return double[]{1.0}");
                return new double[]{1.0};
            } else {
                LOG.warn("count of gt == 0, count of matches is {} return double[matches.size()}]", matches.size());
                return new double[matches.size()];
            }
        }
        Collections.sort(matches);
        List<Double> precs = new LinkedList<>();
//        double[] res = new double[matches.size()];
        int fp = 0;
        int fn = 0;
        int tp = 0;
        int idx = 0;
        for (KwsMatch match : matches) {
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
        double[] res = new double[tp + fn];
        for (int i = 0; i < precs.size(); i++) {
            res[i] = precs.get(i);
        }
        return res;
    }

}
