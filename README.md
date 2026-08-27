# learning-java

Interview preparation notes for Java, Spring Boot and Kafka —
rapid-fire answers, study material, and hands-on labs.

Callsign **HOT COFFEE**, base 6 of the interview fleet. Operating
charter lives in [CLAUDE.md](CLAUDE.md); HQ is
`~/Documents/interview-hq`.

## What's in here

| File | What it is |
|---|---|
| [core-java.md](core-java/core-java.md) | Rapid-fire Q&A across OOP, collections, exceptions, Java 8+, traps, Spring/JDBC/REST, Kafka and SDLC/testing. Answers sized for **speaking**, not writing. |
| [java-versions.md](core-java/java-versions.md) | Java 7 → 25 timeline: version, year, headline feature, one snippet. |
| [java-8.md](core-java/java-8.md) | The Tier-1 "Java 8 from zero" study block (§A–§I) — functional interfaces, lambdas, streams, `Optional`, `java.time`. |
| [threads-jvm.md](core-java/threads-jvm.md) | Threads, locks, the memory model, executors and JVM internals — ordered so that no answer depends on one below it. |
| [Threads lab index](core-java/lab/07-threads/README.md) | Eight runnable stations for the concurrency section — races, visibility, deadlock, pools, virtual threads. |
| [Core Java lab index](core-java/lab/README.md) | 64 self-testing challenges plus eight concurrency stations, organized as one numbered learning path. |
| [spring-boot-basics.md](spring-boot/spring-boot-basics.md) | DI/IoC, beans and scopes, auto-config, web MVC, validation, data/JPA, transactions and proxies, testing, actuator. |
| [spring-security-basics.md](spring-boot/spring-security-basics.md) | Senior anti-fumble kit: filter-chain architecture, authentication, route/method authorization, SpEL, JWT, sessions, CSRF/CORS, error boundaries, tests and production scenarios. |
| [spring-data-jpa-performance.md](spring-boot/spring-data-jpa-performance.md) | Senior persistence kit: entity states, mappings, fetch plans/N+1, projections, pagination, JDBC batching, bulk DML, locking, caches, OSIV, diagnostics and production scenarios. |
| [kafka-basics-exercise.md](kafka/kafka-basics-exercise.md) | Log and partitions, ordering, producers, consumer groups, offsets and commit semantics, delivery guarantees, rebalancing, ISR, retention, DLQ. |
| [kafka-basics.md](kafka/kafka-basics.md) | The self-contained Kafka answer sheet (§1–§12) — every term defined in place, sized for speaking. |

Study notes are grouped by subject, one folder each. The Core Java lab and its
index live alongside the Core Java notes:

```
core-java/
├── core-java.md
├── java-8.md
├── java-versions.md
├── threads-jvm.md
└── lab/
    ├── README.md
    ├── 01-introduction/
    ├── 02-strings/
    ├── 03-big-numbers/
    ├── 04-data-structures/
    ├── 05-oop/
    ├── 06-exceptions/
    ├── 07-threads/
    ├── 08-advanced/
    └── Scratchpad.java

kafka/
├── kafka-basics-exercise.md
└── kafka-basics.md

spring-boot/
├── spring-boot-basics.md
├── spring-security-basics.md
├── spring-data-jpa-performance.md
└── spring-boot-basics.ESTIMATE.md
```

## The labs

Every exercise is a single file you launch directly; no Maven or Gradle
build is required. The Java challenge track starts compile-safe but
unsolved: implement the marked seam or seams, then let its embedded
checks judge the result.

```bash
cd core-java/lab
java 01-introduction/WelcomeToJava.java
```

The concurrency stations are demonstrations: predict the output first,
then run them and explain why you were right or wrong.

```bash
cd core-java/lab
java 07-threads/01-start-vs-run/StartVsRun.java
```

Two stations hang on purpose. That hang is the lesson — kill them
with `Ctrl-C`, or guard with `timeout 6`.

## Readiness

Notes are marked 🟢 drilled aloud · 🟡 racked (exists, not drilled) ·
🔴 not built. **Reading is not loading** — a kit only earns 🟢 after a
blind rep out loud.

**The rep scorecard at the foot of each kit is authoritative**;
[CLAUDE.md](CLAUDE.md) summarises them. `core-java/core-java.md` and
`core-java/threads-jvm.md` are through rep 1 in full; `kafka/kafka-basics.md` is
half-way; the runnable labs, `kafka/kafka-basics-exercise.md` and
the Spring Boot/Security/JPA kits are racked and unrepped/unsolved.
