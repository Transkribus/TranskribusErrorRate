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

    public static List<String> getTextFromPageDom(String path) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(path));
            NodeList elementsByTagName = doc.getElementsByTagName("Unicode");
            ArrayList<String> res = new ArrayList<>(elementsByTagName.getLength());
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                Node item = elementsByTagName.item(i);
                if (item.hasChildNodes()) {
                    item = item.getFirstChild();
                    String textContent = item.getNodeValue();
                    res.add(i, textContent);
                } else {
                    res.add(i, "");
                }
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
