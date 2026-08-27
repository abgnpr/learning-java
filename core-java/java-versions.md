# Java version Timeline 🐧

**Why this exists:** "which Java version?" and "what's new in Java 8?"
are panel certainties. This is the chronological spine — say the
version, the headline feature, one line of code. Java 8 gets a study
block of its own in [java-8.md](java-8.md). The spoken rapid-fire
layer is
[core-java.md §4](core-java.md#4-java-8-know-it-cold);
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
confirm the deployment JDK before an interview (see [core-java.md](core-java.md)
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

**Next:** the Java 8 feature set built from nothing — functional
interfaces, lambdas, streams, `Optional`, `java.time` —
[java-8.md](java-8.md).
