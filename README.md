# learning-java

Interview preparation notes for Java, Spring Boot and Kafka —
rapid-fire answers, study material, and one hands-on lab.

Callsign **HOT COFFEE**, base 6 of the interview fleet. Operating
charter lives in [CLAUDE.md](CLAUDE.md); HQ is
`~/Documents/interview-hq`.

## What's in here

| File | What it is |
|---|---|
| [core-java.md](core-java.md) | Rapid-fire Q&A across OOP, collections, exceptions, Java 8+, threads/JVM, traps, Spring/JDBC/REST, Kafka and SDLC/testing. Answers sized for **speaking**, not writing. |
| [java-versions.md](java-versions.md) | Java 7 → 25 timeline: version, year, headline feature, one snippet. Plus a Tier-1 "Java 8 from zero" study block (§A–§I). |
| [s5-threads-lab.md](s5-threads-lab.md) | Eight runnable stations for the concurrency section — races, visibility, deadlock, pools, virtual threads. Code in [lab-threads/](lab-threads/). |
| [spring-boot-basics.md](spring-boot-basics.md) | DI/IoC, beans and scopes, auto-config, web MVC, validation, data/JPA, transactions and proxies, testing, actuator. |
| [kafka-basics.md](kafka-basics.md) | Log and partitions, ordering, producers, consumer groups, offsets and commit semantics, delivery guarantees, rebalancing, ISR, retention, DLQ. |

Everything sits flat at the repo root. `lab-threads/` is the one
directory, because the threads lab ships actual code.

## The lab

The only kit you run rather than read. Each station is a single file
you launch directly — predict the output first, then run it, then
read why you were right or wrong:

```bash
cd lab-threads
java StartVsRun.java
```

Two stations hang on purpose. That hang is the lesson — kill them
with `Ctrl-C`, or guard with `timeout 6`.

## Readiness

Notes are marked 🟢 drilled aloud · 🟡 racked (exists, not drilled) ·
🔴 not built. **Reading is not loading** — a kit only earns 🟢 after a
blind rep out loud. Current state is tracked in
[CLAUDE.md](CLAUDE.md); today everything is 🟡.
