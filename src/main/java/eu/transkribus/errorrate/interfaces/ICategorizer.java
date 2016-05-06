/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.interfaces;

import eu.transkribus.errorrate.tokenizer.TokenizerCategorizer;

/**
 * The categorizer defines 3 properties of a unicode character (codepoint=CP). A
 * Tokenizer uses this categorizer to tokenize a CP-sequence (see
 * {@link TokenizerCategorizer} for more details).
 *
 * @author gundram
 */
public interface ICategorizer {

    /**
     * should return the category of the codepoint.
     *
     * @param c codepoint
     * @return the corresponding category
     */
    public String getCategory(char c);

    /**
     * should return, if the codepoint is a delimiter (separator) of tokens.
     *
     * @param c codepoint
     * @return if the codepoint is a delimiter
     */
    public boolean isDelimiter(char c);

    /**
     * should return if the codepoint cannot be combined with consecutive
     * codepoint from the same category to one token.
     *
     * @param c codepoint
     * @return if the codepoint is an isolated token
     */
    public boolean isIsolated(char c);

    /**
     * To change an existing categorizer in an easy way, it is possible to refer
     * the categorizer to a java property file. This file is UTF-8 encoded and
     * contains a mapping (String for a category or Boolean if the character is
     * a delimiter or if it is isolated).
     */
    public interface IPropertyConfigurable extends ICategorizer {

        /**
         * path to a file fulfilling the java property layout. Coding have to be
         * UTF-8. To put an "'" to the letter category, a line in the property
         * file could look like <br> 
         * <tt>'=L</tt>
         *
         * @param filename file name
         */
        public void putCategoryProperties(String filename);

        /**
         * path to a file fulfilling the java property layout. Coding have to be
         * UTF-8. To add the codepoint "CHARACTER TABULATION" into the class of
         * separators, a line in the property file could look like that:
         * <br><tt>\u0009=true</tt>
         *
         * @param filename file name
         */
        public void putSeparatorProperties(String filename);

        /**
         * path to a file fulfilling the java property layout. Coding have to be
         * UTF-8. To separate numbers from each others, one can add lines like
         * that: <br> <tt>0=true<br>1=true<br>&hellip;<br>9=true</tt>
         *
         * @param filename filen ame
         */
        public void putIsolatedProperties(String filename);

    }

}
