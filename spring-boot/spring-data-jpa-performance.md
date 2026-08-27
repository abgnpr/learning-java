# Spring Data JPA & Hibernate Performance — Senior Anti-Fumble Kit

For a backend engineer who can write a `JpaRepository` but must reason
about the SQL, memory, locks, and consistency hidden behind the object
model. The goal is to predict **when SQL runs**, deliberately choose
**what is fetched**, and diagnose a slow or incorrect persistence path
without blaming “Hibernate magic.”

**Legend** — exercise styles:
🔮 predict the result · 🛠 build/query · 🐛 diagnose/fix ·
💭 explain the trade-off · 🏭 production scenario

Try each prompt aloud before opening its `<details>` solution. The
answer card near the end is for rehearsal after the exercises, not a
replacement for retrieval practice.

> ## 🎯 Senior core path
>
> **§0 → §1 → §2 → §3 → §4 → §5 → §6 → §7 → §9 → §10 → §11**
>
> Mental model · persistence context · mappings · fetching · query
> shape · pagination · writes · concurrency · boundary · diagnostics ·
> production scenarios.
>
> A senior JPA answer connects four costs: **round trips**, **rows and
> columns transferred**, **objects retained in the persistence
> context**, and **database contention**. “Make it lazy” is not a fetch
> plan. “Add a cache” is not a diagnosis.

> **Version line.** Examples use `jakarta.persistence` terminology and
> modern Spring Data JPA/Hibernate behavior. Boot 2.7 uses the older
> `javax.persistence` namespace and normally Hibernate 5.6; Boot 3/4
> use `jakarta.persistence` and newer Hibernate generations. SQL shape,
> generated aliases, batching details, and provider extensions vary by
> version and database, so verify the actual SQL and execution plan.
> The persistence-context, ownership, fetching, flush, and locking
> concepts remain the interview foundation.

---

## 0. The one picture — an object graph driving a relational machine · 🎯 CORE PATH

```text
service transaction
    │
    ▼
EntityManager / Hibernate Session
    │ owns one persistence context
    ├─ identity map: (entity type, id) → managed Java object
    ├─ snapshots / dirty tracking
    ├─ pending INSERT / UPDATE / DELETE work
    └─ lazy proxies and collections
    │
    ├─ flush → SQL DML (not necessarily commit)
    └─ query → SQL SELECT → rows → entities/projections
                         │
                         ▼
                 JDBC driver / connection pool
                         │
                         ▼
              database indexes, locks, query plan
```

- [ ] **0.1** 💭 JPA, Hibernate, Spring Data JPA, and JDBC — give one
  line for each without treating them as synonyms.
- [ ] **0.2** 💭 What is a persistence context, and why is calling it
  “just a cache” incomplete?
- [ ] **0.3** 💭 What four costs should you inspect before claiming a
  JPA path is efficient?
- [ ] **0.4** 🔮 A repository method returns in 20 ms locally but 900 ms
  in production. Name five layers that could explain the difference.

<details><summary>Solutions 0</summary>

- 0.1 **JPA/Jakarta Persistence** is the ORM specification/API.
  **Hibernate ORM** is a provider implementing it and adding its own
  features. **Spring Data JPA** builds repository abstractions and
  query generation on top of JPA. **JDBC** is the lower-level Java API
  that executes SQL through database connections; Hibernate ultimately
  uses it.
- 0.2 A persistence context is a **unit-of-work identity map plus
  change tracker and write-behind queue**. It guarantees one managed
  instance per entity identity within the context, supplies dirty
  checking, coordinates cascades and flush ordering, and backs lazy
  loading. It is not a general query-result cache and should not become
  an unbounded application cache.
- 0.3 Inspect: (1) database round trips; (2) rows/columns and Cartesian
  multiplication; (3) Java objects retained and dirty-checked; and
  (4) contention — connection waits, row/table locks, transaction
  duration, and database CPU/I/O. A fifth practical dimension is cache
  hit/staleness behavior.
- 0.4 Query count/N+1, production data cardinality/selectivity, missing
  or different indexes/statistics, connection-pool wait, network
  latency, lock contention, database resource pressure, result mapping
  and GC, a different database dialect/plan, or logging/observability
  overhead. Local H2 with 100 rows proves almost none of these.

</details>

---

## 1. Entity states, identity, dirty checking & flush ⭐⭐ · 🎯 CORE PATH

- [ ] **1.1** 💭 Define transient, managed, detached, and removed entity
  states. Which state gets automatic dirty checking?
- [ ] **1.2** 🔮 Within one persistence context, two calls to
  `find(Order.class, 42L)` return the same Java instance or merely
  equal instances? Does that prove every query is cached?
- [ ] **1.3** 🔮 A managed entity's field changes, but no repository
  `save()` is called. What happens at commit?
- [ ] **1.4** 🐛 Code calls `merge(detached)` and then keeps editing the
  original detached object. Which instance is tracked?
- [ ] **1.5** 💭 Flush vs commit: what does each guarantee, and when can
  automatic flush happen before commit?
- [ ] **1.6** 🐛 A unique constraint failure appears at transaction
  commit, far from the line that created the entity. Why, and how can a
  test or use case surface it earlier?
- [ ] **1.7** 🏭 A batch job loads 500,000 entities in one transaction and
  memory climbs until OOM. Explain the persistence-context cause.
- [ ] **1.8** 💭 Does `@Transactional(readOnly = true)` make writes
  impossible?

<details><summary>Solutions 1</summary>

- 1.1 **Transient**: new object, not associated with a context.
  **Managed**: associated with the current context; changes are tracked.
  **Detached**: has identity but its context no longer manages it.
  **Removed**: managed and scheduled for deletion. Automatic dirty
  checking applies to managed entities.
- 1.2 The same entity identity resolves to the **same Java object
  instance** in one persistence context. That is the first-level
  identity map. It does not mean arbitrary JPQL/repository queries are
  skipped; a query can still hit the database and then reconcile rows
  with already managed instances.
- 1.3 Hibernate compares/tracks the managed entity, generates an
  `UPDATE` at flush if it is dirty, and commits it with the transaction.
  Calling `save()` is not required for an already managed JPA entity,
  though repository-oriented code may retain the call for abstraction
  consistency.
- 1.4 `merge` copies state into a **managed instance and returns it**.
  The argument remains detached. Continue with the returned object or,
  more commonly, load the managed entity inside the transaction and
  apply an explicit command/patch to it.

  ```java
  Order managed = entityManager.merge(detached);
  managed.rename("tracked");
  detached.rename("not automatically tracked");
  ```

- 1.5 **Flush** synchronizes pending in-memory changes to SQL; the
  transaction can still roll back. **Commit** makes the transaction
  durable/visible according to database rules. With normal automatic
  flushing, Hibernate flushes before commit, before a query whose
  result could be affected by pending changes, and on explicit
  `flush()`.
- 1.6 Write-behind delays SQL until flush, so database constraints may
  fail later. Call `flush()` at the point where the use case needs the
  database invariant checked, and in persistence tests flush then clear
  before asserting. Do not scatter flushes merely to imitate immediate
  SQL; each flush reduces batching freedom.
- 1.7 The persistence context holds strong references and dirty-checking
  state for managed entities. Page/chunk the work and periodically
  `flush()` then `clear()`, or use stateless/JDBC/bulk techniques when
  entity lifecycle semantics are unnecessary. One enormous transaction
  also creates long locks, a large rollback scope, and WAL/undo pressure.
- 1.8 No. It is a **hint and optimization**, not a universal write
  firewall. Spring/Hibernate can reduce flush/dirty-check work and the
  JDBC/database may receive a read-only hint, but correctness must not
  depend on every database rejecting DML.

</details>

---

## 2. Association mapping & aggregate boundaries ⭐⭐ · 🎯 CORE PATH

- [ ] **2.1** 💭 In a bidirectional `Order`–`OrderLine` relationship,
  which side normally owns the foreign key? What does `mappedBy` mean?
- [ ] **2.2** 🐛 Code adds a line only to `order.getLines()` but never
  calls `line.setOrder(order)`. Why can the database relationship be
  missing despite the in-memory collection looking correct?
- [ ] **2.3** 💭 Cascade vs fetch: are they related? Explain
  `CascadeType.PERSIST` and `FetchType.LAZY` on the same association.
- [ ] **2.4** 💭 `orphanRemoval = true` vs cascading remove — what event
  triggers each?
- [ ] **2.5** 🏭 Why is a bidirectional one-to-many often more efficient
  than a unidirectional one-to-many using a link table?
- [ ] **2.6** 🏭 When should a many-to-many join table become its own
  entity?
- [ ] **2.7** 🐛 Lombok-generated `toString`, `equals`, and `hashCode`
  include every association. Name the performance and correctness
  failures this can cause.
- [ ] **2.8** 💭 What makes entity equality difficult when IDs are
  generated after persistence?

<details><summary>Solutions 2</summary>

- 2.1 The child `OrderLine` normally owns the relation because its
  table contains `order_id`; its `@ManyToOne @JoinColumn` writes that
  foreign key. `Order.lines` is the inverse side:
  `@OneToMany(mappedBy = "order")`. `mappedBy` names the child property
  that owns the mapping; it is not a column name.
- 2.2 JPA writes the relationship according to the **owning side**.
  Maintain both sides with aggregate helper methods:

  ```java
  public void addLine(OrderLine line) {
      lines.add(line);
      line.setOrder(this);
  }

  public void removeLine(OrderLine line) {
      lines.remove(line);
      line.setOrder(null);
  }
  ```

- 2.3 They are independent. **Cascade** propagates persistence-context
  operations between lifecycle-related entities. `PERSIST` means
  persisting the order also persists new lines. **Fetch** controls when
  association state is loaded; `LAZY` means lines are not loaded until
  explicitly fetched/accessed. Cascade does not mean eager.
- 2.4 Removing a child from a parent's collection/reference triggers
  deletion with **orphan removal** when the child's lifecycle belongs
  to that parent. Cascading `REMOVE` propagates when the **parent itself
  is removed**. Use both only for true composition/ownership, not for
  shared reference entities.
- 2.5 A unidirectional collection may need a separate link table and
  inefficient delete/reinsert maintenance. In the bidirectional shape,
  the child owns one ordinary foreign key and Hibernate can update the
  relevant row. The in-memory bidirectionality costs helper-method
  discipline but better matches the relational model.
- 2.6 Promote it when the relationship has attributes or behavior —
  membership date, status, quantity, ordering, audit fields — or needs
  independent identity/lifecycle. `StudentCourse`/`Enrollment` is then
  an entity with two many-to-one associations, not an invisible link.
- 2.7 `toString()` can initialize lazy graphs or recurse infinitely.
  Generated equality can traverse collections, trigger SQL, recurse,
  or change after persistence. A mutable/generated ID in `hashCode`
  can make an entity unreachable inside a `HashSet` after the ID is
  assigned. Exclude associations and design identity deliberately.
- 2.8 A transient entity has no generated ID, then gains one on
  persistence. Equality based only on ID makes all null-ID objects look
  equal if written poorly; changing hash code after insertion breaks
  hashed collections. Prefer a stable immutable natural key when one
  truly exists, or a carefully tested entity equality strategy; do not
  blindly generate it from every field.

</details>

### Mapping shape

```java
@Entity
class Order {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany(mappedBy = "order",
               cascade = CascadeType.PERSIST,
               orphanRemoval = true)
    private Set<OrderLine> lines = new HashSet<>();
}

@Entity
class OrderLine {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
```

The annotations do not define a fetch plan for every use case. They
define a safe mapping baseline; queries decide what each operation
needs.

---

## 3. Fetch planning, N+1 & Cartesian explosions ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 💭 State the default JPA fetch types for to-one and to-many
  associations. Why are explicit lazy to-ones usually safer?
- [ ] **3.2** 🔮 One query loads 100 orders; iterating
  `order.getCustomer().getName()` emits 100 more queries. Name the
  problem and its cost formula.
- [ ] **3.3** 🛠 Give four ways to solve N+1, and say when each fits.
- [ ] **3.4** 💭 `JOIN` vs `JOIN FETCH` in JPQL — which changes the
  returned fetch plan?
- [ ] **3.5** 🐛 One query fetch-joins `orders.lines` and
  `orders.adjustments`; the result explodes. Explain the relational
  multiplication.
- [ ] **3.6** 🐛 A `Page<Order>` query fetch-joins `lines`. Why can
  pagination become incorrect, in-memory, or rejected?
- [ ] **3.7** 💭 Entity graph vs fetch join: compare reuse, dynamic query
  text, and control.
- [ ] **3.8** 💭 Association batch fetching vs JDBC statement batching:
  same feature or entirely different?
- [ ] **3.9** 🏭 Why can `EAGER` still produce N+1, and why is “make
  everything eager” not a fix?
- [ ] **3.10** 🐛 JSON serialization unexpectedly runs SQL after the
  controller returns. What boundary allowed it?

<details><summary>Solutions 3</summary>

- 3.1 JPA defaults `@ManyToOne` and `@OneToOne` to **EAGER**;
  `@OneToMany` and `@ManyToMany` default to **LAZY**. Explicit lazy
  associations prevent the mapping from forcing data into every use
  case. Some to-one lazy behavior depends on proxies/enhancement and
  mapping shape, so inspect generated SQL.
- 3.2 **N+1 selects**: one root query plus one secondary query per root
  or association proxy — `1 + N` round trips. The database may execute
  fast individual queries, yet network/driver/connection latency makes
  the total disastrous.
- 3.3 Common plans:

  - `JOIN FETCH` in JPQL for a query-specific graph, especially to-one
    chains or one bounded collection.
  - `@EntityGraph` for a reusable/declarative repository fetch graph.
  - DTO/interface projection when the caller needs fields, not managed
    aggregates.
  - Batch/subselect fetching to reduce lazy secondary selects when a
    join would create a huge Cartesian product.

  The correct choice comes from cardinality and the response shape,
  not a global annotation.
- 3.4 A normal `JOIN` participates in predicates/ordering but does not
  necessarily initialize the association. `JOIN FETCH` tells the
  provider to materialize that association as part of this query's
  result graph.
- 3.5 If an order has 20 lines and 10 adjustments, parallel joins can
  yield **200 rows for that one order** before Hibernate reconstructs
  collections. Add another to-many and multiply again. Fetch to-one
  paths together safely; split multiple large collections across
  queries or use batch/subselect fetching.
- 3.6 SQL pagination limits **rows**, while the API wants distinct root
  orders. A to-many join produces multiple rows per order, so applying
  limit/offset directly can cut roots or collections. Providers may
  paginate in memory or reject/warn. Use two steps: page stable order
  IDs first, then fetch the graph for those IDs; or return a projection
  designed for the page.
- 3.7 A fetch join is explicit in the query and gives tight control
  over joins/predicates but duplicates query variants. An entity graph
  overlays a reusable attribute plan onto a repository/query and keeps
  query text simpler, but generated SQL/provider behavior still needs
  inspection. Neither repairs an unbounded graph automatically.
- 3.8 Entirely different. **Association batch fetching** groups lazy
  loads of several entity/collection IDs into fewer SELECTs.
  **JDBC batching** sends many similar INSERT/UPDATE/DELETE statements
  to the driver/database together (§6). One fixes read round trips;
  the other fixes write round trips.
- 3.9 Eager is a requirement to have data initialized, not a guarantee
  of one SQL join. A provider may issue secondary selects, producing
  N+1. Global eagerness also over-fetches columns/rows and makes
  multiple collections explode. Use lazy mapping defaults plus an
  operation-specific plan.
- 3.10 Open-EntityManager-in-View (OSIV) or another still-open
  persistence context let Jackson traverse a lazy association and
  query during serialization. Disable that accidental boundary and
  build the response DTO with an explicit fetch plan inside the service
  transaction (§9).

</details>

---

## 4. Query shape, repositories & projections ⭐⭐ · 🎯 CORE PATH

- [ ] **4.1** 💭 Derived query vs JPQL `@Query` vs native SQL: when do
  you move down each level?
- [ ] **4.2** 💭 Entity result vs DTO/interface projection: compare
  selected columns, dirty checking, navigation, and API coupling.
- [ ] **4.3** 🛠 Write a DTO projection for an order list containing
  `id`, `number`, `customerName`, and `total`, without loading full
  `Order` aggregates.
- [ ] **4.4** 🐛 A closed interface projection selects three scalar
  properties, but a nested projection unexpectedly materializes a
  whole join. Why should generated SQL still be inspected?
- [ ] **4.5** 💭 Why is returning JPA entities directly from REST
  controllers dangerous beyond aesthetics?
- [ ] **4.6** 🐛 A repository builds a method name with 11 predicates
  and conditional filters. What abstraction should replace it?
- [ ] **4.7** 🏭 A JPQL query is semantically clear but the database
  chooses a bad plan. What evidence do you gather before rewriting it
  as native SQL?
- [ ] **4.8** 💭 `existsBy...` vs loading an entity/list to test
  existence — why can the former be cheaper?

<details><summary>Solutions 4</summary>

- 4.1 Use derived methods for short stable predicates. Use JPQL when
  the name becomes unreadable or you need explicit joins, aggregate,
  projection, update, or fetch strategy. Use native SQL for database
  features, complex reporting/window functions, carefully tuned plans,
  or bulk paths that ORM cannot express well. Native SQL buys control
  but costs portability and mapping simplicity.
- 4.2 Entities load managed state, participate in identity/dirty
  checking, and can navigate associations — useful for a write model.
  Projections select the view's required columns and are not a mutable
  managed aggregate — ideal for read APIs/reports. Projection shape is
  explicit, but nested projections/joins still need scrutiny.
- 4.3 A record and constructor projection:

  ```java
  record OrderSummary(Long id, String number,
                      String customerName, BigDecimal total) {}

  interface OrderRepository extends JpaRepository<Order, Long> {
      @Query("""
          select new com.acme.api.OrderSummary(
              o.id, o.number, c.name, o.total)
          from Order o
          join o.customer c
          where o.status = :status
          order by o.createdAt desc, o.id desc
          """)
      List<OrderSummary> summaries(@Param("status") OrderStatus status);
  }
  ```

- 4.4 Spring Data can optimize top-level closed projections, but nested
  paths require joins and may materialize more than the interface makes
  obvious. Projection is an intention; SQL is the evidence.
- 4.5 Serialization may trigger lazy SQL/N+1, hit
  `LazyInitializationException`, recurse through bidirectional graphs,
  leak internal columns, expose mass-assignment risks on input, and
  couple the API contract to the schema. Map request/response DTOs at a
  deliberate boundary.
- 4.6 Use a `Specification`, Querydsl/criteria, or a dedicated query
  repository that composes optional predicates. If the query is a
  report, a clear JPQL/native SQL query may be better than a generic
  abstraction. Readability and plan visibility matter more than
  avoiding all query text.
- 4.7 Capture actual SQL and bind values safely, production-like data
  cardinalities, `EXPLAIN (ANALYZE)`/vendor plan, index/statistics
  state, row estimates vs actual rows, sort/hash spill, lock/wait time,
  and network/result-mapping cost. The fix may be an index, predicate,
  statistics refresh, smaller projection, or changed fetch plan — not
  native syntax.
- 4.8 An existence query can stop at the first match and returns a
  scalar boolean/count-like result. Loading an entity transfers and
  maps columns; loading a list may scan every match. Confirm generated
  SQL because provider/version query shapes differ.

</details>

---

## 5. Pagination, scrolling & large reads ⭐⭐ · 🎯 CORE PATH

- [ ] **5.1** 💭 `Page<T>` vs `Slice<T>` vs `List<T>` with `Pageable`:
  which normally requires a total-count query?
- [ ] **5.2** 🐛 Page 1 and page 2 sometimes duplicate/skip rows even
  without application bugs. What is missing from the sort?
- [ ] **5.3** 💭 Offset vs keyset pagination: compare random-page access,
  deep-page cost, and concurrent inserts.
- [ ] **5.4** 🛠 Design a stable keyset order for events sorted newest
  first when timestamps can tie.
- [ ] **5.5** 🐛 A count query over several joins dominates response
  time even though the page content is fast. Give three options.
- [ ] **5.6** 🏭 An export calls `findAll()` for ten million rows. What
  safer processing shape would you use?
- [ ] **5.7** 💭 What resource-lifecycle rule applies to a repository
  method returning `Stream<T>`?

<details><summary>Solutions 5</summary>

- 5.1 `Page<T>` normally runs content plus **count** to provide total
  elements/pages. `Slice<T>` asks for enough rows to know whether
  another slice exists, avoiding a total. A `List<T>` with `Pageable`
  applies limit/offset without page metadata. Pick only the contract
  the client needs.
- 5.2 Pagination needs a **total, deterministic order**. Add a unique
  tie-breaker such as ID after the business sort. `ORDER BY created_at`
  alone is unstable when many rows share a timestamp.
- 5.3 Offset is simple and supports numbered/random pages, but deep
  offsets make the DB scan/skip preceding rows and concurrent changes
  cause drift. Keyset/seek pagination continues after the last sort
  key, using an index efficiently and behaving more stably under
  inserts, but does not naturally jump to arbitrary page numbers.
- 5.4 Sort by `(createdAt DESC, id DESC)` and carry both values in the
  cursor. The next predicate is logically:

  ```sql
  where created_at < :lastCreated
     or (created_at = :lastCreated and id < :lastId)
  order by created_at desc, id desc
  limit :size
  ```

  Back it with a matching composite index and protect/validate any
  cursor exposed to clients.
- 5.5 Return a `Slice` when totals are not required; supply a simpler
  explicit `countQuery`; cache/approximate totals when the product
  permits; or redesign expensive joins so the count operates on the
  root IDs/predicates. Do not claim an exact total cheaply if the
  database must recompute an expensive global answer each request.
- 5.6 Process stable keyset windows/chunks inside bounded transactions,
  project only export columns, stream with carefully configured JDBC
  fetch size where supported, and write incrementally. Avoid retaining
  all managed entities. For a large offline export, JDBC or a batch
  framework may fit better than an ORM entity graph.
- 5.7 The stream usually holds database/JPA resources and must be
  consumed within an open transaction/context and **closed** (use
  try-with-resources). Returning it past the service boundary is a
  resource leak and lazy-boundary trap.

</details>

---

## 6. Write paths, JDBC batching & bulk DML ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **6.1** 🔮 Does `saveAll(10_000)` mean one SQL INSERT? Explain
  repository looping, flush, and JDBC batching.
- [ ] **6.2** 💭 Which identifier strategy commonly prevents Hibernate
  insert batching, and why?
- [ ] **6.3** 🛠 Configure Hibernate batching and sketch a bounded
  persist/flush/clear loop.
- [ ] **6.4** 💭 What do `hibernate.order_inserts` and
  `hibernate.order_updates` trade for better batches?
- [ ] **6.5** 🐛 JPQL bulk update changes 50,000 rows, but already loaded
  entities still show old values. Why?
- [ ] **6.6** 💭 Entity-by-entity update vs bulk DML: compare callbacks,
  cascades, version checks, SQL count, and persistence-context state.
- [ ] **6.7** 🏭 A bulk import needs maximum throughput and no entity
  callbacks. When should you deliberately use JDBC instead of JPA?
- [ ] **6.8** 🐛 A loop calls `flush()` after every `persist()`. Why can
  that destroy performance?

<details><summary>Solutions 6</summary>

- 6.1 No. `saveAll` iterates entities through repository save
  semantics. Hibernate can defer SQL until flush and the JDBC driver
  may batch compatible statements when configured, but it is not one
  multi-row SQL statement by contract. Verify statement/batch counts.
- 6.2 `GenerationType.IDENTITY`: Hibernate usually must execute each
  INSERT immediately to obtain its generated ID, so it cannot group
  those inserts into normal JDBC batches. A pooled database sequence
  lets Hibernate obtain IDs ahead of time and batch writes.
- 6.3 Example configuration and loop:

  ```yaml
  spring:
    jpa:
      properties:
        hibernate.jdbc.batch_size: 50
        hibernate.order_inserts: true
        hibernate.order_updates: true
  ```

  ```java
  for (int i = 0; i < rows.size(); i++) {
      entityManager.persist(toEntity(rows.get(i)));
      if ((i + 1) % 50 == 0) {
          entityManager.flush();
          entityManager.clear();
      }
  }
  entityManager.flush();
  entityManager.clear();
  ```

  Tune batch/chunk size against driver limits, memory, transaction log,
  lock duration, and failure/restart requirements.
- 6.4 Ordering spends CPU/memory sorting pending actions and may change
  statement order. It groups same-table SQL into fuller batches and
  ordered updates can reduce inconsistent lock ordering/deadlocks.
  Measure on the actual workload.
- 6.5 Bulk JPQL/SQL executes directly in the database and **bypasses
  managed entity state**. The first-level context is stale. Clear it,
  refresh affected objects, or isolate bulk work so stale managed
  entities are never used. `@Modifying(clearAutomatically = true)` can
  help but may discard unflushed changes; flush deliberately first.
- 6.6 Entity updates honor dirty checking, entity callbacks, cascades,
  and normal optimistic versioning but require loading/tracking and
  often many statements. Bulk DML is one/few statements and avoids
  object cost, but bypasses entity callbacks/cascades and can bypass
  version semantics unless explicitly encoded; it also invalidates
  persistence-context assumptions.
- 6.7 Choose `JdbcTemplate`/`JdbcClient`, database bulk loaders, or batch
  tooling for large append/update jobs that need explicit SQL, vendor
  features, controlled batches, and no aggregate lifecycle. JPA is a
  poor tax to pay when the operation is fundamentally tabular.
- 6.8 Each flush forces synchronization, breaking the opportunity to
  accumulate a full batch and adding round trips/dirty checks. Flush at
  meaningful chunk or constraint boundaries, not after every row.

</details>

---

## 7. Optimistic/pessimistic locking & atomic updates ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **7.1** 🔮 Two transactions read balance 100, both subtract 30,
  and both write 70. Name the anomaly and show how `@Version` changes
  the outcome.
- [ ] **7.2** 💭 What SQL idea implements optimistic version checking?
- [ ] **7.3** 🏭 When an optimistic lock fails, should you retry only the
  final repository `save`, or the whole business transaction?
- [ ] **7.4** 💭 `PESSIMISTIC_READ` vs `PESSIMISTIC_WRITE`: what database
  mechanism do they usually request, and what are the costs?
- [ ] **7.5** 🛠 Declare a repository query that obtains a pessimistic
  write lock. Where must it be called?
- [ ] **7.6** 🏭 Why must a pessimistic lock never span a user interaction
  or a slow remote HTTP call?
- [ ] **7.7** 🐛 A check-then-decrement stock operation still oversells
  under load. Give an atomic SQL alternative.
- [ ] **7.8** 🏭 Deadlock vs optimistic conflict: how do retry policy and
  observability differ?

<details><summary>Solutions 7</summary>

- 7.1 It is a **lost update**. Add a version column:

  ```java
  @Version
  private long version;
  ```

  Both read version 5; the first update sets balance 70/version 6. The
  second update's `WHERE id=? AND version=5` affects zero rows, so the
  provider raises an optimistic-lock failure instead of silently
  overwriting.
- 7.2 The generated `UPDATE`/`DELETE` includes the previously read
  version in its `WHERE` clause and increments the version. Affected
  row count zero proves the snapshot was stale.
- 7.3 Retry the **whole unit of work** with a fresh persistence context:
  reread current state, re-evaluate invariants, and reapply the command.
  Retrying only `save` repeats stale state. Retry only when the
  operation and external side effects are idempotent/safely coordinated,
  with a small bounded backoff.
- 7.4 They usually map to database row locks such as `FOR SHARE` /
  `FOR UPDATE`, subject to dialect support. They serialize contenders
  and can simplify hot-resource decisions, but consume connections,
  block transactions, increase tail latency, and introduce timeouts and
  deadlocks.
- 7.5 For example:

  ```java
  interface AccountRepository extends JpaRepository<Account, Long> {
      @Lock(LockModeType.PESSIMISTIC_WRITE)
      @Query("select a from Account a where a.id = :id")
      Optional<Account> findByIdForUpdate(@Param("id") long id);
  }
  ```

  Call it inside a short active transaction; the lock's useful lifetime
  is the database transaction.
- 7.6 The connection and row lock remain occupied while a human/network
  decides. That destroys concurrency and can cascade pool exhaustion,
  timeouts, and deadlocks. Keep transactions short; use optimistic
  versioning/reservation state/workflow boundaries across remote or
  human steps.
- 7.7 Make the invariant part of one DML statement:

  ```sql
  update inventory
     set available = available - :qty
   where product_id = :id
     and available >= :qty
  ```

  Affected rows 1 means success; 0 means insufficient stock/not found.
  This avoids a race between SELECT and UPDATE and can outperform
  loading/locking an entity.
- 7.8 Both may be transient and retryable, but optimistic conflicts are
  expected contention/business concurrency; deadlocks are DB lock-cycle
  victims and may reveal inconsistent lock order or overly broad
  transactions. Track them separately, cap retries, include operation
  and safe entity/type context, and fix systematic lock ordering rather
  than hiding it behind infinite retries.

</details>

---

## 8. First-level, second-level & query caches ⭐

- [ ] **8.1** 💭 First-level vs second-level cache: scope, mandatory/
  optional status, and what each stores.
- [ ] **8.2** 🔮 Does the first-level cache make repeated
  `findByStatus(ACTIVE)` query calls free?
- [ ] **8.3** 🏭 What data is a good second-level cache candidate, and
  what data is dangerous?
- [ ] **8.4** 🐛 Another service updates the same table with JDBC, but
  this service's second-level cache returns old values. Why?
- [ ] **8.5** 💭 Why is Hibernate's query cache not equivalent to
  caching fully independent DTO responses?
- [ ] **8.6** 🏭 Before adding a cache to a slow endpoint, what should be
  measured and corrected first?

<details><summary>Solutions 8</summary>

- 8.1 First-level cache is the mandatory **per-persistence-context
  identity map of managed entities**. Second-level cache is optional,
  shared across contexts for configured entity/collection regions, and
  requires a cache provider/policy. Neither is automatically a safe
  distributed application cache.
- 8.2 No. The query normally still executes. Rows resolving to already
  managed IDs reuse those instances, but the persistence context does
  not generally remember arbitrary query result sets.
- 8.3 Good: read-mostly, bounded, reference data with clear staleness
  tolerance and high ID lookup reuse. Dangerous: hot mutable balances,
  inventory, permissions, or data updated outside Hibernate without a
  robust invalidation story. Cache by measured access pattern, not by
  entity popularity in the domain model.
- 8.4 Hibernate cannot automatically observe every external writer.
  Its cache invalidation coordinates changes it knows about; direct
  JDBC/another service can bypass that mechanism. Use ownership,
  events/invalidation, expiry, cache bypass for strong reads, or do not
  cache that data.
- 8.5 Query cache stores result-set identity/scalar information and
  coordinates with entity/cache regions and table update timestamps;
  it still may need entity-region/database resolution. An application
  DTO cache owns a complete response and explicit key/TTL/invalidation
  policy. Both can be stale, but their semantics and blast radius
  differ.
- 8.6 Measure query count, plan/index, selected data, latency breakdown,
  cardinality, connection wait, update rate, desired hit ratio, and
  acceptable staleness. Fix N+1/bad SQL/indexes first. A cache can hide
  an inefficient query until misses or invalidation storms take down
  the database.

</details>

---

## 9. Transaction boundary, OSIV & repository semantics ⭐⭐ · 🎯 CORE PATH

- [ ] **9.1** 💭 Where should the normal transaction boundary live for a
  use case spanning multiple repositories?
- [ ] **9.2** 🐛 A controller touches `order.getLines()` after the service
  transaction closes and gets `LazyInitializationException`. Give the
  real fix and the tempting workaround.
- [ ] **9.3** 💭 What does Open-EntityManager-in-View change, and which
  architectural/performance bugs can it hide?
- [ ] **9.4** 🔮 Is a Spring Data repository `save()` always needed after
  modifying a managed entity?
- [ ] **9.5** 💭 Transactional defaults for inherited CRUD reads vs
  custom declared query methods — what should you verify?
- [ ] **9.6** 🐛 A service starts a transaction, calls three repositories,
  and one repository annotation says `readOnly=true`. Which boundary
  normally controls?
- [ ] **9.7** 🏭 Why is a database transaction around a remote HTTP call
  often a performance and correctness smell?

<details><summary>Solutions 9</summary>

- 9.1 At the **service/use-case boundary** that owns the business
  invariant. One transaction can coordinate all repository work and
  define exactly when managed entities are valid. Repository methods
  remain data operations; controllers do not own persistence lifetime.
- 9.2 Fetch/project exactly what the response needs **inside the
  transaction**, then return a DTO. Join fetch, entity graph, projection,
  or explicit initialization can implement that plan. The workaround
  is OSIV, which keeps the context available through web rendering.
- 9.3 OSIV keeps an `EntityManager` bound through the web request, so
  lazy access can run after the service returns. It hides missing fetch
  plans, allows SQL during serialization/view rendering, spreads data
  access into the web layer, enables N+1 surprises, and can combine
  inconsistent reads from work outside the original transaction. Many
  teams disable it with `spring.jpa.open-in-view=false` and make query
  boundaries explicit.
- 9.4 No, not from pure JPA semantics. Managed changes are dirty-checked
  at flush. Keep `save()` when creating/merging through the repository
  abstraction or for team consistency; do not believe the call itself
  is what makes a managed update persistent.
- 9.5 Inherited `CrudRepository` read operations inherit read-only
  transaction configuration from `SimpleJpaRepository`; writes use a
  normal transaction. **Declared query methods do not automatically
  receive transaction configuration in the same way**, so declare the
  service/repository boundary you require. Never infer the active
  transaction solely from a method name.
- 9.6 The outer service transaction normally defines the actual
  boundary; inner repository attributes do not replace an already
  active `REQUIRED` transaction. Put transaction policy on the facade
  use case so it is visible and coherent.
- 9.7 A transaction may hold a pooled connection, locks, and stale
  state while an unpredictable network call waits or times out. The
  remote side cannot roll back with the local DB. Shorten the local
  transaction and coordinate via idempotency, state machines,
  outbox/events, or compensating actions. The transactions-deep kit
  owns those patterns.

</details>

---

## 10. Diagnostics & tests that expose reality ⭐⭐ · 🎯 CORE PATH

- [ ] **10.1** 💭 What evidence proves an N+1 fix worked?
- [ ] **10.2** 🐛 A `@DataJpaTest` passes without emitting the expected
  INSERT or constraint failure. Why should the test flush and clear?
- [ ] **10.3** 🏭 Why can H2 make a repository test pass while Postgres/
  Oracle production fails or performs badly?
- [ ] **10.4** 💭 What should be logged/observed in development and
  production without leaking sensitive bind values?
- [ ] **10.5** 🏭 Which pool/database metrics separate “slow SQL” from
  “waiting to obtain a connection”?
- [ ] **10.6** 💭 What does an execution plan tell you that elapsed time
  alone cannot?
- [ ] **10.7** 🛠 Design a regression test for a list endpoint that must
  stay within a fixed query budget.
- [ ] **10.8** 🏭 Why must performance tests use realistic cardinality
  and data distribution rather than merely many uniform rows?

<details><summary>Solutions 10</summary>

- 10.1 Assert/measure the number and shape of SQL statements for
  representative N, verify associations are initialized before the
  boundary closes, and compare rows/time under realistic data. “The
  exception disappeared” may only mean OSIV moved the queries.
- 10.2 The test transaction can keep entities managed and defer writes
  until rollback, masking SQL, mapping, and constraints. `flush()`
  forces synchronization; `clear()` removes the identity-map shortcut
  so the assertion reloads actual database state.
- 10.3 Dialects differ in SQL, sequences/identity, locking, isolation,
  null/order behavior, types, constraints, query planners, indexes,
  and statistics. Use Testcontainers/another real target database for
  persistence integration tests whose behavior matters.
- 10.4 In safe non-production environments: formatted SQL, statement
  counts, Hibernate statistics, query comments and carefully controlled
  binds. In production: latency histograms, slow-query samples/traces,
  query fingerprints, row counts, pool usage/wait, database waits and
  error categories. Do not log passwords, tokens, account data, or
  unrestricted bind values.
- 10.5 Hikari active/idle/pending connections, acquisition timeouts and
  connection-acquire latency distinguish pool wait; database execution
  duration, CPU/I/O, lock waits, rows examined/returned and slow-query
  telemetry describe SQL work. End-to-end duration without this split
  misdiagnoses pool starvation as a slow query.
- 10.6 The plan shows access path (index vs scan), join order/algorithm,
  estimated vs actual cardinality, sorts, loops, spills, filters and
  I/O. Those explain *why* time grows and whether an index/query rewrite
  can help. Use actual/analysis mode carefully on writes and production.
- 10.7 Seed enough parents/children to trigger lazy behavior, clear the
  context, invoke the service/list mapping, consume the DTO fully, and
  assert statement count (for example, two regardless of N) using a
  datasource proxy/statistics/statement inspector. Run against the
  production database engine and fail if the count regresses.
- 10.8 Query plans depend on selectivity, skew, correlation, nulls,
  duplicates, tenant sizes, association cardinality, and hot keys.
  Uniform synthetic data can choose a plan production would never use
  and misses contention on popular rows.

</details>

---

## 11. Senior production scenarios ⭐⭐⭐ · 🎯 CORE PATH

- [ ] **11.1** 🏭 A list endpoint is fast for 20 test orders and collapses
  at 5,000 production orders. It returns entities with two collections.
  Walk through your diagnosis order.
- [ ] **11.2** 🏭 DB CPU is low, SQL is individually fast, but request
  latency and connection demand are high. Which ORM smell fits?
- [ ] **11.3** 🏭 Turning on a second-level cache improves average latency
  but customers occasionally see old balances. What went wrong in the
  decision process?
- [ ] **11.4** 🏭 A nightly import is slow despite
  `hibernate.jdbc.batch_size=50`. IDs use identity columns and the loop
  calls `saveAndFlush`. Diagnose both batch killers.
- [ ] **11.5** 🏭 Two nodes oversell the final item even though each
  service method is `synchronized`. Why, and what database-level designs
  work?
- [ ] **11.6** 🏭 Deep page 20,000 times out. The product requires
  “next/previous,” not arbitrary page-number jumping. Redesign it.
- [ ] **11.7** 🏭 Removing a child from a collection does not delete its
  row. Which three mapping/state questions do you ask?
- [ ] **11.8** 🏭 A bulk status update succeeds, then the response shows
  old status values from entities loaded earlier in the same request.
  Fix the consistency boundary.
- [ ] **11.9** 🏭 P95 database time rose after adding a remote fraud call
  inside a transaction, even though query plans did not change. Explain.
- [ ] **11.10** 🏭 A developer proposes one giant fetch join to eliminate
  all lazy loading. How do you challenge the proposal quantitatively?

<details><summary>Solutions 11</summary>

- 11.1 Capture query count and SQL; disable/avoid OSIV masking; inspect
  whether serialization walks collections; measure returned SQL rows
  for Cartesian multiplication; examine page/count queries and actual
  plan/indexes; inspect heap/GC and pool wait. Redesign as a bounded DTO
  page, fetch one needed collection or use two-step ID paging, and load
  other collections separately/batched only when required.
- 11.2 N+1: many short round trips spend time in network/driver/pool
  coordination while no individual SQL looks slow. Query count per
  request and connection acquisitions expose it.
- 11.3 Mutable correctness-critical data was cached without a staleness,
  invalidation, ownership, and external-writer model. Average latency
  was optimized before consistency requirements were stated. Remove/
  bypass the cache for balances or design an authoritative coherence
  strategy; “short TTL” is a business trade-off, not proof of safety.
- 11.4 Identity generation forces inserts early to retrieve IDs, and
  `saveAndFlush` flushes each iteration, so statements cannot accumulate.
  Use a sequence/pooled generator where supported, ordinary persist/save
  plus chunk flush/clear, or a JDBC/database bulk path.
- 11.5 `synchronized` protects one JVM instance, not other nodes or
  writers. Use an atomic conditional update, optimistic `@Version`, or
  a short pessimistic DB lock/serialized reservation workflow. Choose
  based on contention and retry semantics.
- 11.6 Use cursor/keyset pagination over a stable indexed order plus a
  unique tie-breaker, returning opaque next/previous cursors. This avoids
  scanning/skipping 400,000 prior rows and reduces drift under inserts.
- 11.7 Is `orphanRemoval=true` on a true parent-owned association? Was
  the child removed from the managed collection and both sides made
  consistent? Is the parent managed inside a transaction that flushes?
  Also confirm the child is not intentionally shared.
- 11.8 Bulk DML bypassed the persistence context. Flush pending changes
  before the bulk operation, execute it in a clear boundary, then clear/
  refresh or return a result produced by a fresh query. Do not mix stale
  managed state with direct DML casually.
- 11.9 The transaction and perhaps a connection/locks remain open while
  waiting on the remote network. Concurrent work queues behind those
  resources, raising pool/lock latency without changing SQL execution.
  Move the remote call outside the transaction and model failure/
  idempotency explicitly.
- 11.10 Estimate cardinalities and actual SQL rows: roots × collection A
  × collection B, column width, network bytes, object count, dedup work,
  page correctness and memory. Compare with a projection, one-collection
  join plus batched second query, or two-step ID fetch. “One query” can
  be far more expensive than three bounded queries.

</details>

---

## 12. Rapid-fire trap wall 🔮 · all keep

- [ ] **12.1** Does `save()` immediately execute SQL?
- [ ] **12.2** Is flush the same as commit?
- [ ] **12.3** Does the first-level cache cache arbitrary query results?
- [ ] **12.4** Is the object passed to `merge()` made managed?
- [ ] **12.5** Does changing a managed entity require `save()`?
- [ ] **12.6** Does `LAZY` by itself prevent N+1?
- [ ] **12.7** Does `EAGER` guarantee one joined SQL query?
- [ ] **12.8** Is a normal JPQL `JOIN` automatically a fetch join?
- [ ] **12.9** Is fetch-joining two large collections usually safe?
- [ ] **12.10** Is a to-many fetch join safe with offset pagination?
- [ ] **12.11** Are cascade and fetch the same concern?
- [ ] **12.12** Does `saveAll()` mean one bulk SQL statement?
- [ ] **12.13** Does IDENTITY normally preserve insert batching?
- [ ] **12.14** Does JPQL bulk DML update managed objects in memory?
- [ ] **12.15** Does `@Version` block the second transaction?
- [ ] **12.16** Does Java `synchronized` prevent lost updates across
  service replicas?
- [ ] **12.17** Does `Page` normally avoid a count query?
- [ ] **12.18** Is deep keyset pagination normally proportional to the
  number of skipped rows?
- [ ] **12.19** Does OSIV fix a missing fetch plan?
- [ ] **12.20** Is H2 enough to prove Postgres/Oracle query behavior?

<details><summary>Solutions 12</summary>

- 12.1 **Not necessarily; write-behind often delays it until flush.**
- 12.2 **No. Flush sends SQL; the transaction may still roll back.**
- 12.3 **No. It is primarily an entity identity map.**
- 12.4 **No. State is copied to and returned as a managed instance.**
- 12.5 **No. Dirty checking persists managed changes at flush.**
- 12.6 **No. Accessing N lazy associations creates N+1.**
- 12.7 **No. The provider can use secondary selects.**
- 12.8 **No. Use `JOIN FETCH` or another fetch plan.**
- 12.9 **No. It can create a Cartesian product.**
- 12.10 **Usually no. Page roots/IDs first or use a projection.**
- 12.11 **No. Lifecycle propagation vs loading strategy.**
- 12.12 **No. It iterates; JDBC batching must be configured/possible.**
- 12.13 **No. The insert is needed to discover each ID.**
- 12.14 **No. Clear/refresh the persistence context.**
- 12.15 **No. Optimistic locking detects a stale write instead of
  blocking it up front.**
- 12.16 **No. It protects only one JVM object/instance.**
- 12.17 **No. `Page` normally adds a total-count query.**
- 12.18 **No. A matching index seeks from the last keyset.**
- 12.19 **No. It postpones the boundary and can hide N+1.**
- 12.20 **No. Use the target engine for important integration behavior.**

</details>

---

## Senior answer card — rehearse after the exercises

| Prompt | Interview-sized answer |
|---|---|
| Persistence context? | A unit-of-work identity map, dirty tracker, write-behind queue and lazy-loading scope. It guarantees one managed instance per entity identity, not cached arbitrary query results. |
| When does SQL run? | SELECT on query/lazy access; DML normally at flush. Automatic flush can occur before commit or a query affected by pending changes. Flush is not commit. |
| Entity states? | Transient, managed, detached and removed. Dirty checking applies to managed entities; `merge` returns a managed copy and leaves its argument detached. |
| N+1? | One root query plus one secondary query per root/association. Fix with a query-specific fetch join/entity graph, DTO projection, or deliberate batch/subselect fetch. |
| Why not all eager? | Eager may still issue secondary selects and globally over-fetch; multiple collections create Cartesian products. Map lazy and design fetch plans per use case. |
| Fetch join + page? | A to-many join multiplies SQL rows, while pagination wants distinct roots. Page IDs first then fetch, or use a page projection. |
| `saveAll` batching? | `saveAll` iterates; Hibernate write-behind plus configured JDBC batching may group compatible DML. IDENTITY and per-row flush defeat insert batches. |
| Bulk update trap? | Bulk JPQL/SQL bypasses managed state, callbacks/cascades and normal dirty checking. Flush first when needed, then clear/refresh and handle version semantics explicitly. |
| Optimistic lock? | `@Version` adds the old version to the write predicate. Zero updated rows detects stale state; retry the whole idempotent use case with fresh state. |
| Pessimistic lock? | A database row lock such as `FOR UPDATE` held for a short transaction. It serializes contenders but costs blocking, connections, timeouts and deadlock risk. |
| Page vs Slice? | `Page` normally adds an exact total-count query; `Slice` only determines whether more content exists. Use keyset scrolling for deep sequential navigation. |
| OSIV? | It keeps the persistence context available through web rendering, making lazy loading convenient but hiding fetch plans and permitting SQL/N+1 during serialization. Prefer explicit DTOs inside the service transaction. |
| Cache? | First-level is mandatory per context; second-level/query caches are optional and introduce staleness/invalidation complexity. Fix query shape/indexes before caching. |
| How diagnose? | Count SQL, inspect generated statements and actual execution plans, measure rows/bytes/object count, pool wait and locks, and reproduce with target DB plus realistic data distribution. |

---

## Primary references

- [Spring Data JPA — Persisting entities](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html)
- [Spring Data JPA — Query methods and entity graphs](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)
- [Spring Data — Projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html)
- [Spring Data JPA — Transactionality](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)
- [Hibernate ORM — Short guide](https://docs.hibernate.org/orm/7.1/introduction/html_single/)
- [Hibernate ORM — User guide](https://docs.hibernate.org/orm/7.1/userguide/html_single/)
- [Hibernate Query Language guide](https://docs.hibernate.org/orm/7.1/querylanguage/html_single/)

---

## Extensions — after the senior core

- Hibernate bytecode enhancement, enhanced dirty tracking and lazy
  to-one details.
- Inheritance mapping strategies and polymorphic-query performance.
- Composite/natural IDs, `@MapsId`, database-generated columns and
  custom types.
- Multi-tenancy strategies, filters and database row-level security.
- Envers/auditing, temporal/history tables and soft-delete semantics.
- Hibernate `StatelessSession`, database bulk loaders and Spring Batch
  tuning for very large jobs.
- Querydsl, advanced Criteria API, window functions/CTEs and jOOQ as a
  SQL-first alternative.
- Read replicas, routing data sources and consistency after write.
- Schema migration/index rollout with Flyway or Liquibase.

---

## How to drill this kit

1. Draw §0 and narrate when objects become managed, when SQL executes,
   and when the connection/database become involved.
2. For every query exercise, predict **statement count and row shape**
   before reading the solution.
3. Build one Postgres/Testcontainers scratch domain with Order,
   OrderLine and Customer. Reproduce N+1, a Cartesian product, an
   optimistic conflict, stale state after bulk DML, and batch inserts.
4. For each optimization, record the before/after SQL count, rows,
   execution plan and latency. “It felt faster” is not a rep.
5. Repeat §§3, 6, 7, 10 and 11 after a week; they carry most senior
   production follow-ups.

## Rep scorecard — 🟢 only after a blind aloud rep

| Block | Rep 1 | Rep 2 | Can diagnose/build? |
|---|---:|---:|---:|
| §0 mental model | ⬜ | ⬜ | ⬜ |
| §1 entity states/flush | ⬜ | ⬜ | ⬜ |
| §2 mapping/aggregates | ⬜ | ⬜ | ⬜ |
| §3 fetching/N+1 | ⬜ | ⬜ | ⬜ |
| §4 queries/projections | ⬜ | ⬜ | ⬜ |
| §5 pagination/large reads | ⬜ | ⬜ | ⬜ |
| §6 batching/bulk writes | ⬜ | ⬜ | ⬜ |
| §7 locking/concurrency | ⬜ | ⬜ | ⬜ |
| §8 caches | ⬜ | ⬜ | ⬜ |
| §9 boundaries/OSIV | ⬜ | ⬜ | ⬜ |
| §10 diagnostics/tests | ⬜ | ⬜ | ⬜ |
| §11 production scenarios | ⬜ | ⬜ | ⬜ |
| §12 trap wall | ⬜ | ⬜ | ⬜ |
