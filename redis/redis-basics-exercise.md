# Redis Basics — Anti-Fumble Exercises

For a backend engineer who has **run Redis in production** — a cache,
a counter, session state — but reached for it by habit. Goal: **name,
explain and predict** what Redis is doing, above all where atomicity
begins and ends, under interview fire rather than at first contact.

**Legend** — exercise styles:
🔮 predict-the-behavior · 🛠 build/configure · 🐛 fix-the-bug · 💭 explain-the-difference

Solutions are hidden under each section. Try first, then expand. A
local Redis is enough to *prove* nearly all of these:

```bash
cd lab && podman compose up -d && podman exec -it redis-lab redis-cli
```

Sections carrying a 🧪 have a runnable Java counterpart in
[lab/README.md](lab/README.md). The spoken answers live in
[redis-basics.md](redis-basics.md).

> ### 🎯 Minimum viable path (Pareto — the 20% that gates 80%)
>
> Redis questions collapse onto one axis: *what is Redis doing that
> your application cannot do for itself?* Do the `🎯 CORE PATH`
> sections in this order:
>
> > **§4 → §2 → §9 → §10 → §6 → §8**
> >
> > atomicity · expiry & eviction · caching patterns · distributed
> > locks · pub/sub vs streams · replication & cluster.
>
> - **§4 (atomicity) is THE topic.** One command is atomic, two are a
>   race — and `MULTI`, `WATCH` and Lua are the three ways out, each
>   with a different failure mode. Over-invest there; it is where the
>   ⚓ IMPS story lives.
> - **§9 and §10** are where production scars show. Stampede and the
>   "a lock is really a lease" answer separate operators from users.
> - **Checkbox key:** `[ ]` do · `[~]` skim (low payoff right now) ·
>   `[x]` done · **keep** = do it anyway, even in a `[~]` section.
> - §3, §5 and §11 are breadth. The **Extensions** list at the bottom
>   is the deliberate "later" pile.

> **📖 Version caveat.** Redis 6 added ACLs, RESP3 and threaded I/O
> (for sockets, **not** for command execution). Redis 7 added
> Functions (`FUNCTION LOAD`, a durable successor to `EVAL` scripts)
> and sharded pub/sub. The JSON, Search, time-series and probabilistic
> types are **modules** — present in Redis Stack and Redis Enterprise,
> absent from a stock `redis-server`. Licensing moved to RSALv2/SSPL
> in 2024 and Redis 8 returned an AGPL option; **Valkey** is the
> Linux-Foundation fork of the 7.2 line and is command-compatible.
> Where a fact depends on the line it is flagged.

---

## §1 The model — a data-structure server · foundational (do once)

🧪 Lab: none — this one is vocabulary.

- [ ] **1.1** 💭 Complete precisely: "Redis is a ___ server." Why is
  "Redis is a cache" an answer that fails the follow-up question?
- [ ] **1.2** 💭 Redis executes commands **single-threaded**. Give one
  reason that makes it *faster* and one reason it makes it *fragile*.
- [ ] **1.3** 🔮 A colleague runs `KEYS user:*` against a production
  instance holding 20 million keys. Describe what every other client
  observes, and give the command they should have run.
- [ ] **1.4** 💭 Redis 6 introduced "threaded I/O". What exactly was
  threaded, and what deliberately was not?
- [ ] **1.5** 💭 State the one-sentence consequence of single-threaded
  execution that the whole atomicity section (§4) is built on.

<details><summary>Solutions 1</summary>

- 1.1 *(ref: Redis docs — Introduction)* "Redis is an **in-memory
  data-structure server**." A key maps to a *typed* value — string,
  hash, list, set, sorted set, stream — and the server performs that
  type's operations. "A cache" explains `GET`/`SET` and nothing else:
  it cannot explain leaderboards, rate limiters, queues or session
  stores, which is where the follow-up goes.
- 1.2 *(ref: Redis docs — FAQ)* **Faster:** no locks, no contention,
  no context switching between threads competing for the same data,
  and every command sees a consistent world for free. **Fragile:** one
  slow command blocks every other client — there is no second thread
  to make progress.
- 1.3 *(ref: Redis docs — KEYS)* `KEYS` scans the entire keyspace in
  one blocking command. Every other client stalls for its duration —
  seconds, at 20 million keys — so the instance looks hung and
  timeouts cascade upstream. Use **`SCAN`**, a cursor that returns a
  bounded slice per call: `SCAN 0 MATCH user:* COUNT 100`.
- 1.4 *(ref: Redis 6 release notes)* **Socket reading and writing**
  were threaded — parsing requests and writing replies. **Command
  execution was not**: commands still run one at a time on one thread,
  which is what preserves the atomicity guarantee.
- 1.5 **One command is atomic; two commands are a race.** Everything
  in §4, most of §9, and the ⚓ IMPS debit-cap bug follow from it.
</details>

---

## §2 Keys, expiry and eviction ⭐⭐ · 🎯 CORE PATH

🧪 Lab: [OtpCache.java](lab/01-data-types/OtpCache.java) ·
[ApiCounter.java](lab/01-data-types/ApiCounter.java)

- [ ] **2.1** 💭 `SET k v` followed by `EXPIRE k 60` versus
  `SET k v EX 60`. Both look identical. Name the failure that
  separates them.
- [ ] **2.2** 🔮 A key has 40 seconds left to live. A plain `SET k v2`
  runs. What is `TTL k` afterwards, and why is this a common
  production surprise?
- [ ] **2.3** 💭 What do `TTL` returning `-1` and `-2` each mean?
- [ ] **2.4** 🐛 A session is stored as a hash. A developer asks how to
  expire only the `csrfToken` field after 5 minutes while the rest of
  the session lives an hour. Answer them.
- [ ] **2.5** 💭 Does Redis remove a key the instant its TTL passes?
  Describe the two mechanisms and what that implies about memory.
- [ ] **2.6** 🛠 A pure cache instance is being sized. Name the two
  settings you must configure, and the policy you would choose.
- [ ] **2.7** 🐛 An instance runs `maxmemory-policy volatile-lru`.
  Memory is full and writes are failing with an OOM error, although
  `INFO` shows millions of keys. Diagnose it.
- [ ] **2.8** 🔮 A fixed-window counter is implemented as `INCR` then
  `EXPIRE`, both on every request. A client sends traffic continuously.
  When does the window close?

<details><summary>Solutions 2</summary>

- 2.1 *(ref: Redis docs — SET, EXPIRE)* Two commands are not atomic. A
  crash, a failover or a connection drop between them leaves a key
  **with no expiry** — an immortal entry that nothing will reclaim.
  `SET k v EX 60` sets value and expiry in one atomic command.
- 2.2 `TTL k` is **-1**: the key now lives forever. A plain `SET`
  replaces the key wholesale, including clearing its expiry. This is
  how caches silently become permanent stores. `SET k v2 KEEPTTL`
  preserves it (Redis 6+), and `HSET` on an existing hash does not
  touch the key's TTL.
- 2.3 **`-1`** = the key exists and has **no expiry**. **`-2`** = the
  key **does not exist**. Confusing the two is how "expired" and
  "never written" get handled identically by accident.
- 2.4 You cannot. **A TTL belongs to the key, not to a field.** The
  hash expires as one unit. If `csrfToken` needs its own lifetime it
  needs its own key — `session:<id>:csrf` with its own `EX`. (Redis
  7.4 added `HEXPIRE` for per-field TTLs; on anything earlier, and in
  most deployed estates, the answer is separate keys.)
- 2.5 *(ref: Redis docs — Expiration)* Two mechanisms: **lazy**, on
  access — the key is checked and dropped when someone touches it —
  and an **active** background sampling pass. So an expired key can
  hold memory for some time after its deadline. Expiry is a contract
  about what you can *read*, not a promise about when memory returns.
- 2.6 **`maxmemory`** (a byte ceiling — without it Redis grows until
  the OS kills it) and **`maxmemory-policy`**. For a pure cache,
  `allkeys-lru`, or `allkeys-lfu` when there is a hot core and a long
  cold tail. For an instance holding anything you cannot rebuild,
  `noeviction` — failing the write is better than losing the data.
- 2.7 `volatile-lru` only considers keys **that have a TTL**. If the
  application writes most keys without one, almost nothing is
  eligible, and the policy behaves like `noeviction` — writes fail
  while memory is full of keys the policy refuses to touch. Fix:
  either set TTLs consistently, or switch to `allkeys-lru`.
- 2.8 **Never.** Each request re-arms the expiry, so the window slides
  forward with the traffic and the counter is never reset — a busy
  caller is never throttled, which is precisely backwards. Set the
  expiry **only when `INCR` returns 1**, the call that created the
  key.
</details>

---

## §3 The core types ⭐ · mostly `[~]` (two keeps)

🧪 Lab: [UserProfileStore.java](lab/01-data-types/UserProfileStore.java) ·
[UniqueVisitors.java](lab/01-data-types/UniqueVisitors.java) ·
[Leaderboard.java](lab/01-data-types/Leaderboard.java)

- [ ] **3.1** 💭 **keep** — Give the choosing rule in one sentence:
  what makes you pick a type?
- [ ] **3.2** 🛠 **keep** — Name the type for each: a leaderboard · a
  set of unique daily visitors · a user object whose fields are
  updated independently · a work queue · an event log that two
  independent readers both consume fully.
- [ ] **3.3** 🐛 A service loads a 50-field hash with `HGETALL`, picks
  one field in Java, and discards the rest — on every request. Fix it.
- [ ] **3.4** 💭 `SADD` returns a number. What does it tell you, and
  which two-command pattern does that let you delete?
- [ ] **3.5** 🔮 A leaderboard is a Redis **list** that the service
  sorts in Java after fetching. Name two things that break at scale,
  and the replacement.
- [ ] **3.6** 💭 You must count unique visitors across 200 million
  events per day and can tolerate ~1% error. Which structure, and what
  does it cost in memory?
- [ ] **3.7** 💭 Why is `LPUSH` + `BRPOP` a fragile work queue, and
  what makes `BLMOVE` better?

<details><summary>Solutions 3</summary>

- 3.1 Pick the type whose **server-side operation** matches the
  question you will ask. If the answer needs work done after the
  fetch, the type is probably wrong.
- 3.2 Leaderboard → **sorted set** (`ZADD`/`ZREVRANGE`). Unique
  visitors → **set** (`SADD`/`SCARD`), or HyperLogLog at very high
  cardinality. User object → **hash** (`HSET`/`HGET`). Work queue →
  **list** (`LPUSH`/`BRPOP`), or a **stream** with a consumer group
  when acknowledgement matters. Event log two readers both consume →
  **stream** (`XRANGE` does not consume).
- 3.3 `HGET key field` — fetch the one field. `HGETALL` on a wide hash
  moves the whole object across the network and allocates it, per
  request, to use 2% of it. `HMGET` for a few fields.
- 3.4 It returns **the number of members actually added** — `1` if the
  member was new, `0` if it was already present. That single reply
  answers "have I seen this before?" **in the write itself**, deleting
  the `SISMEMBER`-then-`SADD` pattern, which is check-then-act and
  therefore racy.
- 3.5 (a) The whole list crosses the network on every read, so cost
  grows with the *board*, not with the page requested. (b) Two
  concurrent score updates are read-modify-write in Java and lose each
  other. Replace with a **sorted set**: `ZINCRBY` updates atomically
  and `ZREVRANGE 0 9` returns the top ten without transferring the
  rest.
- 3.6 **HyperLogLog** — `PFADD` / `PFCOUNT`. Roughly **12 KB per
  counter** regardless of cardinality, with ~0.81% standard error, and
  `PFMERGE` unions them for weekly and monthly rollups. A set would be
  exact and cost gigabytes.
- 3.7 `BRPOP` **removes** the item and hands it to the consumer. If
  that consumer dies before finishing, the item is gone — there is no
  record it was ever taken. `BLMOVE source dest LEFT RIGHT` moves it
  atomically onto a *processing* list, so a crashed consumer's work is
  still visible and can be reclaimed. (A stream consumer group does
  this properly, with a pending list — §6.)
</details>

---

## §4 Atomicity — INCR, MULTI, WATCH, Lua ⭐⭐ · 🎯 CORE PATH (THE signature topic)

🧪 Lab: [AtomicWorkflow.java](lab/02-atomicity/AtomicWorkflow.java) ·
[LuaRateLimiter.java](lab/02-atomicity/LuaRateLimiter.java)

- [ ] **4.1** 💭 State the rule the whole section rests on, in one
  sentence.
- [ ] **4.2** 🐛 ⚓ The shipped bug: a per-customer debit cap is checked
  with `HGET` → compare in Java → `HSET`. Describe the exact
  interleaving that lets two concurrent payments both pass, and name
  the general defect shape.
- [ ] **4.3** 🛠 Fix 4.2 two ways — one with `WATCH`, one with Lua —
  and say which you would ship under heavy contention, and why.
- [ ] **4.4** 💭 Does `MULTI`/`EXEC` roll back if one command fails?
  Answer precisely, then say what `DISCARD` actually does.
- [ ] **4.5** 🔮 Inside `MULTI`, a developer writes `GET balance` and
  tries to branch on the result before `EXEC`. What do they get back,
  and why is the whole idea impossible?
- [ ] **4.6** 💭 `WATCH` is compare-and-swap. Name the Java concurrency
  primitive it corresponds to, and the responsibility it puts on the
  caller that `MULTI` alone does not.
- [ ] **4.7** 🛠 Write the Lua for a fixed-window rate limiter:
  increment, expire on first use, return remaining budget or -1.
- [ ] **4.8** 💭 In a Lua script, why must keys be passed in `KEYS`
  rather than hard-coded in the body? (Answer names Cluster.)
- [ ] **4.9** 💭 What is `EVALSHA` for, and what must a client be ready
  to handle when it uses one?
- [ ] **4.10** 🔮 A script loops over ten million keys. What happens to
  the rest of the clients?

<details><summary>Solutions 4</summary>

- 4.1 **A single command is atomic; a sequence of commands is a race.**
  Every Redis concurrency bug lives in the gap between two commands.
- 4.2 ⚓ Two outward payments for the same customer arrive together.
  Both `HGET` the cap and read the same used-amount, say 80 000 of a
  100 000 limit. Both add their 15 000 in Java, both compute 95 000,
  both decide there is room, both `HSET`. The cap is now wrong and
  110 000 has been let through. The shape is **check-then-act**
  (equivalently read-modify-write) — the same defect as `count++`
  between two threads, moved across a network.
- 4.3 **`WATCH`:** `WATCH cap` → `HGET` → decide → `MULTI` → `HSET` →
  `EXEC`; a nil reply means the key changed and the caller retries.
  **Lua:** one `EVAL` that reads the cap, compares and writes, running
  uninterrupted server-side. Ship **Lua** under heavy contention:
  `WATCH` degrades into a retry storm when many callers contend for
  the same key — every retry is another round trip — while the script
  succeeds first time, always.
- 4.4 **No rollback exists.** Commands queued between `MULTI` and
  `EXEC` run as one uninterrupted block, but a command that fails at
  runtime — wrong type for the key — simply fails while the others
  still apply. (A *syntax* error at queue time does abort the whole
  transaction at `EXEC`.) **`DISCARD`** throws away the queued block
  *before* `EXEC`; it is a cancel, not an undo.
- 4.5 They get back **`QUEUED`**, a placeholder — not the value. The
  commands have not run yet; that is the entire point of queueing
  them. Branching on a value requires either reading it **before**
  `MULTI` and protecting the decision with `WATCH`, or moving the
  branch server-side into Lua.
- 4.6 It corresponds to **`AtomicInteger.compareAndSet`** /
  `AtomicReference.compareAndSet` — optimistic concurrency. The
  responsibility it adds is the **retry loop**: a nil `EXEC` is not an
  error to log, it is a signal to go round again, and the caller must
  bound how many times.
- 4.7 ```lua
  local used = redis.call('INCR', KEYS[1])
  if used == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[2])
  end
  local limit = tonumber(ARGV[1])
  if used > limit then return -1 end
  return limit - used
  ```
  Note the expiry is attached only when the script created the key —
  the same trap as 2.8, now closed atomically.
- 4.8 Because in **Cluster**, the node that should run the script is
  chosen from the keys the script declares. Keys hidden inside the
  body are invisible to the router, so the script may run on a node
  that does not own them. Declaring them in `KEYS` is also what lets
  Redis verify they share a slot.
- 4.9 `EVALSHA <sha1>` runs a script already cached on the server, so
  the body crosses the wire once instead of on every call. The client
  must handle a **`NOSCRIPT`** error — the cache is not persistent and
  is emptied by `SCRIPT FLUSH`, restarts and failovers — by loading
  the script and retrying. Every mature client does this for you.
- 4.10 Everything stops. A script runs to completion with no other
  client interleaved, so a long script is an outage with extra steps.
  Past `busy-reply-threshold` Redis starts answering other clients
  `BUSY` and only `SCRIPT KILL` (or `SHUTDOWN NOSAVE` if it has
  written) recovers it. Scripts must be short and bounded.
</details>

---

## §5 Pipelining — latency, not atomicity · `[~]` awareness (one keep)

🧪 Lab: [BulkPipeline.java](lab/02-atomicity/BulkPipeline.java)

- [ ] **5.1** 💭 **keep** — Pipelining versus `MULTI`/`EXEC`: what does
  each buy, and which one is atomic?
- [ ] **5.2** 🔮 1000 `GET`s on a link with 0.5 ms round-trip latency.
  Estimate the wall time one-by-one, and pipelined.
- [ ] **5.3** 🐛 A batch job pipelines 5 million commands in one go and
  the client runs out of heap. Explain and fix.
- [ ] **5.4** 💭 Can another client's command execute in the middle of
  your pipeline? What does that rule out?

<details><summary>Solutions 5</summary>

- 5.1 **Pipelining** buys **fewer round trips** and is **not atomic** —
  the server still executes commands one at a time and other clients
  interleave freely. **`MULTI`/`EXEC`** buys **isolation** — nothing
  interleaves — and does nothing for latency. They compose: a pipeline
  may contain a transaction.
- 5.2 One by one: 1000 × 0.5 ms ≈ **500 ms**, nearly all of it waiting,
  with the server idle. Pipelined: one round trip plus execution ≈ a
  **few milliseconds**. This is why pipelining is the first thing to
  reach for on a bulk path.
- 5.3 Every reply is buffered client-side until the pipeline is
  synchronised, so 5 million replies are held at once. **Batch it** —
  flush every 1000–10 000 commands. The round-trip saving is already
  almost entirely realised at that size.
- 5.4 **Yes.** That rules out using a pipeline for anything requiring
  a consistent view or a read-then-write decision — that is `MULTI`,
  `WATCH` or Lua. A pipeline is a transport optimisation, nothing more.
</details>

---

## §6 Pub/Sub and Streams ⭐ · 🎯 CORE PATH

🧪 Lab: [NotificationBus.java](lab/03-messaging/NotificationBus.java) ·
[OrderEventStream.java](lab/03-messaging/OrderEventStream.java) ·
[StreamWorkerGroup.java](lab/03-messaging/StreamWorkerGroup.java)

- [ ] **6.1** 💭 A message is published to a channel with no
  subscribers. Where does it go? What does `PUBLISH` return?
- [ ] **6.2** 🐛 Cache invalidation is broadcast over pub/sub. A
  service restarts, taking 4 seconds to reconnect. What is wrong with
  its cache afterwards, and what would you change?
- [ ] **6.3** 💭 Two readers both need to process every entry of a log,
  independently, and a reader that joins tomorrow must see today's
  entries. Pub/sub or streams? Why?
- [ ] **6.4** 💭 What does `XREADGROUP` guarantee that `XREAD` does
  not?
- [ ] **6.5** 🔮 A worker reads three entries with `XREADGROUP`,
  processes two, and is killed. What is the state of those three
  entries, and how does another worker pick up the lost one?
- [ ] **6.6** 🛠 A stream grows unboundedly. Name the mechanism that
  bounds it, and say why a TTL is the wrong tool.
- [ ] **6.7** 💭 Someone proposes replacing Kafka with Redis Streams
  for a payment switch. Give the honest comparison — two things
  Streams do just as well, and two reasons to decline.

<details><summary>Solutions 6</summary>

- 6.1 *(ref: Redis docs — Pub/Sub)* **Nowhere — it is dropped.**
  Pub/sub is fire-and-forget with no history and no storage: delivery
  is to whoever is subscribed at that instant. `PUBLISH` returns **the
  number of subscribers that received it**, which is the only delivery
  report the protocol offers, and it is perfectly happy to return 0.
- 6.2 Every invalidation published during those 4 seconds was lost, so
  the service resumes with a cache holding **stale entries it will
  never be told about** — and no way to detect it. Fixes: flush the
  local cache on reconnect; use short TTLs so staleness is bounded;
  or move invalidation onto a **stream** with a consumer group, where
  the missed entries are still there on reconnect.
- 6.3 **Streams.** `XRANGE`/`XREAD` do not consume, so two independent
  readers both see everything, and the log persists, so a reader
  joining tomorrow reads from the beginning. Pub/sub can do neither —
  it has no history at all.
- 6.4 `XREAD` gives **every** reader **every** entry. `XREADGROUP`
  gives each entry to **exactly one member of the group**, and holds
  it in the group's **pending list** until `XACK`. That is the
  difference between fan-out and work distribution — and the pending
  list is what makes delivery recoverable.
- 6.5 All three left the "undelivered" state when they were read, so
  no other worker will be offered them by `>`. The two processed ones
  were acknowledged and are gone from the pending list; **the third is
  still pending**, attributed to the dead consumer. Another worker
  reclaims it with **`XAUTOCLAIM`** (or `XPENDING` + `XCLAIM`) after a
  minimum idle time, which is the recovery path pub/sub cannot offer.
- 6.6 **`XTRIM MAXLEN`** (or `MAXLEN` on `XADD`), optionally
  approximate with `~` so trimming stops at a node boundary and stays
  cheap; `MINID` trims by id/age instead. A TTL is wrong because it
  expires the **whole key** — the entire stream disappears at once,
  rather than the oldest entries rolling off.
- 6.7 **Just as well:** ordering with monotonic ids, consumer groups
  with acknowledgement and recovery of a dead worker's messages.
  **Decline because:** retention is bounded by RAM, not disk, so long
  replay windows are impractical; and there is no partitioned
  horizontal scale of a single stream, nor the operational ecosystem
  (Connect, Streams, tooling, established failure playbooks) that a
  payment switch leans on. ⚓ IMPS runs Kafka for exactly these
  reasons, and Redis for the debit cap only.
</details>

---

## §7 Persistence — RDB, AOF and durability ⭐ · `[~]` (two keeps)

🧪 Lab: none — this is configuration and honesty.

- [ ] **7.1** 💭 **keep** — RDB versus AOF in one line each.
- [ ] **7.2** 💭 **keep** — The three `appendfsync` values, what each
  loses on a crash, and which is the usual production answer.
- [ ] **7.3** 🔮 A master acknowledges a write, then dies. A replica is
  promoted. Is the write there? Answer precisely and name the property
  responsible.
- [ ] **7.4** 💭 What does `WAIT numreplicas timeout` do, and what does
  it still not guarantee?
- [ ] **7.5** 💭 Give the one-sentence answer to "can we keep the
  payment ledger in Redis?"

<details><summary>Solutions 7</summary>

- 7.1 **RDB** — periodic point-in-time **snapshots**; compact, fast to
  restore, and loses everything since the last one. **AOF** — an
  **append-only log of write commands**, replayed on restart and
  rewritten periodically to stay compact; finer-grained recovery at
  the cost of a larger file and more I/O. Most production runs both.
- 7.2 **`always`** — fsync per write; loses ~nothing, and is markedly
  slower. **`everysec`** — fsync once a second; loses up to one
  second; **the default and the usual answer**. **`no`** — the OS
  decides; fastest, and loses whatever was buffered.
- 7.3 **Possibly not.** Redis replication is **asynchronous**: the
  master replies to the client before the replica has the data. If it
  dies in that window, the promoted replica never saw the write, and
  it is lost despite having been acknowledged. The property is
  asynchronous replication — it is a deliberate latency trade, not a
  bug.
- 7.4 It blocks until `numreplicas` replicas have acknowledged the
  writes issued so far, or the timeout elapses, and returns how many
  did. It **narrows** the window in 7.3 but does not close it: it is
  not a consensus protocol, the acknowledgement is not durable on the
  replica's disk, and a timeout still returns rather than failing the
  write.
- 7.5 **No** — Redis can lose an acknowledged write in a failover, so
  the money record belongs in a store with synchronous durability;
  keep in Redis only what can be rebuilt from that record. ⚓ IMPS
  keeps the transaction journal in Oracle and the debit cap in Redis,
  which is this sentence in production form.
</details>

---

## §8 Replication, Sentinel and Cluster ⭐ · 🎯 CORE PATH

🧪 Lab: [FailoverExperiment.java](lab/04-operations/FailoverExperiment.java) ·
[ShardedCache.java](lab/04-operations/ShardedCache.java)

- [ ] **8.1** 💭 Replication, Sentinel and Cluster each solve a
  different problem. Name the problem for each, in one line.
- [ ] **8.2** 🔮 Replication alone is configured, master plus two
  replicas, no Sentinel. The master dies. What happens to the
  application?
- [ ] **8.3** 🛠 Why are three sentinels deployed rather than two?
- [ ] **8.4** 💭 How does a client using Sentinel learn where to send
  writes, and what must it do after a failover? (This is the whole
  point of Sentinel.)
- [ ] **8.5** 💭 How many hash slots does Cluster have, and how is a
  key's slot computed?
- [ ] **8.6** 🐛 `MSET user:1:profile x user:1:sessions y` fails on a
  cluster with `CROSSSLOT`. Explain, then fix it without changing what
  the two keys mean.
- [ ] **8.7** 🔮 A Lua script declares two keys that live in different
  slots. What does Cluster do?
- [ ] **8.8** 💭 A team wants Cluster "because it's more scalable".
  Name two costs they are signing up for.

<details><summary>Solutions 8</summary>

- 8.1 **Replication** — read scale-out and a warm standby copy.
  **Sentinel** — *availability* for a single master: detect its
  failure, promote a replica, and tell clients. **Cluster** —
  *capacity*: shard the keyspace across several masters when one
  cannot hold the data or serve the traffic.
- 8.2 Nothing promotes a replica, so writes fail permanently while
  reads may still work against the replicas. Recovery is a human
  running `REPLICAOF NO ONE` and repointing the application.
  Replication gives copies, never failover.
- 8.3 Failover requires a **quorum** to agree the master is down, and
  the promotion itself needs a majority of the sentinel set. With two
  sentinels, losing one leaves no majority and no failover is possible
  — the exact moment you need it. Three tolerates one loss; odd
  numbers avoid split votes.
- 8.4 The client is configured with the **sentinel addresses and the
  master's name**, not the master's address. It asks a sentinel
  `SENTINEL get-master-addr-by-name <name>` and connects to the answer,
  subscribing to sentinel events. After a failover it **re-resolves**
  and reconnects to the new master — which is why a sentinel-aware
  pool survives without being reconfigured or restarted.
- 8.5 **16384** slots. `slot = CRC16(key) mod 16384`, with each master
  owning a contiguous range. Clients cache the slot map and route
  directly; a **`MOVED`** reply tells a client its map is stale.
- 8.6 A multi-key command runs only if every key is in the **same
  slot**, and those two hash to different slots. Fix with a **hash
  tag** — only the text inside braces is hashed — so
  `{user:1}:profile` and `{user:1}:sessions` share a slot while still
  naming the same two things. The keys keep their meaning; only the
  routing changes.
- 8.7 It **refuses** the script, for the same reason as 8.6: the
  command cannot be routed to one node that owns everything it
  touches. Scripts in a cluster must confine themselves to one slot,
  which in practice means hash tags.
- 8.8 (a) **Multi-key operations stop being free** — transactions,
  scripts and `MSET`/`SINTER` need their keys in one slot, so the data
  model has to be designed around hash tags. (b) **Operational weight**
  — more nodes, resharding, slot migration, and client libraries that
  must handle `MOVED`/`ASK`. Also: a single hot key still lives on a
  single node, so Cluster does nothing for hot-key skew.
</details>

---

## §9 Caching patterns — aside, invalidation, stampede ⭐⭐ · 🎯 CORE PATH

🧪 Lab: none runnable — this is design, and it is asked in words.

- [ ] **9.1** 🛠 Write the cache-aside read path and write path in
  pseudocode.
- [ ] **9.2** 🐛 A team updates the cache on write instead of deleting
  it, "to keep it warm". Construct the interleaving that leaves the
  cache holding a value the database never had.
- [ ] **9.3** 💭 Cache-aside, write-through, write-behind: one line
  each, and why cache-aside is the default.
- [ ] **9.4** 🔮 A popular product page is cached with a 10-minute TTL.
  It serves 2000 requests/second. Describe precisely what happens at
  the instant the key expires.
- [ ] **9.5** 🛠 Give three fixes for 9.4 and say which you would apply
  first.
- [ ] **9.6** 💭 What is cache **penetration**, and how is it different
  from a stampede? Give its fix.
- [ ] **9.7** 🐛 A nightly job warms 100 000 cache entries in a loop,
  all with `EX 3600`. Predict the incident at T+1 hour and give the
  one-line fix.

<details><summary>Solutions 9</summary>

- 9.1 **Read:** `v = redis.get(k)`; on a miss, `v = db.load(k)` then
  `redis.set(k, v, EX ttl)`; return `v`. **Write:**
  `db.update(k, v)` then `redis.del(k)` — write the source of truth
  first, then invalidate.
- 9.2 Reader misses and loads **v1** from the database. Before it
  writes to the cache, a writer updates the database to **v2** and
  sets the cache to v2. The slow reader now completes its `SET` with
  **v1**, overwriting v2. The cache holds v1 while the database holds
  v2, and nothing will correct it until the TTL lapses. `DEL` has no
  such failure: it is idempotent, and the next read repopulates from
  the database.
- 9.3 **Cache-aside** — the application loads on a miss and
  invalidates on a write. **Write-through** — writes go through the
  cache to the database synchronously; consistent, slower writes.
  **Write-behind** — writes hit the cache and flush asynchronously;
  fastest, and it can lose data. Cache-aside is the default because it
  **degrades safely**: a cold or broken cache is slow, not wrong.
- 9.4 All in-flight requests miss simultaneously. With 2000 req/s,
  roughly 2000 concurrent identical database loads start at once — a
  **stampede**. The database saturates, every request slows, and the
  slowness keeps the window open longer, which piles on more misses.
  Nothing about this needs a traffic spike; the expiry alone caused it.
- 9.5 (a) **Rebuild lock** — the first miss wins
  `SET k:lock 1 NX EX 10` and loads; the rest wait briefly or serve
  stale. (b) **Serve stale while rebuilding** — store a logical expiry
  inside the value under a longer physical TTL, so one caller
  refreshes and everyone else keeps getting the old value and **nobody
  ever sees a miss**. (c) **Jitter the TTL** so hot keys do not expire
  in lockstep. Apply **(c) first** — one line, no new failure modes —
  then (b) for the genuinely hot keys.
- 9.6 **Penetration** is repeated lookups for a key that exists
  **nowhere** — not in cache, not in the database — so the cache can
  never help and every request reaches the database. A stampede is
  many requests for a key that *does* exist and has just expired. Fix
  penetration by **caching the negative result** with a short TTL, or
  keeping a **Bloom filter** of ids known to exist. It matters because
  it is also the shape of a cheap denial-of-service.
- 9.7 All 100 000 entries were written within a few minutes of one
  another, so they **all expire within the same few minutes** — a
  cache avalanche one hour later, with the database taking the entire
  working set's load at once. Fix: **jitter**, `EX 3600 + random(0,
  600)`, so expiry is spread across ten minutes instead of stacked.
</details>

---

## §10 Distributed locks ⭐ · 🎯 CORE PATH

🧪 Lab: none runnable — the honest answer is the deliverable.

- [ ] **10.1** 🛠 Write the acquire command, with every option, and say
  what each option is for.
- [ ] **10.2** 🐛 A lock is released with `DEL lock:resource`.
  Construct the sequence where this releases **someone else's** lock.
- [ ] **10.3** 🛠 Write the correct release, and say why it cannot be
  two commands.
- [ ] **10.4** 💭 Why is a random token stored as the value, rather
  than `1`?
- [ ] **10.5** 🔮 A holder acquires a 30-second lease, then suffers a
  40-second GC pause. Describe what can go wrong, and say whether any
  Redis setting prevents it.
- [ ] **10.6** 💭 Given 10.5, what actually keeps the protected
  operation correct?
- [ ] **10.7** 💭 What is Redlock, and what is the one-sentence
  critique?

<details><summary>Solutions 10</summary>

- 10.1 `SET lock:resource <random-token> NX PX 30000`. **`NX`** — set
  only if absent, which makes acquisition atomic so exactly one caller
  wins. **`PX 30000`** — an expiry in milliseconds, mandatory, so a
  holder that crashes does not lock the resource forever. **The token**
  — identifies the holder, for release (10.4).
- 10.2 Holder A acquires with a 30-second lease and stalls. The lease
  **expires**. Holder B acquires the now-free lock. A wakes up,
  finishes, and runs `DEL lock:resource` — deleting **B's** lock. C
  now acquires it while B still believes it holds it, and two holders
  run concurrently.
- 10.3 A Lua script comparing and deleting in one step:
  ```lua
  if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('DEL', KEYS[1])
  else
    return 0
  end
  ```
  It cannot be two commands because `GET`-then-`DEL` is
  **check-then-act**: the lease can expire and be re-acquired between
  them, which is exactly 10.2.
- 10.4 Because a lock you cannot **identify** is a lock you cannot
  safely release. With value `1`, every holder's release looks
  identical and there is no way to tell "my lock" from "the lock
  someone else acquired after mine expired".
- 10.5 The lease expires mid-pause while the holder still believes it
  holds the lock. Another caller acquires it legitimately, and when
  the first resumes, **two holders run at once** — and the first may
  also write as though it were still exclusive. **No Redis setting
  prevents this**: the lease is a timer, and Redis cannot know the
  holder is frozen. A longer lease only widens the outage when a
  holder really does die.
- 10.6 Making the protected operation **safe without the lock**: an
  idempotency key so a repeat is a no-op, a conditional/compare-and-set
  write, a database uniqueness constraint, or a **fencing token** — a
  monotonically increasing number handed out with the lock, which the
  downstream resource records and uses to reject a stale holder's
  write. The lock then prevents wasted work rather than carrying
  correctness.
- 10.7 **Redlock** acquires the same lock on a majority of N
  independent Redis masters, to survive losing one. The critique: it
  still relies on **bounded clock drift and bounded process pauses**,
  so it does not fix 10.5 — it makes the lock more available, not
  safe. For correctness, fence at the resource.
</details>

---

## §11 Redis from Java · `[~]` unless the JD says Spring

🧪 Lab: every lab in [lab/](lab/README.md) — they all use Jedis directly.

- [ ] **11.1** 💭 Jedis versus Lettuce: the one-line difference, and
  which is Spring Boot's default.
- [ ] **11.2** 🐛 A `@Service` holds one `Jedis` instance as a field
  and several request threads use it. Predict the symptoms.
- [ ] **11.3** 🐛 `@Cacheable("products")` is added. Six months later
  the instance is out of memory. What was never configured?
- [ ] **11.4** 🐛 Keys in `redis-cli` look like
  `\xac\xed\x00\x05t\x00\x08products`. Explain and fix.
- [ ] **11.5** 💭 Under load, calls to Redis start taking seconds
  although Redis itself reports sub-millisecond latency. Name the most
  likely cause.

<details><summary>Solutions 11</summary>

- 11.1 **Jedis** is synchronous and blocking, one connection per
  operation borrowed from a pool, with an API that maps one-to-one
  onto Redis commands. **Lettuce** is Netty-based, thread-safe, and
  supports sync, async and reactive over a shared connection — and is
  **Spring Boot's default**.
- 11.2 A `Jedis` instance wraps **one socket and is not thread-safe**.
  Concurrent threads interleave writes into the same connection, so
  replies are read by the wrong caller: nonsensical values, class-cast
  errors, protocol desynchronisation, `JedisConnectionException`. Fix:
  `JedisPool`, one connection borrowed per operation — or Lettuce.
- 11.3 **A TTL.** Spring's `RedisCacheConfiguration` has **no expiry
  by default**, so `@Cacheable` caches forever and the keyspace grows
  without bound. Set `entryTtl(...)`, and set `maxmemory` with an
  eviction policy as a backstop.
- 11.4 That is **JDK serialization** — the default `RedisTemplate`
  serializer. It makes keys and values unreadable from `redis-cli`,
  unusable from any non-Java client, and brittle against class
  changes. Fix: configure `StringRedisSerializer` for keys and a JSON
  serializer (`GenericJackson2JsonRedisSerializer`) for values.
- 11.5 **Connection-pool exhaustion.** Threads are blocked waiting to
  borrow a connection, not waiting on Redis. Server-side latency looks
  perfect because the commands are fast once they are sent. Check pool
  size against concurrency, check for connections leaked by a missing
  try-with-resources, and set connect *and* socket timeouts so a
  stalled call fails instead of hanging forever.
</details>

---

## §12 Rapid-fire traps 🔮 · all **keep**

One line each, out loud, no notes. Anything you fumble is a section to
re-drill.

- [ ] **12.1** Why is `KEYS *` a production incident?
- [ ] **12.2** What happens to a key's TTL when a plain `SET`
  overwrites it?
- [ ] **12.3** Can a single field inside a hash expire on its own?
- [ ] **12.4** Does `MULTI`/`EXEC` roll back on error?
- [ ] **12.5** Is a pipeline atomic?
- [ ] **12.6** Why must a rate-limit counter's `EXPIRE` be conditional?
- [ ] **12.7** Can Redis lose a write it has acknowledged?
- [ ] **12.8** What is missing from `SETNX lock 1` as a lock?
- [ ] **12.9** Why must a lock release be a Lua script?
- [ ] **12.10** On a write, do you update the cache or delete it?
- [ ] **12.11** Why jitter TTLs?
- [ ] **12.12** `volatile-lru` on a keyspace with no TTLs — what
  happens?
- [ ] **12.13** Why does `MSET` fail on a cluster, and what fixes it?
- [ ] **12.14** Is one `Jedis` instance safe to share across threads?
- [ ] **12.15** What does `@Cacheable` do about expiry by default?

<details><summary>Solutions 12</summary>

- 12.1 It blocks every client while it scans the whole keyspace. Use
  `SCAN`.
- 12.2 It is **cleared** — the key becomes permanent. `KEEPTTL`
  preserves it.
- 12.3 No. A TTL belongs to the key; separate lifetimes need separate
  keys. (Redis 7.4's `HEXPIRE` is the exception, and is not widely
  deployed.)
- 12.4 **No.** There is no rollback; a failing command does not stop
  the others. `DISCARD` cancels before `EXEC`, which is not an undo.
- 12.5 **No.** It removes round trips; other clients still interleave.
- 12.6 Because re-arming it on every hit slides the window forward
  with traffic so it never closes. Set it only when `INCR` returns 1.
- 12.7 **Yes** — replication is asynchronous, so a failover can
  promote a replica that never received it. `WAIT` narrows the window
  without closing it.
- 12.8 **An expiry.** Without `PX`/`EX`, a crashed holder locks the
  resource permanently. And a **token**, so release can be safe.
- 12.9 Because `GET`-then-`DEL` is check-then-act: the lease can
  expire and be re-acquired in between, so you delete someone else's
  lock.
- 12.10 **Delete.** `DEL` is idempotent and self-correcting; an update
  races with a concurrent reader and can persist a value the database
  never held.
- 12.11 So keys written together do not expire together and stampede
  the database at the same instant.
- 12.12 Nothing is eligible for eviction, so writes fail with an OOM
  error while memory is full.
- 12.13 The keys hash to different slots and a multi-key command needs
  one slot. A **hash tag** — `{user:1}:…` — forces them together.
- 12.14 **No.** One socket, not thread-safe. Use `JedisPool`.
- 12.15 **Nothing** — it caches forever unless `entryTtl` is
  configured.
</details>

---

## Extensions — the deliberate "later" pile

Spin-offs, each worth its own kit only if a JD or a round asks:

- **`redis-streams-deep.md`** — `XAUTOCLAIM` and dead-letter
  handling, consumer-group lag measurement, stream-vs-Kafka migration.
- **`redis-modules.md`** — RedisJSON paths and `JSON.MERGE`, RediSearch
  index design and aggregations, vector search with HNSW, time series,
  Bloom and Cuckoo filters.
- **`redis-ops.md`** — `INFO` and `MEMORY DOCTOR`, `SLOWLOG`,
  `LATENCY DOCTOR`, big-key hunting with `--bigkeys`, `redis-benchmark`,
  client output-buffer limits, TLS and ACLs.
- **`redis-cluster-deep.md`** — resharding, slot migration, `ASK` vs
  `MOVED`, hot-key mitigation, client-side caching (RESP3 tracking).
- **`spring-data-redis.md`** — `RedisTemplate` configuration,
  `@Cacheable`/`@CacheEvict` semantics, repositories and secondary
  indexes, `RedisMessageListenerContainer`, Lettuce pooling and
  `ClientResources`.
- **Rate-limiter family** — fixed window, sliding-window log via
  sorted sets, sliding-window counter, token bucket in Lua, and the
  accuracy-versus-memory trade between them.

## How to work through this

1. **Core path first** — §4 → §2 → §9 → §10 → §6 → §8. Everything
   else is breadth that only pays once these land.
2. **Say the answer out loud before expanding the solution.** A
   silent read feels like knowing and is not; the fumble only shows
   up in speech.
3. **Prove it at the CLI.** Nearly every exercise here is a handful
   of `redis-cli` commands, and watching `TTL` return `-1` after a
   `SET` beats reading that it does.
4. **Then run the lab.** The Java counterpart in
   [lab/README.md](lab/README.md) turns the same idea into a failing
   test you have to make pass — §4 in particular is not really learnt
   until 60 threads have hit a limit of 20 and stopped at exactly 20.
5. **Mark honestly.** `[x]` means said aloud, unprompted, in one
   breath. A section you could "explain if asked" is not done.
