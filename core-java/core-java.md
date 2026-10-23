# Core Java Rapid-fire Kit

- **Goal:** explain Java clearly, predict its behavior, choose the right API, and debug a plausible mistake.

- **Prerequisites:** basic syntax, methods, and a small program you can compile and run.

- **Version baseline:** unlabelled Java examples use Java 8 language and APIs. Later features carry a release label.

- **Practice runtime:** most repository labs use a full JDK 21+. Java 25 labs have separate run instructions.

- **Reading style:** learn the rule, trace the example, answer the mastery questions, then complete the linked drill.

- **Speaking style:** give the direct answer first. Add the reason, a concrete example, and a tradeoff when asked.

- **Examples:** code blocks are independent. Statement snippets omit standard imports and belong inside a method; declarations and intentionally rejected lines are labelled.

---

## Navigation

- [§1 Bedrock — Java identity + OOP](#1-bedrock--java-identity--oop)

- [Generics and type safety](#generics-and-type-safety)

- [§2 Collections](#2-collections)

- [Numbers and money](#numbers-and-money)

- [§3 Exceptions](#3-exceptions)

- [I/O and resource ownership](#io-and-resource-ownership)

- [§4 Java 8+](#4-java-8-know-it-cold)

- [§5 Threads + JVM](#5-threads--jvm)

- [§6 Trap wall](#6-trap-wall-the-code-output-classics-spoken)

- [§7 Enterprise — Spring / JDBC / REST / integration](#7-enterprise--spring--jdbc--rest--integration)

- [§8 Kafka](#8-kafka-)

- [§9 SDLC + testing](#9-sdlc--testing)

- [Precision card](#precision-card)

- [Integrated practice](#integrated-practice)

---

## Learning paths

- **Foundations:** §1 → generics → §2 → numbers → §3 → I/O.

- **Functional Java:** §4 → [Java 8 foundations](java-8.md) → stream and async drills.

- **Runtime reasoning:** §5 → [threads and JVM](threads-jvm.md) → §6 predictions.

- **Application interviews:** §7 → §8 → §9 → the relevant specialist guide.

- **Hands-on route:** [Core Java interview practice](lab/INTERVIEW-PATH.md).

---

## §1 Bedrock — Java identity + OOP

### Java, bytecode, and the runtime

- **Why is Java platform-independent?**

  - `javac` normally produces JVM bytecode in `.class` files.

  - A compatible JVM executes that bytecode on its own operating system and hardware.

  - The JVM itself is platform-specific.

  - **Boundary:** an older JVM cannot normally run class files targeting a newer release. Native libraries and platform assumptions also affect portability.

- **JDK vs JRE vs JVM?**

  - **JVM:** loads, verifies, and executes bytecode; manages runtime memory.

  - **JRE:** the runtime concept — JVM plus the libraries needed to run an application.

  - **JDK:** runtime plus development tools such as `javac`, `jar`, `javadoc`, and diagnostic tools.

  - **Modern packaging:** a separately installed JRE is not universal; distributions may provide a JDK or a custom runtime image.

- **What does JIT compilation do?**

  - Compiles frequently executed code into native machine code during execution.

  - Can optimize using runtime observations.

  - Warm-up and compilation costs matter when measuring short-lived programs.

- **Is Java purely object-oriented?**

  - Java has objects and primitives.

  - `int`, `boolean`, and the other primitives are not objects; wrappers support object-based APIs.

### Entry points and program arguments

- **Traditional entry point — Java 8–21, without preview features:**

  - `public static void main(String[] args)`.

  - `public`: accessible to the launcher.

  - `static`: invoked without constructing the application class.

  - `void`: no Java return value; use an exit status deliberately when needed.

  - `String... args` is an equivalent parameter declaration.

  - Overloading `main` does not make arbitrary overloads launchable.

- **Java 25+:** compact source files and instance `main` methods are permanent features.

  - The traditional entry point remains valid.

  - Launchable forms can omit `public`, `static`, or the argument array under the launcher rules.

  - **Reference:** [Oracle — compact source files and instance main methods](https://docs.oracle.com/en/java/javase/25/language/compact-source-files-instance-main-methods.html).

### Values, references, and parameter passing

- **Is Java pass-by-value?**

  - Always.

  - A primitive argument copies its primitive value.

  - An object argument copies a reference value.

  - Both references may point to the same object, so mutation can be visible to the caller.

  - Reassigning the parameter cannot reassign the caller's variable.

```java
// Java 8 — statements inside a method.
List<String> original = new ArrayList<>();
List<String> alias = original;
alias.add("paid");
alias = new ArrayList<>();
System.out.println(original); // [paid]
```

- **Reference vs object?**

  - A reference identifies an object or is `null`.

  - Two variables can refer to one object; one variable can later refer to another.

  - The object's lifetime does not end just because one local variable leaves scope.

### Access modifiers

| Modifier | Same class | Same package | Other-package subclass | Unrelated external caller |
|---|---|---|---|---|
| `private` | Yes | No | No | No |
| package access | Yes | Yes | No | No |
| `protected` | Yes | Yes | Restricted | No |
| `public` | Yes | Yes | Yes | Yes |

- **Package access:** no access-modifier keyword; `default` is not written on the declaration.

- **Protected across packages:** subclass code can access an instance member through itself or a reference of the subclass type or a subtype, not an arbitrary superclass reference.

- **Nested classes:** classes enclosed by the same top-level class may access each other's private members.

- **Top-level classes/interfaces:** `public` or package access; no `private` or `protected`.

- **Local variables:** no access modifiers; `final` is allowed.

- **Boundary:** the containing type must also be accessible; Java 9+ module exports can further restrict access.

### static, final, and object state

- **`static`:** belongs to the class, rather than each instance.

  - A static method has no `this`.

  - It can use an instance member through an explicit object reference.

  - A static field is shared by instances of that runtime class; separate defining class loaders can define separate classes.

- **`final`:** restricts reassignment, overriding, or inheritance.

  - Variable: assign once, subject to definite-assignment rules.

  - Reference: the reference stays fixed; the referenced object may remain mutable.

  - Method: cannot be overridden.

  - Class: cannot be subclassed.

```java
final List<String> names = new ArrayList<>();
names.add("Ada");              // legal: changes the object
// names = new ArrayList<>();  // compile error: reassigns the reference
```

### OOP pillars and design choices

- **Encapsulation:** keep state and its invariants behind a controlled API.

  - A setter for every field does not automatically protect invariants.

- **Abstraction:** expose the operations callers need while hiding implementation detail.

- **Inheritance:** express a valid subtype relationship and inherit permitted behavior.

- **Polymorphism:** invoke an overridden instance method through a common supertype.

- **Composition:** hold a collaborator and delegate work to it.

  - Use when behavior must vary independently or an inheritance relationship is unsuitable.

- **Substitution rule:** a subtype must honor its supertype's behavioral contract.

  - A subtype that unexpectedly rejects ordinary operations can break callers even when it compiles.

### Interface vs abstract class

- **Interface:** a contract or capability that unrelated classes can implement.

  - A class may implement multiple interfaces.

  - No constructors or per-instance fields.

  - Fields are implicitly `public static final`; their referenced objects can still be mutable.

  - Ordinary abstract methods are implicitly `public abstract`.

  - Java 8: `default` and `static` methods.

  - Java 9+: private helper methods.

- **Abstract class:** shared implementation or state within a related class family.

  - May have constructors, instance fields, protected members, and concrete methods.

  - A class can extend one class; `Object` itself has no superclass.

- **Why both?**

  - Interfaces let callers depend on capabilities without choosing a class hierarchy.

  - An abstract base can share implementation among selected implementations.

  - `is-a` and `can-do` are design heuristics; implementing an interface also creates a subtype relationship.

- **Why no multiple class inheritance?**

  - Java avoids competing superclass state, constructors, and inherited implementations.

  - Interfaces can extend multiple interfaces; default-method conflicts have explicit rules.

### Constructor injection without a framework

```java
// Java 8 — type declarations; imports: java.util.Objects.
interface Gateway {
    void debit(String account);
}

final class PaymentService {
    private final Gateway gateway;

    PaymentService(Gateway gateway) {
        this.gateway = Objects.requireNonNull(gateway);
    }

    void pay(String account) {
        gateway.debit(account);
    }
}
```

- **What this buys:** required dependencies are explicit; tests can pass a fake gateway.

- **Pattern:** composition plus dependency injection; Spring can perform the same construction.

- **Boundary:** a final dependency reference does not make the gateway's internal state thread-safe.

### Overloading, overriding, and hiding

- **Overloading:** same method name, different parameter lists.

  - Selection uses compile-time types and applicability rules.

  - Return type or `throws` clause alone cannot distinguish overloads.

  - Fixed-arity applicability without boxing is considered before boxing, then varargs applicability.

- **Overriding:** a subtype provides an implementation of an inherited instance method.

  - Runtime receiver type selects the body after compile-time method selection.

  - Cannot reduce visibility or widen the checked-exception contract.

  - A covariant reference return type is allowed.

- **Static methods:** hidden; selected using compile-time type information.

- **Private methods:** not inherited as overridable methods.

- **Final methods:** inherited when accessible, but cannot be overridden.

- **Fields:** hidden rather than dynamically dispatched.

- **Practice:** predict the field, overload, and override results in [§6](#6-trap-wall-the-code-output-classics-spoken).

### Construction and initialization

- **Constructor rules:**

  - Same simple name as the class; no return type.

  - A default constructor is supplied only when the class declares no constructor.

  - Constructors are not inherited; abstract classes can have constructors.

  - `this(...)` delegates to another constructor in the same class.

  - `super(...)` delegates to a superclass constructor.

- **Java 8–21, without preview features:** an explicit `this(...)` or `super(...)` must be the first constructor statement.

  - If neither is supplied, the compiler inserts `super()`.

  - Compilation fails if the required superclass constructor is inaccessible or absent.

- **Java 25+:** a restricted prologue may precede the explicit constructor invocation.

  - Argument validation and preparation can happen there.

  - Early-construction rules restrict use of the object under construction.

  - **Reference:** [Oracle — flexible constructor bodies](https://docs.oracle.com/en/java/javase/25/language/flexible-constructor-bodies.html).

- **Loading vs initialization:**

  - Loading obtains the class representation; initialization executes its static initialization logic.

  - Initialization occurs on specified active uses, such as constructing an instance or invoking a declared static method.

  - Reading an inlined compile-time constant need not initialize its declaring class.

- **Ordinary initialization order:**

  1. Superclass static initialization before subclass static initialization, when required.

  2. Static fields and blocks in textual order within each class, once per successful initialization.

  3. For each object: superclass construction, then subclass instance fields/blocks in textual order, then the remaining constructor body.

- **Constructor hazard:** calling an overridable method can run subclass code before subclass initialization is complete.

- **Reference:** [JLS §12 — execution and initialization](https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html).

### Object methods and equality

- **Common `Object` methods:** `equals`, `hashCode`, `toString`, `getClass`, `wait`, `notify`, and `notifyAll`.

- **`clone`:** protected on `Object`; its ordinary field copy is shallow.

  - `Cloneable` enables the `Object.clone()` mechanism; it does not declare a public cloning method.

  - Constructors and explicit copy factories often express ownership more clearly.

- **`finalize`:** historical cleanup mechanism; deprecated for removal since Java 18.

  - Execution is not guaranteed; use explicit resource ownership and try-with-resources.

- **`==` vs `equals`:**

  - Primitive `==`: compares primitive values after applicable conversions.

  - Reference `==`: compares identity.

  - `Object.equals`: identity by default; classes may override it for logical equality.

- **The full `equals` contract:**

  - Reflexive: `x.equals(x)`.

  - Symmetric: `x.equals(y)` matches `y.equals(x)`.

  - Transitive: equal links imply equal endpoints.

  - Consistent while equality-relevant state is unchanged.

  - A non-null object's `equals(null)` returns false.

- **The `hashCode` contract:**

  - Equal objects must have equal hash codes.

  - Unequal objects may have equal hash codes.

  - Keep hashing consistent while equality-relevant state is unchanged.

  - Override `equals` and `hashCode` together for value objects.

- **Inheritance trap:** extra equality fields in a subclass can break symmetry with its parent.

  - A final value class avoids that inheritance problem.

  - Choose and document equality semantics deliberately; there is no universal `instanceof` versus `getClass` recipe.

### Writing an immutable value

- **Protect construction:** validate inputs and establish invariants.

- **Protect state:** final class where appropriate, private final fields, no mutating operations.

- **Protect ownership:** copy mutable inputs and prevent mutable references escaping.

- **Protect elements:** copying a collection copies its structure, not its mutable elements.

```java
// Java 8 — type declaration; imports: java.util.*.
final class Tags {
    private final List<String> values;

    Tags(List<String> values) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    List<String> values() {
        return values;
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Tags)) return false;
        return values.equals(((Tags) other).values);
    }

    @Override public int hashCode() {
        return values.hashCode();
    }
}
```

- **Why this example works:** callers cannot change the stored list structure; its String elements are immutable.

- **Value semantics:** equality and hashing use the ordered list contents; the defensive copy keeps that state stable against changes to the caller's list.

- **Records — Java 16+:** concise data carriers with final component fields and generated value methods.

  - A mutable component can still change; records are not automatically deeply immutable.

  - Validate and defensively copy components when the contract requires a snapshot.

### Strings, wrappers, and Unicode

- **Why is `String` immutable?**

  - Safe sharing, stable values, reusable hashes, and reliable use as keys.

  - String literals are interned; `intern()` returns the canonical pooled reference.

  - `new String("a")` creates a distinct String object; use `equals` for content.

- **String vs StringBuilder vs StringBuffer?**

  - `String`: immutable value.

  - `StringBuilder`: mutable builder, suitable for local repeated concatenation.

  - `StringBuffer`: synchronized builder; a sequence of calls is not automatically one atomic action.

- **Unicode trap:** `char` is a UTF-16 code unit, not necessarily one complete Unicode character.

  - `String.length()` counts code units.

  - `codePoints()` handles Unicode code points; user-perceived characters can contain multiple code points.

- **Boxing/unboxing:** automatic conversion between primitives and wrappers.

  - Unboxing `null` throws `NullPointerException`.

  - For `Integer`, the required small-value cache covers −128 through 127; implementations can cache more.

  - Wrapper identity is unsuitable for numeric equality; handle nulls and compare values deliberately.

### Enums, annotations, reflection, and serialization

- **Enum:** a fixed set of named instances with type safety.

  - May have fields, methods, and constructors.

  - Compare enum constants with `==`; use `EnumSet` or `EnumMap` for enum-based collections.

  - Persist a stable business identifier, not `ordinal()`.

- **Annotation:** metadata consumed by a compiler, processor, framework, or reflection code.

  - `@Target` selects permitted declaration/use sites.

  - `@Retention` selects `SOURCE`, `CLASS`, or `RUNTIME`; default is `CLASS`.

  - Runtime reflection requires runtime-retained metadata.

- **Reflection:** inspect types and members, construct objects, or invoke methods dynamically.

  - Useful for frameworks, serializers, and tooling.

  - Trades compile-time checks for runtime checks and greater coupling to structure.

  - Java 9+ module boundaries can restrict deep reflection; access overrides are not universal escape hatches.

- **Marker interface vs annotation:**

  - A marker interface participates in the type system and generic bounds.

  - An annotation supplies metadata; it is not a drop-in replacement for a marker type.

- **Java object serialization:** converts supported object graphs into bytes.

  - `Serializable` is a marker; default serialization covers non-static, non-transient instance fields.

  - `serialVersionUID` participates in compatibility checks; matching it does not make arbitrary schema changes safe.

  - `transient` affects default serialization; it is not a general secrecy guarantee.

  - Treat deserialization as a trust boundary; use a suitable data format and, where needed, deserialization filters.

### Bedrock mastery and practice

- Explain mutation versus reassignment without using the phrase “pass-by-reference.”

- Choose interface, abstract class, or composition for a payment gateway and justify the choice.

- Explain why a final reference, a copied list, and a record give different guarantees.

- Predict initialization and dispatch before running the program.

- **Drills:** [Language Semantics](lab/10-core-contracts/LanguageSemantics.java) · [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java) · [Payment Routing](lab/05-oop/PaymentRoutingContracts.java) · [Immutable Snapshot](lab/10-core-contracts/ImmutableRecordSnapshot.java).

- **Optional depth:** [Reflection](lab/08-advanced/ReflectionMethodNames.java) · [Annotations](lab/08-advanced/BudgetAnnotations.java).

---

## Generics and type safety

### The problem generics solve

- **Generic type:** a type declaration with type parameters, such as `List<E>`.

- **Benefit:** express relationships between input/output types and reject incompatible uses at compile time.

- **Type parameter vs wildcard:**

  - `T`: a named type that can connect multiple positions in one declaration.

  - `?`: an unknown type; use when no name is needed for that relationship.

- **Primitives:** type arguments are reference types; use `Integer`, not `int`.

### Invariance vs array covariance

- **Generic types are invariant:** `List<Integer>` is not a subtype of `List<Number>`.

  - Otherwise a caller could insert a `Double` into an integer list.

- **Reference arrays are covariant:** `Integer[]` is a subtype of `Number[]`.

  - Arrays retain their runtime component type and check stores.

```java
Number[] numbers = new Integer[1];
// numbers[0] = 1.5; // compiles, then throws ArrayStoreException

// List<Number> values = new ArrayList<Integer>(); // compile error
```

### PECS — permitted reads and writes

| Declaration | Read as | Insert non-null values |
|---|---|---|
| `List<Number>` | `Number` | Any `Number` subtype |
| `List<? extends Number>` | `Number` | No generally safe insertion |
| `List<? super Integer>` | `Object` | `Integer` values |
| `List<?>` | `Object` | No generally safe insertion |

- **Producer extends:** read values through an upper bound.

- **Consumer super:** insert values through a lower bound.

- **Critical distinction:** wildcard bounds restrict type-safe operations; they do not freeze the collection.

  - `remove`, `clear`, and iterator removal may still work when supported.

  - `null` can be type-correct to insert through a wildcard, but the collection may reject nulls.

```java
List<? extends Number> source = new ArrayList<Integer>(Arrays.asList(1, 2));
Number first = source.get(0);
source.clear(); // legal: a wildcard does not make this list unmodifiable

List<? super Integer> target = new ArrayList<Number>();
target.add(3);
Object value = target.get(0); // Integer is not the guaranteed read type
```

### Generic methods and useful bounds

```java
// Java 8 — method declaration.
static <T> void copyInto(List<? extends T> source, List<? super T> target) {
    for (T value : source) {
        target.add(value);
    }
}
```

- **Why `<T>` comes before the return type:** it declares the method's type parameter.

- **Why both wildcards:** accept a source of subtypes and a destination capable of storing the selected type.

- **Bound:** `<T extends Number>` restricts T to Number subtypes.

- **Multiple bounds:** `<T extends Number & Comparable<T>>`; a class bound comes first.

- **Comparable bound:** `<T extends Comparable<? super T>>` also accepts ordering inherited from a supertype.

- **Wildcard capture:** a helper method can name an unknown type to safely relate operations on it.

### Erasure and runtime checks

- **Erasure:** compilation replaces type parameters with their erasures, inserts casts where needed, and may generate bridge methods.

  - Unbounded `T` erases to `Object`; a bounded parameter erases to its leftmost bound.

  - `List<String>` and `List<Integer>` do not have different runtime implementation classes merely because of their arguments.

  - Generic declaration metadata may remain available through reflection.

- **Runtime objects keep their actual classes:** erasure does not turn an Integer object into an Object-only object.

- **Consequences:**

  - `new T()` and `new T[10]` are not generally legal.

  - For an Object-typed value, `instanceof List<String>` cannot test an erased element type; `instanceof List<?>` is legal.

  - Overloads with the same erased signature clash: `m(List<String>)` and `m(List<Integer>)` cannot coexist.

### Raw types, heap pollution, and varargs

- **Raw type:** `List` without its type argument.

  - Exists for legacy interoperability; discards useful checks.

  - Prefer `List<?>` when the element type is unknown.

- **Heap pollution:** a reference of a parameterized type refers to an object that violates that type's expectation.

  - Raw writes and unsafe casts can cause failures far from the original mistake.

- **Generic varargs:** backed by arrays, so they can expose a mismatch between erased generic types and runtime array checks.

- **`@SafeVarargs`:** a promise that the implementation does not perform unsafe operations with its varargs parameter.

  - It does not repair an unsafe body.

  - Java 8: permitted on constructors and static/final methods; Java 9+: also private instance methods.

  - Apply only after checking writes and escapes of the varargs array.

### Generics mastery and practice

- Write `copyInto` from memory and explain both wildcard directions.

- Explain why an extends-bounded list can be cleared but cannot accept an arbitrary Number.

- Predict whether a mistake fails at compilation, an array store, or a later cast.

- **Drills:** [Variance and PECS](lab/10-core-contracts/GenericVariancePecs.java) · [Erasure and Safe Varargs](lab/10-core-contracts/ErasureSafeVarargs.java).

- **Reference:** [JLS §4 — types, bounds, and erasure](https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html).

---

## §2 Collections

### The framework shape

```text
Iterable
└─ Collection
   ├─ List              ArrayList, LinkedList
   ├─ Set               HashSet, LinkedHashSet, TreeSet
   └─ Queue
      ├─ PriorityQueue
      └─ Deque          ArrayDeque, LinkedList

Map                     HashMap, LinkedHashMap, TreeMap
```

- **`Collection`:** a group of elements; root of List, Set, and Queue.

- **`Map`:** key/value associations; separate from Collection.

- **`Collections`:** utility class with methods such as `sort`, `reverse`, and `unmodifiableList`.

- **Hierarchy detail:** TreeSet implements NavigableSet → SortedSet → Set; TreeMap has corresponding map interfaces.

- **LinkedList:** implements both List and Deque; the diagram shows selected implementations, not every class or inheritance edge.

- **Java 21+:** sequenced collection interfaces expose first/last elements and reversed views for defined encounter orders.

  - **Practice:** [Sequenced Collection Views](lab/13-version-features/SequencedCollectionViews.java).

### Choose by the required contract

| Need | Usual starting point | Main tradeoff |
|---|---|---|
| Index access, append, scan | `ArrayList` | Middle insertion/removal shifts elements |
| Uniqueness, no order contract | `HashSet` | Requires stable equality/hashing |
| Uniqueness plus insertion order | `LinkedHashSet` | Extra ordering bookkeeping |
| Key lookup, no order contract | `HashMap` | Expected constant-time lookup; no thread safety |
| Key lookup plus encounter order | `LinkedHashMap` | Insertion or access order |
| Sorted keys, range queries | `TreeMap` | Logarithmic key operations |
| Sorted unique values | `TreeSet` | Comparator equality determines uniqueness |
| Stack or FIFO queue | `ArrayDeque` | No null elements; not thread-safe |
| Repeated next-by-priority | `PriorityQueue` | Heap order, not sorted iteration |
| Shared concurrent key updates | `ConcurrentHashMap` | No nulls; no whole-map snapshot |
| Blocking handoff | `BlockingQueue` implementation | Capacity and wait policy must be chosen |

- **Array vs ArrayList:** arrays have fixed length and can hold primitives; ArrayList resizes and holds references.

- **ArrayList vs LinkedList:**

  - ArrayList: `get(i)` O(1); append amortized O(1); middle removal O(n).

  - LinkedList: index access O(n); end operations O(1).

  - Iterator-position insertion/removal can be O(1) after reaching that position.

  - LinkedList needs nodes and links; ArrayList often has better locality.

  - OpenJDK ArrayList commonly grows by about 1.5×; the growth factor is not an API guarantee.

- **Vector / Stack / Hashtable:** legacy synchronized types.

  - Individual synchronized methods do not make a multi-call algorithm atomic.

  - Hashtable rejects null keys and values; HashMap and ConcurrentHashMap have different null contracts.

  - Choose an implementation and coordination policy for the actual access pattern.

### HashMap — the lookup model

- **Storage:** a table of bins containing key/value nodes.

- **Hashing:** the implementation spreads the key's hash, then maps it to a table index.

- **Collision:** different keys can reach the same bin; key equality identifies the mapping within it.

- **Replacement:** putting an equal key updates the existing mapping's value.

```text
key → hashCode → spread hash → table index
                                   │
                                   └─ bin → matching key → value
```

- **OpenJDK 8 implementation details:**

  - Default capacity: 16; the ordinary default table is allocated lazily.

  - Default load factor: 0.75; resize normally occurs after size exceeds capacity × load factor.

  - Power-of-two capacity allows indexing with `(capacity - 1) & hash`.

  - Resize normally doubles capacity; entries split between the old index and old index + old capacity.

- **Treeification:**

  - `TREEIFY_THRESHOLD` is 8; ordinary `put` requests treeification while adding beyond an eight-node list bin.

  - Table capacity must be at least 64; otherwise that request resizes the table.

  - A red-black tree improves collision behavior, but a universal O(log n) lookup claim is too strong for equal-hash keys that cannot be ordered.

- **Complexity:** expected O(1) basic lookup/update with suitably distributed hashes; resize and pathological collisions cost more.

- **Nulls:** HashMap permits one null key and null values.

  - `get(k) == null` can mean absent or explicitly mapped to null.

  - `containsKey(k)` distinguishes those cases in an appropriately coordinated access pattern.

- **Thread safety:** concurrent unsynchronized mutation is unsupported; a successful run does not establish safety.

- **Reference:** [OpenJDK 8 HashMap implementation](https://raw.githubusercontent.com/openjdk/jdk8u/master/jdk/src/share/classes/java/util/HashMap.java).

### Equality, mutable keys, and sets

- **HashSet:** normally stores elements as keys in a backing HashMap.

- **Stable-key rule:** do not change equality/hash-relevant state while an object is a hash key or set member.

  - The entry stays in its original bin; a changed lookup hash can search elsewhere.

  - The object can remain visible during iteration while lookup/removal fails.

  - Prefer immutable logical keys; changing an unrelated field is a different case.

- **TreeSet / TreeMap:** comparison returning zero identifies an existing element/key.

  - Ordering inconsistent with `equals` can violate the general Set/Map contract.

  - BigDecimal is a standard example: numerical ordering ignores scale, while `equals` includes it.

- **Null-key nuance:** natural-order TreeMap rejects null keys; a comparator that supports null can allow them.

  - **Reference:** [TreeMap null-key contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html#put(K,V)).

### Ordering and comparator composition

- **Comparable:** the type supplies its natural order through `compareTo`.

- **Comparator:** a separate ordering strategy through `compare`.

- **Comparison contract:** negative, zero, or positive; not necessarily −1, 0, or 1.

  - Respect sign symmetry, transitivity, and consistent treatment of comparison ties.

  - Avoid subtraction-based comparison; subtraction can overflow.

```java
List<String> names = new ArrayList<>(Arrays.asList("Zoe", "Al", "Amy"));
Comparator<String> byLengthThenName = Comparator.comparingInt(String::length)
    .thenComparing(Comparator.naturalOrder());
names.sort(byLengthThenName);
System.out.println(names); // [Al, Amy, Zoe]
```

- **Tie-breakers:** later comparators run only when earlier keys compare equal.

- **Reversal:** calling `reversed()` after a chain reverses the whole chain; reverse one key before attaching it for mixed directions.

- **Nulls:** `nullsFirst` / `nullsLast` wrap an ordering; reversing the wrapper also reverses null placement.

- **Stable list sort:** elements comparing equal preserve their original relative order.

- **Depth:** [application comparator patterns](comparator-patterns.md) covers mixed directions, nulls, locale rules, business ranks, and ranked reports.

### Queue, Deque, and PriorityQueue

- **Queue:** holds elements for processing according to its ordering policy.

  - Many queues are FIFO; PriorityQueue is an important exception.

- **Deque:** insertion/removal at either end; supports FIFO and LIFO use.

- **Failure styles:**

| Operation | Exception form | Special-value form |
|---|---|---|
| Insert | `add(e)` | `offer(e)` |
| Remove head | `remove()` | `poll()` |
| Inspect head | `element()` | `peek()` |

- **Deque aliases:** `push` = `addFirst`; `pop` = `removeFirst`.

- **ArrayDeque:** good starting point for a local stack or queue; rejects null elements.

- **PriorityQueue:** binary heap; the head is the least element according to its ordering.

  - `offer` / `poll`: O(log n); `peek`: O(1).

  - Iterator order is not sorted; ties need not follow insertion order.

  - For top K, keep a heap whose head is the next candidate to discard.

### Views, fixed size, snapshots, and immutability

| API | Structural changes | Backing changes visible? | Null policy |
|---|---|---|---|
| `Arrays.asList(array)` | No resizing; `set` allowed | Yes, both directions | Permits null elements |
| `Collections.unmodifiableList(list)` | Through wrapper: no | Yes | Follows backing contents |
| `new ArrayList<>(list)` | Yes | No shared list structure | Permits null elements |
| `List.of(...)` — Java 9+ | No | No caller-owned backing list | Rejects nulls |
| `List.copyOf(list)` — Java 10+ | No | Snapshot of structure | Rejects nulls |

- **All shallow cases:** shared mutable element objects can still change.

- **Primitive-array trap:** `Arrays.asList(new int[]{1, 2})` produces one element — the `int[]` — not two Integers.

- **`subList`:** a backed range view, not an independent copy.

  - A structural change to the backing list outside the view makes the view's semantics undefined.

  - Copy with `new ArrayList<>(view)` when independent structure is required.

- **Map views:** `keySet`, `values`, and `entrySet` reflect backing-map changes.

  - Supported removals through those views remove mappings.

  - They do not support arbitrary insertion through `add`.

- **Depth:** [static factories, views, and snapshots](static-factory-methods.md).

### Iteration and concurrent access

- **Iterator:** forward traversal; optional `remove`.

- **ListIterator:** list traversal in both directions; optional `add`, `set`, and `remove` with state rules.

- **Fail-fast iterators:** best-effort detection of unsupported structural changes, often using a modification count.

  - `ConcurrentModificationException` is a bug signal, not a synchronization mechanism.

  - It can occur in one thread; detection is not guaranteed.

- **Removal during traversal:** use a supported `iterator.remove()` or an appropriate bulk operation such as `removeIf`.

- **CopyOnWriteArrayList iterator:** traverses a snapshot; iterator mutation operations are unsupported.

- **ConcurrentHashMap iterator:** weakly consistent; can reflect some concurrent changes without being a snapshot.

- **“Fail-safe”:** informal umbrella wording; name the actual snapshot or weak-consistency contract.

### Map update methods

- **`putIfAbsent`:** inserts when absent or mapped to null under the ordinary Map contract.

- **`computeIfAbsent`:** calls the function for absent/null mappings; a null result is not installed.

- **`computeIfPresent`:** runs for an existing non-null value; null result removes the mapping.

- **`compute`:** computes from the key and current value, possibly null; null result removes or leaves absent.

- **`merge`:** installs the supplied non-null value for an absent/null mapping; otherwise combines old and supplied values.

```java
Map<String, Integer> counts = new HashMap<>();
counts.merge("paid", 1, Integer::sum);
counts.merge("paid", 1, Integer::sum);
counts.computeIfAbsent("failed", key -> 0);
System.out.println(counts.get("paid"));   // 2
System.out.println(counts.get("failed")); // 0
```

- **Atomicity boundary:** default Map methods do not promise atomic coordination across threads.

- **ConcurrentHashMap:** provides specified atomic per-key operations; a `get`-then-`put` sequence is still not automatically atomic.

  - No null keys or values.

  - OpenJDK 8 uses CAS for empty-bin insertion and bin-level synchronization for relevant contended updates; retrievals generally do not block.

  - Keep remapping callbacks short; avoid recursive updates and slow remote calls inside them.

  - Atomic publication of a mutable value does not make later mutations of that value thread-safe.

- **Reference:** [ConcurrentHashMap contracts](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html).

### LinkedHashMap and LRU

- **Insertion order:** ordinary LinkedHashMap iteration follows insertion order.

- **Access order:** select it with the constructor flag for LRU-style behavior.

- **Eviction hook:** `removeEldestEntry` can enforce a simple entry-count limit.

- **Limits:** not inherently concurrent; expiry, memory bounds, and loading coordination require additional design.

### Collections mastery and practice

- Choose a structure from ordering, duplicates, nulls, access pattern, and concurrency requirements.

- Predict a mutable-key lookup, a TreeSet comparison tie, and a backed-view mutation.

- Explain why `computeIfAbsent` and a thread-safe map do not solve every shared-value race.

- **Drills:** [Collection Choices](lab/10-core-contracts/CollectionChoiceContracts.java) · [Hash Keys](lab/04-data-structures/HashKeyContract.java) · [Equality and Ordering](lab/10-core-contracts/EqualityOrderingContract.java).

- **Behavior drills:** [Views and Iteration](lab/04-data-structures/CollectionViewsAndIteration.java) · [Map Updates](lab/04-data-structures/MapUpdateContracts.java) · [LRU](lab/04-data-structures/AccessOrderLruCache.java) · [Navigation](lab/04-data-structures/NavigableTimeSeries.java).

---

## Numbers and money

### Integer arithmetic and conversions

- **Integral overflow:** ordinary `int` and `long` arithmetic wraps; it does not automatically throw.

- **Checked arithmetic:** `Math.addExact`, `subtractExact`, and `multiplyExact` throw on overflow.

- **Widen before calculating:** assigning an overflowing int calculation to long cannot repair the result.

```java
int count = 50_000;
long wrong = count * count;        // int multiplication overflows first
long correct = (long) count * count;
System.out.println(wrong);   // -1794967296
System.out.println(correct); // 2500000000
```

- **Promotion:** arithmetic on byte, short, and char usually promotes to int.

- **Compound assignment:** includes an implicit conversion back to the left-hand type; `b += 1` differs from `b = b + 1` for a byte.

- **Integer division:** truncates toward zero; divide-by-zero throws `ArithmeticException`.

- **Floating-point division:** can produce infinity or NaN; NaN is not equal to itself using `==`.

### BigInteger vs BigDecimal

- **BigInteger:** arbitrary-precision integer arithmetic; still limited by available resources.

- **BigDecimal:** an unscaled integer and a scale, suitable for explicitly controlled decimal arithmetic.

  - Immutable: arithmetic returns a result; it does not mutate the operands.

  - Currency, permitted scale, and rounding rules remain application decisions.

### Construct decimal amounts deliberately

```java
BigDecimal exact = new BigDecimal("0.1");
BigDecimal fromDouble = new BigDecimal(0.1);
System.out.println(exact); // 0.1
System.out.println(fromDouble.compareTo(exact) == 0); // false
```

- **String constructor:** preserves the decimal value expressed by the string.

- **Double constructor:** represents the actual binary floating-point input, which may differ from the intended decimal.

- **`BigDecimal.valueOf(double)`:** uses the double's canonical decimal string; it cannot recover precision already lost in earlier double arithmetic.

### Equality, scale, and rounding

```java
BigDecimal a = new BigDecimal("2.0");
BigDecimal b = new BigDecimal("2.00");
System.out.println(a.equals(b));          // false: scale differs
System.out.println(a.compareTo(b) == 0);  // true: numerical value matches

BigDecimal third = BigDecimal.ONE.divide(
    new BigDecimal("3"), 2, RoundingMode.HALF_EVEN);
System.out.println(third); // 0.33
```

- **Exact divide:** `1 / 3` without a suitable rounding policy throws because the decimal expansion does not terminate.

- **`setScale(2, UNNECESSARY)`:** validates that scale conversion can happen exactly; it is not a rounding policy for arbitrary inputs.

- **HALF_EVEN:** resolves exact midpoint ties toward an even retained digit; use the business-required mode.

- **Money model:** carry the currency and scale policy alongside the amount; not every currency uses two fractional digits.

- **Aggregation:** specify when rounding occurs; rounding each item can differ from rounding the total.

### Numbers mastery and practice

- Predict overflow before widening and explain a correct fix.

- Explain why HashSet and TreeSet can treat the same BigDecimal values differently.

- Define the policy for non-terminating division and invalid input scale.

- **Drills:** [Money Contracts](lab/03-big-numbers/BigDecimalMoneyContracts.java) · [BigDecimal Ordering](lab/03-big-numbers/BigDecimalOrdering.java) · [BigInteger Arithmetic](lab/03-big-numbers/BigIntegerArithmetic.java).

---

## §3 Exceptions

### Hierarchy and checked contracts

```text
Throwable
├─ Error                         unchecked
└─ Exception
   ├─ RuntimeException           unchecked
   └─ other Exception subclasses checked
```

- **Throwable:** base type of objects that can be thrown and caught.

- **Unchecked:** RuntimeException, Error, and their subclasses.

- **Checked:** every other Throwable type, including Throwable itself; callers must catch or declare checked exceptions that can escape.

- **Recoverability is a separate question:** runtime exceptions can represent operational or domain failures; checked exceptions are not automatically recoverable.

- **Error:** serious conditions from which ordinary application code is not generally expected to recover.

  - Not every Error means the JVM has collapsed; AssertionError is a useful counterexample.

  - Catch the failures your boundary can meaningfully handle; avoid a blanket catch-and-continue policy for Throwable.

- **Reference:** [JLS §11 — exception classification and checking](https://docs.oracle.com/javase/specs/jls/se25/html/jls-11.html).

### throw, throws, and propagation

- **`throw`:** raises a Throwable object at a specific execution point.

- **`throws`:** declares exceptions a method or constructor may let escape.

- **Propagation:** exceptional completion unwinds calls until a matching handler is found.

  - An uncaught exception terminates that thread after uncaught-exception handling.

  - Executor tasks can capture failures in Futures; failure visibility depends on how work is submitted and observed.

- **Boundary decision:** recover, translate while preserving cause, or propagate to an owner that can decide.

  - Logging and rethrowing at every layer creates duplicate noise.

  - An empty catch can turn a failed operation into apparent success.

### Catch order and multi-catch

- Catch a subtype before its supertype; a subtype handler after a matching supertype handler is unreachable and rejected.

- Multi-catch alternatives cannot be related by subtyping.

- A multi-catch parameter cannot be reassigned.

```java
// Java 8 — method declaration; import java.io.*.
static String readFirstLine(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        return reader.readLine();
    } catch (FileNotFoundException failure) {
        throw new IOException("Missing input: " + file, failure);
    }
}
```

- **What the example shows:** a specific failure is translated while preserving its cause; the reader is closed by its owner.

- **Encoding:** FileReader in this Java 8 example uses the platform default; choose an explicit charset for a defined file format.

### finally and try-with-resources

- **`finally`:** runs when control leaves the corresponding try/catch through normal or abrupt completion, subject to process/runtime termination.

  - A try that never completes does not reach its finally block.

  - Process termination, `Runtime.halt`, or a crash can prevent cleanup.

  - A return or exception from finally can replace an earlier return or exception; avoid it.

- **Try-with-resources — Java 7+:** closes successfully initialized AutoCloseable resources in reverse declaration order.

  - If a later resource initializer fails, earlier resources are still closed.

  - If the body throws and close also throws, the body exception remains primary and close failures are suppressed.

  - If the body succeeds but closing fails, a close failure becomes primary.

  - A resource expression evaluating to null is not closed.

- **Cause vs suppressed:**

  - Cause: the underlying failure wrapped or associated with another failure.

  - Suppressed: an additional failure preserved while another exception takes priority.

- **Java 9+:** an existing final or effectively-final resource variable can be used directly in the resource specification.

### Custom exceptions and translation

```java
// Java 8 — type declaration.
final class PaymentFailure extends RuntimeException {
    PaymentFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- **Design the contract:** choose checked or unchecked based on the API's handling expectations and consistency.

- **Preserve context:** meaningful operation identifiers and the original cause help diagnosis.

- **Translate once at the right boundary:** infrastructure failures can become domain failures; an HTTP layer can map those to response semantics.

- **Do not expose internals:** a client response and an internal diagnostic log serve different audiences.

### Overriding and checked exceptions

| Parent declaration | Override may declare |
|---|---|
| No checked exception | No new checked exception |
| `throws IOException` | IOException, a checked subtype, or none |
| `throws IOException, SQLException` | Compatible exceptions from either family, or none |

- **Restriction:** an override cannot widen the inherited checked-exception contract.

- **Unchecked exceptions:** not subject to that restriction.

- **Call-site rule:** compile-time reference type determines the checked exceptions the caller must handle.

- **Dispatch rule:** runtime receiver type determines which overridden body executes.

```java
// Java 8 — type declarations; import java.io.IOException.
class ReaderBase {
    void read() throws IOException {}
}

class ReaderChild extends ReaderBase {
    @Override void read() {}
}

// ReaderChild child = new ReaderChild(); child.read(); // no checked handling
// ReaderBase base = child; base.read();               // handle/declare IOException
```

### Interruption and class-loading failures

- **InterruptedException:** commonly signals that a blocking operation was interrupted.

  - Propagate it when your API permits.

  - If handling it at a boundary that cannot propagate, restore interrupt status with `Thread.currentThread().interrupt()` and follow the cancellation policy.

  - Restoration is not permission to keep executing the cancelled operation indefinitely.

- **ClassNotFoundException:** typically a checked failure from an explicit class-loading request such as `Class.forName`.

- **NoClassDefFoundError:** a linkage failure, including a missing runtime definition or reuse of a class whose initialization previously failed.

  - For failed initialization, inspect the earlier initialization failure as well as the later missing-definition symptom.

### Exceptions mastery and practice

- Explain compile-time checking without equating it with recoverability.

- Predict the primary and suppressed exceptions when work and cleanup both fail.

- Explain why an override can omit IOException while a base-typed caller must still handle it.

- **Drills:** [Exception Boundaries](lab/06-exceptions/ExceptionBoundaryContracts.java) · [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java) · [Resource Suppression](lab/11-io-time/ResourceSuppressionOwnership.java).

---

## I/O and resource ownership

### Bytes, characters, and buffering

- **InputStream / OutputStream:** byte-oriented I/O; appropriate for binary formats.

- **Reader / Writer:** character-oriented I/O.

- **Charset:** maps between bytes and characters; specify it at the boundary for a defined text format.

- **Buffering:** reduces many small underlying I/O operations; it does not change ownership or guarantee durability.

- **Boundary:** `flush()` pushes buffered data onward; it does not universally mean data has reached durable storage.

### Files and paths

- **Path:** a filesystem path value; it does not imply the target exists.

- **Files:** operations such as opening, copying, walking, and reading metadata.

- **Small input:** `readAllLines` can be convenient, but stores the whole result in memory.

- **Large input:** process incrementally with a reader or a resource-backed stream.

```java
// Java 8 — method declaration; imports: java.nio.file.*, java.nio.charset.*,
// java.io.IOException, java.util.stream.Stream.
static long nonBlankLines(Path path) throws IOException {
    try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
        return lines.filter(line -> !line.trim().isEmpty()).count();
    }
}
```

- **Why the try matters:** consuming a stream does not automatically close a file-backed stream.

- **Failure shape:** Files.lines can fail opening with IOException; traversal can fail with UncheckedIOException.

- **Filesystem race:** checking existence before opening does not guarantee that opening will succeed; handle the operation's failure.

### Who closes what?

- The API contract should identify the owner of an opened resource.

- A method that opens a file normally closes it.

- A method borrowing a caller-supplied stream should not silently take ownership.

- Closing a wrapper often closes its underlying resource; account for that when borrowing streams.

- Do not return a lazy file stream after closing it inside the method; either return materialized data or transfer ownership explicitly.

### Binary data and stream identity

- **Binary format:** define field order, byte order, lengths, and invalid/truncated-input behavior.

- **`read`:** may return fewer bytes than requested; `-1` signals end of stream where specified.

- **`available()`:** not a reliable total-file-size or total-message-length API.

- **I/O stream vs Stream API:** an InputStream transports bytes; a `java.util.stream.Stream<T>` describes element processing.

### I/O mastery and practice

- Choose bytes or characters from the file format, then choose the encoding explicitly.

- Explain why a terminal stream operation does not replace resource cleanup.

- Predict cleanup behavior when a wrapper, caller, and helper share a resource.

- **Drills:** [Streaming Text](lab/11-io-time/StreamingTextAnalytics.java) · [Binary Codec](lab/11-io-time/BufferedBinaryCodec.java) · [Ownership and Suppression](lab/11-io-time/ResourceSuppressionOwnership.java).

---

## §4 Java 8+ (know it cold)

- **Study companion:** [Java 8 foundations](java-8.md).

- **Release companion:** [Java version timeline](java-versions.md).

### Functional interfaces and lambdas

- **Functional interface:** one effective abstract method after inheritance and the Object-method exclusions.

  - Default/static methods do not count toward the abstract operation.

  - `@FunctionalInterface` is optional; it lets the compiler check the intended contract.

| Interface | Shape | Method |
|---|---|---|
| `Predicate<T>` | T → boolean | `test` |
| `Function<T,R>` | T → R | `apply` |
| `Supplier<T>` | () → T | `get` |
| `Consumer<T>` | T → void | `accept` |
| `UnaryOperator<T>` | T → T | `apply` |
| `BinaryOperator<T>` | (T,T) → T | `apply` |

- **Two inputs:** BiPredicate, BiFunction, and BiConsumer supply corresponding shapes.

- **Primitive specializations:** IntPredicate, IntUnaryOperator, and ToIntFunction avoid unnecessary boxing in suitable APIs.

- **Lambda:** an implementation of a target functional interface.

  - Target typing supplies the expected parameter/result contract.

  - Captured local variables must be final or effectively final.

  - A captured reference can point to a mutable object; capture does not make that object thread-safe.

  - Lambda `this` refers to the enclosing instance; an anonymous class has its own `this`.

```java
Function<Integer, Integer> addOne = n -> n + 1;
Function<Integer, Integer> doubleIt = n -> n * 2;
System.out.println(addOne.andThen(doubleIt).apply(3)); // 8
System.out.println(addOne.compose(doubleIt).apply(3)); // 7
```

- **Predicate composition:** `and` and `or` short-circuit; `negate` reverses the result.

- **Depth:** [functional interfaces](java-8.md#a-functional-interface--the-slot-a-lambda-fills) · [lambdas](java-8.md#b-lambda--the-compact-syntax).

### Method references

| Form | Example | Equivalent shape |
|---|---|---|
| Static method | `Integer::parseInt` | text → parsed int |
| Bound instance | `System.out::println` | value → print on that receiver |
| Unbound instance | `String::length` | text → text.length() |
| Constructor | `ArrayList::new` | () → new list |

- **Receiver rule:** an unbound instance reference takes the receiver from the functional interface's first argument.

- **Use:** when the reference clearly expresses one compatible invocation; a lambda is useful when arguments need additional adaptation.

- **Depth:** [method references](java-8.md#c-method-reference--shorthand-for-a-one-call-lambda).

### Streams — the execution model

- **Stream:** a sequence of elements plus a computation pipeline; it is not a storage collection.

- **Pipeline:** source → zero or more intermediate operations → terminal operation.

  - Intermediate operations are lazy; a terminal operation requests the result.

  - Processing may short-circuit; optimization can skip callbacks that do not affect the result.

  - A stream is single-use; detection of invalid reuse is not guaranteed.

- **Intermediate examples:** `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `skip`.

- **Terminal examples:** `collect`, `reduce`, `count`, `findFirst`, `anyMatch`, `forEach`.

- **Do not classify by return type alone:** `isParallel()` returns boolean but does not consume the stream.

```java
List<String> input = Arrays.asList("Ada", "Bo", "Amy");
List<String> result = input.stream()
    .filter(name -> name.length() >= 3)
    .map(name -> name.toUpperCase(Locale.ROOT))
    .sorted()
    .collect(Collectors.toList());
System.out.println(result); // [ADA, AMY]
```

- **Non-interference:** callbacks must not modify a non-concurrent source during traversal.

- **Stateless behavior:** results should not depend on changing shared state.

- **Source ownership:** ordinary transformations do not structurally mutate the collection; callbacks can still mutate referenced objects.

- **`peek`:** suitable for observation while debugging; mandatory business side effects should not depend on it executing.

- **Depth:** [streams and execution](java-8.md#d-stream-api--the-payoff).

### map, flatMap, and primitive streams

- **`map`:** transform each element into one result.

- **`flatMap`:** transform each element into a stream, then flatten the resulting streams.

- **`mapToInt` / `mapToLong` / `mapToDouble`:** primitive pipelines with numeric terminal operations.

```java
List<List<Integer>> groups = Arrays.asList(
    Arrays.asList(1, 2), Arrays.asList(3));
int total = groups.stream()
    .flatMap(List::stream)
    .mapToInt(Integer::intValue)
    .sum();
System.out.println(total); // 6
```

- **Empty inputs:** `sum` has a zero identity; `average`, `min`, and `max` may return empty optional results.

- **Encounter order:** source order, processing order, and final result order are distinct concepts.

  - `findAny` need not select the first encountered element.

  - Parallel `forEach` does not preserve encounter order; `forEachOrdered` preserves the required action order.

  - `unordered()` removes an ordering requirement; it does not shuffle the source.

### reduce vs collect

- **`reduce`:** combine values using an associative operation and a valid identity when supplied.

  - Identity must leave a value unchanged under the operation.

  - Accumulator and combiner must agree for parallel reduction.

  - Subtraction is not associative; a non-neutral identity can be counted multiple times in parallel execution.

- **`collect`:** mutable reduction into containers created, accumulated, and combined according to the collector contract.

  - Ordinary parallel collection can use isolated intermediate containers safely.

  - A mutable result container does not imply shared concurrent mutation of one container.

- **Custom collector:** supplier, accumulator, combiner, finisher, and characteristics.

  - `IDENTITY_FINISH` permits treating the accumulator as the result.

  - `CONCURRENT` permits concurrent accumulation into a shared result container under the collector execution rules.

  - `UNORDERED` removes the obligation to preserve encounter order.

### Collectors and duplicate policies

- **`groupingBy`:** group by a classification key; downstream collectors aggregate each group.

- **`partitioningBy`:** returns both Boolean partitions, including empty ones.

- **`toMap`:** choose what duplicate logical keys mean; the no-merge overload throws on duplicates.

```java
Map<String, Long> lengths = Stream.of("a", "b", "aa")
    .collect(Collectors.groupingBy(
        text -> "length-" + text.length(), Collectors.counting()));
System.out.println(lengths.get("length-1")); // 2

Map<String, Integer> votes = Stream.of("yes", "no", "yes")
    .collect(Collectors.toMap(Function.identity(), value -> 1, Integer::sum));
System.out.println(votes.get("yes")); // 2
```

- **Mutability:** `Collectors.toList()` does not guarantee a particular implementation or mutability.

  - Use `toCollection(ArrayList::new)` when a mutable ArrayList is part of the requirement.

  - Use an appropriate snapshot/wrapper when structural immutability is required.

- **Map order:** supply a suitable map factory when encounter or sorted iteration order matters.

- **Nulls:** define an input/null policy; collector behavior is not simply the backing map's null policy.

- **Depth:** [collectors](java-8.md#e-collectors--what-collect-uses).

### Optional — absence as a return contract

- **Purpose:** represent a possibly absent non-null value, primarily in return types.

- **Creation:** `of(value)` rejects null; `ofNullable(value)` converts null to empty; `empty()` represents absence.

- **Transformation:**

  - `map`: transform a present value; a null mapping result becomes empty.

  - `flatMap`: use when the function already returns Optional; returning null from that function is an error.

  - `filter`: retain the present value only when the predicate matches.

- **Exit deliberately:** fallback, throw, or perform a present-value action.

  - `orElse(expression)` evaluates its argument even when a value is present.

  - `orElseGet(supplier)` invokes the supplier only when empty.

  - Java 8 `orElseThrow` takes an exception supplier; the no-argument overload arrived in Java 10.

- **Design limits:** do not return null instead of Optional; avoid routine `isPresent()`/`get()` unwrapping when composition is clearer.

- **Depth:** [Optional](java-8.md#f-optional--maybe-absent-made-explicit).

### Default and static interface methods

- **Default methods:** let interfaces evolve with inherited implementations.

- **Resolution:**

  1. A matching concrete superclass method wins over an interface default.

  2. A more-specific interface default wins over the one it overrides.

  3. Unrelated competing defaults require an explicit override.

- **Extra boundary:** an abstract superclass method can require the class to supply an implementation; an interface default does not automatically satisfy it.

- **Explicit selection:** `InterfaceName.super.method()` can call an eligible direct superinterface's default.

- **Static interface methods:** call through the declaring interface; they are not inherited like instance defaults.

- **Depth:** [default/static methods](java-8.md#g-default--static-methods-in-interfaces).

### java.time — choose the meaning first

| Meaning | Type |
|---|---|
| Date without time | `LocalDate` |
| Wall-clock time without date | `LocalTime` |
| Local date/time without offset or zone | `LocalDateTime` |
| Point on the timeline | `Instant` |
| Date/time plus fixed UTC offset | `OffsetDateTime` |
| Date/time plus region time-zone rules | `ZonedDateTime` |
| Seconds/nanoseconds amount | `Duration` |
| Years/months/days amount | `Period` |

- **Immutability:** methods such as `plusDays` return a new value.

- **DST:** a local time can be invalid or ambiguous in a region; define the gap/overlap policy.

- **Calendar vs elapsed time:** adding one calendar day and adding 24 hours can differ across a DST transition.

- **Testable time:** inject a Clock and use a fixed clock in tests.

```java
Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
LocalDate today = LocalDate.now(clock);
System.out.println(today.plusDays(1)); // 2026-01-02
System.out.println(today);             // 2026-01-01
```

- **Legacy APIs:** Date, Calendar, and SimpleDateFormat are mutable; ordinary shared use needs coordination.

- **Depth:** [java.time](java-8.md#h-javatime--the-new-datetime-api).

### CompletableFuture — compose work and failure

- **Future:** an eventual result that callers can wait for or attempt to cancel.

- **CompletableFuture:** supports completion plus dependent processing stages.

| Operation | Choose it for |
|---|---|
| `thenApply` | Transform a completed value |
| `thenCompose` | Flatten a dependent operation returning another stage |
| `thenCombine` | Combine two independent results |
| `exceptionally` | Recover from failure with a value |
| `handle` | Transform success or failure |
| `whenComplete` | Observe the outcome; not a recovery operator |

```java
CompletableFuture<Integer> result = CompletableFuture.completedFuture(3)
    .thenApply(value -> value + 1)
    .thenCompose(value -> CompletableFuture.completedFuture(value * 2));
System.out.println(result.join()); // 8
```

- **Execution:** a non-Async continuation may run on a completion thread or another caller participating in completion.

- **Async overloads:** without an explicit executor, normally use the common ForkJoinPool; low common-pool parallelism has a thread-per-task fallback.

- **Waiting:** `get()` exposes checked waiting/execution failures; `join()` exposes exceptional completion through unchecked exceptions.

  - Both can block.

  - `get(timeout, unit)` bounds the wait; timeout does not automatically cancel the underlying operation.

- **Combination:** `allOf` waits for all inputs and returns a Void future; `anyOf` completes from the first completion, including failure.

- **Cancellation:** CompletableFuture cancellation records exceptional completion; its `mayInterruptIfRunning` argument does not control the underlying computation.

- **Depth:** [CompletableFuture](java-8.md#i-completablefuture--compose-asynchronous-stages).

### Parallel streams and executor choice

- **Good candidate:** sufficiently large, splittable, CPU-intensive work with independent operations; measure the actual workload.

- **Common-pool boundary:** parallel streams and default async futures can compete for common workers.

- **Blocking I/O:** can occupy those workers and delay unrelated work; choose an explicit, capacity-controlled execution model.

- **Shared mutable output:** a plain ArrayList updated from parallel `forEach` is unsafe.

  - Prefer a correct reduction/collector or a deliberately coordinated design.

- **Pools:**

  - Fixed pool: bounded worker count, normally an unbounded task queue.

  - Cached pool: can grow the thread count substantially.

  - Bounded ThreadPoolExecutor: explicit workers, queue, and rejection policy.

  - Java 21+: a virtual thread per blocking task can simplify concurrency; downstream capacity still needs limits.

- **Depth:** [parallel streams, pools, and blocking I/O](java-8.md#parallel-streams).

### Other Java 8 changes and version boundaries

- **Collections:** `removeIf`, `List.sort`, `replaceAll`, and Map compute/merge operations.

- **Utilities:** standard Base64, String joining, and additional Arrays helpers.

- **Concurrency:** CompletableFuture, LongAdder, and StampedLock.

- **Metadata:** repeatable/type-use annotations; `-parameters` can retain method parameter names for reflection.

- **JVM:** PermGen was removed; HotSpot class metadata moved to native-memory Metaspace, which can still be exhausted.

| Convenience | First permanent release |
|---|---:|
| `List.of`, `Map.of`, private interface helpers | 9 |
| `List.copyOf`, local-variable `var` | 10 |
| `Optional.isEmpty` | 11 |
| Switch expressions | 14 |
| Records, `Stream.toList` | 16 |
| Sealed classes | 17 |
| Virtual threads, record patterns, pattern switch, sequenced collections | 21 |
| Flexible constructor bodies, compact files/instance main | 25 |

- **`Stream.toList()` — Java 16+:** returns an unmodifiable list; its contract differs from `Collectors.toList()`.

- **LTS line through Java 25:** 8 → 11 → 17 → 21 → 25.

- **Depth:** [other Java 8 additions](java-8.md#j-other-java-8-additions-worth-knowing) · [version details](java-versions.md).

### Java 8 mastery and practice

- Choose a functional interface from its input/output shape.

- Trace a short-circuiting pipeline and state which effects are actually guaranteed.

- Handle duplicate keys, collector mutability, and empty inputs deliberately.

- Choose map/flatMap, reduce/collect, and thenApply/thenCompose from the required result shape.

- Choose a time type and an executor from the meaning and workload.

- **Drills:** [Functional Composition](lab/08-advanced/FunctionalCompositionContracts.java) · [Lazy First Match](lab/09-stream-api/LazyFirstMatch.java) · [Grouping](lab/09-stream-api/GroupingSales.java) · [Duplicate Keys](lab/09-stream-api/MergingVotes.java).

- **Follow-ups:** [Reduction](lab/09-stream-api/ReduceTransactions.java) · [Optional Workflow](lab/08-advanced/OptionalAccountWorkflow.java) · [Default Resolution](lab/08-advanced/DefaultMethodResolution.java) · [Future Execution](lab/12-concurrency-async/CompletableFutureExecution.java) · [Clock](lab/11-io-time/ClockDrivenTime.java).

---

## §5 Threads + JVM

- **Full guide:** [threads, locks, executors, and JVM](threads-jvm.md).

- **Runnable stations:** [threads lab index](lab/07-threads/README.md).

### The three concurrency questions

- **Atomicity:** can another thread observe or cause an intermediate state in a compound operation?

- **Visibility/order:** which writes are guaranteed visible, and which ordering relationships exist?

- **Liveness:** can work keep making progress without deadlock, livelock, starvation, or indefinite waiting?

- **Race example:** `count++` is a read-modify-write sequence; a volatile field does not make it atomic.

### Choose a coordination mechanism

| Mechanism | Main purpose | Boundary |
|---|---|---|
| `synchronized` | Mutual exclusion plus visibility via monitor handoff | All relevant accesses need a compatible policy |
| `volatile` | Visibility/order for a variable | Does not make arbitrary compound actions atomic |
| Atomic classes | Specified atomic updates/CAS | Several fields still need a joint invariant strategy |
| `ReentrantLock` | Explicit locking, optional interruptible/timed acquisition | Unlock in finally |
| `BlockingQueue` | Producer/consumer coordination | Choose capacity and shutdown behavior |
| `Semaphore` | Limit concurrent access | Release permits correctly |

- **Happens-before:** a specified relationship that constrains visibility and ordering.

  - Monitor unlock → later lock of that same monitor.

  - Volatile write → subsequent read of that variable.

  - Actions before `Thread.start()` → actions in the started thread.

  - Thread completion → successful return from a join on that thread.

- **Publication:** sharing a reference safely matters; immutability and synchronization rules work together.

- **Final fields:** construction has special visibility guarantees when used correctly; leaking `this` during construction undermines that design.

### Thread lifecycle and cancellation

- **`start()` vs `run()`:** start schedules a new thread; calling run directly is an ordinary method call.

- **Six states:** NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.

  - RUNNABLE includes JVM-level runnable execution; it is not a separate “running” enum state.

- **`wait()` vs `sleep()`:**

  - wait requires owning the monitor and releases that monitor while waiting.

  - sleep does not release monitors already held.

  - Check wait conditions in a loop; wakeup does not prove the condition holds.

- **Interruption:** cooperative cancellation; blocking APIs and task code must respond according to their contracts.

### Executors and bounded work

- **Executor:** separates task submission from thread creation/management.

- **Capacity choices:** workers, queue length, rejection behavior, downstream concurrency, and deadlines.

- **Lifecycle:** stop accepting work, decide how queued/running work should finish, and bound shutdown waiting.

- **Failure observation:** inspect Futures or arrange explicit completion/error handling; submission alone does not ensure failures are reported to the owner.

- **Deadlock trap:** a task waits for another task queued behind it in the same exhausted executor.

- **Virtual threads — Java 21+:** lightweight JVM-managed threads suited to many blocking tasks.

  - They do not make CPU computation faster or remove shared-state races.

  - Do not pool virtual threads to reuse them; limit scarce resources such as DB connections separately.

### JVM memory, loading, and diagnosis

- **Heap:** shared logical runtime area for objects/arrays; implementations can optimize allocation.

- **Thread stacks:** frames for method execution, local variables, and operand stacks.

- **Metaspace — HotSpot Java 8+:** native-memory storage for class metadata.

- **Class identity:** binary name plus defining class loader.

  - Same-named classes loaded by different defining loaders can be incompatible.

- **GC:** reclaims memory for unreachable objects; it does not close external resources on a reliable schedule.

  - Collectors have different latency/throughput strategies; “all GC is generational” is not a universal rule.

  - A memory leak can mean objects remain reachable although the application no longer needs them.

- **Diagnosis:**

  - Thread dump: blocked threads, lock ownership, deadlock, and waiting stacks.

  - Heap dump: retained objects and reference paths.

  - GC logs/JFR: allocation, pauses, CPU, and runtime event evidence.

  - Measure the symptom before selecting a fix.

### Threads and JVM mastery

- Explain why volatile fixes a visibility problem but not an increment race.

- Design a bounded executor lifecycle and a cooperative cancellation path.

- Identify what a thread dump can establish and what needs memory/allocation evidence.

- **Drills:** [Cancellation](lab/12-concurrency-async/CooperativeCancellation.java) · [Bounded Executor](lab/12-concurrency-async/BoundedExecutorLifecycle.java) · [Metrics Publication](lab/12-concurrency-async/ConcurrentMetricsPublication.java) · [Future Composition](lab/12-concurrency-async/CompletableFutureComposition.java).

---

## §6 Trap wall (the code-output classics, spoken)

- Predict the output, compiler error, or exception before expanding the answer.

- State the governing rule; one remembered output is not enough.

- **Run a complete Java 8 program on JDK 9+:** save it using its public class name, then compile with a release target and run the class.

```bash
javac --release 8 DispatchQuiz.java
java DispatchQuiz
```

### 1. String identity and runtime concatenation

```java
String a = "ab";
String prefix = "a";
System.out.println(a == "a" + "b");
System.out.println(a == prefix + "b");
System.out.println(a.equals(prefix + "b"));
```

<details>
<summary>Answer and rule</summary>

- Output: `true`, `false`, `true`.

- The constant expression is interned; the non-constant concatenation produces a distinct String value object.

- `equals` compares content.

</details>

### 2. Boxing and null unboxing

```java
Integer a = 127, b = 127;
Integer missing = null;
System.out.println(a == b);
System.out.println(a == 127);
try {
    int value = missing;
} catch (NullPointerException failure) {
    System.out.println("null unboxing");
}
```

<details>
<summary>Answer and rule</summary>

- Output: `true`, `true`, `null unboxing`.

- The first comparison uses cached wrapper identity; the second unboxes for a primitive comparison.

- Unboxing a null wrapper throws; mixed primitive/wrapper expressions can trigger the same problem.

</details>

### 3. Mutation versus parameter reassignment

```java
// Complete Java 8 program.
import java.util.ArrayList;
import java.util.List;

public class AliasQuiz {
    static void change(List<String> values) {
        values.add("paid");
        values = new ArrayList<>();
        values.add("failed");
    }

    public static void main(String[] args) {
        List<String> values = new ArrayList<>();
        change(values);
        System.out.println(values);
    }
}
```

<details>
<summary>Answer and rule</summary>

- Output: `[paid]`.

- Mutation reaches the shared object; reassignment changes only the copied parameter reference.

</details>

### 4. Overload selection, overrides, and fields

```java
// Complete Java 8 program.
public class DispatchQuiz {
    static class Parent {
        int id = 1;
        String choose(Object value) { return "parent-object"; }
        String name() { return "parent"; }
    }
    static class Child extends Parent {
        int id = 2;
        String choose(String value) { return "child-string"; }
        @Override String name() { return "child"; }
    }
    public static void main(String[] args) {
        Parent value = new Child();
        System.out.println(value.choose("x"));
        System.out.println(value.name());
        System.out.println(value.id);
    }
}
```

<details>
<summary>Answer and rule</summary>

- Output: `parent-object`, `child`, `1`.

- Overload selection sees Parent's methods; Child's String overload is not selected.

- Runtime dispatch selects the name override; field access uses the reference type.

</details>

### 5. Initialization happens at different frequencies

```java
// Complete Java 8 program.
public class InitQuiz {
    static class Parent {
        static { System.out.print("PS "); }
        { System.out.print("PI "); }
        Parent() { System.out.print("PC "); }
    }
    static class Child extends Parent {
        static { System.out.print("CS "); }
        { System.out.print("CI "); }
        Child() { System.out.print("CC "); }
    }
    public static void main(String[] args) {
        new Child();
        new Child();
    }
}
```

<details>
<summary>Answer and rule</summary>

- Output: `PS CS PI PC CI CC PI PC CI CC`.

- Static initialization runs once, superclass first.

- Each construction repeats parent instance initialization/constructor, then child instance initialization/constructor.

</details>

### 6. Fixed-size list and backing array

```java
String[] array = {"A", "B"};
List<String> view = Arrays.asList(array);
view.set(0, "X");
System.out.println(array[0]);
try {
    view.add("C");
} catch (UnsupportedOperationException failure) {
    System.out.println("fixed size");
}
System.out.println(Arrays.asList(new int[]{1, 2}).size());
```

<details>
<summary>Answer and rule</summary>

- Output: `X`, `fixed size`, `1`.

- Arrays.asList is backed and fixed-size, not unmodifiable; a primitive array becomes one list element.

</details>

### 7. remove by index versus value

```java
List<Integer> values = new ArrayList<>(Arrays.asList(10, 20, 30));
values.remove(1);
values.remove(Integer.valueOf(30));
System.out.println(values);
```

<details>
<summary>Answer and rule</summary>

- Output: `[10]`.

- `remove(int)` selects an index; `remove(Object)` removes a matching value.

</details>

### 8. Wildcard does not mean frozen

```java
List<? extends Number> values = new ArrayList<Integer>(Arrays.asList(1, 2));
Number first = values.get(0);
// values.add(3); // compile error if uncommented
values.clear();
System.out.println(first);
System.out.println(values.size());
```

<details>
<summary>Answer and rule</summary>

- Output: `1`, `0`.

- The upper bound permits reading as Number; it does not prohibit supported removals.

</details>

### 9. Equality versus numerical ordering

```java
BigDecimal a = new BigDecimal("2.0");
BigDecimal b = new BigDecimal("2.00");
System.out.println(new HashSet<>(Arrays.asList(a, b)).size());
System.out.println(new TreeSet<>(Arrays.asList(a, b)).size());
```

<details>
<summary>Answer and rule</summary>

- Output: `2`, `1`.

- HashSet uses equals/hashCode; TreeSet identifies elements by comparison returning zero.

</details>

### 10. Finally replaces an earlier result

```java
// Complete Java 8 program; intentionally demonstrates a bad cleanup pattern.
public class FinallyQuiz {
    static int value() {
        try { return 1; }
        finally { return 2; }
    }
    public static void main(String[] args) {
        System.out.println(value());
    }
}
```

<details>
<summary>Answer and rule</summary>

- Output: `2`.

- Abrupt completion of finally replaces the pending return; it can also discard an exception. Keep returns out of cleanup blocks.

</details>

### 11. Stream observations are not guaranteed effects

```java
long count = Stream.of(1, 2)
    .peek(value -> System.out.println("seen " + value))
    .count();
System.out.println("count=" + count);
```

<details>
<summary>Answer and rule</summary>

- Required result: `count=2`.

- Do not require the `seen` lines: an implementation can optimize away traversal when the count is known and the operations preserve size.

- Mandatory business actions need an execution contract that requires them.

</details>

### 12. A record can expose mutable state — Java 16+

```java
// Java 16+ — local record declaration and statements inside a method.
record Snapshot(List<String> values) {}
List<String> source = new ArrayList<>();
Snapshot snapshot = new Snapshot(source);
source.add("changed");
System.out.println(snapshot.values());
```

<details>
<summary>Answer and rule</summary>

- Output: `[changed]`.

- The component reference is final; the referenced list remains mutable.

- For a structural snapshot, validate and copy in the record constructor; mutable elements need their own ownership policy.

</details>

### More one-line predictions

- `x = x++`: x receives its old value after the increment side effect.

- `'a' + 1`: int value `98`; `"a" + 1`: String `"a1"`.

- `"1" + 2 + 3`: `"123"`; `1 + 2 + "3"`: `"33"`.

- `Integer.MAX_VALUE + 1`: wraps to Integer.MIN_VALUE.

- `5 / 0`: ArithmeticException; `5.0 / 0`: positive infinity.

- A local variable read before definite assignment: compile error; fields/array elements receive default values.

- Traditional colon-style switch labels can fall through; Java 14+ arrow switch rules do not fall through.

- **Drills:** [Language Semantics](lab/10-core-contracts/LanguageSemantics.java) · [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java) · [Views and Iteration](lab/04-data-structures/CollectionViewsAndIteration.java).

---

## §7 Enterprise — Spring / JDBC / REST / integration

### Spring core and dependency injection

- **IoC:** the framework/container controls object construction, wiring, and lifecycle instead of application code managing every dependency itself.

- **DI:** dependencies arrive through constructors, setters, or other injection points.

- **Bean:** an object managed by a Spring container.

- **Constructor injection:** makes required dependencies explicit and supports plain-constructor tests.

  - A single constructor normally needs no `@Autowired` annotation in Spring 4.3+.

  - Final fields prevent reassignment; collaborators still need appropriate thread-safety contracts.

- **Ambiguous dependency:** use a suitable qualifier or primary candidate; type, qualifiers, and supported name matching participate in resolution.

- **Boot vs Framework:** Boot adds conventions, dependency management, auto-configuration, starters, and application runtime conveniences.

  - Plain Spring does not require XML, an external server, or a WAR.

  - Auto-configuration uses conditions; back-off behavior depends on the particular configuration.

- **`@SpringBootApplication`:** combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`.

- **Reference:** [Spring — dependency injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html).

### Bean scopes, lifecycle, and the request path

- **Singleton:** one instance per bean definition per container; not a JVM-wide singleton guarantee.

- **Prototype:** a new instance for each container lookup/resolution; injecting it once into a singleton does not create a fresh instance per method call.

- **Web scopes:** request/session and other supported web-context scopes.

- **Lifecycle sketch:** instantiate → inject → initialization callbacks → ready for use → destruction callbacks where managed.

  - Bean post-processors and proxies participate in the complete lifecycle.

  - Prototype destruction is not automatically managed like singleton destruction.

- **Thread safety:** singleton scope does not synchronize business methods.

  - Stateless services with safely used collaborators simplify concurrent use.

  - Mutable shared state requires a deliberate concurrency policy.

- **Stereotypes:** Component is generic; Service expresses business role; Repository expresses persistence role and enables supported exception translation; RestController adds response-body handling to Controller.

- **Servlet lifecycle:** init → request handling through service → destroy.

  - A servlet instance can serve concurrent requests; avoid per-request mutable instance fields.

- **Spring MVC:** DispatcherServlet finds a handler; argument resolution, validation, conversion, and exception handling shape the request/response path.

```text
HTTP → filters → DispatcherServlet → controller → service → repository
                                    │
HTTP ← response conversion / exception handling ← result or failure
```

### Transactions — reason from the boundary

- **Usual imperative proxy model:** a caller invokes a managed proxy that applies transaction advice around the target method.

- **Transaction manager:** coordinates the participating resources; the annotation alone does not make unrelated operations atomic.

```text
caller → proxy → begin/join transaction → target method
          │
          └─ completion rules + rollback-only state → commit attempt or rollback
```

- **Self-invocation:** `this.otherMethod()` bypasses proxy interception.

  - An existing transaction can still be active.

  - The inner method's annotation does not independently apply through that call.

  - A separate injected collaborator can provide a real proxy boundary.

- **Visibility:**

  - JDK interface proxies expose public interface methods.

  - Spring 6+ class-based proxies can support protected/package-visible transactional methods.

  - Private methods cannot be intercepted by subclass overriding; final methods cannot be overridden by a class-based proxy.

- **Rollback defaults:** RuntimeException/Error trigger rollback; checked exceptions do not by default.

  - Explicit rollback rules or configured defaults can change that behavior.

  - Declare the intended business-failure policy; do not infer it from the exception's name.

- **Caught exceptions:** catching one does not guarantee commit.

  - If no failure reaches advice and no rollback-only marker exists, ordinary completion can commit.

  - An inner participating transaction may already have marked rollback-only; outer completion can raise UnexpectedRollbackException.

- **Propagation:**

  - REQUIRED: join an existing transaction or start one.

  - REQUIRES_NEW: use an independent physical transaction; outer rollback does not undo its committed work.

  - Independent work can need an additional connection and can introduce lock/pool pressure.

- **ACID:** atomicity, consistency, isolation, durability.

  - Isolation and durability depend on the database, transaction manager, configuration, and actual resources.

  - A local DB transaction does not roll back an HTTP side effect or an unrelated broker operation.

- **Thread boundary:** an imperative thread-bound transaction does not automatically follow work submitted to another executor.

- **References:** [transactional methods](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html) · [rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html) · [propagation and rollback-only](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html).

- **Depth:** [transaction boundaries and consistency](../spring-boot/spring-boot-transactions-deep.md).

### HTTP, REST, and SOAP

- **REST:** an architectural style based on resources, representations, a uniform interface, stateless interactions, and other constraints.

  - HTTP APIs often use JSON; REST does not require JSON.

- **SOAP:** an XML-based messaging protocol; WSDL commonly describes a service contract.

  - HTTP is a common transport, not the only possible transport.

- **Safe method:** intended semantics do not request a state change; incidental logging does not make GET unsafe.

- **Idempotent method:** repeating an identical request has the same intended effect as performing it once.

  - It need not return the same status or representation each time.

| Method | Typical intent | Safe by semantics? | Idempotent by semantics? |
|---|---|---|---|
| GET / HEAD | Retrieve representation / metadata | Yes | Yes |
| POST | Process submitted content | No | Not guaranteed |
| PUT | Create/replace target state | No | Yes |
| PATCH | Apply partial modification | No | Not guaranteed |
| DELETE | Remove target association | No | Yes |

- **Application idempotency:** a POST operation can add deduplication using a stable key and atomic persistence of the result.

- **Caching:** depends on method, response semantics, and cache controls.

  - GET is commonly cacheable, not universally cached.

  - POST responses can be cacheable under specified conditions, including explicit freshness and a matching Content-Location.

- **Status codes:**

  - 200: successful response; 201: created; 202: accepted for processing; 204: success with no response content.

  - 400: invalid request; 401: missing/invalid authentication credentials, with an authentication challenge where required.

  - 403: understood but refused; it does not prove the caller was authenticated.

  - 404: target not found or existence concealed; 409: conflict with current state.

  - 429: too many requests; 500: server failure; 502: invalid upstream response; 503: temporarily unavailable; 504: upstream timeout.

- **Reference:** [RFC 9110 — HTTP semantics](https://www.rfc-editor.org/rfc/rfc9110.html).

### JDBC — ownership, parameters, and results

- **Flow:** DataSource → Connection → PreparedStatement → ResultSet.

- **DataSource:** a connection factory; its implementation may be basic, pooled, or integrated with distributed transactions.

  - Closing a pooled connection normally returns the logical handle to the pool.

- **PreparedStatement:** expresses SQL with separately bound values.

  - Parameters are 1-indexed.

  - Bound values do not become SQL syntax.

  - Dynamic identifiers/order fragments still need a controlled construction policy.

  - Preparation location, plan reuse, and performance depend on the driver/database; “always faster” is not a contract.

```java
// Java 8 — method declaration; imports: java.sql.*, javax.sql.DataSource.
static int updateStatus(DataSource source, long id, String status) throws SQLException {
    String sql = "UPDATE payment SET status = ? WHERE id = ?";
    try (Connection connection = source.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, status);
        statement.setLong(2, id);
        return statement.executeUpdate();
    }
}
```

- **Ownership boundary:** this method opens and closes its connection; it does not demonstrate participation in a caller-owned Spring transaction.

  - Use Spring's transaction-aware access facilities when participating in a Spring-managed transaction.

- **Execution methods:**

  - `executeQuery`: returns a ResultSet.

  - `executeUpdate`: returns an affected-row count, or zero for statements returning no count such as supported DDL.

  - `execute`: returns whether the first result is a ResultSet; false does not mean the statement failed.

- **Manual transactions:** set auto-commit false, commit on success, roll back on failure, and preserve failure/cleanup information.

  - Multiple statements are not grouped merely because they use the same connection with auto-commit enabled.

- **Pooling:** define connection limits, acquisition waits, operation timeouts, and leak/resource ownership expectations.

- **JPA vs Hibernate:** JPA is a persistence specification; Hibernate is an implementation. ORM does not remove SQL, transaction, or fetch-performance concerns.

- **References:** [DataSource](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/javax/sql/DataSource.html) · [PreparedStatement](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html).

### Integration and distributed consistency

- **API gateway:** edge routing and shared API concerns such as authentication and rate limits.

- **ESB:** an integration approach for routing, transformation, and protocol bridging; its exact responsibilities depend on the system.

- **SOA / microservices:** service-oriented approaches with different boundary, governance, deployment, and data-ownership choices.

  - An ESB is not a mandatory definition of all SOA systems.

  - Small services alone do not establish independent ownership or deployability.

- **Saga / compensation:** a business operation spans local commits; later compensating actions address earlier effects.

  - Compensation is a new business action, not an erasure of history.

  - It can fail and need retry, deduplication, reconciliation, or manual resolution.

- **Outbox:** persist business state and an event record in one local transaction, then publish reliably with duplicate handling.

  - It coordinates DB state and publication; consumers still need appropriate processing semantics.

- **⚓ IMPS example:** receivers use Spring Boot 2.7, the application uses raw JDBC, and Kafka separates fast receipt from downstream CBS work.

  - A reversion creates a compensating posting; it does not erase the original payment posting.

  - A durable journal and recovery policy matter alongside broker delivery behavior.

### Patterns — name the varying part

| Pattern | Design question | Java-oriented example |
|---|---|---|
| Strategy | Which algorithm can change independently? | Comparator supplied to sorting |
| Factory | Who selects/constructs the implementation? | Named factory returning an interface |
| Adapter | How do incompatible APIs meet? | Wrap a vendor payment client |
| Decorator | How is behavior added through wrapping? | Buffered I/O wrappers |
| Observer | Who reacts to published changes? | Registered event listeners |
| Template method | Which inherited algorithm steps vary? | Base algorithm calling subclass hooks |
| Builder | How is a complex value assembled? | Validate staged constructor inputs |
| Singleton | What instance-lifetime scope is required? | Enum-based instance construction |

- **Static factory vs GoF Factory Method:** a named static construction method is not automatically the inheritance-based Factory Method pattern.

- **Strategy vs template method:** composition of a supplied collaborator versus inheritance and overridden hooks.

- **JdbcTemplate nuance:** callback-based customization does not require the caller to subclass a template-method base.

- **Singleton caveats:** scope depends on class loading; an enum's mutable state still needs a concurrency policy.

- **Double-checked locking:** correct publication requires the appropriate volatile reference and construction discipline.

### Build and operations vocabulary

- **Maven:** POM, dependency coordinates, lifecycle phases; `mvn verify` includes the configured verification lifecycle.

- **Gradle:** tasks, plugins, Groovy or Kotlin DSL; use the wrapper, such as `./gradlew build`, for a controlled Gradle version.

- **Reproducibility:** declare toolchain/release targets and control dependency/plugin versions.

- **Actuator:** operational endpoints for supported health, metrics, and management capabilities; exposure and authorization require configuration.

- **⚓ IMPS build:** Gradle multi-project; confirm the deployed JDK independently of source/API assumptions.

### Enterprise mastery and practice

- Explain what a proxy intercepts, which resource owns the transaction, and why a normal return may still roll back.

- Distinguish HTTP idempotency, application deduplication, and exactly-once business effects.

- Explain a JDBC result boolean without confusing it with success/failure.

- **Study:** [Spring basics](../spring-boot/spring-boot-basics.md) · [senior Spring reference](../spring-boot/spring-senior-core.md) · [JPA performance](../spring-boot/spring-data-jpa-performance.md).

- **Follow-ups:** [security](../spring-boot/spring-security-basics.md) · [resilience](../spring-boot/spring-boot-resilience.md) · [observability](../spring-boot/spring-boot-observability.md).

---

## §8 Kafka ⚓

- **Full guide:** [Kafka answer sheet](../kafka/kafka-basics.md).

- **Practice:** [Kafka exercises](../kafka/kafka-basics-exercise.md).

### The compact model

- **Topic:** a named stream divided into partitions.

- **Partition:** an ordered append-only log; offsets identify positions within that partition.

- **Consumer group:** cooperating consumers divide partition ownership; independent groups maintain independent progress.

- **Ordering:** reason within a partition, then account for producer and application-processing behavior.

- **Replay:** seek/read earlier retained records; committing an offset does not delete a record.

### Delivery is a chain of guarantees

- **Producer durability:** acknowledgements, replication/ISR policy, retries, and idempotent-producer configuration influence what reaches and survives in the log.

- **Consumer progress:** committing before or after processing changes loss/duplicate exposure around failure.

- **Business side effects:** a DB write and an offset commit are separate operations unless a specific coordination design joins the required scope.

- **Deduplication:** a stable logical identifier plus atomic state transitions can make redelivery safe.

- **Exactly-once boundary:** Kafka transactions can coordinate supported Kafka reads/writes and offsets; arbitrary external effects need their own design.

- **⚓ IMPS anchor:** Kafka decouples receipt from CBS processing; the DB journal supports deduplication and recovery decisions.

### Kafka mastery

- If processing succeeds and the consumer crashes before committing, what happens on redelivery?

- If the offset commits and the DB operation then fails, what recovery path exists?

- What is the ordering key, and can worker concurrency reorder processing after polling?

- What happens when retries exceed the operating budget: pause, retry later, dead-letter, or manual repair?

- **Depth:** use the answer sheet for the configuration and failure details behind each scenario.

---

## §9 SDLC + testing

### Delivery models and Scrum

- **SDLC:** requirements → design → implementation → verification/validation → deployment → operation and maintenance, with feedback across phases.

- **Waterfall:** sequential phases and explicit handoffs; changes still need a management process.

- **Agile:** iterative delivery and feedback; Scrum is one framework, not a synonym for all Agile work.

- **V-model:** pairs development concerns with corresponding test concerns.

- **Spiral:** risk-driven cycles of planning, evaluation, engineering, and review.

- **Scrum accountabilities:**

  - Product Owner: maximize value and manage the Product Backlog effectively.

  - Scrum Master: help establish Scrum and improve the Scrum Team's effectiveness.

  - Developers: create the usable Increment and manage their plan for achieving the Sprint Goal.

- **Events:** Sprint, Sprint Planning, Daily Scrum, Sprint Review, Sprint Retrospective.

  - Sprint: fixed length of one month or less, not necessarily two to four weeks.

  - Daily Scrum: a 15-minute event for Developers to inspect progress and adapt their plan; not a mandatory manager-status meeting.

- **Artifacts → commitments:**

  - Product Backlog → Product Goal.

  - Sprint Backlog → Sprint Goal.

  - Increment → Definition of Done.

- **Empiricism:** transparency, inspection, adaptation.

- **Values:** commitment, focus, openness, respect, courage.

- **Reference:** [The Scrum Guide](https://scrumguides.org/scrum-guide.html).

### Choose a test boundary

| Level | Main question | Representative boundary |
|---|---|---|
| Unit | Does this behavior satisfy its contract? | Plain object/function with controlled collaborators |
| Integration | Do real components cooperate correctly? | Service plus real DB, broker, or external contract stub |
| System / end-to-end | Does the deployed flow work across its relevant parts? | Running application and connected dependencies |
| Acceptance / UAT | Does it meet the agreed business need? | Representative user/business scenarios |

- **Unit tests do not require mocks:** pure functions and value objects can be tested directly.

- **Integration tests:** need realistic versions, lifecycle, isolation, and cleanup of the participating components.

- **Test pyramid:** favors much fast feedback and a smaller number of expensive broad checks; exact proportions depend on the system.

- **Coverage:** reports executed code/branches; high coverage does not prove correct assertions, complete requirements, or safe concurrency.

### A concrete Java test — JUnit Jupiter

- **Contract:** a non-negative amount incurs a 2.5% fee, rounded to two decimal places using HALF_EVEN.

- **Boundaries:** zero, ordinary input, midpoint ties in both directions, negative input, and null input.

```java
// Complete test class — Java 17+, JUnit Jupiter 6 with junit-jupiter-params.
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

public class FeeTest {
    static BigDecimal fee(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("negative amount");
        }
        return amount.multiply(new BigDecimal("0.025"))
            .setScale(2, RoundingMode.HALF_EVEN);
    }

    @ParameterizedTest
    @CsvSource({"0.00,0.00", "100.00,2.50", "1.00,0.02", "3.00,0.08"})
    void calculatesFee(String amount, String expected) {
        assertEquals(new BigDecimal(expected), fee(new BigDecimal(amount)));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> fee(new BigDecimal("-0.01")));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(NullPointerException.class, () -> fee(null));
    }
}
```

- **What these assertions catch:** incorrect percentage, missing scale, wrong midpoint rounding, and accepted invalid inputs.

- **Real application boundary:** move the calculation into the production type and call that type from the test.

- **JUnit basics:** Test, assertions, BeforeEach, parameterized sources, and lifecycle/isolation rules.

- **Runtime baseline:** JUnit 6 requires Java 17+; JUnit 5 supports Java 8+. Keep Jupiter API, engine, and parameterized-test dependencies version-aligned.

- **Mockito stubbing:** supply collaborator outcomes needed to exercise behavior.

- **Mockito verification:** check interactions when those interactions are part of the observable contract.

  - Avoid verifying every internal call; that couples tests to implementation detail.

  - A mocked database cannot establish SQL validity, transaction behavior, locking, or ORM fetch behavior.

- **Reference:** [JUnit Jupiter examples](https://docs.junit.org/5.12.1/user-guide/index.html#writing-tests-parameterized-tests) · [JUnit 6 baseline](https://docs.junit.org/6.0.3/release-notes.html).

### Test selection and vocabulary

- **Smoke:** quick broad confidence that a build's essential flows work.

- **Sanity:** a focused check of a change or affected area; teams vary in how they use the term.

- **Retest:** verify the specific defect fix.

- **Regression:** detect unintended effects on previously supported behavior, including the changed area where appropriate.

- **Black-box:** design tests from externally visible behavior/specification.

- **White-box:** use knowledge of internal structure to select tests and examine paths/branches.

- **Grey-box:** use partial internal knowledge while exercising an external boundary.

- **Functional:** what the system does; non-functional: qualities such as performance, security, and usability.

- **Verification:** evidence that specified requirements are met.

- **Validation:** evidence that the result meets the intended use and user need.

  - Whether code executes is not the defining distinction.

### Performance and security testing

- **Performance workloads:**

  - Load: expected demand.

  - Stress: behavior beyond expected capacity.

  - Spike: abrupt demand changes and recovery.

  - Soak: sustained demand and gradual resource problems.

  - Volume: behavior with large stored/input datasets.

- **Evidence:** throughput, latency distributions, errors, saturation, and recovery under a defined workload.

  - Name the workload, environment, and percentile; “fast” without conditions is not an acceptance criterion.

- **Security techniques:**

  - SAST: static source/bytecode analysis.

  - DAST: test the running application from outside.

  - SCA: inspect third-party components and known vulnerabilities.

  - IAST: runtime instrumentation during exercised flows.

  - VA/PT: assess weaknesses and validate exploitability; specific engagement scope varies.

- **Java-facing checks:** parameterized SQL, authorization, validation, safe parsing, dependency hygiene, and failure responses without sensitive internals.

### Defects, TDD, BDD, and CI/CD

- **Error → defect → failure:** human mistake → flaw in an artifact → observed incorrect behavior during execution.

- **Severity:** impact; priority: urgency/order of fixing. Ownership depends on the team's process.

- **Defect lifecycle:** reported → triaged → assigned → fixed → verified → closed; alternatives include reopened, duplicate, rejected, or deferred.

- **TDD:** failing test → minimal implementation → refactor while preserving passing tests.

- **BDD:** collaborative discovery of behavior through concrete examples; Given/When/Then is a useful expression format.

  - Cucumber/Gherkin can automate examples; BDD does not require that tool.

- **CI:** integrate changes frequently with automated build and checks.

- **Continuous delivery:** keep changes deployable; production release may require a decision.

- **Continuous deployment:** qualifying changes are released automatically under the pipeline's policy.

```text
change → build → unit checks → integration/security checks → artifact
                                                          │
              runtime feedback ← rollout/health checks ← deploy
```

- **Reproducible checks:** controlled dependencies, isolated test data, deterministic time/randomness where appropriate, and bounded waits.

- **Concurrency tests:** one successful schedule does not prove correctness; use coordination and explicit invariants instead of arbitrary sleeps.

### Testing mastery and practice

- Turn a requirement into examples before choosing a test framework.

- Explain what the fee tests establish and which production concerns they do not exercise.

- Choose a real integration boundary for persistence, transactions, authorization, or broker behavior.

- **Depth:** [production testing](../spring-boot/spring-boot-testing-deep.md) · [observability](../spring-boot/spring-boot-observability.md) · [resilience](../spring-boot/spring-boot-resilience.md).

---

## Precision card

| Prompt | Precise answer |
|---|---|
| Java parameter passing? | Values are copied, including reference values |
| Does final mean immutable? | It prevents reassignment; referenced state can remain mutable |
| What selects an overload? | Compile-time types and applicability rules |
| What selects an override? | Runtime receiver type after method selection |
| Equal objects and hashes? | Equal objects must have equal hash codes; the converse is unnecessary |
| Is an extends-bounded list read-only? | No; bounds restrict safe insertion, not supported removal |
| HashMap always constant-time? | Expected constant time with appropriate hashing; not a universal worst-case guarantee |
| TreeMap never allows null keys? | Natural ordering rejects them; a comparator can support them |
| Unmodifiable means snapshot? | No; an unmodifiable wrapper can expose backing changes |
| Checked means recoverable? | No; checked status is a type-based compiler contract |
| Does a stream terminal close a file? | No; resource-backed streams need explicit ownership/closure |
| Does Collectors.toList guarantee mutability? | No |
| Does a record guarantee deep immutability? | No |
| Is a volatile increment atomic? | No |
| Does fixed pool size bound queued work? | Not with the ordinary unbounded-queue factory |
| Does a normal transaction return guarantee commit? | No; rollback-only state and completion failures still matter |
| Does POST forbid application idempotency? | No; the application can provide deduplication semantics |
| Does an offset commit prove a DB side effect? | No; the coordination boundary must be designed |

---

## Integrated practice

### Build one transaction report

- **Starter:** [Transaction Report](lab/14-integration/TransactionReport.java).

- **Input boundary:** parse a file with defined text encoding and resource ownership.

- **Type boundary:** represent identifiers, states, currency, and exact amounts deliberately.

- **Collection boundary:** define equality, duplicate policy, grouping, ordering, and snapshots.

- **Failure boundary:** distinguish invalid input from infrastructure failure; preserve useful causes.

- **Output boundary:** deterministic ordering and protected result ownership.

- **Variation:** change the duplicate policy, add an invalid row, or introduce a tied sort key; predict the required behavior before editing.

### Evidence of understanding

- Explain a rule without reading its prepared answer.

- Predict the output or rejection of a changed example.

- Implement a representative drill without copying a prior solution.

- Add an edge case that catches a plausible mistake.

- State one alternative and its tradeoff.

- **Full route:** [essential practice and guide coverage](lab/INTERVIEW-PATH.md).
