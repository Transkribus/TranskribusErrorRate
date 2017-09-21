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
        PR_CURVE, M_PR_CURVE
    }

    public double[] calcStatistic(List<KwsMatchList> matchlists);
}
