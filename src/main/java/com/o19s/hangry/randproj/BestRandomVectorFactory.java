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

public class BestRandomVectorFactory implements RandomVectorFactory {

    SeededRandomVectorFactory seeded;
    double[][] allVectors;
    List<Set<Integer>> regions;
    Set<Integer> allSet;

    private static class BiggestSetFirstComp implements Comparator<Set<Integer>> {

        @Override
        public int compare(Set<Integer> o1, Set<Integer> o2) {
            return o2.size() - o1.size();
        }
    }

    public BestRandomVectorFactory(int seed, double[][] vectors) {
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


        for (int quartile = 0; quartile < hist.hist.length; quartile++) {
            if (quartile / (double) hist.hist.length <= 0.05) {
                fars += hist.hist[quartile];
            } else if (quartile / (double) hist.hist.length >= 0.95) {
                neighbors += hist.hist[quartile];
            } else if (quartile / (double) hist.hist.length >= 0.50) {
                above += hist.hist[quartile];
                aboveIds.addAll(hist.ids[quartile]);
            } else {
                below += hist.hist[quartile];
                belowIds.addAll(hist.ids[quartile]);

            }
        }
        return new Pair<Set<Integer>, Set<Integer>>(belowIds, aboveIds);
    }

    @Override
    public double[] nextVector() {
        // check if we need to reset
//        if (regions.first().size() <= 1) {
//            reset();
//        }

        // Get biggest region to subdivide
        int max = 0;
        Set<Integer> biggestRegion = null;
        for (Set<Integer> ids: regions) {
            if (max < ids.size()) {
                max = ids.size();
                biggestRegion = ids;
            }
        }

        if (max == 1) {
            reset();
            biggestRegion = regions.get(0);
        }


        Pair<Set<Integer>, Set<Integer>> bestSplit = null;
        double[] bestProjection = null;
        int bestDiff = this.allVectors.length + 1;

        for (int i = 0; i < 1000; i++) {
            double[] projection = seeded.nextVector();

            Histogram hist = VectorUtils.projectionPerformance(biggestRegion, this.allVectors, projection);
            Pair<Set<Integer>, Set<Integer>> histReport = histogramReport(hist);
            int diff = abs(histReport.getKey().size() - histReport.getValue().size());
            if (diff < bestDiff) {
                bestSplit = histReport;
                bestProjection = projection;
                bestDiff = diff;
            }
            if (diff == 0) {
                break;
            }
            if (diff < biggestRegion.size() / 100) {
                break;
            }
        }

        // Apply best projection and regenerate the regions
        List<Set<Integer>> newRegions = new ArrayList<Set<Integer>>();
        for (Set<Integer> ids: regions) {
            Histogram hist = VectorUtils.projectionPerformance(ids, this.allVectors, bestProjection);
            Pair<Set<Integer>, Set<Integer>> histReport = histogramReport(hist);
            newRegions.add(histReport.getKey());
            newRegions.add(histReport.getValue());
        }

        this.regions = newRegions;

        return bestProjection;
    }
}
