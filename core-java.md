# Core Java Rapid-fire Kit

- Core Java Rapid-fire Kit
  - [§1 Bedrock — Java identity + OOP](#1-bedrock--java-identity--oop)
  - [§2 Collections](#2-collections)
  - [§3 Exceptions](#3-exceptions)
  - [§4 Java 8+ (know it cold)](#4-java-8-know-it-cold)
  - [§5 Threads + JVM](#5-threads--jvm)
  - [§6 Trap wall (the code-output classics, spoken)](#6-trap-wall-the-code-output-classics-spoken)
  - [§7 Enterprise — Spring / JDBC / REST / integration](#7-enterprise--spring--jdbc--rest--integration)
  - [§8 Kafka ⚓ → kafka-answers.md](#8-kafka-)
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

  Two independent axes decide access: **same package or not**, and
  **subclass or not**. Four cells:

  |Modifier|Same class|Same pkg|Other pkg, subclass|Other pkg, rest|
  |---|---|---|---|---|
  |`private`|✅|❌|❌|❌|
  |default (none)|✅|✅|❌|❌|
  |`protected`|✅|✅|✅|❌|
  |`public`|✅|✅|✅|✅|

  - "same pkg" is any class in the package — subclass or not, so a
    subclass sitting in the same package rides that column, not the
    `protected` one
  - "other pkg, rest" is a class outside the package that does *not*
    extend yours — the ordinary caller, and the only audience
    `public` adds over `protected`
  - each row widens the one above it: once a ❌ appears, everything
    to its right is ❌ too
  - `protected` across packages has a catch: the subclass may touch
    the member on itself (`this.x`, or a reference typed as its own
    class), not on an arbitrary instance of the parent
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
- **Why were `default` methods introduced?** — **interface evolution**.
  Pre-8, adding a method to a published interface broke every
  implementor at compile time. Java 8 needed `stream()` on
  `Collection` and `forEach()` on `Iterable` to make lambdas useful —
  impossible without breaking the world, so a `default` body lets an
  interface grow while old implementors keep compiling untouched.
  - `static` interface methods came along to keep factories/helpers on
    the contract itself (`List.of`, `Comparator.comparing`) instead of
    a separate `Collections`-style utility class
  - the cost: interfaces carry behavior, so the diamond can bite again
    — which is what the default-method tie-break rules exist for
  - not a licence for state — still no instance fields, so an abstract
    class remains the answer when you need data
- **Can a class extend two classes?** — no. `extends` takes exactly
  one class, `implements` takes any number.

  |Shape|Legal?|
  |---|---|
  |`class D extends A`|✅ one superclass|
  |`class D extends A, B`|❌ won't compile|
  |`class D implements X, Y, Z`|✅ any number of interfaces|
  |`class D extends A implements X, Y`|✅ one class + many interfaces|
  |`interface Z extends X, Y`|✅ interfaces DO multiply-extend|

  - every class has exactly one direct superclass — `Object` when you
    name none — so the hierarchy is a tree, not a graph
  - *multilevel* ≠ *multiple*: `C extends B`, `B extends A` is fine at
    any depth; only two parents on one `extends` is banned
  - need capability from two places → **composition**: hold both as
    fields and delegate. No ambiguity, and the parts are swappable at
    runtime
  - C++ permits it (virtual inheritance); Java traded that power for a
    simpler object model
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

  Three tie-break rules when a `default` clashes:
  - **class wins** — any concrete method inherited from a superclass
    beats an interface default of the same signature
  - **most specific interface wins** — a sub-interface's default beats
    the one it overrides
  - **neither applies → compile error**, until the class overrides;
    `Iface.super.method()` reaches a chosen parent's version

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
    ```

    ```java
    emps.sort(Comparator.comparing(Emp::getSalary).reversed());          // external: by salary
    ```

  *One-liner:* Comparable = the class sorts itself, one way.
  Comparator = you hand in a sort rule, as many ways as needed.

- **Queue vs Deque?** — Queue: one end in, other end out, FIFO.
  Deque extends it with both ends open, so the same object serves
  as a queue or a stack.

  **The shape:**
  - `Queue` extends Collection — insert at the tail, remove from
    the head
  - `Deque` (double-ended queue) extends Queue — insert and
    remove at *either* end, covering FIFO *and* LIFO
  - `ArrayDeque` and `LinkedList` implement Deque; `PriorityQueue`
    implements Queue only

  **Methods — every operation ships in two flavours**, same
  action, different failure:

    | Operation | Throws | Returns `null`/`false` |
    | --- | --- | --- |
    | insert | `add(e)` | `offer(e)` |
    | remove head | `remove()` | `poll()` |
    | inspect head | `element()` | `peek()` |

  - prefer the special-value column — a full/empty queue is
    normal traffic, not an exception
  - Deque doubles each one with a `First`/`Last` suffix:
    `offerFirst`/`offerLast`, `pollFirst`/`pollLast`,
    `peekFirst`/`peekLast`
  - stack aliases sit on the head: `push` = `addFirst`,
    `pop` = `removeFirst`

  **Picking an implementation:**
  - default to `ArrayDeque` for both stack and queue — one
    resizable circular array, contiguous and cache-friendly,
    nothing extra to collect
  - over `Stack`: legacy, extends Vector — every call pays
    synchronization even single-threaded
  - over `LinkedList`: a Node object (value + 2 pointers) per
    element — scattered memory, GC load
  - the one disqualifier: `ArrayDeque` forbids nulls (they'd be
    ambiguous with `poll`'s empty signal) — need them, take
    `LinkedList`

  *One-liner:* Queue = FIFO with polite (`offer`/`poll`) and rude
  (`add`/`remove`) methods. Deque = both ends, so it replaces
  Stack. `ArrayDeque` for both, unless you need nulls.

- **PriorityQueue?** — a Queue that ignores arrival order: a
  binary heap where `poll` always hands back the smallest element
  by the comparator, not the oldest.

  - binary heap in an array — no ends to speak of, which is why
    it implements Queue but not Deque
  - min-heap by default: needs `Comparable` elements or a
    `Comparator` at construction; flip to max-heap via
    `Comparator.reverseOrder()`
  - `offer`/`poll` O(log n), `peek` O(1)
  - trap: iteration order is *not* sorted — only the head is
    guaranteed; no nulls
  - reach for it on "top K", scheduling, Dijkstra — anywhere
    "who's next" is a priority, not a position

  *One-liner:* PriorityQueue = heap in an array, head is always
  the min, iteration order means nothing.

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

→ **[threads-jvm.md](threads-jvm.md)** — threads · locks · the memory
model · executors · JVM internals, ordered so that no answer depends
on one below it. Runnable companion:
[s5-threads-lab.md](s5-threads-lab.md).

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

*(flow: Spring core → request path → beans and transactions → REST →
data layer → integration architecture → tooling → patterns)*

- **IoC?** — *inversion of control*: normally your code controls its
  dependencies (`new` them, hard-code them); inverted, the container
  — Spring's runtime registry of objects — constructs and wires them
  and hands them to you. You declare, it injects. DI (*dependency
  injection*) is the mechanism (constructor/setter/field). A **bean** is just
  an object the container owns: it builds it, injects into it, hands
  it out.

- **@Autowired?** — marks a constructor/field/setter the container
  should fill. Resolution is **by type**: exactly one bean of that
  type → injected; none → startup failure (unless `required = false`
  or `Optional<T>`); two or more → `NoUniqueBeanDefinitionException`,
  broken by `@Primary` on the default one or `@Qualifier("beanName")`
  at the injection point, else by matching the field/parameter name
  against the bean name.
  Constructor injection preferred: the dependency can be `final` (no
  half-built bean, safe to publish across threads), a missing one
  fails at startup instead of NPE-ing on first call, and the class is
  constructible in a test with plain `new` — no reflection, no
  container. Single-constructor classes don't need the annotation
  (Spring 4.3+).

- **Spring Boot vs Spring?** — not a rival framework: Boot *is*
  Spring plus opinions. Plain Spring gives you the container but
  makes you assemble everything around it — pick compatible library
  versions yourself, declare every `DataSource`/`EntityManager`/
  `DispatcherServlet` bean by hand in XML or `@Configuration`, and
  build a WAR to deploy into a Tomcat someone else installed. Boot
  removes each of those:
  - starters — one dependency pulls a curated, version-aligned bundle
  - auto-configuration — beans you didn't declare, inferred from
    what's on the classpath, backing off the moment you declare
    your own
  - embedded Tomcat — `java -jar`, no external server
  - Actuator — health, metrics, env endpoints out of the box

  Same container, same beans, same annotations underneath — you can
  still override any of it.

  ⚓ IMPS receivers are Spring Boot 2.7 services.

- **Is Spring a web framework?** — no: it's a **container** — DI, bean
  lifecycle, transactions, configuration — and the web module is one
  branch off it, alongside data access, security, scheduling,
  messaging and batch. A Spring app with no HTTP endpoint at all is
  ordinary: batch jobs and queue consumers are Spring's home turf as
  much as REST is. ⚓ IMPS is exactly that shape — the receivers speak
  HTTP, the 7 consumers just read Kafka and never serve a request.

- **@SpringBootApplication?** *(MCQ staple)* — `@Configuration` +
  `@EnableAutoConfiguration` + `@ComponentScan`.

- **Servlet lifecycle?** — a servlet is the object the web container
  hands each HTTP request to, one thread per request: init → service
  (doGet/doPost) → destroy. Spring MVC's DispatcherServlet is a front
  controller on top (MVC = model-view-controller).

  ```text
  request ─▶ DispatcherServlet ─▶ @Controller method ─▶ model
                    │                                     │
  response ◀─ JSON (@ResponseBody) or view template ◀─────┘
  ```

- **Bean scopes?** — singleton (default — one per container, not the
  GoF pattern), prototype, plus request/session on the web.
  Follow-up trap: *is a singleton bean thread-safe?* No — one
  instance serves every request and each request runs on its own
  thread; it's safe only if stateless (no mutable fields).

- **Bean lifecycle?** *(MCQ staple)* — container instantiates →
  injects dependencies → `@PostConstruct` init → in service →
  `@PreDestroy` on shutdown.

- **Stereotypes?**
  - `@Component` — generic managed bean
  - `@Service` — business layer
  - `@Repository` — persistence (+ exception translation: JDBC's
    checked `SQLException` → Spring's unchecked `DataAccessException`)
  - `@Controller` / `@RestController` — web; `@RestController` =
    `@Controller` + `@ResponseBody` (JSON bodies)

- **@Transactional?** — nobody ever holds your bean. Spring hands
  the caller a **proxy** — a generated stand-in with the same
  interface — and the proxy is what opens the transaction, calls
  your method, then commits or rolls back. You write zero
  `commit()` / `rollback()`.

  ```text
  caller ──▶ proxy ─── begin ──▶ your method
                       commit (returned normally)   ◀┐
                       rollback (threw unchecked)   ◀┘
  ```

  Everything a panel asks about it follows from *the proxy is on
  the outside*:

  - **self-invocation trap** — the proxy only sees calls that
    arrive **through** it. One method of the class calling
    `this.otherMethod()` goes straight to the object, so
    `@Transactional` on `otherMethod` does nothing. Same reason
    it must be `public` — a private method has no door to wrap.
    Fix: put the annotated method on another bean and inject it.
    *Hook: the annotation is on the door; an inside call never
    uses the door.*

    ```java
    @Service
    class TransferService {

      public void transfer(Txn t) {
        doTransfer(t);              // ✗ plain this.doTransfer —
      }                             //   no proxy, no transaction

      @Transactional
      public void doTransfer(Txn t) { ... }
    }

    // fix: cross a bean boundary so the call goes through a proxy
    @Service
    class TransferService {
      private final LedgerService ledger;   // injected bean

      public void transfer(Txn t) {
        ledger.doTransfer(t);       // ✓ proxied call
      }
    }
    ```

  - **swallowed-exception trap** — catch the exception inside the
    method and the proxy sees a normal return, so it **commits**.
    Rethrow, or mark the transaction rollback-only.

    ```java
    @Transactional
    public void pay(Txn t) {
      ledger.debit(t);
      try {
        npci.send(t);
      } catch (SendException e) {
        log.error("send failed", e);   // ✗ swallowed → the debit
      }                                //   above COMMITS
    }

    @Transactional
    public void pay(Txn t) {
      ledger.debit(t);
      try {
        npci.send(t);
      } catch (SendException e) {
        log.error("send failed", e);
        throw new PaymentFailedException(e);   // ✓ proxy sees it
        // or: TransactionAspectSupport
        //       .currentTransactionStatus().setRollbackOnly();
      }
    }
    ```

  - **rollback rules** — rolls back on **unchecked**
    (`RuntimeException`, `Error`); a **checked** exception commits
    unless you say `rollbackFor = Exception.class`. The reasoning:
    unchecked = a bug, unwind; checked = an outcome you declared,
    so Spring assumes you handled it. *Hook: Spring only panics
    about what you didn't declare.*

    ```java
    @Transactional
    void a() throws IOException { save(); throw new IOException(); }
    // checked → COMMITS. The save() is persisted.

    @Transactional(rollbackFor = Exception.class)
    void b() throws IOException { save(); throw new IOException(); }
    // → rolls back

    @Transactional
    void c() { save(); throw new IllegalStateException(); }
    // unchecked → rolls back, no configuration needed
    ```

  - **propagation** — `REQUIRED` (default) joins the caller's
    transaction if one is running, else starts one; `REQUIRES_NEW`
    suspends the caller's and runs in its own, so it survives the
    caller rolling back. Bank-panel example: the audit row must
    stay even when the payment unwinds → `REQUIRES_NEW`.

    ```java
    @Transactional                                  // TX-1
    public void pay(Txn t) {
      audit.record(t);        // TX-2: commits on its own
      ledger.debit(t);        // joins TX-1
      throw new PaymentFailedException();
    }                         // TX-1 rolls back the debit;
                              // the audit row stays.

    @Service
    class AuditService {
      @Transactional(propagation = Propagation.REQUIRES_NEW)
      public void record(Txn t) { ... }
    }
    ```
  - **ACID**, the property set the annotation is buying you —
    **atomicity** (all the statements land or none do),
    **consistency** (valid state to valid state, constraints
    intact), **isolation** (concurrent transactions don't read
    each other's half-done work — the level comes from the DB,
    usually read-committed), **durability** (once committed it
    survives a crash).
  - Scope note: this is one database's transaction. Across
    services there is nothing to roll back — that is where the
    saga in this section's pattern list takes over.

- **Actuator?** — production endpoints: /health, /metrics — the
  monitoring surface. (Support-role JD language — use it.)

- **REST?** — resources + HTTP verbs, stateless. GET read, POST
  create, PUT replace, PATCH partial, DELETE remove. Idempotent =
  repeating the request leaves the server in the same state (not:
  returns the same response) — GET/PUT/DELETE; POST is not.

- **GET vs POST?** *(the famous one)*
  - GET — data in the URL, cacheable, idempotent, for reads
  - POST — data in the body, not cached, not idempotent, for
    writes

- **Status codes?** — 200 OK · 201 Created · 204 No Content · 400
  Bad Request · 401 Unauthorized · 403 Forbidden · 404 Not Found ·
  409 Conflict · 500 Internal Server Error · 502/503
  upstream/unavailable.
  The 401/403 pair is the trap: 401 is *named* Unauthorized but
  *means* unauthenticated — no or bad credentials; 403 means
  authenticated and still refused. Memory hook: *401 = "who are
  you?", 403 = "I know you — no."*

- **REST vs SOAP?** — both send a request over HTTP to a remote
  service; they differ in what the contract is made of.
  - **REST** — *representational state transfer*: lightweight,
    JSON, HTTP verbs. An architectural style, not a protocol.
  - **SOAP** — *simple object access protocol*: XML envelope +
    **WSDL** (web services description language) contract file,
    WS-Security. An actual protocol, with a schema to validate
    against.
  - Memory hook: *REST speaks JSON verbs, SOAP speaks XML
    contracts.*
  - ⚓ NPCI speaks signed XML over HTTPS — I've lived the XML+PKI
    world

- **JDBC flow?** — DataSource (where connections come from — a pool,
  not a fresh socket per call) → Connection → PreparedStatement →
  ResultSet; close in reverse (try-with-resources snippet: §3).
  (Older-panel keyword: driver *types 1–4*; type 4 = pure-Java
  thin driver — what ojdbc8 is.)

- **executeQuery vs executeUpdate vs execute?** *(MCQ staple)*
  - `executeQuery` → ResultSet (SELECT)
  - `executeUpdate` → int rows affected (INSERT/UPDATE/DELETE)
  - `execute` → boolean, handles either

- **Statement vs PreparedStatement?** — precompiled + parameterized:
  the SQL is parsed before the values arrive, so a parameter can
  never become SQL syntax → injection-proof and faster. Parameters
  are **1-indexed** —
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

- **SOA vs microservices?** — both are **service-oriented
  architecture**: split the system into services that talk over the
  network. They differ in where the smarts and the boundaries sit.
  - SOA (the 2000s enterprise style) — a few coarse services wired
    through an **ESB** (enterprise service bus) that routes,
    transforms and bridges protocols; a canonical schema and
    central governance; services often share one database and ship
    on the same release train.
  - microservices — many small services, each owning its own data
    and deployable on its own; the smarts live in the service, the
    network stays plain HTTP or a broker; teams release
    independently.
  - Memory hook: *SOA puts the logic in one smart bus,
    microservices put it in the endpoints and keep the pipes
    dumb.*

- **Build tools?** — Maven (pom.xml, convention) / Gradle (Groovy
  DSL, faster, flexible). ⚓ IMPS is a **Gradle** multi-project.

- **Design patterns you know?**
  - singleton — private constructor + static instance; enum is
    the safe form; double-checked locking needs `volatile` or
    another thread can see the reference assigned before the
    constructor has finished ([threads-jvm.md](threads-jvm.md) §7)
  - factory — a method decides which concrete class to hand back,
    so the caller codes to the interface and never says `new`.
    `Executors.newFixedThreadPool(4)` returns an
    `ExecutorService`; the caller never learns the class name.
    *Hook: you order by what you want, not by who makes it.*
  - strategy — pull the varying algorithm out into an interface
    and pass the one you want in; swap behaviour without touching
    the caller. `list.sort(comparator)` — the comparator **is**
    the strategy. *Hook: same engine, pluggable gearbox.*
  - observer — subject keeps a list of listeners and publishes an
    event; listeners register and unregister without the subject
    knowing them. Spring's `ApplicationEventPublisher` +
    `@EventListener`. *Hook: don't call us, we'll call you.*
  - template method — an abstract base fixes the **order** of
    steps in a `final` method and leaves the varying steps to
    subclasses. Spring's `JdbcTemplate` owns the skeleton (get
    connection, execute, translate exceptions, close) and you
    supply only the `RowMapper`. *Hook: the base owns the recipe,
    the subclass fills in the ingredients.*
  - strategy vs template method, if they push: strategy varies
    behaviour by **composition** at runtime (hand in another
    object), template method varies it by **inheritance** at
    compile time (override a hook). Same goal, different lever.
  - **and one I shipped at scale: compensating transaction
    (saga)** — a **saga** is one business transaction split across
    services into a chain of local commits, each paired with a
    compensating action that semantically undoes it; there is no
    two-phase commit to roll back with, so you *unwind* instead.
    ⚓ IMPS (Immediate Payment Service) — the reversion flow fires
    the compensating leg against **CBS** (core banking system)
    when the **NPCI** (National Payments Corporation of India) leg
    dies. Flagship answer. *Hook: no rollback across services —
    you pay it back, you don't take it back.*

    **The same answer for a functional banker** — banking first,
    tech second. ⚓ Every IMPS leg parks the money in the bank's
    **IMPS pool account**, so no leg is ever left half-booked on
    a customer.

    - *Outward* (our customer is the remitter) — debit the
      customer, credit the pool, narration
      `IMPS/<RRN>/<beneficiary>/<bank>/XXX<last4>/<note>`; then
      the ReqPay goes to NPCI. If NPCI won't take it, or the
      RespPay / RespChkTxn comes back FAILURE, the reversion leg
      pays the customer back **out of the pool**, narration
      `IMPSREV/48/<RRN>/…`, and the customer's daily debit
      counter is given the amount back.
    - *Inward* (our customer is the beneficiary) — credit the
      beneficiary out of the pool, narration `IMPS/48/<RRN>/…`.
      If the transaction is later declared failed, the reversion
      leg pulls that credit **off the beneficiary back into the
      pool** with the same `IMPSREV` narration.
    - Either direction: a **reversion** record moves `marked` →
      `reverted`, the CBS approval number of the undo posting is
      stamped on it, and the transaction's settlement amount is
      zeroed so nothing is left to settle for it. Both postings
      stay on the statement — the reversal is a fresh entry, not
      an erased one, which is what makes it *compensating* and
      not a rollback.
    - Tech second: the failure only **publishes** — a Kafka
      `reversion` message keyed by transaction id. A separate
      consumer does the money leg, so a CBS outage delays a
      reversion but never loses it, and the direction is read off
      the ReqPay message id (our own bank-code prefix = outward).

  *One-liner for the section:* Spring's container wires the beans,
  `@Transactional` guards the DB work, the servlet front controller
  routes REST verbs to status codes at the edge, JDBC talks to
  Oracle through pooled prepared statements — and the gateway
  guards that edge while the ESB integrates the inside.

## §8 Kafka ⚓

→ **[kafka-answers.md](kafka-answers.md)** — the log model ·
partitions and ordering · producers · consumer groups and offsets ·
delivery guarantees · rebalancing · retention and DLQ · Spring
Kafka · the IMPS anchor. Self-contained, answers sized for speaking.

*One-liner for the section:* Kafka is a replayable append-only log
split into ordered partitions, where the partition is both the unit
of ordering and the unit of parallelism, and **commit timing** is the
knob that sets your delivery guarantee — IMPS uses it to ACK NPCI
fast and let the CBS leg run at its own pace, with the DB journal,
not Kafka, as the truth for dedupe and recovery.

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
    - Product Backlog — ordered, ever-evolving list of everything
      the product might need
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
| §5 Threads + JVM | 🟢 | ☐ | ☐ |
| §6 Trap wall | 🟢 | ☐ | ☐ |
| §7 Enterprise | 🟢 | ☐ | ☐ |
| §8 Kafka ⚓ | 🟢 | ☐ | ☐ |
| §9 SDLC/testing | 🟢 | ☐ | ☐ |

*Drill order suggestion: §8 first (flagship + profile anchor), then
§1→§6 core, then §7, §9. — Rico coughed this up; Kowalski approved.*
