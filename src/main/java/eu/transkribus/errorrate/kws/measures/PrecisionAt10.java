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
public class PrecisionAt10 extends Precision {

    @Override
    public double calcMeasure(List<KwsMatchList> matchlists) {
        double sum = 0.0;
        for (KwsMatchList matchList : matchlists) {
            matchList.sort();
            sum += calcPrecision(matchList, 10);
        }
        return sum / matchlists.size();
    }

}
