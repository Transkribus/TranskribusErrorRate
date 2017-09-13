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
import java.util.Collections;
import java.util.LinkedList;

public class KwsWord {

    @Expose
    private String kw;
    @Expose
    LinkedList<KwsEntry> pos = new LinkedList<>();
    private int maxSize = -1;

    public KwsWord(String kw, int maxSize) {
        this.kw = kw;
        this.maxSize = maxSize;
    }

    public KwsWord(String kw) {
        this.kw = kw;
    }

    public String getKeyWord() {
        return kw;
    }

    public synchronized void add(KwsEntry entry) {
        if (maxSize <= 0 || pos.size() < maxSize) {
            pos.add(entry);
            Collections.sort(pos);
            return;
        }
        if (pos.getLast().getConf() < entry.getConf()) {
            pos.removeLast();
            pos.add(entry);
            Collections.sort(pos);
        }
    }

    public int size() {
        return pos.size();
    }

    public LinkedList<KwsEntry> getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return "KeyWord{" + "kw=" + kw + ", entries=" + pos + '}';
    }

}
