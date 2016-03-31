/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.costcalculator;

import eu.transkribus.errorrate.interfaces.ICostCalculator;

/**
 * makes a simple comparison between recognition and reference. It returns 1 for
 * insertion and deletion, 0 for substitution, if both strings are equal, 1
 * otherwise.
 *
 * @author gundram
 */
public class CostCalculatorDft implements ICostCalculator {

    @Override
    public double getCostSubstitution(String reco, String reference) {
        return reco.equals(reference) ? 0 : 1;
    }

    @Override
    public boolean isEqual(String reco, String reference) {
        return reco.equals(reference);
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
