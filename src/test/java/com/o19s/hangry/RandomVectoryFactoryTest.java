package com.o19s.hangry;

import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import com.o19s.hangry.randproj.VectorUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RandomVectoryFactoryTest {

    @Test
    public void testGeneratesRand() {
        SeededRandomVectorFactory f = new SeededRandomVectorFactory((byte)12, (short)300);
        double[] vect = f.nextVector();
        assertEquals(vect.length, 300);
        for (int i = 0; i < vect.length - 1; i++) {
            assertNotEquals(vect[i], vect[i+1]);
        }
    }


}