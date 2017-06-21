/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import eu.transkribus.languageresources.extractor.pagexml.PAGEXMLExtractor;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/**
 *
 * @author gundram
 */
public class TextLineUtil {


    public static String getTextFromPageDom(String path) {
        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
        return extractor.extractTextFromDocument(path);
    }
    
    public static List<Pair<String, String>> getTextFromPageDom(String path1, String path2) {
        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
        return extractor.extractTextFromDocumentPairwise(path1, path2);
    }

}
