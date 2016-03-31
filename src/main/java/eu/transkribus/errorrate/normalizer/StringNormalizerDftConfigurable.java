/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.normalizer;

import eu.transkribus.errorrate.interfaces.IStringNormalizer;
import eu.transkribus.errorrate.types.Properties;
import java.text.Normalizer.Form;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Normalize the String using some unicode methods. The
 *
 * @author gundram
 */
public class StringNormalizerDftConfigurable extends StringNormalizerDft implements IStringNormalizer.IPropertyConfigurable {

    public static Logger LOG = Logger.getLogger(StringNormalizerDftConfigurable.class.getName());
    Properties probs = new Properties();

    public StringNormalizerDftConfigurable(Form form, boolean toUpper) {
        super(form, toUpper);
    }

    public StringNormalizerDftConfigurable() {
        super();
    }

    @Override
    public void putSubstitutionProperties(String filename) {
        probs.load(filename);
    }

    @Override
    public String normalize(String string) {
        for (Iterator<String> iterator = probs.propertyNames(); iterator.hasNext();) {
            String next = iterator.next();
            string = string.replace(next, probs.getProperty(next));
        }
        return super.normalize(string);
    }

}
