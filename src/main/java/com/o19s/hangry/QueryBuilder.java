package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostAttribute;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    // Insert Oprah Gif
    // TREEEEES!
    RandomProjectionTree[] trees;

    public QueryBuilder(RandomProjectionTree[] trees) {
        this.trees = trees;
    }


    public Query buildQuery(String field, double[] queryVector) throws IOException {
        return this.buildQuery(field, queryVector, VectorTokenizer.FULL_DEPTH, 1);
    }

    public Query buildQuery(String field, double[] queryVector, int depth) throws IOException {
        return this.buildQuery(field, queryVector, depth, 1);
    }

    public Query buildQuery(String field, double[] queryVector, int depth, int minTreeMatch) throws IOException {
        // TODO check depth not larger than
        VectorTokenizer tokenizer = new VectorTokenizer(queryVector, this.trees, depth);

        BooleanQuery.Builder bqb = new BooleanQuery.Builder();

        // collect every query token, turn into prefix query
        TermToBytesRefAttribute termAtt = tokenizer.getAttribute(TermToBytesRefAttribute.class);
        BoostAttribute boostAtt = tokenizer.getAttribute(BoostAttribute.class);

        while (tokenizer.incrementToken()) {
            Query pq = new PrefixQuery(new Term(field, termAtt.getBytesRef()));
            float boost = boostAtt.getBoost();
            pq = new BoostQuery(pq, boost);
            bqb.add(pq, BooleanClause.Occur.SHOULD);
        }
        tokenizer.end();

        bqb.setMinimumNumberShouldMatch(minTreeMatch);
        BooleanQuery bq = bqb.build();
        return bq;
    }
}
