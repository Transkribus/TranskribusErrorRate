/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.CustomTerminal;
import com.panayotis.gnuplot.terminal.DefaultTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;
import com.panayotis.gnuplot.terminal.SVGTerminal;
import com.panayotis.gnuplot.terminal.TextFileTerminal;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describtion of Gnuplot:
 *
 *
 * @author Tobias Strauß <tobias.strauss@uni-rostock.de>
 * since 05.01.2014
 */
public class PlotUtil {

    private static Logger LOG = LoggerFactory.getLogger(PlotUtil.class);

    public static Consumer<JavaPlot> getDefaultTerminal() {
        return new Consumer<JavaPlot>() {
            @Override
            public void accept(JavaPlot t) {
                GNUPlotTerminal term = new DefaultTerminal();
                t.setTerminal(term);
                t.plot();
            }

        };
    }

    public static Consumer<JavaPlot> getPostScriptTerminal(final File outFile) {
        return new Consumer<JavaPlot>() {
            @Override
            public void accept(JavaPlot t) {
                GNUPlotTerminal term = new PostscriptTerminal(outFile.getPath());
                t.setTerminal(term);
                t.plot();
            }

        };
    }

    public static Consumer<JavaPlot> getSVGTerminal(final File outFile) {
        return new Consumer<JavaPlot>() {
            @Override
            public void accept(JavaPlot t) {
                GNUPlotTerminal term = new SVGTerminal(outFile.getPath());
                t.setTerminal(term);
                t.plot();
            }

        };
    }

    public static Consumer<JavaPlot> getLaTexTikZTerminal(File file) {
        return new Consumer<JavaPlot>() {
            @Override
            public void accept(JavaPlot t) {
                try {
                    file.createNewFile();
                    TextFileTerminal tex = new TextFileTerminal("tikz color standalone", file.getPath());
                    tex.set("output", file.getPath());
                    tex.processOutput(new FileInputStream(file));
                    t.setTerminal(tex);
                    t.plot();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public static Consumer<JavaPlot> getImageFileTerminal(final File file) {
        return getImageFileTerminal(file, 1024, 768);
    }

    public static Consumer<JavaPlot> getImageFileTerminal(final File file, final int width, final int height) {
        return new Consumer<JavaPlot>() {

            @Override
            public void accept(JavaPlot t) {
                try {
                    ImageTerminal ter = new ImageTerminal();
//                    file.createNewFile();
                    ter.set("size", width + "," + height);
                    t.setTerminal(ter);
                    t.plot();
//                    ter.processOutput(new FileInputStream(file));
                    BufferedImage image = ter.getImage();
                    ImageIO.write(image, file.getName().toLowerCase().endsWith("jpg") ? "jpg" : "png", file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

    }

    public static JavaPlot plotCurve(List<double[]> val, String[] result_names, String title, double ymin, double ymax, JavaPlot.Key setKey) {
        JavaPlot p = new JavaPlot();

        //p.set("xrange", "[-1.1:1.1]");
        //p.getAxis("x").setBoundaries(-1.1, 1.1);
        if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
            p.set("yrange", "[" + ymin + ":" + ymax + "]");
            //p.getAxis("y").setBoundaries(-1.1, 1.1);
        }

        p.setTitle(title);
        p.setKey(setKey);

        for (int h = 1; h < val.get(0).length; h++) {
            double[][] points = new double[val.size()][2];
            for (int i = 0; i < val.size(); i++) {
                points[i][0] = val.get(i)[0];
                points[i][1] = val.get(i)[h];
            }
            DataSetPlot dsp = new DataSetPlot(points);
            if (result_names != null && result_names.length > h) {
                dsp.setTitle(result_names[h]);
            } else {
                dsp.setTitle("Result " + h);
            }
            p.addPlot(dsp);
            PlotStyle stl = ((AbstractPlot) p.getPlots().get(h - 1)).getPlotStyle();
            stl.setStyle(Style.HISTEPS);
            //stl.setLineType(NamedPlotColor.BLUE);
//        stl.setStyle(Style.POINTS);
//        stl.setPointType(3); // nice stars
//        stl.setPointSize(2);
        }
        return p;
    }

    public static JavaPlot plot(double[] yAxis) {
        return plot(Arrays.asList(yAxis));
    }

    public static JavaPlot plot(List<double[]> yAxis) {

        String title = null;
        double[] xAxis = null;
        double minValue = Double.MAX_VALUE;
        double maxValue = -Double.MAX_VALUE;
        for (double[] yAxi : yAxis) {
            if (xAxis == null) {
                xAxis = new double[yAxi.length];
                for (int i = 0; i < xAxis.length; i++) {
                    xAxis[i] = i;
                }
            }
            double[] minMax = minmax(yAxi);
            minValue = Math.min(minValue, minMax[0]);
            maxValue = Math.max(maxValue, minMax[1]);
        }

        return plot(xAxis, yAxis, title, null, minValue, maxValue, JavaPlot.Key.TOP_RIGHT);
    }

    private static double[] minmax(double[] d) {
        if (d == null || d.length == 0) {
            return null;
        }
        double min = d[0];
        double max = d[0];
        for (int i = 1; i < d.length; i++) {
            min = Math.min(d[i], min);
            max = Math.max(d[i], max);
        }
        return new double[]{min, max};
    }

    public static JavaPlot plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax) {
        return plot(xAxis, yAxis, title, result_names, ymin, ymax, JavaPlot.Key.TOP_RIGHT);
    }

    public static JavaPlot plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, double ymin, double ymax, JavaPlot.Key key) {
        Pair<String, String> p = null;
        if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
            p = new Pair<>("yrange", "[" + ymin + ":" + ymax + "]");
        }
        if (p != null) {
            return plot(xAxis, yAxis, title, result_names, key, p);
        } else {
            return plot(xAxis, yAxis, title, result_names, key);
        }
    }

    public static JavaPlot genAndPlotHists(List<List<Double>> lists) {
        double xmax = -Double.MAX_VALUE, xmin = Double.MAX_VALUE;

        for (List<Double> arrayList : lists) {
            Collections.sort(arrayList);
            xmax = Math.max(xmax, arrayList.get(arrayList.size() - 1));
            xmin = Math.min(xmin, arrayList.get(0));
        }
        int num = 100;
        double ymax = 0;
        double h = (xmax - xmin) / (num - 1);
        List<double[]> yVals = new ArrayList<double[]>();
        String[] names = new String[lists.size()];
        int size = 0;
        for (List<Double> arrayList : lists) {
            size += arrayList.size();
        }
        int nameIdx = 0;

        for (List<Double> arrayList : lists) {
            double[] vals = new double[num];
            int idx = 0;
            for (Double val : arrayList) {
                while (val > xmin + (idx + 0.5) * h) {
                    idx++;
                }
                vals[idx]++;
            }
            for (int i = 0; i < vals.length; i++) {
                vals[i] /= size;
                if (vals[i] > ymax) {
                    ymax = vals[i];
                }
            }
            yVals.add(vals);
            names[nameIdx] = "" + nameIdx + " (" + arrayList.size() + " values)";
            nameIdx++;
        }
        double[] xVals = new double[num];
        for (int i = 0; i < xVals.length; i++) {
            xVals[i] = xmin + i * h;

        }
        return plot(xVals, yVals, "Histogramm", names, 0, ymax);
    }

    public static String KEY_XLABEL = "xlabel";
    public static String KEY_YLABEL = "ylabel";

    public static JavaPlot plot(double[] xAxis, List<double[]> yAxis, String title, String[] result_names, JavaPlot.Key key, Pair<String, String>... options) {
        int plotcnt = -1;
        JavaPlot p = new JavaPlot();
        for (Pair<String, String> option : options) {
            if (option.getFirst().equals(KEY_XLABEL)) {
                p.getAxis("x").setLabel(option.getSecond());
            } else if (option.getFirst().equals(KEY_YLABEL)) {
                p.getAxis("y").setLabel(option.getSecond());
            } else {
                p.set(option.getFirst(), option.getSecond());
            }
        }

        if (xAxis == null) {
            int size = 0;
            for (int i = 0; i < yAxis.size(); i++) {
                size = Math.max(size, yAxis.get(i).length);
            }
            xAxis = new double[size];
            for (int i = 0; i < xAxis.length; i++) {
                xAxis[i] = i;
            }
        }
        if (title != null) {
            p.setTitle(title);
        }
        p.setKey(key);
        for (int h = 0; h < yAxis.size(); h++) {
            double[] yY = yAxis.get(h);
            double[][] points = new double[yY.length][];
            for (int i = 0; i < yY.length; i++) {
                points[i] = new double[]{xAxis[i], yY[i]};
            }
            DataSetPlot dsp = new DataSetPlot(points);
            if (result_names != null && result_names.length > h) {
                dsp.setTitle(result_names[h]);
            } else {
                dsp.setTitle("Result " + h);
            }

            p.addPlot(dsp);
            plotcnt++;
            PlotStyle stl = ((AbstractPlot) p.getPlots().get(plotcnt)).getPlotStyle();
            stl.setStyle(Style.LINES);
        }

        return p;
    }

    public static JavaPlot getPRCurve(double[] stat) {
        return getPRCurves(Arrays.asList(stat), new LinkedList<>());
    }

    public static JavaPlot getPRCurves(List<double[]> stats, List<String> names) {
        double[] xAxis = new double[stats.get(0).length];
        for (int i = 0; i < xAxis.length; i++) {
            xAxis[i] = ((double) i) / (xAxis.length - 1);
        }
        return PlotUtil.plot(xAxis, stats, "Precision-Recall-Curve", names.toArray(new String[0]), JavaPlot.Key.BOTTOM_LEFT,
                new Pair<>("grid", "back"),
                new Pair<>("xtics", "0.0,0.05"),
                new Pair<>("ytics", "0.0,0.05"),
                new Pair<>(PlotUtil.KEY_XLABEL, "Recall"),
                new Pair<>(PlotUtil.KEY_YLABEL, "Precision"));
    }

}
