package com.o19s.hangry;

import java.util.Random;
import java.util.stream.DoubleStream;

public class VectorFactory {

    private Random _r;

    VectorFactory() {
        _r = new Random();

    }

    VectorFactory(int seed) {
        _r = new Random(seed);

    }


    public double[] random(int dims) {
        return _r.doubles(dims).toArray();
    }

    public double[] random(int dims, double min, double max) {
        return _r.doubles(dims, min, max).toArray();
    }

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

    public static double[] normalize(double[] vect) {
        double mag = magnitude(vect);
        for (int i = 0; i < vect.length; i++) {
            vect[i] = vect[i] / mag;
        }
        return vect;
    }


}
