# hangry
Hangry is a demo of Approximate Nearest Neighbors in Lucene search, using random projections to create terms that can be used to perform ANN using an inverted index with no extra data structures.

**This is experimental code**, and a work in progress. Though we welcome your testing and input to make a less of a work in progress! For a "demo" see [this test](https://github.com/o19s/hangry/blob/master/src/test/java/com/o19s/hangry/VectorFieldTest.java#L114)

## WE NEED YOU to support Vector Search efforts in Lucene 

Please support these efforts so we can have good vector search in Lucene / Solr

- [Lucene JIRA](https://issues.apache.org/jira/browse/LUCENE-9004) by Michael Sokolov to implement [Hierarchical Navigable Small World graphs](https://www.semanticscholar.org/paper/Efficient-and-robust-approximate-nearest-neighbor-Malkov-Yashunin/699a2e3b653c69aff5cf7a9923793b974f8ca164) for Approximate Nearest Neighbors
- [Solr 12890](https://issues.apache.org/jira/browse/SOLR-12890) with ideas from Trey Grainger to implement Solr vector scoring functions

## How Hangry works

A random projection is a randomly generated vector into a vector space. A random projection *tree* is multiple random vectors, with some notion of hierarchy.

Let's say we want to see if v1 `[0.4, 0.5]` and v2 `[0.5,0.5]` are approximate nearest neighbors. And we have generated a randomly generated vector, rp `[-0.242,0.712]`.

Recall that for two vectors

```
v1 dot rp
```

and 

```
v2 doc rp
```

will have the same sign (+1) or (-1) if they are both on the same side of rp.

If we take LOTS of random vectors (rp1, rp2, rp3), and repeat this, for close vectors, we expect the sign to be the same more often. That is, the close vectors are much more often on the same side than far apart vectors. So for close vectors v1 and v2, when we take dot products between rp1 rp2 and rp3, we get the resulting signs

|   |  rp1 | rp2  | rp3  |
|---|------|------|------|
| v1|   1  |  -1  |  1   |
| v2|   1  |  -1  |  1   |


But for a vector far from v1, say v3 `[-0.5,-0.5]`, we might get:

|   |  rp1 | rp2  | rp3  |
|---|------|------|------|
| v1|   1  |  -1  |  1   |
| v3|   -1 |  1   |  -1  |


## Tokenizing Vectors

In this codebase, we have built a tokenizer that takes a vector and turns it into terms based on the result of a number of random projections. We encode a single character in that term for the result of each dot product w/ a random projection. This builds on Lucene's strength in string processing and efficient string storage. We index a 1 character when the vector has a dot product > 0 for a random projection and a 0 when the vector has a dot product <= 0. 

From the above example, our vectors would be represented as terms:

```
v1: 101
v2: 101
v3: 010
```

Notice that if a query vector came in that looked closer to `v1` it would look more like the `v1` "bitmask". It might be `100` still pretty close!

## Forest of Random Projection Trees for Precision / Recall Tradeoffs

It's actually a better situation if we have many random projection trees, where our vectors can be analyzed by multiple "angles" so to speak. In that context, v1 might come out as `101` in one set of projections, but `011` in another set of projections. The neighbor, v2, might get a similar treatment, being `101` in the first projection tree, and `010` in the other. Let's say there's v3, which is close to v1, but yet farther away than v2. It's projection might be `111` and `010`

This is actually really useful, we can use this, and Lucene's fast Prefix scoring to score closer matches higher in relevance. We can also use prefix queries to decide how to limit the result set of matches.

If q=v1, then the query is for `101` and `011`. If we search for these two terms exactly (101 OR 011), we would only get exact or nearly exact and immediately adjacent vectors to v1. BUT what happens if we

1. Change OR to AND
2. Change the term match `101` to a prefix query `10*`

So we end up with a query that is `10* OR 01*`

Notice we have opened up the recall a lot. Further, the relevance scoring here will work to promote vectors that are exact matches to the top of the results. Followed by vectors that only match one clause. 

Vectors that don't match the criteria won't appear in the results.

## Code Walkthru

We recommend examining the [unit test]() for now until a demo can be created, but here's a quick code walkthrough

Create a factory for random vectors, built from a consistent seed. Then build 10 trees, each of depth 5 (so you'd get tokens like `11001` and `11111` etc)

```
    RandomVectorFactory factory = new SeededRandomVectorFactory(/*seed*/0,/*dimensions*/2);

    // Create 10 random projected vectors
    RandomProjectionTree rp[] = new RandomProjectionTree[10];
    for (int i = 0; i < rp.length; i++) {
        rp[i] = new RandomProjectionTree(/*depth*/5, factory);
    }
```

Index some vectors!
```
StandardAnalyzer analyzer = new StandardAnalyzer();
Directory dir = new RAMDirectory();
IndexWriterConfig config = new IndexWriterConfig(analyzer);

IndexWriter iw = new IndexWriter(dir, config);

double vect1[] = {0.5,0.5};
double vect2[] = {0.4,0.5};
double vect3[] = {-0.4,0.5};

// Build 3 docs
iw.addDocument(buildDoc("doc1", vect1, rp));
iw.addDocument(buildDoc("doc2", vect2, rp));
iw.addDocument(buildDoc("doc3", vect3, rp));
iw.commit();
````

Most of that was boilerplate, but `buildDoc` is where the indexing magic happens

```
private Document buildDoc(String title, double[] vector, RandomProjectionTree[] rp) {
        Document doc = new Document();
        doc.add(new StringField("title", title, Field.Store.YES));
        doc.add(new TextField("vector", new VectorTokenizer(vector, rp)));
        return doc;

    }
```

Notice above, the use of `VectorTokenizer` which is a token stream, that takes a vector, a series of random projection trees, and outputs the `11010` type projection terms.

There is a special QueryBuilder instance that uses VectorTokenizer to build the needed queries, and this is exercised in this unit test code:

```
QueryBuilder qb = new QueryBuilder(rp);

// Test direct query for this, full depth of the tree, should exact match
double queryVect[] = {0.5,0.5};
Query q = qb.buildQuery("vector", queryVect, 2);
TopDocs docs = searcher.search(q, 10);
```

Based on the passed in depth, a prefix query of tree depth 2 (making queries like `10* OR ...`) is constructed. In this case, this opens the recall up a lot. This of course is a tunable parameter for precision/recall tradeoff.

### Help Needed

Anyway, this is what we've been working on sofar on the side. There's lots of work to do to build this out for Solr or Elasticsearch or instigate parallel efforts to ensure that the community has first class vector support.

Special Thanks to Rene Kriegler and Manoj Bharawaj for input, inspiration, and feedback








