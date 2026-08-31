# Spring Transactions — Senior Backend Anti-Fumble Kit

For a backend engineer who knows `@Transactional` syntax but must reason
about **which physical resource is atomic**, **why a transaction rolls back
after an exception was caught**, and **what happens when a payment workflow
crosses a database, Kafka, and another service**. The goal is to predict the
outcome under failure and contention, not recite propagation enum names.

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/configure · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Attempt every prompt aloud before opening its `<details>` solution. The
answer card near the end is for rehearsal after the exercises, not a
substitute for retrieval practice.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §8 → §9 → §10 → §11**
>
> Resource boundary · proxy mechanics · logical/physical scopes ·
> propagation · rollback · isolation/contention · attributes/managers ·
> remote calls · database/Kafka consistency · tests · production failures.
>
> A senior transaction answer names four things: **atomic resource**,
> **boundary**, **failure point**, and **recovery/idempotency strategy**.
> “Put `@Transactional` on it” answers none of them.

> **Version line.** Examples use imperative Spring Framework 6/7 and
> Spring Boot 3/4 terminology. Boot 2.7 normally uses Framework 5.3, but
> the proxy, propagation, resource, rollback-only, and savepoint models are
> the same. Framework 6.2+ can globally change annotation rollback defaults
> with `@EnableTransactionManagement(rollbackOn = ALL_EXCEPTIONS)`; older
> estates normally retain the runtime-exception default unless each method
> declares rules. Imperative transactions are generally thread-bound;
> reactive transactions use Reactor context and belong in a separate kit.

---

## 0. The one picture — scopes around one or more resources · 🎯 CORE PATH

```text
caller
  │
  ▼
Spring AOP transaction proxy                 logical scope A
  │  PlatformTransactionManager.begin()
  ▼
service method
  ├─ repository A                            logical scope B (REQUIRED)
  ├─ repository B                            logical scope C (REQUIRED)
  └─ may mark rollback-only
  │
  ▼
thread-bound resource holder
  ├─ EntityManager / JDBC Connection
  └─ one physical database transaction
  │
  ├─ flush: send SQL; transaction remains open
  ├─ commit: database makes work durable/visible per isolation
  └─ rollback: database discards this physical transaction

HTTP service / Kafka broker / second database
  └─ separate resource: not made atomic by the database transaction above
```

- [ ] **0.1** 💭 Logical transaction scope versus physical transaction:
  define both and explain how three `REQUIRED` methods can create three
  logical scopes over one database transaction.
- [ ] **0.2** 💭 What exactly can a normal `JpaTransactionManager` or
  `DataSourceTransactionManager` commit atomically? Does the same annotation
  automatically include an HTTP call, Kafka send, and second database?
- [ ] **0.3** 🔮 A method returns normally, then commit fails because a
  deferred constraint is violated. Was “the business method succeeded”?
- [ ] **0.4** 💭 ACID is a property of what boundary? Why is saying “the
  microservice operation is ACID” incomplete?

<details><summary>Solutions 0</summary>

- 0.1 A **logical scope** is Spring's transactional policy around one
  advised invocation: propagation, rollback rules and rollback-only choice.
  A **physical transaction** is the actual resource transaction, such as one
  database connection transaction. With `REQUIRED`, the outer service and
  inner repository scopes normally participate in the same physical
  transaction even though each advised method has its own logical scope.
- 0.2 A local JPA/JDBC manager controls the resource it was configured for:
  normally one `EntityManagerFactory`/`DataSource` and its database
  transaction. An HTTP peer, Kafka broker, and unrelated datasource have
  independent failure and commit boundaries. They require their own local
  transactions plus coordination, or a compatible distributed-transaction
  mechanism; sharing the annotation text does not merge them.
- 0.3 No durable success exists yet. Returning from application code only
  begins the completion phase. Flush/commit may still reveal constraints,
  optimistic conflicts, deadlocks, connection loss, or an already-set
  rollback-only marker. Report success only after the required commit has
  completed; design retries and responses for commit-time failures.
- 0.4 ACID describes a **particular transaction manager/resource system and
  boundary**. A database transaction can be ACID for its rows while the
  end-to-end payment spans multiple separately committed systems and is only
  eventually consistent. Name the local atomic unit and the cross-boundary
  protocol instead of borrowing the database's guarantee for the whole flow.

</details>

---

## 1. Boundary, proxy & resource mechanics ⭐⭐ · 🎯 CORE PATH

- [ ] **1.1** 💭 Trace `@Transactional` mechanically from external caller
  through proxy, interceptor, transaction manager, target method and
  completion.
- [ ] **1.2** 🐛 `transfer()` calls `this.writeLedger()` and only
  `writeLedger()` is annotated. Why is there no new advised boundary?
- [ ] **1.3** 💭 Where should the usual transaction live when one use case
  changes an account, ledger and transfer through three repositories?
- [ ] **1.4** 🔮 Does the proxy necessarily borrow a JDBC connection at the
  first bytecode instruction of the method? When can resource acquisition be
  lazy?
- [ ] **1.5** 💭 How do `JdbcTemplate` and a JPA repository obtain the same
  transaction-bound connection when configured against the same datasource?
- [ ] **1.6** 🐛 A transactional method starts `CompletableFuture.runAsync`
  and the task writes to the database. Does the original transaction follow
  the task?
- [ ] **1.7** 💭 Flush versus commit versus transaction completion: state
  what each means and which failures can appear at each point.
- [ ] **1.8** 🏭 What resources can remain occupied while a transaction is
  open even when the application thread is waiting on the network?

<details><summary>Solutions 1</summary>

- 1.1 An external call reaches an AOP proxy. The transaction interceptor
  resolves metadata, chooses a `PlatformTransactionManager`, obtains or joins
  a transaction, invokes the target, then asks the manager to commit or roll
  back according to outcome and rollback status. The annotation is metadata;
  the proxy/interceptor and manager perform the work.
- 1.2 `this.writeLedger()` targets the same object directly and never crosses
  the proxy, so its annotation is not independently evaluated. Put the
  transaction on the externally called use-case method or move the inner
  operation to another injected bean. See the proxy visibility/self-call
  drills in [Spring Boot basics §8](spring-boot-basics.md).
- 1.3 Put it on the **service/facade use-case boundary** that owns the atomic
  business invariant. The repositories should participate in that one unit;
  a controller should not own persistence lifetime, and three independent
  repository commits cannot protect a cross-repository invariant.
- 1.4 Not necessarily. The manager establishes transactional context at the
  boundary, but a connection/provider resource may be acquired lazily when
  the first data operation needs it, depending on manager, pool and datasource
  proxies. Do not assume “method entered” and “connection borrowed” are the
  same timestamp; measure pool acquisition and transaction duration.
- 1.5 Spring's data integrations use transaction synchronization and
  resource utilities to bind/reuse the resource for the current thread.
  `JdbcTemplate` and `JpaTransactionManager` can participate through the same
  datasource when configured consistently. Directly calling arbitrary
  `dataSource.getConnection()` in legacy code may bypass that coordination;
  prefer Spring-aware access APIs.
- 1.6 No. Imperative transaction state is normally thread-bound. A common
  pool task runs on another thread with no inherited transaction and may
  commit independently. Wait for computation before the transaction, or put
  a deliberate transactional method around the work executed on the worker.
  Passing a transaction across arbitrary asynchronous work is not a safe fix.
- 1.7 **Flush** synchronizes pending ORM changes to SQL but can still roll
  back. **Commit** asks the database/resource to finalize the transaction and
  can expose deferred constraints, conflicts, deadlocks or I/O failure.
  **Completion** also runs synchronization callbacks and cleanup; a secondary
  coordinated resource can fail after a primary resource committed. Do not
  collapse these phases into “method returned.”
- 1.8 A pooled connection, database locks, MVCC snapshots/versions,
  persistence-context memory, transaction-log/undo pressure and possibly
  coordinated producer/session resources. Long waits increase contention,
  pool starvation, deadlock exposure and recovery scope even when CPU usage
  is near zero.

</details>

---

## 2. `REQUIRED`, rollback-only & `UnexpectedRollbackException` ⭐⭐⭐ · 🎯 CORE PATH

```text
outer REQUIRED ─────────────────────────────────────────┐
                                                       │
  inner REQUIRED ─ marks the shared transaction         │
                   rollback-only ───────────────────────┤ same physical tx
                                                       │
outer catches exception and returns normally            │
outer asks to commit ──▶ rollback instead ──▶ UnexpectedRollbackException
```

- [ ] **2.1** 💭 Why does every `REQUIRED` method have a logical scope if
  all of them share one physical transaction?
- [ ] **2.2** 🔮 Inner `REQUIRED` throws a runtime exception through its
  proxy; outer catches it and returns normally. Predict final outcome.
- [ ] **2.3** 💭 Why is `UnexpectedRollbackException` useful rather than a
  framework annoyance to suppress?
- [ ] **2.4** 🐛 A team catches `DataIntegrityViolationException` and then
  performs more JPA writes in the same transaction. Why is “continue after a
  database error” unsafe?
- [ ] **2.5** 🔮 Inner `REQUIRED` declares a different isolation level and
  timeout from the active outer transaction. Which settings normally win?
- [ ] **2.6** 🛠 What manager flag can reject incompatible isolation or
  read-only declarations when joining an existing transaction?
- [ ] **2.7** 💭 Is there a supported general-purpose “clear rollback-only
  and commit anyway” operation? What boundary should change instead?
- [ ] **2.8** 🏭 An audit record must survive a failed transfer. Why does
  placing both writes in the same `REQUIRED` transaction contradict that
  requirement?

<details><summary>Solutions 2</summary>

- 2.1 Each advised invocation needs its own policy decision and can vote for
  rollback. Spring maps those logical scopes onto the existing physical
  transaction. This lets the inner scope mark the shared transaction
  rollback-only without pretending it owns an independent database commit.
- 2.2 The inner interceptor marks the shared physical transaction
  rollback-only. Catching the exception prevents it from leaving the outer
  method, but does not erase that marker. When the outer scope requests
  commit, Spring rolls back and throws `UnexpectedRollbackException`.
- 2.3 The outer caller believed it requested a commit. Returning normally
  would falsely report durability even though an inner participant vetoed
  it. The exception makes the actual rollback visible at the boundary where
  success would otherwise have been reported.
- 2.4 A persistence provider or Spring interceptor may already have marked
  the transaction rollback-only, and the persistence context can contain
  state that no longer matches the database after a failed SQL operation.
  Stop the unit, roll it back, then retry/recover in a fresh transaction if
  the operation is safe. An exception catch is control flow, not database
  recovery.
- 2.5 The active outer physical transaction's characteristics normally win;
  a joining `REQUIRED` scope does not restart it with new isolation, timeout
  or read-only characteristics. Put those attributes on the boundary that
  starts the transaction or use a genuinely independent transaction where
  that is semantically correct.
- 2.6 Enable `validateExistingTransaction` on the applicable
  `AbstractPlatformTransactionManager`. Non-lenient validation rejects an
  incompatible isolation level and read-write participation in a read-only
  outer transaction instead of silently accepting the outer settings.
- 2.7 No normal application fix is “un-vote” the shared transaction after a
  participant or resource declared it invalid. Let it roll back. Move
  recoverable work to a well-defined independent transaction, or restructure
  validation so expected business rejection happens before the transaction
  becomes invalid.
- 2.8 `REQUIRED` makes both writes one all-or-nothing database unit, so a
  transfer rollback also removes its audit row. A separate
  `REQUIRES_NEW` audit service can commit independently, but costs another
  connection and has its own failure modes. For reliable integration history,
  model an append-only state/event in the same successful business boundary
  or design an explicit failure-audit channel rather than assuming one
  propagation value solves every audit requirement.

</details>

---

## 3. Propagation as resource behavior ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 💭 Give one precise line for `REQUIRED`, `REQUIRES_NEW`,
  `NESTED`, `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, and `NEVER`.
- [ ] **3.2** 🔮 Outer `REQUIRED` holds connection A and calls an external
  bean using `REQUIRES_NEW`. How many simultaneous database connections can
  that thread need?
- [ ] **3.3** 🏭 A pool has 20 connections and 20 request threads enter an
  outer transaction, then all request `REQUIRES_NEW`. Explain the deadlock-
  like starvation.
- [ ] **3.4** 🐛 `REQUIRES_NEW` is placed on a private helper/self-invoked
  method. Does it suspend the outer transaction?
- [ ] **3.5** 💭 `NESTED` versus `REQUIRES_NEW`: compare physical resource,
  rollback mechanism, locks and outer/inner outcomes.
- [ ] **3.6** 🐛 A JPA service assumes `NESTED` gives a fully independent
  nested persistence context. Why is that non-portable and usually false?
- [ ] **3.7** 💭 When are `MANDATORY`, `NEVER`, and `NOT_SUPPORTED` useful as
  executable architectural assertions?
- [ ] **3.8** 🔮 `SUPPORTS` performs two JDBC calls with no outer transaction.
  Does it promise one atomic unit?
- [ ] **3.9** 🏭 Should every log/audit/notification call use
  `REQUIRES_NEW`? Challenge that design.

<details><summary>Solutions 3</summary>

- 3.1 `REQUIRED`: join or create. `REQUIRES_NEW`: suspend any current scope
  and create an independent physical transaction. `NESTED`: use a savepoint
  inside one physical transaction when supported; otherwise create like
  `REQUIRED` when none exists. `SUPPORTS`: join if present, otherwise no
  transaction. `NOT_SUPPORTED`: run without one, suspending an existing
  scope. `MANDATORY`: require one or fail. `NEVER`: require none or fail.
- 3.2 Normally **two**: the outer transaction keeps A bound while the inner
  independent transaction obtains B. Deeper/concurrent inner scopes can need
  more. Pool sizing must account for simultaneous held outer and requested
  inner resources, not just request-thread count.
- 3.3 Every connection is held by an outer transaction. Every thread waits
  for a second connection, but none can finish/release its outer connection
  until the inner call proceeds. Timeouts eventually break the cycle. Avoid
  the nesting or size/admit work so at least the inner demand is satisfiable;
  Spring's reference explicitly warns pools must exceed concurrent threads
  by at least one for a single nested `REQUIRES_NEW` pattern.
- 3.4 No. Propagation metadata is evaluated only when a call crosses the
  transaction proxy. The helper runs in the existing outer transaction.
  Move it to an injected bean or use `TransactionTemplate` when a compact,
  explicit independent block is genuinely required.
- 3.5 `NESTED` uses **one physical transaction/connection** and a savepoint;
  inner rollback can return to the savepoint, but outer rollback still loses
  everything and locks often remain until outer completion. `REQUIRES_NEW`
  uses another physical transaction/resource, can commit independently and
  releases its locks at inner completion, but consumes another connection and
  cannot see uncommitted outer changes under normal isolation.
- 3.6 Spring's portable nested behavior is primarily JDBC savepoints.
  `DataSourceTransactionManager` supports them when the driver does.
  `JpaTransactionManager` defaults nested support off because JPA itself does
  not define nested transactions; a JDBC savepoint does not rewind the
  EntityManager's cached object graph. Never design JPA aggregate semantics
  on the assumption that a savepoint restores Java-side state.
- 3.7 `MANDATORY` says “this low-level invariant update is valid only inside
  a caller-owned unit.” `NEVER` catches accidental transactional invocation
  of work that must not hold resources. `NOT_SUPPORTED` deliberately moves a
  long/non-transactional operation outside an existing boundary. They expose
  boundary mistakes early, but overuse couples helpers to calling context.
- 3.8 No. With no existing transaction, `SUPPORTS` executes non-
  transactionally; each statement may auto-commit independently. It provides
  context-sensitive participation, not an atomicity guarantee.
- 3.9 No. It doubles connection demand, creates independent outcomes, hides
  partial failure questions, and may turn observability code into a request
  availability dependency. Decide whether the record is business state,
  compliance evidence, best-effort telemetry, or a reliable event. Give each
  category an explicit durability and failure design.

</details>

---

## 4. Rollback rules, caught exceptions & completion failures ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **4.1** 💭 State Spring's traditional default rollback rule. What
  Framework 6.2+ option can change the global annotation default?
- [ ] **4.2** 🛠 Make a checked `TransferRejectedException` roll back using a
  type-safe rule. Why are broad name-pattern rules risky?
- [ ] **4.3** 🔮 A runtime exception is caught **inside the same target
  method** and never crosses the interceptor. Will Spring's default
  exception rule see it?
- [ ] **4.4** 🔮 An inner proxied `REQUIRED` method throws a runtime exception
  and the outer catches it. Contrast this with 4.3.
- [ ] **4.5** 💭 Business rejection versus technical failure: should every
  rejected command be modeled as an exception that rolls back?
- [ ] **4.6** 🐛 A method returns `false` after catching an SQL exception, but
  the caller later receives `UnexpectedRollbackException`. Diagnose it.
- [ ] **4.7** 💭 When is `TransactionStatus.setRollbackOnly()` reasonable,
  and why is exception-based rollback normally clearer?
- [ ] **4.8** 🏭 The database commit succeeds, but the client connection drops
  before the response arrives. From the caller's view, what is the outcome and
  what makes retry safe?

<details><summary>Solutions 4</summary>

- 4.1 Traditionally Spring rolls back on an unhandled `RuntimeException` or
  `Error`, not a checked exception. Framework 6.2+ can use
  `@EnableTransactionManagement(rollbackOn = ALL_EXCEPTIONS)` for a global
  all-exception annotation default; method-specific rules still override as
  applicable. State the estate's configured policy instead of assuming.
- 4.2 Prefer the exception class:

  ```java
  @Transactional(rollbackFor = TransferRejectedException.class)
  public void transfer(...) throws TransferRejectedException { ... }
  ```

  Class-name patterns match substrings and can accidentally include similarly
  named, nested, or versioned exception classes. Type-safe rules survive
  refactoring and communicate the intended hierarchy.
- 4.3 The interceptor sees a normal return, so its default exception rule does
  not mark rollback. The resource/provider might independently mark the
  transaction rollback-only after a database failure, but a caught ordinary
  exception does not magically do so. If failure invalidates the unit, let an
  appropriate exception cross the boundary or mark rollback explicitly.
- 4.4 In 4.3 no inner interceptor observes the exception. Here the inner
  transaction interceptor observes the runtime exception and marks its
  logical scope/shared physical transaction rollback-only before the outer
  catches it. The later commit therefore rolls back unexpectedly from the
  outer scope's perspective.
- 4.5 No. Expected validation can happen before mutation and return a typed
  business result without opening/invalidation of a transaction. Once a unit
  has partially mutated data, exception-driven rollback is often appropriate.
  Choose based on invariant and API semantics, not a rule that “business
  exceptions are checked” or “all rejection is exceptional.”
- 4.6 The SQL/provider or an inner interceptor marked the physical
  transaction rollback-only. Returning `false` hid the original signal but
  could not restore the unit. Log/preserve the causal exception, stop work,
  and let the boundary report rollback; retry only in a fresh transaction
  under a defined policy.
- 4.7 It is useful in programmatic templates when the callback deliberately
  converts a failure into a result yet must veto commit. It is less visible
  than throwing a domain/technical exception and can create surprising
  rollback-only state for callers, so keep it close to an explicit
  transaction boundary and document the result semantics.
- 4.8 The caller has an **ambiguous outcome**: the server committed, but the
  acknowledgement was lost. Retrying safely requires a stable idempotency key
  and durable record of the original result/state, or a status-query/
  reconciliation protocol. A transport timeout never proves rollback.

</details>

---

## 5. Isolation, locks, deadlocks & retry ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **5.1** 💭 Define dirty read, non-repeatable read and phantom. Why do
  database implementations make the simple isolation table incomplete?
- [ ] **5.2** 🔮 Two `READ_COMMITTED` transactions read balance 100, each
  writes 70 after subtracting 30. What anomaly remains?
- [ ] **5.3** 🛠 Give three defensible ways to protect a debit invariant:
  optimistic versioning, pessimistic locking, and atomic conditional SQL.
- [ ] **5.4** 💭 Does `@Transactional(isolation = SERIALIZABLE)` mean no
  application retry is needed?
- [ ] **5.5** 🏭 Explain a database deadlock, how the database resolves it,
  and why one transaction is chosen as a victim.
- [ ] **5.6** 🛠 State the prevention and recovery playbook for deadlocks in
  a two-account transfer.
- [ ] **5.7** 💭 Why must a deadlock/serialization retry rerun the **whole
  unit of work** in a fresh transaction rather than only the last SQL?
- [ ] **5.8** 🐛 `@Retryable` wraps `@Transactional`, but nobody knows which
  advice is outermost. Why can advice order change correctness?
- [ ] **5.9** 🔮 An inner joining `REQUIRED` method declares
  `SERIALIZABLE`, while the outer began `READ_COMMITTED`. Does the physical
  transaction upgrade midway?

<details><summary>Solutions 5</summary>

- 5.1 A dirty read observes uncommitted work; a non-repeatable read sees one
  row change between reads; a phantom is a changed set of rows matching a
  predicate. SQL-standard names are a starting model, but databases differ in
  MVCC snapshots, predicate locks, gap locks and which anomalies their level
  prevents. Answer for the target engine and verify the real behavior.
- 5.2 A **lost update** can remain: the second write overwrites the first
  based on stale state. `READ_COMMITTED` preventing dirty reads does not make
  a read-modify-write sequence atomic. Protect the invariant explicitly.
- 5.3 Options:

  ```text
  optimistic:  UPDATE account SET balance=?, version=version+1
               WHERE id=? AND version=?         → zero rows means conflict

  pessimistic: SELECT ... FOR UPDATE             → serialize contenders

  atomic SQL:  UPDATE account SET balance=balance-30
               WHERE id=? AND balance>=30        → inspect affected-row count
  ```

  Choose using contention, invariant complexity and retry cost. The locking
  trade-offs are drilled further in
  [JPA performance §7](spring-data-jpa-performance.md).
- 5.4 No. Serializable implementations may abort a transaction when they
  detect a serialization conflict; deadlocks and infrastructure failures are
  also possible. The caller needs a bounded retry policy for specifically
  classified transient failures, and the entire operation/external effects
  must be safe to retry.
- 5.5 A deadlock is a wait cycle: transaction A holds a resource B needs,
  while B holds one A needs. The database detects the cycle and aborts a
  victim so the other can progress. Victim choice is database policy/cost,
  not evidence that the victim's business request was wrong.
- 5.6 Lock accounts in one deterministic order (for example smaller ID
  first), keep the transaction short, index predicates so locks are narrow,
  avoid remote calls inside, and monitor lock waits/deadlocks. On a classified
  victim exception, roll back completely, add bounded backoff/jitter and retry
  the idempotent use case with fresh reads.
- 5.7 Earlier reads and decisions belonged to the aborted snapshot and every
  statement in the physical transaction was rolled back. Retrying only the
  last statement reuses stale assumptions or misses prior changes. Start a
  new transaction, reread state, revalidate invariants and repeat the unit.
- 5.8 If retry surrounds transaction advice, each attempt can receive a fresh
  transaction—the usual requirement. If transaction advice surrounds retry,
  multiple attempts may occur inside one already-failed/rollback-only
  transaction. Make aspect order explicit or put retry orchestration in an
  outer bean and the transactional attempt in another bean; test the actual
  proxy chain. The wider timeout, backoff, idempotency and composition rules
  live in [Spring Boot resilience](spring-boot-resilience.md).
- 5.9 No. A participating scope normally inherits the already-active
  physical transaction's characteristics. It cannot upgrade isolation
  halfway through. Put isolation on the method that starts the transaction,
  or enable existing-transaction validation so the mismatch fails loudly.

</details>

---

## 6. `readOnly`, timeout & transaction-manager selection ⭐⭐ · 🎯 CORE PATH

- [ ] **6.1** 💭 Is `@Transactional(readOnly = true)` a portable write
  prohibition? State what it can optimize and what it cannot guarantee.
- [ ] **6.2** 🐛 A write inside a read-only JPA transaction appears to work in
  one environment and disappears/fails in another. Why was the contract
  misunderstood?
- [ ] **6.3** 💭 What does Spring's transaction `timeout` bound? Does it
  replace HTTP client, connection-acquisition and end-to-end request
  deadlines?
- [ ] **6.4** 🔮 Inner `REQUIRED(timeout = 2)` joins an outer transaction
  with a 30-second timeout. Does it create a two-second sub-deadline?
- [ ] **6.5** 🛠 Select `ordersTransactionManager` for one method and
  `ledgerTransactionManager` for another.
- [ ] **6.6** 🏭 A method writes to two datasources while annotated with only
  one local manager. Are both writes atomic?
- [ ] **6.7** 💭 Local transaction managers versus JTA/XA: when is XA
  conceptually applicable, and why do microservices often prefer local
  transactions plus coordination?
- [ ] **6.8** 🐛 Boot finds multiple `TransactionManager` beans and an
  unqualified annotation uses the wrong/ambiguous one. What should be made
  explicit?

<details><summary>Solutions 6</summary>

- 6.1 It is a **semantic hint** to Spring, the manager, ORM, driver and
  database. It can reduce dirty-check/flush work or request a read-only
  connection/transaction, depending on stack. It is not a portable security
  boundary or universal guarantee that every write throws. Enforce write
  permissions and database routing separately; treat a write in a read-only
  use case as a bug.
- 6.2 Provider flush mode, transaction manager, driver and database read-only
  enforcement differ. Hibernate/Spring may skip automatic flush, or the DB
  may reject DML, while another stack only treats the flag as a hint. Do not
  use environment-specific accidental behavior as correctness; separate
  commands from queries and test against the production engine.
- 6.3 It is a transaction-manager/resource timeout applied where supported,
  often to the newly started transaction and JDBC statements. It is not one
  universal wall-clock budget and does not configure DNS/connect/read
  timeouts, pool acquisition, Kafka, or a peer service. Build an end-to-end
  deadline budget with explicit per-dependency timeouts.
- 6.4 Normally no. A joining scope uses the outer physical transaction's
  timeout; local characteristics are ignored unless validation rejects a
  mismatch. A new timeout applies when that boundary actually starts a new
  `REQUIRED`/`REQUIRES_NEW` transaction.
- 6.5 Qualify the manager explicitly:

  ```java
  @Transactional("ordersTransactionManager")
  public void createOrder(...) { ... }

  @Transactional(transactionManager = "ledgerTransactionManager")
  public void postLedger(...) { ... }
  ```

  The value can be a manager bean name or matching qualifier. A type-level
  qualifier can reduce repetition when every method in a service shares one
  manager.
- 6.6 No. The selected local manager commits only its configured resource.
  The second datasource may auto-commit or run under a separate manager, so
  either side can succeed alone. Use one database transaction when the data
  truly needs one atomic boundary, or choose an explicit compatible
  distributed/local coordination design.
- 6.7 JTA/XA/2PC is conceptually for multiple XA-capable resources enlisted
  in one distributed transaction under a coordinator. It adds protocol,
  operational coupling, blocking/recovery and compatibility constraints;
  many brokers/services are not ordinary XA participants. Independently
  deployable services commonly keep local transactions short and use outbox,
  idempotency, sagas and reconciliation for cross-service consistency.
- 6.8 Define the default deliberately (`@Primary`/
  `TransactionManagementConfigurer` where appropriate) and qualify methods
  that use another manager. Also make repository/entity-manager wiring
  unambiguous. “There is an annotation” does not prove which resource is
  protected.

</details>

---

## 7. `TransactionTemplate`, synchronization & transaction-bound events ⭐⭐ · 🎯 CORE PATH

- [ ] **7.1** 💭 When is `TransactionTemplate` clearer than another
  annotated method/proxy?
- [ ] **7.2** 🛠 Write a programmatic block that returns a value and marks
  rollback-only when a business condition is detected.
- [ ] **7.3** 🏭 A workflow needs “short DB transaction → slow remote call →
  short DB transaction.” Why is a template useful?
- [ ] **7.4** 💭 What lifecycle points does `TransactionSynchronization`
  expose, and why is `afterCommit` not another chance to write in the already
  committed transaction?
- [ ] **7.5** 🐛 An `afterCommit` callback writes through a `REQUIRED`
  repository, but no new commit follows. What boundary is missing?
- [ ] **7.6** 💭 `@TransactionalEventListener` phases and default: name them.
  What happens when an event is published without an active transaction?
- [ ] **7.7** 🔮 Does `@TransactionalEventListener(AFTER_COMMIT)` guarantee
  durable message delivery after a process crash?
- [ ] **7.8** 💭 Why can programmatic demarcation improve correctness but
  harm design when spread throughout business code?

<details><summary>Solutions 7</summary>

- 7.1 Use it for a small number of imperative blocks whose boundaries must be
  explicit inside an orchestration method, dynamic transaction definitions,
  sequential transactions separated by non-transactional work, or code that
  cannot conveniently cross a proxy. Declarative service boundaries remain
  clearer for the common case.
- 7.2 For example:

  ```java
  Transfer transfer = transactionTemplate.execute(status -> {
      Transfer created = repository.createPending(command);
      if (!limits.allow(created)) {
          status.setRollbackOnly();
          return created.rejected();
      }
      return created;
  });
  ```

  Prefer throwing a meaningful exception when rollback is itself the failure
  result; explicit rollback-only is useful when the callback deliberately
  returns a value while vetoing commit.
- 7.3 The template makes resource lifetime visible:

  ```text
  tx 1: persist PENDING and commit
        call remote system with idempotency key (no DB tx held)
  tx 2: persist SUCCEEDED/FAILED if transition is still valid and commit
  ```

  The workflow is now a state machine, not one false atomic unit. It needs
  recovery for a crash between phases.
- 7.4 Synchronizations can observe before-commit, before-completion,
  after-commit and after-completion phases (plus suspend/resume/savepoint
  hooks in current APIs). After commit, the original resource outcome is
  final even if handles remain bound during cleanup. New durable work needs
  its own transaction; callback failure cannot roll back the completed one.
- 7.5 Call an external bean using `REQUIRES_NEW` or an explicit new template
  if that follow-up database write is required. Better, ask whether an
  outbox row should have been written inside the original transaction so a
  crash between commit and callback cannot lose the action.
- 7.6 Phases are `BEFORE_COMMIT`, `AFTER_COMMIT` (default),
  `AFTER_ROLLBACK`, and `AFTER_COMPLETION`. Without an active transaction the
  listener is normally not invoked; `fallbackExecution = true` opts into
  ordinary execution, which changes the guarantee and should be deliberate.
- 7.7 No. It orders an **in-process callback** relative to transaction
  completion. The process can crash after database commit but before/during
  listener work, and there is no durable broker handoff merely because the
  annotation exists. Use a transactional outbox or another durable protocol
  when loss is unacceptable.
- 7.8 It exposes exact boundaries and avoids self-invocation ambiguity, but
  couples business code to Spring APIs and can scatter commit decisions into
  every branch. Keep it at orchestration/infrastructure seams, name reusable
  transaction definitions, and avoid replacing a coherent service boundary
  with callback soup.

</details>

---

## 8. Remote calls, ambiguity & idempotency ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **8.1** 🏭 Why is an HTTP/gRPC call inside a database transaction a
  connection-pool and locking risk even when the peer normally answers in
  30 ms?
- [ ] **8.2** 🔮 Local DB update succeeds, remote call succeeds, then local
  commit fails. Can database rollback undo the peer's action?
- [ ] **8.3** 🔮 Remote service commits but its response times out. Should the
  caller blindly retry a payment POST?
- [ ] **8.4** 🛠 Sketch an idempotency-key table/constraint and the response
  behavior for duplicate concurrent requests.
- [ ] **8.5** 💭 “Call remote before DB” versus “DB before remote”: explain
  why changing order does not eliminate the dual-write problem.
- [ ] **8.6** 🏭 Design a pending/succeeded/failed state machine around a
  remote payment switch without holding one database transaction open.
- [ ] **8.7** 💭 Compensation versus rollback: why is a refund/reversal a new
  business transaction rather than time travel?
- [ ] **8.8** 🏭 What recovery job or reconciliation evidence is needed for
  operations stuck in `PENDING` after a crash?

<details><summary>Solutions 8</summary>

- 8.1 Tail latency, packet loss, DNS, peer saturation and retries make the
  wait unbounded relative to the happy path. During it, the application can
  hold a pooled connection, locks, snapshots and an open persistence context.
  Enough waiting requests starve the pool and turn one slow dependency into a
  service-wide outage.
- 8.2 No. The peer owns a separate committed resource. Local rollback only
  reverts local database state. Record a recoverable workflow state and issue
  an idempotent compensation/reconciliation action when the business permits;
  never claim the annotation rolled the peer back.
- 8.3 No. Timeout means **unknown**, not failed. Retry with the same stable
  idempotency key so the peer can return the first result without performing
  the charge twice, or query operation status. If the peer offers neither,
  reconciliation is required before another irreversible attempt.
- 8.4 A minimal model has a unique `(client_id, idempotency_key)`, request
  fingerprint/hash, status and stored response/reference. In one transaction,
  insert/claim the key and operation state. A concurrent duplicate either
  waits/reloads or loses the unique race, then returns the original outcome.
  Reject reuse of the same key with a different request body.
- 8.5 Remote-first can leave a remote success with no local record. DB-first
  can leave committed local state with no remote action. A crash fits between
  any two independent commits. Solve the protocol with durable intent,
  idempotency, outbox/state machine and recovery—not statement order alone.
- 8.6 One defensible flow: transaction 1 validates and inserts `PENDING` with
  operation/idempotency key; commit. Call the switch outside a DB transaction.
  Transaction 2 conditionally moves `PENDING → SUCCEEDED/FAILED` using the
  same external reference. Timeouts remain `PENDING/UNKNOWN`; a poller or
  reconciliation process resolves them. State transitions must be monotonic
  and duplicate-safe.
- 8.7 Rollback erases uncommitted work inside one resource. Compensation is a
  later, durable domain action—refund, release, reversal—with its own failure,
  authorization, accounting and audit trail. Some effects are not exactly
  reversible, so the saga must model semantic compensation and escalation.
- 8.8 Persist attempt count, timestamps, external correlation/reference,
  request fingerprint and last known result. A recovery worker claims stale
  records safely, queries/retries idempotently, applies a conditional state
  transition, and emits metrics/alerts. Reconcile internal ledger/state to the
  external system's authoritative reports; do not delete ambiguity from logs.

</details>

---

## 9. Database + Kafka, outbox & saga ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **9.1** 🔮 Code updates the database, commits, then sends Kafka. Name
  the failure window. Reverse the order and name the other window.
- [ ] **9.2** 💭 What does a Kafka transaction make atomic, and what does
  Kafka exactly-once semantics **not** make atomic?
- [ ] **9.3** 🏭 Spring synchronizes a database transaction and Kafka
  transaction, committing DB first and Kafka second. Is that one atomic
  two-resource commit?
- [ ] **9.4** 🛠 Sketch the transactional outbox write path and relay path.
- [ ] **9.5** 🔮 Relay publishes an event, crashes before marking the outbox
  row sent, then restarts. What delivery occurs and what must the consumer do?
- [ ] **9.6** 💭 How do aggregate ID, event ID and aggregate sequence help
  ordering and deduplication?
- [ ] **9.7** 🏭 Polling publisher versus change-data-capture outbox relay:
  compare operational trade-offs.
- [ ] **9.8** 💭 Saga choreography versus orchestration. When does each become
  hard to operate?
- [ ] **9.9** 🏭 Design compensation for `debit → reserve FX → credit` when
  the credit step fails after the first two committed.
- [ ] **9.10** 💭 Why do outbox and saga still require idempotent consumers,
  observable state and reconciliation?

<details><summary>Solutions 9</summary>

- 9.1 DB-first can commit business state and crash/fail before publishing, so
  downstream never learns it. Kafka-first can publish an event whose database
  change later rolls back/fails, so consumers act on nonexistent state. This
  is the **dual-write problem**.
- 9.2 A Kafka producer transaction can atomically publish records/offsets
  within Kafka's transactional protocol; listener read-process-write flows
  can use Kafka EOS for broker-visible outputs and offsets. It does not enlist
  an ordinary relational database or HTTP service in the same Kafka broker
  transaction. “Exactly once” has a defined scope, not a universal business
  guarantee.
- 9.3 No. Spring can coordinate/synchronize two local transaction managers,
  but commits still occur in sequence. If DB commit succeeds and the later
  Kafka commit fails, the primary outcome cannot be rolled back; current
  Spring Kafka surfaces the secondary failure so the application can take
  remedial action. That is coordinated best effort, not atomic 2PC.
- 9.4 Inside one **local database transaction**, update business rows and
  insert an outbox row containing event ID, aggregate ID/sequence, type,
  payload and creation time. After commit, a relay claims committed unsent
  rows, publishes to Kafka, then records publication/advances its cursor.
  The atomic guarantee is business state + durable intent, not DB + broker.
- 9.5 At-least-once publication: the row remains eligible and is published
  again. The consumer needs an inbox/dedup record keyed by stable event ID or
  an idempotent state transition, committed atomically with its local effect.
  Do not rely on a short-lived in-memory set.
- 9.6 Event ID identifies the same delivery across retries. Aggregate ID is a
  natural Kafka key for per-aggregate partition order. Aggregate sequence
  lets consumers detect duplicates, gaps and stale events. These fields do
  not create global ordering; they make the required ordering contract
  explicit and diagnosable.
- 9.7 Polling is simple and database-portable but needs claiming, indexing,
  batch/lag tuning and cleanup. CDC can stream the database log with lower
  polling load and preserve commit order well, but introduces connector/log
  retention, schema-evolution and operational dependencies. Both need
  duplicate handling, monitoring and replay policy.
- 9.8 Choreography lets participants react to events and fits short, loosely
  coupled flows, but dependencies/cycles and end-to-end visibility become
  obscure as steps grow. Orchestration centralizes workflow state, commands,
  timeouts and compensation, improving visibility for complex flows but
  creating a critical coordinator that must itself be durable and scalable.
- 9.9 Persist the saga and step outcomes. If debit is compensable, issue an
  idempotent credit/reversal to the source; release/cancel the FX reservation
  using its stable reference; mark compensation progress and retry transient
  failures. If an FX execution is irreversible, it may be the pivot: hedge or
  route to manual repair instead of pretending an exact undo exists.
- 9.10 Relays, brokers, consumers and compensation workers can crash between
  effect and acknowledgement, producing retries and ambiguous states. Local
  idempotency prevents duplicate effects; workflow/outbox state makes progress
  visible; metrics and reconciliation find omissions, poison records and
  business mismatches that messaging guarantees alone cannot resolve.

</details>

---

## 10. Tests & diagnostics that prove boundaries ⭐⭐ · 🎯 CORE PATH

- [ ] **10.1** 🛠 Test the caught-inner-exception scenario and assert
  `UnexpectedRollbackException` plus unchanged database state.
- [ ] **10.2** 🐛 A `@Transactional` repository test passes, but production
  fails at commit. What should the test force before making assertions?
- [ ] **10.3** 💭 Why should lock/isolation/deadlock tests use the production
  database engine through Testcontainers rather than H2?
- [ ] **10.4** 🔮 A `@SpringBootTest(webEnvironment = RANDOM_PORT)` test is
  annotated `@Transactional`. Will the server-thread database writes always
  roll back with the test thread?
- [ ] **10.5** 🛠 Design a concurrency test for two simultaneous debits that
  proves the balance invariant and the chosen conflict behavior.
- [ ] **10.6** 🛠 Design outbox tests for atomic insert, duplicate relay
  publication and idempotent consumption.
- [ ] **10.7** 💭 Which transaction/pool/database signals diagnose “slow SQL”
  versus “long transaction” versus “waiting for a connection/lock”?
- [ ] **10.8** 🏭 How would you test `REQUIRES_NEW` pool pressure without
  writing a flaky sleep-based test?

<details><summary>Solutions 10</summary>

- 10.1 Call the **Spring-managed outer bean**, make a separate inner bean
  throw a runtime exception under `REQUIRED`, catch it in the outer method,
  and use AssertJ/JUnit to expect `UnexpectedRollbackException` from the
  outer boundary. Query from a fresh transaction/context and assert neither
  write committed. Calling objects with `new` would omit the proxy behavior
  being tested.
- 10.2 Flush and, when relevant, end/commit the transaction. ORM tests can
  defer DML and constraints until flush/commit, while the test framework
  normally rolls back. Use `EntityManager.flush()`, clear before rereading,
  and use `TestTransaction`/a non-rollback integration test when commit-phase
  behavior is the subject. Always clean test data deterministically.
- 10.3 Engines differ in MVCC, isolation, savepoints, lock granularity,
  deadlock detection, constraint timing, SQL states and timeout behavior. An
  H2 success cannot establish Oracle/Postgres behavior. Coordinate two real
  connections with latches/barriers and assert outcomes, not elapsed sleeps.
- 10.4 No. Imperative test transaction state is bound to the test thread;
  the embedded server handles HTTP on another thread and opens application
  transactions there. Those commits are real. Clean up explicitly or use an
  isolated schema/container. The same thread-bound warning applies to
  preemptive test timeouts that run the test body elsewhere.
- 10.5 Seed one account, start two workers behind a barrier, give each a
  separate transaction, and attempt conflicting debits. Assert final balance
  never violates the invariant and assert the selected mechanism: one
  optimistic failure, serialized pessimistic execution, or one zero-row
  conditional update. Repeat enough to validate coordination but do not
  assert scheduler order.
- 10.6 Verify business row and outbox row commit/rollback together. Then make
  a fake broker/publisher acknowledge before the relay's “sent” update fails;
  rerun and assert the same event ID is published again. Deliver that event
  twice to the consumer and assert its inbox/local effect commits once. Test
  ordering/gap handling separately.
- 10.7 Record transaction duration/outcome, active transaction count where
  available, Hikari active/idle/pending/acquisition latency/timeouts, SQL
  duration/rows, database sessions, lock-wait/deadlock/serialization metrics,
  and trace spans separating pool wait from execution. A query can be fast
  while its transaction is long because application/network time surrounds
  it.
- 10.8 Use a deliberately tiny isolated pool and coordination barriers: N
  workers each begin an outer transaction and prove they hold their resource,
  then simultaneously enter an inner bean using `REQUIRES_NEW`. Configure a
  bounded acquisition timeout and assert pool timeout under insufficient
  capacity; repeat with correct capacity or redesigned boundaries. Barriers
  create state deterministically; sleeps merely hope it occurs.

</details>

---

## 11. Senior production scenarios ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **11.1** 🏭 Transfer rows update successfully, the method returns, but
  the API reports 500 with `UnexpectedRollbackException`. Build the incident
  timeline and first evidence to collect.
- [ ] **11.2** 🏭 Pool usage pins at 100%, SQL latency is low, and thread dumps
  show calls inside `REQUIRES_NEW`. Diagnose and contain.
- [ ] **11.3** 🏭 A service holds row locks for 12 seconds while a sanctions
  API responds. Redesign the boundary without losing workflow correctness.
- [ ] **11.4** 🏭 Deadlocks spike after a new batch transfer feature ships.
  What code/query evidence separates inconsistent lock order from missing
  indexes or oversized batches?
- [ ] **11.5** 🏭 Duplicate payment requests arrive concurrently with the
  same idempotency key. Both pass an initial “does key exist?” query. Fix the
  race.
- [ ] **11.6** 🏭 A database row says `COMPLETED`, but no Kafka event exists.
  The service uses “save, then send.” Give immediate repair and durable fix.
- [ ] **11.7** 🏭 Outbox lag grows for one tenant while other tenants flow.
  What ordering/poison/hot-key hypotheses and metrics do you inspect?
- [ ] **11.8** 🏭 A synchronized DB+Kafka method throws because Kafka commit
  failed after DB commit. Should the handler retry the entire method blindly?
- [ ] **11.9** 🏭 An FX saga's compensation fails for six hours. What makes
  that an operable business process rather than a hidden exception loop?
- [ ] **11.10** 🏭 A read-only transaction routes to a replica immediately
  after a write, and the customer cannot see the transfer. Is this a
  transaction rollback bug?
- [ ] **11.11** 🏭 A team proposes `SERIALIZABLE` globally to fix every race.
  Challenge the correctness and capacity assumptions.
- [ ] **11.12** 🏭 An operations dashboard counts method exceptions but not
  commit failures. What outcome telemetry should surround the transaction?

<details><summary>Solutions 11</summary>

- 11.1 Trace proxy boundaries and propagation, preserve the original inner
  exception, inspect transaction DEBUG logs in a safe environment, and find
  where rollback-only was first set. Confirm the final database state from a
  fresh transaction. Common timeline: inner runtime/provider failure → marker
  set → outer catch/normal return → commit request → rollback →
  `UnexpectedRollbackException`.
- 11.2 Each outer scope holds one connection while its independent inner
  scope waits for another; low SQL time rules out query execution as the main
  bottleneck. Stop/admit less traffic, shorten acquisition timeouts carefully,
  disable the pathological path if possible, then remove unnecessary nesting
  or size pools/executors from measured concurrent resource demand. Merely
  increasing the pool can move overload to the database.
- 11.3 Transaction 1 validates and records a durable `PENDING_SCREENING`
  intent, then commits. Call sanctions outside a DB transaction using a
  stable operation key. Transaction 2 conditionally advances to approved/
  rejected; ambiguous calls remain pending for recovery. If funds must be
  protected, model a short local reservation/hold with expiry and explicit
  release, not a 12-second row lock.
- 11.4 Capture deadlock graphs/wait-for edges and the exact SQL/index plans.
  If transfers lock account A then B in inconsistent business order, impose a
  stable key order. If an update scans many rows because a predicate lacks an
  index, it can lock far more than intended. Bound batch size/transaction
  length and compare lock counts/wait time before and after each fix.
- 11.5 “Check then insert” races. Put a unique constraint on the idempotency
  identity, insert/claim inside a transaction, and handle the loser by loading
  the winning durable record. Bind the key to a request fingerprint and store
  the completed response/reference; use a conditional state machine for
  in-progress duplicates.
- 11.6 Repair by deriving missing events from authoritative state/audit and
  publishing with stable IDs under an approved replay process. Durable fix:
  write the state change and outbox intent in the same local DB transaction,
  relay at least once, and deduplicate consumers. Add an alert comparing
  business commits/outbox insertion/publication lag.
- 11.7 Inspect per-tenant/aggregate backlog age, relay attempts/error class,
  partition/key skew, row-claim queries and indexes, payload/schema failures,
  broker acknowledgements and one poison event blocking ordered processing.
  Quarantine/retry without violating aggregate order; do not let one tenant's
  hot key starve unrelated keys.
- 11.8 No. DB state already committed, so rerunning business logic can double
  effects or hit changed invariants. Treat the failure as a partial outcome:
  publish/reconcile from durable state with a stable event ID, compensate if
  required, and prefer outbox for future operations. Retry only an explicitly
  idempotent recovery step.
- 11.9 Persist saga/compensation state, owner, attempts, next retry, stable
  external references and reason; use bounded backoff, alerts/SLOs, a manual
  repair path, audit evidence and reconciliation. Compensation is production
  workflow with its own availability target, not a catch block that can retry
  forever invisibly.
- 11.10 Usually it is **read-after-write consistency/replica lag**, not local
  rollback. Route an appropriate consistency window to the primary, use a
  session/causal token if the platform supports one, or make pending state
  explicit. `readOnly` does not itself guarantee which datasource or freshness
  policy is used.
- 11.11 Stronger isolation can abort more transactions, increase lock/
  predicate contention and reduce throughput; it still needs retry and does
  not make remote effects atomic. Identify the violated invariant, use atomic
  SQL/version/targeted locks or serializable only on the use cases that need
  it, and capacity-test with production contention/data shape.
- 11.12 Record transaction name/use case, manager/resource, started/joined/
  suspended where safely observable, duration, commit/rollback outcome,
  rollback/commit failure category, retry attempt and correlation ID. Pair
  this with pool wait, lock waits and downstream spans. Never label metrics
  with account IDs or raw exception messages that explode cardinality/leak
  data.

</details>

---

## 12. Rapid-fire trap wall 🔮 · all keep

- [ ] **12.1** Does `@Transactional` make a private/self-invoked method
  transactional through proxy mode?
- [ ] **12.2** Are logical and physical transactions always one-to-one?
- [ ] **12.3** Can an inner `REQUIRED` scope independently commit before its
  outer scope?
- [ ] **12.4** Does catching an inner runtime exception clear rollback-only?
- [ ] **12.5** Is `UnexpectedRollbackException` thrown when rollback-only is
  first marked or when commit is attempted?
- [ ] **12.6** Does `REQUIRES_NEW` reuse the outer connection?
- [ ] **12.7** Can widespread `REQUIRES_NEW` exhaust a correctly functioning
  connection pool?
- [ ] **12.8** Is `NESTED` an independent physical transaction?
- [ ] **12.9** Does JPA define portable nested transactions?
- [ ] **12.10** Does `SUPPORTS` create a transaction when none exists?
- [ ] **12.11** Does a checked exception traditionally roll back by default?
- [ ] **12.12** Does a caught exception that never crosses an interceptor
  automatically trigger Spring's exception rollback rule?
- [ ] **12.13** Is flush equivalent to commit?
- [ ] **12.14** Can commit fail after the target method returned normally?
- [ ] **12.15** Does inner `REQUIRED(isolation = SERIALIZABLE)` upgrade an
  active `READ_COMMITTED` transaction?
- [ ] **12.16** Does `readOnly=true` portably prohibit every write?
- [ ] **12.17** Does transaction timeout configure HTTP client timeouts?
- [ ] **12.18** Do two local transaction managers automatically create one
  atomic transaction over two databases?
- [ ] **12.19** Does an imperative transaction automatically propagate into
  `@Async`/`CompletableFuture` work?
- [ ] **12.20** Can database rollback undo a successful remote call?
- [ ] **12.21** Does an HTTP timeout prove the remote operation failed?
- [ ] **12.22** Does idempotency mean “ignore every duplicate request”?
- [ ] **12.23** Does Kafka EOS make a relational DB update exactly-once in the
  same atomic boundary?
- [ ] **12.24** Is Spring DB+Kafka transaction synchronization atomic 2PC?
- [ ] **12.25** Does an outbox guarantee exactly one broker delivery?
- [ ] **12.26** Can an in-memory `AFTER_COMMIT` listener replace a durable
  outbox when message loss is unacceptable?
- [ ] **12.27** Is saga compensation the same as database rollback?
- [ ] **12.28** Should a deadlock retry only the failed final SQL statement?
- [ ] **12.29** Does a test-thread transaction always roll back server-thread
  work invoked through a real HTTP port?
- [ ] **12.30** Is globally stronger isolation always the safest production
  fix?

<details><summary>Solutions 12</summary>

- 12.1 **No. The call must cross an interceptable proxy boundary.**
- 12.2 **No. Multiple `REQUIRED` logical scopes can share one physical
  transaction.**
- 12.3 **No. It only votes on the shared physical transaction.**
- 12.4 **No. The marker survives the catch.**
- 12.5 **Normally at the outer completion/commit attempt.**
- 12.6 **No. It suspends the outer scope and needs an independent resource.**
- 12.7 **Yes. Outer transactions can hold every connection while inner
  scopes wait for another.**
- 12.8 **No. It normally uses a savepoint in one physical transaction.**
- 12.9 **No. JPA itself does not define nested transaction semantics.**
- 12.10 **No. It runs non-transactionally without an existing scope.**
- 12.11 **Traditionally no; configure a type rule or Framework 6.2+ global
  policy.**
- 12.12 **No. The interceptor sees a normal return, though the resource may
  independently mark rollback-only.**
- 12.13 **No. Flush sends SQL; the unit can still roll back.**
- 12.14 **Yes. Constraints, conflicts, I/O or rollback-only can fail
  completion.**
- 12.15 **No. The existing outer characteristics normally win.**
- 12.16 **No. It is a stack-dependent hint/optimization, not a portable
  authorization boundary.**
- 12.17 **No. Configure dependency/pool/request deadlines separately.**
- 12.18 **No. Qualifying one annotation selects one manager; XA or an
  explicit coordination protocol is required.**
- 12.19 **No. Imperative transaction state is normally thread-bound.**
- 12.20 **No. Compensation is a new cross-resource action.**
- 12.21 **No. The result is ambiguous; query or retry idempotently.**
- 12.22 **No. Bind the key to the same request and return its durable original
  outcome; reject conflicting reuse.**
- 12.23 **No. Kafka EOS has a Kafka-defined broker/offset scope.**
- 12.24 **No. The local commits are ordered and a later one can fail.**
- 12.25 **No. Relays can publish at least once; consumers must deduplicate or
  be idempotent.**
- 12.26 **No. A crash after DB commit can lose the callback/message.**
- 12.27 **No. Compensation is a later domain transaction with its own
  failure modes.**
- 12.28 **No. Roll back and retry the whole idempotent unit with fresh
  state.**
- 12.29 **No. The server normally uses another thread and transaction.**
- 12.30 **No. Target the invariant and capacity-test contention/retries.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Logical vs physical? | Each advised method has a logical scope and rollback vote; `REQUIRED` scopes can map to one physical resource transaction. |
| `UnexpectedRollbackException`? | An inner participant marked the shared transaction rollback-only, the outer still requested commit, and Spring reports that rollback occurred instead of falsely reporting success. |
| `REQUIRES_NEW`? | Suspend the outer scope and use an independent physical transaction/resource. It commits independently but needs another connection and can exhaust the pool. |
| `NESTED`? | A savepoint in one physical transaction when the manager/resource supports it. It is not portable nested JPA semantics and outer rollback still loses all work. |
| Caught exception? | Catching changes Java control flow, not transaction validity. If an inner interceptor/provider marked rollback-only, stop and let the unit roll back. |
| Isolation? | A database concurrency contract, not a substitute for invariant design. Use versioning, targeted locks or atomic SQL and retry classified aborts as a whole fresh unit. |
| `readOnly`? | A semantic hint/optimization whose enforcement varies by manager/provider/database; not a portable write prohibition or security boundary. |
| Transaction timeout? | A manager/resource setting for a newly started transaction where supported, not an end-to-end deadline or HTTP/pool timeout. |
| Multiple managers? | Qualify the manager/resource explicitly. Two local managers do not become one atomic transaction because annotations share a method. |
| Remote call boundary? | Do not hold DB resources over unpredictable network work. Persist durable state, call with idempotency outside the transaction, then finalize conditionally and recover ambiguity. |
| Idempotency key? | A stable client/operation key claimed with a unique constraint, bound to a request fingerprint and stored result so concurrent/repeated calls produce one business effect. |
| DB + Kafka? | A local DB transaction cannot atomically commit an ordinary Kafka transaction. Synchronization orders commits but leaves a partial-failure window. |
| Outbox? | Commit business state and event intent in one DB transaction; relay committed rows at least once and make consumers idempotent/deduplicating. |
| Saga? | A durable sequence of local transactions with retries and semantic compensations; choreography distributes control, orchestration centralizes workflow state. |
| Deadlock recovery? | Prevent with deterministic/narrow/short locking, then roll back and retry the whole idempotent unit with fresh state, bounded backoff and metrics. |
| How test? | Use Spring-managed proxies, target DB/Testcontainers, independent threads/connections, forced flush/commit, and assert final state plus conflict/rollback outcome. |

---

## Primary references

- [Spring Framework — Transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring Framework — `@Transactional` settings and multiple managers](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
- [Spring Framework — Transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)
- [Spring Framework — Rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)
- [Spring Framework — Programmatic transactions](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)
- [Spring Framework — Transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html)
- [Spring Framework — Test-managed transactions](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html)
- [Spring Framework — `JpaTransactionManager` savepoint limitations](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/orm/jpa/JpaTransactionManager.html)
- [Spring for Apache Kafka — Transactions](https://docs.spring.io/spring-kafka/reference/kafka/transactions.html)
- [Spring for Apache Kafka — Exactly-once semantics](https://docs.spring.io/spring-kafka/reference/kafka/exactly-once.html)
- [PostgreSQL — Transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html)
- [PostgreSQL — Deadlocks](https://www.postgresql.org/docs/current/explicit-locking.html#LOCKING-DEADLOCKS)
- [AWS — Transactional outbox pattern](https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/transactional-outbox.html)
- [Microsoft — Saga pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/saga)

---

## Extensions — after the senior core

- JTA/XA internals, two-phase commit logs, heuristic outcomes and recovery.
- Reactive transaction management with `TransactionalOperator`, Reactor
  context and cancellation.
- Advanced Spring Kafka consumer-initiated transactions, retry-topic
  incompatibilities and broker fencing/transactional-ID operations.
- Debezium/outbox event router, log retention and schema-evolution operations.
- Temporal workflow engines for durable orchestration and human/manual steps.
- Database-specific predicate/gap locks, deferrable constraints and advisory
  locks.
- Read/write routing, replica consistency tokens and failover semantics.
- Formal ledger/double-entry invariants and accounting reconciliation.

---

## How to drill this kit

1. Draw §0, then trace one successful commit, one inner rollback-only vote,
   and one commit-time failure without reading the answers.
2. Build two Spring beans and reproduce `UnexpectedRollbackException`,
   `REQUIRES_NEW` independent commit and pool exhaustion with a deliberately
   small pool.
3. Use Postgres/Testcontainers and two controlled threads to reproduce an
   optimistic conflict and a deadlock/serialization abort; retry the whole
   unit in a fresh transaction.
4. Implement a minimal transfer state machine plus outbox table. Crash the
   relay after publish/before acknowledgement and prove consumer idempotency.
5. For every production scenario, say: atomic resource → boundary → partial
   failure → durable recovery → monitoring. Repeat §§2, 3, 8, 9 and 11 after
   a week.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 mental model | ⬜ | ⬜ | ⬜ |
| §1 proxy/resources | ⬜ | ⬜ | ⬜ |
| §2 rollback-only/`REQUIRED` | ⬜ | ⬜ | ⬜ |
| §3 propagation | ⬜ | ⬜ | ⬜ |
| §4 rollback rules | ⬜ | ⬜ | ⬜ |
| §5 isolation/deadlocks | ⬜ | ⬜ | ⬜ |
| §6 attributes/managers | ⬜ | ⬜ | ⬜ |
| §7 programmatic/events | ⬜ | ⬜ | ⬜ |
| §8 remote calls/idempotency | ⬜ | ⬜ | ⬜ |
| §9 DB/Kafka/outbox/saga | ⬜ | ⬜ | ⬜ |
| §10 tests/diagnostics | ⬜ | ⬜ | ⬜ |
| §11 production scenarios | ⬜ | ⬜ | ⬜ |
| §12 trap wall | ⬜ | ⬜ | ⬜ |
