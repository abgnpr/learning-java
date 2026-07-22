# JAVA THEATER 🐧 — the Java/Spring/Kafka war (SHELL)

**Status: SHELL — created 2026-07-20 on Skipper's order. Files
migrate in later; do not duplicate content meanwhile.**

HQ: `~/Documents/interview-hq` (penguin protocol lives there).
Base 6 on the MAP. **Callsign: HOT COFFEE** (Skipper-confirmed
2026-07-20).

## What migrates here (parked in HQ TODO, triggers set)

1. `interview-hq/campaigns/cboi/core-java.md` — rapid-fire core-Java
   kit (OOP · collections · exceptions · Java 8+ · threads/JVM ·
   Spring/JDBC/REST · Kafka · SDLC). Moves AFTER CBOI concludes —
   it serves that battle first.
2. `interview-hq/campaigns/cboi/java-versions.md` — same trigger.
3. `interview-hq/campaigns/track-a/lld-drills.md` — the 9-problem
   machine-coding curriculum. Graduates here.

## Ground & ammunition

- Drill ground: `~/Programs/design-patterns` (MAP annex a1 — Gradle
  Java repo + diagrams). LLD reps happen THERE; notes live HERE.
- The Java flagship story: the IMPS switch (Spring Boot 2.7 receiver,
  Kafka topics, 7 plain-Java consumers, reversion saga) — recon:
  `interview-hq/audits/project-recon/imps-switch-recon.md`.
- Book: *Head First Design Patterns* (owned). [OPT] *Java
  Concurrency in Practice* per ARMORY.

## Drill kits (`notes/`) — built in place, not migrated

Anti-fumble exercise docs in the `go-basics.md` manner (🔮 predict ·
🛠 build · 🐛 fix · 💭 explain · solutions under `<details>` ·
Pareto core path + Extensions list). Readiness: 🟢 drilled aloud ·
🟡 racked (exists, not drilled) · 🔴 not built.

| Kit | Covers | Core path | Status |
|---|---|---|---|
| `notes/spring-boot-basics.md` | DI/IoC · beans/scopes · config · **auto-config** · web MVC · validation · data/JPA · **transactions & proxies** · testing · actuator | §1→§4→§3→§5→§7→§8 | 🟡 racked 2026-07-21 |
| `notes/kafka-basics.md` | log/partitions · **ordering** · producers · consumer groups · **offsets & commit semantics** · **delivery guarantees** · rebalancing · durability/ISR · retention/compaction · DLQ · Spring Kafka | §1→§3→§4→§5→§2→§6 | 🟡 racked 2026-07-21 |

Both standalone (no IMPS anchors — Skipper's call: IMPS's Spring is
thin). Kafka *could* take optional 🔗 IMPS hooks later (§4.3 commit
timing, §9 DLQ ↔ reversion saga). Extensions lists inside each doc
are the spin-off queue (e.g. `spring-security-basics.md`,
`kafka-streams-basics.md`).

## Scope decision pending (HQ TODO: "Java wall")

Thin Java/Spring/Kafka magazine vs steer-to-Go — decided by what
real Track A JDs say (JD recon). This shell exists so the decision
has a home when it lands.

## Laws (inherited)

Readiness is earned · truth only (never claim beyond the recon
docs — e.g. no unit-test-coverage claims on IMPS) · one gate at a
time (CBOI preempts).
