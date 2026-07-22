# JAVA THEATER 🐧 — the Java/Spring/Kafka war

**Status: STOCKED — shell 2026-07-20, repo + core-Java cargo landed
2026-07-22 (Skipper's early-move order, ahead of the post-CBOI
trigger). Five kits racked, zero drilled.**

HQ: `~/Documents/interview-hq` (penguin protocol lives there).
Base 6 on the MAP. **Callsign: HOT COFFEE** (Skipper-confirmed
2026-07-20).

## Migration status

**Landed 2026-07-22** — moved out of `interview-hq/campaigns/cboi/`
while CBOI is still live; the campaign board now points here for
every core-Java task (OBJ-5). Trigger was "after CBOI concludes";
Skipper overrode it.

1. ✅ `core-java.md` — rapid-fire core-Java kit (OOP ·
   collections · exceptions · Java 8+ · threads/JVM · Spring/JDBC/
   REST · Kafka · SDLC).
2. ✅ `java-versions.md` — Java 7→25 timeline + the Tier-1
   "Java 8 from zero" study block.
3. ✅ `s5-threads-lab.md` + `lab-threads/` — 8 runnable
   predict-then-run stations (built 2026-07-21, not in the original
   manifest).
4. ⬜ `interview-hq/campaigns/track-a/lld-drills.md` — the 9-problem
   machine-coding curriculum. Still to graduate here.

⚠️ **Open debt — deduplication (Skipper: "we will deduplicate
later").** `core-java.md` §7 (Spring/JDBC/REST) and §8 (Kafka) now
overlap `spring-boot-basics.md` and `kafka-basics.md`.
Deliberately left as-is on arrival. Resolve in one pass: rapid-fire
answers stay in `core-java.md`, depth lives in the basics kits,
cross-link rather than repeat.

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
| `core-java.md` | OOP · collections · exceptions · Java 8+ · threads/JVM · traps · Spring/JDBC/REST · Kafka · SDLC/testing — **rapid-fire, answers sized for speaking** | §5→§4→§2→§3→§1 | 🟡 racked (CBOI-built) |
| `java-versions.md` | Java 7→25 timeline, headline feature + snippet each · LTS line 8/11/17/21/25 · **Tier-1 "Java 8 from zero" study block** (§A–§I) | timeline→§A–§I | 🟡 racked (CBOI-built) |
| `s5-threads-lab.md` + `lab-threads/` | 8 runnable predict→run→explain stations: start/run · lost update · volatile · wait/sleep · deadlock · pools · producer-consumer · virtual threads | S1→S8 in order | 🟡 racked 2026-07-21, **0/8 stations repped** |
| `spring-boot-basics.md` | DI/IoC · beans/scopes · config · **auto-config** · web MVC · validation · data/JPA · **transactions & proxies** · testing · actuator | §1→§4→§3→§5→§7→§8 | 🟡 racked 2026-07-21 |
| `kafka-basics.md` | log/partitions · **ordering** · producers · consumer groups · **offsets & commit semantics** · **delivery guarantees** · rebalancing · durability/ISR · retention/compaction · DLQ · Spring Kafka | §1→§3→§4→§5→§2→§6 | 🟡 racked 2026-07-21 |

The threads lab is the only kit with **runnable code** — Java 21
single-file launcher, no build: `cd lab-threads && java
StartVsRun.java`. S3 and S5 hang on purpose; guard with `timeout 6`.
All 8 verified compiling on Temurin 21.0.11 (2026-07-22).

The two `*-basics` kits are standalone (no IMPS anchors — Skipper's
call: IMPS's Spring is thin). Kafka *could* take optional 🔗 IMPS
hooks later (§4.3 commit timing, §9 DLQ ↔ reversion saga). Extensions
lists inside each doc are the spin-off queue (e.g.
`spring-security-basics.md`, `kafka-streams-basics.md`).

**Still serving CBOI:** `core-java.md`, `java-versions.md` and the
threads lab are live ammunition for the pending CBOI interview
(OBJ-5). Its task board — `interview-hq/campaigns/cboi/TODO.md` —
points here. Do not restructure them while that battle is open.

## Scope decision pending (HQ TODO: "Java wall")

Thin Java/Spring/Kafka magazine vs steer-to-Go — decided by what
real Track A JDs say (JD recon). This theater exists so the decision
has a home when it lands.

## Layout

Flat by design (Skipper's call 2026-07-22) — every kit sits at the
repo root, no `notes/` folder. `lab-threads/` is the one directory,
because the threads lab ships runnable code.

## Laws (inherited)

Readiness is earned · truth only (never claim beyond the recon
docs — e.g. no unit-test-coverage claims on IMPS) · one gate at a
time (CBOI preempts).
