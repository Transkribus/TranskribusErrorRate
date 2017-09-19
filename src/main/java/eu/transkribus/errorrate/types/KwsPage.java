/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class KwsPage {

    @Expose
    private String pageID;
    @Expose
    private List<KwsLine> lines = new LinkedList<>();
    private String pageId;

    public KwsPage(String pageId) {
        this.pageId = pageId;
    }

    /**
     *
     * @param pageId
     * @param lines
     */
    public KwsPage(String pageId, List<KwsLine> lines) {
        this.pageId = pageId;
        this.lines = lines;
    }

    public List<KwsLine> getLines() {
        return lines;
    }

    public String getPageID() {
        return pageID;
    }

    public void addLine(KwsLine line) {
        lines.add(line);
    }

}
