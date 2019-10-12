package com.o19s.hangry.helpers;

public class LabeledVector {
    public int label;
    public double[] vector;
    public LabeledVector(int label, double[] vector) {
        this.label = label;
        this.vector = vector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass())
            return false;
        LabeledVector otherLabeledVect = (LabeledVector)obj;
        return otherLabeledVect.label == this.label;
    }

    @Override
    public int hashCode() {
        return this.label;
    }
}
