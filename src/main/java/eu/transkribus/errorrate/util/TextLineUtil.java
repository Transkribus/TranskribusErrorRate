/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.math3.util.Pair;
import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.io.UnsupportedFormatVersionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gundram
 */
public class TextLineUtil {

    public static List<TextLine> getTextLinesFromPage(String path) throws UnsupportedFormatVersionException {
        Page aPage;
        aPage = org.primaresearch.dla.page.io.xml.XmlInputOutput.readPage(path);
        if (aPage == null) {
            throw new RuntimeException("page from path '" + path + "' cannot be loaded.");
        }
        LinkedList<TextLine> textLines = new LinkedList<>();
        List<Region> regionsSorted = aPage.getLayout().getRegionsSorted();
        for (Region reg : regionsSorted) {
            if (reg instanceof TextRegion) {
                for (LowLevelTextObject tObj : ((TextRegion) reg).getTextObjectsSorted()) {
                    if (tObj instanceof TextLine) {
                        textLines.add((TextLine) tObj);
                    }
                }
            }
        }
        return textLines;
    }

    public static String getTextFromPageDom(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(path));
            NodeList elementsByTagName = doc.getElementsByTagName("Unicode");
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                Node item = elementsByTagName.item(i);
                if (item.hasChildNodes()) {
                    item = item.getFirstChild();
                    String textContent = item.getNodeValue();
                    sb.append(textContent);
                }
                if (i != elementsByTagName.getLength() - 1) {
                    sb.append('\n');
                }
            }
            return sb.toString();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Node getChild(Node node, String nameChild) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals(nameChild)) {
                return childNodes.item(i);
            }
        }
        return null;
    }

    private static boolean equals(Node node1, Node node2) {
        if (node1 == node2) {
            return true;
        }
        if (node1 == null) {
            return false;
        }
        return node1.equals(node2);
    }

    private static boolean equalsBaseline(Node node1, Node node2) {
        if (node1 == node2) {
            return true;
        }
        String points1 = node1.getAttributes().getNamedItem("points").getTextContent();
        String points2 = node2.getAttributes().getNamedItem("points").getTextContent();
        return points1.equals(points2);
    }

    private static boolean equalsTextLine(Node node1, Node node2) {
//        if (!node1.getAttributes().getNamedItem("id").getTextContent().equals(node2.getAttributes().getNamedItem("id").getTextContent())) {
//            return false;
//        }
//        if (!equals(getChild(node1, "Coords"), getChild(node2, "Coords"))) {
//            return false;
//        }
        return equalsBaseline(getChild(node1, "Baseline"), getChild(node2, "Baseline"));
    }

    public static List<Pair<String, String>> getTextFromPageDom(String path1, String path2) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc1 = db.parse(new File(path1));
            Document doc2 = db.parse(new File(path2));
            NodeList elementsByTagName1 = doc1.getElementsByTagName("TextLine");
            NodeList elementsByTagName2 = doc2.getElementsByTagName("TextLine");
            List<Pair<String, String>> res = new LinkedList<>();
            boolean sameFile = true;
            if (elementsByTagName1.getLength() != elementsByTagName2.getLength()) {
                sameFile = false;
            }
            int len = elementsByTagName1.getLength();
            for (int i = 0; i < len && sameFile; i++) {
                Node textLine1 = elementsByTagName1.item(i);
                Node textLine2 = elementsByTagName2.item(i);
                if (!equalsTextLine(textLine1, textLine2)) {
                    sameFile = false;
                    break;
                }
                Node textEquiv1 = getChild(textLine1, "TextEquiv");
                Node textEquiv2 = getChild(textLine2, "TextEquiv");
                if ((textEquiv1 == null && textEquiv2 != null) || textEquiv1 != null && textEquiv2 == null) {
                    sameFile = false;
                    break;
                }
                if (textEquiv1 == null && textEquiv2 == null) {
                    continue;
                }
                String unicode1 = getChild(textEquiv1, "Unicode").getTextContent();
                String unicode2 = getChild(textEquiv2, "Unicode").getTextContent();
                res.add(new Pair<>(unicode1 == null ? "" : unicode1, unicode2 == null ? "" : unicode2));
            }
            //fallback if no exact mapping could be done
            if (!sameFile) {
                res.clear();
                res.add(new Pair<>(getTextFromPageDom(path1), getTextFromPageDom(path2)));
            }
            return res;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> getTextFromPage(String path) throws UnsupportedFormatVersionException {
        List<TextLine> textLinesFromPage = getTextLinesFromPage(path);
        ArrayList<String> res = new ArrayList<>(textLinesFromPage.size());
        for (TextLine textLine : textLinesFromPage) {
            res.add(textLine.getText());
        }
        return res;
    }
}
