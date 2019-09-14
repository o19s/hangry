package com.o19s.hangry;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class VectorFactoryTest {

    @Test
    public void testGeneratesRand() {
        VectorFactory f = new VectorFactory(12);
        double vect[] = f.random(300);
        assertEquals(vect.length, 300);
        for (int i = 0; i < vect.length - 1; i++) {
            assertNotEquals(vect[i], vect[i+1]);

        }

    }

    @Test
    public void testGeneratesRandBounded() {
        VectorFactory f = new VectorFactory(12);
        double vect[] = f.random(300, -100.0, -80.0);
        assertEquals(vect.length, 300);
        for (int i = 0; i < vect.length - 1; i++) {
            assertNotEquals(vect[i], vect[i+1]);
            assertTrue(vect[i] >= -100.0);

            assertTrue(vect[i] <= -80.0);

        }
    }

    @Test
    public void testDotProduct() {
        double [] vect1 = {1.0, 1.0};
        double [] vect2 = {2.5, -0.4};

        // Dot Product = 1*2.5 + 1*-0.4 = 2.5 + -0.4 = 2.1
        assertEquals(VectorFactory.dotProduct(vect1, vect2), 2.1, 0.01);

        double [] vect3 = {12.95, -25.1};
        double [] vect4 = {-2512, 100.5};

        // Dot Product = 12.95*-2512 + -25.1+100.5 = -35052.95
        assertEquals(VectorFactory.dotProduct(vect3, vect4), -35052.95, 0.01);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDotProductThrows() {
        double [] vect1 = {1.0, 1.0, 1.0};
        double [] vect2 = {2.5, -0.4};
        VectorFactory.dotProduct(vect1, vect2);
    }

    @Test
    public void testDotProductUpToVect1Len() {
        double [] vect1 = {1.0, 1.0};
        double [] vect2 = {2.5, -0.4, 0.5};

        // Dot Product = 1*2.5 + 1*-0.4 = 2.5 + -0.4 = 2.1
        // 0.5 in vect2 is IGNORED
        assertEquals(VectorFactory.dotProduct(vect1, vect2), 2.1, 0.01);
    }

    @Test
    public void testNormalize() {
        double vect1[] = {0.001, 0.001, 0.001};
        double vect2[] = {1.0, 1.0, 1.0};

        VectorFactory.normalize(vect1);
        VectorFactory.normalize(vect2);

        Assert.assertArrayEquals(vect1, vect2, 0.01);
    }


}