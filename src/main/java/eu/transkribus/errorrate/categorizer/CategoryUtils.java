/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.categorizer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class that maps each unicode point to its general category category
 *
 * @author gundram
 */
public class CategoryUtils {

    private static final Logger LOG = Logger.getLogger(CategoryUtils.class.getName());

    /**
     * returns the category of the codepoint. The return value has 2 characters.
     * The first (uppercase) character determines the main general category, the
     * second (lowercase) determines the sub category.
     *
     * @param c
     * @return
     */
    public static final String getCategory(char c) {
        switch (Character.getType(c)) {
            case Character.CONTROL:
                return "Cc";
            case Character.FORMAT:
                return "Cf";
            case Character.UNASSIGNED:
                return "Cn";
            case Character.PRIVATE_USE:
                return "Co";
            case Character.SURROGATE:
                return "Cs";

            case Character.LOWERCASE_LETTER:
                return "Ll";
            case Character.MODIFIER_LETTER:
                return "Lm";
            case Character.OTHER_LETTER:
                return "Lo";
            case Character.TITLECASE_LETTER:
                return "Lt";
            case Character.UPPERCASE_LETTER:
                return "Lu";

            case Character.COMBINING_SPACING_MARK:
                return "Mc";
            case Character.ENCLOSING_MARK:
                return "Me";
            case Character.NON_SPACING_MARK:
                return "Mn";

            case Character.DECIMAL_DIGIT_NUMBER:
                return "Nd";
            case Character.LETTER_NUMBER:
                return "Nl";
            case Character.OTHER_NUMBER:
                return "No";

            case Character.CONNECTOR_PUNCTUATION:
                return "Pc";
            case Character.DASH_PUNCTUATION:
                return "Pd";
            case Character.END_PUNCTUATION:
                return "Pe";
            case Character.FINAL_QUOTE_PUNCTUATION:
                return "Pf";
            case Character.INITIAL_QUOTE_PUNCTUATION:
                return "Pi";
            case Character.OTHER_PUNCTUATION:
                return "Po";
            case Character.START_PUNCTUATION:
                return "Ps";

            case Character.CURRENCY_SYMBOL:
                return "Sc";
            case Character.MODIFIER_SYMBOL:
                return "Sk";
            case Character.MATH_SYMBOL:
                return "Sm";
            case Character.OTHER_SYMBOL:
                return "So";

            case Character.LINE_SEPARATOR:
                return "Zl";
            case Character.PARAGRAPH_SEPARATOR:
                return "Zp";
            case Character.SPACE_SEPARATOR:
                return "Zs";
            default:
                LOG.log(Level.SEVERE, "no category found for {0} - maybe bug in code.", Character.getName(c));
                return null;

        }
    }

    /**
     * returns the general category of the codepoint. The return value has one
     * (uppercase) character which determines the main general category.
     *
     * @param c
     * @return
     */
    public static String getCategoryGeneral(char c) {
        switch (Character.getType(c)) {
            case Character.CONTROL:
            case Character.FORMAT:
            case Character.UNASSIGNED:
            case Character.PRIVATE_USE:
            case Character.SURROGATE:
                return "C";
            case Character.LOWERCASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.OTHER_LETTER:
            case Character.TITLECASE_LETTER:
            case Character.UPPERCASE_LETTER:
                return "L";
            case Character.COMBINING_SPACING_MARK:
            case Character.ENCLOSING_MARK:
            case Character.NON_SPACING_MARK:
                return "M";
            case Character.DECIMAL_DIGIT_NUMBER:
            case Character.LETTER_NUMBER:
            case Character.OTHER_NUMBER:
                return "N";
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.START_PUNCTUATION:
                return "P";
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.MATH_SYMBOL:
            case Character.OTHER_SYMBOL:
                return "S";
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.SPACE_SEPARATOR:
                return "Z";
            default:
                LOG.log(Level.SEVERE, "no category found for {0} - maybe bug in code.", Character.getName(c));
                return null;

        }
    }
}
