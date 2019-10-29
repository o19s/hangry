package com.o19s.hangry.r.lucene;

import com.o19s.hangry.r.RandomProjectionForest;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostAttribute;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class QueryBuilder {

    // Insert Oprah Gif
    // TREEEEES!
    final RandomProjectionForest forest;

    public QueryBuilder(final RandomProjectionForest forest) {
        this.forest = forest;
    }


    public Query buildQuery(final String field, final double[] queryVector) throws IOException {
        return this.buildQuery(field, queryVector, forest.getNumHyperplanes());
    }

    public Query buildQuery(final String field, final double[] queryVector, final int depth) throws IOException {
        return this.buildQuery(field, queryVector, depth, forest.getNumTrees());
    }

    public Query buildQuery(final String field, final double[] queryVector, final int depth, final int treesShouldMatch)
            throws IOException {
        // TODO check depth not larger than
        final VectorTokenizer tokenizer = new VectorTokenizer(queryVector, forest, depth);

        BooleanQuery.Builder bqb = new BooleanQuery.Builder();

        // collect every query token, turn into prefix query
        TermToBytesRefAttribute termAtt = tokenizer.getAttribute(TermToBytesRefAttribute.class);
        //BoostAttribute boostAtt = tokenizer.getAttribute(BoostAttribute.class);

        while (tokenizer.incrementToken()) {
            Query pq = new PrefixQuery(new Term(field, termAtt.getBytesRef()));
            //float boost = boostAtt.getBoost();
            //pq = new BoostQuery(pq, boost);
            bqb.add(pq, BooleanClause.Occur.SHOULD);
        }
        tokenizer.end();

        bqb.setMinimumNumberShouldMatch(treesShouldMatch);
        BooleanQuery bq = bqb.build();
        return bq;
    }
}
