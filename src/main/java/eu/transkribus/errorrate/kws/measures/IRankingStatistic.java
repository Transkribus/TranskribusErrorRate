/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.kws.KwsMatchList;
import java.util.List;

/**
 *
 * @author tobias
 */
public interface IRankingStatistic {

    public enum Statistic {
        PR_CURVE(new PRCurve()),
        M_PR_CURVE(new MPRCurve());
        private IRankingStatistic method;

        private Statistic(IRankingStatistic method) {
            this.method = method;
        }

        public IRankingStatistic getMethod() {
            return method;
        }

    }

    public double[] calcStatistic(List<KwsMatchList> matchlists);
}
