# JAVA THEATER 🐧 — the Java/Spring/Kafka war

**Status: STOCKED — shell 2026-07-20, repo stood up 2026-07-22.
Five kits racked, partially drilled.**

HQ: `~/Documents/interview-hq` (penguin protocol lives there).
Base 6 on the MAP. **Callsign: HOT COFFEE** (Skipper-confirmed
2026-07-20).

**Campaign state lives in HQ, never here** (Skipper's rule
2026-07-22). This repo holds Java knowledge and drill state only —
which kits exist, what they cover, how far each is repped. Which
interview is live, what a given panel asked, what's due when: all of
that belongs to `interview-hq/campaigns/`. Kits must read as useful
for *any* Java interview, not one of them.

## Still to land

- ⬜ `interview-hq/campaigns/track-a/lld-drills.md` — the 9-problem
  machine-coding curriculum. Graduates here on its HQ trigger.

⚠️ **Open debt — deduplication (Skipper: "we will deduplicate
later").** `core-java.md` §7 (Spring/JDBC/REST) and §8 (Kafka)
overlap `spring-boot-basics.md` and `kafka-basics.md`.
Deliberately left as-is. Resolve in one pass: rapid-fire answers
stay in `core-java.md`, depth lives in the basics kits, cross-link
rather than repeat.

## Ground & ammunition

- Drill ground: `~/Programs/design-patterns` (MAP annex a1 — Gradle
  Java repo + diagrams). LLD reps happen THERE; notes live HERE.
- The Java flagship story: the IMPS switch (Spring Boot 2.7 receiver,
  Kafka topics, 7 plain-Java consumers, reversion saga) — recon:
  `interview-hq/audits/project-recon/imps-switch-recon.md`.
- Book: *Head First Design Patterns* (owned). [OPT] *Java
  Concurrency in Practice* per ARMORY.

## Drill kits — the magazine

Anti-fumble exercise docs in the `go-basics.md` manner (🔮 predict ·
🛠 build · 🐛 fix · 💭 explain · solutions under `<details>` ·
Pareto core path + Extensions list). Readiness: 🟢 drilled aloud ·
🟡 racked (exists, not drilled) · 🔴 not built.

| Kit | Covers | Core path | Status |
|---|---|---|---|
| `core-java.md` | OOP · collections · exceptions · Java 8+ · traps · Spring/JDBC/REST · Kafka · SDLC/testing — **rapid-fire, answers sized for speaking**. §5 is a pointer to `threads-jvm.md`, one-liner only | §4→§2→§3→§1 | 🟡 **6/9 sections at rep 1** — §7, §8 unrepped (scorecard at the doc's foot is authoritative) |
| `java-versions.md` | Java 7→25 timeline, headline feature + snippet each · LTS line 8/11/17/21/25 · **Tier-1 "Java 8 from zero" study block** (§A–§I) | timeline→§A–§I | 🟡 racked |
| `threads-jvm.md` | thread · **the two problems** (race/atomicity, visibility/happens-before) · critical section · monitor + `synchronized` · wait/notify + producer-consumer · thread states · **ReentrantLock/Condition** · volatile/atomics · liveness · executors + virtual threads · concurrent collections · JVM/GC — ordered so no answer depends on one below it | §1→§11 in order | 🟡 racked 2026-07-22, **0/11 blocks repped** |
| `s5-threads-lab.md` + `lab/threads/` | 8 runnable predict→run→explain stations: start/run · lost update · volatile · wait/sleep · deadlock · pools · producer-consumer · virtual threads | S1→S8 in order | 🟡 racked 2026-07-21, **0/8 stations repped** |
| `spring-boot-basics.md` | DI/IoC · beans/scopes · config · **auto-config** · web MVC · validation · data/JPA · **transactions & proxies** · testing · actuator | §1→§4→§3→§5→§7→§8 | 🟡 racked 2026-07-21 |
| `kafka-basics.md` | log/partitions · **ordering** · producers · consumer groups · **offsets & commit semantics** · **delivery guarantees** · rebalancing · durability/ISR · retention/compaction · DLQ · Spring Kafka | §1→§3→§4→§5→§2→§6 | 🟡 racked 2026-07-21 |

The threads lab is the only kit with **runnable code** — Java 21
single-file launcher, no build: `cd lab/threads && java
StartVsRun.java`. S3 and S5 hang on purpose; guard with `timeout 6`.
All 8 verified compiling on Temurin 21.0.11 (2026-07-22).

The two `*-basics` kits are standalone (no IMPS anchors — Skipper's
call: IMPS's Spring is thin). Kafka *could* take optional 🔗 IMPS
hooks later (§4.3 commit timing, §9 DLQ ↔ reversion saga). Extensions
lists inside each doc are the spin-off queue (e.g.
`spring-security-basics.md`, `kafka-streams-basics.md`).

Kits may be live ammunition for a campaign running out of HQ — check
`interview-hq/campaigns/` before restructuring anything, since a
board over there may be pointing at these paths.

## Scope decision pending (HQ TODO: "Java wall")

Thin Java/Spring/Kafka magazine vs steer-to-Go — decided by what
real Track A JDs say (JD recon). This theater exists so the decision
has a home when it lands.

## Layout

Notes flat by design (Skipper's call 2026-07-22) — every kit sits at
the repo root, no `notes/` folder. Runnable code is quarantined under
`lab/`: `lab/threads/` holds the eight stations, `lab/Scratchpad.java`
is the try-things-out file (Skipper's, 2026-07-22).

## Working practice

**Mermaid diagrams: write them, don't render them.** Emit the
mermaid block and ask Skipper to eyeball it. Do NOT shell out to
`mmdc`/npx to render an SVG and then read the SVG back — it burns
tokens for nothing. Visual verification is Skipper's job; syntax
is cheap to fix on report.

**No edit narration in the kits.** A kit teaches Java — it never
talks about its own revision history. Banned: "provenance" /
"what changed" / move-log appendices, and sentences that describe
the document instead of the topic ("this is *now* defined",
"reordered so that…", "same answers as before", "every row is
vocabulary you *already have*"). The tell is a word doing
document-deixis — *now*, *later*, *still*, *no longer*, "the
section below" — where the topic word belongs. Cross-references
are fine as plain section links (`[§4](#4-…)`), not as prose about
where the reader is in the file. Changelogs go in the commit
message or the chat reply; the drill sheet stays clean.

## Laws (inherited)

Readiness is earned · truth only (never claim beyond the recon
docs — e.g. no unit-test-coverage claims on IMPS) · one gate at a
time (a live campaign in HQ preempts theater work).
