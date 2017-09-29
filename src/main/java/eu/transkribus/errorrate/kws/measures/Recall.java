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
public class Recall implements IRankingMeasure {

    @Override
    public double calcMeasure(List<KwsMatchList> matchlists) {
        LinkedList<KwsMatch> list = new LinkedList<>();
        int ref_size = 0;
        for (KwsMatchList matchList : matchlists) {
            list.addAll(matchList.matches);
        }
        KwsMatchList kwsMatchList = new KwsMatchList(list);
        kwsMatchList.sort();
        return calcRecall(kwsMatchList);
    }

    private double calcRecall(KwsMatchList matches) {
        double numOfTp = 0.0;
        int gt = matches.getRefSize();
        if (gt == 0) {
            if (matches.matches.isEmpty()) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        matches.sort();

        for (KwsMatch match : matches.matches) {
            switch (match.type) {
                case TRUE_POSITIVE:
                    numOfTp++;
            }
        }
        return numOfTp / gt;
    }

}
