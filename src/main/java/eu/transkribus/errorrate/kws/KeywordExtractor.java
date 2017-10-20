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
    private final boolean part;
    private final boolean upper;
    private int maxSize = 1000;
    private final String prefix = "([^\\pL\\pN\\pM\\p{Cs}\\p{Co}])";
    private final String suffix = "([^\\pL\\pN\\pM\\p{Cs}\\p{Co}])";
    private final Pattern prefixPattern = Pattern.compile("^" + prefix);
    private final Pattern suffixPattern = Pattern.compile(suffix + "$");

    public KeywordExtractor() {
        this(false, false);
    }

    public KeywordExtractor(boolean part, boolean upper) {
        this.part = part;
        this.upper = upper;
    }

    private Pattern getPattern(String kw) {
        if (upper) {
            kw = kw.toUpperCase();
        }
        Pattern res = keywords.get(kw);
        if (res == null) {
            res = part ? Pattern.compile(kw) : Pattern.compile("((^)|" + prefix + ")" + kw + "(($)|" + suffix + ")");
            if (keywords.size() > maxSize) {
                keywords.clear();
            }
            keywords.put(kw, res);
        }
        return res;
    }

    public double[][] getKeywordPosition(String keyword, String line) {
        if (upper) {
            line = line.toUpperCase();
        }
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
            String textline = upper ? line.textEquiv.toUpperCase() : line.textEquiv;
            Set<String> tokenize = new HashSet<>(tokenizer.tokenize(textline));
            for (String keyword : tokenize) {
                double[][] keywordPosition = getKeywordPosition(keyword, textline);
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
                String textline = upper ? line.textEquiv.toUpperCase() : line.textEquiv;
                double[][] keywordPosition = getKeywordPosition(keyword, textline);
                for (double[] ds : keywordPosition) {
                    kwsLine.addKeyword(keyword, PolygonUtil.getPolygonPart(line.baseLine, ds[0], ds[1]));
                }
            }
        }
        return page;
    }

}
