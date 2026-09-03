# Core Java lab roadmap

This roadmap owns the next additions to the runnable Java practice track. The
existing numbered challenges remain the active solving path; these items are
planned practice ground, not completion claims.

## Next build order

### 1. Core contracts and language mechanics

Add a `10-core-contracts` section with self-testing challenges for:

- generic bounds, wildcards, PECS, type erasure, and heap-pollution traps;
- `equals`, `hashCode`, `Comparable`, and comparator consistency;
- mutable keys in hash collections and identity versus value semantics;
- immutability, defensive copies, records, and sealed hierarchies;
- collection choice and behavioral contracts, not just collection syntax.

### 2. I/O and date/time

Add an `11-io-time` section with self-testing challenges for:

- `Path` and `Files`, buffered text and binary I/O, and explicit charsets;
- try-with-resources, ownership, suppressed exceptions, and large-file
  streaming;
- `Instant`, `LocalDate`, `ZonedDateTime`, `Duration`, `Period`, and `Clock`;
- daylight-saving transitions and locale-independent parsing/formatting.

### 3. Practical concurrency and asynchronous composition

Keep the eight demonstrations in `07-threads` as the predict-run-explain
foundation. Add a `12-concurrency-async` section of TODO-based challenges that
require the learner to implement:

- interruption-aware tasks and cooperative cancellation;
- `ExecutorService` shutdown, bounded queues, rejection, and timeouts;
- `CompletableFuture` composition, fan-out/fan-in, exception recovery, and
  cancellation;
- atomics, `ConcurrentHashMap`, locks/conditions, semaphores, and safe
  publication;
- deterministic concurrency tests and virtual-thread-appropriate blocking
  work.

## Capstone: payment ledger

The payment ledger is a project, not a micro-lab. It should prove that the
language, concurrency, database, Spring, Kafka, testing, and observability
material can be combined without weakening financial invariants.

### Invariants

- Money uses `BigDecimal` with an explicit currency, scale, and rounding rule.
- Every transfer creates balanced, immutable debit and credit postings.
- Postings are append-only; corrections use reversal entries rather than
  rewriting financial history.
- The ledger is the source of truth; cached balances are projections that can
  be rebuilt and reconciled.
- Reusing an idempotency key with the same request returns the original result;
  reusing it with a different request is rejected.
- Balance changes and ledger postings commit atomically.
- Concurrent requests cannot overspend or lose an update.
- Transfer states have explicit legal transitions; failures never invent a
  successful state.
- Time comes from an injected `Clock`; audit events retain ordering evidence.
- Ambiguous downstream outcomes are recoverable through reconciliation.

### Delivery slices

1. **Domain core** — accounts, transfers, postings, invariants, and unit tests
   with no framework.
2. **Transactional persistence** — PostgreSQL migrations, JDBC or JPA,
   optimistic/conditional updates, and Testcontainers integration tests.
3. **Application boundary** — REST API, validation, idempotency, concurrent
   request tests, and stable error contracts.
4. **Reliable events** — transactional outbox, Kafka publication, idempotent
   consumer, retry/DLQ policy, and reconciliation job.
5. **Operational proof** — structured audit logs with redaction, metrics,
   tracing, failure injection, and a documented recovery runbook.

Avoid microservice sprawl, a UI, or invented throughput claims. One service
with strong invariants and evidence is a better interview artifact than many
thin services.

When implementation begins, give the capstone its own repository rather than
embedding a Spring application inside the single-file lab tree. That project
is the right place to practice Maven or Gradle, JUnit 5, integration testing,
Testcontainers, database migrations, and repeatable local infrastructure.

## Boundaries and later work

- Algorithms and data structures remain in `~/Programs/neetcode-150`; do not
  duplicate that tracker here.
- Java LLD and machine-coding exercises remain in `~/Programs/learning-lld`.
- SQL drills remain in `~/Programs/learning-sql`; the ledger consumes that
  knowledge through its persistence and transaction tests.
- JVM diagnosis remains a later hands-on extension: use deliberately broken
  programs to capture thread dumps, heap evidence, GC logs, `jcmd` output, and
  Java Flight Recorder recordings.
