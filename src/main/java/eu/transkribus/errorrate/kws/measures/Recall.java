/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.types.KWS.Match;
import eu.transkribus.errorrate.types.KWS.MatchList;
import static eu.transkribus.errorrate.types.KWS.Type.TRUE_POSITIVE;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class Recall implements IRankingMeasure {

    @Override
    public double calcMeasure(List<MatchList> matchlists) {
        LinkedList<Match> list = new LinkedList<>();
        int ref_size = 0;
        for (MatchList matchList : matchlists) {
            list.addAll(matchList.matches);
        }
        MatchList kwsMatchList = new MatchList(list);
        kwsMatchList.sort();
        return calcRecall(kwsMatchList);
    }

    private double calcRecall(MatchList matches) {
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

        for (Match match : matches.matches) {
            switch (match.type) {
                case TRUE_POSITIVE:
                    numOfTp++;
            }
        }
        return numOfTp / gt;
    }

}
