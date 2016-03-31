/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.costcalculator;

import eu.transkribus.errorrate.interfaces.ICostCalculator;
import java.util.Locale;

/**
 * like {@link CostCalculatorDft} but applying a
 * {@link String#toUpperCase(java.util.Locale)} with {@link Locale#ROOT} to
 * reference and recognition.
 *
 * @author gundram
 */
public class CostCalculatorDftUpper extends CostCalculatorDft implements ICostCalculator {

    @Override
    public double getCostSubstitution(String reco, String reference) {
        return super.getCostSubstitution(convert(reco), convert(reference));
    }

    private String convert(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean isEqual(String reco, String reference) {
        return convert(reco).equals(convert(reference));
    }

    @Override
    public double getCostInsertion(String reference) {
        return 1;
    }

    @Override
    public double getCostDeletion(String reco) {
        return 1;
    }

}
