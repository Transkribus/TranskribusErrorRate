/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import java.util.List;

/**
 *
 * @author tobias
 */
public interface IRankingMeasure {

    public Stats calcStat(KwsMatchList matches);

    public static class Stats {
        public int falsePositives;
        public int falseNegatives;
        public int corrects;
        public double measure;

        public Stats() {
        }
    }
}
