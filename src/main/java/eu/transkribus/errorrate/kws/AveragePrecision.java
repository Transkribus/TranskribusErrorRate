/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

/**
 *
 * @author tobias
 */
public class AveragePrecision implements IRankingMeasure {

    @Override
    public Stats calcStat(KwsMatchList matches) {
        double prec = 0.0;
        double ap = 0.0;
        int gt = matches.ref_size;
        if (gt == 0) {
            if (matches.matches.isEmpty()) {
                return new Stats(1.0, 0, 0, 0, gt);
            } else {
                return new Stats(0.0, 0, matches.matches.size(), 0, gt);
            }
        }
        matches.sort();

        int fp = 0;
        int fn = 0;
        int cor = 0;
        int count = 0;
        for (KwsMatch match : matches.matches) {
            count++;
            switch (match.type) {
                case falseNegative:
                    fn++;
                    break;
                case falsePositve:
                    fp++;
                    break;
                case match:
                    cor++;
                    prec++;
                    ap += prec / count;
            }
        }
        return new Stats(ap / gt, cor, fp, fn, gt);
    }

}
