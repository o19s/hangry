package com.o19s.hangry.randproj;

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
        for (double v : vect) {
            sum += v * v;
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

    public static double[] normalize(double[] vect) {
        double mag = magnitude(vect);
        for (int i = 0; i < vect.length; i++) {
            vect[i] = vect[i] / mag;
        }
        return vect;
    }

}
