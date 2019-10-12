package com.o19s.hangry.randproj;

// TODO: abstract an interface, this
// prescribes random projections managed and recreated via a remembered
// seed, which is generally good enough for our purposes now, but we
// probably need more reliability to store and remember the seeds
public class RandomProjectionTree {
    // Assumes normalized vector inputs

    private double[][] _projections;

    public RandomProjectionTree(int depth, RandomVectorFactory vectFactory) {
        _projections = new double[depth][];

        for (int i = 0; i < depth; i++) {
            _projections[i] = vectFactory.nextVector();
        }
    }

    private static double sameSign(double val1, double val2) {
        return Math.signum(val1 * val2);
    }

    public int getDepth() {
        return _projections.length;
    }


    public double similarity(double[] vect1, double[] vect2) {
        double same = 0;
        for (double[] projection : _projections) {
            double dp1 = VectorUtils.dotProduct(vect1, projection);
            double dp2 = VectorUtils.dotProduct(vect2, projection);
            same += sameSign(dp1, dp2);

        }
        return same / (double)_projections.length;
    }

    public String encodeProjection(double[] vect, int depth) {

        StringBuilder s = new StringBuilder();

        double same = 0;
        for (int i = 0; i < depth; i++) {
            double sign = Math.signum(VectorUtils.dotProduct(vect, _projections[i]));
            if (sign > 0) {
                s.append('1');
            } else {
                s.append('0');
            }
        }
        return s.toString();
    }


}
