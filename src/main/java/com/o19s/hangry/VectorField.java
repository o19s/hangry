package com.o19s.hangry;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;

public class VectorField extends Field {

    private RandomProjections _projections;

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

    public VectorField(String name, byte numProjections, byte seed, double[] vect) {
        super(name, TYPE_STORED);

        _projections = new RandomProjections(numProjections, (short)vect.length, seed);
        String encodedProjections = _projections.encodeProjection(vect);

        setStringValue(encodedProjections);
    }


}
