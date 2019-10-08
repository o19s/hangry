package com.o19s.hangry;

import java.util.Base64;

public class RandomProjections {
    // Assumes normalized vector inputs

    private VectorFactory _vf;
    private double[][] _projections;

    public RandomProjections(byte numProjections, short dims, byte seed) {
        _vf = new VectorFactory(seed, dims);
        _projections = new double[numProjections][];

        for (int i = 0; i < numProjections; i++) {
            _projections[i] = _vf.random(-1.0, 1.0);
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

    private String projectionHeader() {
        byte[] projection = new byte[4];
        projection[0] = _vf._seed;
        projection[1] = (byte)(_vf._dims & 0x00ff);
        projection[2] = (byte)(_vf._dims & 0xff00 >> 16);
        projection[3] = (byte)_projections.length;
        return Base64.getEncoder().encodeToString(projection);
    }

    public String encodeProjection(double[] vect) {

        StringBuilder s = new StringBuilder(projectionHeader());

        double same = 0;
        for (int i = 0; i < _projections.length; i++) {
            double sign = Math.signum(VectorFactory.dotProduct(vect, _projections[i]));
            if (sign > 0) {
                s.append('1');
            } else {
                s.append('0');
            }

        }
        return s.toString();
    }

    public String getQuery(double[] vect, int resolution) {
        StringBuilder s = new StringBuilder(projectionHeader());
        return encodeProjection(vect).subSequence(0, resolution-1).toString();

    }


}
