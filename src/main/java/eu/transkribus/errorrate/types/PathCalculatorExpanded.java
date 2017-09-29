/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author gundram
 * @param <Reco> hypothesis type
 * @param <Reference> reference type
 */
public class PathCalculatorExpanded<Reco, Reference> {

    private static final Logger LOG = Logger.getLogger(PathCalculatorExpanded.class.getName());
    private final List<ICostCalculator<Reco, Reference>> costCalculators = new ArrayList<>();
    private final Comparator<IDistance<Reco, Reference>> comparatorAcc = new Comparator<IDistance<Reco, Reference>>() {

        @Override
        public int compare(IDistance<Reco, Reference> o1, IDistance<Reco, Reference> o2) {
            return Double.compare(o1.getCostsAcc(), o2.getCostsAcc());
        }
    };

    public static interface ICostCalculator<Reco, Reference> {

        public void init(DistanceMat<Reco, Reference> mat, List<Reco> recos, List<Reference> refs);

        public boolean isValid(int y, int x);

//        public double getCost(PathCalculatorExpanded.IDistance[][] map, int y, int x);
        public IDistance<Reco, Reference> getDistance(int y, int x);

    }

    public void addCostCalculator(ICostCalculator<Reco, Reference> costCalculator) {
        costCalculators.add(costCalculator);
    }

    public static enum Manipulation {

        INS, DEL, SUB, COR, SPECIAL;
    }

    /**
     * Distance type of the dynamic programming table
     *
 * @param <Reco> hypothesis type
 * @param <Reference> reference type
     */
    public static interface IDistance<Reco, Reference> {

        /**
         * cost of own manipulation
         *
         * @return cost
         */
        public double getCosts();

        /**
         * prefix cost for this position
         *
         * @return accumulated cost
         */
        public double getCostsAcc();

        /**
         * list of recognition tokens used in this method. It can have length 0
         * (insertion), 1 (substitution, deletion) or anything else (special)
         *
         * @return recognition
         */
        public List<Reco> getRecos();

        /**
         * list of reference tokens used in this method. It can have length 0
         * (deletion), 1 (substitution, insertion) or anything else (special)
         *
         * @return reference
         */
        public List<Reference> getReferences();

        /**
         * returns manipulation (see {@link Manipulation})
         *
         * @return manipulation
         */
        public Manipulation getManipulation();

        /**
         * returns the coordinates from the previous {@link IDistance} object,
         * which is part of the lowest cost path.
         *
         * @return previous best position
         */
        public int[] getPrevious();

        /**
         * returns the used cost calculator.
         *
         * @return used costCalculator
         */
        public ICostCalculator<Reco, Reference> getCostCalculator();

    }

    public static class DistanceMat<Reco, Reference> {

        private final IDistance<Reco, Reference>[][] dist;

        /**
         * creates a distance matrix from the given size
         *
         * @param y row dimension
         * @param x column dimension
         */
        public DistanceMat(int y, int x) {
            this.dist = new IDistance[y][x];
        }

        /**
         * returns the internal distance matrix
         *
         * @return distance matrix
         */
        public IDistance<Reco, Reference>[][] getDist() {
            return dist;
        }

        /**
         * returns a given element from the distance matrix
         *
         * @param y row dimension
         * @param x column dimension
         * @return distance of specific position
         */
        public IDistance<Reco, Reference> get(int y, int x) {
            return dist[y][x];
        }

        /**
         * sets an element into the distance matrix
         *
         * @param y row dimension
         * @param x column dimension
         * @param distance distance
         */
        public void set(int y, int x, IDistance<Reco, Reference> distance) {
            dist[y][x] = distance;
        }

        /**
         * returns the size in y dimension
         *
         * @return size of y (recognition length + 1)
         */
        public int getSizeY() {
            return dist.length;
        }

        /**
         * returns the size in x dimension
         *
         * @return size of x  (reference length + 1)
         */
        public int getSizeX() {
            return dist[0].length;
        }

        /**
         * returns the last element. Note that {@link IDistance#getCostsAcc()}
         * gives the minimal costs to come from the recognition to the
         * reference.
         *
         * @return last element
         */
        public IDistance<Reco, Reference> getLastElement() {
            return get(getSizeY() - 1, getSizeX() - 1);
        }

        /**
         * returns the path which results the minimal costs through the distance
         * matrix.
         *
         * @return best path through matrix
         */
        public List<IDistance<Reco, Reference>> getBestPath() {
            IDistance<Reco, Reference> lastElement = getLastElement();
            if (lastElement == null) {
                throw new RuntimeException("Distance Matrix not completely calculated.");
            }
            LinkedList<IDistance<Reco, Reference>> res = new LinkedList<>();
            res.add(lastElement);
            int[] pos = lastElement.getPrevious();
            while (pos != null) {
                lastElement = dist[pos[0]][pos[1]];
                res.addFirst(lastElement);
                pos = lastElement.getPrevious();
            }
            res.removeFirst();
            return res;
        }

    }

    /**
     * calculates the dynamic programming.
     *
     * @param reco hypothesis
     * @param ref reference
     * @return distance matrix
     */
    public DistanceMat<Reco, Reference> calcDynProg(List<Reco> reco, List<Reference> ref) {
        if (ref == null || reco == null) {
            throw new RuntimeException("target or output is null");
        }
        //adds an empty element at the beginning. This simulates the empty sequence
        boolean deleteFirstReco = false;
        if (reco.isEmpty() || reco.get(0) != null) {
            reco.add(0, null);
            deleteFirstReco = true;
        }
        //adds an empty element at the beginning. This simulates the empty sequence
        boolean deleteFirstRef = false;
        if (ref.isEmpty() || ref.get(0) != null) {
            ref.add(0, null);
            deleteFirstRef = true;
        }
        int recoLength = reco.size();
        int refLength = ref.size();
        //creates the distance matrix
        final DistanceMat<Reco, Reference> distMat = new DistanceMat<>(recoLength, refLength);
        //put starting point into the distance matrix.
        IDistance<Reco, Reference> distanceInfinity = new Distance<>(null, null, 0, Double.MAX_VALUE, null);
        distMat.set(0, 0, new Distance<Reco, Reference>(null, null, 0, 0, null));
        final LinkedList<IDistance<Reco, Reference>> candidates = new LinkedList<>();
        //initialize all costcalculators with the distance matrix, the recognition and the reference
        for (ICostCalculator<Reco, Reference> costCalculator : costCalculators) {
            costCalculator.init(distMat, reco, ref);
        }
        //calculates the rest of the distance matrix
        for (int recoIdx = 0; recoIdx < recoLength; recoIdx++) {
            for (int refIdx = recoIdx == 0 ? 1 : 0; refIdx < refLength; refIdx++) {
                candidates.clear();
                //if costcalculators can construct a path to the given position, they are added to the candidates list
                for (ICostCalculator<Reco, Reference> costCalculator : costCalculators) {
                    if (costCalculator.isValid(recoIdx, refIdx)) {
                        candidates.add(costCalculator.getDistance(recoIdx, refIdx));
                    }
                }
                //adds new distance in the distance matrix.
                //If the list of candidates is empty the costs are infinity.
                //Otherwise the candidate is taken, which generates the minimal costs.
                distMat.set(recoIdx, refIdx, candidates.isEmpty() ? distanceInfinity : Collections.min(candidates, comparatorAcc));
            }
        }
        //delete previous addes elements
        if (deleteFirstReco) {
            reco.remove(0);
        }
        //delete previous addes elements
        if (deleteFirstRef) {
            ref.remove(0);
        }
        return distMat;

    }

    /**
     * returns best path / minimal cost path
     *
     * @param reco hypothesis
     * @param ref reference
     * @return best path through recognition - reference matching
     */
    public List<IDistance<Reco, Reference>> calcBestPath(Reco[] reco, Reference[] ref) {
        return calcBestPath(Arrays.asList(reco), Arrays.asList(ref));
    }

    /**
     * returns best path / minimal cost path
     *
     * @param reco hypothesis
     * @param ref reference
     * @return best path through recognition - reference matching
     */
    public List<IDistance<Reco, Reference>> calcBestPath(List<Reco> reco, List<Reference> ref) {
        return calcDynProg(reco, ref).getBestPath();
    }

    /**
     * calculates the minimal costs to come from the recognition to the reference
     * @param reco hypothesis
     * @param ref reference
     * @return cost of best path
     */
    public double calcCosts(List<Reco> reco, List<Reference> ref) {
        return calcDynProg(reco, ref).getLastElement().getCostsAcc();
    }

    /**
     * Distance matrix
 * @param <Reco> hypothesis type
 * @param <Reference> reference type
     */
    public static class Distance<Reco, Reference> implements IDistance<Reco, Reference> {

//        private final Distance previousDistance;
        private final Manipulation manipulation;
        private final double costs;
        private final double costsAcc;
        private final int[] previous;
        private List<Reco> recos;
        private List<Reference> references;
        private final ICostCalculator<Reco, Reference> costCalculator;

        private Distance(Manipulation manipulation, ICostCalculator<Reco, Reference> costCalculator, double costs, double costAcc, int[] previous) {
            this.costCalculator = costCalculator;
            this.manipulation = manipulation;
            this.costs = costs;
            this.costsAcc = costAcc;
            this.previous = previous;
        }

        public Distance(Manipulation manipulation, ICostCalculator<Reco, Reference> costCalculator, double costs, double costAcc, int[] previous, Reco reco, List<Reference> references) {
            this(manipulation, costCalculator, costs, costAcc, previous);
            this.recos = new LinkedList<>();
            recos.add(reco);
            this.references = references;
        }

        public Distance(Manipulation manipulation, ICostCalculator<Reco, Reference> costCalculator, double costs, double costAcc, int[] previous, Reco reco, Reference reference) {
            this(manipulation, costCalculator, costs, costAcc, previous);
            this.recos = new LinkedList<>();
            recos.add(reco);
            this.references = new LinkedList<>();
            references.add(reference);
        }

        public Distance(Manipulation manipulation, ICostCalculator<Reco, Reference> costCalculator, double costs, double costAcc, int[] previous, List<Reco> recos, Reference reference) {
            this(manipulation, costCalculator, costs, costAcc, previous);
            this.references = new LinkedList<>();
            references.add(reference);
            this.recos = recos;
        }

        public Distance(Manipulation manipulation, ICostCalculator<Reco, Reference> costCalculator, double costs, double costAcc, int[] previous, List<Reco> recos, List<Reference> references) {
            this.costCalculator = costCalculator;
            this.manipulation = manipulation;
            this.costs = costs;
            this.previous = previous;
            this.costsAcc = costAcc;
            this.recos = recos;
            this.references = references;
        }

        @Override
        public double getCosts() {
            return costs;
        }

        @Override
        public double getCostsAcc() {
            return costsAcc;
        }

        @Override
        public List<Reference> getReferences() {
            return references;
        }

        @Override
        public List<Reco> getRecos() {
            return recos;
        }

        @Override
        public Manipulation getManipulation() {
            return manipulation;
        }

        @Override
        public int[] getPrevious() {
            return previous;
        }

        @Override
        public String toString() {
            return "cost=" + costs + ";manipulation=" + manipulation + ";costAcc=" + costsAcc;
        }

        @Override
        public ICostCalculator<Reco, Reference> getCostCalculator() {
            return costCalculator;
        }

    }
}
