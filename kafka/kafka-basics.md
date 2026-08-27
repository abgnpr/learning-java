# Kafka Rapid-fire Kit ⚓

- Kafka Rapid-fire Kit
  - [§1 The model — a log, not a queue](#1-the-model--a-log-not-a-queue)
  - [§2 Topic, partition, offset](#2-topic-partition-offset)
  - [§3 The cluster — brokers, replication, durability](#3-the-cluster--brokers-replication-durability)
  - [§4 The producer](#4-the-producer)
  - [§5 Consumers, groups and offsets](#5-consumers-groups-and-offsets)
  - [§6 Delivery guarantees](#6-delivery-guarantees)
  - [§7 Rebalancing & consumer liveness](#7-rebalancing--consumer-liveness)
  - [§8 Retention, compaction, lag and failure handling](#8-retention-compaction-lag-and-failure-handling)
  - [§9 Spring Kafka — the same ideas, wired](#9-spring-kafka--the-same-ideas-wired)
  - [§10 Why Kafka is fast](#10-why-kafka-is-fast)
  - [§11 IMPS — the anchor ⚓](#11-imps--the-anchor-)
  - [§12 Rapid-fire traps](#12-rapid-fire-traps)

**Why this exists:** Kafka is the flagship line on the profile — the
IMPS switch runs on it — so a Java round that sees "Kafka" on the CV
probes it properly rather than asking one definition and moving on.
Every term used is defined inside this kit; nothing here defers to
another doc.

**How to drill:** aloud, blind, one section per sitting. Answers are
sized for SPEAKING — say the 1–3 lines, then stop talking. Anchors to
the IMPS switch are marked ⚓; never claim beyond them.

> ### 🎯 The 80/20 — what actually gets asked
>
> Kafka interviews cluster hard. Three ideas plus one story carry
> most rounds; the rest is vocabulary that only comes up once the
> first three land.
>
> **Tier 1 — own these cold (≈70% of Kafka questions)**
>
> > **[§2](#2-topic-partition-offset) →
> > [§5](#5-consumers-groups-and-offsets) →
> > [§6](#6-delivery-guarantees) →
> > [§11](#11-imps--the-anchor-)**
> >
> > partitions & ordering · groups & offsets · **delivery
> > guarantees** · the IMPS story
>
> - **§2** — the partition is both the unit of **ordering** and the
>   unit of **parallelism**; the key picks the partition. Every other
>   Kafka design question resolves to this sentence.
> - **§5** — a group divides partitions one-owner-each; offsets live
>   in Kafka per group per partition and mean **next to read**.
> - **§6 is THE topic** — at-most / at-least / exactly-once, and that
>   **commit timing is the knob** that picks between them. Over-invest
>   here; it's the most-probed Kafka question in existence.
> - **§11** — the IMPS answer, including the honest "offsets were
>   committed at receipt, and I'd change that." A candidate who names
>   their own design flaw outranks one who recites semantics.
>
> **Tier 2 — the follow-ups when Tier 1 lands well (≈25%)**
>
> > **[§7](#7-rebalancing--consumer-liveness) →
> > [§3](#3-the-cluster--brokers-replication-durability) →
> > [§8](#8-retention-compaction-lag-and-failure-handling)**
> >
> > rebalancing & the three timeout knobs · `acks` +
> > `min.insync.replicas` · retention, lag, poison/DLQ
>
> These separate "used Kafka" from "operated Kafka." For a payments
> track, `acks=all` + `min.insync.replicas=2` on RF=3 and the
> poison-message answer are the two most likely of the set.
>
> **Tier 3 — breadth, skim unless the JD names it (≈5%)**
>
> > [§4](#4-the-producer) producer internals ·
> > [§9](#9-spring-kafka--the-same-ideas-wired) Spring Kafka (**do it
> > if the JD says Spring**) · [§10](#10-why-kafka-is-fast) why it's
> > fast · [§1](#1-the-model--a-log-not-a-queue) log-vs-queue framing
> > (cheap — five minutes, and it's the best opening sentence you can
> > give) · [§12](#12-rapid-fire-traps) traps as a final self-test
>
> **The five sentences.** If there is time for nothing else:
>
> 1. Kafka is a replayable append-only log, not a queue — reading
>    deletes nothing; retention does.
> 2. Order is guaranteed within a partition, never across; the key
>    decides the partition.
> 3. A consumer group splits partitions one-owner-each, so partition
>    count caps parallelism.
> 4. Commit before processing loses, commit after processing
>    duplicates — that choice *is* your delivery guarantee.
> 5. At-least-once plus an idempotent consumer keyed on a business id
>    is what production actually runs. ⚓ IMPS dedupes in the Oracle
>    journal, not in Kafka.

---

## §1 The model — a log, not a queue

- **What is Kafka?** — a distributed, durable, **replayable
  append-only log**. Producers append records to topics; consumers
  read them at their own pace by position. It decouples who writes
  from who reads, in time as well as in code.
- **Log vs queue — the one framing that matters** — the single most
  common misconception is "Kafka is a queue."
  - a **queue** *hands out and removes*: one consumer takes a
    message, it's gone
  - a **log** is a *durable sequence of records*: a read is
    **non-destructive**, many independent readers hold their own
    position in the same data, and any of them can **rewind**
  - **reading never deletes anything.** Data leaves Kafka on
    **retention** (age or size) or **compaction** — never because
    somebody consumed it
  - *Say it as:* a queue is a hand-off; a log is a **record of what
    happened** that many parties read independently.
- **Then can it act like a queue?** — yes, and both shapes come from
  one knob, the `group.id`:
  - consumers in the **same group** split the partitions → work is
    load-shared, each record handled once by the group → **queue
    semantics**
  - consumers in **different groups** each get the **full stream** →
    **pub/sub broadcast**
  - so "queue or pub-sub?" = "same group or different groups?"
- **Kafka vs a traditional MQ (ActiveMQ / RabbitMQ)?**

    | | Kafka | Classic MQ |
    | --- | --- | --- |
    | delivery | consumer **pulls** | broker **pushes** |
    | after consume | record **stays** (retention) | **deleted** on ack |
    | position | **consumer** tracks its offset | **broker** tracks per-message acks |
    | replay | rewind the offset | gone |
    | scale-out | add **partitions** | add consumers, no ordering unit |
    | routing | key → partition | exchanges/bindings, per-message routing |

  - Why the offset model is cheaper: the broker keeps **one integer
    per partition per group**, not per-message delivery state for
    every consumer. That's what makes replay free.
  - What you give up: no per-message ack/redelivery, no priority
    queues, no selective consumption — Kafka gives you a sequence,
    and order is the price of parallelism.

- **Record anatomy?** — a Kafka record is four things:
  - **key** (optional) — decides the partition, hence ordering
  - **value** — the payload (bytes; JSON/Avro/Protobuf after
    serialization)
  - **timestamp** — event time or append time
  - **headers** — key/value metadata (trace ids, retry counts,
    failure reasons on a DLQ record)

## §2 Topic, partition, offset

- **Topic / partition / offset?**
  - **topic** — a named stream of records
  - **partition** — one append-only, strictly ordered log that a
    topic is split into
  - **offset** — a record's position **within a partition**, a
    monotonically increasing integer
  - the **partition is both units**: the unit of **ordering** and the
    unit of **parallelism**. Everything else in Kafka follows from
    that one sentence.

  **The mental picture:** a topic is a set of parallel logs, each one
  strictly append-only, each with its own counter.

  ```text
  Topic: outward-reqpay
   ├─ Partition 0: [msg0][msg1][msg2][msg3] offset→ 4
   ├─ Partition 1: [msg0][msg1]            offset→ 2
   └─ Partition 2: [msg0][msg1][msg2]      offset→ 3

  key = txn-ref → hash(key) % numPartitions → always same partition
  ```

  - offsets are **per partition**, so offset 0 exists once in every
    partition — an offset is meaningless without naming its partition
  - Why keyed: two events for the same transaction ref must land in
    the same partition to preserve their order relative to each
    other — Kafka orders *within* a partition, never across.
- **State the ordering guarantee precisely** — order is preserved
  **within a single partition**, and **not** across partitions of a
  topic.
  - global ordering across a topic exists only if the topic has
    **one partition** — which caps you at one consumer
  - so the real design question is never "how do I get ordering?" but
    **"what is the smallest thing that must stay ordered?"** — pick
    that as the key
- **What sets `numPartitions`?** — the **topic**, fixed at creation.
  Not the producer, not the key, not the size of the consumer group;
  producers just read the count out of topic metadata.
  - **Declared** — `kafka-topics.sh --create --topic outward-reqpay
    --partitions 3 --replication-factor 3`, or a `NewTopic` bean that
    `KafkaAdmin` applies at startup.
  - **Auto-created** — with broker `auto.create.topics.enable=true`,
    the first producer to touch an unknown topic gets one at the
    broker default `num.partitions`, which is **1**. The trap: no
    error, no parallelism, one consumer doing everything.
  - **Altered** — `--alter --partitions 6`. Up only; Kafka never
    shrinks a topic, because the extra logs have data in them.
  - Raising it **breaks key stickiness**: the mapping is
    `hash(key) % numPartitions`, so changing the divisor sends
    `txn-ref-42` to a new partition while its earlier events sit in
    the old one. Per-key ordering holds on each side of the resize
    and not across it — repartitioning a keyed topic is a
    **correctness event**, not an ops tweak.
- **Which partition does a record go to?** — decided **client-side**,
  by the producer, before anything reaches a broker. Four rules,
  checked in order:

  ```text
  1. explicit partition on the record  → that one
  2. key != null                       → murmur2(keyBytes) % numPartitions
  3. key == null                       → sticky: one partition per batch
  4. partitioner.class set             → your Partitioner decides
  ```

  - **Explicit** — `new ProducerRecord<>(topic, 1, key, value)` pins
    partition 1 and overrides the rest. Rare outside tests.
  - **Keyed** — deterministic across every producer and every JVM:
    `txn-ref-42` lands on the same partition every time, as long as
    `numPartitions` holds. It hashes the **serialized bytes**, not
    the object, so a different serializer routes the same logical key
    somewhere else.
  - **Keyless** — not round-robin per record, the usual
    misconception. The producer is **sticky**: it fills one partition
    until the batch closes (`batch.size` reached or `linger.ms`
    elapsed), then moves to another. Fewer, fuller batches — higher
    throughput, spread that evens out over time rather than
    alternating record by record.
  - *Say it as:* explicit partition wins; a key means
    `hash(key) % partitions` and buys same-key-same-partition
    ordering; no key means sticky batching for throughput.
- **🐛 The random-UUID key bug** *(classic)* — a team keys by random
  UUID "for even spread," then reports per-customer events processing
  out of order.
  - the UUID scatters one customer's events across **all**
    partitions, and partitions have no order between them
  - fix: key by the **entity whose order matters** (`customerId`,
    `txn-ref`), and accept that spread is driven by key distribution
  - the corollary: a **hot key** (one customer with 90% of traffic)
    creates a hot partition, and no amount of extra partitions helps
- **How many partitions should a topic have?**
  - the floor: **partition count is the ceiling on consumer-group
    parallelism** — 3 partitions means at most 3 useful consumers
  - so size it for **peak** parallelism, not today's
  - costs of going very high: more **open file handles and memory**
    per broker, **slower leader election / failover** (more
    partitions to move), **longer rebalances**, more replication
    fan-out latency
  - *One-liner:* partitions buy parallelism, cost failover time, and
    are practically one-way once the topic is keyed.

## §3 The cluster — brokers, replication, durability

- **Broker / cluster / controller?**
  - **broker** — a server that hosts partitions and serves reads and
    writes
  - **cluster** — a set of brokers
  - **controller** — the broker responsible for cluster metadata:
    leader elections, partition assignment, ISR changes
- **ZooKeeper vs KRaft?** — classic Kafka kept cluster metadata in
  **ZooKeeper**; modern Kafka runs it on the brokers themselves via a
  Raft quorum (**KRaft**) — production-ready in 3.3, default in 3.5,
  ZooKeeper removed in **4.0**. One less system to operate.
- **Replication — leader, follower, ISR?**
  - **replication factor (RF)** — how many copies of each partition
    exist, on different brokers
  - each partition has exactly one **leader** (serves all reads and
    writes for it) and RF−1 **followers** that replicate the leader's
    log; followers serve no clients, they exist for failover
  - **ISR (in-sync replicas)** — the replicas caught up with the
    leader within `replica.lag.time.max.ms`. A follower that falls
    behind (slow disk, GC pause, network) is **dropped from the ISR**
    until it catches up
  - a new leader is elected **from the ISR** when the old one dies —
    which is why the ISR, not RF, is the number that decides whether
    you can survive a failure
- **`acks` config?** *(MCQ staple)* — the producer's durability knob:

    | `acks` | waits for | can lose when |
    | --- | --- | --- |
    | `0` | nobody | any hiccup |
    | `1` | leader's write | leader dies before replicating |
    | `all` | every in-sync replica (ISR) | — (slowest) |

  - default is **`acks=all`** on the 3.0+ line (it was `1` before)
- **The `acks=all` trap — `min.insync.replicas`** *(the follow-up
  that separates users from operators)*
  - `acks=all` waits for every replica **currently in the ISR** — and
    if the ISR has shrunk to just the leader, "all" means **one**
  - **`min.insync.replicas=2`** on RF=3 makes the broker **reject**
    the write (`NotEnoughReplicas`) rather than accept an
    under-replicated one
  - the durable payments config is the pair: **RF=3, `acks=all`,
    `min.insync.replicas=2`** — it trades availability for never
    silently losing an acknowledged write
  - reads are unaffected: a produce fails under min-ISR, a consume
    still succeeds from the surviving leader
- **High watermark?** — the highest offset replicated to **all** ISR
  members. Consumers can read **only up to it**, never the leader's
  newest un-replicated record — so you can never consume a record
  that would vanish if the leader died.
- **Unclean leader election?** — `unclean.leader.election.enable`.
  If **no** in-sync replica survives, enabling it promotes an
  **out-of-sync** replica: the partition stays **available** but
  **loses** the records that replica never received.
  - disabled (the default) = stay durable, partition goes offline
    until an ISR member returns
  - for a payments topic: **keep it disabled** — losing committed
    money events is worse than being unavailable

## §4 The producer

- **Is `send()` synchronous?** — no. `send()` appends to an in-memory
  buffer and returns a `Future`; a background I/O thread batches and
  ships it. Three styles:
  - **fire-and-forget** — `send(record)`, ignore the result. Fastest,
    silently loses on failure.
  - **callback** — `send(record, (meta, ex) -> …)`. Async, and you
    react to the outcome. The usual production choice.
  - **sync** — `send(record).get()`. Blocks for the ack; simplest to
    reason about, slowest.
  - `delivery.timeout.ms` bounds the **total** time — batching plus
    retries — before a send is reported failed.
- **🐛 The app exits and the record vanishes** — `send()` buffers, so
  a JVM that exits without `flush()` / `close()` drops whatever is
  still in the accumulator. Always close the producer on shutdown.
- **Batching — why adding latency raises throughput**
  - `linger.ms` — wait this long to let a batch fill (0 by default on
    the 3.x line)
  - `batch.size` — cap per batch (16 KB default)
  - `compression.type` — `lz4` / `zstd`, applied per batch
  - a few ms of linger lets records **coalesce into one request**:
    fewer round-trips, and a bigger batch compresses better. Pure
    latency-minimizing sends tiny, frequent, poorly-compressing
    requests.
- **Idempotent producer?** — `enable.idempotence`, **default `true`
  since 3.0**.
  - the problem: a producer retries after a network hiccup, and the
    broker appends the same record **twice**
  - the mechanism: each record carries a **producer id + per-partition
    sequence number**; the broker drops a sequence it has already seen
  - what it covers: **duplicates from producer retries, within one
    producer session**, while preserving order
  - what it does **not** cover: duplicates across producer restarts,
    application-level resends, or anything on the **consumer** side —
    which is where end-to-end duplicates actually come from
- **🐛 Records landing out of order on one partition** — a producer
  with `retries=5`, `max.in.flight.requests.per.connection=5` and
  idempotence **off**: batch 2 succeeds, batch 1 is retried and lands
  after it.
  - fix (a): **turn idempotence on** — it preserves order with
    in-flight up to 5
  - fix (b): `max.in.flight.requests.per.connection=1` — one batch at
    a time, correct but slow
  - (a) is the right answer; (b) is what people did before 0.11
- **Transactional producer?** — `transactional.id` +
  `initTransactions` / `beginTransaction` / `commitTransaction`.
  - makes writes **across multiple partitions and topics — plus the
    consumer's offset commit — atomic**: all visible or none
  - consumers see them only with `isolation.level=read_committed`
  - the `transactional.id` is what **fences a zombie producer**: an
    older instance with the same id is locked out after a restart
  - this is the machinery behind exactly-once ([§6](#6-delivery-guarantees))

## §5 Consumers, groups and offsets

- **Consumer group?** — the way to read one topic in parallel without
  handling anything twice. Consumers sharing a `group.id` act as
  **one logical consumer**: Kafka assigns each partition to exactly
  one member of the group.

  ```text
  Group "reqpay-processors" — 3 partitions, 2 consumers

   Partition 0 ─┐
                ├─ Consumer A
   Partition 1 ─┘
   Partition 2 ──── Consumer B
  ```

  Consumer A dies, its heartbeats stop, the group **rebalances**, and
  B takes over the orphaned partitions:

  ```text
   Partition 0 ─┐
   Partition 1 ─┼─ Consumer B
   Partition 2 ─┘
  ```

  Three pieces behind that:

  - **`group.id` is the identity** — a config string on the consumer;
    every process booted with the same string joins the same group.
    Kafka tracks the group, never the hostnames.
  - **The unit of division is the partition, not the message** — one
    owner per partition, so every event for a key is handled in order
    by one consumer while the group as a whole runs parallel. A
    partition can't be split between two members, so consumers past
    the partition count sit idle: **partition count is the ceiling on
    group parallelism.**
  - **Offsets live in Kafka, per group per partition** — in the
    internal `__consumer_offsets` topic, not in consumer memory.
    That's what lets a survivor resume where the dead consumer
    stopped, and why work done but not yet committed comes back.
- **🔮 Assignment math** — a 4-partition topic:
  - 2 consumers → 2 partitions each
  - 4 consumers → 1 each (the ideal)
  - 6 consumers → 4 work, **2 sit idle** with nothing assigned
  - the two real ways to add throughput: **add partitions** (raises
    the ceiling) or **make each consumer faster** — never "add more
    consumers than partitions"
- **The poll loop?** — a Kafka consumer is **single-threaded and
  pull-based**. `poll(timeout)` does two jobs at once:
  - fetches a batch of records (`max.poll.records`, 500 by default)
  - drives **heartbeats and group coordination** underneath
  - so heavy work between polls is dangerous: exceed
    `max.poll.interval.ms` and the group declares you dead
    ([§7](#7-rebalancing--consumer-liveness))
  - the shape is always: poll → process → commit → poll again
- **`auto.offset.reset`?** — what to do **when the group has no valid
  committed offset** (brand-new group, or the offset expired or fell
  off retention). Applies at that moment only, never during normal
  running.
  - `earliest` → start at the oldest surviving record (replay
    history)
  - `latest` → start at the end (skip everything before now) —
    **the default**
  - `none` → throw
  - picking wrong is the classic "why did my new consumer replay 10
    million records" / "why did it skip the backlog" incident
- **What is a committed offset?** — the group's saved position in a
  partition. It is **the next record to read, not the last one
  read** (last processed + 1):

  ```text
  P0: [m0][m1][m2][m3][m4][m5]
                   ↑
                   offset 3 = "m0–m2 done, start here"

  A read m3 and m4, processed both, died before committing
    → B starts at 3 → m3 and m4 delivered a second time
  ```

  - Memory hook: *the group is one reader wearing several hats; the
    committed offset is a bookmark before the next page, not after
    the last one.*
- **Auto-commit vs manual commit?**
  - `enable.auto.commit=true` (the default) + `auto.commit.interval.ms`
    (5s default): a timer commits the **latest polled** offset from
    inside `poll` — regardless of how far processing actually got
  - manual (`enable.auto.commit=false` + `commitSync`/`commitAsync`)
    lets you commit **exactly when the work is durably done**
  - auto-commit is the single most common source of silent message
    loss, because it takes the timing decision away from you
- **commitSync vs commitAsync?** — sync blocks until the offset is
  stored (safe, retries, slower); async fires and moves on (fast, no
  retry). Memory hook: *sync stops to confirm; async sends and hopes.*
  - the standard pattern: **`commitAsync` per batch in the loop, one
    `commitSync` in a `finally` / on shutdown** so the final position
    is durably stored before exit

  ```java
  // at-least-once — the safe shape
  records = consumer.poll(Duration.ofMillis(500));
  process(records);            // work first
  consumer.commitAsync();      // only then say "done"
  ```

  ```java
  // IMPS: enable.auto.commit=true, auto.commit.interval.ms=500
  records = consumer.poll(Duration.ofMillis(500));  // ← commit for the
  process(records);                                 //   PREVIOUS batch
                                                    //   fires in here
  // crash mid-process → those offsets were already reported done
  ```

  Commit *after* processing is the safe pattern. Auto-commit takes
  the decision away from you: the timer fires from inside `poll`,
  committing the batch the previous `poll` returned, regardless of
  where processing had reached.

## §6 Delivery guarantees

- **Delivery semantics?** *(the trio — say all three)*
  - **at-most-once**: commit *before* processing — never duplicates,
    may **lose**
  - **at-least-once**: commit *after* processing — never loses, may
    **duplicate**
  - **exactly-once**: transactional producer + `read_committed`
    consumer — neither, at a throughput and complexity cost
  - Memory hook: *the commit's position decides — early commit loses,
    late commit repeats. Banks pick repeats + dedupe.*

  ```text
  (A) poll → commit → process     crash between → records skipped  = LOSS
  (B) poll → process → commit     crash between → records replayed = DUPLICATES
  ```

  - there is no third option: you choose **which failure you can
    tolerate**, and the answer for almost every system is (B)
- **Why at-least-once + idempotent consumer is the workhorse** —
  duplicates are cheap if reprocessing the same record yields the
  same state.
  - **idempotent processing** = applying a record twice ≡ applying it
    once
  - you enforce it in **your** datastore, not in Kafka: dedup on the
    event's **business id** (transaction id, event id), not the Kafka
    offset
  - the crux is atomicity: **check-and-apply in one DB transaction**,
    or lean on a **unique constraint / upsert** so a replay no-ops.
    A separate check-then-write lets two deliveries both pass the
    check and both credit
  - *Say it as:* Kafka gives you at-least-once; **your database**
    turns it into effectively-exactly-once
- **Exactly-once (EOS) — what's actually guaranteed?**
  - the recipe: transactional producer → `beginTransaction` →
    produce results → `sendOffsetsToTransaction(offsets, groupMeta)`
    → `commitTransaction`, with downstream consumers on
    `isolation.level=read_committed`
  - the atomic unit is **output records + the input offset commit**,
    together or not at all
  - **the sharp caveat**: it holds for **consume → process → produce
    within Kafka**. Kafka cannot roll back your HTTP POST or your
    non-Kafka DB write — an external side effect is not part of the
    transaction
  - so: worth it for **Kafka-to-Kafka pipelines**; for a consumer
    that writes to a database, at-least-once + a dedup key is simpler
    and just as safe
- **🐛 "We set `enable.idempotence=true`, so we have exactly-once"** —
  the hole is the **consumer path**. Idempotence dedups producer
  retries on the write side only; it says nothing about offset-commit
  timing, redelivery after a rebalance, or external side effects.
  Idempotent producer ≠ exactly-once end-to-end.

## §7 Rebalancing & consumer liveness

- **What is a rebalance?** — the group re-dividing partitions among
  its current members, orchestrated by the **group coordinator** (a
  broker), with one consumer elected leader to compute the
  assignment. Triggers:
  - a consumer **joins**
  - a consumer **leaves** gracefully or **crashes**
  - a consumer **misses its timeouts** (looks dead)
  - the topic's **partition count grows**
  - the **subscription** changes
- **The three liveness knobs** *(a favourite follow-up — they detect
  two different failures)*

    | knob | default | detects |
    | --- | --- | --- |
    | `heartbeat.interval.ms` | 3s | how often the bg thread pings |
    | `session.timeout.ms` | 45s | a **dead / disconnected** consumer |
    | `max.poll.interval.ms` | 5 min | a **live but stuck** consumer |

  - heartbeats run on a **background thread**, so a consumer blocked
    in processing keeps looking alive to `session.timeout.ms`
  - `max.poll.interval.ms` is the one that catches it: heartbeating
    but not calling `poll()` = not making progress
  - they're separate because **"the process is alive"** and **"the
    process is progressing"** are different failures
- **🐛 The constantly-rebalancing consumer** — a 3-minute batch job
  per poll, a group that rebalances forever and reprocesses records.
  - cause: processing exceeds `max.poll.interval.ms` → coordinator
    declares the consumer stuck → rebalance → offsets uncommitted →
    reprocess → repeat
  - fix (a): **lower `max.poll.records`** so a poll's work fits the
    interval (and/or raise the interval)
  - fix (b): **move heavy work off the poll thread** — poll fast,
    hand to a worker pool, manage commits deliberately
  - cranking the timeout alone hides the problem
- **Eager vs cooperative rebalancing?**
  - **eager** — every consumer **revokes all** partitions, then the
    group reassigns from scratch: a **stop-the-world** pause where
    nobody processes
  - **cooperative / incremental** (`CooperativeStickyAssignor`) —
    only the partitions that must move are revoked; everyone else
    **keeps processing**
  - cooperative is the modern choice, and it matters more the bigger
    the group
- **Static membership?** — `group.instance.id`. A consumer that
  restarts within `session.timeout.ms` **rejoins with its previous
  partitions** instead of triggering a full rebalance. On a rolling
  deploy of a large group it turns dozens of disruptive rebalances
  into near-noops.
- **Why is a rebalance a duplicate-processing risk?** — partitions
  get **revoked mid-batch**, so anything processed since the last
  commit is uncommitted, and the new owner reprocesses it.
  - `ConsumerRebalanceListener.onPartitionsRevoked` is the hook to
    **commit offsets / flush state before losing the partition**,
    which shrinks the duplicate window without closing it

## §8 Retention, compaction, lag and failure handling

- **Retention?** — time (`retention.ms`, 7 days by default) or size
  (`retention.bytes`) based, applied to whole **log segments**, and
  **independent of consumption**. Kafka keeps messages whether or not
  anyone read them.
  - ⚓ IMPS's duplicate window was reasoned against the topic's
    ~1-hour retention.
- **🐛 A consumer that fell off the retention window** — group offline
  a week, retention 3 days: its committed offset points at deleted
  data, so the fetch fails and `auto.offset.reset` kicks in —
  `earliest` gives a **gap**, `latest` **skips everything**. The
  expired records are not recoverable either way.
- **Log compaction?** — `cleanup.policy=compact` instead of `delete`.
  - compaction keeps **at least the latest value for each key** and
    garbage-collects superseded versions, so the topic becomes a
    **snapshot of current state per key** while remaining a log
  - a **tombstone** (key with a `null` value) marks a delete: after
    compaction, nothing remains for that key
  - canonical uses: changelog/state topics, CDC, config topics — and
    `__consumer_offsets` itself is compacted
  - the payoff: a consumer can rebuild the **entire current state**
    by replaying from the start, bounded by the number of keys rather
    than the number of events
- **Consumer lag?** — how far a consumer's committed offset trails the
  partition's log-end offset. The first Kafka metric to graph in
  production support; `kafka-consumer-groups --describe` shows it per
  partition.
  - rising lag on **one** partition = a hot key or a stuck consumer;
    rising on **all** = the group is under-provisioned
- **Poison message / DLQ?** — a record that fails processing every
  retry and **blocks the whole partition behind it** (head-of-line
  blocking), because a partition must be processed in order.
  - Kafka has **no native DLQ** — the broker has no such concept
  - the **pattern** is client-side: after N attempts, publish the
    failed record to a separate **dead-letter topic** (with failure
    details in headers) and commit the original offset so the
    partition proceeds
  - ⚓ IMPS doesn't have one today — retry-then-stuck is the honest
    answer, and it's a natural "what I'd add."
- **Blocking vs non-blocking retry?**
  - **blocking** — retry the same record in place with backoff:
    preserves order, **halts the partition** for the duration
  - **non-blocking** — forward the failure to timed **retry topics**
    and move on: the main partition keeps flowing, at the cost of
    **losing strict ordering** for retried records
  - choose per topic: order-critical → blocking; throughput-critical
    → retry topics
  - either way, retries imply redelivery — retry and DLQ machinery
    only makes sense on top of **idempotent processing**

## §9 Spring Kafka — the same ideas, wired

- **Produce and consume?** — Boot auto-configures a `KafkaTemplate`,
  the producer/consumer factories and the listener container infra
  from `spring.kafka.*` (bootstrap servers, `consumer.group-id`,
  serializers).
  - produce: `kafkaTemplate.send(topic, key, value)`
  - consume: `@KafkaListener(topics = "inward-reqpay", groupId = "g")`
- **`concurrency = N` on `ConcurrentKafkaListenerContainerFactory`?**
  — spins up N listener threads, **each an independent consumer in
  the group**. So N is capped by partition count exactly like any
  group: threads beyond it get no partitions and idle. Concurrency
  maps to **consumers**, not to threads-per-partition.
- **Container ack modes?** — the commit-timing lever in Spring:
  - `RECORD` — commit after **each** record
  - `BATCH` (default) — commit after the whole poll batch
  - `MANUAL` / `MANUAL_IMMEDIATE` — you get an `Acknowledgment` and
    commit **when your code says**, which is how you get precise
    at-least-once (commit once the side effect is durable)
- **Error handling and the DLT?** — `DefaultErrorHandler(new
  DeadLetterPublishingRecoverer(template), new FixedBackOff(...))` on
  the container factory: retry with backoff, then publish to
  `<topic>.DLT` and move on (blocking retry). `@RetryableTopic`
  instead creates timed **retry topics** + a DLT for non-blocking
  retry.
- **Kafka transactions in Spring — the honest caveat?** — a
  transactional `KafkaTemplate` / `KafkaTransactionManager` makes
  multi-record sends plus offset commits atomic **on the Kafka side**.
  A Kafka transaction and a **JDBC** transaction are two different
  resource managers with no true XA between them, so a crash between
  the two commits leaves them disagreeing. The robust answer for
  "DB write + Kafka publish atomically" is the **transactional
  outbox**: write the event into a DB table in the same transaction,
  relay it to Kafka separately.

## §10 Why Kafka is fast

- **sequential append-only disk writes** — no random seeks; disk is
  fast when you stop making it seek
- **batching** — producers and consumers move records in blocks, and
  compression applies per batch
- **zero-copy** — the broker streams file bytes straight to the
  socket without copying through application memory
- **the dumb broker** — no per-message delivery state, no routing
  logic; the consumer owns its position, so the broker just serves a
  log by offset

## §11 IMPS — the anchor ⚓

- **Why Kafka in IMPS?** *(THE anchor — say this fluently)* — NPCI
  gives a **20-second SLA**: the receiver must validate, persist, and
  ACK fast, so receipt is decoupled from processing. One topic per
  message type — inward/outward × ReqPay/ReqChkTxn/ReqValAdd, plus
  reversion — so each flow scales and fails independently, each with
  its own consumer processor.

  ```text
  NPCI ──signed XML──▶ Receiver ── INSERT journal ──▶ ACK (fast)
                          │
                          └─▶ topic per msg type ──▶ Processor
                              (inward-reqpay, …)        │
                                                 CBS leg via RMI
  ```

  Walkthrough, one inward ReqPay: receiver verifies the message,
  INSERTs it into the Oracle journal, produces to the inward-reqpay
  topic, ACKs NPCI — seconds, well inside 20. The processor polls the
  topic at its own pace, runs the CBS credit over RMI, sends the
  RespPay. A slow CBS never breaks the NPCI-facing SLA.

  *One-liner:* Kafka sits between receipt and processing, so the ACK
  never waits on the bank's core.
- **What delivery guarantee does IMPS have?** — **at-least-once with
  DB-level idempotency**: the receiver INSERTs every message into the
  txn journal, and processors skip anything whose row is already
  marked processed. NPCI genuinely replays ReqPay, so dedupe is by
  **database state, not by Kafka**.
- **"What if the processor crashes mid-message?"** *(honesty gold)* —
  normally the group rebalances and the message is redelivered. In
  IMPS offsets were committed at receipt — auto-commit was ON (500ms)
  plus a manual `commitAsync` at dequeue — so Kafka considers it
  done; the DB insert at the receiver is the real recovery anchor.
  **First thing I'd redesign: commit after processing, not before.**

## §12 Rapid-fire traps

- **1 partition, 3 consumers in one group — how many consume?** —
  one; the other two idle.
- **5 keyless records to a 3-partition topic, need produce-order?** —
  not guaranteed; no ordering across partitions. Order needs one key
  or one partition.
- **`acks=all` with `min.insync.replicas=1` on RF=3, leader dies
  pre-replication?** — data lost; "all" was satisfied by the leader
  alone.
- **Add partitions to a keyed topic — what silently breaks?** —
  `hash(key) % N` remaps existing keys, so per-key ordering breaks
  across the resize.
- **Auto-commit on, processing throws, loop keeps polling?** — likely
  **loss**: the timer commits the offset regardless of the exception,
  so the failed record is never retried.
- **Two consumers, same `group.id`, different topics?** — works, but
  it's a smell: shared lag/offset tooling, and they can rebalance
  each other. Use distinct group ids per logical consumer.
- **`producer.send()` then the app exits?** — the record may never
  leave; unsent batches sit in the accumulator. `flush()`/`close()`.
- **Consumer reads a record — is it deleted?** — no. Retention and
  compaction delete; consumption never does.

*One-liner for the kit:* Kafka is a replayable append-only log
split into ordered partitions, where the partition is both the unit
of ordering and the unit of parallelism, and **commit timing** is the
knob that sets your delivery guarantee — IMPS uses it to ACK NPCI
fast and let the CBS leg run at its own pace, with the DB journal,
not Kafka, as the truth for dedupe and recovery.

## Rep scorecard — 🟢 only after a blind aloud rep

| Section | Tier | Rep 1 | Rep 2 | Rep 3 |
|---|---|---|---|---|
| §2 Topic, partition, offset | 1 | 🟢 | ☐ | ☐ |
| §5 Consumers, groups, offsets | 1 | 🟢 | ☐ | ☐ |
| §6 Delivery guarantees | 1 | 🟢 | ☐ | ☐ |
| §11 IMPS anchor ⚓ | 1 | 🟢 | ☐ | ☐ |
| §7 Rebalancing & liveness | 2 | ☐ | ☐ | ☐ |
| §3 Cluster & durability | 2 | ☐ | ☐ | ☐ |
| §8 Retention, lag, DLQ | 2 | ☐ | ☐ | ☐ |
| §4 The producer | 3 | ☐ | ☐ | ☐ |
| §9 Spring Kafka | 3 | ☐ | ☐ | ☐ |
| §1 Log vs queue | 3 | 🟢 | ☐ | ☐ |
| §10 Why Kafka is fast | 3 | 🟢 | ☐ | ☐ |
| §12 Rapid-fire traps | 3 | ☐ | ☐ | ☐ |

*Tier 1 earns a second rep before Tier 3 gets a first one.*
