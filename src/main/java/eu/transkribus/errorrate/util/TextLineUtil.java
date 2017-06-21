/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import eu.transkribus.languageresources.extractor.pagexml.PAGEXMLExtractor;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.math3.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gundram
 */
public class TextLineUtil {


//    public static List<Pair<String, String>> getTextFromPageDom(String path1, String path2) {
//        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
//        return extractor.extractTextFromDocument(path);
//    }
    
    public static List<Pair<String, String>> getTextFromPageDom(String path1, String path2) {
        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
        return extractor.extractTextFromFilePairwise(path1, path2);
    }
}