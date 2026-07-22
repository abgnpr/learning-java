# Java version Timeline 🐧

**Why this exists:** "which Java version?" and "what's new in Java 8?"
are panel certainties. This is the chronological spine — say the
version, the headline feature, one line of code — plus the Tier-1
study block (Java 8 from zero). The spoken rapid-fire layer is
[core-java.md §4](core-java.md#4-java-8-paper-had-this--know-it-cold);
it links back into the sections here. LTS line = **8, 11, 17, 21,
25** — memorize those five.

**How to drill:** aloud, blind. For each version say *number → year →
one headline feature*. If pressed, drop the snippet. Don't recite all
features — the panel wants the one that matters.

---

## Java 7 (2011) — "Project Coin" ergonomics

Small syntax sugar + one big resource fix.

- **try-with-resources** — auto-closes `AutoCloseable`, reverse order.
- **Multi-catch** — one `catch` for several exception types.
- **Diamond operator** — `<>` infers generic type on the right.
- **Strings in `switch`**.
- Underscores in numeric literals (`1_000_000`); binary literals (`0b1010`).

```java
try (InputStream in = Files.newInputStream(path)) {   // auto-closed
    ...
} catch (IOException | SQLException e) {        // multi-catch
    ...
}
Map<String, List<Integer>> m = new HashMap<>(); // diamond
```

---

## Java 8 (2014) — THE one. Functional Java. LTS ⭐

Biggest release in the language's history. Panel opens here.

- **Lambdas** — inline functional-interface impl.
- **Stream API** — declarative collection pipelines.
- **Functional interfaces** — `Predicate` / `Function` / `Supplier` / `Consumer`.
- **`default` / `static` methods in interfaces** — evolve without breaking.
- **`Optional`** — explicit "maybe absent", kills some NPEs.
- **New `java.time`** — immutable, thread-safe date/time.
- **Method references** (`::`).
- **PermGen → Metaspace** (class metadata moved to native memory).

```java
List<String> names = people.stream()
    .filter(p -> p.age() > 18)          // lambda
    .map(Person::name)                  // method reference
    .collect(Collectors.toList());      // stream terminal

Optional.ofNullable(user).map(User::email).orElse("none");
LocalDate.now().plusDays(30);           // java.time
```

---

## Java 9 (2017) — modularity

- **JPMS (Project Jigsaw / module system)** — `module-info.java`,
  strong encapsulation of packages.
- **JShell** — the REPL.
- Collection factory methods — `List.of(...)`, `Map.of(...)` (immutable).
- Private interface methods; Stream `takeWhile`/`dropWhile`.
- **G1 becomes the default garbage collector.**

```java
List<Integer> nums = List.of(1, 2, 3);   // immutable
module com.bank.imps { requires spring.core; }  // module-info.java
```

---

## Java 10 (2018) — `var`

- **Local-variable type inference** — `var` for locals only (not
  fields, params, or return types). Type is still static.

```java
var list = new ArrayList<String>();   // inferred ArrayList<String>
for (var e : list) { ... }
```

---

## Java 11 (2018) — LTS ⭐

The LTS most shops jumped to after 8. Cleanup + convenience.

- **`var` allowed in lambda parameters**.
- New `String` methods — `isBlank`, `strip`, `lines`, `repeat`.
- **`HttpClient`** standardized (`java.net.http`) — async, HTTP/2.
- Run a single-file source directly: `java Hello.java` (no compile step).
- Removed Java EE / CORBA modules.

```java
"  hi  ".strip();          // "hi"
"ab".repeat(3);            // "ababab"
HttpClient.newHttpClient().send(request, ofString());
```

---

## Java 12–13 (2019) — previews warming up

- **Switch expressions** (preview 12, standard in 14) — arrow form,
  returns a value, no fall-through.
- **Text blocks** (preview 13, standard in 15) — multi-line strings.

```java
int days = switch (month) {
    case JAN, MAR, MAY -> 31;
    case FEB -> 28;
    default -> 30;
};
```

---

## Java 14 (2020) — switch expressions standard

- **Switch expressions** finalized (see above).
- **Helpful NullPointerExceptions** — the message names *which*
  variable was null.
- **`record`** (preview) — see Java 16.

---

## Java 15 (2020) — text blocks standard

- **Text blocks** finalized — `"""` multi-line, no escaping.
- Sealed classes (preview).

```java
String json = """
    { "id": 1,
      "name": "IMPS" }
    """;
```

---

## Java 16 (2021) — records

- **`record`** finalized — immutable data carrier; auto
  constructor, `equals`, `hashCode`, `toString`, accessors.
- **Pattern matching for `instanceof`** — bind in the check.

```java
record Point(int x, int y) {}          // that's the whole class
Point p = new Point(1, 2);
p.x();                                  // accessor, not getX()

if (obj instanceof String s) {          // s bound automatically
    System.out.println(s.length());
}
```

---

## Java 17 (2021) — LTS ⭐

The current "modern default" for many teams.

- **Sealed classes** finalized — restrict which classes may extend/implement.
- Records + pattern-matching-`instanceof` now standard.
- Strong encapsulation of JDK internals (`sun.misc` locked down).

```java
sealed interface Shape permits Circle, Square {}
final class Circle implements Shape { ... }
```

---

## Java 21 (2023) — LTS ⭐ virtual threads

Huge for backend/concurrency answers.

- **Virtual threads (Project Loom)** — millions of cheap threads;
  blocking code scales without async spaghetti.
- **Pattern matching for `switch`** finalized — match on type.
- **Record patterns** — destructure records in patterns.
- Sequenced collections (`getFirst`/`getLast`).

```java
Thread.startVirtualThread(() -> handleRequest());   // cheap thread

String msg = switch (shape) {                        // type patterns
    case Circle c -> "circle r=" + c.radius();
    case Square s -> "square";
};
```

---

## Java 25 (2025) — LTS ⭐ (latest LTS)

- Continues Loom/pattern-matching maturation; structured concurrency
  and scoped values progressing toward standard.
- Safe one-liner: *"25 is the newest LTS; the big backend shift was
  21's virtual threads."*

---

## The two answers to have cold

**"Which Java version did IMPS run?"** — ⚠️ NOT pinned in Gradle;
confirm the deployment JDK before the day (see [core-java.md](core-java.md)
⚓ header). Spring Boot 2.7.10 → runs on 8/11/17.

**"What's new since Java 8?"** — modules (9), `var` (10), `HttpClient`
(11), switch expressions (14), text blocks (15), records (16), sealed
(17), **virtual threads (21)**. Lead with records and virtual threads
— those are the ones interviewers probe.

**Skipper's translation:** one file, Java 7 → 25, headline feature +
tiny snippet each. Five LTS versions to memorize: 8, 11, 17, 21, 25.
The two power features to name-drop: **records** (16) and **virtual
threads** (21).

---
---

## 📚 Tier-1 study block — Java 8 from zero

*Read this to LEARN; the cue-cards above are for drilling once it's
loaded. This assumes you've never used these features. Same bullet
rhythm as [core-java.md](core-java.md) — bold question, em-dash
answer, code with aligned `// comments`. Deep companion to
[core-java.md](core-java.md) §4.*

### The one idea underneath everything

- **What actually changed in Java 8?** — before 8 you could only pass
  *objects* around and call methods on them; you could not pass
  *behavior*. Java 8 made a function a thing you can store in a
  variable, hand to a method, and return. **Lambdas, method
  references, and streams are all built on that single shift.** Learn
  them in that order.
- **Hook** — before 8 you handed people a *toolbox* (an object) and
  hoped they picked the right tool; from 8 you hand over the *move
  itself* (a function). Every section below is that one trick in a
  different costume.

### §A Functional interface — the "slot" a lambda fills

- **What is it?** — an interface with **exactly one abstract method**
  (SAM = Single Abstract Method). That lone method is the slot; a
  lambda is what you drop into it. `@FunctionalInterface` is an
  optional annotation that makes the compiler enforce "exactly one".
- **Picture it** — the interface is a wall socket with one fixed
  shape; a lambda is any plug that fits. `Runnable` is a no-pin
  socket; `Comparator<String>` is a two-pin socket that returns an
  int.
- **You already knew some** — `Runnable` (`run`), `Comparator`
  (`compare`), `Callable` (`call`) are SAM interfaces. Java 8 just
  let a lambda stand in for them.

  ```java
  @FunctionalInterface
  interface Validator {                 // one abstract method
      boolean check(String s);
  }

  Validator notBlank = s -> !s.isBlank();  // lambda fills the slot
  notBlank.check("hi");                     // true
  ```

- **The core four** (in `java.util.function`, memorize these) —

  |Interface|Method|Shape|Read it as|
  |---|---|---|---|
  |`Predicate<T>`|`test`|T → boolean|a yes/no question|
  |`Function<T,R>`|`apply`|T → R|a transform|
  |`Supplier<T>`|`get`|() → T|a factory, takes nothing|
  |`Consumer<T>`|`accept`|T → void|a sink, returns nothing|

  Chant it: **Predicate asks · Function morphs · Supplier gives ·
  Consumer takes.**

  ```java
  Predicate<Integer>       isEven = n -> n % 2 == 0;   // T -> boolean
  Function<String,Integer> len    = s -> s.length();   // T -> R
  Supplier<LocalDate>      today  = LocalDate::now;     // () -> T
  Consumer<String>         print  = System.out::println;// T -> void

  isEven.test(4);        // true
  len.apply("hello");    // 5
  today.get();           // 2026-07-19
  print.accept("hi");    // prints hi
  ```

  *(Bonus names if pressed: `BiFunction<T,U,R>` takes two args;
  `UnaryOperator<T>` = `Function<T,T>`; `BinaryOperator<T>`.)*

### §B Lambda — the compact syntax

- **What is it?** — a short way to write an instance of a functional
  interface: just the parameters and the body, no class ceremony.

  ```java
  // BEFORE — anonymous class, all boilerplate
  names.sort(new Comparator<String>() {
      public int compare(String a, String b) {
          return a.length() - b.length();
      }
  });

  // AFTER — lambda: same thing, only the essence
  names.sort((a, b) -> a.length() - b.length());
  ```

- **Syntax forms** —

  ```java
  ()      -> 42                 // no params
  x       -> x * 2              // one param, parens optional
  (x, y)  -> x + y              // several params
  (x, y)  -> { int s = x + y;   // block body needs braces
               return s; }      //   ...and an explicit return
  ```

- **How does it know the types?** — from the *target*. The compiler
  looks at which functional interface the lambda is handed to, and
  reads parameter/return types off its single abstract method:
  `(a, b) -> a.length() - b.length()` given to a
  `Comparator<String>` slot → `a`, `b` must be Strings. That's
  "target typing" — no declarations needed.
- **Variable capture (the one gotcha)** — a lambda may use a local
  variable from around it only if that variable is **effectively
  final** (assigned exactly once, never reassigned). Reassign it and
  the code won't compile. Instance fields are fine — only *locals*
  have this rule. Hook: the lambda takes a *photograph* of the
  variable, not a live feed — and Java refuses to photograph
  anything that moves.

### §C Method reference — shorthand for a one-call lambda

- **What is it?** — when a lambda does nothing but call one existing
  method, replace the whole lambda with `Type::method`. Rule of
  thumb: if you'd describe the lambda as "it just calls X", stop
  miming the song — hand over the sheet music and write `::X`.

  |Kind|Written as|Equivalent lambda|
  |---|---|---|
  |static method|`Integer::parseInt`|`s -> Integer.parseInt(s)`|
  |method of a *specific* object|`System.out::println`|`x -> System.out.println(x)`|
  |method of *any* object of a type|`String::toLowerCase`|`s -> s.toLowerCase()`|
  |constructor|`ArrayList::new`|`() -> new ArrayList<>()`|

- **The confusing row is the third** — an "any object" reference can
  even absorb TWO lambda params. Rule: **the first argument becomes
  the receiver**, the rest become arguments —
  `(a, b) -> a.compareTo(b)` ≡ `String::compareTo`. Puzzled by a
  `::`? Expand it back into the lambda.

### §D Stream API — the payoff

- **What is a stream?** — a **pipeline that carries elements from a
  source through a chain of operations.** It is NOT a data structure;
  it stores nothing, it flows. You describe *what* you want done; the
  stream does it when you finally ask for a result.
- **Picture it** — a conveyor belt. The source loads items onto the
  belt, every intermediate op is a station bolted along it (standing
  idle), and the terminal is the big green ON button. Button not
  pressed → belt never moves. That is the whole API in one image.
- **Three parts, always in this order** —
  1. **source** — a collection, array, or `Stream.of(...)`
  2. **intermediate ops** — return another stream, and are **lazy**
     (they stack up but don't run yet): `filter`, `map`, `sorted`,
     `distinct`, `limit`, `skip`
  3. **terminal op** — produces a result or a side effect and **fires
     the whole chain**: `collect`, `forEach`, `count`, `reduce`,
     `anyMatch`, `findFirst` *(exhaustive menu at the end of this
     section)*

  ```java
  // BEFORE — external iteration: you drive the loop
  List<String> out = new ArrayList<>();
  for (Person p : people) {
      if (p.age() >= 18) {
          out.add(p.name().toUpperCase());
      }
  }

  // AFTER — internal iteration: you declare the pipeline
  List<String> out = people.stream()
      .filter(p -> p.age() >= 18)         // intermediate (lazy)
      .map(p -> p.name().toUpperCase())   // intermediate (lazy)
      .collect(Collectors.toList());      // terminal — runs it all
  ```

- **Three laws to say out loud** —
  - **lazy** — nothing happens until the terminal; no terminal, no
    work (no ON press, no belt movement)
  - **single-use** — one run per belt; a consumed stream throws if
    touched again — build a fresh one from the source
  - **non-mutating** — the shelf you loaded from keeps every item;
    the source collection is never changed
- **map vs flatMap** — `map` transforms one element into one
  (1→1); `flatMap` transforms one element into *many* and flattens
  the results into a single stream (1→many). Image: `map` swaps
  what's inside each box, box count unchanged; `flatMap` rips the
  cartons open and spills their contents onto the belt.

  ```java
  List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));

  nested.stream()
      .flatMap(List::stream)              // flatten the inner lists
      .collect(Collectors.toList());      // [1, 2, 3, 4]
  ```

- **The full menu** — exhaustive for panel purposes. ⭐ = say these
  first; the rest are recognize-and-nod.

  **Sources (load the belt):**

  |Source|Gives|
  |---|---|
  |`list.stream()`|stream over any collection|
  |`Arrays.stream(arr)`|stream over an array|
  |`Stream.of(a, b, c)`|stream of literals|
  |`IntStream.range(0, n)` / `rangeClosed(1, n)`|0…n−1 / 1…n|
  |`Stream.iterate(seed, f)` / `Stream.generate(s)`|infinite — cap with `limit`|
  |`Files.lines(path)`|file lines, lazily read|

  **Intermediates (lazy — return a new Stream, belt keeps building):**

  |Op|Takes|Does|Notes|
  |---|---|---|---|
  |⭐ `filter`|`Predicate`|keeps passing elements|stateless|
  |⭐ `map`|`Function`|1→1 transform|stateless|
  |⭐ `flatMap`|`Function` → Stream|1→many, flattened|stateless|
  |`mapToInt` / `boxed`|—|object ↔ primitive stream|unlocks `sum()`/`average()`|
  |⭐ `distinct`|—|drops duplicates (by `equals`)|stateful|
  |⭐ `sorted`|optional `Comparator`|sorts|stateful — buffers everything|
  |`peek`|`Consumer`|look without touching|debugging only|
  |⭐ `limit`|`long n`|first n only|short-circuits — tames infinite|
  |`skip`|`long n`|drops first n|stateful|
  |`takeWhile` / `dropWhile`|`Predicate`|take/drop until first fail|Java 9|
  |`parallel` / `sequential`|—|switch execution mode|care — [core-java.md §4](core-java.md#4-java-8-paper-had-this--know-it-cold) parallelStream|

  **Terminals (fire the belt — return anything BUT a Stream):**

  |Op|Returns|Does|Notes|
  |---|---|---|---|
  |⭐ `collect`|List / Set / Map / …|gathers via `Collectors`|the workhorse — §E|
  |`toList()`|`List<T>`|shortcut for the above|Java 16; unmodifiable|
  |⭐ `forEach`|`void`|side effect per element|unordered when parallel|
  |⭐ `count`|`long`|how many survive|—|
  |⭐ `reduce`|`T` / `Optional<T>`|fold to one value|identity + accumulator|
  |`min` / `max`|`Optional<T>`|extreme by `Comparator`|empty stream → empty box|
  |⭐ `anyMatch`|`boolean`|does one pass?|short-circuits|
  |`allMatch` / `noneMatch`|`boolean`|do all / none pass?|short-circuit too|
  |⭐ `findFirst`|`Optional<T>`|first element|short-circuits|
  |`findAny`|`Optional<T>`|whichever is cheapest|meant for parallel|
  |`toArray`|`T[]`|array out|`toArray(String[]::new)`|
  |`sum` / `average` / `summaryStatistics`|number / stats|math in one pass|primitive streams only|

  **Terminal-spotting trick (MCQ gold)** — check the return type:
  returns a `Stream` → intermediate, belt still building; returns
  anything else (`List`, `long`, `boolean`, `Optional`, `void`) →
  terminal, belt fires. **Short-circuiters** (`limit`, `anyMatch`,
  `findFirst`) can stop early — the only way an infinite stream
  ever ends.

### §E Collectors — what `collect` uses

- **The workhorse terminal** — `collect(...)` gathers the stream into
  a result. `Collectors` is the packing station at the end of the
  belt — box it, bag it, glue it, or sort it into labelled bins:

  ```java
  words.stream().collect(Collectors.toList());        // → List
  words.stream().collect(Collectors.toSet());         // → Set
  words.stream().collect(Collectors.joining(", "));   // → "a, b, c"

  // groupingBy = SQL GROUP BY → Map<key, List<item>>
  Map<Integer, List<String>> byLen = words.stream()
      .collect(Collectors.groupingBy(String::length));
  // {2=[hi, ok], 5=[hello]}

  // groupingBy + downstream — count per key (THE interview one)
  Map<Integer, Long> counts = words.stream()
      .collect(Collectors.groupingBy(String::length,
                                     Collectors.counting()));
  // {2=2, 5=1}

  // partitioningBy — boolean split, ALWAYS exactly two keys
  Map<Boolean, List<String>> longShort = words.stream()
      .collect(Collectors.partitioningBy(w -> w.length() > 3));

  // toMap — build a lookup table
  Map<String, Integer> lenOf = words.stream()
      .collect(Collectors.toMap(w -> w, String::length));

  // reduce = fold a stream to one value (identity + accumulator)
  int sum = nums.stream().reduce(0, Integer::sum);
  ```

  `reduce` is the snowball: starts at the identity, rolls down the
  belt, packs everything it meets into one value. MCQ bait:
  `partitioningBy` always yields exactly two keys (true/false, even
  if one list is empty); `toMap` **throws** on a duplicate key
  unless you pass a third "merge" argument.

### §F Optional — "maybe absent," made explicit

- **The problem it solves** — a method that "returns an Account" can
  silently return `null` instead. Every caller must remember the
  null check; the one who forgets ships an NPE. `Optional<Account>`
  moves the warning into the *type*: an Account **or nothing** comes
  back — the compiler makes you deal with the nothing. Hook: `null`
  is a box that lies about being full; `Optional` is a box that
  admits it might be empty.
- **Before / after** — task: show the account's branch name in upper
  case, or `"UNKNOWN"` if anything along the way is missing.

  ```java
  // BEFORE — defensive ifs; one forgotten check = NPE in production
  Account acc = findAccount(id);            // might be null!
  String branch = "UNKNOWN";
  if (acc != null) {
      String b = acc.getBranch();           // might ALSO be null
      if (b != null) {
          branch = b.toUpperCase();
      }
  }

  // AFTER — one chain, zero ifs, same meaning
  String branch = findAccount(id)           // Optional<Account>
      .map(Account::getBranch)              // → Optional<String>
      .map(String::toUpperCase)             // still inside the box
      .orElse("UNKNOWN");                   // open it, with fallback
  ```

- **How the chain behaves** — the empty box short-circuits:
  - account found → each `map` transforms the value inside the box,
    `orElse` unwraps the result
  - account missing — or `getBranch()` returns null → the box is
    empty from that point; every later `map` is **skipped**, and
    `orElse` hands back `"UNKNOWN"`. No NPE possible anywhere.
    (`map` wraps a null result into an empty box automatically —
    that's the whole safety trick.)
- **The lifecycle (the thread tying it together)** — every Optional
  lives the same three steps: **1 make** the box → **2 work inside**
  it (safe while boxed) → **3 open** it. The AFTER chain above is
  exactly that: make (`findAccount`) → work (`map`, `map`) → open
  (`orElse`). Everything below is just those three steps in detail.
- **Step 1 — make the box** — wrap at the boundary where null enters
  the system; past that point, no nulls travel:

  ```java
  Optional<Account> findAccount(String id) {
      Account acc = db.lookup(id);          // legacy API, may be null
      return Optional.ofNullable(acc);      // null → empty box
  }
  // the three factories:
  //   Optional.ofNullable(acc)  null-safe wrap — the usual choice
  //   Optional.of(acc)          acc MUST be non-null (else NPE here)
  //   Optional.empty()          deliberately empty
  ```

- **Step 2 — work inside the box** — reshape the contents without
  opening it; an empty box ignores all of this:

  ```java
  findAccount(id)
      .filter(Account::isActive)       // test fails → box EMPTIES
      .map(Account::getBranch);        // transform → Optional<String>

  // flatMap: when the next step ITSELF returns an Optional
  Optional<Card> card = findAccount(id)
      .flatMap(Account::primaryCard);  // primaryCard(): Optional<Card>
  // map here would nest: Optional<Optional<Card>> — flatMap flattens
  ```

- **Step 3 — open the box** — five exits, one per situation, all on
  the same story:

  ```java
  // fallback VALUE — cheap constant
  Account a = findAccount(id).orElse(Account.GUEST);

  // fallback COMPUTED — supplier runs only if the box is empty
  Account b = findAccount(id).orElseGet(() -> loadGuest());

  // absence is an ERROR — throw instead
  Account c = findAccount(id)
      .orElseThrow(() -> new AccountNotFound(id));

  // just ACT if present — nothing to return
  findAccount(id).ifPresent(acc -> audit(acc));

  // act on BOTH paths (Java 9)
  findAccount(id).ifPresentOrElse(
      acc -> audit(acc),
      ()  -> log.warn("no account " + id));
  ```

- **orElse vs orElseGet (MCQ trap)** — `orElse(loadGuest())` calls
  `loadGuest()` **every time** — even when the account was found,
  you paid for a guest nobody needed (arguments are evaluated before
  the call, always). `orElseGet(() -> loadGuest())` runs it **only
  when empty**. Cheap constant → `orElse`; anything costly →
  `orElseGet`.
- **anti-pattern** — `if (o.isPresent()) o.get();` is just the null
  check again with extra steps — shaking the box, then tearing it
  open. Work through the lid (`map`/`orElse`); avoid `get()`.
- **rule** — `Optional` is a **return-type** signal, not a data
  holder: don't use it for fields or method parameters.

### §G default / static methods in interfaces

- **default method** — a method *with a body* in an interface, marked
  `default`. It lets you **add a method to an interface without
  breaking the thousands of classes already implementing it.** That's
  exactly how `List.sort`, `Iterable.forEach`, and the whole Stream
  bridge were added in Java 8. Hook: a room drawn onto the
  *blueprint* — and every house already built grows the room
  overnight, no renovation.

  ```java
  interface Greeter {
      String name();
      default String greet() {            // body lives in the interface
          return "Hi, " + name();         // implementers get it free
      }
  }
  ```

- **static method** — utility methods on the interface itself:
  `Comparator.comparing(...)`, `Stream.of(...)`.
- **the catch** — two interfaces with the same default method create
  the old diamond ambiguity; the compiler forces you to override and
  pick one (`B.super.hi()`). Cross-ref [core-java.md](core-java.md)
  §1 "Why no multiple class inheritance?".

### §H java.time — the new date/time API

- **Why it exists** — old `Date`/`Calendar` were **mutable** and
  **not thread-safe**, had 0-based months, and `SimpleDateFormat` was
  a famous production-bug source (shared instance → corrupted output
  under threads). The new types are all **immutable and thread-safe**.
  Hook: old `Date` was a shared whiteboard anyone could scribble on
  mid-read; `java.time` hands out printed calendar pages — a "change"
  simply prints a fresh page.
- **The types you name** —

  |Type|Holds|
  |---|---|
  |`LocalDate`|a date only (2026-07-19)|
  |`LocalTime`|a time only|
  |`LocalDateTime`|date + time, no zone|
  |`ZonedDateTime`|date + time + zone|
  |`Instant`|a point on the UTC timeline (epoch)|
  |`Duration` / `Period`|amount of time / amount of dates|
  |`DateTimeFormatter`|thread-safe format + parse|

  ```java
  LocalDate today  = LocalDate.now();                 // 2026-07-19
  LocalDate expiry = today.plusDays(30);              // NEW object (immutable)
  Period    age    = Period.between(dob, today);      // years/months/days

  ZonedDateTime ist = LocalDateTime.now()
      .atZone(ZoneId.of("Asia/Kolkata"));

  DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  today.format(fmt);                                  // "19-07-2026"
  ```

  - **immutable** means every "change" returns a *new* value —
    `today.plusDays(30)` doesn't touch `today`, it hands back a new
    date. Same discipline as `String`.
  - **The trap (code-output classic)** — call without assigning and
    nothing happens:

    ```java
    LocalDate d = LocalDate.of(2026, 7, 19);
    d.plusDays(30);        // result DISCARDED — d unchanged
    d = d.plusDays(30);    // correct — catch the new page
    ```

    Same trap as a bare `s.toUpperCase();` on a String — suspect it
    whenever an immutable type's return value isn't captured.

### §I The LTS list — cheap to memorize, certain to be asked

- **8, 11, 17, 21, 25.** Years: 2014 · 2018 · 2021 · 2023 · 2025.
- **Hook** — 8 then 11 then 17 (three-ish years apart), then a
  settled **two-year cadence**: 17 → 21 → 25. New LTS every 2 years.
  Since 17 they land every second **September**: Sep 2021 · Sep
  2023 · Sep 2025.

---

### The analogy card — nine pictures, the whole of Java 8

*Cover the right column; name the concept from the picture alone.*

|Picture|Concept|
|---|---|
|Socket + plug|functional interface + lambda|
|Photograph, not a live feed|effectively-final capture|
|Hand over the sheet music|method reference `::`|
|Conveyor belt + green ON button|stream · lazy · terminal fires it|
|Cartons ripped open onto the belt|`flatMap`|
|Packing station at belt's end|`collect` / `Collectors`|
|The box that admits it may be empty|`Optional` (`null` is the box that lies)|
|Room drawn onto the blueprint|`default` methods|
|Fresh calendar page per change|`java.time` immutability|

**Skipper's translation (study block):** Java 8 from scratch in
eight bites — functional interface → lambda → method reference →
stream → collectors → Optional → default methods → java.time — plus
the LTS list, now with a picture bolted onto every idea. Read once
to *understand*, replay the analogy card to *recall*, drill blind
off the cue-cards to *load*. The spine: Java 8 made functions
passable; everything else is that one idea in a costume.
