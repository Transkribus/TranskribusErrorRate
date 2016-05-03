/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.interfaces;

/**
 * this interface is used to calculate the distance between two token sequences.
 * Therefore, one has to define the costs to insert a specific
 * groundtruth/reference, delete a hypothesis/recognition and substitution costs
 * from one reference to one recognition. In the CostCalculater can decide, if
 * two references are equal.
 *
 * @author gundram
 */
public interface ICostCalculator {

    /**
     *
     * @param reco hypothesis
     * @param reference groundtruth
     * @return cost ≥0
     */
    public double getCostSubstitution(String reco, String reference);

    /**
     * should return, if the comparison of these tokens should count as correct
     * or as substitution (independent from the costs). Mostly is returns true,
     * if {@link #getCostSubstitution(java.lang.String, java.lang.String) }
     * return 0.0.
     *
     * @param reco hypothesis
     * @param reference groundtruth
     * @return if recognition equals reference
     */
    public boolean isEqual(String reco, String reference);

    /**
     * costs to insert the given reference
     *
     * @param reference groundtruth
     * @return costs ≥0
     */
    public double getCostInsertion(String reference);

    /**
     *
     * @param reco hypothesis
     * @return cost ≥0
     */
    public double getCostDeletion(String reco);

    public interface CategoryDependent {

        /**
         * If the tokens are seperated by a categorizer, each token
         * (refenrence/recognition) is from the same category. It is possible to
         * make these costs dependent on the category (punctuation not as
         * expensive as word)
         *
         * @param categorizer categorizer
         */
        public void setCategorizer(ICategorizer categorizer);

    }

}
