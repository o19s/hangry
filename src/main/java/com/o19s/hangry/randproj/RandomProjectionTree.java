package com.o19s.hangry.randproj;

// TODO: abstract an interface, this
// prescribes random projections managed and recreated via a remembered
// seed, which is generally good enough for our purposes now, but we
// probably need more reliability to store and remember the seeds
public class RandomProjectionTree {
    // Assumes normalized vector inputs

    public double[][] projections;

    public RandomProjectionTree(int depth, RandomVectorFactory vectFactory) {
        this.projections = new double[depth][];

        vectFactory.reset();
        int trimTo = -1;
        for (int i = 0; i < depth; i++) {
            this.projections[i] = vectFactory.nextVector();
            if (this.projections[i] == null) {
                trimTo = i;
                break;
            }
        }

        // vector factory may return null, so we
        // trim out the nulls at the end
        if (trimTo >= 0 ) {
            double[][] proj = new double[trimTo][];
            for (int i = 0; i < trimTo; i++) {
                proj[i] = this.projections[i];
            }
            this.projections = proj;
        }
    }

    private static double sameSign(double val1, double val2) {
        return Math.signum(val1 * val2);
    }

    public int getDepth() {
        return projections.length;
    }


    public double similarity(double[] vect1, double[] vect2) {
        double same = 0;
        for (double[] projection : projections) {
            double dp1 = VectorUtils.dotProduct(vect1, projection);
            double dp2 = VectorUtils.dotProduct(vect2, projection);
            same += sameSign(dp1, dp2);

        }
        return same / (double) projections.length;
    }

    // How close does vector come to the neighborhood
    // of this projection?
    public double neighborhoodDistance(int projection, double[] vector) {
        // TODO do this before hand
        double selfProd = VectorUtils.dotProduct(projections[projection], projections[projection]);
        double vectProd = VectorUtils.dotProduct(vector, projections[projection]);
        return vectProd / selfProd;
    }
    public String encodeProjection(double[] vect) {
        return this.encodeProjection(vect, projections.length);
    }

    public float depthBoost(int depth) {
        return 1.0f;
    }

    public String encodeProjection(double[] vect, int depth) {

        StringBuilder s = new StringBuilder();

        int depthToUse = depth;
        if (depth > projections.length) {
            depthToUse = projections.length;
        }

        double same = 0;
        for (int i = 0; i < depthToUse; i++) {
//            double sign = Math.signum(VectorUtils.dotProduct(vect, _projections[i]));
            double neighborDistance = neighborhoodDistance(i, vect);

            assert(neighborDistance <= 1.0);
            assert(neighborDistance >= -1.0);

//            if (neighborDistance > 0.9) {
//                s.append('9');
//            }
//            else if (neighborDistance > 0.6) {
//                s.append('8');
//            }
//            else if (neighborDistance > 0.4) {
//                s.append('7');
//            }
//            else if (neighborDistance > 0.2) {
//                s.append('6');
//            }
//            else if (neighborDistance < -0.9) {
//                s.append('0');
//            }
//            else if (neighborDistance < -0.6) {
//                s.append('1');
//            }
//            else if (neighborDistance < -0.4) {
//                s.append('2');
//            }
//            else if (neighborDistance < -0.2) {
//                s.append('3');
//            }
//            else {
                // these are 'near-planar' as in close to the
                // hyperplane
                double sign = Math.signum(VectorUtils.dotProduct(VectorUtils.normalize(vect), projections[i]));
                if (sign >= 0) {
                    s.append('+');
                } else {
                    s.append('-');
                }
                //s.append('?');
//            }

//            if (sign > 0) {
//                s.append('1');
//            } else {
//                s.append('0');
//            }
        }
        return s.toString();
    }


}
