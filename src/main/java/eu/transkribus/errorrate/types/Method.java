/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

/**
 * Methods, which should be calculated
 */
public enum Method {

    /**
     * Word Error Rate
     */
    WER,
    /**
     * Word Error Rate constrained to letters and numbers
     */
    WER_ALNUM,
    /**
     * Character Error Rate
     */
    CER,
    /**
     * Character Error Rate constrained to letters and numbers
     */
    CER_ALNUM,
    /**
     * Bag of Tokens/Words
     */
    BOT,
    /**
     * Bag of Tokens/Words constrained to letters and numbers
     */
    BOT_ALNUM,

}
