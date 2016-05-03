/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.interfaces;

/**
 * A String normalizer should normalize a String. For example there can be a
 * substitution of characters and deleting consecutive spaces
 *
 * @author gundram
 */
public interface IStringNormalizer {

    /**
     * normalize the given String
     *
     * @param string input string
     * @return normalized string
     */
    public String normalize(String string);

    /**
     * if it is possible to modify the normalizer by a java property file, the
     * class implementing {@link  IStringNormalizer} should also implement this
     * interface.
     */
    public interface IPropertyConfigurable extends IStringNormalizer {

        /**
         * path to a file fulfilling the java property layout. Coding have to be
         * UTF-8. To substitute "a\u0308" by an "ä", there have to be the line
         * "a\u0308=ä" into the property file.
         *
         * @param filename file name
         */
        public void putSubstitutionProperties(String filename);

    }
}
