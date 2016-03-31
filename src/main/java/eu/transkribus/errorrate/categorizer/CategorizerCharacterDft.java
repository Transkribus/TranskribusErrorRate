/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.categorizer;

import eu.transkribus.errorrate.interfaces.ICategorizer;

/**
 * each codepoint has his own category. There are no delimiter/separators and
 * each codepoint is isolated
 *
 * @author gundram
 */
public class CategorizerCharacterDft implements ICategorizer {

    @Override
    public String getCategory(char c) {
        return String.valueOf(c);
    }

    @Override
    public boolean isDelimiter(char c) {
        return false;
    }

    @Override
    public boolean isIsolated(char c) {
        return true;
    }

}
