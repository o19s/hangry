package com.o19s.hangry;

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
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class VectorFieldTest {

    @Test
    public void basicTest() throws IOException {

        RandomProjections rp = new RandomProjections((byte)5, (short)2, (byte)0);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory dir = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter iw = new IndexWriter(dir, config);

        Document doc = new Document();
        doc.add(new StringField("id", "1234", Field.Store.YES));

        double vect[] = {0.5,0.5};
        doc.add(new StringField("title", "test title", Field.Store.YES));
        doc.add(new StringField("vec1", rp.encodeProjection(vect), Field.Store.YES));

        iw.addDocument(doc);
        iw.commit();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        double queryVect[] = {0.5,0.5};
        Term prefix = new Term("vec1", rp.getQuery(queryVect, 5));

        Query q = new PrefixQuery(prefix);
        TopDocs docs = searcher.search(q, 10);
        assertEquals(docs.totalHits.value, 1);


    }
}
