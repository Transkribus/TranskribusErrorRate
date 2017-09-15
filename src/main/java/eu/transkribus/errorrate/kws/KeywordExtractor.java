/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gundram
 */
public class KeywordExtractor {

    private String[] keywords;
    private boolean seperated;
    Pattern[] patterns;

    public KeywordExtractor(boolean seperated, String... keyword) {
        this.keywords = keyword;
        this.seperated = seperated;
        for (int i = 0; i < keyword.length; i++) {
            String string = keyword[i].trim();
            patterns[i] = getPattern(string);
        }
    }

    private Pattern getPattern(String kw) {
        return seperated ? Pattern.compile("((^)|( ))" + kw + "(($)|( ))") : Pattern.compile(kw);

    }

    public int countKeyword(String keyword, String line) {
        Pattern p = getPattern(keyword);
        Matcher matcher = p.matcher(line);
        int res = 0;
        int idx = 0;
        while (matcher.find(idx)) {
            idx = matcher.start() + 1;
            res++;
        }
        return res;
    }

}
