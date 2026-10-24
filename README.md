# Learning Java

Interview preparation notes for Java, Spring Boot, Kafka and Redis —
rapid-fire answers, study material, and hands-on labs.

Callsign **HOT COFFEE**. Operating
charter lives in [CLAUDE.md](CLAUDE.md); HQ is
`~/Documents/interview-hq`.

This repository owns Java language and framework preparation. LLD lives in
the language-neutral ground at `~/Programs/learning-lld`; its Java lane is
split into:

- `java/patterns/` — hand-written design-pattern examples, including study from
  *Head First Design Patterns*;
- `java/machine-coding/` — timed implementations of the canonical exercise
  contracts in `exercises/`.

A machine-coding problem uses a named pattern only when the design pressure
earns it.

## What's in here

Start with the foundations for each subject, then use the deeper guides,
exercises, and labs to practise.

### Core Java

| Resource | Use it for |
|---|---|
| [Core Java foundations](core-java/core-java.md) | Review language fundamentals and practise interview answers. |
| [Java 8 essentials](core-java/java-8.md) | Learn lambdas, streams, functional interfaces, and common APIs. |
| [Java version guide](core-java/java-versions.md) | Compare Java 7–25 releases and understand feature changes. |
| [Comparator patterns](core-java/comparator-patterns.md) | Build sorting rules with multiple keys, nulls, and business ranks. |
| [Static factory methods](core-java/static-factory-methods.md) | Choose factories and understand views, snapshots, and caching. |
| [Threads and JVM](core-java/threads-jvm.md) | Study concurrency, the memory model, executors, and JVM internals. |
| [Interview practice path](core-java/lab/INTERVIEW-PATH.md) | Follow focused drills through to an integrated transaction report. |
| [Core Java labs](core-java/lab/README.md) | Find self-testing challenges and track implementation progress. |
| [Threads labs](core-java/lab/07-threads/README.md) | Predict, run, and explain eight concurrency demonstrations. |

### Spring Boot

**Foundations and reference**

| Resource | Use it for |
|---|---|
| [Spring Boot foundations](spring-boot/spring-boot-basics.md) | Learn beans, auto-configuration, web APIs, persistence, and testing. |
| [Senior Spring reference](spring-boot/spring-senior-core.md) | Prepare broader senior-level answers and work through production scenarios. |

**Deeper guides**

| Resource | Use it for |
|---|---|
| [Container internals](spring-boot/spring-container-internals.md) | Understand bean lifecycle, configuration, post-processors, and proxies. |
| [Security](spring-boot/spring-security-basics.md) | Work through authentication, authorization, JWT, and web security. |
| [JPA performance](spring-boot/spring-data-jpa-performance.md) | Diagnose fetching, query, batching, locking, and caching problems. |
| [Transactions](spring-boot/spring-boot-transactions-deep.md) | Reason about propagation, rollback, isolation, and cross-system consistency. |
| [Resilience](spring-boot/spring-boot-resilience.md) | Design timeouts, retries, circuit breakers, and graceful degradation. |
| [Observability](spring-boot/spring-boot-observability.md) | Diagnose services using logs, metrics, tracing, and health signals. |
| [Production testing](spring-boot/spring-boot-testing-deep.md) | Choose test scopes and verify database, security, and concurrency behavior. |

**Project scaffolds and coverage**

| Resource | Use it for |
|---|---|
| [Simple project scaffold](spring-boot/scaffold/spring-simple-project-scaffold.md) | Structure a todo application with controllers, services, and JPA repositories. |
| [Domain-oriented project scaffold](spring-boot/scaffold/spring-domain-oriented-project-scaffold.md) | Structure an order feature around a plain-Java domain and explicit adapters. |
| [Senior-core checklist](spring-boot/senior-core-checklist.md) | Check the completed build scope of the senior Spring material. |

### Kafka

| Resource | Use it for |
|---|---|
| [Kafka fundamentals](kafka/kafka-basics.md) | Rehearse spoken answers on partitions, consumers, delivery guarantees, and operations. |
| [Kafka exercises](kafka/kafka-basics-exercise.md) | Test understanding with predict, build, fix, and explain drills. |

### Redis

| Resource | Use it for |
|---|---|
| [Redis fundamentals](redis/redis-basics.md) | Rehearse answers on data types, atomicity, caching, persistence, and scaling. |
| [Redis exercises](redis/redis-basics-exercise.md) | Test understanding with drills and hidden solutions. |
| [Redis labs](redis/lab/README.md) | Run 15 self-testing Java labs against a real Redis server. |

## The in-repository labs

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
java 07-threads/StartVsRun.java
```

Two stations hang on purpose. That hang is the lesson — kill them
with `Ctrl-C`, or guard with `timeout 6`.

The Redis labs run against a real server rather than a simulation, so
they need one started first, plus the Jedis jars, which are downloaded
rather than committed:

```bash
cd redis/lab
podman compose up -d
./get-deps.sh
java -cp "lib/*" 01-data-types/OtpCache.java
```

Two of them need a larger topology — a Sentinel quorum and a six-node
cluster — and those tiers sit behind compose profiles until asked for.
