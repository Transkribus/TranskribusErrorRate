/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.text2image;

import java.awt.Polygon;

/**
 *
 * @author gundram
 */
public interface IBaseLineAligner {

    public IAlignerResult getAlignment(Polygon[] baseLineGT, Polygon[] baseLineLA, Polygon[] baseLineHyp, double thresh, String[] props);

    public int[][] getGTLists(Polygon[] baseLineGT, Polygon[] baseLineHyp, double thresh);

    public interface IAlignerResult {

        public int[][] getGTLists();

        public double[] getRecallsLA();

        public double[] getRecallsHyp();
    }

}
