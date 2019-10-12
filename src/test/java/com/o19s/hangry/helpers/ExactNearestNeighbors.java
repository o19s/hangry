package com.o19s.hangry.helpers;

import com.o19s.hangry.randproj.VectorUtils;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExactNearestNeighbors {


    public static class LabeledVectorCompare implements Comparator<LabeledVector> {

        double[] queryVector;
        LabeledVectorCompare(double[] queryVector) {
            this.queryVector = queryVector;
        }

        @Override
        public int compare(LabeledVector o1, LabeledVector o2) {
            double distance1 = VectorUtils.euclidianDistance(queryVector, o1.vector);
            double distance2 = VectorUtils.euclidianDistance(queryVector, o2.vector);
            double difference =  distance1 - distance2;
            return (int)Math.signum(difference);

        }
    }

    public static SortedSet<LabeledVector> nearestNeighbors(double[][] vectors, double[] queryVector) {
        SortedSet<LabeledVector> sortedSet = new TreeSet<LabeledVector>(new LabeledVectorCompare(queryVector));
        for (int i = 0; i < vectors.length; i++) {
            boolean addedDoc = sortedSet.add(new LabeledVector(i, vectors[i]));
            assert(addedDoc);
        }
        return sortedSet;
    }
}
