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
public class MNDCG extends NDCG {

    @Override
    public double calcMeasure(List<KwsMatchList> matchlists) {
        double sum = 0.0;
        for (KwsMatchList matchList : matchlists) {
            matchList.sort();
            sum += calcNDCG(matchList);
        }
        return sum / matchlists.size();
    }

}
