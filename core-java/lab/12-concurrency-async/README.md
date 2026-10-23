# Practical concurrency and asynchronous composition

These TODO-based challenges follow the prediction stations in
`07-threads`. Their checks use latches, joins, and bounded waits instead of
timing guesses.

| # | Challenge | Focus |
|---:|---|---|
| 93 | [Cooperative Cancellation](CooperativeCancellation.java) | interruption propagation and cleanup |
| 94 | [Bounded Executor Lifecycle](BoundedExecutorLifecycle.java) | bounded queues, rejection, shutdown, timeout |
| 95 | [CompletableFuture Composition](CompletableFutureComposition.java) | dependent stages, fan-out/fan-in, recovery, cancellation |
| 96 | [Concurrent Metrics Publication](ConcurrentMetricsPublication.java) | `ConcurrentHashMap`, adders, atomic safe publication |
| 97 | [Condition-based Bounded Buffer](ConditionBoundedBuffer.java) | locks, conditions, interruptible waiting |
| 98 | [Semaphore Concurrency Gate](SemaphoreConcurrencyGate.java) | permits, limits, exception-safe release |
| 99 | [Virtual-thread Blocking Fan-out](VirtualThreadFanout.java) | thread-per-task blocking, deterministic coordination |
| 128 | [CompletableFuture Execution and Failure](CompletableFutureExecution.java) | explicit executor, observation versus recovery, cancellation versus interruption |

Every test must terminate. Keep blocking operations interruptible, restore or
propagate interruption where the contract requires it, and never replace a
coordination primitive with arbitrary sleeps.

The composition checks keep upstream futures pending and complete inputs in a
different order from their list positions. Returning a stage must not wait for
those inputs. The execution checks use a controlled executor to distinguish
scheduling from immediate evaluation, and a latch-controlled supplier to show
that cancelling a CompletableFuture does not interrupt its running work.

Metrics snapshots publish a complete immutable map atomically. Sampling active
LongAdders across keys is not a globally atomic point in time; exact totals
require a quiescent point, as established by joining writers in the checks.
