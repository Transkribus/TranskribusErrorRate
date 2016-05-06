/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.categorizer;

import eu.transkribus.errorrate.interfaces.ICategorizer;
import eu.transkribus.errorrate.types.Properties;

/**
 * like {@link CategorizerWordDft} but it is possible to add additional property
 * files. If the codepoint is a key in these property files, the resulting value
 * will be returned by the methods. So these property files overwrite the
 * default methods for the specific codepoint. Note that the length of the keys
 * have to be 1 (but things like "\u1234" are allowed). The value for separator
 * and isolated property file have to be true or false.
 *
 * @author gundram
 */
public class CategorizerWordDftConfigurable extends CategorizerWordDft implements ICategorizer.IPropertyConfigurable {

    private Properties propsCategory = new Properties();
    private Properties propsSeparator = new Properties();
    private Properties propsIsolated = new Properties();

//<editor-fold defaultstate="collapsed" desc="setter for property files">
    @Override
    public void putCategoryProperties(String fileName) {
        propsCategory.load(fileName);
    }

    @Override
    public void putSeparatorProperties(String fileName) {
        propsSeparator.load(fileName);
        for (String key : propsSeparator.keySet()) {
            if (key.length() != 1) {
                throw new RuntimeException("wrong formatted property-file '" + fileName + "': key has to have length=1.");
            }
            String property = propsSeparator.getProperty(key);
            if (key.length() != 1) {
                try {
                    Boolean.parseBoolean(property);
                } catch (RuntimeException ex) {
                    throw new RuntimeException("wrong formatted property-file '" + fileName + "': value has to be 'true' or 'false'.");
                }
            }

        }
    }

    @Override
    public void putIsolatedProperties(String fileName) {
        propsIsolated.load(fileName);
        for (String key : propsIsolated.keySet()) {
            if (key.length() != 1) {
                throw new RuntimeException("wrong formatted property-file '" + fileName + "': key has to have length=1.");
            }
            String property = propsIsolated.getProperty(key);
            if (key.length() != 1) {
                try {
                    Boolean.parseBoolean(property);
                } catch (RuntimeException ex) {
                    throw new RuntimeException("wrong formatted property-file '" + fileName + "': value has to be 'true' or 'false'.");
                }
            }

        }
    }
//</editor-fold>

    @Override
    public String getCategory(char c) {
        String res = propsCategory.getProperty(Character.toString(c));
        if (res != null) {
            return res;
        }
        return super.getCategory(c);
    }

    @Override
    public boolean isDelimiter(char c) {
        String res = propsSeparator.getProperty(Character.toString(c));
        if (res != null) {
            return Boolean.parseBoolean(res);
        }
        return super.isDelimiter(c);
    }

    @Override
    public boolean isIsolated(char c) {
        String res = propsIsolated.getProperty(Character.toString(c));
        if (res != null) {
            return Boolean.parseBoolean(res);
        }
        return super.isIsolated(c);
    }

}
