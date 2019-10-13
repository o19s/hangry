package com.o19s.hangry;

import com.o19s.hangry.randproj.BestRandomVectorFactory;
import com.o19s.hangry.randproj.Histogram;
import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import com.o19s.hangry.randproj.VectorUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class RandomProjectionTreeTest {

    @Test
    public void testSimilarity() throws IOException {
        RandomVectorFactory factory = new SeededRandomVectorFactory(0,3);
        RandomProjectionTree rp = new RandomProjectionTree(100, factory);

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

    public void histogramReport(Histogram hist) {
        int above = 0, below = 0, neighbors = 0, fars = 0;
        Set<Integer> aboveIds = new HashSet<Integer>();
        Set<Integer> belowIds = new HashSet<Integer>();


        for (int quartile = 0; quartile < hist.hist.length; quartile++ ) {
            if (quartile / (double)hist.hist.length <= 0.05) {
                fars += hist.hist[quartile];
            }
            else if (quartile / (double)hist.hist.length >= 0.95) {
                neighbors += hist.hist[quartile];
            }
            else if (quartile / (double)hist.hist.length >= 0.50) {
                above += hist.hist[quartile];
                aboveIds.addAll(hist.ids[quartile]);
            }
            else {
                below += hist.hist[quartile];
                belowIds.addAll(hist.ids[quartile]);

            }
        }
        System.out.printf("Far %d Neig %d Abov %d(%s) Belo %d(%s)\n", fars, neighbors,
                            above, aboveIds.toString(),
                            below, belowIds.toString());
    }


    @Test
    public void testEncodedSimilarity() {

        double vect1[] = {0.5,0.5};
        double vect2[] = {0.4,0.5};
        double vect3[] = {0.4,-0.5};
        double vect4[] = {-0.4,-0.5};

        int identicals[] = new int[4];

        double allVect[][] = {vect1, vect2, vect3, vect4};

        RandomVectorFactory factory = new SeededRandomVectorFactory(979, 2);

        int treeDepth = 10;

        for (int i = 0; i < 10; i++) {
            Histogram hists[] = new Histogram[10];
            RandomProjectionTree rp = new RandomProjectionTree(treeDepth, factory);

            int currProj = 0;
            for (double[] proj: rp._projections) {
                hists[currProj] = VectorUtils.projectionPerformance(allVect, proj);
                histogramReport(hists[currProj]);
                currProj++;
            }

            String enc1 = rp.encodeProjection(vect1, treeDepth);
            String enc2 = rp.encodeProjection(vect2, treeDepth);
            String enc3 = rp.encodeProjection(vect3, treeDepth);
            String enc4 = rp.encodeProjection(vect4, treeDepth);

            for (int proj = 0; proj < treeDepth; proj++) {

                char enc1c = enc1.charAt(proj);
                char enc2c = enc2.charAt(proj);
                char enc3c = enc3.charAt(proj);
                char enc4c = enc4.charAt(proj);

                if (enc1c == enc1c) {
                    identicals[0]++;
                }
                if (enc2c == enc1c) {
                    identicals[1]++;
                }
                if (enc3c == enc1c) {
                    identicals[2]++;
                }
                if (enc4c == enc1c) {
                    identicals[3]++;
                }
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

        double vect1[] = {-1.0, -1.0, -1.0};
        double vect2[] = {1.0, 1.0, 1.0};
        double vect3[] = {1.0, 1.0, 0.9};


        double farSim = rp.similarity(vect1, vect2);
        double nearSim = rp.similarity(vect2, vect3);

        assertTrue(nearSim > farSim);

    }

}