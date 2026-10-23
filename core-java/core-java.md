# Core Java Rapid-fire Kit

## Navigation

- [§1 Bedrock — Java identity + OOP](#1-bedrock--java-identity--oop)

- [§2 Generics and type safety](#2-generics-and-type-safety)

- [§3 Collections](#3-collections)

- [§4 Numbers and money](#4-numbers-and-money)

- [§5 Exceptions](#5-exceptions)

- [§6 I/O and resource ownership](#6-io-and-resource-ownership)

- [§7 Java 8+ (know it cold)](#7-java-8-know-it-cold)

- [§8 Threads + JVM](#8-threads--jvm)

- [§9 Trap wall (the code-output classics, spoken)](#9-trap-wall-the-code-output-classics-spoken)

- [§10 Enterprise — Spring / JDBC / REST / integration](#10-enterprise--spring--jdbc--rest--integration)

- [§11 Kafka ⚓](#11-kafka-)

- [§12 SDLC + testing](#12-sdlc--testing)

- [§13 Precision card](#13-precision-card)

- [§14 Integrated practice](#14-integrated-practice)

- [Honesty guardrails](#honesty-guardrails-truth-law)

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

**Practical companion:** follow the [Core Java interview practice path](lab/INTERVIEW-PATH.md)
for language predictions, collection contracts, object design, functional Java,
exceptions, async work, and a transaction-report exercise. Its coverage map
distinguishes Java labs from the separate Spring/Kafka/testing tracks.

**⚓ The verified IMPS stack (from build files):** Spring Boot
**2.7.10** receivers · Kafka clients **3.x** · Oracle via
**ojdbc8** · Redis via **Jedis 3.7** · **Gradle** multi-project
build (NOT Maven) · JAXB + XML digital signatures for NPCI · RMI to
CBS-interface and RRN-server. ⚠️ JDK version is NOT pinned in
Gradle — check the deployment JDK before an interview ("which Java
version?" is a certainty).

**Version baseline:** unlabelled language/API examples use Java 8; later
features carry release labels. Most runnable labs use JDK 21+; Java 25 labs
have separate instructions.

**Code examples:** statements, type declarations, and schematic before/after
fragments are distinguished by context. `...` marks omitted application code;
complete standalone programs are labelled.

---

## Learning paths

- **Foundations:** §1 → §2 → §3 → §4 → §5 → §6.

- **Functional Java:** §7 → [Java 8 foundations](java-8.md) → stream and async drills.

- **Runtime reasoning:** §8 → [threads and JVM](threads-jvm.md) → §9 predictions.

- **Application interviews:** §10 → §11 → §12 → the relevant specialist guide.

- **Hands-on route:** [Core Java interview practice](lab/INTERVIEW-PATH.md).

---

## §1 Bedrock — Java identity + OOP

- **Why is Java platform-independent?** — `javac` compiles to
  bytecode; a compatible JVM runs it. "Write once, run anywhere." Follow-up
  trap: the JVM itself IS platform-specific.

- **JDK vs JRE vs JVM?**

  - JVM — executes bytecode

  - JRE — JVM + libraries (enough to *run*)

  - JDK — JRE + compiler/tools (enough to *develop*)

  - modern packaging: the JRE remains a runtime concept; a separate
    JRE installation is not universal — distributions may provide a
    JDK or a custom runtime image

  - compatibility boundary: class-file target version, native libraries,
    and operating-system assumptions still matter for portability

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

- **Why is `main` public static void?** *(traditional Java 8–21 form, without preview features)*

  - `static` — JVM calls it without creating an object

  - `public` — so JVM can reach it

  - `void` — returns nothing

  - `main` can be overloaded, but JVM only calls the standard one

  - `String... args` is equivalent to `String[] args`

  - **Java 25+:** compact source files and instance `main` methods are
    permanent; eligible launch forms can omit `public`, `static`, or
    the argument array under the launcher rules

  - **Reference:** [Java 25 entry points](https://docs.oracle.com/en/java/javase/25/language/compact-source-files-instance-main-methods.html)

- **Pass-by-value or reference?** — always pass-by-value. For
  objects, the *reference* is copied: you can mutate the object,
  you can't rebind the caller's variable.

  ```java
  // Statements inside a method; imports: java.util.*.
  List<String> original = new ArrayList<>();
  List<String> alias = original;
  alias.add("paid");
  alias = new ArrayList<>();
  System.out.println(original); // [paid]
  ```

  - two references can share one object; changing one reference does
    not change the other, and leaving a local scope need not end the
    object's lifetime

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

  - local variables: no access modifiers — `final` is allowed; scope
    is the enclosing block

  - nested-class boundary: classes enclosed by the same top-level
    class can access each other's private members

  - containing types must also be accessible; Java 9+ module exports
    can impose another boundary

- **`static`?**

  - belongs to the class, shared by all instances

  - static methods have no `this`; instance members need an explicit
    object reference.

- **`final`?**

  - variable: assign once

  - method: no override

  - class: no subclass (e.g. `String`)

  - a final reference fixes the reference, not the object's contents

  ```java
  final List<String> names = new ArrayList<>();
  names.add("Ada");              // legal: changes the object
  // names = new ArrayList<>();  // compile error: reassigns the reference
  ```

- **Four OOP pillars?**

  - encapsulation (data behind methods)

  - inheritance (is-a reuse)

  - polymorphism (one call, many forms)

  - abstraction (expose what, hide how)

- **Abstraction vs encapsulation?** — abstraction hides complexity
  behind an interface (design level); encapsulation hides data
  behind access control (implementation level).

- **Interface vs abstract class?**

  - interface — a contract/capability; no constructors or per-object
    instance state, and a class may implement many

  - abstract class — a shared base for closely related classes; may
    have instance fields, constructors, protected members, and both
    abstract and concrete methods, but a class may extend only one

  - since Java 8 interfaces may have `default`/`static` methods (and
    private helper methods since Java 9), so "interface = 100%
    abstraction" is an outdated interview answer

- **If abstract classes exist, why do we need interfaces?** — they
  solve different design problems. An abstract class says **what an
  object is** and shares state/implementation within one class family;
  an interface says **what an object can do** and lets unrelated
  classes expose the same contract.

  - Java permits only one superclass, but a class may implement many
    interfaces — it can inherit one implementation while advertising
    several independent capabilities

  - callers can depend on the interface instead of a concrete class;
    implementations can then be swapped, mocked, or injected without
    changing the caller

  - an interface does not force implementors into the same class
    hierarchy or give them unwanted state/implementation

  ```java
  interface Payable  { void pay(); }
  interface Retryable { void retry(); }

  abstract class AuditedService {       // shared state + implementation
      protected final String serviceName;

      protected AuditedService(String serviceName) {
          this.serviceName = serviceName;
      }

      protected void audit() { /* common logic */ }
  }

  class CardService extends AuditedService
          implements Payable, Retryable {
      CardService() { super("card"); }
      public void pay()   { audit(); }
      public void retry() { /* retry logic */ }
  }
  ```

  Here `CardService` **is an** `AuditedService`, while `Payable` and
  `Retryable` are independent things it **can do**. The two mechanisms
  complement each other.

  **`is-a` vs `can-do` gist:**

  - class inheritance usually models a strong **is-a** family
    relationship: `SavingsAccount extends Account` → a savings account
    is an account

  - an interface usually models a **can-do** capability:
    `Invoice implements Printable` → an invoice can be printed; an
    unrelated `Report` can implement the same capability

  - this is a design heuristic, not a type-system rule — formally, an
    implementing object **is also an instance of** its interface, so
    `Payable p = new CardService()` is valid

  - **has-a** means composition, not inheritance: a `Car` has an
    `Engine`, so keep an `Engine` field rather than extending `Engine`

  **Decision rule:** need shared mutable state, construction, or common
  protected implementation across a family → abstract class. Need a
  role/API that many possibly unrelated classes can implement, or more
  than one role → interface. Prefer composition when neither needs an
  inheritance relationship.

- **Composition + constructor injection, without a framework?**
  Required collaborators are explicit, and tests can supply fakes.

  ```java
  // Type declarations; imports: java.util.Objects.
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

  - this is **has-a** composition plus dependency injection; Spring
    can perform the same construction

  - a final collaborator reference does not make the collaborator's
    internal state thread-safe

  - **substitution rule:** a subtype must honor its supertype's
    behavioral contract; compiling is not proof of substitutability

  - **encapsulation boundary:** setters for every field do not
    automatically preserve invariants

- **Why were `default` methods introduced?** — **interface evolution**.
  Pre-8, adding an abstract method to a published interface could
  break existing implementors on recompilation. Java 8 needed `stream()` on
  `Collection` and `forEach()` on `Iterable` to make lambdas useful —
  impossible without breaking the world, so a `default` body lets an
  interface grow while old implementors keep compiling untouched.

  - `static` interface methods came along to keep factories/helpers on
    the contract itself (`List.of` since Java 9, `Comparator.comparing`) instead of
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

  - every class except `Object` has exactly one direct superclass —
    `Object` for an ordinary class when you name none — so the class
    hierarchy is a tree, not a graph

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

  - interfaces (pre-8) were safe from duplicated instance state: no
    instance fields, no constructors, no method bodies — compatible
    inherited abstract signatures can merge

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

  - boundary: an inherited abstract superclass method can still
    require an implementation; an interface default does not
    automatically satisfy it

  - static interface methods are called through the declaring
    interface; they are not inherited like instance defaults

- **Overloading vs overriding?**

  - overload: same name, different parameters, compile-time

  - override: subclass redefines, runtime dispatch; can't reduce
    visibility or throw broader checked exceptions; covariant
    return allowed

  - return type or `throws` clause alone cannot distinguish overloads

  - overload resolution considers fixed-arity applicability without
    boxing before boxing, then variable-arity applicability

  - fields are hidden too; they are not dynamically dispatched

- **Can you override static / private / final methods?** — no.
  Static methods are *hidden*, not overridden (resolved by
  reference type).

- **Constructor rules?**

  - no return type

  - compiler supplies a default only if you wrote none

  - Java 8–21, without preview features: the first statement is a
    constructor call — explicit `this()`/`super()`, or an implicit
    `super()` inserted silently if you wrote neither

  - abstract classes DO have constructors — you just can't `new`
    them

  - constructors are not inherited; the inserted `super()` must be
    accessible and available or compilation fails

  - **Java 25+:** a restricted constructor prologue can validate or
    prepare arguments before explicit `this(...)` / `super(...)`;
    early-construction rules restrict use of the object

  - **Reference:** [flexible constructor bodies](https://docs.oracle.com/en/java/javase/25/language/flexible-constructor-bodies.html)

- **Loading vs initialization — same event?** — no. Loading obtains
  the class representation; initialization executes static field
  initializers and static blocks on specified active uses.

  - superclass initialization precedes subclass initialization when
    required; fields and blocks run in textual order per class

  - reading an inlined compile-time constant need not initialize its
    declaring class

  - per object: superclass construction precedes subclass instance
    initializers and the remaining constructor body in the ordinary
    Java 8 construction flow

  - hazard: a constructor calling an overridable method can reach
    subclass code before subclass fields have been initialized

  - **Reference:** [JLS execution and initialization](https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html)

- **Interface variables/methods are implicitly…?** (MCQ staple)

  - variables: `public static final`

  - methods: `public abstract` (pre-default-methods)

- **Marker interface?** — empty interface that tags behavior:
  `Serializable`, `Cloneable`. Annotations can tag metadata, but
  marker interfaces also participate in the type system.

- **Methods every object has?** — from `Object`:

  |Method|Purpose|
  |---|---|
  |`equals`|value/reference comparison|
  |`hashCode`|bucket key for hash collections|
  |`toString`|string representation|
  |`getClass`|runtime class/reflection|
  |`clone`|shallow copy (needs `Cloneable`)|
  |`wait`|release this monitor; wait for notification, interruption, timeout, or a spurious wakeup|
  |`notify`/`notifyAll`|wake one / all waiting threads|
  |`finalize`|unreliable legacy finalization hook (deprecated for removal since Java 18)|

  `wait`/`notify`/`notifyAll` = thread coordination, inherited by
  every object for free; must hold the object's lock
  (`synchronized`) to call, else `IllegalMonitorStateException`.
  Modern code prefers `java.util.concurrent` (`BlockingQueue`,
  `ExecutorService`).

  - `clone` is protected on Object; `Cloneable` does not itself
    declare a public cloning method

  - finalization may never run; explicit resource ownership and
    try-with-resources are the cleanup tools

  - always test a wait condition in a loop; a wakeup is not proof
    that the condition is satisfied

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

  - synchronization on individual StringBuffer methods does not
    make a sequence of calls one atomic action

- **Does `char` mean one Unicode character?** — it is one UTF-16
  code unit; a code point may need two, and a user-perceived character
  may contain multiple code points.

  ```java
  String face = "\uD83D\uDE00";
  System.out.println(face.length());                       // 2 code units
  System.out.println(face.codePointCount(0, face.length())); // 1 code point
  ```

- **String pool?**

  - literals are interned and shared (in heap since Java 7)

  - `new String("a")` forces a separate object

  - `intern()` returns the pooled one

- **equals/hashCode contract?** — equal objects MUST have equal
  hashCodes (converse not required). When overriding `equals`, keep a
  compatible `hashCode`, or lookup for an equal key can miss inside
  HashMap/HashSet.

  - full equals contract: reflexive, symmetric, transitive, and
    consistent while equality-relevant state is unchanged; a
    non-null object's `equals(null)` returns false

  - unequal objects can share a hash code; equal objects must not
    disagree on it

  - inheritance trap: a subclass adding equality fields can break
    symmetry with its parent; a final value class avoids that case

  - choose equality semantics deliberately; neither `instanceof` nor
    `getClass` is a universal recipe

- **`==` vs `equals`?** — `==` compares references (or primitive
  values); `equals` compares content *if overridden* — default
  `Object.equals` IS `==`.

- **Wrapper classes / autoboxing?** — primitives ↔ objects
  automatically since Java 5; watch the Integer cache (−128…127)
  and unboxing-null NPEs (§9).

  ```java
  Integer a = 100, b = 100;
  System.out.println(a == b);   // true  — both hit the cache
  Integer x = 200, y = 200;
  System.out.println(x == y);   // do not rely on identity outside the required cache

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

  - the Range example copies list structure; its Integer elements
    are immutable. A list of mutable elements needs an element-level
    ownership policy too

  - an unmodifiable view of a **defensive copy** can avoid copying
    on every getter; an unmodifiable view of the caller's own list
    does not establish ownership

  - validate inputs and establish invariants during construction

  - **records — Java 16+:** generated value methods and final component
    fields do not make mutable components deeply immutable; copy
    components when the contract requires a snapshot

- **Enums, annotations, and reflection — what belongs where?**

  - **enum:** fixed named instances with type safety; may have fields,
    methods, and constructors. Compare constants with `==`; use
    EnumSet / EnumMap for enum-based collections

  - persist a stable business identifier, not `ordinal()`

  - **annotation:** metadata for compilers, processors, frameworks,
    or reflection. `@Target` controls use sites; `@Retention` controls
    SOURCE / CLASS / RUNTIME lifetime (default: CLASS)

  - reflection needs RUNTIME retention to read annotation metadata

  - **reflection:** inspect types, construct objects, and invoke
    members dynamically; useful for frameworks, with runtime checks
    and tighter coupling to structure

  - Java 9+ modules can restrict deep reflection; an access override
    is not a universal escape hatch

- **Serialization?** — object → byte stream (`Serializable` marker)

  - `transient` — skips a field in default serialization

  - `serialVersionUID` — guards class-version mismatch

  - static fields — not part of default serialized instance state

  - matching `serialVersionUID` does not make arbitrary schema changes
    compatible; `transient` is not a general secrecy guarantee

  - deserialization is a trust boundary; use a suitable data format
    and, where needed, deserialization filters

- **Blind-rep checks:** explain mutation vs reassignment; choose an
  interface, abstract class, or composition; distinguish a final
  reference, copied list, and record; predict initialization and dispatch.

- **Practice:** [Language Semantics](lab/10-core-contracts/LanguageSemantics.java) ·
  [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java) ·
  [Payment Routing](lab/05-oop/PaymentRoutingContracts.java) ·
  [Immutable Snapshot](lab/10-core-contracts/ImmutableRecordSnapshot.java).

- **Optional depth:** [Reflection](lab/08-advanced/ReflectionMethodNames.java) ·
  [Annotations](lab/08-advanced/BudgetAnnotations.java).

---

## §2 Generics and type safety

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

## §3 Collections

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

  - tree scope: selected Java 8 subtype relationships; abstract base
    classes are omitted. Iterable / Collection / List / Set / Queue /
    Deque / Map and the sorted/navigable types are interfaces; the
    named implementations are classes

  - extra class links: Stack extends Vector (legacy); LinkedHashSet
    extends HashSet; LinkedHashMap extends HashMap

  - linked encounter order is not sorted order; SortedSet / SortedMap
    add ordering and ranges, NavigableSet / NavigableMap add nearest
    matches such as `floor` and `ceiling`

  - **Java 21+:** sequenced collection interfaces expose first/last
    elements and reversed views for defined encounter orders

  - **Practice:** [Sequenced Collection Views](lab/13-version-features/SequencedCollectionViews.java)

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

  - ArrayList: dynamic array, O(1) index reads, grows ~1.5× in
    OpenJDK (growth factor is not an API guarantee)

  - LinkedList: doubly-linked, cheap end-ops, O(n) access

  - default choice: ArrayList

    | Op | ArrayList | LinkedList |
    | --- | --- | --- |
    | `get(i)` | O(1) | O(n) |
    | add/remove at end | O(1)* | O(1) |
    | add/remove in middle | O(n) shift | O(1)** |
    | memory per element | reference slot + spare array capacity | node with value reference + 2 links |

  \* amortized — resize copies occasionally.

  \*\* only *at the iterator's position* — reaching it is O(n),
  the trap in "LinkedList inserts are O(1)".
  In practice ArrayList wins almost everywhere — contiguous
  memory, CPU-cache friendly.

- **ArrayList vs Vector?**

  - Vector: legacy synchronized version, grows 2× by default; a
    positive `capacityIncrement` selects fixed-increment growth

  - today: use ArrayList, or a concurrent collection if threads
    are involved

- **How does HashMap work?** *(THE question)*

  - key's `hashCode` → bucket index

  - collisions chain into a linked list

  - a bucket past 8 entries requests treeification (OpenJDK 8);
    table capacity must be at least 64, otherwise it resizes

  - default capacity 16, load factor 0.75, resize doubles

  - one null key allowed

  - lookup: hash finds the bucket, `equals` picks the entry — why
    overriding `equals` requires a compatible `hashCode`

  **The mental picture:** a default HashMap allocates an array of
  16 slots ("buckets") on first use. Each slot can hold a small
  chain of entries. The trick: the key's spread hash *selects* an
  array index — direct bucket access, then search within the bin.
  Expected lookup is O(1) with suitably distributed hashes.

  ```text
  index = hash(key) & 15        // capacity 16 → mask 0..15

  [0]  → null
  [4]  → ("amit",500) → ("zoya",900)   ← collision chain
  [9]  → ("ravi",200)
  [15] → null
  ```

  The bucket positions and names above are **schematic** — they
  illustrate a collision, not the actual hashes of these String
  literals. The same schematic is used by the walkthrough below.

  **put("amit", 500), step by step:**

  1. call `"amit".hashCode()` → an int, then spread its high bits down

  2. mask into an index 0–15 → bucket 4 in the schematic above

  3. bucket empty → drop the entry there; done

  4. bucket occupied → *collision*: walk the chain — if some key
     `equals("amit")`, overwrite its value; else append a node

  5. chain grows past 8 → request a red-black tree; capacity below
     64 triggers resize first. Tree lookup is logarithmic when hash
     or key ordering directs the search; equal-hash, unordered keys
     can still require linear work

  6. size exceeds the load threshold (16 × 0.75 = 12 entries) →
     *resize*: double the array to 32, redistribute entries

  **get("amit") — and why equals matters:**

  - same index math → jump straight to bucket 4

  - but two *different* keys can share a bucket (finite slots,
    infinite keys) — so hash alone can't identify the entry

  - walk the chain, `equals` each key until it matches

  - division of labor: **hashCode = which bucket, equals = which
    entry in it.** Override `equals` without compatible `hashCode` →
    equal keys can land in different buckets → `get` can miss.
    That's the contract.

  **The numbers decoded:**

  - capacity 16 — default array length when first allocated

  - load factor 0.75 — resize trigger, not a limit: normally grow
    after size exceeds 75% of capacity; this counts entries, not
    occupied buckets, and does not prevent long collision chains

  - doubling — keeps capacity a power of 2, so index = cheap
    bit-mask of the hash

  - one null key — its hash is defined as 0, lives in bucket 0

  **The logic behind each design choice:**

  - *why hash into an array at all* — an array makes
    "position → element" cost O(1); hashing
    turns an arbitrary key into a position. Every other choice
    below exists to protect this one property.

  - *why chaining for collisions* — slots are finite, keys
    aren't, so two keys sharing a slot is unavoidable; a chain
    absorbs that without disturbing neighboring buckets, and
    deletes stay simple (unlike open addressing, which shifts
    the problem to probing).

  - *why treeify, and why exactly 8* — with a decent hashCode,
    chain lengths follow a Poisson curve: a chain of 8 is a
    ~1-in-16-million event in OpenJDK's stated model. So 8 is a
    warning — lousy hashCode, a hash-flooding attack, or bad luck —
    and the red-black tree's overhead can be worth paying. Lookup
    can drop O(n) → O(log n), but equal-hash keys without useful
    ordering can still need O(n). Rarity is the argument: trees
    cost memory, so reserve them for the pathological case.

  - *why load factor 0.75* — pure space-time bargain. Resize too
    late → long chains, slow lookups; too early → half-empty
    array, wasted memory. At 75% the *expected* chain length
    stays under ~1, so O(1) still holds statistically. 0.5 wastes
    RAM for little gain; 1.0 increases collision pressure rather
    than guaranteeing a collision.

  - *why capacity is a power of 2* — index becomes
    `hash & (n-1)`, a cheap bit-mask instead of a slow `%`.
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
  equals picks the entry; chains request treeification past 8
  (capacity ≥64); normally resize after size exceeds 75% of capacity.

  **Contract vs implementation:** the capacities, spreading, and
  treeification rules above describe OpenJDK 8. The public contract
  promises expected constant-time basic operations with suitably
  distributed hashes, not a universal worst-case time bound.
  [HashMap API](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html) ·
  [OpenJDK implementation](https://raw.githubusercontent.com/openjdk/jdk8u/master/jdk/src/share/classes/java/util/HashMap.java).

- **HashMap vs Hashtable vs ConcurrentHashMap?**

  - Hashtable: legacy, fully synchronized, no null keys or values

  - ConcurrentHashMap: fine-grained coordination (CAS + bin-level
    sync in OpenJDK 8), retrievals do not take update locks, no
    null keys or values

  - HashMap: unsynchronized, one null key + any number of
    null-valued mappings allowed; coordinate concurrent mutation

    |Type|Null keys|Null values|
    |---|---|---|
    |`HashMap`|1|many|
    |`Hashtable`|0|0|
    |`ConcurrentHashMap`|0|0|

  **CAS + per-bin sync, explained** *(pre-Java-8: coarse segment
  locks — recognize the term if an older panelist uses it)*:

  - empty bin → insert via CAS (lock-free compare-and-swap,
    no thread blocks)

  - collision → relevant updates synchronize at bin level; ordinary
    list bins use the head node, tree bins have extra coordination

  - retrievals do not acquire the update locks

  **Why no nulls (Hashtable + ConcurrentHashMap):**

  - `get(k) == null` is ambiguous — absent, or mapped to null?

  - plain HashMap disambiguates with a follow-up `containsKey(k)`

  - a concurrent map can't — another thread may mutate between
    the two calls

  - banning null makes `get() == null` always mean absent

- **HashSet internals?** — a HashMap in disguise: elements are the
  keys, value is a dummy constant.

- **What if a key changes after insertion?** — keep equality- and
  hash-relevant state stable while it is a key or set member.

  - the entry remains in its old bin; the changed hash can send
    lookups somewhere else

  - an object may still appear in iteration while `containsKey` or
    removal fails; changing unrelated fields is a different case

  - prefer immutable logical keys

  ```java
  // Type declaration; imports: java.util.*.
  final class Key {
      int id;
      Key(int id) { this.id = id; }
      @Override public int hashCode() { return id; }
      @Override public boolean equals(Object other) {
          return other instanceof Key && id == ((Key) other).id;
      }
  }
  ```

  ```java
  Map<Key, String> map = new HashMap<>();
  Key key = new Key(1);
  map.put(key, "paid");
  key.id = 2;  // mistake: changes equality/hash state after insertion
  System.out.println(map.containsKey(key)); // false with OpenJDK HashMap
  System.out.println(map.size());           // 1: entry did not disappear
  ```

- **TreeMap / TreeSet?** — red-black tree, sorted order, O(log n),
  needs `Comparable` or a `Comparator`; natural ordering rejects
  null keys/elements, but a comparator that supports null can allow them.

  - comparison returning zero defines an existing key/element;
    ordering inconsistent with equals can violate the general
    Map / Set contract — BigDecimal is the standard scale example

- **LinkedHashMap?** — HashMap + insertion order; with access-order
  plus `removeEldestEntry` it's a ready-made LRU cache.

  - an LRU hook is only an entry-count policy: concurrency, expiry,
    memory limits, and duplicate loading need additional design

- **Map updates — what do the Java 8 helpers actually do?**

  - `putIfAbsent`: inserts for absent/null mappings under ordinary Map

  - `computeIfAbsent`: absent/null value → compute; null result is
    not installed

  - `computeIfPresent`: existing non-null value → compute; null
    result removes the mapping

  - `compute`: key + current value (possibly null) → replacement;
    null result removes or leaves absent

  - `merge`: absent/null value → install supplied non-null value;
    otherwise combine old and supplied values, removing on null

  ```java
  Map<String, Integer> counts = new HashMap<>();
  counts.merge("paid", 1, Integer::sum);
  counts.merge("paid", 1, Integer::sum);
  counts.computeIfAbsent("failed", key -> 0);
  System.out.println(counts.get("paid"));   // 2
  System.out.println(counts.get("failed")); // 0
  ```

  - default Map helpers do not promise cross-thread atomicity

  - ConcurrentHashMap specifies atomic per-key operations; a
    `get`-then-`put` sequence is still not automatically atomic

  - keep remapping callbacks short; avoid recursive map updates and
    slow remote calls inside them

  - atomic installation of a mutable value does not make later
    mutations of that value thread-safe

- **Fail-fast vs fail-safe iterators?**

  - fail-fast: detects structural modification on a best-effort basis and may
    throw `ConcurrentModificationException` (ArrayList, HashMap); this is a bug
    detector, not a concurrency guarantee

  - "fail-safe" is informal: CopyOnWriteArrayList iterators traverse a snapshot;
    ConcurrentHashMap iterators are weakly consistent and may see some updates

  - common mechanism: collection keeps a `modCount`; iterator
    remembers it at creation and checks during traversal —
    detected mismatch → throw, on a best-effort basis

  - follow-up — *so how DO you remove while iterating?*
    `iterator.remove()` (updates both counts) or
    `list.removeIf(pred)`; never `list.remove()` inside for-each

  - CopyOnWriteArrayList snapshot iterators do not support iterator
    mutation operations; ConcurrentHashMap iteration is not a snapshot

  - a CME can arise within one thread; its absence proves nothing
    about thread safety

- **Iterator vs ListIterator?** *(entry classic)*

  - Iterator — forward only, any Collection,
    `hasNext`/`next`/`remove`

  - ListIterator — Lists only, both directions, plus
    `add`/`set`/`previous`/index queries

- **Read-only collections?**

  - `Collections.unmodifiableList(list)` — wrapper; writes throw
    `UnsupportedOperationException`, but the *backing* list can
    still change underneath

  - `List.of(...)` / `Map.of(...)` (Java 9) — unmodifiable collections,
    no nulls allowed; mutable elements/values can still change

  - `List.copyOf(...)` (Java 10) — an unmodifiable snapshot of collection structure;
    deep immutability also requires immutable or defensively copied elements

  - `List.copyOf` also rejects null elements; all these guarantees
    concern structure, not deep immutability

- **Fixed size vs backed view vs independent copy?**

  ```java
  String[] source = {"new", "paid"};
  List<String> fixed = Arrays.asList(source);
  fixed.set(0, "retry");
  System.out.println(source[0]); // retry: same backing array
  // fixed.add("failed");       // UnsupportedOperationException

  List<String> view = Collections.unmodifiableList(fixed);
  List<String> copy = new ArrayList<>(fixed);
  source[0] = "done";
  System.out.println(view.get(0)); // done: view observes backing changes
  System.out.println(copy.get(0)); // retry: copied list structure
  ```

  - primitive-array trap: `Arrays.asList(new int[]{1, 2})` contains
    one element — the int array — not two Integers

  - `subList` is a backed range view; changing the backing structure
    outside that view makes the view's semantics undefined

  - `new ArrayList<>(view)` gives independent list structure

  - map `keySet` / `values` / `entrySet` views reflect map changes;
    supported removals remove mappings, arbitrary `add` is unsupported

  - **Depth:** [static factories, views, and snapshots](static-factory-methods.md)

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
        private final int id;
        private final String name;
        private final long salary;

        Emp(int id, String name, long salary) {
            this.id = id;
            this.name = name;
            this.salary = salary;
        }
        String getName() { return name; }
        long getSalary() { return salary; }

        public int compareTo(Emp o) {          // natural: by id
            return Integer.compare(id, o.id);
        }
    }
    ```

    ```java
    List<Emp> emps = new ArrayList<>(Arrays.asList(
        new Emp(1, "Ada", 100), new Emp(2, "Zoe", 200)));

    emps.sort(Comparator.comparing(Emp::getSalary).reversed());          // external: by salary
    ```

  *One-liner:* Comparable = the class sorts itself, one way.
  Comparator = you hand in a sort rule, as many ways as needed.

  - comparison returns a sign, not necessarily −1/0/1; honor sign
    symmetry, transitivity, and consistent handling of ties

  - do not subtract to compare integers: subtraction can overflow

  - `reversed()` after a chain reverses the whole chain; reverse
    a single key before chaining when directions differ

  - reversing a nullsFirst/nullsLast wrapper also reverses null placement

  - list sorting is stable: comparison ties retain encounter order

  - **Depth:** [application comparator patterns](comparator-patterns.md)

- **Queue vs Deque?** — Queue: processing order follows its policy;
  many queues are FIFO, but PriorityQueue is not.
  Deque extends it with both ends open, so the same object serves
  as a queue or a stack.

  **The shape:**

  - `Queue` extends Collection — remove from the head; FIFO queues
    insert at the tail, priority queues select by priority

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
    no per-element nodes to collect

  - over `Stack`: legacy, extends Vector — synchronized stack
    operations where local use usually needs no synchronization

  - over `LinkedList`: a Node object (value + 2 pointers) per
    element — scattered memory, GC load

  - one important disqualifier: `ArrayDeque` forbids nulls (they'd be
    ambiguous with `poll`'s empty signal) — need them, take
    `LinkedList`

  *One-liner:* Queue = a head-ordering policy with polite (`offer`/`poll`) and rude
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

- **Generics?** — compile-time type safety; implemented using type
  erasure — hence no `new T[]`, and an Object-typed value cannot be
  checked with `instanceof List<String>` (use `List<?>`).

  - bounds: `<T extends Number>` — restrict what T can be

  - wildcards *(depth reserve — drill last)*: `? extends T` =
    read as T, no generally safe non-null insertion; `? super T` =
    write T, read as Object — **PECS** (Producer Extends, Consumer Super)

  - bounds do not freeze a collection: supported removals can work;
    details and examples: [§2 Generics and type safety](#2-generics-and-type-safety)

  - trap: `List<Dog>` is NOT a `List<Animal>` — generics are
    invariant; that's what wildcards are for

- **Blind-rep checks:** choose from ordering, duplicates, nulls, access
  pattern, and concurrency; predict mutable-key and view mutations;
  explain the limit of an atomic map operation around mutable values.

- **Practice:** [Collection Choices](lab/10-core-contracts/CollectionChoiceContracts.java) ·
  [Hash Keys](lab/04-data-structures/HashKeyContract.java) ·
  [Equality and Ordering](lab/10-core-contracts/EqualityOrderingContract.java).

- **Behavior drills:** [Views and Iteration](lab/04-data-structures/CollectionViewsAndIteration.java) ·
  [Map Updates](lab/04-data-structures/MapUpdateContracts.java) ·
  [LRU](lab/04-data-structures/AccessOrderLruCache.java) ·
  [Navigation](lab/04-data-structures/NavigableTimeSeries.java).

---

## §4 Numbers and money

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

- **Exact divide:** `BigDecimal.ONE.divide(new BigDecimal("3"))` throws
  `ArithmeticException` because the decimal expansion does not terminate.

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

## §5 Exceptions

- **What is an exception?** *(the opener — panels start at
  definitions)* — an event that disrupts normal program flow; in
  Java it's an *object* (a Throwable subtype) thrown at the point
  of failure and caught somewhere up the stack. Handling it can keep
  the program running instead of dying.

- **Hierarchy?** — `Throwable` → `Error` + `Exception`; `Exception`
  → checked + `RuntimeException` (unchecked).

  ```text
  Throwable
   ├─ Error                 (serious conditions — unchecked)
   │    ├─ OutOfMemoryError
   │    ├─ StackOverflowError
   │    └─ NoClassDefFoundError
   └─ Exception
        ├─ IOException, SQLException,      (checked —
        │  ClassNotFoundException           compiler enforced)
        └─ RuntimeException                (unchecked)
             ├─ NullPointerException
             ├─ IndexOutOfBoundsException
             │    └─ ArrayIndexOutOfBoundsException
             ├─ ArithmeticException
             ├─ ClassCastException
             └─ IllegalArgumentException
                  └─ NumberFormatException
  ```

  Trap: checked vs unchecked is decided by *inheritance* (belongs
  to the RuntimeException/Error families or not) — no keyword involved.

- **Checked vs unchecked?**

  - checked — compiler forces handle-or-declare

    - IOException, SQLException, ClassNotFoundException

  - unchecked — RuntimeException and Error families, no compiler demand

    - NPE, ArrayIndexOutOfBounds, ArithmeticException,
      ClassCastException, NumberFormatException,
      IllegalArgumentException

  **Why the split (the design argument):**

  - checked often represents *external* failures a correct program can still hit —
    file missing, network down, DB gone. The compiler demands a
    plan: handle or declare; it does not guarantee recoverability.

  - unchecked often represents *programming bugs* — null, bad index, bad cast.
    Possible on nearly every line; forcing declarations everywhere
    would drown the code. For those bugs, fix the cause, not just the catch.

  - rule of thumb for API design: consider whether callers should be
    required to handle a failure. Runtime exceptions can also represent
    domain or operational failures; inheritance decides the classification.

- **Exception vs Error?** *(the famous one)* — both extend
  `Throwable`, then differ in *intended application handling*:

  - Exception: application-level trouble, often recoverable —
    handle it where recovery is meaningful (IOException, SQLException, NPE)

  - Error: serious conditions ordinary application code is not generally
    expected to recover from — OutOfMemoryError (heap exhausted),
    StackOverflowError (runaway recursion). All Errors are unchecked.
    AssertionError is a counterexample to “every Error means JVM collapse.”

  - spoken contrast: *"Exceptions invite an application handling decision;
    Errors signal serious conditions."*

- **Can you catch Error or Throwable?** — legal syntax, but a blanket
  catch-and-continue policy is dangerous. After OOM/StackOverflow,
  recovery itself may fail. Catch failures your boundary can meaningfully
  handle; do not treat every Throwable as an ordinary business failure.

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

  - `finally` — block: cleanup when control leaves try/catch, subject to termination

  - `finalize` — historical finalization callback, deprecated for removal since Java 18; never rely on it

- **Does finally always run?**

  - normally — even after a `return` in try/catch

  - exceptions include process/runtime termination (`System.exit()`,
    `Runtime.halt()`, JVM crash); a try that never completes never reaches finally

  - trap: a `return` *inside* finally overrides the try's return
    AND swallows its exception (§9 trap)

- **Try / catch / finally — legal combos?** *(MCQ staple)*

  - legal: try+catch, try+finally, try+catch+finally,
    try-with-resources alone

  - a bare `try {}` with none of them → compile error

  - nested try is legal — inner catch gets first shot, unhandled
    ones climb to the outer

- **try-with-resources?** — Java 7; initialized, non-null `AutoCloseable`
  resources have close attempted automatically, reverse order, even on exception.

  ```java
  try (Connection c = ds.getConnection();
       PreparedStatement ps = c.prepareStatement(sql)) {
      ...
  }            // close attempted on ps first, then c
  ```

  - why: the old finally-close dance leaked on double faults and
    took 6 lines; this ensures each initialized resource gets a close attempt

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
  the default handler normally prints the stack trace. ⚓ in a switch that's
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

- **Can we override a method with exceptions?** *(panel favorite,
  ties to §1)* — yes. The `throws` clause is **not part of the method
  signature**, so it does not decide whether a method overrides.
  It only constrains which exceptions the override may let escape.

- **If the base method declares a checked exception, must the
  override also declare it?** — **no**. `throws IOException` means
  "this method *may* let an `IOException` escape," not "every
  implementation must throw one." The override may declare:

  - the same checked exception

  - a narrower subclass of it

  - fewer exceptions, when the parent declares several

  - no checked exception at all

  |Parent declaration|Checked declaration allowed on override|
  |---|---|
  |none|none|
  |`throws Exception`|`Exception`, any checked subtype, or none|
  |`throws IOException`|`IOException`, e.g. `FileNotFoundException`, or none|
  |`throws IOException, SQLException`|either family, both families, or none|

  The override may **not** declare a broader or unrelated checked
  exception. Unchecked exceptions (`RuntimeException`, `Error`, and
  their subclasses) are unrestricted in every row.

  **Memory rule:** the override may *shrink* the checked-exception
  contract, never widen it.

  ```java
  class Base { void read() throws IOException {} }

  // ✅ same / narrower / dropped / unchecked
  class A extends Base {
      @Override void read() throws IOException {}
  }
  class B extends Base {
      @Override void read() throws FileNotFoundException {}
  }
  class C extends Base {
      @Override void read() {}
  }
  class D extends Base {
      @Override void read() throws IllegalStateException {}
  }

  // ❌ broader or new checked — won't compile
  class E extends Base {
      @Override void read() throws Exception {}
  }
  class F extends Base {
      @Override void read() throws SQLException {}
  }
  ```

  **Why Java enforces it:** callers compile against the reference
  type's contract. If `Base.read()` promises at most `IOException`,
  a runtime `Child` implementation cannot surprise that caller with
  the broader checked `Exception`. A narrower exception is safe
  because the parent's catch already covers it.

  ```java
  class Child extends Base {
      @Override void read() {} // drops IOException — legal
  }

  static void callViaBase() throws IOException {
      Base asBase = new Child();
      asBase.read();  // must still handle/declare IOException
  }

  static void callViaChild() {
      Child asChild = new Child();
      asChild.read(); // no handling: Child's declaration is used
  }
  ```

  The object chosen at runtime decides **which body runs**; the
  reference type known at compile time decides **which checked
  exceptions the caller must handle**. An override that declares no
  checked exception may still catch checked exceptions internally,
  and it may always throw unchecked exceptions.

- **ClassNotFoundException vs NoClassDefFoundError?**

  - the Exception — checked; reflectively loading a missing class
    (`Class.forName("com.x.Missing")`)

  - the Error — linkage cannot obtain a required runtime definition
    (e.g. a jar missing from the classpath); it can also follow failed
    class initialization on a later use

  - Memory hook (missing-JAR case): *Exception = asked by name, not found; Error =
    compiled against, vanished.*

  *One-liner for the section:* Throwable splits into Error
  (serious conditions) and Exception; Error and RuntimeException families
  are unchecked, other Throwable types are checked (handle or declare).
  `throw` raises, `throws` declares, finally handles control leaving
  try/catch, try-with-resources attempts cleanup for you.


### Boundary details and practice

- **Exact checked boundary:** every Throwable type outside the RuntimeException
  and Error families is checked, including Throwable itself.

- **Multi-catch constraints:** alternatives cannot be related by subtyping;
  the multi-catch parameter cannot be reassigned.

- **Cleanup edge cases:**

  - If a later resource initializer fails, earlier initialized resources still close.

  - If the body succeeds and close fails, a close failure becomes primary.

  - A null resource expression is skipped.

  - Java 9+: an existing final/effectively-final resource variable can appear
    directly in the resource specification.

- **Cause vs suppressed — predict the output:**

  ```java
  // Complete Java 8+ program; run as SuppressionDemo.
  class SuppressionDemo {
      static final class Resource implements AutoCloseable {
          @Override public void close() {
              throw new IllegalStateException("close");
          }
      }

      public static void main(String[] args) {
          try (Resource resource = new Resource()) {
              throw new IllegalArgumentException("work");
          } catch (IllegalArgumentException failure) {
              System.out.println(failure.getMessage());                 // work
              System.out.println(failure.getSuppressed()[0].getMessage()); // close
              System.out.println(failure.getCause());                   // null
          }
      }
  }
  ```

  - A cause explains an underlying failure; suppression preserves an additional
    failure while another takes priority.

- **InterruptedException:** propagate it when the API permits; at a boundary
  that cannot propagate, restore `Thread.currentThread().interrupt()` and
  follow the cancellation policy. Restoring the flag does not mean continuing
  the cancelled operation indefinitely.

- **Task observation:** executor submission can capture a task's exception in
  its Future. Observe completion; do not assume every failed task prints through
  an uncaught-exception handler.

- **Boundary hygiene:** translate once, preserve the original cause, avoid
  empty catches, and avoid logging-and-rethrowing the same failure at every layer.

- **Mastery:** explain checking without calling it recoverability; predict
  body/initializer/close failures; explain which declaration the caller sees.

- **Drills:** [Exception Boundaries](lab/06-exceptions/ExceptionBoundaryContracts.java)
  · [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java)
  · [Resource Suppression](lab/11-io-time/ResourceSuppressionOwnership.java).

- **References:** [JLS exception contracts](https://docs.oracle.com/javase/specs/jls/se25/html/jls-11.html)
  · [try-with-resources](https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.20.3)
  · [Future observation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html).

---

## §6 I/O and resource ownership

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

## §7 Java 8+ (know it cold)

→ **[java-8.md](java-8.md)** — functional interfaces · lambdas and capture ·
method references · streams and collectors · Optional · interface defaults ·
java.time · CompletableFuture · other Java 8 additions, with code examples
and mastery checks.

- **Practice:** [Java 8 learning route](java-8.md#how-to-learn-this-guide) ·
  [interview labs](lab/INTERVIEW-PATH.md).

- **Release timeline:** [java-versions.md](java-versions.md).

*One-liner for the section:* Java 8 made behavior concise —
lambda fills a one-method interface, `::` abbreviates it,
streams pipe it, Optional boxes the maybe, defaults let
interfaces grow; LTS chant 8-11-17-21-25.

---

## §8 Threads + JVM

→ **[threads-jvm.md](threads-jvm.md)** — threads · locks · the memory
model · executors · JVM internals, ordered so that no answer depends
on one below it. Runnable companion:
[threads lab index](lab/07-threads/README.md).

*One-liner for the section:* threads share the heap but own
their stacks; `synchronized` for exclusion, `volatile` for
visibility, atomics for counters, pools instead of raw threads
(virtual threads since 21 for blocking I/O) — and the JVM cleans
up unreachable objects; collector strategies differ.

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


- **References:** [concurrent utilities and memory consistency](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html) · [executor factory queue choices](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html).

---

## §9 Trap wall (the code-output classics, spoken)

- `"a" == "a"` → true (same pooled literal); `new String("a") ==
  "a"` → false. Content: always `equals`.

- `Integer a=127, b=127; a==b` → true (required cache −128…127);
  identity outside that range is not guaranteed. `Integer == int` unboxes → value compare.

- Unboxing a null wrapper (`int x = (Integer) null`, or a
  mixed-type ternary) → NullPointerException.

- `5 / 0` → ArithmeticException; `5.0 / 0` → Infinity (IEEE double).

- `return` in finally overrides try's return and swallows its
  exception.

- **Init order?**

  - static (fields + blocks, textual order) — once, at class initialization,
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

- Colon-style `switch` can fall through without `break`; Java 14+ arrow
  switch rules do not fall through.

- Array elements default to 0 / false / null; local variables get
  NO default — declaring one unassigned is fine, but *reading* it
  before it's assigned on every path is a compile error (definite
  assignment analysis, not zeroed memory like fields/arrays).

- `0.1 + 0.2 == 0.3` → false (binary floating point can't represent
  them exactly); money → `BigDecimal`, never double.

- `s.concat("x")` / `s.replace(…)` without reassigning → `s`
  unchanged (String immutability, §1).

- `Integer.MAX_VALUE + 1` → wraps silently to `Integer.MIN_VALUE` —
  no overflow exception from ordinary int addition.

- `Arrays.asList(arr)` is fixed-size — size-changing `add`/`remove` →
  UnsupportedOperationException (`set` works).

### Prediction practice

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

- **Drills:** [Language Semantics](lab/10-core-contracts/LanguageSemantics.java) · [Compiler Contracts](lab/10-core-contracts/CompilerContracts.java) · [Views and Iteration](lab/04-data-structures/CollectionViewsAndIteration.java).

---

## §10 Enterprise — Spring / JDBC / REST / integration

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
  should fill. Resolution considers **type**, qualifiers and eligible
  candidates: exactly one suitable bean → injected; none → startup failure
  for a required dependency (optional forms include `required = false`
  where supported, or `Optional<T>`).
  Several candidates can be narrowed using `@Primary` on the default one or
  `@Qualifier("beanName")` at the injection point, or supported
  field/parameter-name matching; unresolved ambiguity →
  `NoUniqueBeanDefinitionException`.
  Constructor injection preferred: the dependency can be `final` (assigned
  during construction; collaborators still need thread-safety contracts), a missing one
  fails at startup instead of NPE-ing on first call, and the class is
  constructible in a test with plain `new` — no reflection, no
  container. Single-constructor classes don't need the annotation
  (Spring 4.3+).

- **Spring Boot vs Spring?** — not a rival framework: Boot *is*
  Spring plus opinions. Plain Spring gives you the container and
  configuration infrastructure; you choose how to assemble the runtime
  and manage compatible libraries. It does not require XML, a WAR,
  an external server, or hand-declaring every infrastructure bean.
  Boot supplies conventional defaults for those choices:

  - starters — one dependency pulls a curated, version-aligned bundle

  - auto-configuration — conditional defaults inferred from
    the classpath and other configuration; many back off for user beans
    according to their particular conditions

  - embedded servlet server (commonly Tomcat) — `java -jar`, no external server required

  - Actuator — health, metrics, env capabilities when included; exposure/security need configuration

  Same container, same beans, same annotations underneath — you can
  still override any of it.

  ⚓ IMPS receivers are Spring Boot 2.7 services.

- **Is Spring only a web framework?** — no: it includes a **container** — DI, bean
  lifecycle, transactions, configuration — and the web module is one
  branch off it, alongside data access, security, scheduling,
  messaging and batch. A Spring app with no HTTP endpoint at all is
  ordinary: batch jobs and queue consumers are Spring's home turf as
  much as REST is. ⚓ IMPS is exactly that shape — the receivers speak
  HTTP, the 7 consumers just read Kafka and never serve a request.

- **@SpringBootApplication?** *(MCQ staple)* — `@SpringBootConfiguration` +
  `@EnableAutoConfiguration` + `@ComponentScan`.

- **Servlet lifecycle?** — a servlet is the object the web container
  hands HTTP request handling to: init → service
  (doGet/doPost) → destroy. Spring MVC's DispatcherServlet is a front
  controller on top (MVC = model-view-controller).

  ```text
  request ─▶ DispatcherServlet ─▶ @Controller method ─▶ model
                    │                                     │
  response ◀─ JSON (@ResponseBody) or view template ◀─────┘
  ```

- **Bean scopes?** — singleton (default — one per bean definition per container, not the
  GoF pattern), prototype, plus request/session on the web.
  Follow-up trap: *is a singleton bean thread-safe?* Not automatically —
  one instance can serve concurrent requests; stateless design helps,
  but collaborators and mutable shared state still need a concurrency policy.

- **Bean lifecycle?** *(MCQ staple)* — container instantiates →
  injects dependencies → `@PostConstruct` init → in service →
  `@PreDestroy` on managed shutdown (a simplified managed-singleton sketch).

- **Stereotypes?**

  - `@Component` — generic managed bean

  - `@Service` — business layer

  - `@Repository` — persistence (+ supported persistence-exception translation;
    `JdbcTemplate` translates JDBC's checked `SQLException` → unchecked `DataAccessException`)

  - `@Controller` / `@RestController` — web; `@RestController` =
    `@Controller` + `@ResponseBody` (converted response bodies, often JSON)

- **@Transactional?** — callers normally hold a Spring **proxy**, not
  the raw bean. That generated stand-in is either a JDK proxy exposing
  the bean's interface or a class-based proxy subclassing the target.
  With transaction management configured, the proxy begins or joins
  the transaction, calls your method, then applies completion rules.
  You write zero `commit()` / `rollback()` for that declarative boundary.

  ```text
  caller ──▶ proxy ─── begin ──▶ your method
                       completion rules + state  ◀┐
                       commit attempt / rollback      ◀┘
  ```

  Everything a panel asks about it follows from *the proxy is on
  the outside*:

  - **self-invocation trap** — the proxy only sees calls that
    arrive **through** it. One method of the class calling
    `this.otherMethod()` goes straight to the object, so
    `@Transactional` on `otherMethod` does nothing. A `private`
    method has no proxy-visible door to wrap either. With a JDK
    interface proxy, transactional methods must be public interface
    methods; since Spring Framework 6, class-based proxies can also
    advise protected and package-visible methods. Public remains the
    portable, least-surprising service boundary. Fix self-invocation
    by putting the annotated method on another bean and injecting it.
    *Hook: the annotation is on the door; an inside call never
    uses the door.*

    ```java
    @Service
    class TransferService {

      public void transfer(Txn t) {
        doTransfer(t);              // ✗ plain this.doTransfer —
      }                             //   no new advice; existing TX can remain

      @Transactional
      public void doTransfer(Txn t) { ... }
    }

    // fix: cross a bean boundary so the call goes through a proxy
    @Service
    class TransferService {
      private final LedgerService ledger;   // injected bean

      TransferService(LedgerService ledger) {
        this.ledger = ledger;
      }

      public void transfer(Txn t) {
        ledger.doTransfer(t);       // ✓ proxied call
      }
    }
    ```

  - **swallowed-exception trap** — catch the exception inside the
    method and the proxy sees a normal return, so it can **commit**
    if nothing has marked rollback-only. Rethrow under an appropriate
    rollback rule, or mark the transaction rollback-only.

    ```java
    @Transactional
    public void pay(Txn t) {
      ledger.debit(t);
      try {
        npci.send(t);
      } catch (SendException e) {
        log.error("send failed", e);   // ✗ swallowed → the debit
      }                                //   can commit if not rollback-only
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

  - **rollback rules** — by default rolls back on **unchecked**
    (`RuntimeException`, `Error`); a **checked** exception does not
    request rollback by default. `rollbackFor = Exception.class`
    changes that rule; other explicit or configured rules can too.
    This is framework policy, not proof that unchecked means bug or
    checked means handled. *Hook: declare the business rollback policy.*

    ```java
    @Transactional
    public void a() throws IOException { save(); throw new IOException(); }
    // checked → commit attempt under default rules, unless rollback-only.

    @Transactional(rollbackFor = Exception.class)
    public void b() throws IOException { save(); throw new IOException(); }
    // → rolls back

    @Transactional
    public void c() { save(); throw new IllegalStateException(); }
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

  - **ACID**, the database transaction property set (with the right participating resources/configuration) —
    **atomicity** (all the statements land or none do),
    **consistency** (valid state to valid state, constraints
    intact), **isolation** (controls permitted interference
    between concurrent transactions — guarantees depend on the selected
    level), **durability** (committed work survives failures within the
    database's configured durability guarantees).

  - Scope note: this local transaction covers its participating
    database work. An unrelated HTTP/broker side effect does not roll back
    with it — that is where the saga in this section's pattern list takes over.

- **Actuator?** — production endpoints: commonly /actuator/health,
  /actuator/metrics — the monitoring surface, subject to configured
  exposure and authorization. (Support-role JD language — use it.)

- **REST?** — resources + HTTP verbs, stateless. GET read, POST
  process submitted content (often create), PUT create/replace, PATCH partial, DELETE remove. Idempotent =
  repeating the request leaves the server in the same state (not:
  returns the same response) — GET/HEAD/PUT/DELETE by semantics;
  POST/PATCH are not guaranteed idempotent.

- **GET vs POST?** *(the famous one)*

  - GET — requests a representation; commonly uses URL parameters,
    is safe/idempotent, and responses can be cached under cache rules

  - POST — processes submitted content, commonly in the body; not
    inherently idempotent. Responses can be cacheable under specified
    conditions; do not assume “POST can never be cached.”

- **Status codes?** — 200 OK · 201 Created · 204 No Content · 400
  Bad Request · 401 Unauthorized · 403 Forbidden · 404 Not Found ·
  409 Conflict · 500 Internal Server Error · 502/503
  upstream/unavailable.
  The 401/403 pair is the trap: 401 is *named* Unauthorized but
  *means* missing/invalid authentication credentials; 403 means
  understood but refused, and does not prove authentication succeeded.
  Memory hook: *401 = “credentials needed”; 403 = “request refused.”*

- **REST vs SOAP?** — both commonly appear in remote HTTP services;
  they differ in what the contract is made of. SOAP can use other transports.

  - **REST** — *representational state transfer*: resource-oriented
    architectural constraints; HTTP APIs often use JSON, but REST does
    not require JSON. An architectural style, not a protocol.

  - **SOAP** — *simple object access protocol*: XML envelope +
    often a **WSDL** (web services description language) contract file
    and WS-Security. An actual protocol, with XML/schema-based
    validation.

  - Memory hook: *REST organizes resources and representations;
    SOAP standardizes XML messages.*

  - ⚓ NPCI speaks signed XML over HTTPS — I've lived the XML+PKI
    world

- **JDBC flow?** — DataSource (where connections come from — basic,
  pooled, or distributed-transaction implementations) → Connection → PreparedStatement →
  ResultSet; close in reverse (try-with-resources snippet: §5).
  (Older-panel keyword: driver *types 1–4*; type 4 = pure-Java
  thin driver — Oracle JDBC in thin mode, available through ojdbc8.)

- **executeQuery vs executeUpdate vs execute?** *(MCQ staple)*

  - `executeQuery` → ResultSet (SELECT)

  - `executeUpdate` → int rows affected (INSERT/UPDATE/DELETE),
    or zero for statements returning no row count, such as supported DDL

  - `execute` → boolean: true if the first result is a ResultSet,
    false for an update count or no result — not a success/failure flag

- **Statement vs PreparedStatement?** — reusable SQL + bound parameters:
  bound values are treated as data rather than SQL syntax. Dynamic
  table/column/order fragments still need controlled construction;
  preparation and performance depend on driver/database behavior. Parameters
  are **1-indexed** —
  ⚓ I once shipped `setString(0, …)` in IMPS's duplicate check;
  it got erased days later when the check moved to the receiver.
  Honest bug story if "a bug you shipped?" comes.

- **Connection pooling?** — connections are expensive; pools reuse
  them (Boot prefers HikariCP when available). ⚓ IMPS's CBS-interface runs a
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

  - SOA (a common 2000s enterprise style) — a few coarse services wired
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
  or Kotlin DSL, flexible; performance depends on the build). ⚓ IMPS is a **Gradle** multi-project.

- **Design patterns you know?**

  - singleton — private constructor + static instance; enum is
    the safe form; double-checked locking needs `volatile` or
    another thread can see the reference assigned before the
    constructor has finished ([threads-jvm.md §6](threads-jvm.md#6-without-a-lock--volatile-and-atomics))

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
    steps in a method (often `final`) and leaves the varying steps to
    subclasses. Spring's `JdbcTemplate` also owns a JDBC workflow (get
    connection, execute, translate exceptions, close), but customizes it
    through callbacks such as `RowMapper`, not required subclassing.
    *Hook: the base owns the recipe,
    the subclass fills in the ingredients.*

  - strategy vs template method, if they push: strategy varies
    behaviour by **composition** at runtime (hand in another
    object), template method varies it by **inheritance**
    (override a hook; dispatch is still at runtime). Same goal, different lever.

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
      consumer does the money leg, so a CBS outage can delay a
      reversion; avoiding loss also needs durable publication, retries,
      and recovery. The direction is read off
      the ReqPay message id (our own bank-code prefix = outward).

  *One-liner for the section:* Spring's container wires the beans,
  `@Transactional` guards the DB work, the servlet front controller
  routes REST verbs to status codes at the edge, JDBC talks to
  Oracle through pooled prepared statements — and the gateway
  guards that edge while the ESB integrates the inside.


### Boundary details and practice

- **Bean follow-ups:**

  - Injecting a prototype once into a singleton does not make a fresh instance
    appear on each method call; use an explicit lookup/provider strategy.

  - Prototype destruction is not automatically managed like singleton destruction.

  - Post-processors and proxies participate in the complete lifecycle.

  - **Reference:** [Spring scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html).

- **Transaction example assumptions:** the snippets above are independent
  service sketches, not one compilable class. Calls cross an enabled managed
  proxy; `PaymentFailedException` is unchecked, and the participating JDBC work
  uses the same transaction manager/resource. The simple rollback examples use
  default rules and start without an existing transaction.

  - Self-invocation does not cancel an existing transaction; it bypasses the
    invoked method's transaction advice.

  - An inner REQUIRED scope can mark rollback-only, causing an outer normal
    completion to raise `UnexpectedRollbackException`.

  - REQUIRES_NEW can need another connection while the outer connection remains
    held; account for pool exhaustion and locks.

  - Private/final methods cannot be intercepted by class-based overriding;
    final classes cannot be subclass-proxied.

  - An imperative thread-bound transaction does not automatically follow work
    submitted to another executor.

  - **References:** [transactional methods](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
    · [rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html)
    · [propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html).

- **HTTP follow-ups:**

  - Safe semantics do not request a state change; incidental logging does not
    make GET unsafe. HEAD is also safe/idempotent.

  - A POST business operation can implement deduplication with a stable key and
    atomic persistence; method semantics alone do not provide that design.

  - GET is not guaranteed to be cached. POST-response caching requires specified
    conditions, including explicit freshness and matching Content-Location.

  - Additional status codes: **202** accepted for processing; **429** too many
    requests; **504** upstream timeout. **401** uses the required authentication
    challenge; **404** can also conceal a resource's existence.

  - **Reference:** [HTTP semantics](https://www.rfc-editor.org/rfc/rfc9110.html).

- **JDBC — a concrete ownership and binding example:**

  ```java
  // Java 8+ method; imports: java.sql.*, javax.sql.DataSource.
  static int updateStatus(DataSource source, long id, String status)
          throws SQLException {
      String sql = "UPDATE payment SET status = ? WHERE id = ?";
      try (Connection connection = source.getConnection();
           PreparedStatement statement = connection.prepareStatement(sql)) {
          statement.setString(1, status);
          statement.setLong(2, id);
          return statement.executeUpdate();
      }
  }
  ```

  - This method owns its connection; it does not demonstrate participation in
    a caller's Spring-managed transaction. Use transaction-aware access for that.

  - Closing a pooled logical connection normally returns it to the pool.

  - Multiple statements on an auto-commit connection do not become one atomic
    business transaction just because they share a connection.

  - Manual transactions require commit/rollback and preservation of failure and
    cleanup information; define timeouts and acquisition limits as well.

  - **References:** [DataSource](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/javax/sql/DataSource.html)
    · [PreparedStatement results and binding](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html).

- **Distributed consistency:** compensation can fail too; design retries,
  deduplication, reconciliation, and manual repair. An **outbox** stores business
  state and an event record in one local transaction, followed by reliable
  publication with duplicate handling. It does not independently make arbitrary
  external effects exactly once.

- **Pattern boundaries:** a static named factory is not automatically the GoF
  inheritance-based Factory Method. Enum singleton construction does not make
  mutable enum fields thread-safe; scope still depends on class loading.

- **Build discipline:** use `./gradlew build` for a controlled wrapper version,
  declare release/toolchain targets, and control dependency/plugin versions.

- **Mastery:** trace a self-invocation and an inner rollback-only failure;
  predict `execute()`'s boolean; distinguish HTTP idempotency from payment
  deduplication; explain the outward reversion posting without erasing history.

- **Depth:** [Spring basics](../spring-boot/spring-boot-basics.md)
  · [transactions](../spring-boot/spring-boot-transactions-deep.md)
  · [senior Spring reference](../spring-boot/spring-senior-core.md)
  · [JPA performance](../spring-boot/spring-data-jpa-performance.md).

### Honesty guardrails (truth law)

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

---

## §11 Kafka ⚓

→ **[kafka-basics.md](../kafka/kafka-basics.md)** — the log model ·
partitions and ordering · producers · consumer groups and offsets ·
delivery guarantees · rebalancing · retention and DLQ · Spring
Kafka · the IMPS anchor. Self-contained, answers sized for speaking.

*One-liner for the section:* Kafka is a replayable append-only log
split into ordered partitions, where the partition is both the unit
of ordering and the unit of parallelism, and **commit timing** is a
knob that controls consumer loss/duplicate exposure — IMPS uses it to ACK NPCI
fast and let the CBS leg run at its own pace, with the DB journal,
not Kafka, as the truth for dedupe and recovery.

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

### Kafka mastery

- If processing succeeds and the consumer crashes before committing, what happens on redelivery?

- If the offset commits and the DB operation then fails, what recovery path exists?

- What is the ordering key, and can worker concurrency reorder processing after polling?

- What happens when retries exceed the operating budget: pause, retry later, dead-letter, or manual repair?

- **Depth:** use the answer sheet for the configuration and failure details behind each scenario.

---

## §12 SDLC + testing

- **SDLC phases?** — requirements → design → build → test → deploy
  → maintain.

- **Waterfall vs Agile?**

  - waterfall — sequential, sign-off-gated

  - agile — iterative, feedback-driven; Scrum uses sprints

  - V-model — each dev phase pairs a test phase

  - spiral — risk-driven iterations

  ```text
  requirements ─────────────▶ UAT            (V-model: go down
    high-level design ─────▶ system test      building, come up
      detailed design ───▶ integration test   testing — each level
        code ──────────▶ unit test            checks its twin)
  ```

- **Scrum in one breath?** — product backlog, sprint (one month or less),
  daily stand-up, sprint review + retrospective; product owner
  maximizes value, scrum master helps establish Scrum.

    **Actors:**

    - Product Owner — owns the backlog, prioritizes by value; the
      "what"

    - Scrum Master — servant leader, removes impediments, coaches
      the process; the "how"

    - Developers — cross-functional, self-organizing, build the
      increment

    **Events:**

    - Sprint — the container itself, fixed length of one month or less, one goal

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

    - Increment — usable addition to prior increments, must meet
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
    controlled (mocked when useful); usually owned by the developer

  - Integration — checks that separately-built modules talk to each
    other correctly (service ↔ DB, service ↔ service)

  - System — whole application end-to-end against requirements, in
    a prod-like environment

  - UAT — business users verify it meets the real-world need; the
    sign-off gate before go-live

- **Smoke vs sanity vs regression?**

  - smoke — wide but shallow: skims every major feature on a new
    build; the go/no-go gate before deeper testing starts

  - sanity — narrow: quick, focused check of
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
  sitting in an artifact; failure: observed incorrect behavior at runtime. A
  defect can remain dormant; executing defective code does not always cause a failure.

- **Black-box vs white-box?** — black-box tests *what* the software
  does from the outside (inputs → outputs, no view of the code);
  white-box tests *how*, with full sight of the internal paths and
  logic.

  - **black-box** — against the spec (functional, system, UAT);
    catches wrong/missing behavior; does not establish dead-code coverage

    - in Java: MockMvc / TestRestTemplate / RestAssured against REST
      endpoints, Selenium at the UI

  - **white-box** — against the code (paths, branches, loops);
    catches logic gaps; alone cannot establish complete requirements coverage

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
    honestly thin ([honesty guardrails](#honesty-guardrails-truth-law))

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

  - **Distinguish scope from technique** — VAPT is an *engagement* (find
    → exploit → report); SAST/DAST/SCA/IAST are *techniques* that may run
    within or alongside it. DAST can support VA or PT; scope decides.

    ```text
    VAPT  (engagement: find → exploit → report)
    ├── Vulnerability Assessment (VA) — often automated, breadth
    │   ├── Network scanners ...... Nessus, Qualys, OpenVAS
    │   ├── Port scanning ......... Nmap
    │   └── Web-app scanners (DAST) OWASP ZAP, Burp Scanner
    └── Penetration Testing (PT) — tester-led, depth
        ├── Manual exploitation
        ├── Metasploit
        ├── Burp Suite (intercept/exploit mode)
        ├── sqlmap
        └── Kali Linux (toolkit)

    Can run alongside or within scope, often CI-side:
    ├── SAST — source/bytecode, no run ... SonarQube, Fortify, Snyk Code
    ├── SCA  — 3rd-party CVEs ............ Dependency-Check, Snyk
    └── IAST — agent inside running app .. (hybrid, depth reserve)
    ```

  - **VAPT** = Vulnerability Assessment + Penetration Testing —
    the umbrella used in bank security programs; confirm applicable go-live requirements

    - VA (Vulnerability Assessment) — assessment, often automated, that
      *lists* weaknesses (breadth); tools: Nessus, OpenVAS,
      Qualys, Nmap

    - PT (Penetration Testing) — tester-led, deep; a tester actively
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
    — an awareness list of major web-application risks (injection, XSS, broken auth…);
    the reference every panel expects you to name

  - ⚓ honest surface: I wrote injection-safe code (PreparedStatement,
    §10) and NPCI mandates signed XML + PKI — but the formal VAPT/scan
    program is a dedicated security function; name the tools, don't
    claim you ran the pentest ([honesty guardrails](#honesty-guardrails-truth-law))

- **Verification vs validation?** *(classic)* — building it right
  vs building the right thing.

  - verification — *"are we building it right?"* — against the
    spec/design: reviews, walkthroughs,
    inspections, static analysis, and dynamic checks against specified requirements

  - validation — *"are we building the right thing?"* — against
    real user need: functional, system, UAT, and early stakeholder review of intended use

  - Memory hook: *verify against the spec, validate against the user
    need; either can use static or dynamic evidence.*

- **Severity vs priority?** — technical impact vs fix order.

  - severity — how badly it breaks the product (technical); the
    tester often proposes it

  - priority — how soon it must be fixed (business urgency); the
    PM / product owner often sets it through triage

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

- **TDD vs BDD?** *(X-vs-Y classic)* — TDD writes a failing test *first*;
  BDD builds shared understanding through examples, often before implementation:

  - TDD (test-driven) — red → green → refactor: write a failing
    test (often a unit test), make it pass, then clean up; developer-facing,
    JUnit + Mockito

  - BDD (behavior-driven) — describe behavior in plain-language
    scenarios (Given/When/Then), readable by business + QA;
    Cucumber runs Gherkin `.feature` files against step definitions
    BDD does not require Cucumber.

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

### Scrum details beyond the short answer

- **Planning:** why this Sprint is valuable matters alongside what can be done and how it will be delivered.

- **Review:** a working session to inspect the outcome and adapt future work, not just a presentation.

- **Daily Scrum:** 15 minutes for Developers to inspect progress and adapt their plan; it is not a required manager-status meeting.

- **Increment:** more than one can be created in a Sprint; each adds to prior increments and meets the Definition of Done.

### Test boundaries — what the checks actually establish

- **Regression boundary:** unintended effects can appear within the changed area as well as around it; select checks by risk.

- **Unit:** a plain object/function with controlled collaborators; pure functions and value objects can be tested directly.

- **Integration:** use realistic participating components, versions, lifecycle, isolation, and cleanup.

- **Coverage:** executed lines/branches are evidence, not proof of correct assertions, complete requirements, or safe concurrency.

- **Reproducibility:** control dependencies, isolate data, fix time/randomness where appropriate, and bound waits.

- **Concurrency checks:** one successful schedule does not prove correctness; use coordination and explicit invariants instead of arbitrary sleeps.

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

### Testing mastery and practice

- Turn a requirement into examples before choosing a framework.

- Explain which fee checks detect rounding and validation mistakes, and which production concerns they do not exercise.

- Choose a real integration boundary for persistence, transactions, authorization, or broker behavior.

- Describe a false positive from a mocked dependency and a risk that line coverage cannot establish.

- Distinguish Scrum accountabilities from team-specific job titles and defect-triage ownership.

- **References:** [The Scrum Guide](https://scrumguides.org/scrum-guide.html) · [ISTQB foundation syllabus](https://istqb.org/wp-content/uploads/2024/11/ISTQB_CTFL_Syllabus_v4.0.1.pdf) · [BDD discovery, formulation, and automation](https://cucumber.io/docs/bdd/) · [security assessment techniques](https://csrc.nist.gov/pubs/sp/800/115/final).

- **Depth:** [production testing](../spring-boot/spring-boot-testing-deep.md) · [observability](../spring-boot/spring-boot-observability.md) · [resilience](../spring-boot/spring-boot-resilience.md).

---

## §13 Precision card

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

## §14 Integrated practice

### Build one transaction report

- **Starter:** [Transaction Report](lab/14-integration/TransactionReport.java).

- **Input boundary:** parse a file with defined text encoding and resource ownership.

- **Type boundary:** represent tenant/account/transaction identifiers, credit/debit kind,
  exact amounts, and instants deliberately.

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
