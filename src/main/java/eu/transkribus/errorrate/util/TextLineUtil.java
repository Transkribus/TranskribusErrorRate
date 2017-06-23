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

    public static String getTextFromPageDom(String path) {
        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
        return extractor.extractTextFromDocument(path);
    }

    public static List<Pair<String, String>> getTextFromPageDom(String path1, String path2) {
        PAGEXMLExtractor extractor = new PAGEXMLExtractor();
        return extractor.extractTextFromFilePairwise(path1, path2);
    }

//    private static Document loadDoc(File file) {
//        DocumentBuilder db = null;
//        try {
//            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        } catch (ParserConfigurationException ex) {
//            throw new RuntimeException("cannot initialize default parser for doml.", ex);
//        }
//        try {
//            return db.parse(file);
//        } catch (SAXException | IOException ex) {
//            throw new RuntimeException("cannot load file '" + file + "'.", ex);
//        }
//    }
//
//    public static String getTextFromPageDomOld(String path) {
//        StringBuilder sb = new StringBuilder();
//        Document doc = loadDoc(new File(path));
//        NodeList elementsByTagName = doc.getElementsByTagName("TextLine");
//        for (int i = 0; i < elementsByTagName.getLength(); i++) {
//            Node textLine = elementsByTagName.item(i);
//            Node textEquiv = getChild(textLine, "TextEquiv");
//            if (textEquiv == null) {
//                continue;
////                throw new RuntimeException("textline" + textLine.getAttributes().getNamedItem("id").getTextContent() + " does not have a TextEquiv child.");
//            }
//            Node unicode = getChild(textEquiv, "Unicode");
//            if (unicode == null) {
//                throw new RuntimeException("textline" + textLine.getAttributes().getNamedItem("id").getTextContent() + " does not have a TextEquiv child with Unicode child.");
//            }
//            if (unicode.hasChildNodes()) {
//                textLine = textLine.getFirstChild();
//                String textContent = textLine.getNodeValue();
//                sb.append(textContent);
//            }
//            if (i != elementsByTagName.getLength() - 1) {
//                sb.append('\n');
//            }
//        }
//        return sb.toString();
//    }
//
//    private static Node getChild(Node node, String nameChild) {
//        NodeList childNodes = node.getChildNodes();
//        for (int i = 0; i < childNodes.getLength(); i++) {
//            if (childNodes.item(i).getNodeName().equals(nameChild)) {
//                return childNodes.item(i);
//            }
//        }
//        return null;
//    }
//
//    private static boolean equals(Node node1, Node node2) {
//        if (node1 == node2) {
//            return true;
//        }
//        if (node1 == null) {
//            return false;
//        }
//        return node1.equals(node2);
//    }
//
//    private static boolean equalsBaseline(Node node1, Node node2) {
//        if (node1 == node2) {
//            return true;
//        }
//        String points1 = node1.getAttributes().getNamedItem("points").getTextContent();
//        String points2 = node2.getAttributes().getNamedItem("points").getTextContent();
//        return points1.equals(points2);
//    }
//
//    private static boolean equalsTextLine(Node node1, Node node2) {
////        if (!node1.getAttributes().getNamedItem("id").getTextContent().equals(node2.getAttributes().getNamedItem("id").getTextContent())) {
////            return false;
////        }
////        if (!equals(getChild(node1, "Coords"), getChild(node2, "Coords"))) {
////            return false;
////        }
//        return equalsBaseline(getChild(node1, "Baseline"), getChild(node2, "Baseline"));
//    }
//
//    public static List<Pair<String, String>> getTextFromPageDomOld(String path1, String path2) {
//        Document doc1 = loadDoc(new File(path1));
//        Document doc2 = loadDoc(new File(path2));
//        NodeList elementsByTagName1 = doc1.getElementsByTagName("TextLine");
//        NodeList elementsByTagName2 = doc2.getElementsByTagName("TextLine");
//        List<Pair<String, String>> res = new LinkedList<>();
//        boolean sameFile = true;
//        if (elementsByTagName1.getLength() != elementsByTagName2.getLength()) {
//            sameFile = false;
//        }
//        int len = elementsByTagName1.getLength();
//        for (int i = 0; i < len && sameFile; i++) {
//            Node textLine1 = elementsByTagName1.item(i);
//            Node textLine2 = elementsByTagName2.item(i);
//            if (!equalsTextLine(textLine1, textLine2)) {
//                sameFile = false;
//                break;
//            }
//            Node textEquiv1 = getChild(textLine1, "TextEquiv");
//            Node textEquiv2 = getChild(textLine2, "TextEquiv");
//            if ((textEquiv1 == null && textEquiv2 != null) || textEquiv1 != null && textEquiv2 == null) {
//                sameFile = false;
//                break;
//            }
//            if (textEquiv1 == null && textEquiv2 == null) {
//                continue;
//            }
//            String unicode1 = getChild(textEquiv1, "Unicode").getTextContent();
//            String unicode2 = getChild(textEquiv2, "Unicode").getTextContent();
//            res.add(new Pair<>(unicode1 == null ? "" : unicode1, unicode2 == null ? "" : unicode2));
//        }
//        //fallback if no exact mapping could be done
//        if (!sameFile) {
//            res.clear();
//            res.add(new Pair<>(getTextFromPageDomOld(path1), getTextFromPageDomOld(path2)));
//        }
//        return res;
//    }

}
