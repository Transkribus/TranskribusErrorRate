/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.types.KWS;
import eu.transkribus.errorrate.types.KWS.MatchList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class RPrecision extends Precision {

    @Override
    public double calcMeasure(List<MatchList> matchlists) {
        LinkedList<KWS.Match> list = new LinkedList<>();
        int ref_size = 0;
        for (MatchList matchList : matchlists) {
            list.addAll(matchList.matches);
        }
        MatchList kwsMatchList = new MatchList(list);
        kwsMatchList.sort();
        return calcPrecision(kwsMatchList, kwsMatchList.getRefSize());
    }

}
