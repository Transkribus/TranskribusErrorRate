/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

/**
 *
 * @author gundram
 */
import com.google.gson.annotations.Expose;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class KwsWord {

    @Expose
    private String kw;
    @Expose
    LinkedList<KWS.Entry> pos = new LinkedList<>();
    private int maxSize = -1;

    private double minConf = Double.MAX_VALUE;
    private boolean isSorted = false;

    public KwsWord(String kw, int maxSize, double minConf) {
        this(kw, maxSize);
        this.minConf = minConf;
    }

    public KwsWord(String kw, int maxSize) {
        this.kw = kw;
        this.maxSize = maxSize;
    }

    public double getMinConf() {
        return minConf;
    }

    public boolean addAll(Collection<? extends KWS.Entry> c) {
        return pos.addAll(c);
    }

    public KwsWord(String kw) {
        this.kw = kw;
    }

    public String getKeyWord() {
        return kw;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public synchronized void add(KWS.Entry entry) {
        if (maxSize <= 0 || pos.size() < maxSize) {
            pos.add(entry);
            isSorted = false;
//            Collections.sort(pos);
            return;
        }
        if (!isSorted) {
            Collections.sort(pos);
            isSorted = true;
        }
        if (pos.getLast().getConf() < entry.getConf()) {
            pos.removeLast();
            pos.add(entry);
            Collections.sort(pos);
            isSorted = true;
//            System.out.println(minConf + " -> " + entry.getConf());
            minConf = entry.getConf();
        }
    }

    public int size() {
        return pos.size();
    }

    public List<KWS.Entry> getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "KeyWord{" + "kw=" + kw + ", entries=" + pos + '}';
    }

}
