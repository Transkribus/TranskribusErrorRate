/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsEntry;
import java.awt.Polygon;

/**
 *
 * @author tobias
 */
public class KwsMatch implements Comparable<KwsMatch> {

    private final Polygon poly;

    private final String pageId;

    private final String word;

    public enum Type {
        match, falseNegative, falsePositve
    }
    public Type type;
    public double conf;

    public KwsMatch(Type type, KwsEntry entry, String word) {
        this(type, entry.getConf(), entry.getBaseLineKeyword(), entry.getImage(), word);
    }

    public KwsMatch(Type type, double conf, Polygon poly, String pageId, String word) {
        this.type = type;
        this.conf = conf;
        this.poly = poly;
        this.pageId = pageId;
        this.word = word;

    }

    @Override
    public int compareTo(KwsMatch o) {
        return -Double.compare(conf, o.conf);
    }

    @Override
    public String toString() {
        return "KwsMatch{" + "pageId=" + pageId + ", word=" + word + ", type=" + type + ", conf=" + conf + '}';
    }
    
    
}
