package com.o19s.hangry.randproj;

import java.util.Random;

public class SeededRandomVectorFactory implements RandomVectorFactory {
    // Random vectors regenerated (hopefully consistently!) from
    // an original seed. Good for testing & prototyping. Not sure
    // how I feel about this in prod with things like Java and the OS
    // being upgraded out from underneath us

    protected Random r;
    protected long seed;
    protected int dims;
    protected double lowerBounds;
    protected double upperBounds;


    public SeededRandomVectorFactory(long seed, int dims) {
        this(seed, dims, -1.0, 1.0);
    }

    public SeededRandomVectorFactory(long seed, int dims, double lowerBounds, double upperBounds) {
        this.r = new Random(seed);
        this.seed = seed;
        this.dims = dims;
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    public double[] random(double min, double max) {
        return VectorUtils.normalize(this.r.doubles(dims, min, max).toArray());
    }


    @Override
    public double[] nextVector() {
        return random(this.lowerBounds, this.upperBounds);
    }
}
