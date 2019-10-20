package com.o19s.hangry.randproj;

import java.util.Set;

public class VectorUtils {

    // Performs a dot product up until the size of vect1 for speed
    // if vect2.length < vect1.length, you get a ArrayIndexOutOfBoundsException
    public static double dotProduct(double[] vect1, double[] vect2) {
        return dotProduct(vect1,vect2,vect1.length);
    }

    // Performs a dot product up until the size of vect1 for speed
    // if vect2.length < vect1.length, you get a ArrayIndexOutOfBoundsException
    public static double dotProduct(double[] vect1, double[] vect2, int upTo) {
        double sum = 0;
        for (int i = 0; i < upTo; i++) {
            sum += vect1[i] * vect2[i];
        }
        return sum;
    }

    // Performs a dot product up until the size of vect1 for speed
    // if vect2.length < vect1.length, you get a ArrayIndexOutOfBoundsException
    public static double projProduct(double[] vect1, double[] vect2) {
        double sum = 0;
        for (int i = 0; i < vect1.length; i++) {
            sum += vect1[i] * vect2[i+1];
        }
        return sum + vect2[0];
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
                double dotProd = dotProduct(projection, vector);
                double neighDist = neighorDistance(projection, vector);
                h.record(neighDist, vectIdx);
            }
            vectIdx++;
        }
        return h;
    }

    public static double cosineSimilarity(double[] vect1, double[] vect2) {
        double dot = dotProduct(vect1, vect2);
        double mag1 = magnitude(vect1);
        double mag2 = magnitude(vect2);

        return dot / (mag1*mag2);

    }

    public static boolean isVector(double[] vect) {
        double sum = 0.0;
        for (int i = 0; i < vect.length; i++) {
            sum += vect[i];
        }
        return (sum != 0.0);
    }

    public static boolean projectionBetween(double[] vect1, double[] vect2, double[] projSeed) {
        // vect1 and vect2 are d-dimensional vectors
        // Projection seed is a d dimensional vector, where the d-1th dimension will be filled in to make projSeed a
        //
        // We change projSeed to a vector with pN changed into a hyperplane that splits vect1 and vect2
        //
        // You can find a projection (p1...p2) that divides two vectors a and b by solving the inequalities
        //
        //    a1 * p1 + a2 * p2 ... + an * pn >= 0
        //    b1 * p1 + b2 * b2 ... + bn * pn < 0
        //
        //   an * pn  >= -(a1 * p1 + a2 * p2 ... + a(n-1) * p*(n-1))
        //
        //      if an > 0
        //          pn  >= -(a1 * p1 + a2 * p2 ... + a(n-1) * p*(n-1))
        //                 --------------------------------------------
        //                                     an
        //      else if an < 0 <- the sign flips the resulting inequality
        //          pn  <= -(a1 * p1 + a2 * p2 ... + a(n-1) * p*(n-1))
        //                 --------------------------------------------
        //                                     an
        //
        //
        //   bn * pn  <  -(b1 * p1 + b2 * p2 ... + b(n-1) * p*(n-1))
        //
        //
        //   solving for the inequaltiy, we can bound pn to a range of values that guarantee p is a seperating
        //   hyperplane between a and b
        //

        // Error checking, situations there is no projection
        // if either is all 0s, that's not a vector
        if (!isVector(vect1) || !isVector(vect2)) {
            return false;
        }
        if (cosineSimilarity(vect1, vect2) >= 1.0) {
            return false;
        } // We might still come through here due to floating point...

        double dot1 = dotProduct(projSeed, vect1,vect1.length-1);
        double dot2 = dotProduct(projSeed, vect2,vect1.length-1);

        double v1Last = vect1[vect1.length-1];
        double v2Last = vect2[vect1.length-1];

        // Edge cases that prevent divide by 0, where
        // we alsos need to avoid setting to Double.MIN_VALUE to
        // prevent underflow.
        // this should probably be calibrated to the number of dimensions
        if (v1Last == 0.0) {
            v1Last = 0.000001;
        }

        if (v2Last == 0.0) {
            v2Last = 0.000001;
        }

        if (v1Last >= 0 && v2Last >= 0) {
            double floor = -dot1 / v1Last;
            double ceiling = -dot2 / v2Last;

            projSeed[vect1.length-1] = (floor + ceiling) / 2.0;
        }

        return isProjectionBetween(vect1,vect2,projSeed) ;
    }

    public static boolean isProjectionBetween(double[] vect1, double[] vect2, double[] proj) {
        double dot1 = VectorUtils.dotProduct(vect1, proj);
        double dot2 = VectorUtils.dotProduct(vect2, proj);
        if (dot1 >= 0) { // >= is the 'up'
            return dot2 < 0; // < is 'down'
        } else {
            return dot2 >= 0;
        }
    }

    public static double[] normalize(double[] vect) {
        double mag = magnitude(vect);
        for (int i = 0; i < vect.length; i++) {
            vect[i] = vect[i] / mag;
        }
        return vect;
    }

}
