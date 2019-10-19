package com.o19s.hangry.randproj;

import javafx.collections.transformation.SortedList;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.Math.abs;

public class EvenSplitsVectorFactory implements RandomVectorFactory {

    SeededRandomVectorFactory seeded;
    double[][] allVectors;
    List<Set<Integer>> regions;
    Set<Integer> biggestRegion;

    int numVectors;
    Set<Integer> allSet;

    public EvenSplitsVectorFactory(long seed, double[][] vectors) {
        this.seeded = new SeededRandomVectorFactory(seed, vectors[0].length);
        this.allVectors = vectors;
        this.reset();
    }

    private void regionReport() {
        System.out.printf("%d regions| biggest %d | ", regions.size(), biggestRegion.size());

        for (Set<Integer> region: regions) {
            System.out.printf("%d,", region.size());
        }
        System.out.println();
    }

    @Override
    public void reset() {
        if (this.numVectors > 0) {
//            System.out.printf("Resetting after %d vects\n", this.numVectors);
//            regionReport();
        }
        this.regions = new ArrayList<Set<Integer>>();
        allSet = new HashSet<Integer>();
        biggestRegion = allSet;
        for (int i = 0; i < this.allVectors.length; i++) {
            allSet.add(i);
        }
        this.regions.add(allSet);
        numVectors = 0;
    }

    public Pair<Set<Integer>, Set<Integer>> histogramReport(Set<Integer> region, double[] projection) {

        Set<Integer> aboveIds = new HashSet<Integer>();
        Set<Integer> belowIds = new HashSet<Integer>();

        int lhs = 0;
        int rhs = 0;
        for (Integer id: region) {
            double[] thisVect = allVectors[id.intValue()];
            double dotProd = VectorUtils.dotProduct(thisVect, projection);
            if (dotProd >= 0) {
                aboveIds.add(id);
            } else {
                belowIds.add(id);
            }
        }
        return new Pair<Set<Integer>, Set<Integer>>(belowIds, aboveIds);
    }

    protected double sizeProjectionScore(double[] projection, Set<Integer> biggestRegion) {
        // the best projection evenly splits biggestRegion
        int lhs = 0;
        int rhs = 0;
        for (Integer id: biggestRegion) {
            double[] thisVect = allVectors[id.intValue()];
            double dotProd = VectorUtils.dotProduct(thisVect, projection);
            if (dotProd >= 0) {
                rhs++;
            } else {
                lhs++;
            }
        }
        int score = biggestRegion.size() - abs(lhs-rhs);
        return score;
    }

    protected double varianceProjectionScore(double[] projection, Set<Integer> biggestRegion) {
        // the best projection minimizes the variance
        // on each size, as approxmated by the result of the dot
        // product with the
        double lhsDotsMean = 0;
        double rhsDotsMean = 0;
        double allDotsMean = 0;

        List<Double> lhsDots = new ArrayList<Double>();
        List<Double> rhsDots = new ArrayList<Double>();
        List<Double> allDots = new ArrayList<Double>();


        for (Integer id: biggestRegion) {
            double[] thisVect = allVectors[id.intValue()];
            double dotProd = VectorUtils.dotProduct(thisVect, projection);
            if (dotProd >= 0) {
                rhsDotsMean += dotProd;
                rhsDots.add(dotProd);
            } else {
                lhsDotsMean += abs(dotProd);
                lhsDots.add(abs(dotProd));
            }
            allDots.add(abs(dotProd));
            allDotsMean += abs(dotProd);
        }


        lhsDotsMean = lhsDotsMean / lhsDots.size();
        rhsDotsMean = rhsDotsMean / rhsDots.size();
        allDotsMean = allDotsMean / allDots.size();

        double allVar = 0;
        for (Double dp: allDots) {
            allVar += (dp.doubleValue() - allDotsMean) * (dp.doubleValue() - allDotsMean);
        }

        double lhsVar = 0;
        for (Double dp: lhsDots) {
            lhsVar += (dp.doubleValue() - lhsDotsMean) * (dp.doubleValue() - lhsDotsMean);
        }


        double rhsVar = 0;
        for (Double dp: rhsDots) {
            rhsVar += (dp.doubleValue() - rhsDotsMean) * (dp.doubleValue() - rhsDotsMean);
        }

        if (lhsDots.size() == 0) {
            return 0;
        }
        if (rhsDots.size() == 0) {
            return 0;
        }

        return allVar-(lhsVar + rhsVar);
    }

    private double[] drawVector() {
        // Get biggest region to subdivide

//        if (regions.size() == 1) {
//            return seeded.nextVector();
//        }

        Pair<Set<Integer>, Set<Integer>> bestSplit = null;
        double[] bestProjection = null;
        double bestScore = 0;

        int tries = 0;
        double splitScore = 0;

        //regionReport();
        for (tries = 0; tries < 4000000; tries++) {
            double[] projection = seeded.nextVector();

            Pair<Set<Integer>, Set<Integer>> histReport = null;
            splitScore = varianceProjectionScore(projection, biggestRegion);
            if (splitScore > bestScore) {
                bestProjection = projection;
                bestScore = splitScore;
            }
        }
        System.out.printf("Var Reduced %f\n", splitScore);
        return bestProjection;
    }

    private void addRegion(Set<Integer> newRegion, List<Set<Integer>> newRegions) {
        if (newRegion.size() > biggestRegion.size()) {
            biggestRegion = newRegion;
        }
        newRegions.add(newRegion);
    }

    private void splitRegionByProjection(double[] bestProjection, Set<Integer> region, List<Set<Integer>> newRegions) {
        Histogram hist = VectorUtils.projectionPerformance(region, this.allVectors, bestProjection);
        Pair<Set<Integer>, Set<Integer>> histReport = histogramReport(region, bestProjection);
        Set<Integer> lhs = histReport.getKey();
        Set<Integer> rhs = histReport.getValue();
        if (lhs.size() > 0) {
            addRegion(lhs, newRegions);
        }
        if (rhs.size() > 0) {
            addRegion(rhs, newRegions);
        }
    }

    @Override
    public double[] nextVector() {
        double[] bestProjection = drawVector();

        // we've exhausted our ability to split the regions further
        // or so it seems
        if (bestProjection == null) {
            return null;
        }

        //scores.add(projectionScore(bestProjection));

        // Apply best projection and regenerate the regions
        List<Set<Integer>> newRegions = new ArrayList<Set<Integer>>();
        //System.out.printf("Before %s\n", regions.toString());
        biggestRegion = new TreeSet<Integer>();
        for (Set<Integer> ids: regions) {
            splitRegionByProjection(bestProjection, ids, newRegions);
        }
       // System.out.printf("After %s\n", newRegions.toString());

        this.regions = newRegions;
        this.numVectors++;

        return bestProjection;
    }
}
