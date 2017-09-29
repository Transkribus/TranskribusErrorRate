/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws.measures;

import eu.transkribus.errorrate.types.KWS.Match;
import eu.transkribus.errorrate.types.KWS.MatchList;
import static eu.transkribus.errorrate.types.KWS.Type.TRUE_POSITIVE;

/**
 *
 * @author tobias
 */
public abstract class AveragePrecision implements IRankingMeasure {

    public static double calcAveragePrecision(MatchList matches) {
        double prec = 0.0;
        double ap = 0.0;
        int gt = matches.getRefSize();
        if (gt == 0) {
            if (matches.matches.isEmpty()) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        matches.sort();

        int count = 0;
        for (Match match : matches.matches) {
            count++;
            switch (match.type) {
//                case FALSE_NEGATIVE:
//                    fn++;
//                    break;
//                case FALSE_POSITIVE:
//                    fp++;
//                    break;
                case TRUE_POSITIVE:
//                    cor++;
                    prec++;
                    ap += prec / count;
            }
        }
        return ap / gt;
    }

}
