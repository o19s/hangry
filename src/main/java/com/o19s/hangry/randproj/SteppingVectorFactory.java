package com.o19s.hangry.randproj;

public class SteppingVectorFactory extends SeededRandomVectorFactory {
    double stepValue;
    int[][] histogram; // dimension, and step, containing count at each step
    int counter;

    public SteppingVectorFactory(int seed, int dims, double[] medians, double[] mins, double[] maxs) {
        super(seed, dims);
        this.counter = 0;
    }

    @Override
    public double[] nextVector() {
        double[] lastVector = super.nextVector();

        double[] rVal = new double[dims];
        for (int i = 0; i < dims; i++) {
            if (r.nextInt(dims/3) == 0 ) {
                rVal[i % dims] = r.nextDouble();
            }
        }
        this.counter++;
        return VectorUtils.normalize(rVal);

    }
}
