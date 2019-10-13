package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
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
        return this.buildQuery(field, queryVector, VectorTokenizer.FULL_DEPTH);
    }

    public Query buildQuery(String field, double[] queryVector, int depth) throws IOException {
        // TODO check depth not larger than
        VectorTokenizer tokenizer = new VectorTokenizer(queryVector, this.trees, depth);

        BooleanQuery.Builder bqb = new BooleanQuery.Builder();

        // collect every query token, turn into prefix query
        TermToBytesRefAttribute termAtt = tokenizer.getAttribute(TermToBytesRefAttribute.class);
        while (tokenizer.incrementToken()) {
            Query pq = new PrefixQuery(new Term(field, termAtt.getBytesRef()));
            bqb.add(pq, BooleanClause.Occur.SHOULD);
        }
        tokenizer.end();

        BooleanQuery bq = bqb.build();
        bq.setMaxClauseCount(10000);
        return bq;
    }
}
