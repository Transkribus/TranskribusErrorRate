/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.aligner;

import java.awt.Polygon;

/**
 *
 * @author gundram
 */
public interface IBaseLineAligner {

    public IAlignerResult getAlignment(Polygon[] baseLineGT, Polygon[] baseLineLA, Polygon[] baseLineHyp, double thresh, String[] props);

    public int[][] getGTLists(Polygon[] baseLineGT, double[] tolerances, Polygon[] baseLineKeywordHyp, double thresh);

    public double[] calcTolerances(Polygon[] polyTruthNorm);

    public interface IAlignerResult {

        public int[][] getGTLists();

        public double[] getRecallsLA();

        public double[] getRecallsHyp();
    }

}
