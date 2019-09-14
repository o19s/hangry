package com.o19s.hangry;

import org.junit.Test;

import static org.junit.Assert.*;

public class RandomProjectionsTest {

    @Test
    public void testSimilarity() {
        RandomProjections rp = new RandomProjections(100, 3);

        double vect1[] = {0.001, 0.001, 0.001};
        double vect2[] = {1.0, 1.0, 1.0};

        double vect3[] = {0.002, 0.002, 0.002};


        double nearSim1 = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect3, vect1);

        assertEquals(nearSim1, nearSim, 0.01);

    }

    @Test
    public void testDissimilarity() {
        RandomProjections rp = new RandomProjections(100, 3);

        double vect1[] = {-1.0, -1.0, -1.0};
        double vect2[] = {1.0, 1.0, 1.0};
        double vect3[] = {1.0, 1.0, 0.9};


        double farSim = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect2, vect3);

        assertTrue(nearSim > farSim);

    }

}