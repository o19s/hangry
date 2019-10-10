# hangry
Hangry is a demo of Approximate Nearest Neighbors in Lucene search, using random projections to create terms that can be used to perform ANN using an inverted index.

## How it works

A random projection is a randomly generated vector into a vector space. A random projection *tree* is multiple random vectors, with some notion of hierarchy.

Let's say we want to see if v1 and v2 are approximate nearest neighbors. And we have generated a random vector, `rp`.

Recall that

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


But for a vector far from v1, say v3, we might get:

|   |  rp1 | rp2  | rp3  |
|---|------|------|------|
| v1|   1  |  -1  |  1   |
| v2|   -1 |  1   |  -1  |


In this codebase, we have built a tokenizer that takes a vector and turns it into terms based on the result of a number of random projections. We encode a single character in that term for the result of each term. This builds on Lucene's strength in string processing. We index a 1 character when the vector has a dot product > 0 for a random projection and a 0 when the vector has a dot product <= 0






