package com.o19s.hangry.randproj;

import java.util.Set;

public class VectorUtils {

    // Performs a dot product up until the size of vect1 for speed
    // if vect2.length < vect1.length, you get a ArrayIndexOutOfBoundsException
    public static double dotProduct(double[] vect1, double[] vect2) {
        double sum = 0;
        for (int i = 0; i < vect1.length; i++) {
            sum += vect1[i] * vect2[i];
        }
        return sum;
    }

    public static double magnitude(double[] vect) {
        double sum = 0.0;
        for (int i = 0; i < vect.length; i++) {
            sum += vect[i] * vect[i];
        }
        return Math.sqrt(sum);
    }

    public static double euclidianDistance(double[] vect1, double[] vect2) {
        double dist = 0.0;
        for (int i = 0; i < vect1.length; i++) {
            dist += ((vect1[i] - vect2[i]) * (vect1[i] - vect2[i]));
        }
        return Math.sqrt(dist);
    }

    public static double neighorDistance(double[] home, double[] other) {
        double homeProd = VectorUtils.dotProduct(home, home);
        double vectProd = VectorUtils.dotProduct(home, other);
        return vectProd / homeProd;
    }


    public static Histogram projectionPerformance(double[][] allVectors, double[] projection) {
        Histogram h = new Histogram(100);
        int vectIdx = 0;
        for (double[] vector: allVectors) {
            double neighDist = neighorDistance(projection, vector);
            h.record(neighDist, vectIdx);
            vectIdx++;
        }
        return h;
    }

    public static Histogram projectionPerformance(Set<Integer> activeIds, double[][] allVectors, double[] projection) {
        Histogram h = new Histogram(100);
        int vectIdx = 0;
        for (double[] vector: allVectors) {
            if (activeIds.contains(vectIdx)) {
                double neighDist = neighorDistance(projection, vector);
                h.record(neighDist, vectIdx);
            }
            vectIdx++;
        }
        return h;
    }

    public static double[] normalize(double[] vect) {
        double mag = magnitude(vect);
        for (int i = 0; i < vect.length; i++) {
            vect[i] = vect[i] / mag;
        }
        return vect;
    }

}
