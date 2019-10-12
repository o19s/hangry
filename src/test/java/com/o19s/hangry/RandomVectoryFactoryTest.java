package com.o19s.hangry;

import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import com.o19s.hangry.randproj.VectorUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RandomVectoryFactoryTest {

    @Test
    public void testGeneratesRand() {
        SeededRandomVectorFactory f = new SeededRandomVectorFactory((byte)12, (short)300);
        double[] vect = f.nextVector();
        assertEquals(vect.length, 300);
        for (int i = 0; i < vect.length - 1; i++) {
            assertNotEquals(vect[i], vect[i+1]);
        }
    }

    @Test
    public void testDotProduct() {
        double[] vect1 = {1.0, 1.0};
        double[] vect2 = {2.5, -0.4};

        // Dot Product = 1*2.5 + 1*-0.4 = 2.5 + -0.4 = 2.1
        assertEquals(VectorUtils.dotProduct(vect1, vect2), 2.1, 0.01);

        double[] vect3 = {12.95, -25.1};
        double[] vect4 = {-2512, 100.5};

        // Dot Product = 12.95*-2512 + -25.1+100.5 = -35052.95
        assertEquals(VectorUtils.dotProduct(vect3, vect4), -35052.95, 0.01);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDotProductThrows() {
        double[] vect1 = {1.0, 1.0, 1.0};
        double[] vect2 = {2.5, -0.4};
        VectorUtils.dotProduct(vect1, vect2);
    }

    @Test
    public void testDotProductUpToVect1Len() {
        double[] vect1 = {1.0, 1.0};
        double[] vect2 = {2.5, -0.4, 0.5};

        // Dot Product = 1*2.5 + 1*-0.4 = 2.5 + -0.4 = 2.1
        // 0.5 in vect2 is IGNORED
        assertEquals(VectorUtils.dotProduct(vect1, vect2), 2.1, 0.01);
    }

    @Test
    public void testNormalize() {
        double[] vect1 = {0.001, 0.001, 0.001};
        double[] vect2 = {1.0, 1.0, 1.0};

        VectorUtils.normalize(vect1);
        VectorUtils.normalize(vect2);

        Assert.assertArrayEquals(vect1, vect2, 0.01);
    }

    @Test
    public void testEuclideanDistance() {
        double[] vect1 = {2.0, 3.0, 4.0};
        double[] vect2 = {1.0, 1.0, 1.0}; // (2-1)^2 + (3-1)^2 + (4-1)^2 = 1 + 4 + 9
                                          // sqrt(1 + 4 + 9) = 3.7416

        double expectedEuclidDistance = 3.7416;
        double euclidDistance = VectorUtils.euclidianDistance(vect1, vect2);

        Assert.assertEquals(euclidDistance, expectedEuclidDistance,0.01);

    }
}