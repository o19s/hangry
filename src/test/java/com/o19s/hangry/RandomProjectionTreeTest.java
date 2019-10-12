package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RandomProjectionTreeTest {

    @Test
    public void testSimilarity() throws IOException {
        RandomVectorFactory factory = new SeededRandomVectorFactory(0,3);
        RandomProjectionTree rp = new RandomProjectionTree(100, factory);

        // These are all really the same vector
        double[] vect1 = {0.001, 0.001, 0.001};
        double[] vect2 = {1.0, 1.0, 1.0};

        double[] vect3 = {0.002, 0.002, 0.002};


        double nearSim1 = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect3, vect1);

        assertEquals(nearSim1, nearSim, 0.01);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, config);
    }


    @Test
    public void testEncodedSimilarity() {
        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        double[] vect1 = {0.5,0.5};
        double[] vect2 = {0.4,0.5};
        double[] vect3 = {0.4,-0.5};
        double[] vect4 = {-0.4,-0.5};

        int[] identicals = new int[4];

        for (int i = 0; i < 10; i++) {
            RandomProjectionTree rp = new RandomProjectionTree(10, factory);

            String proj1 = rp.encodeProjection(vect1, 2);
            String proj2 = rp.encodeProjection(vect2, 2);
            String proj3 = rp.encodeProjection(vect3, 2);
            String proj4 = rp.encodeProjection(vect4, 2);

            if (proj1.equals(proj1)) {
                identicals[0]++;
            }
            if (proj2.equals(proj1)) {
                identicals[1]++;
            }
            if (proj3.equals(proj1)) {
                identicals[2]++;
            }
            if (proj4.equals(proj1)) {
                identicals[3]++;
            }
        }

        Assert.assertTrue(identicals[1] < identicals[0]);
        Assert.assertTrue(identicals[2] < identicals[1]);
        Assert.assertTrue(identicals[3] < identicals[2]);
    }

    @Test
    public void testDissimilarity() {
        RandomVectorFactory factory = new SeededRandomVectorFactory(0,3);
        RandomProjectionTree rp = new RandomProjectionTree(100, factory);

        double[] vect1 = {-1.0, -1.0, -1.0};
        double[] vect2 = {1.0, 1.0, 1.0};
        double[] vect3 = {1.0, 1.0, 0.9};

        double farSim = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect2, vect3);

        assertTrue(nearSim > farSim);
    }
}