/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.normalizer;

import eu.transkribus.errorrate.categorizer.CategorizerWordDft;
import eu.transkribus.errorrate.interfaces.ICategorizer;
import eu.transkribus.errorrate.interfaces.IStringNormalizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gundram
 */
public class StringNormalizerLetterNumber implements IStringNormalizer {

    private static Logger LOG = Logger.getLogger(StringNormalizerLetterNumber.class.getName());
    private final IStringNormalizer impl;
    private final ICategorizer cat = new CategorizerWordDft();

    public StringNormalizerLetterNumber(IStringNormalizer impl) {
        this.impl = impl;
    }

    @Override
    public String normalize(String string) {
        char[] toCharArray = string.toCharArray();
        StringBuilder sb = new StringBuilder(toCharArray.length);
        final String lz = "LNZ";
        for (char c : toCharArray) {
            if (lz.contains(cat.getCategory(c))) {
                sb.append(c);
            }
        }
        String res = impl != null ? impl.normalize(sb.toString()) : sb.toString();
        LOG.log(Level.FINER, "change '" + string + "' to '" + res + "'");
        return res;
    }
}
