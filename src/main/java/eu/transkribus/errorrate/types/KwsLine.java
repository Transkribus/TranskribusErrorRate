/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import eu.transkribus.errorrate.util.PolygonUtil;
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
    private HashMap<String, List<String>> kws = new HashMap<>();
    private transient HashMap<String, List<Polygon>> kwsL = new HashMap<>();

//    @Expose
//    private KwsPage parent;
    @Expose
    private String bl;
    private transient Polygon blL;
    private transient double tolerance;

    public KwsLine(Polygon baseline) {
        this.blL = baseline;
        this.bl = PolygonUtil.polygon2String(blL);
    }

    public KwsLine(String baseline) {
        this.bl = baseline;
        this.blL = PolygonUtil.string2Polygon(baseline);
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

//    public KwsLine(KwsPage parent) {
//        this.parent = parent;
//    }
    public void addKeyword(String word, Polygon polygon) {
        List<Polygon> getL = kwsL.get(word);
        List<String> get = kws.get(word);
        if (getL == null) {
            getL = new LinkedList<>();
            kwsL.put(word, getL);
            get = new LinkedList<>();
            kws.put(word, get);
        }
        getL.add(polygon);
        get.add(PolygonUtil.polygon2String(polygon));
    }

    public HashMap<String, List<Polygon>> getKeyword2Baseline() {
        if (kwsL == null) {
            kwsL = new HashMap<>();
            for (String keyword : kws.keySet()) {
                LinkedList<Polygon> polyL = new LinkedList<>();
                kwsL.put(keyword, polyL);
                for (String poly : kws.get(keyword)) {
                    polyL.add(PolygonUtil.string2Polygon(poly));
                }
            }
        }
        return kwsL;
    }

//    public KwsPage getParent() {
//        return parent;
//    }
    public Polygon getBaseline() {
        return blL;
    }
}
