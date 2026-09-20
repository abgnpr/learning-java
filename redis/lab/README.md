# Redis labs 🧪

<div align="center">

## `0 / 15` labs complete

`░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░` **0%**

</div>

```text
Data types         ░░░░░░░░░░░░░░░                         0/5
Atomicity          ░░░░░░░░░                               0/3
Messaging          ░░░░░░░░░                               0/3
Operations         ░░░░░░                                  0/2
Modules            ░░░░░░                                  0/2
```

---

Fifteen runnable Java labs against a real Redis server. Each one is a single
file with a marked `TODO` seam and its own embedded checks: an untouched
starter throws `UnsupportedOperationException`, and a solved one prints a
completion line once every check passes.

The client is **Jedis**, the synchronous Java client, used directly rather than
behind `RedisTemplate`. The point is to see what Redis itself is doing before a
framework hides it.

## Before the first lab

Two things: a server, and the client jars.

```bash
cd redis/lab
podman compose up -d        # Redis on 6379, with the JSON and Search modules
./get-deps.sh               # Jedis and its dependencies into lib/
```

`lib/` is gitignored — the jars are downloaded, never committed. Re-run
`get-deps.sh` on a fresh clone.

Two labs need a bigger topology than a single server, so those tiers sit behind
compose profiles and stay down until asked for:

```bash
podman compose --profile sentinel up -d    # lab 12: master, replica, 3 sentinels
podman compose --profile cluster  up -d    # lab 13: 6 nodes, 3 masters
```

When you are done:

```bash
podman compose --profile sentinel --profile cluster down -v
```

## How to run a lab

Open the file, read its task, complete the `TODO`, then run it from
`redis/lab/`:

```bash
cd redis/lab
java -cp "lib/*" 01-data-types/OtpCache.java
```

Every check prints its input alongside both values, so a failure is
diagnosable without a debugger:

```
FAIL  a rejected attempt leaves the key so the caller may retry
      input:    <lab:1:otp:asha>
      expected: condition holds
      actual:   does not hold
```

Each lab confines itself to keys under `lab:<n>:` and clears them on start, so
reruns are clean and no lab can disturb another.

## The labs

### 01 — data types · the vocabulary

| # | Lab | Type | What it teaches | Done |
|---:|---|---|---|:---:|
| 1 | [OtpCache.java](01-data-types/OtpCache.java) | string + TTL | expiry as the storage policy; single-use keys | ⬜ |
| 2 | [UserProfileStore.java](01-data-types/UserProfileStore.java) | hash | one key per object; a TTL covers the key, never a field | ⬜ |
| 3 | [UniqueVisitors.java](01-data-types/UniqueVisitors.java) | set | membership answered by the write; set algebra on the server | ⬜ |
| 4 | [Leaderboard.java](01-data-types/Leaderboard.java) | sorted set | ordering maintained at write time; ranks, not sorts | ⬜ |
| 5 | [ApiCounter.java](01-data-types/ApiCounter.java) | `INCR` | fixed windows, and the expiry that must be set exactly once | ⬜ |

### 02 — atomicity · the part interviews probe

| # | Lab | Mechanism | What it teaches | Done |
|---:|---|---|---|:---:|
| 6 | [AtomicWorkflow.java](02-atomicity/AtomicWorkflow.java) | `WATCH`/`MULTI`/`EXEC` | optimistic concurrency; a stale read must not commit | ⬜ |
| 7 | [BulkPipeline.java](02-atomicity/BulkPipeline.java) | pipelining | round trips, not atomicity — the distinction that gets confused | ⬜ |
| 8 | [LuaRateLimiter.java](02-atomicity/LuaRateLimiter.java) | `EVAL` | the whole decision server-side; 60 threads, limit 20, exactly 20 | ⬜ |

### 03 — messaging · fan-out and work sharing

| # | Lab | Mechanism | What it teaches | Done |
|---:|---|---|---|:---:|
| 9 | [NotificationBus.java](03-messaging/NotificationBus.java) | pub/sub | fire-and-forget; what is published to nobody is gone | ⬜ |
| 10 | [OrderEventStream.java](03-messaging/OrderEventStream.java) | streams | a log that reading does not consume; bounded by trimming | ⬜ |
| 11 | [StreamWorkerGroup.java](03-messaging/StreamWorkerGroup.java) | consumer groups | one entry to one worker; pending until acknowledged | ⬜ |

### 04 — operations · what happens when a node dies

| # | Lab | Topology | What it teaches | Done |
|---:|---|---|---|:---:|
| 12 | [FailoverExperiment.java](04-operations/FailoverExperiment.java) | Sentinel | the client asks who the master is; a forced failover, watched live | ⬜ |
| 13 | [ShardedCache.java](04-operations/ShardedCache.java) | Cluster | 16384 slots, hash tags, and why `MSET` can be refused | ⬜ |

### 05 — modules · beyond the core types

| # | Lab | Module | What it teaches | Done |
|---:|---|---|---|:---:|
| 14 | [JsonDocumentStore.java](05-modules/JsonDocumentStore.java) | RedisJSON | paths into a document; patching a leaf without moving the tree | ⬜ |
| 15 | [SemanticSearch.java](05-modules/SemanticSearch.java) | Search + vectors | FLOAT32 blobs, a KNN query, retrieval with no shared words | ⬜ |

## Scorecard

| Group | Labs | Solved |
|---|---:|---:|
| 01 Data types | 5 | 0 |
| 02 Atomicity | 3 | 0 |
| 03 Messaging | 3 | 0 |
| 04 Operations | 2 | 0 |
| 05 Modules | 2 | 0 |
| **Total** | **15** | **0** |

## Where the answers are

Nowhere in this folder — that is the point. The spoken answer for every idea
these labs exercise is in [redis-basics.md](../redis-basics.md), and the
written-out drills, with solutions folded away, are in
[redis-basics-exercise.md](../redis-basics-exercise.md).
