/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.io.UnsupportedFormatVersionException;

/**
 *
 * @author gundram
 */
public class TextLineUtil {

    public static List<TextLine> getTextLinesFromPage(String path) throws UnsupportedFormatVersionException {
        Page aPage;
        aPage = org.primaresearch.dla.page.io.xml.XmlInputOutput.readPage(path);
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

    public static List<String> getTextFromPage(String path) throws UnsupportedFormatVersionException {
        List<TextLine> textLinesFromPage = getTextLinesFromPage(path);
        ArrayList<String> res = new ArrayList<>(textLinesFromPage.size());
        for (TextLine textLine : textLinesFromPage) {
            res.add(textLine.getText());
        }
        return res;
    }
}
