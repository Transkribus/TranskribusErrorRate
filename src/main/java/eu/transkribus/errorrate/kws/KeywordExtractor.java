/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KwsGroundTruth;
import eu.transkribus.errorrate.types.KwsLine;
import eu.transkribus.errorrate.types.KwsPage;
import eu.transkribus.errorrate.util.PolygonUtil;
import eu.transkribus.languageresources.extractor.pagexml.PAGEXMLExtractor;
import eu.transkribus.languageresources.extractor.xml.XMLExtractor;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gundram
 */
public class KeywordExtractor {

    private HashMap<String, Pattern> keywords = new LinkedHashMap<>();
    private final boolean seperated;
    private int maxSize = 1000;
    private String prefix = "([ ])";
    private String suffix = "([ ])";
    private Pattern prefixPattern = Pattern.compile("^" + prefix);
    private Pattern suffixPattern = Pattern.compile(suffix + "$");

    public KeywordExtractor(boolean seperated) {
        this.seperated = seperated;
    }

    private Pattern getPattern(String kw) {
        Pattern res = keywords.get(kw);
        if (res == null) {
            //CAUTION: changing the pattern have to change the 
            res = seperated ? Pattern.compile("((^)|" + prefix + ")" + kw + "(($)|" + suffix + ")") : Pattern.compile(kw);
            if (keywords.size() > maxSize) {
                keywords.clear();
            }
            keywords.put(kw, res);
        }
        return res;
    }

    public double[][] getKeywordPosition(String keyword, String line) {
        Pattern p = getPattern(keyword);
        Matcher matcher = p.matcher(line);
        int idx = 0;
        List<double[]> startEnd = new LinkedList<>();
        while (matcher.find(idx)) {
            idx = matcher.start() + 1;
            String group = matcher.group();
            Matcher matcherPrefix = prefixPattern.matcher(group);
            Matcher matcherSuffix = suffixPattern.matcher(group);
            double[] match = new double[]{
                (matcherPrefix.find() ? matcher.start() + matcherPrefix.group().length() : matcher.start()) / ((double) line.length()),
                (matcherSuffix.find() ? matcher.end() - matcherSuffix.group().length() : matcher.end()) / ((double) line.length())
            };
            startEnd.add(match);
        }
        return startEnd.toArray(new double[0][]);
    }

    public KwsGroundTruth getKeywordGroundTruth(File[] filePaths, List<String> keywords) {
        String[] both = new String[filePaths.length];
        for (int i = 0; i < both.length; i++) {
            both[i] = filePaths[i].getAbsolutePath();
        }
        return getKeywordGroundTruth(both, both, keywords);
    }

    public KwsGroundTruth getKeywordGroundTruth(String[] filePaths, String[] fileIds, List<String> keywords) {
        List<KwsPage> pages = new LinkedList<>();
        for (int i = 0; i < fileIds.length; i++) {
            String fileId = fileIds[i];
            String filePath = filePaths[i];
            pages.add(getKeywordsFromFile(new File(filePath), keywords, fileId));
        }
        return new KwsGroundTruth(pages);
    }

    public KwsPage getKeywordsFromFile(File file, List<String> keywords, String pageID) {
        List<XMLExtractor.Line> lines = PAGEXMLExtractor.getLinesFromFile(file);
        KwsPage page = new KwsPage(pageID != null ? pageID : "");
        for (XMLExtractor.Line line : lines) {
            KwsLine kwsLine = new KwsLine(line.baseLine);
            page.addLine(kwsLine);
            for (String keyword : keywords) {
                double[][] keywordPosition = getKeywordPosition(keyword, line.textEquiv);
                for (double[] ds : keywordPosition) {
                    kwsLine.addKeyword(keyword, PolygonUtil.getPolygonPart(line.baseLine, ds[0], ds[1]));
                }
            }
        }
        return page;
    }

}
