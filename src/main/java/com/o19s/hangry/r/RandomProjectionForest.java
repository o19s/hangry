package com.o19s.hangry.r;

import java.util.Random;

public class RandomProjectionForest {

    private final RandomProjectionTree[] trees;
    private final int numHyperplanes;

    public RandomProjectionForest(final int numTrees, final int numHyperplanes, final int numDimensions,
                                  final long seed) {

        this.numHyperplanes = numHyperplanes;

        trees = new RandomProjectionTree[numTrees];
        final Random random = new Random(seed);

        for (int i = 0; i < numTrees; i++) {
            trees[i] = RandomProjectionTree.newTree(numHyperplanes, numDimensions, random);
        }

    }

    public int getNumTrees() {
        return trees.length;
    }

    public int getNumHyperplanes() {
        return numHyperplanes;
    }

    public String[] project(final double[] vector) {
        final String[] hash = new String[trees.length];
        for (int i = 0; i < hash.length; i++) {
            hash[i] = trees[i].project(vector);
        }
        return hash;
    }
}
