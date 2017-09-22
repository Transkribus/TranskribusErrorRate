/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.kws.KwsMatch;
import eu.transkribus.errorrate.kws.KwsMatchList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class GlobalAveragePrecision extends AveragePrecision {

    @Override
    public double calcMeasure(List<KwsMatchList> matchlists) {
        LinkedList<KwsMatch> list = new LinkedList<>();
        for (KwsMatchList matchList : matchlists) {
            list.addAll(matchList.matches);
        }
        KwsMatchList kwsMatchList = new KwsMatchList(list, matchlists.get(0).getAligner());
        kwsMatchList.sort();
        return calcAveragePrecision(kwsMatchList);
    }

}
