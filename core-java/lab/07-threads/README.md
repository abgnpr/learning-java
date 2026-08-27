# Threads + JVM — the hands-on lab 🐧🔬

Companion to [threads-jvm.md](../../threads-jvm.md). That kit is the abstract
theatre — races, visibility, deadlock — that only *clicks* once you watch
it misbehave in front of you. Eight tiny runnable stations. You **predict
the output first**, then run, then read *why*. That's the loading rep.

## How to run

Java 21, single-file launcher — no build, no `javac`. From this folder:

```bash
cd core-java/lab/07-threads
java 01-start-vs-run/StartVsRun.java
```

Two stations **hang on purpose** (S3, S5). That hang *is* the lesson.
Kill with `Ctrl-C`, or guard it:
`timeout 6 java 03-visibility/Visibility.java`.

## The drill protocol (do NOT skip the predict)

1. **PREDICT** — read the crux, write your expected output on paper. Out loud.
2. **RUN** — `java <station-folder>/<file>.java`.
3. **EXPLAIN** — reveal below. If your prediction missed, that gap *is* the
   thing you didn't actually understand. Say the interview one-liner aloud.

> **Fleet law:** reading isn't loading. A station is 🟢 only when you
> predicted its output correctly **and** said the one-liner blind.

---

## S1 — `start()` vs `run()`  · file: [StartVsRun.java](01-start-vs-run/StartVsRun.java)

**Proves:** `start()` spawns a new thread that calls `run()`; calling
`run()` directly is just an ordinary method call on the current thread.

```java
new Worker().run();        // direct call — runs on THIS thread
new Worker().start();      // spawns a NEW thread, which calls run()
```

<details><summary><b>Reveal</b> — predict first</summary>

```
run() executed on thread: main        <- direct call, no new thread
run() executed on thread: Thread-N     <- start() spawned one
main is running on thread: main
```
`run()` alone = zero concurrency. The trap MCQ answer.

**Say it:** *"start() creates the thread and the JVM calls run() on it;
calling run() yourself just runs it on the caller — no new thread."*
</details>

---

## S2 — the lost update (race condition)  · file: [LostUpdate.java](02-lost-update/LostUpdate.java)

**Proves:** `i++` is read-modify-write — three steps, not atomic. Eight
threads racing on a plain `int` **lose** updates. `AtomicInteger` (CAS)
doesn't.

```java
unsafe++;                  // read, +1, write — interleavable → lost updates
safe.incrementAndGet();    // one atomic CAS step
```

<details><summary><b>Reveal</b> — predict the two totals first</summary>

```
expected : 800000
unsafe   : 313443   (lost 486557)   <- your number will differ every run
safe     : 800000
```
Over **half** the increments vanished. Rerun — the loss changes, because
it depends on interleaving. That non-determinism *is* the race.

⚓ **This is your IMPS story.** The Redis debit-limit check
(`hget` → compare → `hset`) is the exact same non-atomic read-modify-write
across two concurrent outward payments — both can pass. Fix: MULTI/WATCH
or a Lua script. (threads-jvm.md §2, "Race condition".)

**Say it:** *"i++ is read-modify-write, so two threads interleave and lose
updates. Flags → volatile; counters → AtomicInteger; compound state → a lock."*
</details>

---

## S3 — visibility (`volatile`)  · file: [Visibility.java](03-visibility/Visibility.java)  ⚠️ hangs

**Proves:** without `volatile`, one thread can spin on a **stale cached
copy** of a flag forever — the JIT hoists the read out of the loop.

```java
static boolean stop = false;      // <-- add `volatile` to fix
while (!stop) { spins++; }        // may never see main's stop=true
```

<details><summary><b>Reveal</b> — predict: does it terminate?</summary>

```
main set stop=true — waiting for worker...
   ...hangs forever (exit 124 under timeout)
```
The worker never sees the flip. **Now add `volatile` to `stop` and rerun** —
it stops immediately.

> Honesty: this is JIT/CPU-dependent — on some boxes the plain version
> happens to terminate. The point isn't "it always hangs," it's that
> **`volatile` guarantees it never does** (forces every read/write through
> main memory + a happens-before edge). It fixes *visibility*, not atomicity
> — S2's `i++` would still race even if volatile.

**Say it:** *"volatile is a visibility + ordering guarantee, not atomicity.
It stops a thread reading a stale cached copy — but i++ on a volatile is
still a race."*
</details>

---

## S4 — `wait()` vs `sleep()`, the famous one  · file: [WaitVsSleep.java](04-wait-vs-sleep/WaitVsSleep.java)

**Proves the memory hook physically:** *sleep clutches the lock; wait lets
go.* Same setup twice — a holder takes the lock, a waiter tries to enter.

```java
synchronized (lock) { Thread.sleep(2000); }   // KEEPS the monitor
synchronized (lock) { lock.wait(2000);   }     // RELEASES the monitor
```

<details><summary><b>Reveal</b> — predict WHEN the waiter gets in, each scenario</summary>

```
=== sleep(2000): holder KEEPS the lock ===
[   2ms] holder got lock, sleep(2000) — clutching it
[2026ms] holder leaving synchronized block
[2027ms] waiter *** I GOT IN ***          <- blocked the whole 2 seconds

=== wait(2000): holder RELEASES the lock ===
[   1ms] holder got lock, wait(2000) — letting go
[ 101ms] waiter *** I GOT IN ***          <- got in almost immediately
[2002ms] holder leaving synchronized block
```
Sleep → waiter waits 2000ms. Wait → waiter in at 101ms. You just *saw* the
lock change hands. (`wait` must be inside `synchronized` or it throws
`IllegalMonitorStateException` — try deleting the `synchronized` to see it.)

**Say it:** *"wait is an Object method, releases the monitor, must be inside
synchronized, woken by notify. sleep is a Thread static method, holds every
lock, wakes itself on timeout."*
</details>

---

## S5 — deadlock, then fix it  · file: [Deadlock.java](05-deadlock/Deadlock.java)  ⚠️ hangs

**Proves:** two threads acquiring two locks in **opposite order** deadlock.
Consistent lock ordering fixes it.

```java
// t1:  lock A -> wants B
// t2:  lock B -> wants A     <-- opposite order = deadlock
```

<details><summary><b>Reveal</b> — predict, then find it like an on-call engineer</summary>

```
t1 holds A, wants B
t2 holds B, wants A
   ...hangs forever (exit 124 under timeout)
```
Now **diagnose it live** — this is a real ops skill:

```bash
java 05-deadlock/Deadlock.java &  # or run in another terminal
jps                            # find the pid
jstack <pid> | grep -A6 -i deadlock
#  -> "Found one Java-level deadlock" + the exact two threads + locks
```

**Fix:** make both threads lock in the *same* order (A then B). Edit t2's
two `synchronized` blocks (`B`→`A`, `A`→`B`), rerun → "both finished".

**Say it:** *"Deadlock = each thread holds a lock the other wants. Prevent
it with a global lock ordering, or tryLock with a timeout so a thread backs
off instead of blocking forever."*
</details>

---

## S6 — thread pool = workers + a shared in-tray  · file: [PoolReuse.java](06-pool-reuse/PoolReuse.java)

**Proves the mental picture:** a `newFixedThreadPool(3)` is 3 worker threads
pulling from one shared queue. 6 tasks → 3 workers get **reused**.

```java
ExecutorService pool = Executors.newFixedThreadPool(3);
for (int i = 1; i <= 6; i++) pool.submit(...);   // 6 tasks, 3 workers
```

<details><summary><b>Reveal</b> — predict how many distinct thread names appear</summary>

```
task 1 ran on pool-1-thread-1
task 2 ran on pool-1-thread-2
task 3 ran on pool-1-thread-3
task 4 ran on pool-1-thread-3    <- worker 3 freed up, grabbed t4
task 5 ran on pool-1-thread-1
task 6 ran on pool-1-thread-2
```
Only **three** names — t4/t5/t6 waited in the internal `BlockingQueue` until
a worker freed up. That's the whole point: no thread-per-task explosion.

**Say it:** *"A pool is a small crew plus a shared in-tray. Fixed pool for
steady CPU-bound load (size ≈ cores); cached for bursty short I/O; shutdown()
drains the queue, shutdownNow() interrupts and returns the un-run tasks."*
</details>

---

## S7 — producer/consumer + backpressure  · file: [ProducerConsumer.java](07-producer-consumer/ProducerConsumer.java)

**Proves:** a `BlockingQueue` *is* the producer-consumer pattern — `put()`
blocks when full, `take()` blocks when empty. No hand-rolled wait/notify.

```java
queue.put("txn-" + i);   // producer BLOCKS if the queue is full
String txn = queue.take();// consumer BLOCKS if the queue is empty
```

<details><summary><b>Reveal</b> — predict what the queue size does over time (cap = 5)</summary>

```
produced txn-8   (queue size 5)
produced txn-9   (queue size 5)
produced txn-10  (queue size 5)   <- pinned at 5: producer is now blocked in put()
```
Consumer is slower, so the queue fills to its cap and **stays there** —
the producer is throttled automatically. That's backpressure, for free.

⚓ **This is IMPS's Kafka shape.** Producers (NPCI/CBS callers) append to a
topic; the consumer `poll()`s at its own pace; the partition log is the
shared buffer. (threads-jvm.md §4 "Producer-consumer" + core-java.md §8 Kafka.)

**Say it:** *"Producer-consumer is a shared buffer between them. In real code
it's a BlockingQueue — put/take handle the waiting — nobody hand-rolls
wait/notify. Kafka is the distributed version of the same idea."*
</details>

---

## S8 — a million virtual threads (Java 21)  · file: [VirtualThreads.java](08-virtual-threads/VirtualThreads.java)

**Proves:** virtual threads are KB-scale — a million blocking-I/O tasks is
fine. Platform threads (1 OS thread each, MB stacks) would OOM long before.

```java
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1_000_000; i++)
        ex.submit(() -> Thread.sleep(100));   // simulated I/O
}
```

<details><summary><b>Reveal</b> — predict: does 1,000,000 threads even start?</summary>

```
1,000,000 virtual threads finished in 8686ms
done = 1000000
```
All million ran. Each *mounted* a carrier platform thread, and the moment it
blocked on `sleep` the JVM *unmounted* it and lent the carrier out. Try
`Executors.newFixedThreadPool(1_000_000)` for contrast — you can't afford the
OS threads.

> Honesty (truth law): IMPS is Spring Boot 2.7, *predates* virtual threads.
> Speak of these as *"what I'd reach for today for blocking-I/O fan-out,"*
> not as something you shipped. CPU-bound work gains nothing from them.
> ([java-versions.md → Java 21](../../java-versions.md#java-21-2023--lts--virtual-threads))

**Say it:** *"A virtual thread is JVM-managed and KB-scale; it mounts a
carrier to run and unmounts on blocking I/O. So I/O-bound work is just
one-virtual-thread-per-task — no pool-sizing math. CPU-bound gains nothing."*
</details>

---

## Scorecard — station → §5 claim

Mark 🟢 only after a correct blind prediction **and** the one-liner aloud.

| # | Station | §5 claim it loads | ⚓ anchor | Rep |
|---|---|---|---|---|
| 1 | StartVsRun | run() vs start() | — | ⬜ |
| 2 | LostUpdate | race / atomicity | IMPS Redis debit-limit | ⬜ |
| 3 | Visibility | volatile ≠ atomicity | — | ⬜ |
| 4 | WaitVsSleep | wait vs sleep | — | ⬜ |
| 5 | Deadlock | deadlock + lock ordering | — | ⬜ |
| 6 | PoolReuse | ExecutorService pools | — | ⬜ |
| 7 | ProducerConsumer | producer/consumer + BlockingQueue | IMPS Kafka | ⬜ |
| 8 | VirtualThreads | virtual threads (21) | "what I'd use today" | ⬜ |

*One thread of execution. Eight ways to watch it break. Predict, run, know.* 🐧
