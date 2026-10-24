# Core contracts and language mechanics

These challenges turn rules that are often discussed abstractly in interviews
into executable contracts. Use the prediction drills as an early diagnostic;
attempt the generic and collection contracts after their introductory exercises.

| # | Challenge | Focus |
|---:|---|---|
| 83 | [Generic Variance and PECS](GenericVariancePecs.java) | bounds, wildcards, producer/consumer APIs |
| 84 | [Erasure-safe Generic Varargs](ErasureSafeVarargs.java) | erasure, reifiable types, heap-pollution avoidance |
| 85 | [Equality and Ordering Contract](EqualityOrderingContract.java) | `equals`, `hashCode`, natural ordering |
| 86 | [Immutable Record Snapshot](ImmutableRecordSnapshot.java) | records, sealed types, defensive copies, stable keys |
| 87 | [Collection Choice Contracts](CollectionChoiceContracts.java) | ordering, uniqueness, queue semantics |
| 120 | [Language Semantics](LanguageSemantics.java) | predict reference copying, strings/boxing, initialization, hiding/dispatch, finally |
| 121 | [Compiler Contracts](CompilerContracts.java) | predict target typing, capture, variance, access, overrides, definite assignment |
| 133 | [Functional Behavior Predictions](FunctionalBehaviorPredictions.java) | predict receiver timing, source binding, stream reuse, reduction laws, future recovery boundaries |

For each challenge, explain both the successful implementation and the nearest
plausible implementation that violates the contract.

The three prediction drills require a full JDK 21+. Fill the prediction maps
before running. Compiler snippets compile in isolation into temporary output,
which is removed even when a prediction fails. Record the reason for every
prediction and change one snippet for a later blind rep.

Functional Behavior Predictions uses explicit two-part reductions to expose
invalid identities and non-associative operations. A real parallel stream's
partition count is unspecified; do not memorize a particular split or thread order.
