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
    private HashMap<String, List<Polygon>> keyword2Baseline;

    @Expose
    private KwsPage parent;
    
    @Expose
    private Polygon baseline;

    public KwsLine(KwsPage parent) {
        this.parent = parent;
    }

    public void addKeyword(String word, Polygon polygon) {
        List<Polygon> get = keyword2Baseline.get(word);
        if (get == null) {
            get = new LinkedList<>();
            keyword2Baseline.put(word, get);
        }
        get.add(polygon);
    }

    public HashMap<String, List<Polygon>> getKeyword2Baseline() {
        return keyword2Baseline;
    }

    public KwsPage getParent() {
        return parent;
    }

    public Polygon getBaseline() {
        return baseline;
    }
    
    

}
