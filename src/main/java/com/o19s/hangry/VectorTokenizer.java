package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;

import java.io.IOException;

public class VectorTokenizer extends TokenStream {

    // a set of random projections
    // emiting a number of tokens
    // projections are either pre-seeded or
    // query projections have to match index projections
    // is there a way to get an id for a shard for a seed?

    double[] vector;
    RandomProjectionTree[] randomProjections;
    int currProj;
    int depth;


    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    static public int FULL_DEPTH = -1;


    VectorTokenizer(double[] vector, RandomProjectionTree[] randomProjections, int depth) {
        this.vector = vector;
        this.currProj = 0;
        this.randomProjections = randomProjections;
        this.depth = depth;
    }


    VectorTokenizer(double[] vector, RandomProjectionTree[] randomProjections) {
        this(vector, randomProjections, FULL_DEPTH);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (currProj < randomProjections.length) {
            // probably too much copying, this needs to be tighter
            termAtt.append(randomProjections[currProj].encodeProjection(vector));
            currProj++;
            return true;
        }
        return false;

    }

    @Override
    public final void end() throws IOException {
        this.currProj = 0;
    }


}
