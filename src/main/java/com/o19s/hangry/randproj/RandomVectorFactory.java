package com.o19s.hangry.randproj;

// Build a new random vector for a vector space
// May be stateful trying to pick the next best split
// reset is used to return back to consider to a 'root'
public interface RandomVectorFactory {

    // return another vector, or
    // null if another vector cannot be generated
    // by this strategy, and the tree should stop
    // being generated
    public double[] nextVector();

    // Reset to build a new tree
    default void reset() {};
}
