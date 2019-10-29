package com.o19s.hangry.r;

import java.util.Random;

public class RandomProjectionTree {

    private final double[][] hyperplanes;

    // this is private to ensure the hyperplanes are normalised
    private RandomProjectionTree(final double[][] hyperplanes) {
        this.hyperplanes = hyperplanes;
    }

    public String project(final double[] vector) {

        final char[] hash = new char[hyperplanes.length];
        for (int i = 0; i < hyperplanes.length; i++) {
            hash[i] = VectorZ.dot(vector, hyperplanes[i]) < 0 ? '0' : 1;
        }
        return new String(hash);
    }

    public static RandomProjectionTree newTree(final int numHyperplanes, final int numDimensions, final Random random) {
        final double[][] hyperplanes = new double[numHyperplanes][numDimensions];
        for (int i = 0; i < numHyperplanes; i++) {
            for (int j = 0; j < numDimensions; j++) {
                final double value = random.nextDouble();
                hyperplanes[i][j] = random.nextBoolean() ? value : -value;
            }
            hyperplanes[i] = VectorZ.norm(hyperplanes[i]);
        }
        return new RandomProjectionTree(hyperplanes);
    }

}
