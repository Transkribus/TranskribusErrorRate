/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.categorizer;

import eu.transkribus.tokenizer.interfaces.ICategorizer;


/**
 * categorizes characters by their general category specified by the unicode
 * properties. Therefore, the main category is taken ("L" for letters, "N" for
 * numbers, "S" for symbols,...). Delimiters are all codepoints with category
 * "Z". All codepoints are isolated tokens EXCEPT codepoints with general
 * categories "L" and "N".
 *
 * @author gundram
 */
public class CategorizerWordDft implements ICategorizer {

    @Override
    public String getCategory(char c) {
        if (c == '\n') {
            return "Z";
        }
        return CategoryUtils.getCategoryGeneral(c);
    }

    @Override
    public boolean isDelimiter(char c) {
        if (c == '\n') {
            return true;
        }
        return "Z".equals(CategoryUtils.getCategoryGeneral(c));
    }

    @Override
    public boolean isIsolated(char c) {
        switch (getCategory(c)) {
            case "L":
            case "N":
            case "Nl":
            case "Nd":
            case "LC":
            case "Ll":
            case "Lm":
            case "Lo":
            case "Lt":
            case "Lu":
                return false;
            default:
                return true;
        }
    }

}
