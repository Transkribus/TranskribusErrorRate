/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

/**
 *
 * @author gundram
 */
public enum Count {

    /**
     * Number of tokens in ground truth
     */
    GT,
    /**
     * Number of tokens in hypothesis
     */
    HYP,
    /**
     * Number of tokens to insert to change hypothesis to ground truth (missing
     * words in hypothesis or over-segmentation)
     */
    INS,
    /**
     * Number of tokens to delete to change hypothesis to ground truth (too many
     * words in hypothesis or under-segmentation)
     */
    DEL,
    /**
     * Number of tokens to substitute to change hypothesis to ground truth
     */
    SUB,

    /**
     * Sum of INS, DEL and SUB
     */
    ERR,
    /**
     * number of correct tokens
     */
    COR,
    /**
     * true positives (how often is a token into GT and HYP?)
     */
    TP,
    /**
     * false negatives (how often the HYP missed a token)
     */
    FN,
    /**
     * false positives (how often the HYP assigned a not existing token)
     */
    FP
}
