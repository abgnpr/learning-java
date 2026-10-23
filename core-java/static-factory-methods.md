# Java static factories — know what you get 🐧

- A factory call can return a **new value**, a **cached object**, a **live view**,
  or a **snapshot**. Similar-looking calls can give you very different behavior.

- **Goal:** choose the right creation method and predict what happens when the
  input is null, the source changes, or someone tries to modify the result.

- **Prerequisites:** collections, generics, `equals`, and the basics in
  [java-8.md](java-8.md).

- **Versions:** examples run on Java 21. Later APIs carry their version beside
  their first use; `java.time` and `Optional` start at Java 8.

---

## Navigation

- [§A What a static factory does](#a-what-a-static-factory-does)

- [§B Create collections](#b-create-collections)

- [§C Views, copies, and ownership](#c-views-copies-and-ownership)

- [§D Create dates, instants, and durations](#d-create-dates-instants-and-durations)

- [§E Wrappers, numbers, and Optional](#e-wrappers-numbers-and-optional)

- [§F Internals worth knowing](#f-internals-worth-knowing)

- [§G Recall and quick choice](#g-recall-and-quick-choice)

## How to use the examples

- Read §A → §C first. The most useful distinction is **view versus snapshot**.

- Predict the output before reading the comments.

- Short examples can share setup within a subsection. Assume the appropriate
  `java.util`, `java.util.stream`, `java.time`, or `java.math` imports. The
  collection-view example includes a complete runnable class.

- Commented-out calls show deliberate failures; uncomment one at a time to
  observe the exception.

---

## §A What a static factory does

### A named way to obtain an object

```java
LocalDate settlement = LocalDate.of(2026, 10, 23);
Integer retries = Integer.valueOf(3);
List<String> stages = List.of("VALIDATE", "POST"); // Java 9
```

- A **static factory method** is a static method that returns an instance.
  You call a method instead of invoking a constructor directly.

- The return type tells you what operations are available. Its contract tells
  you which operations are supported and what state is shared.

- A factory can validate input, reuse an existing object, or select a hidden
  implementation. Calling one does **not** promise a new allocation.

- The **Factory Method design pattern** uses an overridable instance method to
  delegate creation. A static factory has no such override point.

### Read the name, then check the contract

| Name | Usual intent | Example |
|---|---|---|
| `of` | Build from supplied values | `List.of("A", "B")` |
| `from` | Extract or convert | `Instant.from(offsetDateTime)` |
| `valueOf` | Represent a value as an object | `Integer.valueOf(3)` |
| `copyOf` | Capture independent container state | `List.copyOf(source)` |
| `asList` | Adapt an existing representation | `Arrays.asList(array)` |
| `now` | Read a clock | `LocalDate.now(clock)` |
| `parse` | Interpret text | `Duration.parse("PT5M")` |

- These names are conventions. `of` does not universally mean immutable;
  `valueOf` does not universally mean cached.

- Ask three questions: **Can I change it? What does it share? What input does
  it reject?**

---

## §B Create collections

- **API:** [List](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html#unmodifiable),
  [Set](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Set.html#unmodifiable),
  and [Map](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html#unmodifiable).

### `List.of`, `Set.of`, `Map.of` — constants without a mutable builder

```java
// All three factories: Java 9.
List<String> stages = List.of("VALIDATE", "POST", "NOTIFY");
Set<String> terminalStates = Set.of("SUCCESS", "FAILED");
Map<String, Integer> retries = Map.of("PAYMENT", 3, "REFUND", 5);

System.out.println(stages.get(0));                 // VALIDATE
System.out.println(terminalStates.contains("FAILED")); // true
System.out.println(retries.get("REFUND"));         // 5
```

- All three return **unmodifiable containers**. Adding, removing, or replacing
  contents through their APIs throws `UnsupportedOperationException`.

- All three reject null elements, keys, or values with `NullPointerException`.

- A list preserves argument order and permits duplicates.

- A set rejects equal duplicates with `IllegalArgumentException`.

- A map rejects equal duplicate keys with `IllegalArgumentException`. Duplicate
  values are fine. `Map.of` does not use last-write-wins semantics.

```java
List<String> repeated = List.of("READ", "READ"); // allowed: two elements

// Set.of("READ", "READ");             // IllegalArgumentException
// Map.of("limit", 3, "limit", 5);     // IllegalArgumentException
// List.of("READ", (String) null);     // NullPointerException
// stages.set(0, "SKIP");              // UnsupportedOperationException

// Need to edit? Request a mutable copy explicitly.
List<String> editable = new ArrayList<>(stages);
editable.add("AUDIT"); // stages still has its original three elements
```

- `Set.of` and `Map.of` give **no iteration-order guarantee**. Do not rely on
  their printed order or use it as an export format.

- Their return types are `List`, `Set`, and `Map`. Do not cast them to
  `ArrayList`, `HashSet`, or `HashMap`.

### `Map.ofEntries` — larger maps and readable pairs

```java
// Java 9.
Map<String, Integer> retryPolicy = Map.ofEntries(
    Map.entry("PAYMENT", 3),
    Map.entry("REFUND", 5),
    Map.entry("REVERSAL", 8));

System.out.println(retryPolicy.get("REVERSAL")); // 8
```

- `Map.of` has overloads for zero through ten mappings. It has no general
  alternating-key/value varargs overload.

- `Map.ofEntries` accepts an arbitrary number of entries, including zero.
  Null entries, keys, or values fail; duplicate keys also fail.

- `Map.entry(k, v)` creates an unmodifiable pair. Its `setValue` method throws;
  the pair is not a live connection to a map.

- `List.of` and `Set.of` also have zero-through-ten overloads, plus a varargs
  form for larger inputs. You do not need to memorize each signature.

### Empty, singleton, and repeated values

```java
List<String> noWarnings = Collections.emptyList();
List<String> oneMissingSlot = Collections.singletonList(null);
List<String> placeholders = Collections.nCopies(3, "PENDING");

System.out.println(noWarnings.size());       // 0
System.out.println(oneMissingSlot.get(0));   // null
System.out.println(placeholders);            // [PENDING, PENDING, PENDING]
```

- `emptyList`, `emptySet`, and `emptyMap` return typed empty containers
  (Java 5). Prefer these or an appropriate `of()` over returning null.

- `singletonList`, `singleton`, and `singletonMap` create unmodifiable
  one-item containers. Unlike `of`, they **allow null**.

- `nCopies(n, value)` represents `n` repetitions of **the same reference**.
  It does not construct or clone `n` objects. Negative counts fail.

```java
List<StringBuilder> shared = Collections.nCopies(3, new StringBuilder());
shared.get(0).append("x");
System.out.println(shared); // [x, x, x]

// Need independent objects? Invoke construction for each position.
List<StringBuilder> separate = IntStream.range(0, 3)
    .mapToObj(i -> new StringBuilder())
    .collect(Collectors.toCollection(ArrayList::new));

separate.get(0).append("x");
System.out.println(separate); // [x, , ]
```

### Mastery check

- Which factories reject duplicates? Which allow a null element?

- Why does `nCopies(3, builder)` expose the same change at all three positions?

---

## §C Views, copies, and ownership

- **API:** [Arrays](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html),
  [Collections](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html),
  [Stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html),
  and [Collectors](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Collectors.html).

### The four list behaviors

| Creation | Change size? | Replace elements with `set`? | Later source writes visible? | Null elements? |
|---|---|---|---|---|
| `Arrays.asList(array)` | No | Yes | Yes | Allowed |
| `Collections.unmodifiableList(list)` | No | No | Yes | Retained |
| `List.of(values)` | No | No | Array-slot writes: no | Rejected |
| `List.copyOf(collection)` | No | No | Container writes: no | Rejected |
| `new ArrayList<>(collection)` | Yes | Yes | Container writes: no | Allowed |

- **Fixed-size:** the number of positions cannot change. Existing positions
  can still be writable.

- **Unmodifiable view:** this reference blocks writes, but delegates reads to
  a container that another reference may change.

- **Snapshot:** later source-container changes do not alter the result.
  The element objects can still be shared.

### Watch the same array through four references

```java
import java.util.*;

public class FactoryViewsDemo {
    public static void main(String[] args) {
        String[] source = {"A", "B"};

        List<String> view = Arrays.asList(source);
        List<String> readOnlyView = Collections.unmodifiableList(view);
        List<String> fromValues = List.of(source); // Java 9
        List<String> snapshot = List.copyOf(view); // Java 10

        source[0] = "X";
        view.set(1, "Y"); // writes through to source[1]

        System.out.println(view);         // [X, Y]
        System.out.println(readOnlyView); // [X, Y]
        System.out.println(fromValues);   // [A, B]
        System.out.println(snapshot);     // [A, B]

        // view.add("Z");             // UnsupportedOperationException
        // readOnlyView.set(0, "Z");  // UnsupportedOperationException
    }
}
```

```text
source array <---- same slots ----> Arrays.asList(source)
                                          ^
                                          |
                               unmodifiableList(view)

List.of(source)     ---- independent slots, original references ----> A, B
List.copyOf(view)   ---- independent slots, original references ----> A, B
```

- `Arrays.asList` is backed by the supplied array. It is **not** a normal
  resizable `ArrayList`; sorting it also rearranges the array.

- `Collections.unmodifiableList` adds a write barrier through that reference.
  It does not take a snapshot or add synchronization.

- `List.copyOf` captures the source's iteration order. It can reuse an eligible
  existing unmodifiable list; a snapshot does not require a fresh identity.

### A snapshot is still shallow

```java
StringBuilder status = new StringBuilder("PENDING");
List<StringBuilder> shallow = List.copyOf(List.of(status));
List<String> frozenText = List.of(status.toString());

status.append("!");

System.out.println(shallow.get(0));   // PENDING!
System.out.println(frozenText.get(0)); // PENDING
```

- Copying a container copies its element references. It does not recursively
  copy objects or nested collections.

- Use immutable elements or copy the mutable state you actually need to own.
  A `final List<T>` only prevents reassignment of the variable.

### Copy policy: nulls, duplicates, and order

```java
// All three copyOf factories: Java 10.
List<String> ordered = List.copyOf(Arrays.asList("B", "A", "B"));
Set<String> unique = Set.copyOf(Arrays.asList("B", "A", "B"));
Map<String, Integer> config = Map.copyOf(Map.of("limit", 5));

System.out.println(ordered);       // [B, A, B]
System.out.println(unique.size()); // 2 — no iteration-order promise
System.out.println(config.get("limit")); // 5
```

- `List.copyOf` preserves duplicates and iteration order.

- `Set.copyOf` **deduplicates**; `Set.of` rejects duplicates. Do not assume which
  equal object survives deduplication.

- `Map.copyOf` captures mappings without promising the source map's order,
  comparator, or special key-equivalence policy.

- All three reject a null source and null contents. A copy also requires a
  stable source: copying during unsynchronized writes is not an atomic snapshot.

```java
// Preserve null elements: own a private copy, then wrap it.
List<String> nullableSource = new ArrayList<>(Arrays.asList("A", null));
List<String> nullableSnapshot = Collections.unmodifiableList(
    new ArrayList<>(nullableSource));

nullableSource.clear();
System.out.println(nullableSnapshot); // [A, null]

// Preserve encounter order: use a private LinkedHashMap copy.
Map<String, Integer> draft = new LinkedHashMap<>();
draft.put("FIRST", 1);
draft.put("SECOND", 2);
Map<String, Integer> orderedSnapshot = Collections.unmodifiableMap(
    new LinkedHashMap<>(draft));

System.out.println(orderedSnapshot.keySet()); // [FIRST, SECOND]
```

- The same ownership distinction applies to `unmodifiableSet` and
  `unmodifiableMap`: wrappers are live; private copies provide isolation.

### Arrays: reference expansion versus one primitive-array element

```java
String[] words = {"A", "B"};
List<String> expanded = List.of(words);
List<String[]> oneArray = List.<String[]>of(words);

int[] numbers = {10, 20};
List<int[]> primitiveArray = Arrays.asList(numbers);
List<Integer> boxed = Arrays.stream(numbers).boxed().toList(); // Java 16

System.out.println(expanded.size());       // 2
System.out.println(oneArray.size());       // 1
System.out.println(primitiveArray.size()); // 1
System.out.println(boxed);                 // [10, 20]
```

- A varargs factory can expand a reference array into element arguments.
  An explicit type argument can instead request one array element.

- An `int[]` is one object; these generic factories do not box each primitive
  automatically. Use a primitive stream and `boxed()` when that is intended.

### Stream results: three similar names, three contracts

| Expression | Since | Result mutation | Null elements |
|---|---|---|---|
| `stream.collect(Collectors.toList())` | 8 | Unspecified | Accepted by OpenJDK 21 |
| `stream.collect(Collectors.toUnmodifiableList())` | 10 | Unsupported | Rejected |
| `stream.toList()` | 16 | Unsupported | Allowed |

```java
List<String> readOnly = Stream.of("A", null).toList();
System.out.println(readOnly); // [A, null]

// readOnly.add("B"); // UnsupportedOperationException

// Explicitly require a mutable ArrayList:
ArrayList<String> editable = Stream.of("A", "B")
    .collect(Collectors.toCollection(ArrayList::new));
editable.add("C");
System.out.println(editable); // [A, B, C]
```

- `Collectors.toList()` currently builds an `ArrayList` in OpenJDK 21, but
  its public contract does not promise that class or mutability.

- `toUnmodifiableSet` deduplicates. `toUnmodifiableMap` needs a merge function
  when duplicate keys are valid input; otherwise they cause
  `IllegalStateException`.

- A parallel stream does not make its result safe for concurrent mutation.
  An unmodifiable container also does not make mutable elements thread-safe.

### Mastery check

- Which result changes when the backing array changes? Which is a snapshot?

- How do you preserve null elements or insertion order while preventing callers
  from modifying a returned container?

- Why is replacing `collect(toList())` with `toList()` potentially a behavior change?

---

## §D Create dates, instants, and durations

- **API:** [java.time](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/package-summary.html).

### Choose the meaning first

| Type | Represents | Typical construction |
|---|---|---|
| `LocalDate` | Calendar date; no time or zone | `LocalDate.of(2026, 10, 23)` |
| `Instant` | Point on the timeline | `Instant.parse("2026-10-23T04:45:00Z")` |
| `Duration` | Elapsed seconds and nanoseconds | `Duration.ofMinutes(5)` |
| `Period` | Calendar years, months, days | `Period.ofMonths(1)` |

- These values are immutable and thread-safe. Operations return values rather
  than changing the original object.

- `of` constructs from components; `parse` reads text; `from` extracts information
  the source already supplies. `from` cannot infer missing time-zone context.

### `now`: make the clock and zone deliberate

```java
Clock clock = Clock.fixed(
    Instant.parse("2026-10-22T20:00:00Z"),
    ZoneId.of("Asia/Kolkata"));

LocalDate businessDate = LocalDate.now(clock);
Instant observed = Instant.now(clock);

System.out.println(businessDate); // 2026-10-23
System.out.println(observed);     // 2026-10-22T20:00:00Z
```

- `LocalDate.now()` uses the machine's default time zone. That may differ from
  the zone defining your business date.

- `LocalDate.now(zone)` chooses the zone. `LocalDate.now(clock)` also lets tests
  control time, including observations around midnight.

- An instant is the same timeline point regardless of display zone. A
  `LocalDate` does not retain the zone used to derive it.

- `Clock.fixed` never advances. To test a later observation, supply another
  fixed clock or use `Clock.offset(clock, duration)`.

### `from`: extraction requires enough information

```java
OffsetDateTime received = OffsetDateTime.parse("2026-10-23T10:15:00+05:30");

LocalDate date = LocalDate.from(received);
Instant instant = Instant.from(received);

System.out.println(date);    // 2026-10-23
System.out.println(instant); // 2026-10-23T04:45:00Z

// LocalDate.from(instant); // DateTimeException: an Instant has no local date

// Supply the zone to derive one:
LocalDate localDate = LocalDate.ofInstant(instant, ZoneId.of("Asia/Kolkata")); // Java 9
System.out.println(localDate); // 2026-10-23
```

- `Instant.from(localDateTime)` also fails: a local timestamp has no offset.
  Resolve it with a deliberate zone or offset first.

- A local time in a region with daylight saving can be ambiguous or invalid.
  Establish the required gap/overlap policy when scheduling in that region.

### Components and units are part of the contract

```java
LocalDate leapDay = LocalDate.of(2024, Month.FEBRUARY, 29);
Instant epoch = Instant.ofEpochMilli(0);
Duration timeout = Duration.ofMinutes(5);
Duration parsed = Duration.parse("PT5M");

System.out.println(leapDay);               // 2024-02-29
System.out.println(epoch);                 // 1970-01-01T00:00:00Z
System.out.println(timeout.equals(parsed)); // true

// LocalDate.of(2023, 2, 29);           // DateTimeException: invalid date
// LocalDate.of(2026, 0, 15);           // DateTimeException: months are 1–12
// Duration.of(1, ChronoUnit.MONTHS);   // UnsupportedTemporalTypeException
```

- Match epoch units: `ofEpochSecond` expects seconds; `ofEpochMilli` expects
  milliseconds. The wrong factory can produce a valid but wildly wrong date.

- `Duration.parse("PT1M")` means one minute. A calendar month is
  `Period.parse("P1M")`.

- Negative durations are valid. A timeout requiring a positive value needs
  an additional domain check.

### `Duration` versus `Period`: one day can mean two things

```java
ZonedDateTime start = ZonedDateTime.of(2024, 3, 30, 12, 0, 0, 0,
    ZoneId.of("Europe/Paris"));

ZonedDateTime calendarDay = start.plus(Period.ofDays(1));
ZonedDateTime elapsedDay = start.plus(Duration.ofDays(1));

System.out.println(calendarDay.getHour()); // 12 — same local hour next day
System.out.println(elapsedDay.getHour());  // 13 — exactly 24 hours later
System.out.println(Duration.between(start, calendarDay).toHours()); // 23
```

- A duration day is exactly 86,400 seconds. A period day follows the calendar
  and zone rules when applied to a zoned date-time.

- Use a `Period` for calendar schedules; use a `Duration` for elapsed amounts.
  `Duration.from(period)` is not a general calendar-to-elapsed conversion.

- `Duration.between` needs temporals that support a seconds-based difference.
  For two `LocalDate` values, use `Period.between` or `ChronoUnit.DAYS.between`
  according to the required result.

### Mastery check

- Why does `Instant.from(offsetDateTime)` succeed while
  `Instant.from(localDateTime)` fails?

- Which construction gives repeatable date-boundary tests?

- When should a daily schedule preserve the local hour rather than 24 elapsed hours?

---

## §E Wrappers, numbers, and Optional

- **API:** [Integer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html),
  [BigDecimal](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html),
  and [Optional](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Optional.html).

### `Integer.valueOf`: numeric equality is not object identity

```java
Integer a = Integer.valueOf(127);
Integer b = Integer.valueOf(127);
Integer c = Integer.valueOf(1000);
Integer d = Integer.valueOf(1000);

System.out.println(a == b);      // true: this range is cached
System.out.println(c.equals(d)); // true: equal numeric values
// c == d compares identity; caching outside the guaranteed range may vary.
```

- `Integer.valueOf(int)` arrived in Java 5. It guarantees cached instances for
  −128 through 127 and may cache additional values.

- Use `equals`, or `Objects.equals` when nulls are valid. Do not make numeric
  equality or synchronization depend on wrapper identity.

- Prefer primitive values when you do not need an object. Boxing does not turn
  a shared `count++` into an atomic operation.

| Primitive factory | Reuse to remember |
|---|---|
| `Boolean.valueOf(boolean)` | Shared `TRUE` / `FALSE` |
| `Byte.valueOf(byte)` | All byte values cached |
| `Short.valueOf(short)`, `Long.valueOf(long)` | At least −128 through 127 cached |
| `Character.valueOf(char)` | At least the first 128 code units cached |
| `Float.valueOf(float)`, `Double.valueOf(double)` | No small-value cache in OpenJDK 21 |

- A `Character` represents one UTF-16 code unit; a supplementary Unicode code
  point needs two code units. Boxing a `char` does not change that boundary.

### Parsing is a separate contract

```java
Integer retries = Integer.valueOf("3");
Integer flags = Integer.valueOf("ff", 16);
Boolean typo = Boolean.valueOf("tru");

System.out.println(retries); // 3
System.out.println(flags);   // 255
System.out.println(typo);    // false — malformed boolean text is not rejected
```

| Text factory | Null input | Other important behavior |
|---|---|---|
| Integer / Byte / Short / Long `valueOf(String)` | `NumberFormatException` | Invalid or out-of-range numbers fail; whitespace is not trimmed |
| `Boolean.valueOf(String)` | Returns false | Only case-insensitive `"true"` produces true |
| Float / Double `valueOf(String)` | `NullPointerException` | NaN and infinity are valid; numeric overflow can become infinity |

- Use a primitive parser such as `Integer.parseInt` when you need a primitive
  result. Validate configuration tokens or finite-number requirements explicitly.

- A primitive factory cannot receive null, but passing a nullable wrapper can
  trigger a null-unboxing failure **before the factory runs**.

### `BigDecimal.valueOf`: keep decimal arithmetic decimal

```java
BigDecimal price = BigDecimal.valueOf(1999, 2); // unscaled value + scale
BigDecimal tenth = BigDecimal.valueOf(0.1);
BigDecimal roundedAlready = BigDecimal.valueOf(0.1 + 0.2);
BigDecimal exact = new BigDecimal("0.1").add(new BigDecimal("0.2"));

System.out.println(price);          // 19.99
System.out.println(tenth);          // 0.1
System.out.println(roundedAlready); // 0.30000000000000004
System.out.println(exact);          // 0.3
```

- `valueOf(unscaled, scale)` means `unscaled × 10^(-scale)`.

- `valueOf(double)` uses the double's decimal string representation. It differs
  from `new BigDecimal(double)`, which captures the exact binary floating-point
  value. Neither can recover precision already lost in earlier calculations.

- `equals` includes scale; `compareTo` compares numeric value:

```java
BigDecimal a = new BigDecimal("19.99");
BigDecimal b = new BigDecimal("19.990");

System.out.println(a.equals(b));        // false
System.out.println(a.compareTo(b) == 0); // true
```

- `BigInteger.valueOf(long)` has the same “convert before arithmetic” lesson:

```java
BigInteger wrong = BigInteger.valueOf(Long.MAX_VALUE * 2);
BigInteger correct = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2));

System.out.println(wrong);   // -2 — primitive multiplication overflowed first
System.out.println(correct); // 18446744073709551614
```

### `Optional`: choose whether null is a failure or an absent result

| Factory | Meaning |
|---|---|
| `Optional.of(value)` | Value must be non-null; otherwise throw `NullPointerException` |
| `Optional.ofNullable(value)` | Null becomes empty; a non-null value becomes present |
| `Optional.empty()` | Explicitly represent no result |

```java
Map<String, String> owners = Map.of("PAY-104", "Ana");
Optional<String> found = Optional.ofNullable(owners.get("PAY-104"));
Optional<String> missing = Optional.ofNullable(owners.get("PAY-999"));

System.out.println(found.orElse("UNASSIGNED"));   // Ana
System.out.println(missing.orElse("UNASSIGNED")); // UNASSIGNED

// Optional.of(owners.get("PAY-999")); // NullPointerException
```

- An Optional-returning method should return an `Optional`, including for
  absence. Returning null introduces a second absence mechanism.

- Wrapping does not protect argument evaluation. Start at the nullable boundary:

```java
String raw = null;

// Optional.ofNullable(raw.trim()); // NullPointerException before ofNullable
Optional<String> normalized = Optional.ofNullable(raw).map(String::trim);
System.out.println(normalized.isPresent()); // false
```

- `OptionalInt`, `OptionalLong`, and `OptionalDouble` store primitive payloads
  without a separate boxed value. A present optional can still allocate an object.

```java
Integer nullableCount = null;
OptionalInt count = nullableCount == null
    ? OptionalInt.empty()
    : OptionalInt.of(nullableCount);

System.out.println(count.orElse(0));             // 0
System.out.println(OptionalInt.of(0).isPresent()); // true — zero is a real value
// OptionalInt.of(nullableCount); // NullPointerException during unboxing
```

### Mastery check

- Why can `valueOf` save an allocation without making `==` a good equality rule?

- Which parsing factory silently maps a typo to false?

- Why must decimal conversion happen before binary arithmetic loses precision?

- Why does `ofNullable(raw.trim())` still fail when `raw` is null?

---

## §F Internals worth knowing

- Public contracts decide correctness. Hidden class names explain behavior and
  costs, but application code should depend on the public return type.

- An unmodifiable container can use compact storage because it does not reserve
  capacity for future edits. A live wrapper is cheap because it keeps delegating
  to storage someone else owns.

- Caching saves allocations, but factories do not all cache. Copying can allocate
  temporary buffers as well as the final result.

<details>
<summary>OpenJDK 21 storage and cache details</summary>

- These observations were checked against OpenJDK 21.0.11 sources. They are
  implementation details unless a public cache guarantee is explicitly stated.

| Factory/result | OpenJDK representation | What it saves or shares |
|---|---|---|
| `List.of()` | Shared `ImmutableCollections$ListN` | Reuses the empty result |
| `List.of` with one/two elements | `ImmutableCollections$List12` | Stores references in fields; no backing array |
| `List.of` with three or more | `ImmutableCollections$ListN` | Array storage without growth capacity |
| `Set.of` | `Set12` for one/two; `SetN` otherwise | Small fields or open-addressed storage, without per-element hash nodes |
| `Map.of` / `ofEntries` | `Map1` for one mapping; `MapN` otherwise | Key/value fields or one open-addressed array |
| `Arrays.asList(array)` | `Arrays$ArrayList` | Shares the supplied array; distinct from `java.util.ArrayList` |
| `Collections.unmodifiableList` | `UnmodifiableList` / `UnmodifiableRandomAccessList` | Delegates to the original list |
| `copyOf` | Eligible-input reuse or compact collection families | Can avoid copying a suitable existing immutable container |
| Ordinary reference-stream `toList()` | Null-allowing `ListN` | Array-backed result; representation is not an API promise |
| `Collections.nCopies` | `CopiesList` | Stores a count and one reference; constant retained container storage |
| `Integer.valueOf(int)` | `IntegerCache` lookup or wrapper construction | At least −128..127 cached by API contract |
| `BigInteger.valueOf(long)` | Small constants, otherwise sign/magnitude storage | OpenJDK reuses −16..16; range is not an API promise |
| `BigDecimal.valueOf` | Compact unscaled value or big-integer storage | Long-based overloads can reuse scale-zero 0..10 and selected scaled zeros |
| `Optional.empty()` | Shared empty `Optional` | No per-call empty wrapper allocation in this implementation |

- Fixed-arity collection overloads avoid a caller-created varargs array. Larger
  results can still need an internal array.

- Compact storage does not eliminate iterator overhead universally. An enhanced
  `for` over a list normally requests an iterator; indexed access has a different
  execution path.

- Object sizes and JIT allocation elimination depend on the runtime. Measure
  the actual workload before claiming a particular speed or byte saving.

- Source entry points: [ImmutableCollections.java](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/ImmutableCollections.java),
  [Collections.java](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/Collections.java),
  and [Integer.java](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/Integer.java).
  The branch can move; use the source archive matching the runtime under study.

</details>

### Share safely

- Publish completed container state through an established happens-before
  relationship, such as class initialization or a volatile reference.

- Concurrent reads of a stable unmodifiable container do not make mutable
  elements, keys, or values safe for concurrent mutation.

- Do not mutate equality/hash-relevant state of set elements or map keys.
  An unmodifiable container cannot protect those objects for you.

- Do not synchronize on value-based objects such as wrappers, optionals,
  date/time values, or `of`-factory collections. Their identity is not a suitable
  application-level coordination mechanism.

---

## §G Recall and quick choice

### Predict the result

```java
List<String> source = new ArrayList<>(List.of("A"));
List<String> view = Collections.unmodifiableList(source);
List<String> snapshot = List.copyOf(source);

source.add("B");
System.out.println(view);
System.out.println(snapshot);
System.out.println(Set.copyOf(Arrays.asList("A", "A")).size());
System.out.println(Optional.ofNullable(null).isPresent());
```

<details>
<summary>Answer</summary>

```text
[A, B]
[A]
1
false
```

- The view reads the current backing list; the snapshot retains its captured
  container state.

- `Set.copyOf` deduplicates. `ofNullable(null)` represents absence.

</details>

### Explain it aloud

- Explain fixed-size, unmodifiable view, shallow snapshot, and immutable value
  using one example of each.

- Choose a collection factory and state its null, duplicate, and ordering policy.

- Explain what changes when a mutable object is stored inside an unmodifiable list.

- Distinguish a clock observation, local date, timeline point, elapsed amount,
  and calendar amount.

- Explain why a factory can return the same object twice without breaking its contract.

### Practice

- [CollectionViewsAndIteration.java](lab/04-data-structures/CollectionViewsAndIteration.java)
  — backing views and allowed mutations.

- [ImmutableRecordSnapshot.java](lab/10-core-contracts/ImmutableRecordSnapshot.java)
  — shallow state and ownership boundaries.

- [ClockDrivenTime.java](lab/11-io-time/ClockDrivenTime.java)
  — deterministic time.

### Choose by requirement

| Requirement | Reach for |
|---|---|
| Small fixed collection of non-null values | `List.of`, `Set.of`, `Map.of` — Java 9 |
| More than ten literal map entries | `Map.ofEntries` — Java 9 |
| Resizable mutable copy | `new ArrayList<>(source)` |
| Fixed-size view that writes through to an array | `Arrays.asList(array)` |
| Read-only access to changing backing state | `Collections.unmodifiableList(source)` |
| Detached unmodifiable collection, with null rejection | `List.copyOf`, `Set.copyOf`, `Map.copyOf` — Java 10 |
| Detached list retaining nulls | `unmodifiableList(new ArrayList<>(source))` |
| Detached map retaining encounter order | `unmodifiableMap(new LinkedHashMap<>(source))` |
| Unmodifiable stream list allowing nulls | `stream.toList()` — Java 16 |
| Explicit mutable stream result | `collect(toCollection(ArrayList::new))` — Java 8 |
| Testable observation of current time | Inject `Clock`; call `now(clock)` |
| Exact elapsed time / calendar schedule | `Duration` / `Period` |
| Boxed primitive value | Wrapper `valueOf`; compare by value |
| Exact decimal amount | Decimal text or `BigDecimal.valueOf(unscaled, scale)` |
| Nullable lookup result | `Optional.ofNullable` |
| Required non-null optional payload | `Optional.of` |
