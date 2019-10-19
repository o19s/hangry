package com.o19s.hangry;

import com.o19s.hangry.helpers.ExactNearestNeighbors;
import com.o19s.hangry.helpers.LabeledVector;
import com.o19s.hangry.randproj.EvenSplitsVectorFactory;
import com.o19s.hangry.randproj.RandomProjectionTree;
import com.o19s.hangry.randproj.RandomVectorFactory;
import com.o19s.hangry.randproj.SeededRandomVectorFactory;
import com.o19s.hangry.randproj.VectorUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static javax.management.Query.TIMES;

public class AnnPerfTest  {

    public static class TestRun extends VectorFieldTest {
        public RandomVectorFactory fitVectFactory;
        public HyperParameters hyperParams;
        public double[][] allVectors;
        public double[] queryVector;
        public double precision;

        private final static Logger RUN_LOGGER = Logger.getLogger(TestRun.class.getName());
        static {
            RUN_LOGGER.setLevel(Level.SEVERE);
        }
        private RandomProjectionTree[] rpTrees;

        TestRun(HyperParameters hyperParams, RandomVectorFactory fitVectFactory, double[][] allVectors, double[] queryVector) {
            this.allVectors = allVectors;
            this.fitVectFactory = fitVectFactory;
            this.allVectors = allVectors;
            this.queryVector = queryVector;
            this.hyperParams = hyperParams;

            this.fit();
        }

        public void fit() {
            RandomProjectionTree rp[] = new RandomProjectionTree[hyperParams.numTrees];
            for (int i = 0; i < rp.length; i++) {
                if (i % 10 == 0) {
                    RUN_LOGGER.fine(String.format("Built Proj %d\n", i));
                }
                rp[i] = new RandomProjectionTree(hyperParams.treeDepth, fitVectFactory);
            }
            this.rpTrees = rp;
        }

        public double runTest() throws IOException {
            double[][] allVectors = manyVectors(hyperParams.staticParams.numDocs, hyperParams.staticParams.numDims);

            RUN_LOGGER.info("Indexing");

            IndexWriter iw = createIndex();
            indexMany(allVectors, hyperParams.staticParams.numDims, this.rpTrees, iw);


            ExactNearestNeighbors nearestNeighbors = new ExactNearestNeighbors(allVectors);
            SortedSet<LabeledVector> nearestNeighbResults =  nearestNeighbors.query(queryVector);
            double farthestDistance = VectorUtils.euclidianDistance(nearestNeighbResults.last().vector, queryVector);

            RUN_LOGGER.info(String.format("Running Exact Nearest Neighbor (Farthest %f)\n", farthestDistance));

            Iterator<LabeledVector> iter = nearestNeighbResults.iterator();
            int i = 0;
            double lastEuclidean = 0;
            while (iter.hasNext()) {
                LabeledVector lv = iter.next();
                RUN_LOGGER.fine(String.format("%d  --  %f\n", lv.label, VectorUtils.euclidianDistance(queryVector, lv.vector)));
                if (i > hyperParams.staticParams.topNtoTest) {
                    lastEuclidean = VectorUtils.euclidianDistance(queryVector, lv.vector);
                    break;
                }
                i++;
            }


            QueryBuilder qb = new QueryBuilder(this.rpTrees);
            BooleanQuery.Builder bqb = new BooleanQuery.Builder();
            for (int queryDepth = 1; queryDepth <= this.hyperParams.treeDepth; queryDepth++) {
                int minMatch = 1;
                if (queryDepth == 1) {
                    minMatch = hyperParams.minShouldMatchDepth1;
                }
                Query q = qb.buildQuery("vector", queryVector,queryDepth,1);
                if (this.hyperParams.treeBuildingVectFactoryClass == EvenSplitsVectorFactory.class) {
                    double boost = Math.pow(2, this.hyperParams.treeDepth - queryDepth);
                    q = new BoostQuery(q, (float)boost);
                }
                bqb.add(q, BooleanClause.Occur.SHOULD);
            }

            Query q = bqb.build();

            IndexSearcher searcher = createSearcher(iw);
            TopDocs docs = searcher.search(q, hyperParams.staticParams.topNtoTest);

            int count = 0;
            for (i = 0; i < docs.scoreDocs.length; i++) {
                int docLabel = Integer.parseInt(searcher.doc(docs.scoreDocs[i].doc).get("title"));
                double euclideanDistance = VectorUtils.euclidianDistance(allVectors[docLabel], queryVector);
                if (euclideanDistance < lastEuclidean) {
                    count += 1;
                }
                RUN_LOGGER.fine(String.format("%d - %f - %s\n", docLabel, euclideanDistance, docs.scoreDocs[i].score));
            }
            RUN_LOGGER.fine(String.format("%d under thresh (prec %f)\n", count, ((double) count / (double) hyperParams.staticParams.topNtoTest)));
            precision =  ((double) count / (double) hyperParams.staticParams.topNtoTest);
            return precision;
        }

    }

    public static class StaticParams {
        public int numDocs;
        public int numDims;
        public int topNtoTest;
        public boolean unbalanced;

        public StaticParams(int numDocs, int numDims, int topNtoTest, boolean unbalanced) {
            this.numDims = numDims;
            this.numDocs = numDocs;
            this.topNtoTest = topNtoTest;
            this.unbalanced = unbalanced;
        }
    }


    public static class HyperParameters {
        // Not hyperparams
        public StaticParams staticParams;
        // Hyper params we would like to find an optimum for
        public int numTrees;
        public int treeDepth;
        public int minShouldMatchDepth1;
        Class<? extends RandomVectorFactory> treeBuildingVectFactoryClass;
        public double precision;

        HyperParameters(StaticParams staticParams, int numTrees, int treeDepth, int minShouldMatchDepth1, Class<? extends RandomVectorFactory> treeBuildingFactoryClass) {
            this.staticParams = staticParams;
            this.numTrees = numTrees;
            this.treeDepth = treeDepth;
            this.minShouldMatchDepth1 = minShouldMatchDepth1;
            this.treeBuildingVectFactoryClass = treeBuildingFactoryClass;
        }

        protected double[][] manyVectors(int howMany, int dims) {
            double[][] allVectors = new double[howMany][];
            SeededRandomVectorFactory seededRandomVectFact = new SeededRandomVectorFactory(0, dims);
            for (int i = 0; i < howMany; i++) {
                double[] vector = seededRandomVectFact.nextVector();
                if (staticParams.unbalanced) {
                    if (i % 2 == 0) {
                        for (int j = 0; j < vector.length; j++) {
                            // make it close-ish to j, but not exactly j
                            vector[j] = j + (vector[j] / 5.0);
                        }
                        vector = VectorUtils.normalize(vector);
                    }
                }
                allVectors[i] = vector;
            }
            return allVectors;
        }

        // Fit a RP Trees to the vector space,
        // return a test run with information to execute this test
        public TestRun createTestRun() {
            RandomVectorFactory seededFactory = new SeededRandomVectorFactory(0, staticParams.numDims);
            double[][] allVectors = manyVectors(staticParams.numDocs, staticParams.numDims);
            Random r = new Random();
            double[] queryVector = allVectors[r.nextInt(allVectors.length)];


            RandomVectorFactory fitVectFact;
            if (treeBuildingVectFactoryClass.equals(EvenSplitsVectorFactory.class)) {
                fitVectFact = new EvenSplitsVectorFactory(152, allVectors);
            } else {
                fitVectFact = new SeededRandomVectorFactory(152, this.staticParams.numDims+1);
            }


            return new TestRun(this, fitVectFact, allVectors, queryVector);

        }



    }


    private double runTest(HyperParameters params) throws IOException {

        double precSum = 0;
        int TIMES = 10;

        System.out.println("============NEW TEST!============");
        System.out.format("trees %d depth %d minshouldmatch %d treebuild %s\n",params.numTrees, params.treeDepth, params.minShouldMatchDepth1, params.treeBuildingVectFactoryClass);


        for (int run = 0; run < TIMES; run++) {


            TestRun testRun = params.createTestRun();
            double prec = testRun.runTest();
            precSum += prec;

        }
        precSum = precSum / TIMES;
        System.out.printf("Prec %f\n", precSum);
        params.precision = precSum;
        return precSum;
    }

    @Test
    @Ignore
    public void testApproximateNearestNeighborPerf() throws IOException {

        int NUM_DOCS = 10000;
        int DIMS = 32;
        int TOPN_TO_TEST = 100;
        boolean UNBALANCED = false;

        StaticParams staticTetsParams = new StaticParams(NUM_DOCS, DIMS, TOPN_TO_TEST, UNBALANCED);

        HyperParameters bestParams = null;
        double bestPrecision = 0;

        for (int numTrees = 128; numTrees <= 128; numTrees +=16) {
            for (int treeDepth = 8; treeDepth <= 8; treeDepth+= 4) {
                for (int minShouldMatchDepth1 = 1; minShouldMatchDepth1 < numTrees; minShouldMatchDepth1+= 12) {
                    for (int treeBuildFact = 1; treeBuildFact < 2; treeBuildFact++) {
                        Class<? extends RandomVectorFactory> treeBuildingFactoryClass = null;

                        switch (treeBuildFact) {
                            case 0:
                                treeBuildingFactoryClass = EvenSplitsVectorFactory.class;
                                break;
                            case 1:
                                treeBuildingFactoryClass = SeededRandomVectorFactory.class;
                        }


                        HyperParameters params = new HyperParameters(staticTetsParams, numTrees, treeDepth, minShouldMatchDepth1, treeBuildingFactoryClass);
                        double precision = runTest(params);
                        if (precision > bestPrecision) {
                            bestPrecision = precision;
                            bestParams = params;
                            System.out.printf("Best Run (%f) trees %d depth %d minshouldmatch %d treebuild %s\n",
                                    bestParams.precision,
                                    bestParams.numTrees,
                                    bestParams.treeDepth, bestParams.minShouldMatchDepth1, bestParams.treeBuildingVectFactoryClass.toString());
                        }
                    }
                }
            }
        }
        System.out.printf("Best Run (%f) trees %d depth %d minshouldmatch %d treebuild %s\n",
                bestParams.precision,
                bestParams.numTrees,
                bestParams.treeDepth, bestParams.minShouldMatchDepth1, bestParams.treeBuildingVectFactoryClass.toString());

    }


}
