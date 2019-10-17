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

    public static double encodingDiff(RandomProjectionTree rpTree, double[] vect1, double vect2[]) {
        String path1 = rpTree.encodeProjection(vect1);
        String path2 = rpTree.encodeProjection(vect2);

        Assert.assertEquals(path1.length(), path2.length());

        int same = 0;
        for (int i = 0; i < path1.length(); i++) {
            if (path1.charAt(i) == path2.charAt(i)) {
                same += pow(2, (path1.length() - i));
            } else {
                same -= pow(2, (path1.length() - i));
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
                {0.1,0.1,0.95},
                {0.12,0.1,0.11},
                {0.11,0.11,0.09},
                {0.12,0.1,0.1},
                {0.12,0.11,0.1},
                {-0.5,-0.5,-0.5}, // -0.81,0.28,0.51
                {-0.4,-0.5,-0.5} // -0.81,0.28,0.51
        };

        RandomVectorFactory vectorFactory = new EvenSplitsVectorFactory(10, unbalancedVectorSpace);

        //RandomVectorFactory seededFactory = new SeededRandomVectorFactory(3,3);

        RandomProjectionTree rpTree = new RandomProjectionTree(6,  vectorFactory);

        double queryVector[] = unbalancedVectorSpace[0];
        ExactNearestNeighbors nearestNeighbors = new ExactNearestNeighbors(unbalancedVectorSpace);


        SortedSet<LabeledVector> sortedSet = nearestNeighbors.query(queryVector);

        double lastDiff = Double.MAX_VALUE;
        for (LabeledVector labeledVector: sortedSet) {
            double thisDiff = encodingDiff(rpTree, queryVector, labeledVector.vector);
            //Assert.assertTrue("This vector should be farther from the last one", thisDiff < lastDiff);
            lastDiff = thisDiff;
        }
    }

}

