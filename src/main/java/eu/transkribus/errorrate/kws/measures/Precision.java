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
public class Precision implements IRankingMeasure {

    @Override
    public double calcMeasure(List<KwsMatchList> matchlists) {
        LinkedList<KwsMatch> list = new LinkedList<>();
        int ref_size = 0;
        for (KwsMatchList matchList : matchlists) {
            list.addAll(matchList.matches);
        }
        KwsMatchList kwsMatchList = new KwsMatchList(list, matchlists.get(0).getAligner());
        kwsMatchList.sort();
        return calcPrecision(kwsMatchList, kwsMatchList.getHypSize());
    }

    public double calcPrecision(KwsMatchList matches, int stopp) {
        double prec = 0.0;
        int gt = matches.getRefSize();
        if (gt == 0) {
            if (matches.matches.isEmpty()) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        matches.sort();

        int lastId = stopp;
        if (matches.matches.size() < stopp) {
            lastId = matches.matches.size();
        }
        for (int i = 0; i < lastId; i++) {
            KwsMatch match = matches.matches.get(i);
            switch (match.type) {
                case TRUE_POSITIVE:
                    prec++;
            }
        }
        return prec / stopp;
    }

}
