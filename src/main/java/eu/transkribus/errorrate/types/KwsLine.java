/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class KwsLine {

    @Expose
    private HashMap<String, List<String>> keyword2Baseline = new HashMap<>();

//    @Expose
//    private KwsPage parent;
    @Expose
    private String baseline;

//    public KwsLine(KwsPage parent) {
//        this.parent = parent;
//    }
    public void addKeyword(String word, String polygon) {
        List<String> get = keyword2Baseline.get(word);
        if (get == null) {
            get = new LinkedList<>();
            keyword2Baseline.put(word, get);
        }
        get.add(polygon);
//        get.add(array2String(polygon.xpoints, polygon.ypoints, polygon.npoints));
    }

    public HashMap<String, List<String>> getKeyword2Baseline() {
        return keyword2Baseline;
    }

//    public KwsPage getParent() {
//        return parent;
//    }
    public String getBaseline() {
        return baseline;
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
