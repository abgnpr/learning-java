# Redis Rapid-fire Kit ⚓

- Redis Rapid-fire Kit
  - [§1 The model — a data-structure server, not a cache](#1-the-model--a-data-structure-server-not-a-cache)
  - [§2 Keys, expiry and eviction](#2-keys-expiry-and-eviction)
  - [§3 The core types](#3-the-core-types)
  - [§4 Atomicity — INCR, MULTI, WATCH, Lua](#4-atomicity--incr-multi-watch-lua)
  - [§5 Pipelining — latency, not atomicity](#5-pipelining--latency-not-atomicity)
  - [§6 Pub/Sub and Streams](#6-pubsub-and-streams)
  - [§7 Persistence — RDB, AOF and what durable means here](#7-persistence--rdb-aof-and-what-durable-means-here)
  - [§8 Replication, Sentinel and Cluster](#8-replication-sentinel-and-cluster)
  - [§9 Caching patterns — aside, invalidation, stampede](#9-caching-patterns--aside-invalidation-stampede)
  - [§10 Distributed locks](#10-distributed-locks)
  - [§11 Redis from Java](#11-redis-from-java)
  - [§12 IMPS — the anchor ⚓](#12-imps--the-anchor-)
  - [§13 Rapid-fire traps](#13-rapid-fire-traps)

**Why this exists:** Redis sits under two systems on the profile — the
IMPS switch's per-customer debit cap and the CBS gateway's session
state — so a Java round that spots it asks more than "what is Redis
for?". Every term used is defined inside this kit; nothing here defers
to another doc.

**How to drill:** aloud, blind, one section per sitting. Answers are
sized for SPEAKING — say the 1–3 lines, then stop talking. Anchors to
shipped work are marked ⚓; never claim beyond them. The runnable
counterpart is [lab/README.md](lab/README.md); the written drills are
[redis-basics-exercise.md](redis-basics-exercise.md).

> ### 🎯 The 80/20 — what actually gets asked
>
> Redis questions collapse onto one axis: *what is Redis doing that
> your application cannot do for itself?* Everything below is a
> variation on that.
>
> **Tier 1 — own these cold (≈70% of Redis questions)**
>
> > **[§4](#4-atomicity--incr-multi-watch-lua) →
> > [§2](#2-keys-expiry-and-eviction) →
> > [§9](#9-caching-patterns--aside-invalidation-stampede) →
> > [§12](#12-imps--the-anchor-)**
> >
> > atomicity & the check-then-act race · TTL and eviction ·
> > cache-aside and stampede · the IMPS story
>
> - **§4 is THE topic.** Redis executes commands one at a time, so a
>   single command is atomic and a *sequence* of them is not. Every
>   interesting Redis bug lives in that gap, and `MULTI`/`WATCH` and
>   Lua are the two ways out.
> - **§2** — a cache without a TTL is a memory leak with good latency.
>   Know `maxmemory-policy` and what `allkeys-lru` actually evicts.
> - **§9** — cache-aside is what everyone writes; stampede and
>   invalidation are what everyone gets wrong.
> - **§12** — the shipped debit-cap race, named honestly, including
>   the fix. A candidate who names their own design flaw outranks one
>   who recites commands.
>
> **Tier 2 — the follow-ups when Tier 1 lands well (≈25%)**
>
> > **[§8](#8-replication-sentinel-and-cluster) →
> > [§7](#7-persistence--rdb-aof-and-what-durable-means-here) →
> > [§6](#6-pubsub-and-streams) →
> > [§10](#10-distributed-locks)**
> >
> > replication/Sentinel/Cluster · RDB vs AOF · pub/sub vs streams ·
> > why a lock is harder than `SETNX`
>
> These separate "used Redis" from "operated Redis". For a payments
> track, the honest answer about Redis durability (§7) and the honest
> answer about distributed locks (§10) are the two most likely.
>
> **Tier 3 — breadth, skim unless the JD names it (≈5%)**
>
> > [§3](#3-the-core-types) the type tour (cheap, and it is the best
> > opening sentence you can give) ·
> > [§5](#5-pipelining--latency-not-atomicity) pipelining ·
> > [§11](#11-redis-from-java) Jedis/Lettuce/Spring Data ·
> > [§13](#13-rapid-fire-traps) traps as a final self-test
>
> **The five sentences.** If there is time for nothing else:
>
> 1. Redis is an in-memory data-structure server: the value has a
>    type, and the type has operations the server performs.
> 2. Command execution is single-threaded, so one command is atomic
>    and two commands are a race.
> 3. `MULTI` queues commands and `WATCH` aborts them if a key changed;
>    Lua runs a whole decision server-side in one step.
> 4. A key without a TTL never leaves, so eviction policy is the only
>    thing standing between a cache and an out-of-memory kill.
> 5. Redis replication is asynchronous, so a failover can lose
>    acknowledged writes — never make it the system of record for
>    money. ⚓ IMPS keeps the journal in Oracle for exactly this
>    reason.

---

## §1 The model — a data-structure server, not a cache

**What is Redis?** An in-memory data-structure server. A key maps to a
*typed* value — string, hash, list, set, sorted set, stream — and the
server exposes the operations that type deserves. `ZADD` maintains
ordering; `SADD` answers membership; `HINCRBY` increments a field
inside an object.

**Why that framing matters.** "Redis is a cache" explains why you set
things and get them back. It does not explain leaderboards, rate
limiters, queues or session stores, all of which are Redis being a
*database with a small working set* rather than a lookaside.

**Single-threaded command execution.** Redis runs commands one at a
time on one core. Counter-intuitively this is a feature: no locks, no
lock contention, and a strong guarantee that a single command sees a
consistent world. Throughput comes from doing very little per command
and from not blocking — I/O is multiplexed, and Redis 6+ uses extra
threads for reading and writing sockets, not for running commands.

**The consequence to carry everywhere:** one command is atomic; two
commands are a race. That single sentence generates
[§4](#4-atomicity--incr-multi-watch-lua), most of [§9](#9-caching-patterns--aside-invalidation-stampede), and the ⚓ IMPS
story in [§12](#12-imps--the-anchor-).

**The corollary about slow commands.** Because one command blocks the
next, a single `KEYS *` across ten million keys stalls every other
client. `SCAN` exists to iterate in bounded slices; `KEYS` is a
production incident with a convenient syntax.

**Say it:** *"Redis is a single-threaded, in-memory data-structure
server — typed values with server-side operations. One command is
atomic, so latency is predictable and a slow command blocks everyone."*

---

## §2 Keys, expiry and eviction

**The key space is flat.** One namespace of binary-safe strings.
Structure is a naming convention — `user:1:profile`, `otp:asha` — and
colons carry no meaning to the server. That convention is what lets
you reason about ownership, and it is what a `SCAN MATCH` pattern
works on.

**Expiry is a property of the key.** `EXPIRE key 60`, or attached to
the write itself with `SET key value EX 60`. Prefer the second: a
`SET` followed by an `EXPIRE` is two commands, and a crash between
them leaves a key that never expires.

- `TTL key` → seconds left, `-1` = no expiry, `-2` = no key.
- Overwriting a key with plain `SET` **clears** its TTL. Overwriting a
  field with `HSET` does not touch the key's TTL.
- A TTL belongs to the key. A hash's fields cannot expire
  individually — if fields need independent lifetimes, they need
  independent keys.

**How expiry actually happens.** Lazily, when a key is accessed, plus
a background sampling pass. So an expired key can occupy memory for a
while after its deadline. Expiry is a correctness contract about what
you can *read*, not a promise about when memory returns.

**Eviction is what happens when memory runs out**, and it is
configured separately from expiry:

| `maxmemory-policy` | Evicts | Use when |
|---|---|---|
| `noeviction` | nothing — writes fail | Redis holds data you cannot lose |
| `allkeys-lru` | least recently used, any key | a pure cache |
| `allkeys-lfu` | least *frequently* used, any key | a cache with a hot core and a long tail |
| `volatile-lru` | least recently used **among keys with a TTL** | one instance mixing cache and non-cache data |
| `volatile-ttl` | nearest expiry first | ditto, when TTL approximates value |

**The trap in `volatile-*`:** if nothing has a TTL, there is nothing
eligible, and the policy degrades to `noeviction` — writes start
failing with an OOM error while memory looks full of evictable-looking
data.

**Say it:** *"Expiry is per key and best set in the same command as
the write. Eviction is separate: `maxmemory` plus a policy. A cache
should be `allkeys-lru`; `noeviction` means writes fail instead of
data vanishing, which is what you want if Redis holds anything you
cannot rebuild."*

---

## §3 The core types

Six types carry almost everything. What matters is which operation the
server performs on your behalf.

**String** — bytes, up to 512 MB. Also the numeric type: `INCR`,
`INCRBY`, `INCRBYFLOAT` parse, add and store in one atomic step.
*Uses:* cached payloads, counters, OTPs, flags, serialized objects.

**Hash** — a map inside one key. `HSET`/`HGET`/`HGETALL`/`HINCRBY`.
*Uses:* an object whose fields you read and write independently — a
user profile, a session. One key means one TTL and one place in the
key space, which matters for [Cluster](#8-replication-sentinel-and-cluster).

**List** — a linked list with cheap ends. `LPUSH`/`RPOP` make a queue;
`BRPOP` blocks until something arrives; `BLMOVE` moves an item to a
second list atomically, which is how you build a queue that survives a
consumer crash.
*Uses:* work queues, capped activity feeds (`LTRIM`).

**Set** — unordered, unique. `SADD` reports whether the member was
new, which answers "have I seen this?" in the write itself.
`SINTER`/`SDIFF`/`SUNION` do set algebra server-side.
*Uses:* unique visitors, tags, de-duplication, "who is online".

**Sorted set** — every member carries a score, and the set is kept in
score order at write time. `ZADD`, `ZINCRBY`, `ZRANGE`/`ZREVRANGE`,
`ZRANK`, `ZRANGEBYSCORE`.
*Uses:* leaderboards, priority queues, sliding-window rate limiters
(score = timestamp), anything "top N" or "between these two values".

**Stream** — an append-only log of entries with increasing ids,
`XADD`/`XRANGE`/`XREADGROUP`. Reading does not consume.
*Uses:* event logs, work distribution with acknowledgement
([§6](#6-pubsub-and-streams)).

**Also worth naming if asked:** bitmaps and `BITCOUNT` (dense boolean
sets, one bit per id), HyperLogLog and `PFADD`/`PFCOUNT` (cardinality
of billions in 12 KB, ~0.8% error), and geospatial `GEOADD`/`GEOSEARCH`
(a sorted set with geohash scores).

**The choosing rule:** pick the type whose *server-side operation*
matches the question you will ask. Counting distinct things is a set
or a HyperLogLog, not a string you parse in Java. Ranking is a sorted
set, not a list you sort after fetching.

**Say it:** *"Strings, hashes, lists, sets, sorted sets, streams. I
choose by the operation I need the server to perform — membership is a
set, ranking is a sorted set, an object with independent fields is a
hash."*

---

## §4 Atomicity — INCR, MULTI, WATCH, Lua

**The whole section in one line:** a single command is atomic; a
sequence of commands is a race. Four tools close the gap, in
increasing order of power.

**1. A single command that already does the job.** The best answer.
`INCR` instead of `GET` then `SET`. `SADD` instead of `SISMEMBER`
then `SADD`. `SET key v NX EX 60` instead of `EXISTS` then `SET` then
`EXPIRE`. If one command expresses the intent, there is no race to
discuss.

**2. `MULTI`/`EXEC` — a queued batch.** Commands between `MULTI` and
`EXEC` are queued and then run as one uninterrupted block.

- What it gives: **isolation**. Nothing interleaves.
- What it does *not* give: rollback. There is no abort-on-error — a
  command that fails at runtime (wrong type for the key) fails, and
  the rest still run. `DISCARD` throws away a batch before `EXEC`,
  which is not the same thing.
- What it also does not give: reading a value and *deciding* with it.
  Queued commands return placeholders, not values.

**3. `WATCH` — optimistic concurrency.** `WATCH key` before reading.
If any watched key is modified by anyone before your `EXEC`, the
`EXEC` runs nothing and reports nil, and you retry.

```
WATCH balance
GET balance          → decide in the client
MULTI
DECRBY balance 60
INCRBY other   60
EXEC                 → nil if balance changed since WATCH
```

This is compare-and-swap, with the same shape as
`AtomicInteger.compareAndSet` — including the retry loop, which is
your responsibility.

**4. Lua via `EVAL` — the decision itself moves server-side.** Redis
runs a script to completion with no other client interleaved, so
read, branch and write become one indivisible step.

```lua
local used = redis.call('INCR', KEYS[1])
if used == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end
if used > tonumber(ARGV[1]) then return -1 end
return tonumber(ARGV[1]) - used
```

- `KEYS` and `ARGV` are separate on purpose: naming keys is what lets
  Cluster route the script.
- `EVALSHA` runs a cached script by digest, so the body crosses the
  wire once.
- Because a script blocks everything, it must stay short. A slow
  script is an outage.

**Choosing between them:** one command if it exists; `WATCH` when
contention is rare and a retry is cheap; Lua when the decision is
non-trivial or contention is high. Lua is usually the right answer for
limits and quotas, because retrying under contention wastes exactly
the round trips you were trying to save.

**Say it:** *"One command is atomic, several are not. `MULTI` gives
isolation but no rollback and no mid-batch decision; `WATCH` turns it
into compare-and-swap with a client retry; Lua moves the decision to
the server and runs it uninterrupted. For a rate limit I'd use Lua."*

⚓ **This is the IMPS answer too** — see [§12](#12-imps--the-anchor-).

---

## §5 Pipelining — latency, not atomicity

**The problem it solves.** One command per round trip means 1000
commands cost 1000 round trips. On a 0.5 ms network that is half a
second of pure waiting, with the server idle throughout.

**What a pipeline is.** The client writes many commands without
waiting for each reply, then reads all the replies. The server is
unchanged — it still executes them one at a time, interleaved freely
with other clients' commands.

**The distinction that gets confused in interviews:**

| | Pipelining | `MULTI`/`EXEC` |
|---|---|---|
| Buys | fewer round trips | isolation |
| Atomic | no | yes |
| Other clients interleave | yes | no |
| Can read a value mid-batch | no | no |

They compose: a pipeline can contain a `MULTI`/`EXEC`.

**Where it breaks down.** An unbounded pipeline buffers every reply in
client memory; batch in chunks of a few thousand. And in a Cluster a
pipeline must be split per node, which is why client libraries expose
it more cautiously there.

**Say it:** *"Pipelining removes round trips, not races. The commands
still execute one at a time and other clients still interleave — if I
need isolation that's `MULTI`, and if I need a decision that's Lua."*

---

## §6 Pub/Sub and Streams

Two fan-out mechanisms that answer different questions.

**Pub/Sub — fire and forget.** `PUBLISH channel message` delivers to
whoever is subscribed *at that instant* and then forgets. No history,
no acknowledgement, no replay. A subscriber that reconnects a second
later has no way to learn what it missed, and `PUBLISH` returns the
number of subscribers reached — the only delivery report the protocol
offers.

*Use it for:* cache invalidation broadcasts, live notifications,
presence — anything where a missed message is genuinely fine.

**Streams — an append-only log.** `XADD` appends an entry with a
server-assigned increasing id. `XRANGE` reads without consuming, so
two readers both see everything and a late reader sees the history.
`XLEN` measures; `XTRIM MAXLEN` bounds.

**Consumer groups — work sharing with acknowledgement.**
`XGROUP CREATE` makes a group; `XREADGROUP` hands each entry to
exactly one member; the entry then sits in the group's **pending**
list until `XACK`. `XPENDING` shows what is outstanding and
`XAUTOCLAIM`/`XCLAIM` moves a dead worker's entries to a live one.

That pending list is the whole point: a crashed worker's messages are
recoverable rather than lost, which pub/sub cannot offer at all.

**Against Kafka, honestly.** Streams give ordering, groups,
acknowledgement and replay — the same shapes. Kafka gives partitioned
horizontal scale, long retention on disk, and an ecosystem. Redis
Streams are excellent when the working set is small and you already
run Redis; they are not a Kafka replacement for a payment switch.

**Say it:** *"Pub/sub is fire-and-forget to whoever is connected;
streams are a log that reading doesn't consume. Consumer groups add
one-entry-to-one-worker plus a pending list, so an unacknowledged
message can be reclaimed rather than lost."*

---

## §7 Persistence — RDB, AOF and what durable means here

**RDB — point-in-time snapshots.** A fork writes the dataset to disk
every N seconds / M changes. Compact, fast to restore, cheap to run.
The cost is a window: everything since the last snapshot is lost on a
crash.

**AOF — an append-only command log.** Every write is appended, and the
file is rewritten periodically to stay compact. `appendfsync` decides
the guarantee:

| `appendfsync` | Loses on crash | Cost |
|---|---|---|
| `always` | ~nothing | an fsync per write; much slower |
| `everysec` | up to 1 second | the default, and the usual answer |
| `no` | whatever the OS held | fastest, weakest |

**Both together** is the common production setting: AOF for recovery
granularity, RDB for fast restarts and backups.

**The sentence that matters in a payments interview.** Even
`appendfsync always` on a single node does not make Redis a ledger.
Replication is asynchronous ([§8](#8-replication-sentinel-and-cluster)), so a master can
acknowledge a write, fail, and be replaced by a replica that never
received it. `WAIT` can block until N replicas acknowledge, which
narrows the window without closing it.

**Say it:** *"RDB is snapshots, AOF is a command log with
`everysec` as the usual trade-off, and most production runs both.
But replication is asynchronous, so an acknowledged write can still
be lost in a failover — I wouldn't put the money record in Redis."*

⚓ IMPS keeps the transaction journal in Oracle and uses Redis for the
debit cap, which is exactly this reasoning applied.

---

## §8 Replication, Sentinel and Cluster

Three separate things, often muddled into one.

**Replication** — a replica follows a master asynchronously.
`REPLICAOF host port`. Gives read scale-out and a warm standby. Does
**not** give failover by itself, and does not give durability, because
the master does not wait for the replica.

**Sentinel** — availability for a single master.

- A set of sentinel processes monitors the master and each other.
- When a quorum agrees the master is down, they elect one and
  **promote a replica**.
- Clients connect *through* the sentinels: the client asks "who is the
  master?" instead of hard-coding an address, and follows the answer
  across a failover.
- Typically three sentinels, so a quorum survives losing one.

Sentinel changes who the master is. It does not shard: one master
still holds the whole dataset.

**Cluster** — horizontal scale, by sharding.

- The key space is divided into **16384 hash slots**; each master owns
  a range. `slot = CRC16(key) mod 16384`.
- Clients learn the slot map and route directly to the owning node; a
  `MOVED` reply corrects a stale map.
- Each master can have replicas, and the cluster fails over on its own
  — no Sentinel needed.

**The Cluster constraint worth knowing cold:** a multi-key command
runs only when every key lives in the same slot. `MSET a 1 b 2` across
two slots is refused with `CROSSSLOT`. The lever is a **hash tag** —
only the part of the key inside braces is hashed:

```
{user:1}:profile     and   {user:1}:sessions     → same slot
user:1:profile       and   user:1:sessions       → almost certainly not
```

So related keys that must be touched together are pinned together on
purpose. The same rule governs Lua scripts and transactions in a
cluster.

**Choosing:** replication for read scale and standby; Sentinel when
one node holds the data and you need it to survive a failure; Cluster
when one node cannot hold the data or serve the traffic. Cluster costs
you cross-slot operations, so it is not a free upgrade.

**Say it:** *"Replication is async copying; Sentinel adds automatic
failover for a single master and tells clients who it is; Cluster
shards 16384 slots across masters and fails over itself. Cluster's
price is that multi-key commands need the keys in one slot, which hash
tags let you force."*

---

## §9 Caching patterns — aside, invalidation, stampede

**Cache-aside (lazy loading)** — what almost everyone writes:

```
read:   value = redis.get(k)
        if miss: value = db.load(k); redis.set(k, value, EX ttl)
write:  db.update(k); redis.del(k)
```

**Delete on write, do not update on write.** A `DEL` is idempotent and
self-correcting: the next reader repopulates from the source of truth.
A `SET` on write races with a concurrent reader's `SET` and can leave
the cache holding a value the database never had.

**The other two patterns, briefly.** *Write-through* writes cache and
database together, keeping them consistent at the cost of write
latency. *Write-behind* writes the cache and flushes to the database
asynchronously — fastest, and it can lose data. Cache-aside is the
default because it degrades safely: a cold cache is slow, not wrong.

**Stampede (a.k.a. dogpile).** A hot key expires; a hundred concurrent
requests all miss; a hundred identical loads hit the database at once.
Three fixes, usually combined:

1. **A rebuild lock.** The first miss takes `SET k:lock 1 NX EX 10`
   and rebuilds; the others briefly wait or serve stale.
2. **Serve stale while rebuilding.** Store the value with a logical
   expiry *inside* it and a longer physical TTL. Past the logical
   expiry one caller refreshes while everyone else keeps getting the
   old value. Nobody ever sees a miss.
3. **Jitter the TTL.** `ttl + random(0, 10%)` so keys written together
   do not expire together. This is the one-line fix for the thundering
   herd that follows a bulk warm-up.

**Two more failure shapes worth naming:**

- **Cache penetration** — repeated lookups for a key that does not
  exist anywhere, so every request reaches the database. Cache the
  negative result with a short TTL, or keep a Bloom filter of known
  ids.
- **Cache avalanche** — a large fraction of the key space expiring (or
  the cache restarting empty) and the database taking full traffic.
  TTL jitter, staged warm-up, and a request limiter in front of the
  loader.

**Say it:** *"Cache-aside, and on write I delete rather than update —
delete is idempotent, update races. The failure to name is the
stampede: a hot key expires and everyone rebuilds it at once, so I'd
use a rebuild lock or serve-stale, plus TTL jitter so keys don't
expire in lockstep."*

---

## §10 Distributed locks

**The naive version, and why it is nearly right:**

```
SET lock:resource <random-token> NX PX 30000
```

`NX` makes acquisition atomic — exactly one caller wins. `PX` is
mandatory: without an expiry, a holder that crashes locks the resource
forever.

**Release must be conditional, and that needs Lua.** Checking "is this
my token?" and then deleting is check-then-act: the lock can expire
between the two, be acquired by someone else, and your `DEL` then
releases *their* lock.

```lua
if redis.call('GET', KEYS[1]) == ARGV[1] then
  return redis.call('DEL', KEYS[1])
else
  return 0
end
```

The random token is what makes this possible — a lock you cannot
identify is a lock you cannot safely release.

**The honest part.** This is a *lease*, not a lock. If the holder
stalls — a long GC pause, a slow disk — the lease can expire while it
still believes it holds the lock, and two holders run at once. No
amount of Redis configuration removes that. Redlock, the multi-node
algorithm, is disputed for exactly this reason.

**So what do you actually do?** Make the protected operation safe
without the lock: an idempotency key, a conditional write, a database
uniqueness constraint, or a fencing token that the downstream resource
checks and rejects if stale. The lock becomes an optimisation that
prevents wasted work, not the thing correctness rests on.

**Say it:** *"`SET key token NX PX ttl` to acquire, and a Lua
compare-and-delete to release, because release is check-then-act. But
it's a lease — a GC pause can let two holders overlap — so I don't
make correctness depend on it. The real guard is an idempotency key or
a unique constraint at the resource."*

---

## §11 Redis from Java

**Jedis** — synchronous, one connection per operation, borrowed from a
pool. The API maps one-to-one onto Redis commands, which is exactly
what you want while learning: `jedis.set`, `jedis.hincrBy`,
`jedis.eval`. A `Jedis` instance is **not thread-safe**; use
`JedisPool` and one connection per thread.

**Lettuce** — Netty-based, thread-safe, supports synchronous,
asynchronous and reactive calls over a shared connection. It is Spring
Boot's default client, and the right choice for reactive stacks or
high connection counts.

**Spring Data Redis** — a layer over either. `RedisTemplate` /
`StringRedisTemplate` for commands, `@Cacheable`/`@CacheEvict` for
declarative caching, `CrudRepository` for hash-mapped objects.

**Two things the abstraction hides that interviews ask about:**

1. **Serialization.** A default `RedisTemplate` uses JDK
   serialization, which produces unreadable keys and values and ties
   the data to your class definitions. Configure
   `StringRedisSerializer` for keys and a JSON serializer for values.
2. **TTL.** `@Cacheable` with no configured TTL caches forever. The
   `RedisCacheConfiguration` has `entryTtl`, and it is not set by
   default.

**Connection choices that matter in production:** pool size (a
blocking pool that runs out looks like Redis being slow), timeouts
(connect *and* socket — a read with no timeout is an unbounded hang),
and, for Cluster, `maxRedirects`.

**Say it:** *"Jedis is synchronous and simple, one connection per
thread from a pool; Lettuce is thread-safe, Netty-based and Spring
Boot's default. Spring Data Redis wraps both — and the two things it
hides that bite are the default JDK serializer and `@Cacheable`
caching forever unless you set `entryTtl`."*

---

## §12 IMPS — the anchor ⚓

**The shape.** The IMPS switch runs a Spring Boot 2.7 receiver, Kafka
topics, seven plain-Java consumers and a reversion saga. The Oracle
transaction journal is the single source of truth. Redis holds a
**per-customer debit cap** for outward payments, and the CBS gateway
on the other project holds **session state** in Redis with a sliding
TTL so any of several workers can serve the next request.

**The story to tell — a race I shipped.** The debit-cap check is
`hget` → compare in Java → `hset`. That is check-then-act, and it is
not atomic: two concurrent outward payments for the same customer can
both read the same balance, both decide there is room, and both pass.
The cap is exceeded, and neither request did anything wrong.

**The fix**, and why each option works:

- **`WATCH` + `MULTI`/`EXEC`** — watch the cap key, read it, queue the
  update; if anything touched the key in between, `EXEC` does nothing
  and the caller retries.
- **A Lua script** — move read-check-write into one server-side step
  so no other client can interleave. Better under contention, because
  there is no retry storm.

**Why this story lands.** It is the same defect as `count++` in a
thread — read-modify-write with a gap — moved across the network. It
connects Redis atomicity, the Java memory model, and payment
correctness in one breath, and it names a flaw in your own design
rather than reciting a command list.

**The boundary to keep.** The journal is in Oracle, not Redis, and
that is deliberate: [§7](#7-persistence--rdb-aof-and-what-durable-means-here) and
[§8](#8-replication-sentinel-and-cluster) are the reasons. Redis holds a cap that can be
rebuilt; Oracle holds the money.

**Never claim:** exactly-once Kafka semantics, atomic Redis operations
in the shipped code, or unit-test coverage on this system.

**Say it:** *"We tracked a per-customer debit cap in Redis as
`hget`-compare-`hset`. That's check-then-act, so two concurrent
outward payments could both pass the cap — the same lost-update shape
as `count++`, over a network. The fix is `WATCH`/`MULTI` or a Lua
script; under contention I'd pick Lua. The journal stayed in Oracle
because Redis replication is asynchronous and I wouldn't put the money
record behind it."*

---

## §13 Rapid-fire traps

| Trap | The answer |
|---|---|
| "Redis is single-threaded, so it must be slow" | It does very little per command and never blocks on locks; throughput is hundreds of thousands of ops/sec. Slow means *your* command was slow. |
| `KEYS *` in production | Blocks every client for the duration. Use `SCAN`, which iterates in bounded slices. |
| `SET` then `EXPIRE` | Two commands; a crash between them leaves an immortal key. Use `SET k v EX n`. |
| Plain `SET` on a key with a TTL | Clears the TTL. The key now lives forever. |
| Expecting per-field expiry in a hash | TTL is per key. Independent lifetimes need independent keys. |
| `MULTI` rolls back on error | It does not. There is no rollback; a failing command does not stop the others. |
| Deciding inside `MULTI` | Queued commands return placeholders. To branch on a value, use `WATCH` or Lua. |
| Pipelining is atomic | It is not. It removes round trips; other clients still interleave. |
| `INCR` then `EXPIRE` on every hit | The window slides forward with traffic and never closes. Set the expiry only when the counter came back as 1. |
| "Redis guarantees my write survived" | Replication is asynchronous. An acknowledged write can be lost in a failover. `WAIT` narrows the window; it does not close it. |
| `SETNX` as a lock, no expiry | A crashed holder locks the resource forever. Always `NX` **and** `PX`. |
| `DEL` to release someone else's lock | Check-then-act. Compare the token and delete in one Lua script. |
| Redlock makes locks safe | A stalled holder can still overlap. Correctness belongs in idempotency keys or unique constraints. |
| Updating the cache on write | Races with concurrent readers. Delete instead; the next read repopulates. |
| All TTLs the same | They expire in lockstep and stampede the database. Jitter them. |
| `volatile-lru` on a keyspace with no TTLs | Nothing is eligible, so writes start failing with OOM. |
| `@Cacheable` with no TTL | Caches forever. Set `entryTtl` on `RedisCacheConfiguration`. |
| Sharing one `Jedis` across threads | Not thread-safe. Use `JedisPool`, one connection per thread. |
| `MSET` across keys in a cluster | `CROSSSLOT` unless the keys share a hash tag. |
| Redis Streams "replace Kafka" | Same shapes, different scale and retention story. Fine beside an existing Redis; not a switch's backbone. |

---

## Rep scorecard — 🟢 only after a blind aloud rep

| Section | Tier | Rep 1 | Rep 2 | Rep 3 |
|---|---|---|---|---|
| §4 Atomicity | 1 | ☐ | ☐ | ☐ |
| §2 Keys, expiry, eviction | 1 | ☐ | ☐ | ☐ |
| §9 Caching patterns | 1 | ☐ | ☐ | ☐ |
| §12 IMPS anchor ⚓ | 1 | ☐ | ☐ | ☐ |
| §8 Replication, Sentinel, Cluster | 2 | ☐ | ☐ | ☐ |
| §7 Persistence | 2 | ☐ | ☐ | ☐ |
| §6 Pub/Sub and Streams | 2 | ☐ | ☐ | ☐ |
| §10 Distributed locks | 2 | ☐ | ☐ | ☐ |
| §3 The core types | 3 | ☐ | ☐ | ☐ |
| §5 Pipelining | 3 | ☐ | ☐ | ☐ |
| §11 Redis from Java | 3 | ☐ | ☐ | ☐ |
| §1 The model | 3 | ☐ | ☐ | ☐ |
| §13 Rapid-fire traps | 3 | ☐ | ☐ | ☐ |

*Tier 1 earns a second rep before Tier 3 gets a first one.*
