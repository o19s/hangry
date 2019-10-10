package com.o19s.hangry;

import com.o19s.hangry.randproj.RandomProjectionTree;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class VectorField extends Field {

    private RandomProjectionTree _projections;

    /** Indexed, not tokenized, omits norms, indexes
     *  DOCS_ONLY, not stored. */
    public static final FieldType TYPE_NOT_STORED = new FieldType();

    /** Indexed, not tokenized, omits norms, indexes
     *  DOCS_ONLY, stored */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_NOT_STORED.setOmitNorms(true);
        TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS);
        TYPE_NOT_STORED.setTokenized(false);
        TYPE_NOT_STORED.freeze();

        TYPE_STORED.setOmitNorms(true);
        TYPE_STORED.setIndexOptions(IndexOptions.DOCS);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setTokenized(false);
        TYPE_STORED.freeze();
    }

    public VectorField(String name, double[] vect) {
        super(name, TYPE_STORED);

//        _projections = new RandomProjectionTree(100, (short)vect.length, );
//        String encodedProjections = _projections.encodeProjection(vect);

        setStringValue("");


    }

    // Return a token stream
    //   posn<dim,seed> 11011   2**16
    // - Construct a query
    //      T posn,
    //      11011
    //         posn <300,23>
    //         posn <300,42>
    // every token is it's own projection
    // every


}
