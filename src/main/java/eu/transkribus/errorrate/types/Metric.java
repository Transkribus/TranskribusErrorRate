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
public enum Metric {

    /**
     * Error [0,infinity) (0 = perfect). Mostly in [0,1]<br>
     * ERR = (#INS + #DEL + #SUB) / #GT
     */
    ERR,
    /**
     * Accuracy [0,1], (1 = perfect).<br>
     * ACC = #COR / #GT
     */
    ACC,
    /**
     * Precision [0,1], (1 = perfect)<br>
     * PREC = #TP / (#FP + #TP)
     */
    PREC,
    /**
     * Recall [0,1], (1 = perfect)<br>
     * REC = #TP / (#FN + #TP)
     */
    REC,
    /**
     * F-measure [0,1], (1 = perfect)<br>
     * F = 2*#TP / (2*#TP + #FP + #TP)
     */
    F
}
