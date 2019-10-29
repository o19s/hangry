package com.o19s.hangry.r;

public class VectorZ {

    public static double dot(final double[] v1, final double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }

        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += v1[i] * v2[i];
        }

        return sum;
    }

    public static double magnitude(final double[] vector) {
        double sum = 0.0;
        for (final double v : vector) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    public static double[] norm(final double[] vector) {

        final double magnitude = magnitude(vector);

        final double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] / magnitude;
        }

        return result;
    }
}
