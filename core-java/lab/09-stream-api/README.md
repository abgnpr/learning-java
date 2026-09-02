# Stream API labs

This section contains 18 self-testing challenges for the Java Stream API. Work
through them in order: each group adds a new part of the stream mental model,
from building a lazy pipeline to designing reductions that remain correct in
parallel.

Every challenge is a standalone Java 21 source file. Run one from
`core-java/lab/`:

```bash
java 09-stream-api/FilterMapPipeline.java
```

Replace only the marked `TODO` seam. The acceptance checks describe observable
behavior, while each file's `Required focus` identifies the API you should
practice. A loop may produce the same answer, but it does not complete a Stream
API drill.

## Mental model

```text
source -> lazy intermediate operations -> terminal operation -> result
```

- A stream does not store elements and cannot be reused after a terminal
  operation.
- Intermediate operations are lazy; traversal begins only when a terminal
  operation requests values.
- Stateless, non-interfering functions are the safe default. Avoid shared
  mutation, especially in parallel pipelines.
- Encounter order matters for operations such as `findFirst`, `skip`, `limit`,
  `takeWhile`, and ordered collectors.
- Reduction functions must be associative, and their identity and combiner
  must obey the reduction contract.

## Learning path

| # | Challenge | Primary focus |
|---:|---|---|
| 65 | [Filter and Map Pipeline](FilterMapPipeline.java) | `filter`, `map`, `sorted`, `toList` |
| 66 | [Laziness and Short-Circuiting](LazyFirstMatch.java) | `peek`, `findFirst`, demand-driven traversal |
| 67 | [Distinct Score Page](DistinctScorePage.java) | `distinct`, `sorted`, `skip`, `limit` |
| 68 | [Flatten Nested Data](FlatMapWords.java) | `flatMap` |
| 69 | [One-to-Many Mapping](MapMultiRanges.java) | `mapMulti` |
| 70 | [Prefix Operations](TakeDropWhileReadings.java) | `dropWhile`, `takeWhile` |
| 71 | [Matching and Finding](MatchAndFindInventory.java) | `allMatch`, `anyMatch`, `findFirst` |
| 72 | [Optional Pipeline](OptionalEmailPipeline.java) | `Optional.stream`, `flatMap` |
| 73 | [Three-Argument Reduction](ReduceTransactions.java) | identity, accumulator, combiner |
| 74 | [Primitive Statistics](PrimitiveStreamStatistics.java) | `mapToInt`, `summaryStatistics` |
| 75 | [Joining Collector](JoiningCollector.java) | `Collectors.joining` |
| 76 | [Grouping Sales](GroupingSales.java) | `groupingBy`, downstream `reducing` |
| 77 | [Partitioning People](PartitionPeople.java) | `partitioningBy`, downstream `mapping` |
| 78 | [Merging Duplicate Keys](MergingVotes.java) | `toMap`, merge function, map supplier |
| 79 | [Teeing Collector](TeeingRange.java) | `teeing`, `minBy`, `maxBy` |
| 80 | [Custom Collector](CustomBracketCollector.java) | supplier, accumulator, combiner, finisher |
| 81 | [Infinite Stream Bounds](InfiniteStreamBounds.java) | `iterate`, `generate`, `limit` |
| 82 | [Parallel Word Frequency](ParallelWordFrequency.java) | parallel streams, concurrent collection |

## Review checkpoints

After completing the section, be able to explain:

1. Which operations are intermediate and which are terminal.
2. Why `findFirst` may inspect only part of its source.
3. When to choose `map`, `flatMap`, or `mapMulti`.
4. The empty-stream identities of `allMatch`, `anyMatch`, and reductions.
5. Why primitive streams avoid boxing and expose numerical terminals.
6. How downstream collectors compose grouping operations.
7. Why duplicate keys require a merge policy with `toMap`.
8. What makes a collector or reduction safe for parallel execution.
9. Why `parallelStream()` is not automatically faster.

Compile this section without writing class files into the repository:

```bash
stream_build=$(mktemp -d)
find 09-stream-api -name '*.java' -print0 \
  | xargs -0 javac --release 21 -Xlint:all -d "$stream_build"
```
