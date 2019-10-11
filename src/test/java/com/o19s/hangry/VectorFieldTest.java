package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class VectorFieldTest {

    private Document buildDoc(String title, double[] vector, RandomProjectionTree[] rp) {
        Document doc = new Document();
        doc.add(new StringField("title", title, Field.Store.YES));
        doc.add(new TextField("vector", new VectorTokenizer(vector, rp)));
        return doc;

    }

    @Test
    public void testExactMatchSingleProjTree() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        RandomProjectionTree rp[] = new RandomProjectionTree[1];
        rp[0] = new RandomProjectionTree(5, factory);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        Document doc = new Document();
        doc.add(new StringField("id", "1234", Field.Store.YES));

        double vect[] = {0.5,0.5};
        doc.add(new StringField("title", "test title", Field.Store.YES));
        doc.add(new TextField("vector", new VectorTokenizer(vect, rp)));

        iw.addDocument(doc);
        iw.commit();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryBuilder qb = new QueryBuilder(rp);

        // Test direct query for this, full depth of the tree, should exact match
        double queryVect[] = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 1);

        // Test opposite query for this, full depth, should inexact match
        double queryVect2[] = {-0.5,-0.5};
        Query q2 = qb.buildQuery("vector", queryVect2);
        TopDocs docs2 = searcher.search(q2, 10);
        assertEquals(docs2.totalHits.value, 0);
    }

    @Test
    public void testApproximateNearestNeighbor() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        RandomProjectionTree rp[] = new RandomProjectionTree[1];
        rp[0] = new RandomProjectionTree(5, factory);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        double vect1[] = {0.5,0.5};
        double vect2[] = {0.4,0.5};
        double vect3[] = {-0.4,0.5};

        iw.addDocument(buildDoc("doc1", vect1, rp));
        iw.addDocument(buildDoc("doc2", vect2, rp));
        iw.addDocument(buildDoc("doc3", vect3, rp));
        iw.commit();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryBuilder qb = new QueryBuilder(rp);

        // Test direct query for this, full depth, should exact match
        double queryVect[] = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect, 2);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 2);
    }

    @Test
    public void testApproximateNearestNeighborClosestScoredHigher() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0,2);

        // Create 10 random projected vectors
        RandomProjectionTree rp[] = new RandomProjectionTree[10];
        for (int i = 0; i < rp.length; i++) {
            rp[i] = new RandomProjectionTree(5, factory);
        }

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        double vect1[] = {0.5,0.5};
        double vect2[] = {0.4,0.5};
        double vect3[] = {-0.4,0.5};

        // Build 3 docs
        iw.addDocument(buildDoc("doc1", vect1, rp));
        iw.addDocument(buildDoc("doc2", vect2, rp));
        iw.addDocument(buildDoc("doc3", vect3, rp));
        iw.commit();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryBuilder qb = new QueryBuilder(rp);

        // Depth 2 out of 5 (depth of trees) should give higher recall, but with
        // closer matches for nearby vectors, gradually farther away
        double queryVect[] = {0.5,0.5};
        Query q = qb.buildQuery("vector", queryVect, 2);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 2);

        assertEquals(searcher.doc(docs.scoreDocs[0].doc).get("title"), "doc1");
        assertEquals(searcher.doc(docs.scoreDocs[1].doc).get("title"), "doc2");
    }


    public void indexMany(int howMany, int dims, RandomProjectionTree[] trees, IndexWriter iw) throws IOException {
        SeededRandomVectorFactory seededRandomVectFact = new SeededRandomVectorFactory(0,dims);
        for (int i = 0; i < howMany; i++) {
            double vectors[] = seededRandomVectFact.nextVector();

            iw.addDocument(buildDoc(Integer.toString(i), vectors, trees));

        }
        iw.commit();
    }

    @Test
    @Ignore
    public void testApproximateNearestNeighborPerf() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0, 300);

        // Create 10 random projected vectors
        RandomProjectionTree rp[] = new RandomProjectionTree[10];
        for (int i = 0; i < rp.length; i++) {
            rp[i] = new RandomProjectionTree(5, factory);
        }

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        long before = System.nanoTime();
        indexMany(100000, 300, rp, iw);
        long after = System.nanoTime();
        double tookMs = (double)(after - before) / 1000000.0;
        System.out.printf("%f", tookMs);
    }


    @Test
    @Ignore
    public void testApproximateNearestNeighborPerfProfiler() throws IOException {

        RandomVectorFactory factory = new SeededRandomVectorFactory(0, 300);

        // Create 10 random projected vectors
        RandomProjectionTree rp[] = new RandomProjectionTree[10];
        for (int i = 0; i < rp.length; i++) {
            rp[i] = new RandomProjectionTree(5, factory);
        }

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        long before = System.nanoTime();
        while (true) {
            indexMany(100000, 300, rp, iw);
        }
    }


}
