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
public interface IRankingMeasure {

    public Stats calcStat(KwsMatchList matches);

    public static class Stats {

        public int gt_size;
        public int falsePositives;
        public int falseNegatives;
        public int corrects;
        public double measure;
        public int querries;

        public Stats() {
            querries = 0;
        }

        Stats(double measure, int corrects, int falsePositives, int falseNegatives, int gt_size) {
            this.measure = measure;
            this.corrects = corrects;
            this.falsePositives = falsePositives;
            this.falseNegatives = falseNegatives;
            this.gt_size = gt_size;
        }

        public void accumulate(Stats calcStat) {
            querries++;
            measure += calcStat.gt_size == 0
                    ? (falsePositives == 0 ? 1 : 0)
                    : (double) calcStat.corrects / calcStat.gt_size;
            gt_size += calcStat.gt_size;
            falseNegatives += calcStat.falseNegatives;
            falsePositives += calcStat.falsePositives;
            corrects += calcStat.corrects;
        }

        @Override
        public String toString() {
            return "Stats{" + "gt_size=" + gt_size + ", falsePositives=" + falsePositives + ", falseNegatives=" + falseNegatives + ", corrects=" + corrects + '}';
        }

    }
}
