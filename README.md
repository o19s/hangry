# hangry
Hangry is a demo of Approximate Nearest Neighbors in Lucene search, using random projections to create terms that can be used to perform ANN using an inverted index.

## How it works

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

If we take LOTS of random vectors (rp1, rp2, rp3), and repeat this, for close vectors, we expect the sign to be the same more often. That is, the close vectors are much more often on teh same side than far apart vectors. So for close vectors v1 and v2, when we take dot products between rp1 rp2 and rp3, we get the resulting signs

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






