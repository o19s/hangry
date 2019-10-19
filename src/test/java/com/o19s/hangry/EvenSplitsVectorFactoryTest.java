package com.o19s.hangry;

import com.o19s.hangry.helpers.ExactNearestNeighbors;
import com.o19s.hangry.helpers.LabeledVector;
import com.o19s.hangry.randproj.EvenSplitsVectorFactory;
import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.SortedSet;

import static org.apache.commons.math3.util.ArithmeticUtils.pow;

public class EvenSplitsVectorFactoryTest {

    public static double encodingDiff(RandomProjectionTree rpTree, double[] vect1, double vect2[], boolean usePowers) {
        String path1 = rpTree.encodeProjection(vect1);
        String path2 = rpTree.encodeProjection(vect2);

        Assert.assertEquals(path1.length(), path2.length());

        int same = 0;
        for (int i = 0; i < path1.length(); i++) {
            double score = 1;
            if (usePowers) {
                score = pow(2, (path1.length() - i));
            }

            if (path1.charAt(i) == path2.charAt(i)) {
                same += score;
            } else {
                same -= score;
            }
        }
        double diff =  ((double) same) / path1.length();
        //System.out.println(path1);
        System.out.println(path2);
        System.out.println(Double.toString(diff));
//        System.out.println("======================");
        return ((double) same) / path1.length();

    }

    @Test
    public void testUnbalancedVectorSpace() {
        double [][] unbalancedVectorSpace = {
                {0.1,0.1,0.95,0.5},
                {0.12,0.1,0.11,0.3},
                {0.11,0.11,0.09,0.1},
                {0.12,0.1,0.1, 0.05},
                {0.12,0.11,0.1, 0.10},
                {-0.5,-0.5,-0.5, -0.5},
                {-0.4,-0.5,-0.5, -0.2}
        };

        for (int seed = 0; seed < 50; seed++) {

            RandomVectorFactory evenSplitVectorFactory = new EvenSplitsVectorFactory(seed, unbalancedVectorSpace);
            RandomVectorFactory seededFactory = new SeededRandomVectorFactory(seed, unbalancedVectorSpace[0].length);

            RandomProjectionTree rpTreeEven = new RandomProjectionTree(6, evenSplitVectorFactory);
            RandomProjectionTree rpTreeRegular = new RandomProjectionTree(6, seededFactory);

            double queryVector[] = unbalancedVectorSpace[0];
            ExactNearestNeighbors nearestNeighbors = new ExactNearestNeighbors(unbalancedVectorSpace);

            SortedSet<LabeledVector> sortedSet = nearestNeighbors.query(queryVector);

            System.out.println("");
            System.out.println("Even Split");
            for (LabeledVector labeledVector : sortedSet) {
                double evenDiff = encodingDiff(rpTreeEven, queryVector, labeledVector.vector, true);
            }

            System.out.println("");
            System.out.println("Regular Split");
            for (LabeledVector labeledVector : sortedSet) {
                double regularDiff = encodingDiff(rpTreeRegular, queryVector, labeledVector.vector, false);
                //Assert.assertTrue("This vector should be same or farther from the last one", thisDiff <= lastDiff);
                //lastDiff = thisDiff;
            }
            System.out.println("");
            System.out.println("");

        }
    }

}

