package com.o19s.hangry;

public class RandomProjections {
    // Assumes normalized vector inputs

    private VectorFactory _vf;
    private double[][] _projections;

    public RandomProjections(int numProjections, int dims) {
        _vf = new VectorFactory();
        _projections = new double[numProjections][];

        for (int i = 0; i < numProjections; i++) {
            _projections[i] = _vf.random(dims, -1.0, 1.0);
        }
    }

    private static double sameSign(double val1, double val2) {
        return Math.signum(val1 * val2);
    }


    public double similarity(double[] vect1, double[] vect2) {
        double same = 0;
        for (int i = 0; i < _projections.length; i++) {
            double dp1 = VectorFactory.dotProduct(vect1, _projections[i]);
            double dp2 = VectorFactory.dotProduct(vect2, _projections[i]);
            same += sameSign(dp1, dp2);

        }
        return same / (double)_projections.length;
    }


}
