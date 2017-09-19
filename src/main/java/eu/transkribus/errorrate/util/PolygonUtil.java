/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import java.awt.Point;
import java.awt.Polygon;

/**
 *
 * @author gundram
 */
public class PolygonUtil {

    public static Polygon string2Polygon(String string) {
        String[] split = string.split(" ");
        int size = split.length;
        int[] x = new int[size];
        int[] y = new int[size];
        for (int i = 0; i < size; i++) {
            String[] point = split[i].split(",");
            x[i] = Integer.parseInt(point[0]);
            y[i] = Integer.parseInt(point[1]);
        }
        return new Polygon(x, y, size);
    }

    public static String polygon2String(Polygon polygon) {
        return array2String(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    private static String array2String(int[] x, int[] y, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(x[i]).append(',').append(y[i]).append(' ');
        }
        return sb.toString().trim();
    }

    public static Polygon reducePoints(Polygon polygon) {
        if (polygon.npoints < 3) {
            return polygon;
        }
        Polygon res = new Polygon();
        Point startPoint = new Point(polygon.xpoints[0], polygon.ypoints[0]);
        Point lastPoint = new Point(polygon.xpoints[1], polygon.ypoints[1]);
        res.addPoint(startPoint.x, startPoint.y);
        Point direction = substract(startPoint, lastPoint);
        for (int i = 2; i < polygon.npoints; i++) {
            Point currentPoint = new Point(polygon.xpoints[i], polygon.ypoints[i]);
            Point directionNew = substract(startPoint, currentPoint);
            if (directionNew.x * direction.y == direction.x * directionNew.y) {//same direction
                lastPoint = currentPoint;
            } else {
                res.addPoint(lastPoint.x, lastPoint.y);
                startPoint = lastPoint;
                lastPoint = currentPoint;
                direction = substract(startPoint, lastPoint);
            }
        }
        res.addPoint(lastPoint.x, lastPoint.y);
        return res;
    }

    private static Point substract(Point first, Point second) {
        return new Point(first.x - second.x, first.y - second.y);
    }

    public static Polygon copy(Polygon p) {
        final int n = p.npoints;
        final int[] x = new int[n];
        final int[] y = new int[n];
        System.arraycopy(p.xpoints, 0, x, 0, n);
        System.arraycopy(p.ypoints, 0, y, 0, n);
        return new Polygon(x, y, n);
    }

    public static Polygon blowUp(Polygon inPoly) {
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

    public static Polygon thinOut(Polygon polyBlown, int desDist) {
        Polygon res = new Polygon();
        if (polyBlown.npoints <= 20) {
            return polyBlown;
        }
        int dist = polyBlown.npoints - 1;
        int minPts = 20;
        int desPts = Math.max(minPts, dist / desDist + 1);
        double step = (double) dist / (desPts - 1);
        for (int i = 0; i < desPts - 1; i++) {
            int aIdx = (int) (i * step);
            res.addPoint(polyBlown.xpoints[aIdx], polyBlown.ypoints[aIdx]);
        }
        res.addPoint(polyBlown.xpoints[polyBlown.npoints - 1], polyBlown.ypoints[polyBlown.npoints - 1]);
        return res;
    }

    public static Polygon getPolygonPart(Polygon baseline, double beginRel, double endRel) {
        Polygon blowUp = PolygonUtil.blowUp(baseline);
        int begin = (int) Math.floor(beginRel * blowUp.npoints);
        int end = Math.min(blowUp.npoints, (int) Math.ceil(endRel * blowUp.npoints)) - 1;
        return new Polygon(new int[]{blowUp.xpoints[begin], blowUp.xpoints[end]}, new int[]{blowUp.ypoints[begin], blowUp.ypoints[end]}, 2);
    }

}
