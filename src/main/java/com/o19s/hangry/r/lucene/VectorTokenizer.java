package com.o19s.hangry.r.lucene;

import com.o19s.hangry.r.RandomProjectionForest;
import com.o19s.hangry.randproj.RandomProjectionTree;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.BoostAttribute;

import java.io.IOException;

public final class VectorTokenizer  extends TokenStream {

    // a set of random projections
    // emiting a number of tokens
    // projections are either pre-seeded or
    // query projections have to match index projections
    // is there a way to get an id for a shard for a seed?

    //double[] vector;
    //RandomProjectionForest forest;
    int currProj;
    //int depth;
    String[] hash;


    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    //private final BoostAttribute boostAtt = addAttribute(BoostAttribute.class);



    private final int depth;

    public VectorTokenizer(final double[] vector, final RandomProjectionForest forest) {
        this(vector, forest, forest.getNumHyperplanes());
    }

    public VectorTokenizer(final double[] vector, final RandomProjectionForest forest, final int depth) {
        // Need to be 255 or fewer random projection trees

        this.currProj = 0;
        this.hash = forest.project(vector);
        this.depth = depth;

    }


    @Override
    public boolean incrementToken() throws IOException {
        termAtt.setEmpty();
        if (currProj < hash.length) {
            // probably too much copying, this needs to be tighter
            termAtt.append(Integer.toString(currProj));
            termAtt.append('-');
            termAtt.append(hash[currProj].substring(0, depth));
            currProj++;
            return true;
        }
        return false;

    }

    @Override
    public final void end() throws IOException {
        this.currProj = 0;
        termAtt.setEmpty();

    }


}
