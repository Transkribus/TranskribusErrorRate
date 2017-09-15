package eu.transkribus.errorrate.aligner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
////////////////////////////////////////////////
import eu.transkribus.errorrate.aligner.IBaseLineAligner.IAlignerResult;
import java.awt.Polygon;
import java.util.Arrays;

/// File:       BaseLineAlignerSameBaselines.java
/// Created:    18.07.2017  14:21:02
/// Encoding:   UTF-8
////////////////////////////////////////////////
/**
 * Desciption of BaseLineAlignerSameBaselines
 *
 *
 * Since 18.07.2017
 *
 * @author Tobias Gruening tobias.gruening.hro@gmail.com
 */
public class BaseLineAlignerSameBaselines implements IBaseLineAligner {

    private static final long serialVersionUID = 1L;

    private static boolean equals(Polygon p1, Polygon p2) {
        if (p1.npoints != p2.npoints) {
            return false;
        }
        for (int i = 0; i < p1.npoints; i++) {
            if (p1.xpoints[i] != p2.xpoints[i] || p1.ypoints[i] != p2.ypoints[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IAlignerResult getAlignment(Polygon[] baseLineGT, Polygon[] baseLineLA, Polygon[] baseLineHyp, double thresh, String[] props) {
        if (baseLineLA.length != baseLineHyp.length) {
            throw new RuntimeException("expect same length of baselines LA and Hyp");
        }
        if (!Arrays.deepEquals(baseLineLA, baseLineHyp)) {
            throw new RuntimeException("baseLineLA and baseLineHyp have to be the same");
        }
        final int[][] res1 = new int[baseLineHyp.length][0];
        for (int i = 0; i < res1.length; i++) {
            Polygon pHyp = baseLineHyp[i];
            for (int j = 0; j < baseLineGT.length; j++) {
                Polygon pGT = baseLineGT[j];
                if (equals(pHyp, pGT)) {
                    res1[i] = new int[]{j};
                    break;
                }
            }
        }
        final double[] res2 = new double[baseLineGT.length];
        Arrays.fill(res2, 1.0);

        return new IAlignerResult() {
            @Override
            public int[][] getGTLists() {
                return res1;
            }

            @Override
            public double[] getRecallsLA() {
                return res2;
            }

            @Override
            public double[] getRecallsHyp() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };
    }

    @Override
    public int[][] getGTLists(Polygon[] baseLineGT, Polygon[] baseLineHyp, double thresh) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
