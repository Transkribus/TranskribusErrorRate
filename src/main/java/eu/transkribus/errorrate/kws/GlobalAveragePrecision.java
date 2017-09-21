/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

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
        int ref_size = 0;
        for (KwsMatchList matchList : matchlists) {
            list.addAll(matchList.matches);
            ref_size += matchList.getRefSize();
        }
        KwsMatchList kwsMatchList = new KwsMatchList(list, ref_size, matchlists.get(0).getAligner());
        kwsMatchList.sort();
        return calcAveragePrecision(kwsMatchList);
    }

}
