# Core Java interview practice

The target is reliable command: implement ordinary Java, predict its behavior,
diagnose a plausible mistake, and explain the tradeoff. A completion mark records
an exercise attempt. Readiness also requires a later attempt with changed inputs.

Use [core-java.md](../core-java.md) for spoken recall and [java-8.md](../java-8.md)
for the functional model. Choose representative drills below before spending time
on optional depth. Keep each starter's API focus even when a shortcut would pass
its output checks. Explain the nearest incorrect solution during review.

## Essential practice

| Order | Concepts | Drills | Evidence of command |
|---:|---|---|---|
| 1 | Language and compiler rules | [Language Semantics](10-core-contracts/LanguageSemantics.java), [Compiler Contracts](10-core-contracts/CompilerContracts.java) | Predict before running; justify each output or rejection. Include pass-by-value, initialization, dispatch, capture, access, and checked exceptions. |
| 2 | Object design | [Payment Routing](05-oop/PaymentRoutingContracts.java), [Immutable Snapshot](10-core-contracts/ImmutableRecordSnapshot.java) | Substitute a dependency, protect invariants, and explain composition and defensive copying. |
| 3 | Collections and keys | [Hash Keys](04-data-structures/HashKeyContract.java), [Equality and Ordering](10-core-contracts/EqualityOrderingContract.java), [Views and Iteration](04-data-structures/CollectionViewsAndIteration.java), [Map Updates](04-data-structures/MapUpdateContracts.java) | Choose a collection; predict mutation, null handling, equality, ordering, and callback invocation. |
| 4 | Generic APIs | [Variance and PECS](10-core-contracts/GenericVariancePecs.java), [Safe Varargs](10-core-contracts/ErasureSafeVarargs.java) | Recreate a useful generic signature, explain what callers may read/write, and distinguish erasure from loss of runtime class identity. |
| 5 | Functional Java | [Functional Composition](08-advanced/FunctionalCompositionContracts.java), [Optional Workflow](08-advanced/OptionalAccountWorkflow.java), [Default Resolution](08-advanced/DefaultMethodResolution.java) | Choose an interface by shape; trace callback order; avoid eager fallback; resolve all three default-method cases. |
| 6 | Streams and reports | [Laziness](09-stream-api/LazyFirstMatch.java), [Flattening](09-stream-api/FlatMapWords.java), [Grouping](09-stream-api/GroupingSales.java), [Duplicate Keys](09-stream-api/MergingVotes.java), [Comparator Composition](09-stream-api/MultiFieldInventorySort.java), [Reduction](09-stream-api/ReduceTransactions.java) | Build a report from a fresh dataset, handle empty/duplicate input, and explain source ownership, encounter order, and associativity. |
| 7 | Boundaries, money, time | [Exception Boundary](06-exceptions/ExceptionBoundaryContracts.java), [Resource Ownership](11-io-time/ResourceSuppressionOwnership.java), [Money](03-big-numbers/BigDecimalMoneyContracts.java), [Clock](11-io-time/ClockDrivenTime.java), [DST](11-io-time/ZonedDstScheduling.java) | Preserve causes/resources, calculate exact amounts, and choose timeline versus calendar types. |
| 8 | Concurrency and async | [Thread stations](07-threads/README.md), [Cancellation](12-concurrency-async/CooperativeCancellation.java), [Executor Lifecycle](12-concurrency-async/BoundedExecutorLifecycle.java), [Future Composition](12-concurrency-async/CompletableFutureComposition.java), [Future Execution](12-concurrency-async/CompletableFutureExecution.java), [Metrics Publication](12-concurrency-async/ConcurrentMetricsPublication.java) | Separate atomicity, visibility, and liveness; compose pending stages; explain executor choice and cancellation; bound waits and shut down work. |
| 9 | Integration | [Transaction Report](14-integration/TransactionReport.java) | Make parsing, logical keys, duplicate policy, exact aggregation, sorting, immutable output, and file ownership agree. |

## Guide coverage

For sorting policies, use [application comparator patterns](../comparator-patterns.md)
and the focused sequence [Multi-field Inventory](09-stream-api/MultiFieldInventorySort.java)
→ [Comparator Policy Catalog](09-stream-api/ComparatorPolicyCatalog.java)
→ [Ranked Results](09-stream-api/RankedResults.java). This adds null/direction
rules, business priorities, and order-preserving report output to the core rep.

| Reading | Practical coverage |
|---|---|
| Core §1 and §6: bedrock and traps | Language Semantics, Compiler Contracts, Payment Routing, Immutable Snapshot |
| Core §2: collections | Hash Keys, Equality and Ordering, Views and Iteration, Map Updates, collection-choice/LRU/navigation exercises |
| Core §3: exceptions | Compiler Contracts, Exception Boundary, Resource Ownership |
| Core §4; Java 8 §A–C | Functional Composition, Compiler Contracts, Lambda Predicates, comparator composition |
| Java 8 §D–E | Stream section; custom collector and parallel word frequency for deeper follow-ups |
| Java 8 §F–H | Optional Workflow, Default Resolution, Clock and DST |
| Java 8 §I | Both CompletableFuture labs and Cooperative Cancellation |
| Java 8 §J | Map Updates and Concurrent Metrics; short spoken/command reps for Base64, parameter names, and utility APIs |
| Core §5 and Java 8 JVM notes | Thread stations, practical concurrency; JVM memory/GC/loading recall in [threads-jvm.md](../threads-jvm.md) and command drills in [java-versions.md](../java-versions.md) |
| Core §7–9 | Separate [Spring](../../spring-boot/spring-boot-basics.md), [Kafka](../../kafka/kafka-basics-exercise.md), and [testing](../../spring-boot/spring-boot-testing-deep.md) tracks; SDLC vocabulary is spoken practice |

## Optional depth

The syntax/string warm-ups are useful when those skills are weak. LRU and
navigation are useful collection extensions. Custom collectors, explicit
conditions, semaphores, reflection/annotations, and version-specific features
are follow-up material after the essential concepts are comfortable. Serialization
and JVM collector names warrant a precise explanation; building a serialization
framework or tuning every collector is outside this path.

## Readiness check

For a representative exercise from each essential group:

1. State the contract and edge cases before coding.
2. Build without copying a previous implementation; keep acceptance checks intact.
3. Add an edge case and show which plausible bug it catches.
4. Explain the governing rule and one alternative's tradeoff aloud.
5. Revisit later with a changed requirement or dataset. A memorized answer does
   not satisfy this step.

For prediction exercises, record a rule beside every prediction before running.
After a miss, change the snippet and predict again. For concurrency, explain
which results are guaranteed and which depend on scheduling; a successful run
alone cannot establish correctness.

Run all new drills with a full JDK 21+. The three Java 25 exercises are optional
and have their own [run instructions](13-version-features/README.md).
