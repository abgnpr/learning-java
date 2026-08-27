# Kafka Basics — Anti-Fumble Exercises

For a backend engineer who has **run Kafka in production** (topics,
consumers, offset commits) but on autopilot. Goal: **name, explain,
and predict** Kafka's core mechanics — the log, partitions, consumer
groups, and above all the **delivery guarantees** — under interview
fire, not first contact.

**Legend** — exercise styles:
🔮 predict-the-behavior · 🛠 build/configure · 🐛 fix-the-bug · 💭 explain-the-difference

Solutions are hidden under each section. Try first, then expand. A
local single-broker Kafka (Docker `confluentinc/cp-kafka` or the
KRaft quickstart) + the `kafka-console-producer/consumer` and
`kafka-consumer-groups` CLIs are enough to *prove* most of these.

> ### 🎯 Minimum viable path (Pareto — the 20% that gates 80%)
>
> Kafka interviews and incidents cluster on a handful of ideas. Do
> the `🎯 CORE PATH` sections in this order:
>
> > **§1 → §3 → §4 → §5 → §2 → §6**
> >
> > partitions/ordering · consumer groups · offsets · **delivery
> > guarantees** · producers · rebalancing.
>
> - **§5 (delivery semantics) is THE topic.** At-most / at-least /
>   exactly-once, and *why offset-commit timing decides which one you
>   have*, is the single most-probed Kafka question. Over-invest there.
> - **Checkbox key:** `[ ]` do · `[~]` skim (low payoff right now) ·
>   `[x]` done · **keep** = do it anyway, even in a `[~]` section.
> - Everything past §6 is durability/ops breadth. The **Extensions**
>   list at the bottom is the deliberate "later" pile.

> **📖 Version caveat — ZooKeeper → KRaft.** Classic Kafka stored
> cluster metadata in **ZooKeeper**. Modern Kafka (**KRaft** mode,
> production-ready 3.3, default 3.5+, **ZooKeeper removed in 4.0**)
> runs metadata on the brokers themselves via a Raft quorum
> (controllers). Client-side, two defaults flipped in **Kafka 3.0**:
> **`enable.idempotence=true`** and **`acks=all`** became the
> producer defaults. Where a fact depends on the line it's flagged
> **`KRaft`** / **`3.0+`**. Refs point at areas of the Apache Kafka
> docs and **KDG** (*Kafka: The Definitive Guide*, Narkhede/Shapira/
> Palino) — stable topic names, not page numbers.

---

## 0. The mental model — log, topic, partition, offset · foundational (do once)

- [ ] **0.1** 💭 Kafka is "a distributed, append-only **log**," not a
  queue. What does that one framing change vs a traditional MQ
  (RabbitMQ/ActiveMQ)? (Think: what happens to a message after it's
  read?)
- [ ] **0.2** 💭 Define, one line each: **topic**, **partition**,
  **offset**, **record**. Which of these is the unit of *ordering* and
  the unit of *parallelism*?
- [ ] **0.3** 💭 A message is read by a consumer. Is it **deleted**?
  What actually decides when data leaves a partition? (Foreshadows §8.)
- [ ] **0.4** 🛠 With the CLI: create a topic with 3 partitions,
  produce 6 messages, and observe how they spread across partitions
  and where the offsets restart. What does `kafka-console-consumer
  --from-beginning` prove about persistence?
- [ ] **0.5** 💭 Broker, cluster, controller — one line each. What does
  the **controller** do, and where does it live in **`KRaft`** vs the
  old ZooKeeper world?

<details><summary>Solutions 0</summary>

- 0.1 *(ref: Kafka docs — Introduction)* A queue **hands out and
  removes** a message (consumed = gone). A **log** is a durable,
  replayable sequence: reads are non-destructive, many independent
  consumers read the same data at their own positions, and you can
  **rewind**. That's why Kafka is an event-streaming platform, not
  just a message broker — the log *is* the source of truth for a
  window of time.
- 0.2 *(ref: Kafka docs — Topics/Partitions)* **Topic** — a named
  stream of records. **Partition** — one append-only ordered log that
  a topic is split into. **Offset** — a monotonically increasing id of
  a record *within a partition*. **Record** — key + value + timestamp + headers.
  The **partition** is both the unit of **ordering**
  (order is per-partition only) and the unit of **parallelism** (one
  partition → one consumer in a group, §3).
- 0.3 *(ref: Kafka docs — Retention)* **No** — reading does not delete.
  Data leaves a partition on the **retention policy** (age or size, or
  compaction), *independently* of whether anyone consumed it. Two
  consumer groups reading the same topic don't affect each other.
- 0.4 *(ref: Kafka docs — Quickstart)* Messages with null keys spread
  across the 3 partitions (round-robin-ish, §2), and **each partition
  has its own offset sequence starting at 0** — so offset 0 exists 3
  times, once per partition. `--from-beginning` re-reads everything
  → the log persisted the records on disk, not in memory.
- 0.5 *(ref: Kafka docs — Design; KRaft)* **Broker** — a server
  holding partitions. **Cluster** — a set of brokers. **Controller** —
  the broker responsible for cluster metadata: leader elections,
  partition assignment, ISR changes. In **`KRaft`** the controller
  role runs on dedicated/co-located controller nodes via a Raft
  quorum; pre-KRaft that state lived in **ZooKeeper**.
</details>

---

## 1. Partitions & ordering ⭐⭐ · 🎯 CORE PATH (the load-bearing idea)

- [ ] **1.1** 💭 State Kafka's **ordering guarantee** precisely. Order
  is preserved across ___ but **not** across ___ . Why is that the
  central trade-off of the whole system?
- [ ] **1.2** 💭 How is a record's **partition chosen**? Give the rule
  for a **keyed** record vs a **null-key** record.
- [ ] **1.3** 🔮 You need all events for `account-42` processed **in
  order**. You use `accountId` as the key. Does this work? What breaks
  the guarantee if you later **add partitions** to the topic?
- [ ] **1.4** 🐛 A team set the message key to a **random UUID** "for
  even spread," then filed a bug that per-user events process out of
  order. Explain and fix.
- [ ] **1.5** 💭 Partition **count**: what does raising it buy you, and
  name **three** things that make it a one-way, non-trivial decision
  (hint: ordering, keys, and you can't easily *decrease* it).
- [ ] **1.6** 💭 "More partitions = more throughput" — where does that
  stop being true? Name two costs of very high partition counts.

<details><summary>Solutions 1</summary>

- 1.1 *(ref: Kafka docs — Guarantees)* Order is preserved **within a
  single partition**, **not across partitions** of a topic. That's the
  bargain: Kafka gets horizontal scale and parallelism by sharding a
  topic into independent ordered logs — so *global* ordering is
  something you deliberately give up unless a topic has one partition.
- 1.2 *(ref: Kafka docs — Producer; KDG — producers)* **Keyed:**
  `partition = hash(key) % numPartitions` (murmur2) → the **same key
  always lands on the same partition** → per-key ordering. **Null
  key:** the **sticky partitioner** (`2.4+`) batches to one partition
  then rotates — roughly even spread, no per-key affinity.
- 1.3 *(ref: Kafka docs — Partitioning)* Yes — keying by `accountId`
  routes all of account-42's events to one partition, so they're
  ordered. But **adding partitions changes `hash(key) % N`** → the
  same key may now map to a *different* partition, and in-flight/old
  events for that key can be split across old and new partitions →
  **ordering breaks across the resize**. Repartitioning is a
  correctness event, not just an ops tweak.
- 1.4 *(ref: Kafka docs — Partitioning)* A random UUID key scatters
  one user's events across **all** partitions → no per-user ordering
  (partitions have no order between them, §1.1). Fix: key by the
  **entity whose order matters** (`userId`), accepting that spread is
  now driven by key distribution, not randomness.
- 1.5 *(ref: KDG — choosing partition count)* Raising partitions adds
  **parallelism** (more consumers can work in a group, §3). It's
  sticky because: (a) it **rebreaks key→partition mapping** and
  ordering (1.3); (b) consumer-group parallelism is **capped by
  partition count**, so you size for peak; (c) Kafka lets you **add**
  partitions but **not remove** them without recreating the topic.
- 1.6 *(ref: KDG — partition count trade-offs)* It plateaus/reverses
  because: **more open file handles and memory** per broker, **longer
  leader-election/failover** time (more partitions to move), bigger
  rebalances, and more end-to-end latency from replication fan-out.
  Thousands of partitions per broker is a real ceiling.
</details>

---

## 2. Producers ⭐ · 🎯 CORE PATH

- [ ] **2.1** 💭 `acks=0` vs `acks=1` vs `acks=all` — what each waits
  for, and the durability/latency trade. What does `acks=all` need
  **`min.insync.replicas`** to actually be safe? (Ties §7.)
- [ ] **2.2** 💭 **Idempotent producer** (`enable.idempotence`,
  **default `true` in `3.0+`**): what duplicate problem does it solve,
  *mechanically* (producer id + sequence numbers), and what does it
  **not** solve?
- [ ] **2.3** 🐛 A producer sets `retries=5` and
  `max.in.flight.requests.per.connection=5` with **idempotence off**,
  and sees records land **out of order** on one partition. Explain and
  give both fixes.
- [ ] **2.4** 💭 Batching for throughput: `linger.ms`, `batch.size`,
  `compression.type`. Why does adding a *little* latency (`linger.ms`)
  often *increase* throughput?
- [ ] **2.5** 💭 `send()` is asynchronous. How do you actually **know a
  send succeeded**? Contrast the fire-and-forget, callback, and
  `.get()` (sync) styles, and what `delivery.timeout.ms` bounds.
- [ ] **2.6** 🛠 **Transactional producer** (`transactional.id`,
  `initTransactions` / `beginTransaction` / `commitTransaction`) —
  what does it make atomic that a plain producer can't? (Sets up
  exactly-once, §5.)

<details><summary>Solutions 2</summary>

- 2.1 *(ref: Kafka docs — Producer acks; KDG — reliability)*
  `acks=0` — don't wait, fastest, can silently lose data. `acks=1` —
  wait for the **leader** only; a leader crash before replication
  loses the write. `acks=all` — wait for **all in-sync replicas**.
  `acks=all` alone isn't enough: if ISR has shrunk to just the leader,
  "all" = 1. Pair it with **`min.insync.replicas=2`** (on RF=3) so a
  write **fails** rather than being under-replicated → the durable
  config.
- 2.2 *(ref: Kafka docs — Idempotent producer)* On a network hiccup a
  producer **retries**, and without dedup the broker could append the
  record **twice**. Idempotence stamps each record with a **producer
  id + per-partition sequence number**; the broker drops a duplicate
  sequence → **no duplicates from producer retries**, and ordering is
  preserved even with `max.in.flight` up to 5. It does **not**
  deduplicate across producer sessions, across application-level resend
  logic, or on the *consumer* side (that's §5).
- 2.3 *(ref: Kafka docs — Producer ordering)* With idempotence off,
  multiple in-flight batches can be **retried independently** — batch
  2 succeeds, batch 1 is retried and lands after → reorder on the
  partition. Fixes: (a) **turn idempotence on** (preserves order up to
  `max.in.flight=5`); or (b) set
  **`max.in.flight.requests.per.connection=1`** (one batch at a time —
  safe but lower throughput). Idempotence is the better fix.
- 2.4 *(ref: KDG — producer performance)* `linger.ms` waits a few ms
  to **fill a batch** instead of sending one record per request;
  `batch.size` caps a batch; `compression.type` (lz4/zstd) shrinks the
  batch on the wire. A small linger lets more records **coalesce into
  one request** → fewer round-trips, better compression ratio → higher
  throughput. Pure latency-minimizing (`linger.ms=0`) sends tiny,
  frequent, less-compressible requests.
- 2.5 *(ref: Kafka docs — Producer)* **Fire-and-forget:** call `send`,
  ignore the future — fastest, can lose on failure. **Callback:**
  `send(record, (meta, ex) -> ...)` — async, you react to
  success/exception. **Sync:** `send(record).get()` — blocks for the
  ack, simplest to reason about, slowest. `delivery.timeout.ms` bounds
  the **total** time (batching + retries) before a send is reported
  failed.
- 2.6 *(ref: Kafka docs — Transactions)* A transactional producer
  makes **writes to multiple partitions/topics — plus the consumer
  offset commit — atomic**: either all appear (to `read_committed`
  consumers) or none do. That's what enables **exactly-once
  consume-process-produce** (§5.5). Needs a stable `transactional.id`
  for fencing zombie producers.
</details>

---

## 3. Consumers & consumer groups ⭐⭐ · 🎯 CORE PATH

- [ ] **3.1** 💭 What is a **consumer group** (`group.id`)? State the
  **one partition → one consumer** rule and what it implies for
  parallelism.
- [ ] **3.2** 🔮 A topic has **4 partitions**. Predict the assignment
  when a group has **2**, **4**, then **6** consumers. What happens to
  the 5th and 6th consumer?
- [ ] **3.3** 💭 Two **different** groups subscribe to the same topic.
  How much of the data does each get? (Pub/sub vs queue framing.)
- [ ] **3.4** 💭 The **poll loop**: a Kafka consumer is
  single-threaded and pull-based. What does one `poll()` do, and why
  is "do heavy work inside the poll loop" dangerous (ties §6
  `max.poll.interval.ms`)?
- [ ] **3.5** 🐛 A service needs to process 12 partitions faster, so a
  dev creates **20 consumer instances in the same group**. Why do 8 sit
  idle, and what are the two real ways to add throughput?
- [ ] **3.6** 💭 `auto.offset.reset` = `earliest` vs `latest` vs
  `none` — what each does **when there's no committed offset**, and how
  a wrong choice causes "my new consumer skipped all the history" or
  "reprocessed everything."

<details><summary>Solutions 3</summary>

- 3.1 *(ref: Kafka docs — Consumer groups)* A consumer group is a set
  of consumers sharing a `group.id` that **cooperatively divide a
  topic's partitions** — each partition is consumed by **exactly one**
  consumer in the group at a time. So **max useful parallelism = number
  of partitions**; the group is how you scale out horizontally.
- 3.2 *(ref: Kafka docs — Assignment)* 2 consumers → 2 partitions each.
  4 consumers → 1 each (ideal). 6 consumers → 4 get one partition each,
  the **5th and 6th are idle** (no partition to assign). Consumers >
  partitions = wasted instances.
- 3.3 *(ref: Kafka docs — Consumer groups)* **Each group gets a full
  copy** of the stream — offsets are tracked **per group**. Same topic,
  two groups = two independent readers (pub/sub across groups; queue
  *within* a group). This is how you fan the same events to, say,
  billing and analytics independently.
- 3.4 *(ref: KDG — the poll loop)* `poll(timeout)` fetches a batch of
  records **and** drives heartbeats/coordination under the hood. It's
  a **single thread** per consumer. If your processing between polls
  takes longer than `max.poll.interval.ms` (default ~5 min), the group
  coordinator thinks the consumer **died** and triggers a rebalance
  (§6) — so long/blocking work in the loop causes phantom rebalances
  and duplicate processing. Offload heavy work or tune batch size.
- 3.5 *(ref: Kafka docs — Consumer groups)* Only 12 consumers can be
  active (one per partition); the other 8 have **nothing assigned**
  (3.2). Real throughput levers: (a) **add partitions** (raises the
  parallelism ceiling — but see §1.5 costs), and (b) **make each
  consumer faster** (batch processing, async I/O, a worker pool *fed
  by* the poll thread while you manage offsets carefully).
- 3.6 *(ref: Kafka docs — Consumer config)* Applies **only when no
  committed offset exists** for the group (new group, or offsets
  expired). `earliest` → start at the beginning (reprocess all
  history). `latest` → start at the end (skip everything before now).
  `none` → **throw** if no offset. New analytics group wanting history
  → `earliest`; a live-only consumer → `latest`. Picking wrong is the
  classic "why did my consumer replay 10M records" incident.
</details>

---

## 4. Offsets & commit semantics ⭐⭐ · 🎯 CORE PATH

- [ ] **4.1** 💭 What **is** a committed offset, and **where is it
  stored** (name the internal topic)? Is it "the last message I read"
  or "the next message I'll read"?
- [ ] **4.2** 💭 **Auto-commit** (`enable.auto.commit=true` +
  `auto.commit.interval.ms`) vs **manual** (`commitSync` /
  `commitAsync`). What does auto-commit silently risk?
- [ ] **4.3** 🔮🐛 **The commit-timing decision that sets your delivery
  semantics.** For each ordering, say whether a crash causes **loss**
  or **duplicates**:
  ```text
  (A) poll → commit offset → process        // commit BEFORE work
  (B) poll → process → commit offset        // commit AFTER work
  ```
- [ ] **4.4** 💭 `commitSync` vs `commitAsync` — blocking vs
  throughput, and why a common pattern is **`commitAsync` in the loop
  + `commitSync` in a `finally`/on-shutdown**.
- [ ] **4.5** 🐛 A consumer auto-commits every 5s and does slow
  processing. It crashes mid-batch. On restart it **skips** records it
  never finished. Which semantic is this, why, and how do you switch
  to at-least-once?
- [ ] **4.6** 💭 Why is Kafka's offset model **cheaper** than a broker
  tracking per-message acks (like a traditional MQ)? What does the
  consumer own that the broker doesn't?

<details><summary>Solutions 4</summary>

- 4.1 *(ref: Kafka docs — Offset management)* A committed offset is the
  group's **saved position** in a partition, stored in the internal
  **`__consumer_offsets`** topic (compacted). Convention: the committed
  offset is the **next** offset to read (i.e. last-processed + 1) — so
  on restart the consumer resumes at exactly that record.
- 4.2 *(ref: Kafka docs — Consumer config)* Auto-commit periodically
  commits the **latest polled** offset on a timer — convenient, but it
  can commit offsets for records **you haven't finished processing**,
  so a crash between the auto-commit and the work loses them
  (at-most-once-ish, §4.3/4.5). Manual commit lets you commit
  **exactly when the work is durably done**.
- 4.3 *(ref: Kafka docs — Delivery semantics)* **(A) commit before
  process → LOSS.** If it crashes after committing but before
  finishing, restart resumes *past* those records → they're never
  processed (**at-most-once**). **(B) commit after process →
  DUPLICATES.** If it crashes after processing but before committing,
  restart **reprocesses** the batch (**at-least-once**). There is no
  free lunch: you choose which failure you can tolerate, and
  at-least-once + **idempotent processing** is the usual answer (§5).
- 4.4 *(ref: KDG — commits)* `commitSync` blocks until the broker
  acks the commit (safe, retries, slower); `commitAsync` fires and
  moves on (fast, no retry — a failed async commit may be superseded).
  Pattern: `commitAsync` after each batch for throughput, then a final
  **`commitSync`** in `finally`/on shutdown so the **last** position is
  durably committed before exit.
- 4.5 *(ref: Kafka docs — Delivery semantics)* This is
  **at-most-once**: the timer committed offsets *ahead* of completed
  work, so unfinished records look done → skipped on restart (=loss).
  Switch to **at-least-once**: `enable.auto.commit=false`, and
  **commit only after** processing succeeds (manual `commitSync`), then
  make processing **idempotent** to absorb the resulting duplicates.
- 4.6 *(ref: Kafka docs — Design)* The **consumer owns its position** —
  it's just an integer per partition per group. The broker doesn't
  track per-message delivery/ack state for each consumer (which is
  expensive bookkeeping in classic MQs); it just serves a log by
  offset. Cheap, and it's what makes **replay** (rewind the offset)
  trivial.
</details>

---

## 5. Delivery guarantees ⭐⭐ · 🎯 CORE PATH (THE signature topic)

- [ ] **5.1** 💭 Define **at-most-once**, **at-least-once**, and
  **exactly-once**. Map each to a **commit-timing** choice from §4.3.
  Which is the sane default for most systems?
- [ ] **5.2** 💭 Why is **at-least-once + idempotent consumer** the
  workhorse pattern? What does "idempotent processing" mean concretely,
  and where do you usually enforce it (hint: a dedup key in *your*
  datastore, not in Kafka)?
- [ ] **5.3** 🛠 Design idempotent processing for "credit a wallet from
  an event." What key do you dedup on, and why is checking-then-writing
  in **one** DB transaction (or an upsert/unique constraint) the crux?
- [ ] **5.4** 💭 **Exactly-once semantics (EOS)** in Kafka: what's
  really guaranteed, and the sharp caveat — it holds for
  **consume→process→produce within Kafka**, not automatically for a
  write to an **external** system. Why?
- [ ] **5.5** 🛠 The EOS recipe: **transactional producer** +
  `isolation.level=read_committed` consumer +
  `sendOffsetsToTransaction`. Walk the atomic unit. When is this worth
  the complexity vs plain at-least-once + idempotency?
- [ ] **5.6** 🐛 A team claims "we set `enable.idempotence=true`, so we
  have exactly-once end-to-end." Where's the hole?

<details><summary>Solutions 5</summary>

- 5.1 *(ref: Kafka docs — Semantics)* **At-most-once** — every record
  processed 0 or 1 times; never duplicated, may be **lost** (commit
  *before* work). **At-least-once** — processed 1+ times; never lost,
  may be **duplicated** (commit *after* work). **Exactly-once** —
  effectively 1 time, no loss, no dup (needs transactions/dedup). Sane
  default: **at-least-once**, made safe with idempotency (5.2).
- 5.2 *(ref: KDG — reliable processing)* Duplicates are cheap to
  tolerate if reprocessing the *same* record yields the *same* state.
  **Idempotent processing** = applying a record twice ≡ applying it
  once. You enforce it in **your** system: a **dedup/business key**
  (event id, transaction id) checked against your datastore, or an
  **upsert**/unique constraint so a replay is a no-op. Kafka gives you
  at-least-once; *you* make it exactly-once-**effectively**.
- 5.3 *(ref: KDG — idempotency patterns)* Dedup on the **event's
  business id** (e.g. `transactionId`), not the Kafka offset. Store
  processed ids (or make the write itself keyed) and do the
  **check-and-apply in a single transaction** — or use a **unique
  constraint / upsert** so a duplicate insert fails/no-ops. If the
  check and the write aren't atomic, two deliveries can both pass the
  check then both credit (a check-then-act race). The DB constraint is
  the real guarantee.
- 5.4 *(ref: Kafka docs — EOS)* Kafka EOS guarantees that a
  **consume→transform→produce** cycle **inside Kafka** is atomic and
  exactly-once *for Kafka topics + offsets*. It can't magically make a
  side-effect on an **external** system (a REST call, a non-Kafka DB)
  part of that transaction — Kafka can't roll back your HTTP POST. For
  external writes you still need idempotency or an outbox. EOS is not
  distributed-transactions-for-everything.
- 5.5 *(ref: Kafka docs — Transactions)* The atomic unit:
  `beginTransaction()` → produce results to output topics →
  `sendOffsetsToTransaction(consumedOffsets, groupMetadata)` →
  `commitTransaction()`. The **output records and the input offset
  commit land together or not at all**, and downstream consumers with
  `isolation.level=read_committed` never see uncommitted output.
  Worth it for **Kafka-to-Kafka pipelines / streaming joins**; for a
  consumer that writes to a DB, plain at-least-once + a DB dedup key is
  simpler and usually enough.
- 5.6 *(ref: Kafka docs — Idempotent vs transactional)*
  `enable.idempotence` only dedups **producer retries within a session
  on the write path** (§2.2). It says nothing about **consumer-side**
  reprocessing (offset-commit timing, §4.3), nothing about **external
  side effects**, and isn't full transactions. "Idempotent producer" ≠
  "exactly-once end-to-end." The consumer path is the hole.
</details>

---

## 6. Rebalancing & consumer liveness ⭐ · 🎯 CORE PATH

- [ ] **6.1** 💭 What is a **rebalance**? List the triggers (member
  joins/leaves, timeout, **partition count change**, subscription
  change). Who coordinates it?
- [ ] **6.2** 💭 **Eager** (stop-the-world) vs **cooperative/
  incremental** rebalancing (`CooperativeStickyAssignor`). What does
  "stop-the-world" cost you, and what does cooperative avoid?
- [ ] **6.3** 💭 The liveness knobs: `heartbeat.interval.ms`,
  `session.timeout.ms`, `max.poll.interval.ms`. Which detects a
  **dead** consumer vs a **stuck/slow** one? Why are they separate?
- [ ] **6.4** 🐛 A consumer does a 3-minute batch job per poll and the
  group **rebalances constantly**, reprocessing records. Root cause and
  two fixes.
- [ ] **6.5** 💭 **Static membership** (`group.instance.id`): what
  rebalance does it *avoid* on a rolling restart/deploy, and why does
  that matter for a large group?
- [ ] **6.6** 💭 Why is a rebalance a **duplicate-processing risk**?
  (Tie to §4.3 — uncommitted offsets when partitions get revoked.)
  What's the `ConsumerRebalanceListener` for?

<details><summary>Solutions 6</summary>

- 6.1 *(ref: Kafka docs — Group management)* A rebalance is the group
  **re-dividing partitions among its current members**. Triggers: a
  consumer **joins** or **leaves**/crashes, a consumer misses
  timeouts, the topic's **partition count grows**, or the subscription
  changes. The **group coordinator** (a broker) orchestrates it; one
  consumer acts as group leader to compute the assignment.
- 6.2 *(ref: Kafka docs — Cooperative rebalancing)* **Eager:** every
  consumer **revokes all** its partitions, then the group reassigns
  from scratch → a **stop-the-world** pause where nobody processes.
  **Cooperative/incremental:** only the partitions that actually need
  to move are revoked; the rest **keep processing** → much smaller
  disruption. Cooperative is the modern default choice for big groups.
- 6.3 *(ref: KDG — consumer liveness)* `heartbeat.interval.ms` (bg
  thread, ~3s) + `session.timeout.ms` (~45s) detect a **dead/
  disconnected** consumer — no heartbeats → evicted.
  `max.poll.interval.ms` (~5 min) detects a **live-but-stuck** consumer
  that heartbeats yet never calls `poll()` (blocked in processing).
  Separate because "the process is alive" and "the process is making
  progress" are different failures.
- 6.4 *(ref: Kafka docs — max.poll.interval)* The 3-min job exceeds
  `max.poll.interval.ms` → coordinator declares the consumer stuck →
  rebalance → offsets uncommitted → reprocessing, repeat. Fixes: (a)
  **reduce `max.poll.records`** so each poll's work fits the interval
  (and/or **raise `max.poll.interval.ms`**); (b) **move heavy work off
  the poll thread** (poll fast, hand records to a worker pool, manage
  commits carefully). Don't just crank the timeout blindly.
- 6.5 *(ref: Kafka docs — Static membership)* With a stable
  `group.instance.id`, a consumer that restarts within
  `session.timeout.ms` **rejoins with its previous partitions** instead
  of triggering a full rebalance. On a rolling deploy of a 50-consumer
  group, that turns dozens of disruptive rebalances into near-noops.
- 6.6 *(ref: Kafka docs — Rebalance listener)* When partitions are
  **revoked** mid-processing, any records processed since the last
  commit are **uncommitted** → the new owner reprocesses them
  (duplicates, §4.3). `ConsumerRebalanceListener.onPartitionsRevoked`
  is the hook to **commit offsets / flush state before losing the
  partition**, shrinking the duplicate window.
</details>

---

## 7. Durability & replication ⭐ · mostly `[~]` (two keeps)

- [ ] **7.1** 💭 **Replication factor**, **leader**, **follower** — one
  line each. Who do producers and consumers talk to, and what do
  followers do? · **keep**
- [ ] **7.2** 💭 **ISR** (in-sync replicas): what puts a replica *in*
  vs *out* of the ISR, and why does the ISR shrink under load or
  network trouble? · **keep**
- [ ] **7.3** 🔮 RF=3, `min.insync.replicas=2`, `acks=all`. Two of the
  three replicas are down. What happens to a **produce**? To a
  **consume**?
- [ ] **7.4** 💭 **Unclean leader election** (`unclean.leader.election
  .enable`): what does enabling it trade — availability vs durability?
  Which do you pick for a payments topic?
- [~] **7.5** 💭 Consumer reads: what's the **high watermark**, and why
  can a consumer only read up to it (not the leader's very latest
  appended offset)?

<details><summary>Solutions 7</summary>

- 7.1 *(ref: Kafka docs — Replication)* **Replication factor** = copies
  of each partition across brokers. Each partition has one **leader**
  (handles all reads/writes for it) and RF−1 **followers** that
  **replicate the leader's log**. Producers and consumers talk to the
  **leader**; followers exist for failover — if the leader dies, an
  in-sync follower is promoted.
- 7.2 *(ref: Kafka docs — ISR)* A follower is **in-sync** while it has
  fetched up to (within a bound of) the leader's log end
  (`replica.lag.time.max.ms`). Fall behind — slow disk, GC, network —
  and it's **removed from the ISR** until it catches up. The ISR is the
  set of replicas eligible to be promoted and that `acks=all` waits
  for.
- 7.3 *(ref: Kafka docs — min.insync.replicas)* Only 1 replica is in
  the ISR (< `min.insync.replicas=2`), so a produce with `acks=all`
  **fails** with `NotEnoughReplicas` — the write is refused to protect
  durability. **Consumes can still succeed** from the surviving leader
  (reads don't require min ISR). This is the durable-by-design
  behavior: refuse under-replicated writes rather than risk loss.
- 7.4 *(ref: Kafka docs — Unclean leader election)* If **no** in-sync
  replica is available, unclean election lets an **out-of-sync**
  replica become leader → the cluster stays **available** but **loses**
  the records that replica never got. Disabled = stay **consistent/
  durable**, refuse to elect, partition goes offline until an ISR
  member returns. For a **payments topic: keep it disabled** — losing
  committed money-events is worse than unavailability.
- 7.5 *(ref: Kafka docs — Replication)* The **high watermark** is the
  highest offset **replicated to all ISR members**. Consumers can only
  read **up to the high watermark**, never the leader's newest
  un-replicated record — so you never consume a record that could
  vanish if the leader failed before replication. Consistency for
  readers.
</details>

---

## 8. Retention, compaction & cleanup · `[~]` awareness

- [~] **8.1** 💭 **Time** (`retention.ms`) vs **size**
  (`retention.bytes`) retention. Is retention about *consumption* at
  all? What happens to a consumer whose offset falls off the end?
- [~] **8.2** 💭 `cleanup.policy=delete` vs **`compact`**. What does
  **log compaction** keep, and what's the canonical use (a changelog /
  "latest state per key" topic)?
- [~] **8.3** 🔮 A compacted topic gets key `A=1`, `A=2`, `A=3`, then
  `A=null` (a **tombstone**). After compaction, what remains for `A`?
- [~] **8.4** 💭 Why does compaction make Kafka usable as a **source of
  truth for state** (e.g. Kafka Streams state stores, `__consumer_
  offsets` itself)?

<details><summary>Solutions 8</summary>

- 8.1 *(ref: Kafka docs — Retention)* Retention deletes **old log
  segments** by age (`retention.ms`) or total size
  (`retention.bytes`) — **independent of whether anyone consumed
  them**. If a slow consumer's committed offset points at data already
  deleted, it hits `auto.offset.reset` (§3.6) → jumps to earliest/
  latest → **gap or replay**. That's the "consumer lag fell off the
  retention window" incident.
- 8.2 *(ref: Kafka docs — Log compaction)* `delete` throws away old
  records wholesale. **`compact`** keeps **at least the latest value
  for each key**, garbage-collecting superseded versions — so the topic
  becomes a **snapshot of current state per key** while still being a
  log. Canonical use: changelog/state topics, CDC, config topics.
- 8.3 *(ref: Kafka docs — Tombstones)* A **tombstone** (key with
  `null` value) signals **deletion**; after compaction (and past
  `delete.retention.ms`) **nothing remains for `A`** — the key is
  removed. That's how compacted topics represent deletes.
- 8.4 *(ref: Kafka docs — Compaction)* Because a consumer can rebuild
  the **entire current state** by replaying a compacted topic from the
  start (bounded by #keys, not #events) — you don't need the full
  history, just the latest per key. That's exactly how stream
  processors restore state stores and how Kafka stores group offsets.
</details>

---

## 9. Error handling & DLQ · `[~]` (one keep — the poison-pill idea)

- [ ] **9.1** 💭 A single **"poison" record** (unparseable / always
  throws) sits at the head of a partition. Why can it **block the whole
  partition**, and what are your options? · **keep**
- [~] **9.2** 💭 Kafka has **no native DLQ**. What is the DLQ *pattern*
  (a separate topic), and who implements it (the client/framework, not
  the broker)?
- [~] **9.3** 💭 **Blocking retry** (retry in place, delays the
  partition) vs **non-blocking retry** (retry topics, e.g. Spring's
  `@RetryableTopic`). The trade-off in ordering vs throughput.
- [~] **9.4** 💭 Why must retry/DLQ logic be paired with **idempotency**
  (§5.2)? What does a retry imply about duplicate delivery?

<details><summary>Solutions 9</summary>

- 9.1 *(ref: KDG — handling errors)* Ordering within a partition means
  the consumer can't just "skip ahead" without either processing or
  **setting aside** the bad record — retrying it forever **stalls every
  record behind it** on that partition (head-of-line blocking).
  Options: **skip + route to a DLQ** after N attempts, **pause and
  alert**, or fix-and-replay. You must decide: block for order, or
  divert for liveness.
- 9.2 *(ref: Kafka docs / Spring Kafka — DLT)* The broker has no DLQ
  concept. The **pattern**: after retries are exhausted, the
  **consumer/framework publishes the failed record to a separate
  "dead-letter" topic** (with headers about the failure) and commits
  the original offset so the partition proceeds. It's **client-side**
  behavior (Spring Kafka's `DeadLetterPublishingRecoverer`, Connect's
  DLQ, etc.).
- 9.3 *(ref: Spring Kafka — @RetryableTopic)* **Blocking:** retry the
  same record in place with backoff — preserves order but **halts the
  partition** during the delay. **Non-blocking:** immediately forward
  failures to timed **retry topics** and move on — keeps the main
  partition flowing at the cost of **losing strict ordering** for the
  retried records. Choose per topic: order-critical → blocking;
  throughput-critical → retry topics.
- 9.4 *(ref: §5.2)* Any retry (in-place, retry-topic, or a rebalance
  reprocess) means a record can be **delivered/processed more than
  once**. Without idempotent processing, retries turn transient errors
  into **duplicate side effects** (double credit, double email). Retry
  and DLQ machinery only makes sense on top of at-least-once +
  idempotency.
</details>

---

## 10. Spring Kafka (the Spring lens) · `[~]` for the fundamentals track, **do** if the JD is Spring

- [ ] **10.1** 🛠 Produce with **`KafkaTemplate`** and consume with
  **`@KafkaListener`**. What does Spring Boot auto-configure for you
  (ties the Spring auto-config idea), and where do you set
  `group.id`/serializers?
- [ ] **10.2** 💭 `ConcurrentKafkaListenerContainerFactory` —
  **`concurrency = N`** spins up N consumer threads. Why is N **capped
  by partition count** (§3.1)? What does concurrency actually map to?
- [ ] **10.3** 💭 Container **ack modes**: `RECORD`, `BATCH` (default),
  `MANUAL`, `MANUAL_IMMEDIATE`. How do these map onto the
  commit-timing/delivery-semantics choice (§4.3)?
- [ ] **10.4** 🛠 `DefaultErrorHandler` with a backoff +
  `DeadLetterPublishingRecoverer` → a `.DLT` topic. Sketch the wiring;
  contrast with `@RetryableTopic` for non-blocking retries (§9.3).
- [ ] **10.5** 💭 `KafkaTransactionManager` /
  `@Transactional` producer in Spring — what does it buy, and the
  honest caveat about mixing a Kafka transaction with a **DB**
  transaction (there's no true XA; it's best-effort / needs the outbox
  pattern).

<details><summary>Solutions 10</summary>

- 10.1 *(ref: Spring for Apache Kafka — reference)* With
  `spring-kafka` + `spring-boot-starter`, Boot auto-configures a
  `KafkaTemplate`, producer/consumer factories, and the listener
  container infra from `spring.kafka.*` properties (bootstrap servers,
  `consumer.group-id`, key/value serializers/deserializers). Produce
  via `kafkaTemplate.send(topic, key, value)`; consume by annotating a
  method `@KafkaListener(topics="t", groupId="g")`.
- 10.2 *(ref: Spring Kafka — concurrency)* `concurrency=N` creates N
  listener threads, each an independent consumer in the group — so
  they divide the partitions like any group (§3.1). If N > partitions,
  the extra threads get **no partitions** and idle. Concurrency maps to
  **consumers in the group**, not to threads-per-partition.
- 10.3 *(ref: Spring Kafka — ack modes)* `RECORD` commits after **each**
  record processed (tighter at-least-once). `BATCH` (default) commits
  after the **whole poll batch** is processed. `MANUAL`/
  `MANUAL_IMMEDIATE` hand you an `Acknowledgment` to commit **when your
  code says** — this is how you implement precise at-least-once (commit
  after the side effect is durable, §4.3). Ack mode **is** the
  commit-timing lever in Spring.
- 10.4 *(ref: Spring Kafka — error handling)* Wire a
  `DefaultErrorHandler(new DeadLetterPublishingRecoverer(template),
  new FixedBackOff(interval, maxAttempts))` on the container factory:
  it retries with backoff, then **publishes to `<topic>.DLT`** and
  moves on (blocking retry, §9.3). For **non-blocking** retries use
  `@RetryableTopic` — Spring creates timed retry topics + a DLT so the
  main partition keeps flowing. Blocking preserves order; retry topics
  preserve throughput.
- 10.5 *(ref: Spring Kafka — transactions)* A transactional
  `KafkaTemplate` makes multi-record sends (and consumed-offset
  commits) atomic on the Kafka side (§2.6/§5.5). The caveat: a Kafka
  transaction and a **JDBC** transaction are **two different
  resource managers** — Spring can *chain* them but there's no true
  distributed (XA) atomicity, so a crash between the two commits can
  leave them disagreeing. The robust answer for "DB write + Kafka
  publish atomically" is the **transactional outbox** pattern (write
  the event to a DB table in the same tx, relay to Kafka separately),
  not a two-resource transaction.
</details>

---

## 11. Rapid-fire gotchas 🔮 · all **keep**

Predict/answer each, then verify on a local cluster where you can:

- [ ] **11.1** A topic has 1 partition and 3 consumers in one group.
  How many are actively consuming?
- [ ] **11.2** You produce 5 records with **no key** to a 3-partition
  topic and need them consumed in produce-order. Guaranteed?
- [ ] **11.3** `acks=all` but `min.insync.replicas=1` on an RF=3 topic.
  A leader-only write succeeds, then the leader dies before any
  follower replicated. Data safe?
- [ ] **11.4** Consumer group `A` has been offline for a week;
  `retention.ms` is 3 days. It comes back with a committed offset. What
  does it read?
- [ ] **11.5** You add partitions to a keyed topic to scale. What
  silently breaks for existing keys?
- [ ] **11.6** `enable.auto.commit=true`, processing throws an
  exception, but the loop keeps polling. Loss or duplicate on the
  thrown record?
- [ ] **11.7** Two consumers accidentally share the **same
  `group.id`** but subscribe to **different** topics. Any problem?
- [ ] **11.8** You call `producer.send(record)` and the app exits
  immediately (no flush/close). Did the record get delivered?

<details><summary>Solutions 11</summary>

- 11.1 *(ref: §3.1)* **One** — a partition maps to exactly one consumer
  per group; the other two idle. Parallelism is capped by partitions.
- 11.2 *(ref: §1.1)* **No.** Null keys spread across 3 partitions, and
  there is **no ordering across partitions**. Guaranteed order needs
  them in **one** partition (same key, or a 1-partition topic).
- 11.3 *(ref: §7.3/7.4)* **Not safe.** `min.insync.replicas=1` lets
  `acks=all` be satisfied by the **leader alone**; if it dies
  pre-replication, the record is **lost** (and unclean election could
  seat a replica that never had it). Durable config is
  `min.insync.replicas=2` on RF=3.
- 11.4 *(ref: §8.1/3.6)* Its committed offset points at data **already
  deleted** by retention → on fetch it triggers `auto.offset.reset`:
  `earliest` → jump to oldest surviving record (a **gap**, missed
  ~4 days), `latest` → jump to now (miss everything). Either way it
  **cannot** get the expired records back.
- 11.5 *(ref: §1.3)* `hash(key) % N` changes with N → existing keys can
  **remap to different partitions**, so **per-key ordering breaks**
  across the resize and a key's history is split across old+new
  partitions.
- 11.6 *(ref: §4.2/4.5)* Likely **loss** (at-most-once flavor):
  auto-commit advances the offset on its timer regardless of the
  exception, so the failed record's offset can get committed and the
  record is **never retried**. Manual commit + error handling (§10.4)
  is the fix.
- 11.7 *(ref: §3)* Fine in principle — group membership is per the
  partitions they're assigned; different topics → disjoint
  assignments. But it's a **smell**: offset/lag tooling and future
  subscription changes get confusing, and a shared group can rebalance
  each other. Use distinct `group.id`s per logical consumer.
- 11.8 *(ref: §2.5)* **Maybe not.** `send()` is async and buffers;
  exiting without `flush()`/`close()` can drop **unsent batches** still
  in the accumulator. Always `flush`/`close` the producer on shutdown
  to drain the buffer.
</details>

---

## Extensions — the deliberate "later" pile

This doc is the Pareto core (log, partitions, groups, offsets,
delivery guarantees). Each bullet is a **candidate for its own drill
file** — pull one in when a JD, an interview, or a real task demands
it. Rough priority top-to-bottom for a backend/payments track.

- **`kafka-exactly-once-deep.md`** — transactions internals (producer
  fencing, `transactional.id`, epoch), `read_committed` mechanics,
  EOS end-to-end limits, the outbox & CDC (Debezium) patterns for
  "DB + Kafka atomically."
- **`kafka-streams-basics.md`** — `KStream`/`KTable`, stateful
  processing, windowing, joins, state stores + changelog topics,
  `processing.guarantee=exactly_once_v2`, interactive queries.
- **`kafka-schema-evolution.md`** — Schema Registry, Avro/Protobuf,
  backward/forward/full compatibility, why schemas beat JSON-blobs at
  scale, serializer/deserializer config.
- **`kafka-connect.md`** — source/sink connectors, SMTs, the DLQ in
  Connect, when Connect beats a hand-rolled consumer (CDC, S3 sink,
  JDBC sink).
- **`kafka-ops-and-lag.md`** — consumer **lag** monitoring
  (`kafka-consumer-groups`, Burrow), partition/throughput capacity
  planning, reassignment, rack awareness, quotas, JMX metrics.
- **`kafka-security.md`** — TLS in transit, SASL (PLAIN/SCRAM/
  OAUTHBEARER/Kerberos), ACLs/authorization, encryption at rest,
  multi-tenant hardening.
- **`kafka-tuning.md`** — producer/consumer/broker tuning knobs,
  `linger.ms`/`batch.size`/`fetch.min.bytes`, compression choices,
  page-cache & disk considerations, zero-copy.
- **`kafka-kraft-internals.md`** — KRaft controller quorum, metadata
  log, migration from ZooKeeper, controller failover — only if a JD or
  platform role names it.
- **`kafka-multicluster.md`** — MirrorMaker 2, cross-region
  replication, disaster recovery, active-active/active-passive,
  tiered storage.

---

## How to work through this

1. Run a **local single-broker Kafka** and lean on three CLIs:
   `kafka-topics`, `kafka-console-producer/consumer`, and
   **`kafka-consumer-groups`** (it shows offsets and **lag** — the
   single most useful debugging view). Prove the §11 gotchas on it.
2. **Predict before you run** every 🔮. For 💭 items, **say the answer
   aloud** in a sentence or two — articulation is the actual interview
   skill, same law as the Go and Spring drills.
3. Do the `🎯 CORE PATH` first: **§1 → §3 → §4 → §5 → §2 → §6**, and
   **re-run §4 and §5** after a week — commit timing and delivery
   guarantees are the highest-probed, fastest-fading ideas.
4. Only pull an **Extensions** file when something real (a JD, a
   round, a task) demands it — don't build breadth for its own sake.

**The three things an interviewer is really testing:** (1) do you
understand the **log/partition model** and that **ordering is
per-partition** (§1); (2) do you understand **consumer groups +
offsets** — parallelism and position (§3, §4); (3) can you reason
about **delivery guarantees** — that **commit timing** is the knob,
at-least-once + idempotency is the workhorse, and exactly-once has
sharp edges (§5). Own those three and the rest is vocabulary.
</content>
