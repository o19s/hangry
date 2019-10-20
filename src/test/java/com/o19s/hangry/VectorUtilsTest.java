package com.o19s.hangry;

import com.o19s.hangry.randproj.VectorUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VectorUtilsTest {

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

    @Test
    public void testProjBetweenPositiveNumbers2() {
        double[] vect1 = {0.1, 0.0};
        double[] vect2 = {5.0, 1.0};
        double[] proj = {0.5, 0.5};
//        double[] projBetween = VectorUtils.projectionBetween(vect1, vect2, proj);
    }

    @Test
    public void testProjDoesntExistBetweenEqualVectors() {
        double[] vect1 = {0.1, 0.0};
        double[] vect2 = {0.1, 0.0};
        double[] proj = {0.5, 0.5};
        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);
        Assert.assertFalse(projExists);

        double[] vect3 = {0.2, 0.4};
        double[] vect4 = {0.1, 0.2};
        projExists = VectorUtils.projectionBetween(vect3, vect4, proj);
        Assert.assertFalse(projExists);

        double[] vect5 = {0.1, 0.1};
        double[] vect6 = {0.1, 0.1};
        projExists = VectorUtils.projectionBetween(vect5, vect6, proj);
        Assert.assertFalse(projExists);

    }

    @Test
    public void testProjDoesntExistBetweenZeroVectors() {
        double[] vect1 = {0.0, 0.0};
        double[] vect2 = {0.1, 0.0};
        double[] proj = {0.5, 0.5};
        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);
        Assert.assertFalse(projExists);

        double[] vect3 = {0.0, 0.0};
        double[] vect4 = {0.0, 0.0};
        projExists = VectorUtils.projectionBetween(vect3, vect4, proj);
        Assert.assertFalse(projExists);

    }

    @Test
    public void testDivBy0Split() {
        double[] vect1 = {0.0, 0.1};
        double[] vect2 = {0.1, 0.0};
        double[] proj = {0.5, 0.5};
        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);
        Assert.assertTrue(projExists);

        double dot1 = VectorUtils.dotProduct(vect1, proj);
        double dot2 = VectorUtils.dotProduct(vect2, proj);
        String projReport = String.format("Proj {%f,%f} doesn't split {%f,%f},{%f,%f}! %f,%f",
                proj[0], proj[1], vect1[0], vect1[1], vect2[0], vect2[1], dot1, dot2);
        if (dot1 <= 0) {
            Assert.assertTrue(projReport, dot2 > 0);
        } else {
            Assert.assertTrue(projReport, dot2 <= 0);
        }
    }

    @Test
    public void testAnotherSameVectorSplit() {
        double[] vect1 = {0.1, 0.3};
        double[] vect2 = {0.5, 1.5};
        double[] proj = {0.5, 0.5};
        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);
        Assert.assertFalse(projExists);
    }

    @Test
    public void testSameVectorSplit() {
        double[] vect1 = {0.1, 0.3};
        double[] vect2 = {0.7, 2.1};
        double[] proj = {0.5, 0.5};
        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);
        Assert.assertFalse(projExists);
    }

    @Test
    public void testProjBetweenPositiveNumbers() {
        double[] vect1 = {2.0,3.0};
        double[] vect2 = {5.0,1.0};

        for (int vect1_0  = 0; vect1_0 < 100; vect1_0++) {
            for (int vect1_1  = 0; vect1_1 < 100; vect1_1++) {
                for (int vect2_0  = 0; vect2_0 < 100; vect2_0++) {
                    for (int vect2_1 = 0; vect2_1 < 100; vect2_1++) {

                        vect1[0] = vect1_0 / 10.0;
                        vect1[1] = vect1_1 / 10.0;
                        vect2[0] = vect2_0 / 10.0;
                        vect2[1] = vect2_1 / 10.0;

                        double[] proj = {0.5, 0.5};
                        boolean projExists = VectorUtils.projectionBetween(vect1, vect2, proj);

                        if (projExists) {
                            double dot1 = VectorUtils.dotProduct(vect1, proj);
                            double dot2 = VectorUtils.dotProduct(vect2, proj);
                            String projReport = String.format("Proj {%f,%f} doesn't split {%f,%f},{%f,%f}! %f,%f",
                                    proj[0], proj[1], vect1[0], vect1[1], vect2[0], vect2[1], dot1, dot2);
                            if (dot1 >= 0) {
                                Assert.assertTrue(projReport, dot2 < 0);
                            } else {
                                Assert.assertTrue(projReport, dot2 >= 0);
                            }
                        }
                    }
                }
            }
        }
    }
}
