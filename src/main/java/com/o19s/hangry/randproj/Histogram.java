package com.o19s.hangry.randproj;

import java.util.ArrayList;
import java.util.HashSet;

public class Histogram {

    public int[] hist;
    public HashSet<Integer>[] ids; // TODO experiment with a bloom filter


    public Histogram(int histSize) {
        hist = new int[histSize];
        ids = new HashSet[histSize];
        for (int i = 0; i < hist.length; i++) {
            ids[i] = new HashSet<Integer>();
        }

    }

    public void record(double value, int id) {
        assert(value <= 1.0);
        assert(value >= -1.0);

        int histIdx = hist.length - 1;
        if (value < 1.0) {
            histIdx = (int)(hist.length * ((value + 1.0) / 2.0));
        }
        assert(histIdx < hist.length);
        hist[histIdx]++;
        ids[histIdx].add(id);
    }
}
