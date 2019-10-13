package com.o19s.hangry.randproj;

public interface RandomVectorFactory {

    double[] nextVector();

    default void reset() {};
}
