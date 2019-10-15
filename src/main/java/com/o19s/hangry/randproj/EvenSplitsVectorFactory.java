package com.o19s.hangry.randproj;

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
    Set<Integer> allSet;

    public EvenSplitsVectorFactory(long seed, double[][] vectors) {
        this.seeded = new SeededRandomVectorFactory(seed, vectors[0].length);
        this.allVectors = vectors;
        this.reset();
    }

    @Override
    public void reset() {
        this.regions = new ArrayList<Set<Integer>>();
        allSet = new HashSet<Integer>();
        for (int i = 0; i < this.allVectors.length; i++) {
            allSet.add(i);
        }
        this.regions.add(allSet);
    }

    public Pair<Set<Integer>, Set<Integer>> histogramReport(Histogram hist) {
        int above = 0, below = 0, neighbors = 0, fars = 0;
        Set<Integer> aboveIds = new HashSet<Integer>();
        Set<Integer> belowIds = new HashSet<Integer>();


        // THERE IS A BUG IN HERE!

        for (int quantile = 0; quantile < hist.hist.length; quantile++) {
            double where = quantile / (double) hist.hist.length;
            if (where >= 0.50) {
                above += hist.hist[quantile];
                aboveIds.addAll(hist.ids[quantile]);
            } else {
                below += hist.hist[quantile];
                belowIds.addAll(hist.ids[quantile]);

            }
        }
        return new Pair<Set<Integer>, Set<Integer>>(belowIds, aboveIds);
    }

    private int projectionScore(double[] projection) {
        int score = 0;
        int regIdx = 0;
        for (Set<Integer> region: regions) {
            // split this region with projection
            int lhs = 0;
            int rhs = 0;
            for (Integer id: region) {
                double[] thisVect = allVectors[id.intValue()];
                double dotProd = VectorUtils.dotProduct(thisVect, projection);
                if (dotProd >= 0) {
                    rhs++;
                } else {
                    lhs++;
                }
            }
            score += region.size() - abs(lhs-rhs);
            //System.out.printf("%d - %d %d - %d\n", regIdx, lhs, rhs, score);
            regIdx++;
        }
        return score;
    }

    private double[] drawVector() {
        // Get biggest region to subdivide

        if (regions.size() == 1) {
            return seeded.nextVector();
        }


//        int max = 0;
//        Set<Integer> biggestRegion = null;
//        for (Set<Integer> ids: regions) {
//            if (max < ids.size()) {
//                max = ids.size();
//                biggestRegion = ids;
//            }
//        }
//
//        if (max == 1) {
//            reset();
//            biggestRegion = regions.get(0);
//        }


        Pair<Set<Integer>, Set<Integer>> bestSplit = null;
        double[] bestProjection = null;
        int bestScore = 0;

        int tries = 0;
        int splitness = 0;

        for (tries = 0; tries < 1000; tries++) {
            double[] projection = seeded.nextVector();

            Pair<Set<Integer>, Set<Integer>> histReport = null;
            splitness = projectionScore(projection);
            if (splitness > bestScore) {
                bestProjection = projection;
                bestScore = splitness;
                System.out.printf("Try %d Choosing %d\n", tries, bestScore);
            }
        }
        System.out.printf("Best Split %d %d\n", bestScore, tries);
        return bestProjection;
    }

    @Override
    public double[] nextVector() {
        // check if we need to reset
//        if (regions.first().size() <= 1) {
//            reset();
//        }


        double[] bestProjection = drawVector();

        // Apply best projection and regenerate the regions
        List<Set<Integer>> newRegions = new ArrayList<Set<Integer>>();
        for (Set<Integer> ids: regions) {
            Histogram hist = VectorUtils.projectionPerformance(ids, this.allVectors, bestProjection);
            Pair<Set<Integer>, Set<Integer>> histReport = histogramReport(hist);
            if (histReport.getValue().size() > 0) {
                newRegions.add(histReport.getValue());
            }
            if (histReport.getKey().size() > 0) {
                newRegions.add(histReport.getKey());
            }
        }

        this.regions = newRegions;

        return bestProjection;
    }
}
