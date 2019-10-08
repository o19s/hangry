package com.o19s.hangry;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RandomProjectionsTest {

    @Test
    public void testSimilarity() throws IOException {
        RandomProjections rp = new RandomProjections((byte)100, (short)3, (byte)0);

        // These are all really the same vector
        double vect1[] = {0.001, 0.001, 0.001};
        double vect2[] = {1.0, 1.0, 1.0};

        double vect3[] = {0.002, 0.002, 0.002};


        double nearSim1 = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect3, vect1);

        assertEquals(nearSim1, nearSim, 0.01);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, config);


    }

    @Test
    public void testDissimilarity() {
        RandomProjections rp = new RandomProjections((byte)100, (short)3, (byte)0);

        double vect1[] = {-1.0, -1.0, -1.0};
        double vect2[] = {1.0, 1.0, 1.0};
        double vect3[] = {1.0, 1.0, 0.9};


        double farSim = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect2, vect3);

        assertTrue(nearSim > farSim);

    }

}