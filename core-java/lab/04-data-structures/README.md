# Data structures: advanced interview path

Complete challenges 28–42 first. The three `DS-A` extensions then move from
using a collection to reasoning about its behavioral contract—the part that
usually matters in a Java interview.

| Order | Challenge | Main idea | Target complexity |
|---:|---|---|---|
| DS-A1 | [Access-order LRU Cache](AccessOrderLruCache.java) | `LinkedHashMap`, access order, eviction hook | `get`/`put`: amortized O(1) |
| DS-A2 | [Navigable Time-series Lookup](NavigableTimeSeries.java) | `TreeMap`, `floorEntry`, `ceilingEntry` | build: O(n log n), lookup: O(log n) |
| DS-A3 | [Hash Key Contract](HashKeyContract.java) | `equals`/`hashCode`, logical keys, hash collections | lookup: average O(1) |

## Interview drill

After each implementation, be ready to explain:

1. Why this collection fits better than the nearest alternative.
2. Which operation changes iteration order, if any.
3. The average and worst-case cost of the important operations.
4. What breaks if a key is mutated after insertion.
5. Whether the implementation is thread-safe and how you would protect it.

These are Java collection exercises, not a second algorithms tracker. Problems
such as LFU cache, top-k frequency, trie design, and graph traversal remain in
the dedicated NeetCode practice repository.
