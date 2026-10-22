# Java version timeline 🐧

- **JEP (JDK Enhancement Proposal):** a proposal that describes and tracks a change to the JDK, such as a language feature, API, or JVM improvement.

- **Interview focus:** say the version, the headline feature, and one line
  of code for questions such as "which Java version?" and "what's new in Java 8?"

- **Study links:** [java-8.md](java-8.md) covers Java 8 in depth;
  [core-java.md §4](core-java.md#4-java-8-know-it-cold) provides rapid-fire answers.

- **LTS line:** **8, 11, 17, 21, 25** — memorize those five.

- **How to drill:** aloud, blind. For each version say *number → year →
  one headline feature*.

- **Follow-up:** if pressed, give the snippet. Lead with the feature that matters
  instead of reciting every change.

- **Coverage:** every permanent JEP delivered in Java 17, 21, and 25 appears
  below. Preview, incubator, and experimental work is isolated so it cannot be
  mistaken for a permanent feature. Small non-JEP maintenance changes are
  outside this guide.

---

## Navigation

- [Java 7–16 foundation](#java-7-2011--project-coin-ergonomics)

- [Java 17 LTS](#java-17-2021--lts-)

- [Java 21 LTS](#java-21-2023--lts--virtual-threads)

- [Java 25 LTS](#java-25-2025--lts--latest-lts)

- [Practice path](#practice-path)

- [Precision card](#precision-card)

## Status words

- **Permanent / final:** part of the Java SE or JDK platform without preview
  flags.

- **Preview:** fully specified but temporary; compile and run with
  `--enable-preview` for that exact JDK release.

- **Incubator:** a provisional API in a `jdk.incubator.*` module.

- **Experimental:** an implementation feature that can require an unlock flag
  and may change incompatibly.

## One-screen timeline

| Release | Year | LTS | Fastest memory hook |
|---:|---:|:---:|---|
| 7 | 2011 |  | try-with-resources |
| 8 | 2014 | ⭐ | lambdas and streams |
| 9 | 2017 |  | modules |
| 10 | 2018 |  | local `var` |
| 11 | 2018 | ⭐ | HTTP client and String conveniences |
| 12–13 | 2019 |  | switch and text-block previews |
| 14 | 2020 |  | switch expressions final |
| 15 | 2020 |  | text blocks final |
| 16 | 2021 |  | records and `instanceof` patterns final |
| 17 | 2021 | ⭐ | sealed classes |
| 21 | 2023 | ⭐ | virtual threads and pattern matching |
| 25 | 2025 | ⭐ | scoped values and simpler source files |

## Java 7 (2011) — "Project Coin" ergonomics

- Small syntax sugar + one big resource fix.

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

### Strings in `switch`

**Old way — an `if` / `else` chain:**

```java
String status = "PAID";
String action;
if ("PAID".equals(status)) action = "ship";
else if ("PENDING".equals(status)) action = "wait";
else action = "review";
```

**Java 7 — switch on the string value:**

```java
String status = "PAID";
String action;
switch (status) {
    case "PAID":    action = "ship"; break;
    case "PENDING": action = "wait"; break;
    default:        action = "review";
}
```

- Matching uses string content and is case-sensitive.

- In Java 7, a null selector throws `NullPointerException`; guard it first
  when null is possible. The old example's `"PAID".equals(status)` is null-safe.

- Arrow cases and switch expressions arrive later; this example uses Java 7
  statement syntax.

---

## Java 8 (2014) — functional Java. LTS ⭐

- A major shift toward functional composition and library evolution.

- **Lambdas** — inline functional-interface impl.

- **Stream API** — declarative collection pipelines.

- **Functional interfaces** — `Predicate` / `Function` / `Supplier` / `Consumer`.

- **`default` / `static` methods in interfaces** — evolve without breaking.

- **`Optional`** — models expected absence in a return type.

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
```

### JPMS — dependencies and exported packages

- **Old way:** put JARs on the class path; public types in internal packages
  are also accessible to other class-path code.

- **Java 9:** a named module declares dependencies with `requires` and its
  public API packages with `exports`.

```java
// bank.core/module-info.java
module bank.core {
    requires java.sql;          // bank.core reads the JDBC module
    exports com.bank.api;       // other modules can use this package
    // com.bank.internal is not exported
}
```

```java
// bank.core/com/bank/api/PaymentService.java
package com.bank.api;
public class PaymentService {}
```

```java
// bank.core/com/bank/internal/LedgerStore.java
package com.bank.internal;
public class LedgerStore {}
```

```java
// bank.app/module-info.java
module bank.app {
    requires bank.core;
}
```

```java
// In bank.app: the dependency and exported package make this legal.
import com.bank.api.PaymentService;

// Compile error even though LedgerStore is public: its package isn't exported.
// import com.bank.internal.LedgerStore;
```

- `exports` enables ordinary access to public API types. `opens` permits deep
  reflection into a package; use it selectively for frameworks that need it.

### Private interface methods

**Java 8 — repeat shared logic inside default methods:**

```java
interface Messages {
    default String hello(String name) { return "Hello, " + name.trim(); }
    default String goodbye(String name) { return "Bye, " + name.trim(); }
}
```

**Java 9 — extract a private helper:**

```java
interface Messages {
    default String hello(String name) { return "Hello, " + clean(name); }
    default String goodbye(String name) { return "Bye, " + clean(name); }
    private String clean(String name) { return name.trim(); }
}
```

- A private interface method has a body, is not inherited, and cannot be
  overridden. Private static helpers are supported too.

### Stream `takeWhile` / `dropWhile`

**Java 8 — stop a loop at the first failed predicate:**

```java
List<Integer> readings = Arrays.asList(1, 2, 0, 3);
List<Integer> prefix = new ArrayList<>();
for (int reading : readings) {
    if (reading <= 0) break;
    prefix.add(reading);
} // [1, 2]
```

**Java 9 — express prefix operations directly:**

```java
List<Integer> readings = List.of(1, 2, 0, 3);

readings.stream().takeWhile(n -> n > 0).collect(Collectors.toList()); // [1, 2]
readings.stream().dropWhile(n -> n > 0).collect(Collectors.toList()); // [0, 3]
readings.stream().filter(n -> n > 0).collect(Collectors.toList());    // [1, 2, 3]
```

- On an ordered stream, `takeWhile` keeps the matching prefix; `dropWhile`
  removes that prefix. Both use the first failing element as the boundary.

- `filter` examines the whole stream when collecting all results. The `3`
  survives filtering but is beyond the prefix boundary.

- Unordered streams do not provide these deterministic prefix results.

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

- A long-term-support release focused on cleanup and convenience.

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

- **`record`** finalized — concise data carrier with generated constructor,
  `equals`, `hashCode`, `toString`, and accessors.

- **Pattern matching for `instanceof`** — bind in the check.

```java
record Point(int x, int y) {}

Point p = new Point(1, 2);             // generated canonical constructor
p.x();                               // 1 — generated accessor, not getX()
p.y();                               // 2
p.equals(new Point(1, 2));            // true — component-based equality
boolean sameHash = p.hashCode() == new Point(1, 2).hashCode(); // true
p.toString();                        // typical output: Point[x=1, y=2]
```

- The record supplies these members unless you explicitly declare the
  corresponding permitted constructor or method.

- Component fields are private and final; records are **shallowly immutable**.
  A component referring to a mutable list still refers to that mutable list.

**Old way — write the equivalent value-class boilerplate:**

```java
final class Point {
    private final int x, y;

    Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Point)) return false;
        Point that = (Point) other;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(x, y); }

    @Override
    public String toString() { return "Point[x=" + x + ", y=" + y + "]"; }
}
```

- This class illustrates the same value semantics; a record's generated
  hash algorithm and exact string format are not contracts to reproduce.

**Pattern matching for `instanceof`:**

```java
if (obj instanceof String s) {          // s bound automatically
    System.out.println(s.length());
}
```

---

## Java 17 (2021) — LTS ⭐

- **Headline:** sealed classes, stronger JDK encapsulation, enhanced PRNGs.

- **Version boundary:** records and pattern matching for `instanceof` were
  finalized in **16**, although they are commonly adopted together with 17.

- **Examples:** snippets are independent; routine imports, enclosing code, and
  checked-exception handling are omitted where obvious.

### Stable coverage map

| JEP | Permanent change in Java 17 | What to remember |
|---:|---|---|
| 306 | Restore always-strict floating point | `strictfp` is redundant |
| 356 | Enhanced pseudo-random number generators | common selectable PRNG API |
| 382 | New macOS rendering pipeline | Metal replaces the deprecated OpenGL pipeline |
| 391 | macOS/AArch64 port | native Apple silicon port |
| 398 | Deprecate Applet API for removal | browser applets remain obsolete |
| 403 | Strongly encapsulate JDK internals | migrate away from internal packages |
| 407 | Remove RMI Activation | activation APIs are gone; ordinary RMI remains |
| 409 | Sealed classes | compiler-enforced closed hierarchies |
| 410 | Remove experimental AOT/JIT compiler | old `jaotc` and Graal JIT experiment removed |
| 411 | Deprecate Security Manager for removal | do not design new sandboxing around it |
| 415 | Context-specific deserialization filters | choose filters per context |

### Sealed classes — JEP 409

- Define a **closed inheritance hierarchy**.

- Every direct permitted subtype must be `final`, `sealed`, or `non-sealed`.

- Permitted subtypes must be in the same module, or the same package when
  using the unnamed module.

- **Reference:** [Sealed Classes](https://docs.oracle.com/en/java/javase/17/language/sealed-classes-and-interfaces.html).

**Old way — documentation could express the intent, but the compiler could
not enforce it:**

```java
interface PaymentResult {}
final class Approved implements PaymentResult {}
final class Declined implements PaymentResult {}
// Any other class could also implement PaymentResult.
```

**Java 17:**

```java
sealed interface PaymentResult permits Approved, Declined {}
record Approved(String receiptId) implements PaymentResult {}
record Declined(String reason) implements PaymentResult {}
// class Pending implements PaymentResult {} // compile error
```

- Exhaustive pattern `switch` becomes final in Java 21; in 17 it is preview.

### Enhanced pseudo-random number generators — JEP 356

- A common **`RandomGenerator` API** supports selectable algorithms and
  interfaces for splittable/jumpable generators.

- Use **`SecureRandom`** for secrets and security-sensitive values.

- **Reference:** [RandomGenerator API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/RandomGenerator.html).

**Old way — couple code to one generator class:**

```java
Random random = new Random(42);
int die = random.nextInt(6) + 1;
```

**Java 17 — select by algorithm behind one interface:**

```java
RandomGenerator random = RandomGeneratorFactory
    .<RandomGenerator>of("L64X128MixRandom")
    .create(42);
int die = random.nextInt(1, 7);
List<Integer> rolls = random.ints(5, 1, 7).boxed().toList();
```

### Always-strict floating-point semantics — JEP 306

- All floating-point operations use consistently strict **IEEE 754 semantics**.

- On old x86 hardware, pre-17 default calculations could use an extended
  exponent range unless marked `strictfp`.

- In **Java 17**, `strictfp` is unnecessary.

- **Reference:** [JEP 306](https://openjdk.org/jeps/306).

```java
// Before 17: opt into strict evaluation where platform differences mattered.
strictfp class Pricing { static double total(double a, double b) { return a + b; } }

// Java 17: this has the same always-strict semantics.
class Pricing17 { static double total(double a, double b) { return a + b; } }
```

- This changes reproducibility at the floating-point edge cases; it does not
  make binary floating point exact for decimal money.

### Context-specific deserialization filters — JEP 415

- A JVM-wide filter factory can choose or combine an **`ObjectInputFilter`**
  for each deserialization context.

- Filters are **opt-in**; Java serialization does not become safe automatically.

- **Reference:** [Serialization Filtering](https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html).

```java
static final ThreadLocal<ObjectInputFilter> CONTEXT_FILTER = new ThreadLocal<>();

// Install once, before deserialization starts.
ObjectInputFilter.Config.setSerialFilterFactory((current, requested) -> {
    ObjectInputFilter selected = current != null ? current : CONTEXT_FILTER.get();
    if (selected == null) return requested;
    if (requested == null) return selected;
    return ObjectInputFilter.merge(selected, requested);
});

CONTEXT_FILTER.set(ObjectInputFilter.Config.createFilter(
    "com.bank.dto.*;java.base/*;maxdepth=20;maxrefs=10000;!*"));
try { readRequest(); } finally { CONTEXT_FILTER.remove(); }
```

- Prefer formats that do not instantiate arbitrary Java classes when you
  control the protocol.

### Strongly encapsulate JDK internals — JEP 403

- The single `--illegal-access=permit` escape hatch no longer opens old JDK
  internals.

- Migrate to **supported APIs**; use a narrow `--add-opens` only as a temporary
  bridge for reflection-heavy libraries.

- Critical internal APIs such as `sun.misc.Unsafe` remain exceptions.

- **Reference:** [JEP 403](https://openjdk.org/jeps/403).

```java
var field = String.class.getDeclaredField("value");
field.setAccessible(true); // InaccessibleObjectException on Java 17
```

```sh
# Temporary, targeted bridge for class-path code; upgrade the library next.
java --add-opens java.base/java.lang=ALL-UNNAMED -jar app.jar
```

### Platform and migration changes

| Change | Immediate migration question |
|---|---|
| Metal rendering — JEP 382 | Does a macOS desktop app rely on OpenGL-specific rendering behavior? |
| macOS/AArch64 port — JEP 391 | Are native libraries also available for Apple silicon? |
| Applet API deprecated for removal — JEP 398 | Has applet code been moved to a supported deployment model? |
| RMI Activation removed — JEP 407 | Does old distributed code import `java.rmi.activation`? |
| Experimental AOT/JIT compiler removed — JEP 410 | Do scripts call `jaotc` or enable the experimental JVMCI compiler? |
| Security Manager deprecated for removal — JEP 411 | Does an application still depend on Security Manager policy files? |

### Not permanent in Java 17

- Pattern matching for `switch` is **preview**.

- The Foreign Function & Memory and Vector APIs are **incubating in 17**.

- **Reference:** [JDK 17 changes](https://docs.oracle.com/en/java/javase/17/migrate/significant-changes-jdk-release.html).

### Practice and mastery

- Complete [RandomGeneratorSelection.java](lab/13-version-features/RandomGeneratorSelection.java).

- Complete [ImmutableRecordSnapshot.java](lab/10-core-contracts/ImmutableRecordSnapshot.java)
  to combine a sealed hierarchy with records. Records themselves became final
  in Java 16.

- Can you explain why `RandomGenerator` is not a replacement for
  `SecureRandom`?

- Can you state the three rules imposed on a direct subtype of a sealed type?

- Can you distinguish a Java language feature from a migration-only platform
  change?

---

## Java 21 (2023) — LTS ⭐ virtual threads

- **Headline:** virtual threads, record patterns, pattern `switch`, sequenced collections.

- **Examples:** snippets are independent; routine imports, enclosing
  classes/methods and checked-exception handling are omitted where obvious.

### Stable coverage map

| JEP | Permanent change in Java 21 | What to remember |
|---:|---|---|
| 431 | Sequenced collections | uniform first, last, and reversed operations |
| 439 | Generational ZGC | young and old generations for low-pause GC |
| 440 | Record patterns | destructure records in patterns |
| 441 | Pattern matching for `switch` | type cases, guards, and exhaustiveness |
| 444 | Virtual threads | thread-per-task style for blocking workloads |
| 449 | Deprecate Windows 32-bit x86 port for removal | migrate 32-bit deployments |
| 451 | Prepare to disallow dynamic agent loading | dynamic attachment warns by default |
| 452 | Key Encapsulation Mechanism API | standard API for KEM protocols |

- **Placeholder:** `handleRequest()` stands for application code.

### Virtual threads — JEP 444

- Cheap threads for many tasks **waiting on I/O**.

- They improve concurrency; they don't make CPU work execute faster.

- **Reference:** [Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

**Old way — bound concurrency with a pool of platform threads:**

```java
try (var executor = Executors.newFixedThreadPool(100)) {
    for (int i = 0; i < 10_000; i++) executor.submit(() -> handleRequest());
}
```

**Java 21 — one virtual thread per task:**

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) executor.submit(() -> handleRequest());
} // close() waits for tasks; still limit access to scarce resources, e.g. DB connections

Thread task = Thread.startVirtualThread(() -> handleRequest());
task.join(); // virtual threads are daemon threads; keep the JVM alive until done
```

### Pattern matching for `switch` — JEP 441

- Match **reference types** in `switch`.

- Add **`when` guards** and handle **`null`** explicitly.

- **Reference:** [Pattern Matching for switch](https://docs.oracle.com/en/java/javase/21/language/pattern-matching-switch.html).

**Old way:**

```java
String describe(Object value) {
    if (value == null) return "missing";
    if (value instanceof String s && s.isBlank()) return "blank";
    if (value instanceof String s) return s.toUpperCase();
    if (value instanceof Integer n) return "int=" + n;
    return value.toString();
}
```

**Java 21:**

```java
String describe(Object value) {
    return switch (value) {
        case null -> "missing";
        case String s when s.isBlank() -> "blank";
        case String s -> s.toUpperCase();
        case Integer n -> "int=" + n;
        default -> value.toString();
    };
}
```

- Switching over a sealed hierarchy can be exhaustive without `default`
  when all permitted subtypes are covered.

### Record patterns — JEP 440

- Extract components directly, including **nested records**.

- **Example types:** `record Point(int x, int y) {}` and
  `record Box(Point point) {}`; `value` has type `Object`.

- **Reference:** [Record Patterns](https://docs.oracle.com/en/java/javase/21/language/record-patterns.html).

**Old way:**

```java
if (value instanceof Box box) {
    Point point = box.point();
    if (point != null) System.out.println(point.x() + point.y());
}
```

**Java 21:**

```java
if (value instanceof Box(Point(int x, int y))) {
    System.out.println(x + y);
} // null doesn't match a record pattern
```

### Sequenced collections — JEP 431

- **New interfaces:** `SequencedCollection`, `SequencedSet`, and `SequencedMap`.

- Uniform access to **both ends** and a reversed **view**.

- **Reference:** [Sequenced Collections](https://docs.oracle.com/en/java/javase/21/core/creating-sequenced-collections-sets-and-maps.html).

**Old way:**

```java
var names = new ArrayList<>(List.of("Ada", "Linus"));
String first = names.get(0);
String last = names.get(names.size() - 1);
var backwards = new ArrayList<>(names);
Collections.reverse(backwards); // separate copy
```

**Java 21:**

```java
SequencedCollection<String> names = new ArrayList<>(List.of("Ada", "Linus"));
names.getFirst();                  // Ada
names.getLast();                   // Linus
names.reversed().addFirst("Grace"); // view changes original: [Ada, Linus, Grace]

SequencedSet<String> ids = new LinkedHashSet<>(List.of("a", "b"));
ids.addFirst("b");                 // moves existing b to front: [b, a]

SequencedMap<String, Integer> scores = new LinkedHashMap<>();
scores.putLast("Ada", 10);
scores.firstEntry();               // Ada=10
scores.pollLastEntry();            // removes and returns Ada=10
```

### Generational ZGC — JEP 439

- Collect **young objects** more frequently, independently of old ones,
  while preserving ZGC's low-pause design.

- **Generational hypothesis:** most objects die young, so collecting them
  separately reduces GC work.

- **Reference:** [JEP 439](https://openjdk.org/jeps/439).

- Generational ZGC is opt-in in **21**, becomes the default ZGC mode in **23**,
  and replaces the non-generational mode in **24**.

```sh
# Java 21: select ZGC and its generational mode.
java -XX:+UseZGC -XX:+ZGenerational -jar app.jar
```

### Key Encapsulation Mechanism API — JEP 452

- A KEM lets a sender create a shared secret plus an encapsulation message;
  the receiver recovers the same secret with its private key.

- Earlier JDKs had no standard KEM abstraction, so applications depended on
  provider-specific APIs or built protocol glue themselves.

```java
var keyPair = KeyPairGenerator.getInstance("X25519").generateKeyPair();

KEM kem = KEM.getInstance("DHKEM");
KEM.Encapsulated sent = kem.newEncapsulator(keyPair.getPublic()).encapsulate();
SecretKey senderKey = sent.key();

SecretKey receiverKey = kem.newDecapsulator(keyPair.getPrivate())
    .decapsulate(sent.encapsulation());

boolean sameSecret = Arrays.equals(
    senderKey.getEncoded(), receiverKey.getEncoded());
```

- Protocol design still requires correct authentication, key lifecycle, and
  algorithm choices; the API supplies the KEM primitive.

- **Reference:** [Java 21 `KEM` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/KEM.html).

### Platform and migration changes

- **Windows 32-bit x86 port deprecated — JEP 449:** move deployments and
  native dependencies to a supported 64-bit target.

- **Dynamic agent loading warning — JEP 451:** tools that attach agents to a
  running JVM now trigger a warning. Startup agents remain supported with
  `-javaagent` or `-agentlib`.

### Not permanent in Java 21

- String templates, unnamed patterns and variables, unnamed classes and
  instance `main`, the Foreign Function & Memory API, Structured Concurrency,
  and Scoped Values are **preview** in 21.

- The Vector API is **incubating**.

- **Reference:** [JDK 21 changes](https://docs.oracle.com/en/java/javase/21/migrate/significant-changes-jdk-release.html).

### Practice and mastery

- Complete [PatternSwitchRouting.java](lab/13-version-features/PatternSwitchRouting.java).

- Complete [SequencedCollectionViews.java](lab/13-version-features/SequencedCollectionViews.java).

- Complete [VirtualThreadFanout.java](lab/12-concurrency-async/VirtualThreadFanout.java).

- Can you explain why virtual threads increase throughput for blocking work
  but do not make CPU-bound work faster?

- Can you distinguish a reversed view from a reversed copy?

- Can you write an exhaustive switch over a sealed hierarchy without a
  `default` arm?

---

## Java 25 (2025) — LTS ⭐ (latest LTS)

- **Headline:** scoped values, module imports, compact source files / instance
  `main`, flexible constructors. These are **final in 25**.

### Stable coverage map

| JEP | Permanent change in Java 25 | What to remember |
|---:|---|---|
| 503 | Remove 32-bit x86 port | JDK no longer builds for Linux x86-32 |
| 506 | Scoped values | bounded, one-way context propagation |
| 510 | Key Derivation Function API | standard KDF abstraction |
| 511 | Module import declarations | import exported packages from a module |
| 512 | Compact source files and instance `main` | less ceremony for small programs |
| 513 | Flexible constructor bodies | statements may precede `super(...)` or `this(...)` |
| 514 | AOT command-line ergonomics | one-step cache training and creation |
| 515 | AOT method profiling | carry training profiles into startup |
| 518 | JFR cooperative sampling | safer, more accurate stack sampling |
| 519 | Compact object headers | 64-bit object headers as a product feature |
| 520 | JFR method timing and tracing | targeted invocation and duration data |
| 521 | Generational Shenandoah | generational mode is no longer experimental |

### Scoped values — JEP 506

- Bind **request context** for the duration of a call.

- Binding cleanup is **automatic**, even on exceptions.

- The **binding is immutable**; a mutable bound object is still mutable.

- **Reference:** [ScopedValue API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html).

**Old way — thread-local context:**

```java
static final ThreadLocal<String> USER = new ThreadLocal<>();

void handle() {
    USER.set("Ada");
    try { logUser(); } finally { USER.remove(); }
}
void logUser() { System.out.println(USER.get()); }
```

**Java 25 — scoped binding:**

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

void handle() {
    ScopedValue.where(USER, "Ada").run(() -> logUser());
}
void logUser() { System.out.println(USER.get()); } // Ada inside scope
```

- Nested scopes can temporarily **rebind** the value.

- Ordinary threads/executors don't automatically inherit it; structured task
  scopes provide inheritance through the still-preview structured concurrency API.

### Key Derivation Function API — JEP 510

- A KDF derives one or more keys from input key material, salt, and
  context-specific information.

- Earlier code used provider-specific APIs or manually assembled primitives.
  Java 25 standardizes the high-level `KDF` API.

```java
KDF hkdf = KDF.getInstance("HKDF-SHA256");

AlgorithmParameterSpec spec = HKDFParameterSpec.ofExtract()
    .addIKM(inputKeyMaterial)
    .addSalt(salt)
    .thenExpand(info, 32);

SecretKey aesKey = hkdf.deriveKey("AES", spec);
```

- Use a protocol-defined construction and parameters; a KDF API does not
  choose the security design for you.

- **Reference:** [Java 25 `KDF` API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/javax/crypto/KDF.html).

### Module import declarations — JEP 511

- Import the public types in packages **exported by a module**, on demand.

- Works in ordinary source files too, without `module-info.java`.

- **Reference:** [Module Import Declarations](https://docs.oracle.com/en/java/javase/25/language/module-import-declarations.html).

**Old way:**

```java
import java.util.List;
import java.util.stream.Stream;
import java.time.LocalDate;
```

**Java 25:**

```java
import module java.base;

class Demo {
    List<LocalDate> dates = Stream.of(LocalDate.of(2025, 9, 16)).toList();
}
```

- `import java.util.*` doesn't include subpackages; a module import covers
  its exported packages.

- Conflicting simple names still need **explicit imports**.

### Compact source files and instance `main` — JEP 512

- Less ceremony for **small programs**.

- **`java.lang.IO`** supplies simple console I/O.

- **Reference:** [JEP 512](https://openjdk.org/jeps/512).

**Old way — `Hello.java`:**

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

**Java 25 — complete `Hello.java`:**

```java
void main() {
    String name = IO.readln("Name: ");
    IO.println("Hello, " + name + "!");
}
```

- **Run:** `java Hello.java`.

- Compact source files implicitly import **`java.base`**.

- Instance `main` methods also work in **explicitly declared classes**.

### Flexible constructor bodies — JEP 513

- Validate or compute arguments **before `super(...)` / `this(...)`**.

- **Example superclass:** `class Account { Account(int balance) {} }`.

- **Reference:** [Flexible Constructor Bodies](https://docs.oracle.com/en/java/javase/25/language/flexible-constructor-bodies.html).

**Old way — use a static helper to validate before the superclass runs:**

```java
class Savings extends Account {
    Savings(int balance) { super(checked(balance)); }
    static int checked(int balance) {
        if (balance < 0) throw new IllegalArgumentException("negative balance");
        return balance;
    }
}
```

**Java 25:**

```java
class Savings extends Account {
    Savings(int balance) {
        if (balance < 0) throw new IllegalArgumentException("negative balance");
        super(balance);
    }
}
```

- **Before `super(...)`:** you can initialize your own fields.

- You cannot read instance fields, call instance methods, or expose `this`
  before `super(...)`.

### Compact object headers — JEP 519

- **Product feature in 25**, off by default.

- **64-bit headers** on supported 64-bit HotSpot platforms reduce per-object
  overhead.

- Especially useful for heaps containing **many small objects**: less space per
  object means a smaller heap footprint and better cache density.

- **Reference:** [java command](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html).

```sh
java -XX:+UseCompactObjectHeaders -jar app.jar
```

### Removed 32-bit x86 port — JEP 503

- JDK 25 removes the Linux x86-32 port. Move the JVM, native libraries, and
  deployment image to a supported architecture.

### Not permanent in Java 25

- **Stable Values** (JEP 502) is a feature name, but the API is **preview**.

- PEM Encodings (JEP 470), Structured Concurrency (JEP 505), and primitive
  patterns (JEP 507) are also **preview**.

- The Vector API (JEP 508) is **incubating**.

- JFR CPU-Time Profiling (JEP 509) is **experimental**.

- Scoped Values are **permanent** in 25.

- **Reference:** [JDK 25 changes](https://docs.oracle.com/en/java/javase/25/migrate/significant-changes-jdk-25.html).

### Practice and mastery

- Complete [ScopedValueContext.java](lab/13-version-features/ScopedValueContext.java).

- Complete [FlexibleConstructorValidation.java](lab/13-version-features/FlexibleConstructorValidation.java).

- Complete [CompactModuleCatalog.java](lab/13-version-features/CompactModuleCatalog.java).

- Can you explain why a scoped binding has a bounded lifetime while a
  `ThreadLocal` value needs explicit removal?

- Can you state what code is forbidden before `super(...)`?

- Can you distinguish an AOT cache from a native executable?

- Can you identify every preview, incubator, and experimental item above
  without calling it permanent?

---

## Practice path

- **Java 8 foundation:** use [java-8.md](java-8.md) and its six Java 8
  compatible labs.

- **Java 17:** [RandomGeneratorSelection.java](lab/13-version-features/RandomGeneratorSelection.java)
  plus [ImmutableRecordSnapshot.java](lab/10-core-contracts/ImmutableRecordSnapshot.java).

- **Java 21:** [PatternSwitchRouting.java](lab/13-version-features/PatternSwitchRouting.java),
  [SequencedCollectionViews.java](lab/13-version-features/SequencedCollectionViews.java),
  and [VirtualThreadFanout.java](lab/12-concurrency-async/VirtualThreadFanout.java).

- **Java 25:** [ScopedValueContext.java](lab/13-version-features/ScopedValueContext.java),
  [FlexibleConstructorValidation.java](lab/13-version-features/FlexibleConstructorValidation.java),
  and [CompactModuleCatalog.java](lab/13-version-features/CompactModuleCatalog.java).

- **Runtime drill:** run one application with the GC, AOT, compact-header,
  and JFR commands shown above; record which options change compatibility,
  startup, footprint, or diagnostics.

## Precision card

| Prompt | Precise answer |
|---|---|
| Records: Java 16 or 17? | Final in 16; commonly adopted on the 17 LTS |
| Sealed classes? | Final in 17 |
| Pattern `switch` and record patterns? | Final in 21 |
| Virtual threads? | Final in 21; best suited to numerous blocking tasks |
| `reversed()` copy or view? | View |
| Scoped Values? | Preview in 21; final in 25 |
| Structured Concurrency in 25? | Preview |
| Stable Values in 25? | Preview despite the name |
| Compact object headers in 25? | Product feature, off by default |
| AOT cache? | Precomputed JVM artifacts for a matching app/JDK/platform; it is not a standalone native binary |

## Final mastery checklist

You know the version timeline when you can do all of these without looking:

- Recite the LTS line: 8, 11, 17, 21, 25.

- Place records, sealed classes, record patterns, and pattern `switch` in the
  release where each became permanent.

- Give one old-way/new-way code contrast for each code-facing 17, 21, and 25
  feature.

- Explain the workloads suited to virtual threads and the role of resource
  limits such as connection pools.

- Explain view aliasing in sequenced collections.

- Explain scoped binding lifetime and nested rebinding.

- Separate permanent features from preview, incubator, and experimental work.

- Name migration risks involving JDK internals, agents, removed ports, and
  removed legacy components.

- Complete the linked labs and explain the nearest plausible wrong solution.
