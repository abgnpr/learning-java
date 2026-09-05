---
name: lab-review
description: Review a core-java lab challenge the user has just attempted — run its checks, critique the solution, annotate it with why-comments in the house style, and update the lab README's counts and revision references. Use after the user says they have solved, attempted, or finished a lab challenge or station, or asks to check/review their lab answer.
---

# Lab review — the post-attempt drill

Picks up at step 4 (**RUN**) of the drill protocol in
`core-java/lab/README.md`, after the user's own BUILD. Covers RUN, EXTEND
and EXPLAIN.

## The one rule that outranks everything

**The user writes the implementation. You never do.**

These are solution-free starters whose entire value is the rep. Writing the
body — or "improving" it while doing something else — takes the rep away and
is the single worst failure mode of this skill.

- Never edit inside the challenge method's body on your own initiative.
- Found something better? **Describe it, show the two versions, and stop.**
  Apply it only after the user says yes, in a separate edit.
- Asked to add comments? Add comments. Touching the code is a different job
  needing separate permission, even when the change looks obviously right.
- If they haven't solved it yet, give the smallest hint that unblocks and let
  them return. Name the rule or the API; don't hand over the line.

Interpret ambiguity narrowly. "Check this" means run and review, not fix.

## Steps

### 1. Run it

```bash
cd core-java/lab
java 01-introduction/LoopsOne.java
```

Path is section-relative from `core-java/lab/`. Concurrency stations (`07-`)
may hang — S3 can, S5 does on purpose — so guard those with `timeout 6`.

Failing checks: report the assertion output as-is and stop. Diagnosing is
fine; rewriting is not.

### 2. Review the solution

Read the whole file and assess:

- **Correctness beyond the checks** — what input class would break it that
  the supplied checks miss? This is the EXTEND step; propose the case.
- **Idiom** — is this how a Java developer would write it? Two `String.format`
  calls glued with `+` where one format string does the job; a loop where a
  stream reads better, or the reverse.
- **Dead code** — guards that the stated contract makes unreachable.
- **Interview readiness** — can they say *why* it works out loud? That is what
  the drill is for.

Report findings as a short list. Distinguish "this is wrong" from "this is a
matter of taste" honestly, and say plainly when the solution is simply good —
manufactured criticism wastes the rep.

### 3. Ask before annotating

Feedback is the deliverable. Annotating is a separate job the user opts into.

End the review with one plain-prose question offering to annotate, and stop
there. No `AskUserQuestion` menu — a single line is the whole ask:

> Want me to annotate this with why-comments?

Fold any code changes you proposed in step 2 into that same line rather than
asking about them separately. Then wait. A reply that names only the file
("this one only", "just Datatypes") is a scope answer, not a yes.

Do not run step 5 either until annotation happens — the README records a
completed drill, and the drill is not complete while the offer is open. If
the user declines annotation outright, ask whether they still want the
counts updated.

### 4. Annotate with why-comments

The house style is set by `01-introduction/StdinStdoutOne.java` and
`StdinStdoutTwo.java`. Read one before writing.

Comments capture **why this and not the obvious alternative** — the trap that
cost time, the API rule that is easy to get backwards. Not what the code does.

- Show the rejected approach as commented-out code when seeing it is the
  lesson (`StdinStdoutOne`'s classic/streams/best progression).
- Name the mechanism precisely: `IntStream.map` is an `IntUnaryOperator`;
  `0` in `%04d` is a flag, not a precision.
- State why an edge case needs no branch when that is the insight — e.g.
  `rangeClosed(1, 0)` is empty and `joining()` returns `""` for an empty
  stream.
- Keep them tight. A dense paragraph above the method beats a comment per
  line.

Then re-run to confirm the file still passes.

### 5. Update the lab README

In `core-java/lab/README.md`:

- Increment the subdomain row and the **Total** row in the progress table.
  Check the current values first — the user often updates them already.
- Add a **Revision references** row for a construct worth revisiting:
  `| 06 — Java Loops I | <construct> | [<title>](<url>) | <YYYY-MM-DD> |`.
  Skip it when the challenge exposed nothing new; the table is for real
  follow-up material, not a completion log.

Official API docs and Baeldung are the established sources here. Do not
invent a URL — if unsure one exists, search for it or leave the row out.

If a kit table in the root `CLAUDE.md` tracks this lab's count, update it too.

## Scope

Stay inside the challenge file and the lab README. Don't refactor the
`check`/`show`/`report` harness, weaken an acceptance check, or restructure
the lab. Don't commit unless asked.

The harness block is standardized across challenges and documented under "The
harness block" in the lab README. A file still carrying the older
`checkEquals(expected, actual, label)` form may be migrated to it while
reviewing that file — copy the block verbatim, rewrite each case as
`check(label, input, expected, actual)`, and end `main` with
`report("Challenge NN")`. Preserve every existing case and its expected value;
migrating is a mechanical rewrite, never a change to what is asserted.
