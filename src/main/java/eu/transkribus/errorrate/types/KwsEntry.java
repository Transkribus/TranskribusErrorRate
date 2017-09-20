/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import java.awt.Polygon;

/**
 *
 * @author gundram
 */
public class KwsEntry implements Comparable<KwsEntry> {

    @Expose
    private double conf;
    @Expose
    private String bl;
    @Expose
    private String line;
    @Expose
    private String image = null;
    private transient Polygon poly;
    private transient Polygon baseline;

    private transient KwsLine parentLine;

    public void setParentLine(KwsLine parentLine) {
        this.parentLine = parentLine;
    }

    public KwsLine getParentLine() {
        return parentLine;
    }
    

    public KwsEntry(double conf, String lineID, Polygon bl, String pageId) {
        this(conf, lineID, array2String(bl.xpoints, bl.ypoints, bl.npoints), pageId);
        this.poly = bl;
    }

    public KwsEntry(double conf, String lineID, Polygon bl, Polygon line, String pageId) {
        this(conf, lineID, bl, pageId);
        this.baseline = line;
    }

    public KwsEntry(double conf, String lineID, String bl, String pageId) {
        this.conf = conf;
        this.bl = bl;
//        this.id = id;
        this.image = pageId;
        this.line = lineID;
    }

    public Polygon getBaseLineKeyword() {
        if (poly == null) {
            poly = string2Polygon(bl);
        }
        return poly;
    }

    public Polygon getBaseLineLine() {
        return baseline;
    }

    public double getConf() {
        return conf;
    }

//    public String getId() {
//        return id;
//    }
    public String getImage() {
        return image;
    }

    public String getLineID() {
        return line;
    }

    public String getBl() {
        return bl;
    }

    @Override
    public String toString() {
        return "Entry{" + "conf=" + conf + ", bl=" + bl + ", lineID=" + line + ", image=" + image + '}';
    }

    @Override
    public int compareTo(KwsEntry o) {
        return Double.compare(o.conf, conf);
    }

    private static Polygon string2Polygon(String string) {
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

    private static String array2String(int[] x, int[] y, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(x[i]).append(',').append(y[i]).append(' ');
        }
        return sb.toString().trim();
    }

}
