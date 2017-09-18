package eu.transkribus.errorrate.aligner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
////////////////////////////////////////////////
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
public class BaseLineAligner implements IBaseLineAligner {

    private static final long serialVersionUID = 1L;
    private int desPolyTickDist = 5;
    private double relTol = 0.15;
    private int maxD = 250;

    private static double recall(Polygon toHit, Polygon hypo, double tol) {
        double cnt = 0.0;
        Rectangle toCntBB = toHit.getBounds();
        for (int i = 0; i < toHit.npoints; i++) {
            int xA = toHit.xpoints[i];
            int yA = toHit.ypoints[i];
            double minDist = Double.MAX_VALUE;
            Rectangle refBB = hypo.getBounds();
            Rectangle inter = toCntBB.intersection(refBB);
            int minI = Math.min(inter.width, inter.height);
            //Early stopping criterion
            if (minI < -3.0 * tol) {
                continue;
            }
            for (int j = 0; j < hypo.npoints; j++) {
                int xC = hypo.xpoints[j];
                int yC = hypo.ypoints[j];
                minDist = Math.min(Math.abs(xA - xC) + Math.abs(yA - yC), minDist);
//                    minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                if (minDist <= tol) {
                    break;
                }
            }
            if (minDist <= tol) {
                cnt++;
            }
            if (minDist > tol && minDist < 3.0 * tol) {
                cnt += (3.0 * tol - minDist) / (2.0 * tol);
            }
        }
        cnt /= toHit.npoints;
        return cnt;
    }

    private static double recallList(Polygon toHit, Polygon[] hypoL, double tol) {
        double cnt = 0.0;
        Rectangle toCntBB = toHit.getBounds();
        for (int i = 0; i < toHit.npoints; i++) {
            int xA = toHit.xpoints[i];
            int yA = toHit.ypoints[i];
            double minDist = Double.MAX_VALUE;
            for (Polygon hypo : hypoL) {
                Rectangle refBB = hypo.getBounds();
                Rectangle inter = toCntBB.intersection(refBB);
                int minI = Math.min(inter.width, inter.height);
                //Early stopping criterion
                if (minI < -3.0 * tol) {
                    continue;
                }
                for (int j = 0; j < hypo.npoints; j++) {
                    int xC = hypo.xpoints[j];
                    int yC = hypo.ypoints[j];
                    minDist = Math.min(Math.abs(xA - xC) + Math.abs(yA - yC), minDist);
                    //                    minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                    if (minDist <= tol) {
                        break;
                    }
                }
            }
            if (minDist <= tol) {
                cnt++;
            }
            if (minDist > tol && minDist < 3.0 * tol) {
                cnt += (3.0 * tol - minDist) / (2.0 * tol);
            }
        }
        cnt /= toHit.npoints;
        return cnt;
    }

    private int[][] getGtList(Polygon[] baseLineGT, Polygon[] baseLineHyp, double[] tols, double thresh) {
        final int[][] res1;
        if (baseLineHyp == null || baseLineHyp.length == 0) {
            res1 = null;
        } else {
            res1 = new int[baseLineHyp.length][];
            for (int i = 0; i < baseLineHyp.length; i++) {
                Polygon aBL_HYP = baseLineHyp[i];
                if (aBL_HYP == null || aBL_HYP.npoints == 0) {
                    continue;
                }
                int xS = aBL_HYP.xpoints[0];
                int yS = aBL_HYP.ypoints[0];
                List<int[]> accGT_BL = new ArrayList<>();
                int[] tRes = null;
                if (baseLineGT != null) {
                    for (int j = 0; j < baseLineGT.length; j++) {
                        Polygon aBL_GT = baseLineGT[j];
                        double aTol = tols[j];
                        double recall = recall(aBL_HYP, aBL_GT, aTol);
                        if (recall > thresh) {
                            int minD = 1000000;
                            for (int k = 0; k < aBL_GT.npoints; k++) {
                                int aD = Math.abs(aBL_GT.xpoints[k] - xS) + Math.abs(aBL_GT.ypoints[k] - yS);
                                minD = Math.min(aD, minD);
                            }
                            accGT_BL.add(new int[]{j, minD});
                        }
                    }
                    Collections.sort(accGT_BL, new Comparator<int[]>() {
                        @Override
                        public int compare(int[] o1, int[] o2) {
                            return Integer.compare(o1[1], o2[1]);
                        }
                    });
                    if (accGT_BL.size() > 0) {
                        tRes = new int[accGT_BL.size()];
                        for (int j = 0; j < accGT_BL.size(); j++) {
                            tRes[j] = accGT_BL.get(j)[0];
                        }
                    }
                }
                res1[i] = tRes;
            }
        }
        return res1;
    }

    @Override
    public IAlignerResult getAlignment(Polygon[] baseLineGT, Polygon[] baseLineLA, Polygon[] baseLineHyp, double thresh, String[] props) {

        Polygon[] polysTruthNorm = normDesDist(baseLineGT);
        double[] tols = calcTols(polysTruthNorm);
        final int[][] res1 = getGtList(baseLineGT, baseLineHyp, tols, thresh);

        final double[] resLA = new double[baseLineGT.length];
        final double[] resHyp = new double[baseLineGT.length];

        for (int i = 0; i < baseLineGT.length; i++) {
            Polygon aBL_GT = baseLineGT[i];
            double aTol = tols[i];
            resLA[i] = recallList(aBL_GT, baseLineLA, aTol);
            resHyp[i] = recallList(aBL_GT, baseLineHyp, aTol);
        }

        return new IAlignerResult() {
            @Override
            public int[][] getGTLists() {
                return res1;
            }

            @Override
            public double[] getRecallsLA() {
                return resLA;
            }

            @Override
            public double[] getRecallsHyp() {
                return resHyp;
            }

        };
    }

    private Polygon[] normDesDist(Polygon[] polyIn) {
        Polygon[] res = new Polygon[polyIn.length];
        for (int i = 0; i < res.length; i++) {
            Rectangle bb = polyIn[i].getBounds();
            if (bb.width > 100000 || bb.height > 100000) {
                Polygon nPoly = new Polygon();
                nPoly.addPoint(0, 0);
                polyIn[i] = nPoly;
            }
            res[i] = normDesDist(polyIn[i]);
            res[i].getBounds();
        }
        return res;
    }

    private Polygon normDesDist(Polygon polyIn) {
        Polygon polyBlown = blowUp(polyIn);
        return thinOut(polyBlown);
    }

    private Polygon blowUp(Polygon inPoly) {
        Polygon res = new Polygon();
        for (int i = 1; i < inPoly.npoints; i++) {
            int x1 = inPoly.xpoints[i - 1];
            int y1 = inPoly.ypoints[i - 1];
            int x2 = inPoly.xpoints[i];
            int y2 = inPoly.ypoints[i];
            int diffX = Math.abs(x2 - x1);
            int diffY = Math.abs(y2 - y1);
            if (Math.max(diffX, diffY) < 1) {
                if (i == inPoly.npoints - 1) {
                    res.addPoint(x2, y2);
                }
                continue;
            }
            res.addPoint(x1, y1);
            if (diffX >= diffY) {
                for (int j = 1; j < diffX; j++) {
                    int xN;
                    if (x1 < x2) {
                        xN = x1 + j;
                    } else {
                        xN = x1 - j;
                    }
                    int yN = (int) (Math.round(y1 + (double) (xN - x1) * (y2 - y1) / (x2 - x1)));
                    res.addPoint(xN, yN);
                }
            } else {
                for (int j = 1; j < diffY; j++) {
                    int yN;
                    if (y1 < y2) {
                        yN = y1 + j;
                    } else {
                        yN = y1 - j;
                    }
                    int xN = (int) (Math.round(x1 + (double) (yN - y1) * (x2 - x1) / (y2 - y1)));
                    res.addPoint(xN, yN);
                }
            }
            if (i == inPoly.npoints - 1) {
                res.addPoint(x2, y2);
            }
        }
        return res;
    }

    private Polygon thinOut(Polygon polyBlown) {
        Polygon res = new Polygon();
        if (polyBlown.npoints <= 20) {
            return polyBlown;
        }
        int dist = polyBlown.npoints - 1;
        int minPts = 20;
        int desPts = Math.max(minPts, dist / desPolyTickDist + 1);
        double step = (double) dist / (desPts - 1);
        for (int i = 0; i < desPts - 1; i++) {
            int aIdx = (int) (i * step);
            res.addPoint(polyBlown.xpoints[aIdx], polyBlown.ypoints[aIdx]);
        }
        res.addPoint(polyBlown.xpoints[polyBlown.npoints - 1], polyBlown.ypoints[polyBlown.npoints - 1]);
        return res;
    }

    private double[] calcTols(Polygon[] polyTruthNorm) {
        double[] tols = new double[polyTruthNorm.length];

        int lineCnt = 0;
        for (Polygon aPoly : polyTruthNorm) {
            double angle = calcRegLineStats(aPoly)[0];
            double orVecY = Math.sin(angle);
            double orVecX = Math.cos(angle);
            double aDist = maxD;
            double[] ptA1 = new double[]{aPoly.xpoints[0], aPoly.ypoints[0]};
            double[] ptA2 = new double[]{aPoly.xpoints[aPoly.npoints - 1], aPoly.ypoints[aPoly.npoints - 1]};
            for (int i = 0; i < aPoly.npoints; i++) {
                double[] pA = new double[]{aPoly.xpoints[i], aPoly.ypoints[i]};
                for (Polygon cPoly : polyTruthNorm) {
                    if (cPoly != aPoly) {
                        if (getDistFast(pA, cPoly.getBounds()) > aDist) {
                            continue;
                        }
                        double[] ptC1 = new double[]{cPoly.xpoints[0], cPoly.ypoints[0]};
                        double[] ptC2 = new double[]{cPoly.xpoints[cPoly.npoints - 1], cPoly.ypoints[cPoly.npoints - 1]};
                        double inD1 = getInDist(ptA1, ptC1, orVecX, orVecY);
                        double inD2 = getInDist(ptA1, ptC2, orVecX, orVecY);
                        double inD3 = getInDist(ptA2, ptC1, orVecX, orVecY);
                        double inD4 = getInDist(ptA2, ptC2, orVecX, orVecY);
                        if ((inD1 < 0 && inD2 < 0 && inD3 < 0 && inD4 < 0) || (inD1 > 0 && inD2 > 0 && inD3 > 0 && inD4 > 0)) {
                            continue;
                        }

                        for (int j = 0; j < cPoly.npoints; j++) {
                            double[] pC = new double[]{cPoly.xpoints[j], cPoly.ypoints[j]};
                            if (Math.abs(getInDist(pA, pC, orVecX, orVecY)) <= 2 * desPolyTickDist) {
                                aDist = Math.min(aDist, Math.abs(getOffDist(pA, pC, orVecX, orVecY)));
                            }
                        }
                    }
                }
            }
//            System.out.println("Line " + lineCnt + " has min dist of: " + aDist);
//            System.out.println("Line " + lineCnt + " has startX: " + aPoly.xpoints[0] + " and startY: " + aPoly.ypoints[0]);
            if (aDist < maxD) {
                tols[lineCnt] = aDist;
            }
            lineCnt++;
        }
        double sumVal = 0.0;
        int cnt = 0;
        for (int i = 0; i < tols.length; i++) {
            double aTol = tols[i];
            if (aTol != 0) {
                sumVal += aTol;
                cnt++;
            }
        }
        double meanVal = maxD;
        if (cnt != 0) {
            meanVal = sumVal / cnt;
        }

        for (int i = 0; i < tols.length; i++) {
            if (tols[i] == 0) {
                tols[i] = meanVal;
            }
            tols[i] = Math.min(tols[i], meanVal);
            tols[i] *= relTol;
        }

        return tols;
    }

    /**
     *
     * @param p
     * @return #0 - angle #1 - absVal
     */
    private double[] calcRegLineStats(Polygon p) {
        if (p.npoints <= 1) {
            return new double[]{0.0, 0.0};
        }
        double m = 0.0;
        double n = Double.POSITIVE_INFINITY;
        if (p.npoints > 2) {
            int xMax = 0;
            int xMin = Integer.MAX_VALUE;
            for (int i = 0; i < p.npoints; i++) {
                int xVal = p.xpoints[i];
                xMax = Math.max(xMax, xVal);
                xMin = Math.min(xMin, xVal);
            }
            if (xMax == xMin) {
                m = Double.POSITIVE_INFINITY;
            } else {
                int[] xPs = new int[p.npoints];
                int[] yPs = new int[p.npoints];
                for (int i = 0; i < p.npoints; i++) {
                    xPs[i] = p.xpoints[i];
                    yPs[i] = -p.ypoints[i];
                }
                BaseLineAligner.LinRegression lR = new LinRegression();
                double[] calcLine = lR.calcLine(xPs, yPs);
                m = calcLine[1];
                n = calcLine[0];
            }
        } else {
            int x1 = p.xpoints[0];
            int x2 = p.xpoints[1];
            int y1 = -p.ypoints[0];
            int y2 = -p.ypoints[1];
            if (x1 == x2) {
                m = Double.POSITIVE_INFINITY;
            } else {
                m = (double) (y2 - y1) / (x2 - x1);
                n = y2 - m * x2;
            }
        }
        double angle = 0.0;
        if (Double.isInfinite(m)) {
            angle = Math.PI / 2.0;
        } else {
            angle = Math.atan(m);
        }

        int fP = 0;
        int lP = p.npoints - 1;

        if (angle > -Math.PI / 2.0 && angle <= -Math.PI / 4.0) {
            if (p.ypoints[fP] > p.ypoints[lP]) {
                angle += Math.PI;
            }
        }
        if (angle > -Math.PI / 4.0 && angle <= Math.PI / 4.0) {
            if (p.xpoints[fP] > p.xpoints[lP]) {
                angle += Math.PI;
            }
        }
        if (angle > Math.PI / 4.0 && angle <= Math.PI / 2.0) {
            if (p.ypoints[fP] < p.ypoints[lP]) {
                angle += Math.PI;
            }
        }

        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return new double[]{angle, n};
    }

    private double getOffDist(double[] aPt, double[] cPt, double orVecX, double orVecY) {
        double diffX = aPt[0] - cPt[0];
        double diffY = -aPt[1] + cPt[1];
        //Since orVec has length 1 calculate the cross product, which is 
        //the orthogonal distance from diff to orVec, take into account 
        //the z-Value to decide whether its a positive or negative distance!
        //double dotProdX = 0;
        //double dotProdY = 0;
        return diffX * orVecY - diffY * orVecX;
    }

    private double getInDist(double[] aPt, double[] cPt, double orVecX, double orVecY) {
        double diffX = aPt[0] - cPt[0];
        double diffY = -aPt[1] + cPt[1];
        //Parallel component of (diffX, diffY) is lambda * (orVecX, orVecY) with
        double lambda = diffX * orVecX + orVecY * diffY;

        return lambda;
    }

    private double getDistFast(double[] aPt, Rectangle bb) {
        double dist = 0.0;
        if (aPt[0] < bb.x) {
            dist += bb.x - aPt[0];
        }
        if (aPt[0] > bb.x + bb.width) {
            dist += aPt[0] - bb.x - bb.width;
        }
        if (aPt[1] < bb.y) {
            dist += bb.y - aPt[1];
        }
        if (aPt[1] > bb.y + bb.height) {
            dist += aPt[1] - bb.y - bb.height;
        }
        return dist;
    }

    @Override
    public int[][] getGTLists(Polygon[] baseLineGT, Polygon[] baseLineHyp, double thresh) {
        Polygon[] polysTruthNorm = normDesDist(baseLineGT);
        double[] tols = calcTols(polysTruthNorm);
        return getGtList(baseLineGT, baseLineHyp, tols, thresh);
    }

    private class LinRegression {

        private double[] calcLine(int[] xPoints, int[] yPoints) {
            int dimA = xPoints.length;
            double minX = 10000;
            double maxX = 0;
            double sumX = 0.0;
            double[][] A = new double[dimA][2];
            double[] Y = new double[dimA];
            for (int i = 0; i < dimA; i++) {
                double[] rowI = A[i];
                int actPx = xPoints[i];
                int actPy = yPoints[i];
                rowI[0] = 1.0;
                rowI[1] = actPx;
                minX = Math.min(minX, actPx);
                maxX = Math.max(maxX, actPx);
                sumX += actPx;
                Y[i] = actPy;
            }
            if (maxX - minX < 2) {
                return new double[]{sumX / dimA, Double.POSITIVE_INFINITY};
            }

            return solveLin(A, Y);
        }

        private double[] solveLin(double[][] mat1, double[] Y) {
            double[][] mat1T = transpose(mat1);
            double[][] multLS = multiply(mat1T, mat1);
            double[] multRS = multiply(mat1T, Y);
            double[][] inv = null;
            if (multLS.length != 2) {
                System.out.println("LinRegression Error: Matrix not 2x2");
            } else {
                inv = new double[2][2];
                double n = (multLS[0][0] * multLS[1][1] - multLS[0][1] * multLS[1][0]);
                if (n < 1E-9) {
                    System.out.println("LinRegression Error: Numerically unstable.");
                    return new double[]{mat1[0][1], Double.POSITIVE_INFINITY};
                }
                double fac = 1.0 / n;
                inv[0][0] = fac * multLS[1][1];
                inv[1][1] = fac * multLS[0][0];
                inv[1][0] = -fac * multLS[1][0];
                inv[0][1] = -fac * multLS[0][1];
            }
            double[] res = multiply(inv, multRS);
            return res;
        }

        private double[][] transpose(double[][] A) {
            double[][] res = new double[A[0].length][A.length];
            for (int i = 0; i < A.length; i++) {
                double[] aA = A[i];
                for (int j = 0; j < aA.length; j++) {
                    res[j][i] = aA[j];
                }
            }
            return res;
        }

        private double[] multiply(double[][] A, double[] x) {
            if (A[0].length != x.length) {
                System.out.println("LinRegression Error: Matrix dimension mismatch.");
            }
            double[] res = new double[A.length];
            for (int i = 0; i < res.length; i++) {
                double[] aA = A[i];
                double tmp = 0.0;
                for (int j = 0; j < aA.length; j++) {
                    tmp += x[j] * aA[j];
                }
                res[i] = tmp;
            }
            return res;
        }

        private double[][] multiply(double[][] A, double[][] B) {
            if (A[0].length != B.length) {
                System.out.println("LinRegression Error: Matrix dimension mismatch.");
            }
            double[][] res = new double[A.length][B[0].length];
            for (int i = 0; i < A.length; i++) {
                double[] aA = A[i];
                for (int j = 0; j < B[0].length; j++) {
                    double tmp = 0.0;
                    for (int k = 0; k < B.length; k++) {
                        tmp += B[k][j] * aA[k];
                    }
                    res[i][j] = tmp;
                }
            }
            return res;
        }

    }

}
