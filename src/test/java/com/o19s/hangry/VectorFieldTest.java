package com.o19s.hangry;

import com.o19s.hangry.helpers.ExactNearestNeighbors;
import com.o19s.hangry.helpers.LabeledVector;
import com.o19s.hangry.randproj.EvenSplitsVectorFactory;
import com.o19s.hangry.randproj.Histogram;
import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import com.o19s.hangry.randproj.VectorUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import static junit.framework.TestCase.assertEquals;

public class VectorFieldTest {

    private Document buildDoc(String title, double[] vector, RandomProjectionTree[] rp) {
        Document doc = new Document();
        doc.add(new StringField("title", title, Field.Store.YES));
        doc.add(new TextField("vector", new VectorTokenizer(vector, rp)));
        return doc;

    }

    private IndexWriter createIndex() throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        return new IndexWriter(dir, config);

    }

    private IndexSearcher createSearcher(IndexWriter iw) throws IOException {
        IndexReader ir = DirectoryReader.open(iw);
        IndexSearcher searcher = new IndexSearcher(ir);
        return searcher;
    }

    @Test
    public void testExactMatchSingleProjTree() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        RandomProjectionTree[] rp = new RandomProjectionTree[1];
        rp[0] = new RandomProjectionTree(5, factory);

        Document doc = new Document();
        doc.add(new StringField("id", "1234", Field.Store.YES));

        double[] vect = {0.5,0.5};
        doc.add(new StringField("title", "test title", Field.Store.YES));
        doc.add(new TextField("vector", new VectorTokenizer(vect, rp)));

        IndexWriter iw = createIndex();
        iw.addDocument(doc);
        iw.commit();
        IndexSearcher searcher = createSearcher(iw);

        QueryBuilder qb = new QueryBuilder(rp);

        // Test direct query for this, full depth of the tree, should exact match
        double[] queryVect = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 1);

        // Test opposite query for this, full depth, should inexact match
        double[] queryVect2 = {-0.5,-0.5};
        Query q2 = qb.buildQuery("vector", queryVect2);
        TopDocs docs2 = searcher.search(q2, 10);
        assertEquals(docs2.totalHits.value, 0);
    }

    @Test
    public void testApproximateNearestNeighbor() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        RandomProjectionTree[] rp = new RandomProjectionTree[1];
        rp[0] = new RandomProjectionTree(5, factory);

        IndexWriter iw = createIndex();

        double[] vect1 = {0.5, 0.5};
        double[] vect2 = {0.4, 0.5};
        double[] vect3 = {-0.4,0.5};

        iw.addDocument(buildDoc("doc1", vect1, rp));
        iw.addDocument(buildDoc("doc2", vect2, rp));
        iw.addDocument(buildDoc("doc3", vect3, rp));
        iw.commit();

        QueryBuilder qb = new QueryBuilder(rp);

        // Test direct query for this, full depth, should exact match
        double[] queryVect = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect, 2);
        IndexSearcher searcher = createSearcher(iw);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 2);
    }

    @Test
    public void testApproximateNearestNeighborClosestScoredHigher() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        // Create 10 random projected vectors
        RandomProjectionTree[] rp = new RandomProjectionTree[10];
        for (int i = 0; i < rp.length; i++) {
            rp[i] = new RandomProjectionTree(5, factory);
        }

        IndexWriter iw = createIndex();

        double[] vect1 = {0.5,0.5};
        double[] vect2 = {0.4,0.5};
        double[] vect3 = {0.4,-0.5};
        double[] vect4 = {-0.4,-0.5};

        // Build 3 docs
        iw.addDocument(buildDoc("doc1", vect1, rp));
        iw.addDocument(buildDoc("doc2", vect2, rp));
        iw.addDocument(buildDoc("doc3", vect3, rp));
        iw.addDocument(buildDoc("doc4", vect4, rp));

        iw.commit();

        QueryBuilder qb = new QueryBuilder(rp);

        // Depth 2 out of 5 (depth of trees) should give higher recall, but with
        // closer matches for nearby vectors, gradually farther away
        double[] queryVect = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect, 2);
        IndexSearcher searcher = createSearcher(iw);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 3);

        assertEquals(searcher.doc(docs.scoreDocs[0].doc).get("title"), "doc1");
        assertEquals(searcher.doc(docs.scoreDocs[1].doc).get("title"), "doc2");
        assertEquals(searcher.doc(docs.scoreDocs[2].doc).get("title"), "doc3");

    }

    public double[][] manyVectors(int howMany, int dims) {
        double[][] allVectors = new double[howMany][];
        SeededRandomVectorFactory seededRandomVectFact = new SeededRandomVectorFactory(0, dims);
        for (int i = 0; i < howMany; i++) {
            double[] vector = seededRandomVectFact.nextVector();
            allVectors[i] = vector;
        }
        return allVectors;
    }

    public void indexMany(double[][] vectors, int dims, RandomProjectionTree[] trees, IndexWriter iw) throws IOException {
        SeededRandomVectorFactory seededRandomVectFact = new SeededRandomVectorFactory(0,dims);
        for (int i = 0; i < vectors.length; i++) {
            iw.addDocument(buildDoc(Integer.toString(i), vectors[i], trees));

            if (i % 100 == 0) {
                System.out.printf("Indexed %d\n", i);
            }
        }
        iw.commit();
    }

    public void computeStats(double[][] vectors, double[] medians, double[] mins, double[] maxs) {

        //double[] hist = new double[20][]; // 20 quantiles of 0.1 for each dimension

        Histogram hist = new Histogram(200);

        for (int j = 0; j < vectors[0].length; j++) {
            maxs[j] = Double.MIN_VALUE;
            mins[j] = Double.MAX_VALUE;

        }

        for (double[] vector : vectors) {
            for (int dim = 0; dim < vector.length; dim++) {
                if (dim == 0) {
                    hist.record(vector[dim], dim);
                }
                if (vector[dim] > maxs[dim]) {
                    maxs[dim] = vector[dim];
                }
                if (vector[dim] < mins[dim]) {
                    mins[dim] = vector[dim];
                }
            }
        }
    }



    @Test
    @Ignore
    public void testApproximateNearestNeighborPerf() throws IOException {

        // Test params
        int DIMENSIONS = 300;
        int TOP_N_TO_TEST = 100;
        int NUM_PROJ_TREES = 1024;
        int PROJ_TREE_DEPTH = 12;
        int MIN_TREE_MATCH = 1;
        int QUERY0_PROJ_TREE_DEPTH = 1; // least precise
        int QUERY1_PROJ_TREE_DEPTH = 2;
        int QUERY2_PROJ_TREE_DEPTH = 3; // most precise

        int NUM_DOCS = 10000;
        int TIMES = 10;

        double precSum = 0;

        for (int run = 0; run < TIMES; run++) {


            SeededRandomVectorFactory seededFactory = new SeededRandomVectorFactory(System.currentTimeMillis(), DIMENSIONS);

            // Create 10 random projected vectors

            double[][] allVectors = manyVectors(NUM_DOCS, DIMENSIONS);

            RandomVectorFactory bestFactory = new EvenSplitsVectorFactory(System.currentTimeMillis(), allVectors);

            double[] mins = new double[DIMENSIONS];
            double[] maxs = new double[DIMENSIONS];
            double[] medians = new double[DIMENSIONS];

            RandomProjectionTree rp[] = new RandomProjectionTree[NUM_PROJ_TREES];
            for (int i = 0; i < rp.length; i++) {
                rp[i] = new RandomProjectionTree(PROJ_TREE_DEPTH, seededFactory);
            }

            System.out.println("Indexing");

            IndexWriter iw = createIndex();
            indexMany(allVectors, DIMENSIONS, rp, iw);

            double[] queryVector = seededFactory.nextVector();

            ExactNearestNeighbors nearestNeighbors = new ExactNearestNeighbors(allVectors);
            SortedSet<LabeledVector> nearestNeighbResults =  nearestNeighbors.query(queryVector);
            double farthestDistance = VectorUtils.euclidianDistance(nearestNeighbResults.last().vector, queryVector);

            System.out.printf("Running Exact Nearest Neighbor (Farthest %f)\n", farthestDistance);

            Iterator<LabeledVector> iter = nearestNeighbResults.iterator();
            int i = 0;
            double lastEuclidean = 0;
            while (iter.hasNext()) {
                LabeledVector lv = iter.next();
                System.out.printf("%d  --  %f\n", lv.label, VectorUtils.euclidianDistance(queryVector, lv.vector));
                if (i > TOP_N_TO_TEST) {
                    lastEuclidean = VectorUtils.euclidianDistance(queryVector, lv.vector);
                    break;
                }
                i++;
            }

            System.out.println();
            System.out.println();

            QueryBuilder qb = new QueryBuilder(rp);
            Query q0 = qb.buildQuery("vector", queryVector, QUERY0_PROJ_TREE_DEPTH, MIN_TREE_MATCH);
            Query q1 = qb.buildQuery("vector", queryVector, QUERY1_PROJ_TREE_DEPTH);
            Query q2 = qb.buildQuery("vector", queryVector, QUERY2_PROJ_TREE_DEPTH);

            BooleanQuery.Builder bqb = new BooleanQuery.Builder();
            bqb.add(q0, BooleanClause.Occur.SHOULD);
            bqb.add(q1, BooleanClause.Occur.SHOULD);
            bqb.add(q2, BooleanClause.Occur.SHOULD);

            Query q = bqb.build();

            IndexSearcher searcher = createSearcher(iw);
            TopDocs docs = searcher.search(q, TOP_N_TO_TEST);

            int count = 0;
            for (i = 0; i < docs.scoreDocs.length; i++) {
                int docLabel = Integer.parseInt(searcher.doc(docs.scoreDocs[i].doc).get("title"));
                double euclideanDistance = VectorUtils.euclidianDistance(allVectors[docLabel], queryVector);
                if (euclideanDistance < lastEuclidean) {
                    count += 1;
                }
                System.out.printf("%d - %f - %s\n", docLabel, euclideanDistance, docs.scoreDocs[i].score);
            }
            System.out.printf("%d under thresh (prec %f)\n", count, ((double) count / (double) TOP_N_TO_TEST));
            precSum +=  ((double) count / (double) TOP_N_TO_TEST);
        }
        System.out.println("====================================");
        System.out.printf("Prec %f\n", precSum / TIMES);

    }


    @Test
    @Ignore
    public void testApproximateNearestNeighborPerfProfiler() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0, 300);

        // Create 10 random projected vectors
        RandomProjectionTree[] rp = new RandomProjectionTree[100];
        for (int i = 0; i < rp.length; i++) {
            rp[i] = new RandomProjectionTree(5, factory);
        }

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        while (true) {
            double[][] allVectors = manyVectors(100000, 300);
            indexMany(allVectors, 300, rp, iw);
        }
    }
}
