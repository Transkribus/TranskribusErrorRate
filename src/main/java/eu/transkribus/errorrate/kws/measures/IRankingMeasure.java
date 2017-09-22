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
public interface IRankingMeasure {

    public enum Measure {
        MAP(new MeanAveragePrecision()),
        GAP(new GlobalAveragePrecision()),
        PRECISION(new Precision()),
        RECALL(new Recall()),
        R_PRECISION(new RPrecision()),
        G_NCDG(new GNDCG()),
        M_NCDG(new MNDCG()),
        PRECISION_AT_10(new PrecisionAt10());

        private IRankingMeasure method;

        private Measure(IRankingMeasure method) {
            this.method = method;
        }

        public IRankingMeasure getMethod() {
            return method;
        }

    }

    public double calcMeasure(List<KwsMatchList> matchlists);

//    public Stats calcStat(KwsMatchList matches);
//    public static class Stats {
//
//        public int gt_size;
//        public int falsePositives;
//        public int falseNegatives;
//        public int truePositives;
//        public double measure;
//        public int queries;
//
//        public Stats() {
//            queries = 0;
//        }
//
//        Stats(double measure, int truePositives, int falsePositives, int falseNegatives, int gt_size) {
//            this.measure = measure;
//            this.truePositives = truePositives;
//            this.falsePositives = falsePositives;
//            this.falseNegatives = falseNegatives;
//            this.gt_size = gt_size;
//        }
//
//        public void accumulate(Stats calcStat) {
//            queries++;
//            falseNegatives += calcStat.falseNegatives;
//            falsePositives += calcStat.falsePositives;
//            measure += calcStat.gt_size == 0
//                    ? (falsePositives == 0 ? 1 : 0)
//                    : (double) calcStat.truePositives / calcStat.gt_size;
//            gt_size += calcStat.gt_size;
//            truePositives += calcStat.truePositives;
//        }
//
//        @Override
//        public String toString() {
//            return "Stats{" + "gt_size=" + gt_size + ", falsePositives=" + falsePositives + ", falseNegatives=" + falseNegatives + ", truePositives=" + truePositives + '}';
//        }
//
//    }
}
