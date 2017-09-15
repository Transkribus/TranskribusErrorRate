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
public class KwsGroundTruth {

    @Expose
    private List<KwsPage> pages;

    public KwsGroundTruth() {
        pages = new LinkedList<>();
    }

    public KwsGroundTruth(List<KwsPage> pages) {
        this.pages = pages;
    }

    public void addPages(KwsPage page) {
        pages.add(page);
    }

    public List<KwsPage> getPages() {
        return pages;
    }

}
