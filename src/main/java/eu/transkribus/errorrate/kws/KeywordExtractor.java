/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.kws;

import eu.transkribus.errorrate.types.KWS;
import eu.transkribus.errorrate.util.PolygonUtil;
import eu.transkribus.interfaces.ITokenizer;
import eu.transkribus.languageresources.extractor.pagexml.PAGEXMLExtractor;
import eu.transkribus.languageresources.extractor.xml.XMLExtractor;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    private final String prefix = "([ ])";
    private final String suffix = "([ ])";
    private final Pattern prefixPattern = Pattern.compile("^" + prefix);
    private final Pattern suffixPattern = Pattern.compile(suffix + "$");

    public KeywordExtractor(boolean seperated) {
        this.seperated = seperated;
    }

    private Pattern getPattern(String kw) {
        Pattern res = keywords.get(kw);
        if (res == null) {
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

    public KWS.GroundTruth getKeywordGroundTruth(File[] filePaths, List<String> keywords) {
        String[] both = new String[filePaths.length];
        for (int i = 0; i < both.length; i++) {
            both[i] = filePaths[i].getAbsolutePath();
        }
        return getKeywordGroundTruth(both, both, keywords);
    }

    public KWS.GroundTruth getKeywordGroundTruth(String[] filePaths, String[] fileIds, List<String> keywords) {
        List<KWS.Page> pages = new LinkedList<>();
        for (int i = 0; i < filePaths.length; i++) {
            String fileId = fileIds == null ? String.valueOf(i) : fileIds[i];
            String filePath = filePaths[i];
            pages.add(getKeywordsFromFile(new File(filePath), fileId, keywords));
        }
        return new KWS.GroundTruth(pages);
    }

    public KWS.GroundTruth getKeywordGroundTruth(String[] filePaths, String[] fileIds, ITokenizer tokenizer) {
        List<KWS.Page> pages = new LinkedList<>();
        for (int i = 0; i < filePaths.length; i++) {
            String fileId = fileIds == null ? String.valueOf(i) : fileIds[i];
            String filePath = filePaths[i];
            pages.add(getKeywordsFromFile(new File(filePath), fileId, tokenizer));
        }
        return new KWS.GroundTruth(pages);
    }

    public KWS.Page getKeywordsFromFile(File file, String pageID, ITokenizer tokenizer) {
        List<XMLExtractor.Line> lines = PAGEXMLExtractor.getLinesFromFile(file);
        KWS.Page page = new KWS.Page(pageID != null ? pageID : "");
        for (XMLExtractor.Line line : lines) {
            KWS.Line kwsLine = new KWS.Line(line.baseLine);
            page.addLine(kwsLine);
            Set<String> tokenize = new HashSet<>(tokenizer.tokenize(line.textEquiv));
            for (String keyword : tokenize) {
                double[][] keywordPosition = getKeywordPosition(keyword, line.textEquiv);
                for (double[] ds : keywordPosition) {
                    kwsLine.addKeyword(keyword, PolygonUtil.getPolygonPart(line.baseLine, ds[0], ds[1]));
                }
            }
        }
        return page;
    }

    public KWS.Page getKeywordsFromFile(File file, String pageID, List<String> keywords) {
        List<XMLExtractor.Line> lines = PAGEXMLExtractor.getLinesFromFile(file);
        KWS.Page page = new KWS.Page(pageID != null ? pageID : "");
        for (XMLExtractor.Line line : lines) {
            KWS.Line kwsLine = new KWS.Line(line.baseLine);
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
