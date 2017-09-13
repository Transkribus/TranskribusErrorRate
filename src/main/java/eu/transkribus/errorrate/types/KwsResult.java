/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import com.google.gson.annotations.Expose;
import java.util.Set;

/**
 *
 * @author gundram
 */
public class KwsResult {

    @Expose
    private Set<KwsWord> keywords;

    public KwsResult(Set<KwsWord> keywords) {
        this.keywords = keywords;
    }

    public Set<KwsWord> getKeywords() {
        return keywords;
    }

}
