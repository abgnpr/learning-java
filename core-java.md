# Core Java Rapid-fire Kit

- Core Java Rapid-fire Kit
  - [§1 Bedrock — Java identity + OOP](#1-bedrock--java-identity--oop)
  - [§2 Collections](#2-collections)
  - [§3 Exceptions](#3-exceptions)
  - [§4 Java 8+ (know it cold)](#4-java-8-know-it-cold)
  - [§5 Threads + JVM](#5-threads--jvm)
  - [§6 Trap wall (the code-output classics, spoken)](#6-trap-wall-the-code-output-classics-spoken)
  - [§7 Enterprise — Spring / JDBC / REST / integration](#7-enterprise--spring--jdbc--rest--integration)
  - [§8 Kafka — every answer anchored in IMPS ⚓](#8-kafka--every-answer-anchored-in-imps-)
  - [§9 SDLC + testing](#9-sdlc--testing)
  - [§10 Honesty guardrails (truth law)](#10-honesty-guardrails-truth-law)
  - [Rep scorecard — 🟢 only after a blind aloud rep](#rep-scorecard---only-after-a-blind-aloud-rep)

**Why this exists:** core Java is where a technical round opens —
theory, code-output, Java 8+, threads/JVM, enterprise, plus
SDLC/testing. **The flagship Java answer is the IMPS switch** (solo,
NPCI-certified) — abstract answers get stronger with "…and in my
IMPS switch, …". Anchors marked ⚓.

**How to drill:** aloud, blind, one section per sitting (`/drill`
rapid-fire). Answers are sized for SPEAKING — say the 1–3 lines,
then stop talking. Explain-level depth; if pressed deeper, anchor to
IMPS code; never bluff. 🟢 per section only after a blind aloud rep
— reading is not loading.

**⚓ The verified IMPS stack (from build files):** Spring Boot
**2.7.10** receivers · Kafka clients **3.x** · Oracle via
**ojdbc8** · Redis via **Jedis 3.7** · **Gradle** multi-project
build (NOT Maven) · JAXB + XML digital signatures for NPCI · RMI to
CBS-interface and RRN-server. ⚠️ JDK version is NOT pinned in
Gradle — check the deployment JDK before an interview ("which Java
version?" is a certainty).

---

## §1 Bedrock — Java identity + OOP

- **Why is Java platform-independent?** — `javac` compiles to
  bytecode; any JVM runs it. "Write once, run anywhere." Follow-up
  trap: the JVM itself IS platform-specific.
- **JDK vs JRE vs JVM?**
  - JVM — executes bytecode
  - JRE — JVM + libraries (enough to *run*)
  - JDK — JRE + compiler/tools (enough to *develop*)
- **Features of Java?**
  - object-oriented
  - platform-independent
  - robust (GC + exceptions)
  - secure
  - multithreaded
  - portable
  - high-performance via JIT — *Just-In-Time compiler: converts hot
    bytecode to native machine code at runtime, instead of pure
    interpretation. Bridges the "write once run anywhere" cost. Why:
    pure interpretation was too slow, pure native compilation broke
    portability — JIT lets the JVM ship bytecode everywhere, then
    earn back native speed locally.*
- **Is Java purely object-oriented?** — not strictly: primitives
  (`int`, `char`…) aren't objects; wrappers bridge the gap.
- **Why is `main` public static void?**
  - `static` — JVM calls it without creating an object
  - `public` — so JVM can reach it
  - `void` — returns nothing
  - `main` can be overloaded, but JVM only calls the standard one
- **Pass-by-value or reference?** — always pass-by-value. For
  objects, the *reference* is copied: you can mutate the object,
  you can't rebind the caller's variable.
- **Access modifiers, narrow → wide?** — `private` → default
  (package) → `protected` (package + subclasses) → `public`.

  |Modifier|Class|Package|Subclass (diff pkg)|World|
  |---|---|---|---|---|
  |`private`|✅|❌|❌|❌|
  |default (none)|✅|✅|❌|❌|
  |`protected`|✅|✅|✅|❌|
  |`public`|✅|✅|✅|✅|

  - applies to: fields, methods, constructors, nested classes
  - top-level classes/interfaces: only `public` or default (no
    `private`/`protected` — nothing outside a package could reach
    them anyway)
  - local variables: no modifiers at all — scope is the enclosing
    block
- **`static`?**
  - belongs to the class, shared by all instances
  - static methods can't touch `this` or instance members.
- **`final`?**
  - variable: assign once
  - method: no override
  - class: no subclass (e.g. `String`)
- **Four OOP pillars?**
  - encapsulation (data behind methods)
  - inheritance (is-a reuse)
  - polymorphism (one call, many forms)
  - abstraction (expose what, hide how)
- **Abstraction vs encapsulation?** — abstraction hides complexity
  behind an interface (design level); encapsulation hides data
  behind access control (implementation level).
- **Interface vs abstract class?**
  - interface — pure contract: no state, a class implements many
  - abstract class — partial implementation: fields +
    constructors, single inheritance
  - since Java 8 interfaces may carry `default`/`static` methods
- **Why no multiple class inheritance?** — the diamond problem:
  `B` and `C` both extend `A`; `D` extends both. `D` now inherits
  `A`'s *state* through two paths — one copy of the fields or two?
  whose constructor runs, how many times? Ambiguous, so Java allows
  one superclass only.
  - real culprit is state (fields + constructors), which only
    classes carry
  - interfaces (pre-8) were safe: no fields, no constructors, no
    method bodies — identical abstract signatures just merge
  - Java 8 default methods added *behavior*, so a clash is possible
    again — compiler refuses to guess, forces an explicit override

  ```java
  interface B { default String hi() { return "B"; } }
  interface C { default String hi() { return "C"; } }

  class D implements B, C {
      public String hi() { return B.super.hi(); } // must pick one
  }
  ```

- **Overloading vs overriding?**
  - overload: same name, different parameters, compile-time
  - override: subclass redefines, runtime dispatch; can't reduce
    visibility or throw broader checked exceptions; covariant
    return allowed
- **Can you override static / private / final methods?** — no.
  Static methods are *hidden*, not overridden (resolved by
  reference type).
- **Constructor rules?**
  - no return type
  - compiler supplies a default only if you wrote none
  - first statement is always a constructor call — explicit
    `this()`/`super()`, or an implicit `super()` inserted silently
    if you wrote neither
  - abstract classes DO have constructors — you just can't `new`
    them
- **Interface variables/methods are implicitly…?** (MCQ staple)
  - variables: `public static final`
  - methods: `public abstract` (pre-default-methods)
- **Marker interface?** — empty interface that tags behavior:
  `Serializable`, `Cloneable`. Modern equivalent: annotations.
- **Methods every object has?** — from `Object`:

  |Method|Purpose|
  |---|---|
  |`equals`|value/reference comparison|
  |`hashCode`|bucket key for hash collections|
  |`toString`|string representation|
  |`getClass`|runtime class/reflection|
  |`clone`|shallow copy (needs `Cloneable`)|
  |`wait`|release lock, pause until notified|
  |`notify`/`notifyAll`|wake one / all waiting threads|
  |`finalize`|pre-GC cleanup hook (deprecated)|

  `wait`/`notify`/`notifyAll` = thread coordination, inherited by
  every object for free; must hold the object's lock
  (`synchronized`) to call, else `IllegalMonitorStateException`.
  Modern code prefers `java.util.concurrent` (`BlockingQueue`,
  `ExecutorService`).
- **Why is String immutable?**
  - pool sharing (identical literals reuse one object) is only safe
    if values can't change
  - security (paths/credentials passed around)
  - thread-safety for free
  - hashCode cached → fast map keys
- **String vs StringBuilder vs StringBuffer?**
  - String — immutable
  - StringBuilder — mutable + fast, not thread-safe
  - StringBuffer — synchronized legacy twin
  - concatenation in a loop → StringBuilder
- **String pool?**
  - literals are interned and shared (in heap since Java 7)
  - `new String("a")` forces a separate object
  - `intern()` returns the pooled one
- **equals/hashCode contract?** — equal objects MUST have equal
  hashCodes (converse not required). Override both together, or the
  object goes missing inside HashMap/HashSet.
- **`==` vs `equals`?** — `==` compares references (or primitive
  values); `equals` compares content *if overridden* — default
  `Object.equals` IS `==`.
- **Wrapper classes / autoboxing?** — primitives ↔ objects
  automatically since Java 5; watch the Integer cache (−128…127)
  and unboxing-null NPEs (§6).

  ```java
  Integer a = 100, b = 100;
  System.out.println(a == b);   // true  — both hit the cache
  Integer x = 200, y = 200;
  System.out.println(x == y);   // false — outside −128..127, new objects

  Integer n = null;
  int m = n;                    // NPE — unboxing null
  ```

- **How do you write an immutable class?** (String is the reference
  example.)
  - `final` class
  - private `final` fields
  - no setters
  - initialize in constructor
  - defensive copies of mutable fields — don't store/return the
    caller's mutable reference; copy in, copy out — else they
    mutate your "immutable" object from outside

  ```java
  public final class Range {
      private final List<Integer> values;

      public Range(List<Integer> values) {
          this.values = new ArrayList<>(values);   // copy IN
      }

      public List<Integer> getValues() {
          return new ArrayList<>(values);          // copy OUT
      }
  }
  ```

- **Serialization?** — object → byte stream (`Serializable` marker)
  - `transient` — skips a field
  - `serialVersionUID` — guards class-version mismatch
  - static fields — never serialized

## §2 Collections

- **Framework shape?** — `Collection` → List / Set / Queue; `Map`
  is separate (key→value). Follow-up trap: `Map` does NOT extend
  `Collection` — panel favorite wrong-answer bait.

  ```text
  Iterable
   └─ Collection
        ├─ List
        │    ├─ ArrayList
        │    ├─ LinkedList   (also Deque, below — dual branch)
        │    └─ Vector
        │
        ├─ Set
        │    ├─ HashSet
        │    ├─ LinkedHashSet
        │    └─ SortedSet
        │         └─ NavigableSet
        │              └─ TreeSet
        │
        └─ Queue
             ├─ PriorityQueue
             └─ Deque
                  ├─ ArrayDeque
                  └─ LinkedList   (also List, above — dual branch)

  Map   (separate — not a Collection)
   ├─ HashMap
   ├─ LinkedHashMap
   └─ SortedMap
        └─ NavigableMap
             └─ TreeMap
  ```

  Why separate: `Map` holds pairs, not single elements — doesn't
  fit `Collection`'s single-element contract.

  Gotcha: `LinkedList` implements both `List` and `Deque` — the
  only class sitting in two branches at once.

- **List vs Set vs Map?**
  - List: ordered, duplicates OK
  - Set: unique elements
  - Map: unique keys → values
  - follow-up — *how does Set enforce uniqueness?* HashSet checks
    `hashCode` then `equals` on add; TreeSet uses
    `compareTo`/`compare` == 0. Duplicate → add returns false.

- **Array vs ArrayList?** *(entry classic)*
  - array: fixed size, primitives OK, `length` field
  - ArrayList: resizable, objects only (autoboxing for
    primitives), `size()` method, rich API

- **Collection vs Collections?** *(trick classic)* —
  `Collection` = root interface; `Collections` = utility class of
  statics (`sort`, `reverse`, `unmodifiableList`,
  `synchronizedList`).

- **ArrayList vs LinkedList?**
  - ArrayList: dynamic array, O(1) index reads, grows ~1.5×
  - LinkedList: doubly-linked, cheap end-ops, O(n) access
  - default choice: ArrayList

    | Op | ArrayList | LinkedList |
    | --- | --- | --- |
    | `get(i)` | O(1) | O(n) |
    | add/remove at end | O(1)* | O(1) |
    | add/remove in middle | O(n) shift | O(1)** |
    | memory per element | value only | value + 2 links |

  \* amortized — resize copies occasionally.

  \*\* only *at the iterator's position* — reaching it is O(n),
  the trap in "LinkedList inserts are O(1)".
  In practice ArrayList wins almost everywhere — contiguous
  memory, CPU-cache friendly.

- **ArrayList vs Vector?**
  - Vector: legacy synchronized version, grows 2×
  - today: use ArrayList, or a concurrent collection if threads
    are involved

- **How does HashMap work?** *(THE question)*
  - key's `hashCode` → bucket index
  - collisions chain into a linked list
  - a bucket past 8 entries treeifies to a red-black tree (Java 8)
  - default capacity 16, load factor 0.75, resize doubles
  - one null key allowed
  - lookup: hash finds the bucket, `equals` picks the entry — why
    both must be overridden together

  **The mental picture:** a HashMap is just an array of 16 slots
  ("buckets"). Each slot can hold a small chain of entries. The
  trick: the key's hash *is* the array index — that's why lookup
  is O(1), no searching.

  ```text
  index = hash(key) & 15        // capacity 16 → mask 0..15

  [0]  → null
  [4]  → ("amit",500) → ("zoya",900)   ← collision chain
  [9]  → ("ravi",200)
  [15] → null
  ```

  **put("amit", 500), step by step:**
  1. call `"amit".hashCode()` → some int, e.g. 93029210
  2. squash it into an index 0–15 → say bucket 4
  3. bucket empty → drop the entry there; done
  4. bucket occupied → *collision*: walk the chain — if some key
     `equals("amit")`, overwrite its value; else append a node
  5. chain grows past 8 → convert to red-black tree, so a bad
     bucket degrades to O(log n), not O(n)
  6. map more than ¾ full (16 × 0.75 = 12 entries) → *resize*:
     double the array to 32, re-place every entry

  **get("amit") — and why equals matters:**
  - same index math → jump straight to bucket 4
  - but two *different* keys can share a bucket (finite slots,
    infinite keys) — so hash alone can't identify the entry
  - walk the chain, `equals` each key until it matches
  - division of labor: **hashCode = which bucket, equals = which
    entry in it.** Override only one → equal keys land in
    different buckets → `get` misses. That's the contract.

  **The numbers decoded:**
  - capacity 16 — starting array length
  - load factor 0.75 — resize trigger, not a limit: grow at 75%
    full, *before* chains get long
  - doubling — keeps capacity a power of 2, so index = cheap
    bit-mask of the hash
  - one null key — its hash is defined as 0, lives in bucket 0

  **The logic behind each design choice:**
  - *why hash into an array at all* — an array is the only
    structure where "position → element" costs O(1); hashing
    turns an arbitrary key into a position. Every other choice
    below exists to protect this one property.
  - *why chaining for collisions* — slots are finite, keys
    aren't, so two keys sharing a slot is unavoidable; a chain
    absorbs that without disturbing neighboring buckets, and
    deletes stay simple (unlike open addressing, which shifts
    the problem to probing).
  - *why treeify, and why exactly 8* — with a decent hashCode,
    chain lengths follow a Poisson curve: a chain of 8 is a
    ~1-in-16-million event. So 8 means something is *wrong* —
    lousy hashCode or a hash-flooding attack — and only then is
    the red-black tree's overhead worth paying: worst case drops
    O(n) → O(log n). Rarity is the argument: trees cost memory,
    so reserve them for the pathological case.
  - *why load factor 0.75* — pure space-time bargain. Resize too
    late → long chains, slow lookups; too early → half-empty
    array, wasted memory. At 75% the *expected* chain length
    stays under ~1, so O(1) still holds statistically. 0.5 wastes
    RAM for little gain; 1.0 guarantees collisions.
  - *why capacity is a power of 2* — index becomes
    `hash & (n-1)`, a one-cycle bit-mask instead of a slow `%`.
    Cost: only low bits get used, so HashMap XORs high bits down
    (`h ^ h >>> 16`) to keep spread.
  - *why resize doubles* — doubling preserves power-of-2 *and*
    makes redistribution cheap: each entry either stays put or
    moves exactly +oldCapacity, decided by one bit. Growing
    geometrically also amortizes resize cost to O(1) per insert.
  - *why one null key allowed* — a convenience ruling, not
    physics: single-threaded code can disambiguate null with
    `containsKey`, so banning it (Hashtable-style) buys nothing;
    null can't produce a hashCode, so it's hard-wired to bucket 0.
  - *why not synchronized* — most maps live on one thread; making
    everyone pay lock cost for the few who share (Hashtable's
    mistake) is backwards. Thread safety is opt-in:
    ConcurrentHashMap.

  *One-liner:* array of buckets; hashCode picks the bucket,
  equals picks the entry; chains treeify past 8; resize at 75%.

- **HashMap vs Hashtable vs ConcurrentHashMap?**
  - Hashtable: legacy, fully synchronized, no null keys or values
  - ConcurrentHashMap: fine-grained locking (CAS + per-bin sync in
    Java 8), lock-free reads, no null keys or values
  - HashMap: fastest, single-threaded, one null key + unlimited
    null values allowed

    |Type|Null keys|Null values|
    |---|---|---|
    |`HashMap`|1|many|
    |`Hashtable`|0|0|
    |`ConcurrentHashMap`|0|0|

  **CAS + per-bin sync, explained** *(pre-Java-8: coarse segment
  locks — recognize the term if an older panelist uses it)*:
  - empty bin → insert via CAS (lock-free compare-and-swap,
    no thread blocks)
  - collision → `synchronized` on that bin's head node only,
    never the whole map
  - reads never block either way

  **Why no nulls (Hashtable + ConcurrentHashMap):**
  - `get(k) == null` is ambiguous — absent, or mapped to null?
  - plain HashMap disambiguates with a follow-up `containsKey(k)`
  - a concurrent map can't — another thread may mutate between
    the two calls
  - banning null makes `get() == null` always mean absent

- **HashSet internals?** — a HashMap in disguise: elements are the
  keys, value is a dummy constant.

- **TreeMap / TreeSet?** — red-black tree, sorted order, O(log n),
  needs `Comparable` or a `Comparator`; no null key.

- **LinkedHashMap?** — HashMap + insertion order; with access-order
  plus `removeEldestEntry` it's a ready-made LRU cache.

- **Fail-fast vs fail-safe iterators?**
  - fail-fast: throws `ConcurrentModificationException` if the
    collection changes mid-iteration (ArrayList, HashMap)
  - fail-safe: iterates a snapshot/weakly-consistent view
    (CopyOnWriteArrayList, ConcurrentHashMap)
  - mechanism: collection keeps a `modCount`; iterator remembers
    it at creation, checks every `next()` — mismatch → throw
  - follow-up — *so how DO you remove while iterating?*
    `iterator.remove()` (updates both counts) or
    `list.removeIf(pred)`; never `list.remove()` inside for-each

- **Iterator vs ListIterator?** *(entry classic)*
  - Iterator — forward only, any Collection,
    `hasNext`/`next`/`remove`
  - ListIterator — Lists only, both directions, plus
    `add`/`set`/`previous`/index queries

- **Read-only collections?**
  - `Collections.unmodifiableList(list)` — wrapper; writes throw
    `UnsupportedOperationException`, but the *backing* list can
    still change underneath
  - `List.of(...)` / `Map.of(...)` (Java 9) — truly immutable
    from birth, no nulls allowed

- **Comparable vs Comparator?** — Comparable: the class's own
  natural order (`compareTo`); Comparator: external strategy,
  many orderings (`Comparator.comparing(...)` since Java 8).

  **Comparable** (`java.lang`):
  - the class itself implements `int compareTo(T other)` —
    "this is how *I* sort"
  - one natural order per class, baked in — String alphabetical,
    Integer numeric, Date chronological
  - `Collections.sort(list)` and `TreeMap`/`TreeSet` use it
    automatically
  - return contract: negative = this before other, 0 = equal,
    positive = this after

  **Comparator** (`java.util`):
  - separate object implements `int compare(T a, T b)` —
    sorting logic lives *outside* the class
  - use when: can't edit the class (third-party), or need
    multiple orders (by name, by salary, by date...)
  - passed in explicitly: `list.sort(cmp)`,
    `new TreeMap<>(cmp)`, `Collections.sort(list, cmp)`

  **Java 8 shorthand:**
  - `Comparator.comparing(Emp::getSalary)` — build from a getter
  - chain: `.thenComparing(Emp::getName)` — tie-breaker
  - flip: `.reversed()`; nulls: `Comparator.nullsFirst(...)`

    ```java
    class Emp implements Comparable<Emp> {
        public int compareTo(Emp o) {          // natural: by id
            return Integer.compare(id, o.id);
        }
    }
    emps.sort(Comparator.comparing(Emp::getSalary)
                        .reversed());          // external: by salary
    ```

  *One-liner:* Comparable = the class sorts itself, one way.
  Comparator = you hand in a sort rule, as many ways as needed.
- **Queue/Deque quick hits?** — `offer`/`poll`/`peek`; ArrayDeque
  beats Stack/LinkedList for stacks and queues; PriorityQueue =
  binary heap, ordered by priority not insertion.

  **Queue — two method families** (same action, different failure):
  - throwing: `add` / `remove` / `element` — exception when
    full/empty
  - special-value: `offer` / `poll` / `peek` — return
    `false`/`null` instead; prefer these

  **Deque** (double-ended queue, `java.util.Deque`):
  - insert/remove at *both* ends: `addFirst`/`addLast`,
    `pollFirst`/`pollLast`
  - one structure, two roles — FIFO queue (add last, poll first)
    or LIFO stack (`push`/`pop` at the head)

  **Why ArrayDeque wins** (their flaw → its counter):
  - vs `Stack`: legacy, extends Vector — every call pays
    synchronization even single-threaded; ArrayDeque carries no
    locks, no such tax
  - vs `LinkedList`: a Node object (value + 2 pointers) per
    element — scattered memory, GC load; ArrayDeque is one
    resizable circular array — contiguous, cache-friendly,
    nothing extra to collect
  - rule of thumb: need a stack or queue → `ArrayDeque`,
    unless nulls required (it forbids them)

  **PriorityQueue:**
  - binary heap in an array; `poll` always gives the *smallest*
    element (min-heap by default)
  - needs `Comparable` elements or a `Comparator` at construction;
    flip to max-heap via `Comparator.reverseOrder()`
  - `offer`/`poll` O(log n), `peek` O(1)
  - trap: iteration order is *not* sorted — only the head is
    guaranteed; no nulls

  *One-liner:* Queue = FIFO with polite (`offer`/`poll`) and rude
  (`add`/`remove`) methods. Deque = both ends, replaces Stack.
  PriorityQueue = heap, head is always the min.
- **Generics?** — compile-time type safety; erased at runtime
  (type erasure) — hence no `new T[]`, no `instanceof List<String>`.
  - bounds: `<T extends Number>` — restrict what T can be
  - wildcards *(depth reserve — drill last)*: `? extends T` =
    read-only producer; `? super T` = write-into consumer —
    **PECS** (Producer Extends, Consumer Super)
  - trap: `List<Dog>` is NOT a `List<Animal>` — generics are
    invariant; that's what wildcards are for

## §3 Exceptions

- **What is an exception?** *(the opener — panels start at
  definitions)* — an event that disrupts normal program flow; in
  Java it's an *object* (a Throwable subtype) thrown at the point
  of failure and caught somewhere up the stack. Handling it keeps
  the program running instead of dying.
- **Hierarchy?** — `Throwable` → `Error` + `Exception`; `Exception`
  → checked + `RuntimeException` (unchecked).

  ```text
  Throwable
   ├─ Error                 (JVM-level — don't catch)
   │    ├─ OutOfMemoryError
   │    ├─ StackOverflowError
   │    └─ NoClassDefFoundError
   └─ Exception
        ├─ IOException, SQLException,      (checked —
        │  ClassNotFoundException           compiler enforced)
        └─ RuntimeException                (unchecked)
             ├─ NullPointerException
             ├─ ArrayIndexOutOfBoundsException
             ├─ ArithmeticException
             ├─ ClassCastException
             ├─ NumberFormatException
             └─ IllegalArgumentException
  ```

  Trap: checked vs unchecked is decided by *inheritance* (extends
  RuntimeException or not) — no keyword involved.
- **Checked vs unchecked?**
  - checked — compiler forces handle-or-declare
    - IOException, SQLException, ClassNotFoundException
  - unchecked — RuntimeException family, no compiler demand
    - NPE, ArrayIndexOutOfBounds, ArithmeticException,
      ClassCastException, NumberFormatException,
      IllegalArgumentException

  **Why the split (the design argument):**
  - checked = *external* failures a correct program can still hit —
    file missing, network down, DB gone. Recoverable, so the
    compiler demands a plan: handle or declare.
  - unchecked = *programming bugs* — null, bad index, bad cast.
    Possible on nearly every line; forcing declarations everywhere
    would drown the code. The fix is fixing the bug, not catching.
  - rule of thumb: caller can reasonably recover → checked;
    caller's code is simply wrong → unchecked.
- **Exception vs Error?** *(the famous one)* — both extend
  `Throwable`, then split by *who can fix it*:
  - Exception: application-level trouble, often recoverable —
    catch it, handle it, move on (IOException, SQLException, NPE)
  - Error: JVM-level collapse, not recoverable by code —
    OutOfMemoryError (heap exhausted), StackOverflowError
    (runaway recursion). All Errors are unchecked.
  - spoken contrast: *"Exceptions are my program's problems;
    Errors are the JVM's problems."*
- **Can you catch Error or Throwable?** — legal syntax, bad idea:
  after OOM/StackOverflow the JVM is in no state to "handle"
  anything. Catch `Exception` at boundaries, let Errors fall.
- **`throw` vs `throws`?** — `throw` raises one; `throws` declares
  a method may.

  ```java
  void read() throws IOException {     // throws — declares
      throw new IOException("boom");   // throw  — raises
  }
  ```

- **`final` vs `finally` vs `finalize`?** *(classic trio)*
  - `final` — keyword: assign-once var / no-override method /
    no-subclass class
  - `finally` — block: cleanup that always runs after try
  - `finalize` — method: pre-GC hook, deprecated, never rely on it
- **Does finally always run?**
  - yes — even after a `return` in try/catch
  - only escapes: `System.exit()`, JVM crash
  - trap: a `return` *inside* finally overrides the try's return
    AND swallows its exception (§6 trap)
- **Try / catch / finally — legal combos?** *(MCQ staple)*
  - legal: try+catch, try+finally, try+catch+finally,
    try-with-resources alone
  - a bare `try {}` with none of them → compile error
  - nested try is legal — inner catch gets first shot, unhandled
    ones climb to the outer
- **try-with-resources?** — Java 7; anything `AutoCloseable` is
  closed automatically, reverse order, even on exception.

  ```java
  try (Connection c = ds.getConnection();
       PreparedStatement ps = c.prepareStatement(sql)) {
      ...
  }            // ps closes first, then c — always
  ```

  - why: the old finally-close dance leaked on double faults and
    took 6 lines; this is leak-proof by construction
  - if body AND close both throw, body's exception wins; close's
    is attached as *suppressed* (`getSuppressed()`)
  - ⚓ natural JDBC answer: "our DB access closes statements and
    connections this way"
- **Custom exceptions?** — extend `Exception` (checked) or
  `RuntimeException` (unchecked); pass message via `super(msg)`.
  ⚓ IMPS maps failures to NPCI response codes (M2/M3/08/12…)
  rather than leaking stack traces to the network.

  ```java
  class TxnException extends RuntimeException {
      TxnException(String msg, Throwable cause) {
          super(msg, cause);           // message + chained cause
      }
  }
  ```

- **Exception propagation?** — uncaught → pops up the call stack
  frame by frame until a matching catch; none found → thread dies,
  default handler prints the stack trace. ⚓ in a switch that's
  never acceptable — the IMPS receivers catch at the boundary and
  map to a response code; a listener thread must not die mid-txn.
- **Exception chaining?** — wrap low-level cause in a domain
  exception, keep the trail: `throw new TxnException("debit leg
  failed", sqlEx)` — `getCause()` preserves forensics. ⚓ how a
  JDBC failure surfaces as one NPCI response code without losing
  the root cause in logs.
- **Multi-catch / catch order?** — `catch (A | B e)` since Java 7;
  subclass must be caught before superclass or it's a compile
  error ("unreachable").

  ```java
  try { ... }
  catch (FileNotFoundException e) { ... }  // subclass first
  catch (IOException e)           { ... }  // parent after
  catch (SQLException | JMSException e) { ... }  // multi-catch
  ```

- **Overriding + checked exceptions?** *(panel favorite, ties to
  §1)*
  - **the rule:** an override can only *shrink* the checked list.
    It may throw the same, a narrower (subclass), fewer, or none.
    It may **not** throw a broader checked exception or a brand-new
    one the parent never declared. Unchecked is unrestricted —
    throw any `RuntimeException`, declared or not.
  - **why (say this):** callers are compiled against the *parent's*
    signature, so they only handle what the parent's `throws` list
    promises. Polymorphism lets a `Base` reference hold a child
    instance — `Base b = new Child(); b.read();` — and the caller
    wrapped that in `try/catch(IOException)`. If `Child.read()`
    could throw a broader `Exception`, the caller would face a
    checked exception the compiler already blessed as impossible.
    So the compiler bans widening at the source. Narrowing is safe:
    a `FileNotFoundException` *is* an `IOException`, the existing
    catch still covers it. Unchecked is exempt because the compiler
    never forced callers to handle it in the first place.

  ```java
  class Base { void read() throws IOException {} }

  // ✅ same / narrower / dropped / unchecked
  class A extends Base { void read() throws IOException {} }
  class B extends Base { void read() throws FileNotFoundException {} }
  class C extends Base { void read() {} }
  class D extends Base { void read() throws IllegalStateException {} }

  // ❌ broader or new checked — won't compile
  class E extends Base { void read() throws Exception {} }
  class F extends Base { void read() throws SQLException {} }
  ```

  - trap line: *narrower-or-fewer OK, broader-or-new checked =
    compile error, unchecked = anything goes*
- **ClassNotFoundException vs NoClassDefFoundError?**
  - the Exception — checked; reflectively loading a missing class
    (`Class.forName("com.x.Missing")`)
  - the Error — a class that existed at compile time is gone at
    runtime (jar missing from classpath)
  - Memory hook: *Exception = asked by name, not found; Error =
    compiled against, vanished.*

  *One-liner for the section:* Throwable splits into Error (JVM,
  don't touch) and Exception; Exception splits into checked
  (external, plan required) and unchecked (bugs). `throw` raises,
  `throws` declares, finally always runs, try-with-resources
  closes for you.

## §4 Java 8+ (know it cold)

*Two-layer ammo. This section = the spoken rapid-fire; the
learn-from-zero layer + full version timeline live in
[java-versions.md](java-versions.md) — each bullet links its deep
dive. Drill here, study there.*

- **Java 8 headline features?** — lambdas, Stream API, functional
  interfaces, `default`/`static` interface methods, `Optional`,
  method references, new `java.time`, PermGen → Metaspace.

- **Functional interface?** — exactly one abstract method;
  `@FunctionalInterface`. Core four:
  - `Predicate` (T→boolean)
  - `Function` (T→R)
  - `Supplier` (()→T)
  - `Consumer` (T→void)
  *Chant: Predicate asks · Function morphs · Supplier gives ·
  Consumer takes.*
  [→ §A](java-versions.md#a-functional-interface--the-slot-a-lambda-fills)

- **Lambda?** — inline implementation of a functional interface:
  `(a, b) -> a + b`. Less ceremony than anonymous classes; types
  inferred from the target interface.
  [→ §B](java-versions.md#b-lambda--the-compact-syntax)

- **Effectively final?** *(the lambda gotcha)* — a lambda may
  capture a local variable only if it's assigned once and never
  reassigned; reassign → compile error. Fields exempt.
  [→ §B](java-versions.md#b-lambda--the-compact-syntax)

- **Method reference?** — `::` shorthand when the lambda just
  calls one method: `String::valueOf`, `list::add`,
  `ArrayList::new`.
  [→ §C](java-versions.md#c-method-reference--shorthand-for-a-one-call-lambda)

- **Stream?**
  - declarative pipeline
  - source
    → lazy intermediates (`map`, `filter`, `sorted`, `distinct`)
    → one terminal (`collect`, `forEach`, `reduce`, `count`)
  - nothing runs until the terminal
  - single-use  *(MCQ staple: reusing a consumed stream →
  IllegalStateException)*
  - never mutates the source

  ```text
  txns.stream() → filter → map → sorted   [all lazy — nothing runs]
                                  └─▶ collect()   [terminal fires it]
  ```

  [→ §D](java-versions.md#d-stream-api--the-payoff)

- **Stream vs Collection?** *(X-vs-Y staple)* — a Collection
  stores elements; a Stream computes over them: no storage, lazy,
  single-use, never mutates its source.

- **Intermediate vs terminal?**
  - intermediates — return a Stream, lazy
  - terminals — produce a result, fire the pipeline
  - spot-the-terminal trick: return type isn't a Stream → terminal

  [→ §D](java-versions.md#d-stream-api--the-payoff)

- **map vs flatMap?** — map: one-to-one transform; flatMap:
  one-to-many, flattened (`List<List<T>>` → elements).
  [→ §D](java-versions.md#d-stream-api--the-payoff)

- **Collectors?** — `collect` gathers the stream:
  - `toList`, `joining` — the plain gathers
  - `groupingBy` — = SQL GROUP BY
  - `partitioningBy` — always exactly two keys (true/false)
  - `toMap` — throws on duplicate key without a merge fn *(MCQ)*
  - `groupingBy` + `counting()` = THE interview combo:

  ```java
  Map<String, Long> perType = txns.stream().collect(
        Collectors.groupingBy(Txn::getType, Collectors.counting())
  );
  // {"ReqPay": 9421, "ReqChkTxn": 130, ...}
  ```

  [→ §E](java-versions.md#e-collectors--what-collect-uses)
- **Optional?** — a container that may hold a value; `orElse`,
  `ifPresent`, `orElseThrow`. Meant for return types — makes
  "maybe absent" explicit instead of returning null.
  [→ §F](java-versions.md#f-optional--maybe-absent-made-explicit)

- **orElse vs orElseGet?** *(MCQ trap)* — `orElse(expr)` evaluates
  its argument every time, present or not; `orElseGet(supplier)`
  runs only when empty. Costly fallback → `orElseGet`.
  [→ §F](java-versions.md#f-optional--maybe-absent-made-explicit)

- **of vs ofNullable?** *(MCQ trap)* — `Optional.of(null)` throws
  NPE immediately; `Optional.ofNullable(null)` → empty Optional.
  Memory hook: *`of` promises a value; `ofNullable` shrugs.*

- **Why default methods?** — evolve interfaces without breaking
  implementers (`Iterable.forEach`, `List.sort` were added that
  way). Clash of two defaults → diamond rules, §1.
  [→ §G](java-versions.md#g-default--static-methods-in-interfaces)

- **java.time vs old Date?**
  - new — LocalDate/LocalDateTime/ZonedDateTime: immutable,
    thread-safe
  - old — Date/Calendar/SimpleDateFormat: mutable, famously not thread-safe
  - trap: `d.plusDays(30)` unassigned does nothing

  [→ §H](java-versions.md#h-javatime--the-new-datetime-api)

- **Metaspace?** — Java 8 removed PermGen; class metadata moved to
  native memory (Metaspace). MCQ staple.

- **parallelStream?** — fork-join under the hood; only for
  CPU-heavy, stateless work on large data — never with shared
  mutable state.

  ```java
  // BROKEN — shared mutable state, racy, undercounts
  List<Integer> total = new ArrayList<>();
  nums.parallelStream().forEach(total::add);   // ArrayList isn't thread-safe

  // CORRECT — no shared state, each thread reduces independently
  int sum = nums.parallelStream().mapToInt(Integer::intValue).sum();
  ```

- **Version awareness (one phrase each):** LTS line = 8, 11, 17,
  21, 25. `var` (10) · records = immutable data carriers (16) ·
  sealed classes (17) · switch expressions (14) · virtual threads
  (21). String `switch` works since 7. Full timeline + snippets:
  [java-versions.md](java-versions.md); LTS memory hook:
  [→ §I](java-versions.md#i-the-lts-list--cheap-to-memorize-certain-to-be-asked);
  "which JDK did IMPS run?" answer:
  [→ the two answers](java-versions.md#the-two-answers-to-have-cold).

  *One-liner for the section:* Java 8 made behavior passable —
  lambda fills a one-method interface, `::` abbreviates it,
  streams pipe it, Optional boxes the maybe, defaults let
  interfaces grow; LTS chant 8-11-17-21-25.

## §5 Threads + JVM

- **What is a thread?** *(the opener)* — a single sequential flow
  of execution within a process; a process is an independent
  running program with its own memory, a thread is a lighter unit
  inside it — threads in one process share heap + static memory,
  each gets its own stack + program counter.

- **Ways to create a thread?**

  - extend `Thread` — burns the one superclass slot

  - implement `Runnable` — preferred, frees inheritance

  - `Callable` + `Future` — returns a value, can throw checked

  - submit any of them to an `ExecutorService` pool (production)

  ```java
  // 1. Extend Thread (least flexible)
  class Worker extends Thread {
      @Override
      public void run() {
          System.out.println("running");
      }
  }
  new Worker().start();


  // 2. Implement Runnable (preferred over extending Thread)
  class Task implements Runnable {
      @Override
      public void run() {
          System.out.println("running");
      }
  }
  new Thread(new Task()).start();

  // Lambda (Runnable is a functional interface)
  new Thread(() -> System.out.println("running")).start();


  // 3. Callable + Future (returns a value and can throw checked exceptions)
  Callable<Integer> job = () -> 42;

  FutureTask<Integer> future = new FutureTask<>(job);
  new Thread(future).start();

  int result = future.get();      // waits for completion


  // 4. ExecutorService (recommended for real applications)
  ExecutorService pool = Executors.newFixedThreadPool(4);

  pool.execute(new Task());           // fire-and-forget Runnable

  Future<Integer> future = pool.submit(job); // Callable -> Future
  int result = future.get();

  pool.shutdown();
  ```

  | Approach                  | Creates Thread?        | Returns Value?      | Recommended?            |
  | ------------------------- | ---------------------- | ------------------- | ----------------------- |
  | Extend `Thread`           | Yes                    | No                  | Rarely                  |
  | Implement `Runnable`      | Yes (via `new Thread`) | No                  | Yes                     |
  | `Callable` + `FutureTask` | Yes (via `new Thread`) | Yes                 | Occasionally            |
  | `ExecutorService`         | No (uses thread pool)  | Optional (`Future`) | ✅ Yes (production code) |


- **run() vs start()?** *(trap)* — `start()` spawns a new thread
  that calls `run()`; calling `run()` directly is just a method
  call on the current thread.

- **monitor?** — every Java object carries one implicitly: a lock +
  a wait-set. `synchronized` acquires the lock half;
  `wait`/`notify`/`notifyAll` operate on the wait-set half. All the
  "monitor" mentions below (BLOCKED state, `synchronized`,
  `wait`/`notify`) are this same one thing, not a dashboard/ops
  sense.

- **Thread states?** *(MCQ staple)*

  ```mermaid
    stateDiagram-v2
        direction LR
        [*] --> NEW
        NEW --> RUNNABLE : start()
        RUNNABLE --> TERMINATED : run() ends
        TERMINATED --> [*]

        RUNNABLE --> BLOCKED : lock busy
        BLOCKED --> RUNNABLE : acquired
        RUNNABLE --> WAITING : wait()/join()
        WAITING --> RUNNABLE : notify()
        RUNNABLE --> TIMED_WAITING : sleep(t)/wait(t)
        TIMED_WAITING --> RUNNABLE : timeout
  ```

  | State | Entered by | Leaves via |
  | --- | --- | --- |
  | NEW | constructed | `start()` |
  | RUNNABLE | `start()`, re-scheduled | runs / blocks / waits |
  | BLOCKED | contended `synchronized` | monitor acquired |
  | WAITING | `wait()`, `join()`, `park()` | `notify()`, target ends, unpark |
  | TIMED_WAITING | `sleep(t)`, `wait(t)`, `join(t)` | timeout **or** `notify()` |
  | TERMINATED | `run()` returns | — *(one-way door)* |

  - the confusable trio: BLOCKED = stuck *entering* synchronized
    (wants a monitor); WAITING = parked *indefinitely*
    (`wait()`/`join()`); TIMED_WAITING = parked *with a deadline*
    (`sleep(t)`, `wait(t)`)

  - `join()` = caller waits until that thread terminates

  - one-way doors: NEW and TERMINATED are visited once — a
    terminated thread can't `start()` again *(MCQ trap)*

- **synchronized?** — mutual exclusion: only one thread at a time
  can hold a given monitor, so the guarded code can't interleave.
  - **instance method** `synchronized void m()` — locks `this`.
    Two threads calling `m()` on the *same* instance serialize; on
    different instances they don't (different monitors).
  - **block** `synchronized(obj) { ... }` — locks whatever `obj` you
    pick, not necessarily `this`. Narrows the critical section and
    lets you choose the lock object explicitly.
  - **static method** `synchronized static void m()` — locks the
    `Class` object (`Foo.class`), not any instance. So a static
    synchronized method and an instance synchronized method on the
    *same* object do **not** exclude each other — two different
    monitors *(MCQ trap)*.
  - reentrant: a thread already holding the monitor can re-enter
    another synchronized method/block guarded by it without
    deadlocking itself.
  - also gives visibility: exiting a synchronized block flushes
    writes, entering re-reads from main memory (happens-before,
    same family as `volatile`).

- **synchronized vs Lock (ReentrantLock)?** *(X-vs-Y staple)*
  - `synchronized`: implicit, JVM releases it automatically even on
    exception — simpler, less error-prone
  - `ReentrantLock` (`java.util.concurrent.locks`): explicit
    `lock()`/`unlock()` in a try/finally; adds `tryLock` (timeout,
    no blocking forever), `lockInterruptibly`, fairness policy,
    multiple `Condition`s per lock (vs one wait-set per monitor)
  - rule of thumb: `synchronized` by default; reach for
    `ReentrantLock` only when you need those extras
- **volatile?** — visibility + ordering guarantee, NOT atomicity:
  `i++` on a volatile is still a race. Flags: volatile; counters:
  AtomicInteger (CAS, lock-free).
  - why visibility is even a problem: each core may cache a
    variable; without `volatile` another thread can spin on a
    stale cached copy forever. `volatile` forces every read/write
    through main memory and stops the compiler/CPU from reordering
    around it (happens-before edge) — but a whole statement like
    `i++` is still read-modify-write, three steps, so two threads
    can still interleave and lose an update.
- **wait vs sleep?** *(the famous one)*
  - `wait` — Object method; *releases* the monitor; must be inside
    synchronized (else IllegalMonitorStateException); woken by
    `notify`/`notifyAll`
  - `sleep` — Thread static method; *holds* any locks; wakes
    itself after the timeout
  - Memory hook: *wait lets go of the lock; sleep clutches it.*
- **notify vs notifyAll?** — `notify` wakes one arbitrary waiter
  (risky — might wake the wrong one); `notifyAll` wakes all to
  recompete for the lock — the safe default. (Object-method table:
  §1.)
- **Runnable vs Callable?**
  - Runnable — `void run()`, no checked throws
  - Callable — returns a value, may throw checked; retrieved via
    `Future.get()` (blocks)
  - CompletableFuture (8) — chains async steps without blocking
- **ExecutorService — thread pools?** *(THE question)*
  - a fixed set of worker threads pulls tasks off a shared queue,
    instead of spawning a new OS thread per task
  - types: `newFixedThreadPool(n)` (bounded, steady load),
    `newCachedThreadPool()` (grows/shrinks, bursty short tasks),
    `newSingleThreadExecutor()` (serial, one worker),
    `newScheduledThreadPool(n)` (delayed/periodic tasks)
  - `shutdown()` — stop accepting new tasks, finish the queue, then
    exit; `shutdownNow()` — interrupt running tasks, drop the queue,
    return what never ran

  **The mental picture:** a pool is a small crew of workers plus one
  shared in-tray (the task queue). Submitting a task drops it in the
  tray; whichever worker is free next picks it up. No worker means
  no task runs; too many workers means threads fighting over CPU
  and context-switch overhead.

  ```text
  submit(task) → [queue: t4 t5 t6] → worker1 (running t1)
                                      worker2 (running t2)
                                      worker3 (running t3)
  ```

  **`newFixedThreadPool(3)`, step by step, tasks t1..t6 submitted:**
  1. workers 1–3 pick up t1, t2, t3 immediately — pool is full
  2. t4, t5, t6 wait in the internal `BlockingQueue`
  3. worker1 finishes t1 → pulls t4 off the queue next
  4. this repeats until the queue drains
  5. call `shutdown()` — no new submits accepted, but t5/t6 still
     get to run to completion before the pool dies
  6. call `shutdownNow()` instead — workers get `interrupt()`,
     t5/t6 never start, returned as a list of un-run tasks

  **The logic behind pool sizing (design *why*):**
  - *why not one thread per task* — OS threads are expensive (MB-
    scale stack, kernel context-switch cost); thousands of
    short-lived threads thrash the scheduler more than they help
  - *why not one giant shared thread forever* — no concurrency at
    all, and one slow task blocks everything behind it
  - *CPU-bound rule of thumb* — pool size ≈ number of cores (more
    threads than cores just fights for the same CPU, adds
    switching overhead for no gain)
  - *I/O-bound rule of thumb* — pool size can exceed cores by a lot:
    threads spend most of their time *blocked* waiting on network/
    disk, not competing for CPU, so more workers = more overlap
  - *why bound the queue at all* — an unbounded queue under
    sustained overload just grows forever → OutOfMemoryError;
    bounded queue + rejection policy fails fast instead

  *One-liner:* a pool is workers + a shared task queue; fixed pool
  for steady CPU-bound load, cached for bursty short I/O work;
  `shutdown()` drains, `shutdownNow()` interrupts.

- **Virtual threads (Java 21)?** *(the modern follow-up)*
  - platform thread = thin wrapper over one OS thread — MB-scale
    stack, thousands at most
  - virtual thread = JVM-managed, KB-scale — millions are fine; it
    *mounts* a carrier (platform) thread to run, and when it blocks
    on I/O the JVM unmounts it and lends the carrier to another
  - what it changes: I/O-bound work no longer needs pool-sizing
    math — one virtual thread per task
    (`Executors.newVirtualThreadPerTaskExecutor()`); CPU-bound
    work gains nothing
  - honesty: IMPS is Spring Boot 2.7 — predates virtual threads;
    speak of them as "what I'd use today," not as shipped
  - [→ Java 21 deep dive](java-versions.md#java-21-2023--lts--virtual-threads)
- **Deadlock?** — two threads each holding a lock the other wants.
  Avoid: consistent lock ordering, or `tryLock` with timeout.
  Starvation/livelock: one-liners if asked.
- **Producer-consumer, the classic shape?** — one or more producer
  threads add work, one or more consumer threads take it, a shared
  buffer between them.
  - naive version: `wait`/`notifyAll` on a shared object — producer
    waits while buffer full, consumer waits while buffer empty,
    each notifies the other after changing state

    ```java
    synchronized (buffer) {
        while (buffer.isEmpty())   // while, NOT if — spurious
            buffer.wait();         //   wakeups recheck the guard
        txn = buffer.remove();
        buffer.notifyAll();
    }
    ```

  - real code: `BlockingQueue` (`put`/`take`) does this internally
    — nobody hand-rolls wait/notify in production
  - ⚓ this *is* IMPS's Kafka shape: producers (NPCI/CBS callers)
    append to a topic, consumer processors `poll()` at their own
    pace — the broker's partition log is the shared buffer.
- **Race condition?** — outcome depends on interleaving; classic
  shape is check-then-act. ⚓ **I shipped one:** IMPS's Redis
  debit-limit check (`hget` → compare → `hset`) isn't atomic — two
  concurrent outward payments can both pass. Fix: Redis MULTI/WATCH
  or a Lua script. (Honest "what I'd fix" gold.)
- **ThreadLocal?** — a per-thread copy of a variable; used for
  non-thread-safe things like the old SimpleDateFormat.
- **Daemon threads?** — background threads; JVM exits when only
  daemons remain (GC is one).
- **JVM memory areas?** — heap (young: eden + survivors; old gen),
  one stack per thread, Metaspace (class metadata), code cache.
  Stack: references + frames; heap: objects.

  ```text
  Heap (shared)   │ young gen: eden → S0/S1 │ old gen │  ← objects
  Stacks (1/thread) frames: locals + references
  Metaspace         class metadata (native memory, Java 8+)
  Code cache        JIT-compiled hot methods
  ```

- **How does GC work?** — reclaims unreachable objects;
  generational: minor GC on young gen (frequent, cheap), major/full
  on old gen (stop-the-world pauses). G1 is default since Java 9.
  `System.gc()` is only a suggestion.
  - object journey: born in eden → survives a minor GC → bounces
    between survivor spaces → promoted to old gen after enough
    survivals — *why:* most objects die young, so collecting eden
    often and old gen rarely is cheap
- **Can Java leak memory?** — yes: lingering references
  - static maps/caches that only grow
  - unclosed resources
  - listeners never deregistered
- **StackOverflowError vs OutOfMemoryError?** — runaway
  recursion fills a thread stack vs heap exhausted.
- **JIT?** — JVM interprets bytecode, then compiles hot paths to
  native for speed.
- **Classloaders?** — bootstrap → platform → application; parent
  delegation (parents get first refusal — stops core classes being
  spoofed).

  *One-liner for the section:* threads share the heap but own
  their stacks; `synchronized` for exclusion, `volatile` for
  visibility, atomics for counters, pools instead of raw threads
  (virtual threads since 21 for blocking I/O) — and the JVM cleans
  up generationally, young and often.

## §6 Trap wall (the code-output classics, spoken)

- `"a" == "a"` → true (same pooled literal); `new String("a") ==
  "a"` → false. Content: always `equals`.
- `Integer a=127, b=127; a==b` → true (cache −128…127);
  `a=128, b=128` → false. `Integer == int` unboxes → value compare.
- Unboxing a null wrapper (`int x = (Integer) null`, or a
  mixed-type ternary) → NullPointerException.
- `5 / 0` → ArithmeticException; `5.0 / 0` → Infinity (IEEE double).
- `return` in finally overrides try's return and swallows its
  exception.
- **Init order?**
  - static (fields + blocks, textual order) — once, at class load,
    superclass first
  - instance (fields + blocks, textual order) → constructor body —
    every `new`
  - subclass: parent's instance-init+ctor finishes before child's
    (via `super()`)
- `x = x++` leaves x unchanged (post-increment hands back the old
  value).
- `'a' + 1` → 98: char arithmetic promotes to int.
- `"1" + 2 + 3` → "123" but `1 + 2 + "3"` → "33" (left-to-right).
- Reassigning a parameter inside a method never affects the
  caller's variable (pass-by-value).
- **Fields NOT polymorphic** — resolved at compile time by
  reference type; instance methods resolve at runtime by actual
  object type (real dynamic dispatch). `static` methods behave like
  fields: hidden, not overridden.

  ```java
  A obj = new B();
  obj.x        // → A.x  (compiler looks at declared type A)
  obj.name()   // → B's override (JVM looks at actual object)
  ```

- `switch` falls through without `break`.

- Array elements default to 0 / false / null; local variables get
  NO default — declaring one unassigned is fine, but *reading* it
  before it's assigned on every path is a compile error (definite
  assignment analysis, not zeroed memory like fields/arrays).

- `0.1 + 0.2 == 0.3` → false (binary floating point can't represent
  them exactly); money → `BigDecimal`, never double.

- `s.concat("x")` / `s.replace(…)` without reassigning → `s`
  unchanged (String immutability, §1).

- `Integer.MAX_VALUE + 1` → wraps silently to `Integer.MIN_VALUE` —
  no overflow exception in Java.

- `Arrays.asList(arr)` is fixed-size — `add`/`remove` →
  UnsupportedOperationException (`set` works).

## §7 Enterprise — Spring / JDBC / REST / integration

*(flow: Spring core → web layer → data layer → integration
architecture → tooling → patterns)*

- **IoC?** — the container creates and wires objects; you declare,
  it injects. DI is the mechanism (constructor/setter/field).
- **@Autowired?** — inject by type (then by name/`@Qualifier`).
  Constructor injection preferred: immutable, test-friendly.
- **Spring Boot vs Spring?** — opinionated Spring, four gifts:
  - starters — curated dependency bundles
  - auto-configuration — sensible defaults from the classpath
  - embedded Tomcat — runnable jar, no server setup
  - Actuator — production endpoints out of the box

  ⚓ IMPS receivers are Spring Boot 2.7 services.
- **@SpringBootApplication?** *(MCQ staple)* — `@Configuration` +
  `@EnableAutoConfiguration` + `@ComponentScan`.
- **Bean scopes?** — singleton (default), prototype, plus
  request/session on the web. Follow-up trap: *is a singleton bean
  thread-safe?* No — one instance serves all requests; it's safe
  only if stateless (no mutable fields).
- **Bean lifecycle?** *(MCQ staple)* — container instantiates →
  injects dependencies → `@PostConstruct` init → in service →
  `@PreDestroy` on shutdown.
- **Stereotypes?**
  - `@Component` — generic managed bean
  - `@Service` — business layer
  - `@Repository` — persistence (+ exception translation)
  - `@Controller` / `@RestController` — web; `@RestController` =
    `@Controller` + `@ResponseBody` (JSON bodies)
- **@Transactional?** — proxy wraps the method in a DB transaction;
  rolls back on unchecked exceptions by default. Follow-up trap:
  *what about checked exceptions?* → they don't trigger rollback
  unless you set `rollbackFor = Exception.class`. (Bank-panel
  favorite behind it: **ACID** — atomicity, consistency,
  isolation, durability.)
- **Actuator?** — production endpoints: /health, /metrics — the
  monitoring surface. (Support-role JD language — use it.)
- **Servlet lifecycle?** — init → service (doGet/doPost) → destroy;
  Spring MVC's DispatcherServlet is a front controller on top
  (MVC = model-view-controller).

  ```text
  request ─▶ DispatcherServlet ─▶ @Controller method ─▶ model
                    │                                     │
  response ◀─ JSON (@ResponseBody) or view template ◀─────┘
  ```

- **REST?** — resources + HTTP verbs, stateless. GET read, POST
  create, PUT replace, PATCH partial, DELETE remove. Idempotent:
  GET/PUT/DELETE; POST is not.
- **GET vs POST?** *(the famous one)*
  - GET — data in the URL, cacheable, idempotent, for reads
  - POST — data in the body, not cached, not idempotent, for
    writes
- **Status codes?** — 200 OK · 201 Created · 204 No Content · 400
  bad request · 401 unauthenticated vs 403 unauthorized · 404 · 409
  conflict · 500 server error · 502/503 upstream/unavailable.
  Memory hook for the pair: *401 = "who are you?", 403 = "I know
  you — no."*
- **REST vs SOAP?**
  - REST — lightweight, JSON, HTTP verbs
  - SOAP — XML envelope + WSDL contract, WS-Security
  - Memory hook: *REST speaks JSON verbs, SOAP speaks XML
    contracts.*
  - ⚓ NPCI speaks signed XML over HTTPS — I've lived the XML+PKI
    world
- **JDBC flow?** — DataSource → Connection → PreparedStatement →
  ResultSet; close in reverse (try-with-resources snippet: §3).
  (Older-panel keyword: driver *types 1–4*; type 4 = pure-Java
  thin driver — what ojdbc8 is.)
- **executeQuery vs executeUpdate vs execute?** *(MCQ staple)*
  - `executeQuery` → ResultSet (SELECT)
  - `executeUpdate` → int rows affected (INSERT/UPDATE/DELETE)
  - `execute` → boolean, handles either
- **Statement vs PreparedStatement?** — precompiled + parameterized
  → SQL-injection-proof and faster. Parameters are **1-indexed** —
  ⚓ I once shipped `setString(0, …)` in IMPS's duplicate check;
  it got erased days later when the check moved to the receiver.
  Honest bug story if "a bug you shipped?" comes.
- **Connection pooling?** — connections are expensive; pools reuse
  them (HikariCP is Boot's default). ⚓ IMPS's CBS-interface runs a
  hand-rolled pool over the CBS proprietary socket.
- **ORM?** — Hibernate maps entities ↔ tables; JPA is the spec
  Hibernate implements. (IMPS used raw JDBC deliberately — full SQL
  control.)
- **ESB vs API gateway?** — enterprise service bus: central
  integration layer doing routing, transformation, protocol
  bridging between core systems (CBS ↔ channels). API gateway = the
  edge door for APIs: authN/Z, rate limiting, routing, monitoring.
  Gateway guards the edge; ESB integrates the inside. (Both are JD
  words — be fluent.)

  ```text
  outside ─▶ API gateway ─▶ │ services ◀─ ESB ─▶ CBS, channels │
             (edge door:      (inside: routing, transformation,
              auth, limits)    protocol bridging)
  ```

- **SOA vs microservices?** — same service idea; SOA integrates via
  a smart bus (ESB), microservices keep the pipes dumb and the
  services small, independently deployable. Memory hook: *SOA
  routes through one smart bus, microservices talk over many dumb
  pipes.*
- **Build tools?** — Maven (pom.xml, convention) / Gradle (Groovy
  DSL, faster, flexible). ⚓ IMPS is a **Gradle** multi-project.
- **Design patterns you know?**
  - singleton — private constructor + static instance; enum is
    the safe form; double-checked locking needs volatile
  - factory, observer — one-liners on demand
  - **and one I shipped at scale: compensating transaction
    (saga)** — IMPS's reversion flow undoes the CBS leg when the
    NPCI leg dies. ⚓ Flagship answer.

  *One-liner for the section:* Spring's container wires the beans,
  `@Transactional` guards the DB work, the servlet front controller
  routes REST verbs to status codes at the edge, JDBC talks to
  Oracle through pooled prepared statements — and the gateway
  guards that edge while the ESB integrates the inside.

## §8 Kafka — every answer anchored in IMPS ⚓

- **What is Kafka?** — distributed, durable pub-sub event log:
  producers append to topics, consumers read at their own pace by
  offset. Decouples producers from consumers.
- **Topic / partition / offset?** — topic = named stream, split
  into partitions; each partition is an ordered log; offset = a
  consumer's bookmark. Ordering is guaranteed per partition only;
  keyed messages always land on the same partition.

  **The mental picture:** a topic is a set of parallel logs, each
  one strictly append-only, each with its own counter.

  ```text
  Topic: outward-reqpay
   ├─ Partition 0: [msg0][msg1][msg2][msg3] offset→ 4
   ├─ Partition 1: [msg0][msg1]            offset→ 2
   └─ Partition 2: [msg0][msg1][msg2]      offset→ 3

  key = txn-ref → hash(key) % numPartitions → always same partition
  ```

  Why keyed: two events for the same transaction ref must land in
  the same partition to preserve their order relative to each
  other — Kafka only orders *within* a partition, never across.

- **Consumer group?** — consumers sharing a group id split the
  partitions between them; kill one and Kafka **rebalances** — the
  survivors take over from the last committed offset.

  ```text
  Group "reqpay-processors" (3 partitions, 2 consumers)
   Partition 0 ─┐
   Partition 1 ─┼─ Consumer A
   Partition 2 ───  Consumer B

   Consumer A dies → rebalance →
   Partition 0 ─┐
   Partition 1 ─┼─ Consumer B  (picks up from last committed offset)
   Partition 2 ─┘
  ```

  Magic number: partitions ≥ consumers in a group, or extra
  consumers sit idle — one partition can't be split between two.

  Follow-up classic — *queue or pub-sub?* Both, via group ids:
  same group = partitions load-shared (queue); different groups =
  each group reads its own full copy (broadcast).
- **Broker / replication?** — brokers host partitions; each
  partition has one leader + follower replicas for failover.
  Followers fully caught up = the **ISR** (in-sync replicas); a
  new leader is elected from the ISR when the old one dies.
- **Why Kafka in IMPS?** *(THE anchor — say this fluently)* — NPCI
  gives a **20-second SLA**: the receiver must validate, persist,
  and ACK fast, so receipt is decoupled from processing. One topic
  per message type — inward/outward × ReqPay/ReqChkTxn/ReqValAdd,
  plus reversion — so each flow scales and fails independently,
  each with its own consumer processor.

  ```text
  NPCI ──signed XML──▶ Receiver ── INSERT journal ──▶ ACK (fast)
                          │
                          └─▶ topic per msg type ──▶ Processor
                              (inward-reqpay, …)        │
                                                 CBS leg via RMI
  ```

  Walkthrough, one inward ReqPay: receiver verifies the message,
  INSERTs it into the Oracle journal, produces to the inward-reqpay
  topic, ACKs NPCI — seconds, well inside 20. The processor polls
  the topic at its own pace, runs the CBS credit over RMI, sends
  the RespPay. A slow CBS never breaks the NPCI-facing SLA.

  *One-liner:* Kafka sits between receipt and processing, so the
  ACK never waits on the bank's core.
- **Kafka vs a traditional MQ (ActiveMQ/RabbitMQ)?**
  - consumers *pull* at their own pace; MQ *pushes*
  - Kafka retains the log after consume → replay possible; MQ
    deletes on ack
  - scales horizontally by adding partitions
  - consumers track their own offsets; MQ broker tracks delivery
- **Delivery semantics?** *(the trio — say all three)*
  - at-most-once: commit *before* processing — never duplicates,
    may lose
  - at-least-once: commit *after* processing — never loses, may
    duplicate
  - exactly-once: transactional producer + consumer — neither, at
    a throughput cost
  - Memory hook: *the commit's position decides — early commit
    loses, late commit repeats. Banks pick repeats + dedupe.*
  - ⚓ IMPS is **at-least-once with DB-level idempotency**: the
    receiver INSERTs every message into the txn journal, and
    processors skip anything whose row is already marked
    processed — NPCI genuinely replays ReqPay, so dedupe is by
    database state, not by Kafka.
- **"What if the processor crashes mid-message?"** *(honesty
  gold)* — normally the group rebalances and the message is
  redelivered. In IMPS offsets were committed at receipt —
  auto-commit was ON (500ms) plus a manual `commitAsync` at
  dequeue — so Kafka considers it done; the DB insert at the
  receiver is the real recovery anchor. **First thing I'd
  redesign: commit after processing, not before.**
- **commitSync vs commitAsync?** — sync blocks until the offset is
  stored (safe, slower); async fires and moves on (fast, may lose
  the commit on crash). Memory hook: *sync stops to confirm; async
  sends and hopes.*

  ```java
  records = consumer.poll(Duration.ofMillis(500));
  process(records);            // IMPS bug: this should come first
  consumer.commitAsync();      // offset marked done either way
  ```

  The order above is the trap — commit *after* processing is the
  safe pattern; IMPS's auto-commit fired on a timer regardless of
  where processing had reached.
- **`acks` config?** *(MCQ staple)* — producer durability knob:

    | `acks` | waits for | can lose when |
    | --- | --- | --- |
    | `0` | nobody | any hiccup |
    | `1` | leader's write | leader dies before replicating |
    | `all` | every in-sync replica (ISR) | — (slowest) |

  A payments system runs `acks=all`.
- **Retention?** — time/size-based; Kafka keeps messages whether or
  not consumed. ⚓ IMPS's duplicate window was reasoned against the
  topic's ~1-hour retention.
- **Consumer lag?** — how far a consumer's offset trails the log
  head; the first Kafka metric to graph in production support.
- **Poison message / DLQ?** — a message that fails processing every
  retry and blocks the partition behind it; standard fix is a
  dead-letter topic to shunt it aside so the queue keeps moving.
  ⚓ IMPS doesn't have one today — retry-then-stuck is the honest
  answer, and it's a natural "what I'd add."
- **Why is Kafka fast?** *(classic)* — three tricks:
  - sequential append-only disk writes (no random seeks)
  - batching: producers and consumers move messages in blocks
  - zero-copy: broker streams file bytes to the socket without
    copying through application memory
- **ZooKeeper?** *(depth reserve)* — older clusters kept metadata in
  ZooKeeper; modern Kafka replaces it with KRaft (built-in Raft
  quorum).

  *One-liner for the section:* Kafka is a replayable append-only
  log split into ordered partitions; IMPS uses it to ACK NPCI fast
  and let the CBS leg run at its own pace, with the DB journal —
  not Kafka — as the truth for dedupe and recovery.

## §9 SDLC + testing

- **SDLC phases?** — requirements → design → build → test → deploy
  → maintain.

- **Waterfall vs Agile?**
  - waterfall — sequential, sign-off-gated
  - agile — iterative, feedback-driven sprints
  - V-model — each dev phase pairs a test phase
  - spiral — risk-driven iterations

  ```text
  requirements ─────────────▶ UAT            (V-model: go down
    high-level design ─────▶ system test      building, come up
      detailed design ───▶ integration test   testing — each level
        code ──────────▶ unit test            checks its twin)
  ```

- **Scrum in one breath?** — product backlog, sprint (2–4 wks),
  daily stand-up, sprint review + retrospective; product owner
  owns *what*, scrum master owns *process*.

    **Actors:**
    - Product Owner — owns the backlog, prioritizes by value; the
      "what"
    - Scrum Master — servant leader, removes impediments, coaches
      the process; the "how"
    - Developers — cross-functional, self-organizing, build the
      increment

    **Events:**
      - Sprint — the container itself, timeboxed 2–4 wks, one goal
      - Sprint Planning — team decides what + how, sets the Sprint
        Goal
      - Daily Scrum — 15 min, syncs the next 24h against the Sprint
        Goal
      - Sprint Review — demo the increment, gather stakeholder
        feedback
      - Sprint Retrospective — team inspects itself, agrees process
        improvements

    **Artifacts:**
      - Product Backlog — ordered, ever-evolving list of everything the product might need
      - Sprint Backlog — this sprint's slice of the backlog + the
        plan to deliver it
      - Increment — everything completed this sprint, must meet
        Definition of Done

    **Commitments** (one per artifact above):
      - Product Goal — long-term objective the Product Backlog
        serves
      - Sprint Goal — single objective that gives the sprint
        coherence
      - Definition of Done — shared checklist for what "complete"
        means

    **Pillars** (empirical process control):
      - Transparency — process and work visible to everyone who
        needs it
      - Inspection — check artifacts and progress often, not just
        at the end
      - Adaptation — adjust promptly when inspection finds a
        deviation

    **Values:**
      - Commitment — team commits to its goals
      - Focus — attention stays on this sprint's work
      - Openness — open about the work and its obstacles
      - Respect — team members respect each other's capability and
        independence
      - Courage — do the right thing, tackle the hard problem

- **Test levels?** — unit → integration → system → UAT (business
  users sign off). (Older-panel keyword: **STLC** — requirement
  analysis → planning → case design → environment setup →
  execution → closure.)
  - Unit — smallest piece (method/class) tested alone, dependencies
    mocked; owned by the developer
  - Integration — checks that separately-built modules talk to each
    other correctly (service ↔ DB, service ↔ service)
  - System — whole application end-to-end against requirements, in
    a prod-like environment
  - UAT — business users verify it meets the real-world need; the
    sign-off gate before go-live

- **Smoke vs sanity vs regression?**
  - smoke — wide but shallow: skims every major feature on a new
    build; the go/no-go gate before deeper testing starts
  - sanity — narrow but deep: quick, usually unscripted dive into
    just the changed area; "does the fix even make sense?"
  - regression — re-run the existing suite (usually automated,
    broad) to confirm nothing else broke
  - Memory hook: *smoke checks the whole build barely, sanity
    checks one bit closely, regression re-checks everything that
    already passed.*

- **Retesting vs regression?** *(X-vs-Y classic)* — retesting:
  the fixed bug itself, again; regression: everything *around* it.
  Memory hook: *retest the fix, regress the rest.*

- **Error vs defect vs failure?** *(definitional ladder — old-panel
  favorite)* — error: the human mistake; defect/bug: that mistake
  sitting in the code; failure: the defect firing at runtime. A
  defect that never executes never fails.

- **Black-box vs white-box?** — black-box tests *what* the software
  does from the outside (inputs → outputs, no view of the code);
  white-box tests *how*, with full sight of the internal paths and
  logic.
  - **black-box** — against the spec (functional, system, UAT);
    catches wrong/missing behavior, misses dead code
    - in Java: MockMvc / TestRestTemplate / RestAssured against REST
      endpoints, Selenium at the UI
  - **white-box** — against the code (paths, branches, loops);
    catches logic gaps, misses missing requirements
    - in Java: JUnit 5 + Mockito to drive the branches/edge cases,
      JaCoCo (Maven/Gradle plugin) measuring line/branch coverage,
      often gating the build below a threshold
  - **grey-box** — partial inside knowledge (DB schema, API
    contract) while testing from outside; common in integration +
    security
  - Memory hook: *black-box tests the spec, white-box tests the
    code, grey-box knows just enough to aim.*
  - ⚓ IMPS: NPCI's certification suite exercises the receivers
    black-box (signed XML in, response code out); the thin unit
    layer underneath is white-box on mapping/validation logic —
    honestly thin (§10)

- **Functional vs non-functional?** — what it does vs how well:
  performance, security (⚓ JD names VAPT), usability.

- **Performance testing — the flavors?** *(non-functional, "how
  well")*
  - load — expected peak traffic; do response times hold
  - stress — push *past* the limit to find the breaking point
  - spike — sudden burst, then watch recovery
  - soak / endurance — sustained load for hours; surfaces memory
    leaks and resource exhaustion
  - volume — large *data sets*, not large traffic
  - tools: **Apache JMeter** (the classic — thread groups =
    virtual users, record/replay HTTP), Gatling, k6, LoadRunner
  - metrics that matter: throughput (req/s), latency (p95/p99),
    error rate
  - Memory hook: *load = does it hold, stress = where it breaks,
    soak = does it rot.*

- **Security testing — VAPT, SAST, DAST?** *(⚓ JD names VAPT —
  bank must-know)*
  - **Don't list these as peers** — VAPT is the *engagement* (find
    → exploit → report); SAST/DAST/SCA/IAST are *techniques* run
    within it. DAST in particular sits inside the VA phase.

    ```text
    VAPT  (engagement: find → exploit → report)
    ├── Vulnerability Assessment (VA) — automated, breadth
    │   ├── Network scanners ...... Nessus, Qualys, OpenVAS
    │   ├── Port scanning ......... Nmap
    │   └── Web-app scanners (DAST) OWASP ZAP, Burp Scanner
    └── Penetration Testing (PT) — manual, depth
        ├── Manual exploitation
        ├── Metasploit
        ├── Burp Suite (intercept/exploit mode)
        ├── sqlmap
        └── Kali Linux (toolkit)

    Run alongside, mostly CI-side (not part of the PT engagement):
    ├── SAST — source/bytecode, no run ... SonarQube, Fortify, Snyk Code
    ├── SCA  — 3rd-party CVEs ............ Dependency-Check, Snyk
    └── IAST — agent inside running app .. (hybrid, depth reserve)
    ```

  - **VAPT** = Vulnerability Assessment + Penetration Testing —
    the umbrella banks mandate before go-live
    - VA (Vulnerability Assessment) — automated, broad scan that
      *lists* weaknesses (breadth); tools: Nessus, OpenVAS,
      Qualys, Nmap
    - PT (Penetration Testing) — manual, deep; a tester actively
      *exploits* them (depth); tools: Metasploit, Burp Suite,
      sqlmap, Kali Linux (toolkit)
    - hook: *VA finds the unlocked doors, PT walks through them.*
  - **SAST** (Static Application Security Testing) — scans source/
    bytecode *without running it* (white-box), runs early in CI.
    Tools: SonarQube, Checkmarx, Fortify, Snyk Code
  - **DAST** (Dynamic Application Security Testing) — attacks the
    *running* app from outside (black-box), no source needed.
    Tools: OWASP ZAP, Burp Suite
    - Burp shows up under *both* PT and DAST by design — manual
      intercept/exploit (PT) **and** automated scan (DAST); same
      toolkit, two modes. Name the reason before a panel "catches"
      the overlap.
  - **SCA** (Software Composition Analysis) — scans third-party
    dependencies for known CVEs (Common Vulnerabilities and
    Exposures; the Log4Shell class). Tools: OWASP Dependency-Check,
    Snyk
  - **IAST** (Interactive Application Security Testing) — hybrid:
    an agent inside the running app watches execution (depth
    reserve — name only)
  - **OWASP Top 10** (Open Worldwide Application Security Project)
    — the canonical web-vuln list (injection, XSS, broken auth…);
    the reference every panel expects you to name
  - ⚓ honest surface: I wrote injection-safe code (PreparedStatement,
    §7) and NPCI mandates signed XML + PKI — but the formal VAPT/scan
    program is a dedicated security function; name the tools, don't
    claim you ran the pentest (§10)

- **Verification vs validation?** *(classic)* — building it right
  vs building the right thing.
  - verification — *"are we building it right?"* — against the
    spec/design, usually **no code run**: reviews, walkthroughs,
    inspections, static analysis
  - validation — *"are we building the right thing?"* — against
    real user need, **code runs**: functional, system, UAT
  - Memory hook: *verify the spec on paper, validate the user by
    running it.*

- **Severity vs priority?** — technical impact vs fix order.
  - severity — how badly it breaks the product (technical); the
    tester sets it
  - priority — how soon it must be fixed (business urgency); the
    PM / product owner sets it
  - the four corners make it click:
    - high sev + high pri — payment flow down → fix now
    - high sev + low pri — crash in a rarely-used report → can wait
    - low sev + high pri — typo in the logo on the login page →
      trivial but embarrassing, fix fast
    - low sev + low pri — minor UI misalignment → whenever
  - Memory hook: *severity = how much it hurts, priority = how
    fast it must go.* (Cross-ref `itil-vocab.md` P1–P4.)

- **Defect lifecycle?** — new → assigned → fixed → retest →
  closed, or reopened.
  - New — tester logs the defect
  - Assigned — lead hands it to a developer
  - Open / In-progress — developer is fixing it
  - Fixed / Resolved — dev done, awaiting verification
  - Retest — tester re-runs the case against the fix
  - Closed — verified gone; or Reopened — fix failed retest,
    back to the dev
  - side exits: Deferred (fix later), Rejected (not a bug),
    Duplicate (already logged)

- **JUnit/Mockito?** — JUnit 5: `@Test`, assertions,
  `@BeforeEach`; Mockito stubs dependencies so units test alone.
  Test pyramid — *why:* the lower the layer, the faster and
  cheaper the feedback, so put the bulk there:

  ```text
        /  E2E  \       few — slow, brittle, whole-system
       / integr. \      some — real wiring, real DB
      /   unit    \     many — fast, isolated, run every build
  ```

  Follow-up trap: *"how did you test IMPS?"* → the honest answer
  lives in §10 — never claim coverage.

- **TDD vs BDD?** *(X-vs-Y classic)* — both write tests *first*;
  they differ in language and audience:
  - TDD (test-driven) — red → green → refactor: write a failing
    unit test, make it pass, then clean up; developer-facing,
    JUnit + Mockito
  - BDD (behavior-driven) — describe behavior in plain-language
    scenarios (Given/When/Then), readable by business + QA;
    Cucumber runs Gherkin `.feature` files against step definitions
  - Memory hook: *TDD checks the code works, BDD checks it does
    the right thing — in language everyone reads.*

- **CI/CD?**
  - CI (Continuous Integration) — every commit builds + tests
    automatically, so integration breaks surface in minutes, not
    at a big-bang merge
  - CD — two readings, know both:
    - Continuous **Delivery** — pipeline keeps every build
      *deployable*; the push to prod stays a manual button
    - Continuous **Deployment** — no button; every green build
      ships to prod automatically
  - DevOps — dev + ops sharing the pipeline and the pager

  **Conventional Java pipeline (the shape):**

  ```text
  git push → CI server → mvn/gradle build → JUnit + JaCoCo +
  SonarQube gate → publish JAR/WAR to Nexus → Docker image →
  deploy (K8s / app server)
  ```

  **Tools to name:**
  - CI/CD servers: **Jenkins** (the Java classic), GitHub Actions,
    GitLab CI, CircleCI, TeamCity, Bamboo
  - build step: Maven / Gradle (⚓ IMPS is Gradle) — the pipeline
    runs `mvn verify` / `gradle build`
  - quality gates: JUnit + JaCoCo (coverage), SonarQube (SAST) —
    fail the build below threshold
  - artifact repo: Nexus, JFrog Artifactory (where built JARs land)
  - ship: Docker → registry → Kubernetes; Ansible / Terraform for
    infra
  - Memory hook: *CI proves it builds, CD ships it — delivery
    stops at a button, deployment doesn't.*

  *One-liner for the section:* phases flow requirements → deploy;
  the V pairs each build level with its test twin; smoke asks
  "alive?", sanity "fixed?", regression "broke anything?";
  verification builds it right, validation builds the right thing.

## §10 Honesty guardrails (truth law)

- **"How did you test IMPS?"** — straight answer: NPCI's
  certification suite + bank UAT + production hardening; unit
  coverage is thin (one smoke test per module) — **JUnit discipline
  is what I'd add today.** Never claim test coverage.
- Never claim: exactly-once Kafka semantics, atomic Redis ops,
  inbound XML-signature verification (outbound signing only),
  P2P/MMID support (P2A via netbanking only), NSQ (design doc
  predates the Kafka migration — the shipped system is Kafka).
- ⚠️ Before any interview: confirm deployment **JDK version** (not
  pinned in Gradle) so "which Java version?" gets a one-word answer.

## Rep scorecard — 🟢 only after a blind aloud rep

| Section | Rep 1 | Rep 2 | Rep 3 |
|---|---|---|---|
| §1 Bedrock | 🟢 | ☐ | ☐ |
| §2 Collections | 🟢 | ☐ | ☐ |
| §3 Exceptions | 🟢 | ☐ | ☐ |
| §4 Java 8+ | 🟢 | ☐ | ☐ |
| §5 Threads + JVM | ☐ | ☐ | ☐ |
| §6 Trap wall | 🟢 | ☐ | ☐ |
| §7 Enterprise | ☐ | ☐ | ☐ |
| §8 Kafka ⚓ | ☐ | ☐ | ☐ |
| §9 SDLC/testing | 🟢 | ☐ | ☐ |

*Drill order suggestion: §8 first (flagship + profile anchor), then
§1→§6 core, then §7, §9. — Rico coughed this up; Kowalski approved.*
