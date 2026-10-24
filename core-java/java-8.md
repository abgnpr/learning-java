# Java 8 — solid foundations 🐧

Java 8 introduced the programming model that still shapes modern Java:
functional interfaces, lambdas, method references, streams, `Optional`,
default interface methods, `java.time`, and `CompletableFuture`.

- **Goal:** understand the contracts well enough to write, debug, and explain
  Java 8 code without relying on memorized snippets.

- **Prerequisites:** classes, interfaces, generics, collections, exceptions,
  and basic concurrency vocabulary.

- **Version boundary:** unless a later release is named explicitly, every API
  and syntax example in this guide is available in Java 8.

- **Official release context:** use [java-versions.md](java-versions.md) for the
  chronological view and the [Core Java kit](core-java.md) for the broader
  interview study path.

---

## Navigation

- [§A Functional interfaces](#a-functional-interface--the-slot-a-lambda-fills)

- [§B Lambdas](#b-lambda--the-compact-syntax)

- [§C Method references](#c-method-reference--shorthand-for-a-one-call-lambda)

- [§D Streams](#d-stream-api--the-payoff)

- [§E Collectors](#e-collectors--what-collect-uses)

- [§F Optional](#f-optional--maybe-absent-made-explicit)

- [§G Default and static interface methods](#g-default--static-methods-in-interfaces)

- [§H Date and time](#h-javatime--the-new-datetime-api)

- [§I CompletableFuture](#i-completablefuture--compose-asynchronous-stages)

- [§J Other Java 8 additions](#j-other-java-8-additions-worth-knowing)

- [§K Release context](#k-the-lts-list--cheap-to-memorize-certain-to-be-asked)

## How to learn this guide

- **First pass:** read §A → §E in order. They form one chain:
  functional interface → lambda → method reference → stream → collector.

- **Second pass:** study §F → §J independently. These sections do not require
  one fixed order.

- **Active recall:** answer each section's mastery check without looking back.
  Explain the problem, the mechanism, and the tradeoff before quoting syntax.

- **Interview practice:** use the [coverage and practice path](lab/INTERVIEW-PATH.md)
  to connect these concepts with collection contracts, language rules, and an
  integrated report. Most drills use a JDK 21 harness; the six exercises in the
  next list retain their Java 8 compilation baseline.

- **Practice:** complete each TODO or rewrite each solved exercise from memory in
  this order:

  1. [LambdaPredicates.java](lab/08-advanced/LambdaPredicates.java)
  2. [FilterMapPipeline.java](lab/09-stream-api/FilterMapPipeline.java)
  3. [GroupingSales.java](lab/09-stream-api/GroupingSales.java)
  4. [CustomBracketCollector.java](lab/09-stream-api/CustomBracketCollector.java)
  5. [OptionalAccountWorkflow.java](lab/08-advanced/OptionalAccountWorkflow.java)
  6. [DefaultMethodResolution.java](lab/08-advanced/DefaultMethodResolution.java)

- **Then reinforce the remaining concepts:** these labs use a full JDK 21+
  and connect Java 8 behavior with broader language and library contracts:

  1. [MultiFieldInventorySort.java](lab/09-stream-api/MultiFieldInventorySort.java)
  2. [ClockDrivenTime.java](lab/11-io-time/ClockDrivenTime.java)
  3. [ZonedDstScheduling.java](lab/11-io-time/ZonedDstScheduling.java)
  4. [CompletableFutureComposition.java](lab/12-concurrency-async/CompletableFutureComposition.java)
  5. [FunctionalCompositionContracts.java](lab/08-advanced/FunctionalCompositionContracts.java)
  6. [CompilerContracts.java](lab/10-core-contracts/CompilerContracts.java)
  7. [MapUpdateContracts.java](lab/04-data-structures/MapUpdateContracts.java)
  8. [CompletableFutureExecution.java](lab/12-concurrency-async/CompletableFutureExecution.java)

- **Interview combinations and boundary checks** (JDK 21+):

  1. [EmployeeStreamQueries.java](lab/09-stream-api/EmployeeStreamQueries.java) —
     per-department maxima/averages, nested grouping, and second distinct salary.
  2. [FunctionalBehaviorPredictions.java](lab/10-core-contracts/FunctionalBehaviorPredictions.java) —
     receiver timing, source binding, reduction laws, and future recovery position.
  3. [DateBoundaryContracts.java](lab/11-io-time/DateBoundaryContracts.java) —
     strict parsing, year patterns, and monthly anchor-day scheduling.

- **Run an exercise on JDK 8:** compile it first, then run the class:

  ```bash
  javac FilterMapPipeline.java
  java FilterMapPipeline
  ```

- **Verify Java 8 compatibility on JDK 9+:** compile with `--release 8`:

  ```bash
  javac --release 8 FilterMapPipeline.java
  ```

---

## The one idea underneath everything

- **Why make behavior easy to pass?** — Sorting, filtering, and retrying often
  keep the same algorithm while changing one comparison, condition, or action.
  Accepting that behavior as an input avoids rewriting the algorithm for every
  business rule.

- Java already passed behavior by wrapping it in objects such as anonymous
  `Runnable` and `Comparator` implementations.

- A **functional interface** describes one operation.

- A **lambda** or **method reference** supplies that operation concisely.

- A **stream** asks for those operations and arranges them into a pipeline.

- Java remains statically typed: a lambda has no useful type by itself. It is
  converted to a specific functional-interface target type.

```text
functional interface -> lambda / method reference -> stream operation
       Predicate<T>   ->       x -> x > 0          -> filter(...)
       Function<T,R>  ->       x -> transform(x)   -> map(...)
       Consumer<T>    ->       x -> use(x)         -> forEach(...)
```

---

## §A Functional interface — the "slot" a lambda fills

Official reference: [JLS §9.8, Functional Interfaces](https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.8).

- **Why does a lambda need a functional interface?** — One lambda body needs
  one operation to implement, with known parameters, result, and exceptions.
  Existing callback interfaces such as `Runnable` and `Comparator` already fit
  that model; a general interface with several independent abstract operations
  does not identify one body to supply.

### Definition

- A **functional interface** has one effective abstract method, commonly called
  its **SAM**: Single Abstract Method.

- Abstract methods matching public methods of `Object` do not count toward the
  SAM.

- The abstract operation can be inherited; compatible inherited declarations
  can represent one effective method.

- `default` and `static` methods do not count because they already have bodies.

- `@FunctionalInterface` is optional, but it makes the compiler reject changes
  that accidentally break the SAM contract.

```java
@FunctionalInterface
interface Validator {
    boolean check(String value);
}

Validator notBlank = value -> !value.trim().isEmpty();
boolean valid = notBlank.check("Java");                 // true
```

### The core four

|Interface|SAM method|Shape|Meaning|
|---|---|---|---|
|`Predicate<T>`|`test(T)`|T → boolean|ask a question|
|`Function<T,R>`|`apply(T)`|T → R|transform a value|
|`Supplier<T>`|`get()`|() → T|produce a value|
|`Consumer<T>`|`accept(T)`|T → void|use a value|

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
Function<String, Integer> length = text -> text.length();
Supplier<LocalDate> today = LocalDate::now;
Consumer<String> print = System.out::println;

isEven.test(4);         // true
length.apply("Java");  // 4
today.get();            // current LocalDate
print.accept("hello"); // prints hello
```

### Choose an interface by its shape

- **Why use the built-in shapes?** — A caller that needs a test or transform
  can accept a standard contract instead of depending on a new domain-specific
  interface each time. Keep a custom interface when its name or contract adds
  meaning, such as `Validator` above.

- **Two inputs:** `BiPredicate<T,U>`, `BiFunction<T,U,R>`, or
  `BiConsumer<T,U>`.

- **Same input and output type:** `UnaryOperator<T>` is a specialized
  `Function<T,T>`.

- **Two same-type inputs and one result:** `BinaryOperator<T>` is a specialized
  `BiFunction<T,T,T>`.

- **Primitive values:** use interfaces such as `IntPredicate`, `IntConsumer`,
  `IntUnaryOperator`, and `ToIntFunction<T>` to avoid boxing.

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
UnaryOperator<String> trim = String::trim;
BinaryOperator<Integer> maximum = Integer::max;
ToIntFunction<String> lengthAsInt = String::length;
```

### Compose small operations

**Predicates:** `and` requires both conditions; `or` requires either;
`negate` reverses the result. Evaluation is left to right: `and` skips the
second predicate after `false`, while `or` skips it after `true`.

```java
Predicate<Integer> positive = n -> n > 0;
Predicate<Integer> even = n -> n % 2 == 0;

positive.and(even).test(4);  // true — both pass
positive.and(even).test(-2); // false — positive fails; even is skipped
positive.or(even).test(3);   // true — positive passes; even is skipped
positive.negate().test(-2); // true — not positive
```

**Functions:** `f.andThen(g)` runs `f` then `g`; `f.compose(g)` runs `g` then
`f`. The same functions and input can therefore produce different answers:

```java
Function<Integer, Integer> addOne = n -> n + 1;
Function<Integer, Integer> doubleIt = n -> n * 2;

addOne.andThen(doubleIt).apply(3); // 8 — doubleIt(addOne(3)): 3 → 4 → 8
addOne.compose(doubleIt).apply(3); // 7 — addOne(doubleIt(3)): 3 → 6 → 7
```

Composition returns new behavior; `test` or `apply` executes it. Each step's
output type must fit the next step's input type.

### Comparator composition

Later keys break ties only when earlier keys compare equal. Assume Employee
exposes department, salary, and name getters. Natural String order is
case-sensitive lexicographical order.

```java
// Department ASC, salary ASC, name ASC.
Comparator<Employee> allAscending = Comparator.comparing(Employee::getDepartment)
    .thenComparingInt(Employee::getSalary)
    .thenComparing(Employee::getName);

// Department ASC, salary DESC, then natural name ASC when both earlier keys tie.
Comparator<Employee> mixedOrder = Comparator.comparing(Employee::getDepartment)
    .thenComparing(Comparator.comparingInt(Employee::getSalary).reversed())
    .thenComparing(Employee::getName);

employees.sort(mixedOrder);
// Within Tech at salary 120: Ana before Ben; lower salaries follow both.
```

- Reverse a key **before** attaching it to change only that field's direction.
  `mixedOrder.reversed()` reverses the entire chain, including department/name.
- Append an immutable ID for repeatable complete ties. List sorting otherwise
  preserves the original order of rows that compare as zero.
- Use primitive key helpers rather than subtraction, which can overflow.

The [application comparator patterns](comparator-patterns.md) guide covers all
field-direction combinations, nulls, case/locale rules, business ranks, ranked
maps, sort menus, and selection, with worked datasets and a practice route.
Start the hands-on reps with [MultiFieldInventorySort.java](lab/09-stream-api/MultiFieldInventorySort.java).

### Mastery check

- Can you choose the correct interface from only the input and output shapes?

- Can you explain why `@FunctionalInterface` is useful but optional?

- Can you distinguish `Function<T,T>` from `UnaryOperator<T>`?

- Why does a lambda need one effective abstract operation rather than an
  arbitrary interface?

- Can you predict the order of `compose` and `andThen`?

---

## §B Lambda — the compact syntax

Official reference: [JLS §15.27, Lambda Expressions](https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.27).

- **Why lambdas if anonymous classes already work?** — Supplying one
  comparison should not require repeating a class body and method declaration.
  The target interface supplies the contract; the lambda supplies the behavior.
  The sorting algorithm still decides when to call that behavior.

### What a lambda creates

- A lambda is a concise implementation of a compatible functional interface.

- Its target interface supplies the parameter types, return type, and allowed
  checked exceptions.

- A lambda cannot target `Object` directly because `Object` does not describe a
  SAM shape.

```java
// Does not compile: Object supplies no functional-interface target.
// Object operation = value -> value + 1;

Function<Integer, Integer> operation = value -> value + 1;
```

### Before and after

```java
// Before Java 8: anonymous class
names.sort(new Comparator<String>() {
    @Override
    public int compare(String left, String right) {
        return Integer.compare(left.length(), right.length());
    }
});

// Java 8: lambda
names.sort((left, right) -> Integer.compare(left.length(), right.length()));
```

### Syntax forms

```java
() -> 42

value -> value * 2

(left, right) -> left + right

(left, right) -> {
    int sum = left + right;
    return sum;
}
```

- Parentheses are optional only for one inferred parameter.

- A single expression returns its value implicitly when the target SAM returns
  a value.

- A block body needs `return` on every value-producing path.

### Target typing

- The same lambda text can represent different interfaces when their shapes are
  compatible.

```java
Predicate<String> nonEmpty = text -> !text.isEmpty();
Function<String, Boolean> boxedCheck = text -> !text.isEmpty();
```

- The surrounding assignment, method argument, cast, or return position tells
  the compiler which target type to use.

- An explicit cast can resolve an otherwise ambiguous overload:

```java
process((Predicate<String>) text -> !text.isEmpty());
```

### Variable capture

- **Why must captured locals be effectively final?** — Capture retains a
  local value; it does not turn the local variable into a shared, reassignable
  binding. The restriction prevents dynamically changing captured locals and
  their concurrency hazards.

- A lambda may capture a local variable or parameter only when it is `final` or
  **effectively final**: it is not assigned again after initialization or its
  one definite assignment.

- The captured value may be an object reference. The object itself can still be
  mutable, although shared mutation is usually a poor stream design.

- Capture does not make that object thread-safe.

- Instance fields and static fields are not captured local variables, so the
  effectively-final rule does not apply to them.

```java
int taxPercent = 18;
Function<Integer, Integer> addTax =
    price -> price + price * taxPercent / 100;

// taxPercent = 20; // compile error: taxPercent would no longer be effectively final
```

### Lambda versus anonymous class

- Inside a lambda, `this` refers to the enclosing object.

- Inside an anonymous class, `this` refers to the anonymous object.

```java
class Scope {
    int value = 1;
    Supplier<Integer> lambda = () -> this.value;
    Supplier<Integer> anonymous = new Supplier<Integer>() {
        int value = 2;
        public Integer get() { return this.value; }
    };
}
Scope scope = new Scope();
scope.lambda.get();    // 1 — enclosing Scope's this
scope.anonymous.get(); // 2 — anonymous object's this
```

- A lambda cannot introduce a checked exception that the target SAM does not
  permit.

- Do not depend on a lambda's generated class, object identity, or serialization
  form. Those are implementation details.

### Mastery check

- Can you identify the target type of every lambda in an expression?

- Can you explain why a reassigned local variable cannot be captured?

- Can you convert an anonymous `Comparator` into a lambda?

- Can you explain what `this` means inside that lambda?

---

## §C Method reference — shorthand for a one-call lambda

- **Why use a method reference?** — When the operation already has a name,
  repeating its argument forwarding adds little information. Use a method
  reference when the lambda only forwards its arguments to one existing method
  or constructor.

### The four forms

|Kind|Method reference|Forwarding lambda shape|
|---|---|---|
|static method|`Integer::parseInt`|`text -> Integer.parseInt(text)`|
|bound instance method|`System.out::println`|`value -> System.out.println(value)`|
|unbound instance method|`String::toLowerCase`|`text -> text.toLowerCase()`|
|constructor|`ArrayList::new`|`() -> new ArrayList<>()`|

### The receiver rule

- In an unbound instance reference, the first SAM argument becomes the receiver.

```java
BiFunction<String, String, Integer> compare = String::compareTo;

// Equivalent:
BiFunction<String, String, Integer> expanded =
    (left, right) -> left.compareTo(right);
```

### Bound receivers: creation time versus invocation time

- **Why can replacing a lambda change behavior?** — A bound method reference
  evaluates its receiver expression once when the reference is created. A
  forwarding lambda evaluates the receiver expression in its body whenever
  that body runs. The table's call shapes therefore need a receiver-timing
  check before being treated as interchangeable code.

```java
class Holder { String value = " first "; }
Holder holder = new Holder();

Supplier<String> bound = holder.value::trim;
Supplier<String> deferred = () -> holder.value.trim();

holder.value = " second ";

System.out.println(bound.get());    // first
System.out.println(deferred.get()); // second
```

- `bound` retained the original String receiver. `deferred` retained access to
  `holder` and reads its current field when invoked. Capturing the holder does
  not freeze its fields or make their mutation thread-safe.

- The same distinction applies to `getService()::execute`: `getService()` runs
  during reference creation, although `execute()` runs when the callback is
  invoked. In `() -> getService().execute()`, both calls are deferred.

```java
holder.value = null;

// Throws during reference creation, before any get():
// Supplier<String> invalid = holder.value::trim;

Supplier<String> delayedFailure = () -> holder.value.trim(); // creation succeeds
// delayedFailure.get(); // throws NullPointerException when invoked
```

- Choose a bound reference when one particular receiver should be retained;
  choose a lambda when looking up the receiver at invocation is intentional.
  See [JLS method-reference evaluation](https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.13.3).

<details>
<summary>Predict: after the field becomes null, can the original bound.get() still work?</summary>

- Yes. It still targets the original non-null String and returns `first`.
  Changing the field does not retarget an existing bound reference.

</details>

### When to use one

- Keep the lambda when it makes argument order, adaptation, or extra behavior
  clearer.

```java
Function<String, Integer> clear = text -> Integer.parseInt(text.trim());

// Integer::parseInt alone would omit the trim step.
```

- Overloaded methods still rely on the target functional interface to choose a
  compatible signature.

### Mastery check

- Can you expand each of the four method-reference forms into a lambda?

- Can you identify the receiver in `String::compareTo`?

- Can you predict when a receiver expression runs and when a null receiver fails?

- Can you explain why some one-line lambdas are clearer when left as lambdas?

---

## §D Stream API — the payoff

Official reference: [Java 8 Stream package](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html).

### Exact mental model

- **Why streams when loops already work?** — A loop puts traversal, selection,
  transformation, and result-building in your hands. A pipeline separates the
  requested operations from traversal, so the library can arrange evaluation
  and stop when the requested result permits it. Choose a loop when explicit
  control flow makes the task clearer.

- A stream is a sequence of elements supporting aggregate operations.

- It is not a collection and does not store its own elements.

- A pipeline contains one source, zero or more intermediate operations, and one
  terminal operation.

- Intermediate operations are lazy. Traversal starts when a terminal operation
  requests a result.

```text
source -> intermediate operations (lazy) -> terminal operation (runs pipeline)
```

### Before and after

```java
// External iteration: the code controls every step.
List<String> loopResult = new ArrayList<>();

for (Person person : people) {
    if (person.getAge() >= 18) {
        loopResult.add(person.getName().toUpperCase(Locale.ROOT));
    }
}

// Internal iteration: the pipeline describes the result.
List<String> streamResult = people.stream()
    .filter(person -> person.getAge() >= 18)
    .map(Person::getName)
    .map(name -> name.toUpperCase(Locale.ROOT))
    .collect(Collectors.toList());
```

### How functional interfaces connect to streams

|Stream operation|Function shape|Typical interface|
|---|---|---|
|`filter`|T → boolean|`Predicate<T>`|
|`map`|T → R|`Function<T,R>`|
|`forEach`|T → void|`Consumer<T>`|
|`generate`|() → T|`Supplier<T>`|
|`reduce`|(T,T) → T|`BinaryOperator<T>`|

```java
Predicate<Person> adult = person -> person.getAge() >= 18;
Function<Person, String> name = Person::getName;
Consumer<String> print = System.out::println;

people.stream()
    .filter(adult)
    .map(name)
    .forEach(print);
```

### A complete Java 8 program

```java
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StreamWalkthrough {
    static final class Person {
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }
    }

    public static void main(String[] args) {
        List<Person> people = Arrays.asList(
            new Person("Ada", 36),
            new Person("Bob", 15),
            new Person("Grace", 28));

        List<String> adultNames = people.stream()
            .filter(person -> person.getAge() >= 18)
            .map(Person::getName)
            .map(name -> name.toUpperCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

        System.out.println(adultNames); // [ADA, GRACE]
    }
}
```

### Three stream laws

- **Lazy:** intermediate operations run only when a terminal operation demands
  elements.

  - **What does waiting buy us?** The terminal defines the demanded result.
    Filter and map can pass elements along without building a collection after
    every stage; a short-circuiting terminal can stop requesting more. Stateful
    operations such as sorting may still need buffering.

  ```java
  Stream<String> pipeline = names.stream()
      .filter(name -> {
          System.out.println("checking " + name);
          return !name.isEmpty();
      });

  // Nothing has printed yet.
  long count = pipeline.count();
  ```

- **Single use:** invoke operations on a stream only once. Reuse is invalid.
  An implementation may throw `IllegalStateException` when it detects reuse,
  but detection is not guaranteed.

  - **Why no rewind?** A source may be a one-way iterator or an open file.
    Replay would require saving elements or reopening the source; the Stream
    contract promises neither. Ask the source for a fresh traversal.

  ```java
  Stream<String> stream = names.stream();
  long count = stream.count();

  // Invalid: the stream has already been consumed.
  // Optional<String> first = stream.findFirst();

  // Ask the reusable source for a new stream instead.
  Optional<String> first = names.stream().findFirst();
  ```

- **Non-interfering and usually stateless:** behavioral parameters must not
  modify a non-concurrent source during traversal, and their results should not
  depend on changing shared state. A stream does not make mutable source
  elements immutable.

  - **What breaks with shared state?** Results can depend on which callbacks
    run and when they run. Short-circuiting, skipped callbacks, and parallel
    execution can expose that dependency. Changing the traversed source also
    changes what the traversal is trying to observe.

  ```java
  // Avoid this shared mutation.
  List<String> output = new ArrayList<>();
  names.stream().filter(name -> {
      output.add(name);
      return true;
  }).count();
  ```

### Source binding: a stream is not a collection snapshot

- **What does a stream remember when it is created?** — It remembers a source
  and an evaluation plan. For an `ArrayList`, traversal binds to the source
  late enough that changes made before terminal evaluation can be included.

```java
List<String> names = new ArrayList<>(Arrays.asList("Ada"));
Stream<String> pending = names.stream();

names.add("Lin"); // before traversal starts

System.out.println(pending.collect(Collectors.toList())); // [Ada, Lin]
```

- Adding `Lin` happened before traversal, so this example does not mutate the
  source from a running callback. Changing a non-concurrent source during
  traversal violates non-interference; a possible `ConcurrentModificationException`
  is only best-effort bug detection, not a synchronization mechanism.

- Source policy matters. A late-binding collection traversal and a snapshot
  traversal are different contracts; do not assume every kind of stream binds
  at exactly the same moment.

- If membership must be captured at a particular point, make an owned copy
  before streaming it: `new ArrayList<>(names).stream()`. Arrange appropriate
  coordination if another thread can modify `names` during that copy.

- The copy is shallow: it copies element references, so mutable objects inside
  it still need an ownership policy. See [ArrayList.spliterator](https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html#spliterator--)
  and the [Collection traversal contract](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html#spliterator--).

<details>
<summary>Predict: does new ArrayList&lt;&gt;(people).stream() freeze each person's fields?</summary>

- No. The copy owns its list structure, but it refers to the same Person
  objects. Independent element state requires a suitable element copy or an
  immutable representation.

</details>

### `map` versus `flatMap`

- **Why flatten here?** — A later stage may need one sequence of inner values
  so it can filter, transform, or total them without handling each nested
  container itself.

- `map` transforms each input into exactly one output value.

- `flatMap` transforms each input into a stream of zero or more values, then
  flattens those streams into one stream.

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4));

Stream<Stream<Integer>> mapped = nested.stream().map(List::stream);
// Each element is itself a Stream<Integer>; the nesting remains.

List<Integer> flat = nested.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());

// [1, 2, 3, 4]
```

### Java 8 stream sources

|Source|Purpose|
|---|---|
|`collection.stream()`|sequential collection stream|
|`collection.parallelStream()`|parallel collection stream|
|`Arrays.stream(array)`|array stream|
|`Stream.of(a, b, c)`|stream of supplied values|
|`IntStream.range(0, n)`|integers from 0 through n−1|
|`IntStream.rangeClosed(1, n)`|integers from 1 through n|
|`Stream.iterate(seed, operation)`|infinite iterative source|
|`Stream.generate(supplier)`|infinite generated source|
|`Files.lines(path, charset)`|lazy I/O-backed line stream|

### Java 8 intermediate operations

- **Why distinguish stateless and stateful stages?** — Mapping can handle one
  element independently. Sorting needs enough input to establish order, and
  distinctness needs remembered values. Storage and coordination requirements
  therefore depend on the operations, not just on using a stream.

|Operation|Purpose|Property|
|---|---|---|
|`filter`|keep matching elements|stateless|
|`map`|one-to-one transformation|stateless|
|`flatMap`|zero-to-many transformation and flattening|stateless|
|`mapToInt`, `mapToLong`, `mapToDouble`|enter a primitive stream|stateless|
|`boxed`|turn primitive elements into wrappers|stateless|
|`distinct`|remove duplicates using `equals`|stateful|
|`sorted`|sort by natural order or a comparator|stateful|
|`peek`|observe elements flowing past|mainly for diagnostics|
|`limit`|take at most n elements|short-circuiting|
|`skip`|discard the first n elements|stateful|

- `parallel()` and `sequential()` change the execution mode of the pipeline.
  They are not element transformations.

### Follow an element through the pipeline

- **Does Java filter the entire input before mapping anything?** — A sequential
  pipeline of stateless operations can pass each demanded element through its
  stages. It need not build an intermediate list after every stage.

- `findFirst()` requests the first output in encounter order. Its `Optional`
  result represents possible absence, and it can stop requesting elements once
  a result is found.

```java
Optional<Integer> first = Stream.of(1, 2, 3, 4)
    .filter(n -> {
        System.out.println("filter " + n);
        return n % 2 == 0;
    })
    .map(n -> {
        System.out.println("map " + n);
        return n * 10;
    })
    .findFirst();

System.out.println(first);
```

```text
filter 1
filter 2
map 2
Optional[20]
```

- Element `1` fails the filter. Element `2` reaches the mapper and satisfies
  `findFirst`, so this traversal does not need `3` or `4`. This explains the
  example's sequential trace; it is not a general callback-count guarantee for
  optimized or parallel pipelines.

- A stateful operation can change the execution shape. An ordinary `sorted`
  stage must establish the ordering of its input before emitting the first
  sorted value. See [stream operations and pipelines](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html).

#### Operation order changes the question

```java
List<Integer> values = Arrays.asList(5, 1, 4, 2, 3);

List<Integer> smallestTwo = values.stream()
    .sorted().limit(2).collect(Collectors.toList());

List<Integer> sortedFirstTwo = values.stream()
    .limit(2).sorted().collect(Collectors.toList());

System.out.println(smallestTwo);    // [1, 2]
System.out.println(sortedFirstTwo); // [1, 5]
```

- The first asks for the smallest two values from the whole input. The second
  sorts only the first two encountered values. Moving `limit` changes meaning,
  even when it also reduces work.

- With an infinite source, that ordering can determine whether evaluation
  finishes at all:

```java
// Do not run: sorting waits for the infinite upstream to finish.
// Stream.iterate(0, n -> n + 1).sorted().limit(5)
//     .collect(Collectors.toList());

List<Integer> bounded = Stream.iterate(0, n -> n + 1)
    .limit(5).sorted().collect(Collectors.toList());
System.out.println(bounded); // [0, 1, 2, 3, 4]
```

- A short-circuiting operation is necessary for a finite result from infinite
  input, but its presence alone is insufficient. Every upstream stage must be
  able to supply what the downstream stage requests. See [Stream.sorted and limit](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html).

<details>
<summary>Predict: can sorted().findFirst() find the minimum of an arbitrary infinite stream?</summary>

- No. The sort cannot establish a first value by consuming only an arbitrary
  finite prefix. `findFirst` cannot short-circuit a sort that has not emitted
  anything. A finite bound or additional knowledge about the source is needed.

</details>

### Java 8 terminal operations

|Operation|Result|Purpose|
|---|---|---|
|`collect`|arbitrary result container|mutable reduction|
|`forEach`|`void`|perform an action|
|`forEachOrdered`|`void`|perform an action in encounter order|
|`count`|`long`|count elements|
|`reduce`|value or `Optional`|immutable reduction|
|`min`, `max`|`Optional<T>`|find an extreme|
|`anyMatch`|`boolean`|whether at least one matches|
|`allMatch`|`boolean`|whether every element matches|
|`noneMatch`|`boolean`|whether no element matches|
|`findFirst`|`Optional<T>`|first element in encounter order|
|`findAny`|`Optional<T>`|an arbitrary element|
|`toArray`|array|materialize an array|
|primitive `sum`, `average`, `summaryStatistics`|number or statistics|numeric aggregation|

- A stream-returning pipeline operation is generally intermediate.

- A non-stream result or side effect generally marks a terminal operation.

- Know the operation's contract: `isParallel()` returns `boolean` but does not
  consume the stream; it only queries the execution mode.

- `limit`, matching operations, and finding operations can stop traversal early.

- Empty input matters: `allMatch` and `noneMatch` are true because there is no
  counterexample; `anyMatch` is false because there is no matching element.

```java
Stream.<Integer>empty().allMatch(n -> n > 0); // true
Stream.<Integer>empty().anyMatch(n -> n > 0); // false
Stream.<Integer>empty().noneMatch(n -> n > 0); // true
Stream.<Integer>empty().reduce(0, Integer::sum); // 0 — supplied identity
Stream.<Integer>empty().reduce(Integer::sum); // Optional.empty()
```

- An infinite stream needs a short-circuiting path before it can produce a
  finite result.

### Primitive streams

- `IntStream`, `LongStream`, and `DoubleStream` avoid boxing every numeric
  element.

- They provide numeric terminals such as `sum`, `average`, `min`, `max`, and
  `summaryStatistics`.

- **Empty inputs:** `sum` has a zero identity; `average`, `min`, and `max` can
  return empty optional results.

```java
int totalAge = people.stream()
    .mapToInt(Person::getAge)
    .sum();

OptionalDouble averageAge = people.stream()
    .mapToInt(Person::getAge)
    .average();
```

### Encounter order

- A `List` stream has list encounter order.

- A `HashSet` stream has no defined encounter order.

- `findFirst` respects encounter order when one exists.

- `findAny` allows more freedom, especially in parallel pipelines.

- `forEach` does not promise encounter order in parallel.

- `forEachOrdered` preserves encounter order, which can reduce parallelism.

- `unordered()` removes an ordering requirement; it does not shuffle the source.

### Side effects and `peek`

- **Why is `peek` unsuitable for required business work?** — A stream promises
  the terminal result, not an exact callback trace. A correct result may need
  fewer elements or no traversal through an unneeded stage. Put required work
  in an execution path whose contract requires it.

- Prefer pipelines that compute and return values.

- Use `forEach` when a side effect is the actual terminal purpose.

- Use `peek` mainly to observe a pipeline while debugging.

- Do not make program correctness depend on `peek` executing for every source
  element; short-circuiting may stop before later elements are requested.

- On Java 9+, `count()` may use a known source size and skip traversal through
  `map`/`peek` entirely. A terminal operation does not guarantee every callback
  runs. See the [count API note](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html#count()).

### Parallel streams

- **What problem are we trying to solve?** — A sequential stream performs its
  traversal on the calling thread. If each element requires substantial,
  independent computation, several workers may finish the same calculation
  sooner by sharing the elements. Splitting and coordinating that work also
  costs time, so first establish what result must stay the same.

```java
List<String> words = Arrays.asList("a", "bb", "c", "dd");

Map<Integer, Long> sequentialCounts = words.stream()
    .collect(Collectors.groupingBy(
        String::length, Collectors.counting()));

Map<Integer, Long> parallelCounts = words.parallelStream()
    .collect(Collectors.groupingBy(
        String::length, Collectors.counting()));

System.out.println(sequentialCounts.equals(parallelCounts)); // true
// Both count length 1 -> 2 words, length 2 -> 2 words.
```

- This small example establishes correctness, not a speedup. `String::length`
  is cheap, and four words provide little work to share. An expensive,
  independent calculation over many elements is a more promising candidate.

- `words.stream()` is sequential. Request parallel execution with
  `words.parallelStream()` or `words.stream().parallel()`.

- The last `parallel()` or `sequential()` setting applies to the whole pipeline;
  these calls do not assign separate execution modes to individual stages.
  See [BaseStream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/BaseStream.html).

- **Why doesn't `parallel().map(...).sequential()` create two phases?** — Those
  calls configure evaluation of the same pipeline; they do not insert a queue,
  executor, or stored result between stages. Separate phases need an explicit
  result boundary or a separately designed task boundary.

- A parallel terminal operation such as `collect()` waits for its result.
  Parallel execution does not make the call asynchronous.

- Parallel execution is most promising for substantial, independent CPU work
  over a source that splits efficiently, with associative reductions.

#### How fork/join runs the work

- **How can several workers produce one answer?** — Each partition computes a
  partial answer. For the word counts, those partial answers are maps; merging
  them adds the counts for matching lengths. The merge must preserve the
  intended result regardless of how the input was divided.

```text
                         ["a", "bb", "c", "dd"]
                              split input
                         /                 \
                 ["a", "bb"]           ["c", "dd"]
                      |                      |
                 {1=1, 2=1}             {1=1, 2=1}
                         \                 /
                            merge counts
                            {1=2, 2=2}
```

- A **task** is a piece of work. A **thread** executes work; a pool's reusable
  threads are its **workers**. A **thread pool** manages those workers, so a
  task can run without creating a fresh thread for every submission.

- The JDK stream implementation splits the input into partitions, processes
  partitions as tasks, and combines partial results when needed.

- A source's `Spliterator` supplies traversal and partitioning. `tryAdvance`
  visits an element; `trySplit` can hand part of the remaining input to another
  traversal. Splitting into balanced, useful pieces helps workers share the
  computation. Splitting poorly can leave one worker with most of the work.
  See [Spliterator](https://docs.oracle.com/javase/8/docs/api/java/util/Spliterator.html).

- A task can process many elements. There is no new thread per element.

- `ForkJoinPool` uses **work-stealing**: an available worker can take queued
  work from another worker. The caller may also participate in stream work.

- **Why can ordinary maps work here?** — A non-concurrent collector such as
  `groupingBy` gives partitions isolated containers and combines their results.
  This avoids simultaneous mutation of one ordinary map. See
  [`groupingBy` with parallel streams](#groupingby-with-parallel-streams) for
  the collector contracts and the concurrent alternative.

- Work-stealing balances runnable tasks; it cannot free a worker that is
  waiting inside an ordinary blocking network call.

- Implementation references: [OpenJDK stream tasks](https://github.com/openjdk/jdk8u/blob/master/jdk/src/share/classes/java/util/stream/AbstractTask.java)
  and [ForkJoinTask](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html).

#### Caveat 1: the common pool is shared

- **Where did the workers come from?** — `parallelStream()` has no executor
  argument. The JDK supplies execution machinery, so application code can
  request parallelism without constructing and managing a pool for each
  pipeline. That convenience also means unrelated callers can share capacity.

- In the usual OpenJDK implementation, a parallel stream started from an
  ordinary application thread uses `ForkJoinPool.commonPool()` for forked
  work. A new stream does not receive a dedicated pool.

- Other parallel streams in the same JVM compete for those workers.

- `CompletableFuture.supplyAsync()` and `runAsync()` without an executor
  normally use the same pool. In Java 8, if common-pool parallelism is below
  two, those async methods create a new thread per task instead.
  See [CompletableFuture execution policies](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

```text
request A: parallelStream() ---------+
request B: parallelStream() ---------+--> common ForkJoinPool workers
supplyAsync(...), no executor ------+
```

- **Practical consequence:** a slow parallel operation in request A can delay
  unrelated stream or future work in request B. Multiple requests can also
  compete for CPU even when each request looks fast in isolation.

- Suppose one server request computes a large report while another schedules
  a future to render a small response. Their Java objects are unrelated, but
  their tasks can need the same pool workers. Isolation between request data
  does not imply isolation between execution resources.

<details>
<summary>Follow-up: how many workers does the common pool target?</summary>

- In OpenJDK 8, the default common-pool target is generally
  `max(1, availableProcessors() - 1)`. It is configurable; this is a
  parallelism target, not an absolute limit on all participating threads.
  A separately constructed `new ForkJoinPool()` defaults to
  `availableProcessors()` instead.
  See the [OpenJDK 8 common-pool implementation](https://github.com/openjdk/jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/ForkJoinPool.java).

```java
System.out.println(Runtime.getRuntime().availableProcessors());
System.out.println(ForkJoinPool.getCommonPoolParallelism());
```

</details>

#### Caveat 2: blocking I/O can occupy the workers

- **What changes when the callback calls a remote service?** — A CPU task
  spends its execution time calculating. A blocking HTTP or JDBC call can
  spend much of its time waiting for a network response or connection. During
  that wait, the platform thread cannot simply take a different task from its
  queue and execute it.

```java
List<String> responses = ids.parallelStream()
    .map(id -> fetchRemote(id)) // blocking HTTP, JDBC, or another remote call
    .collect(Collectors.toList());
```

- A worker waiting for a response remains occupied even though it does little
  CPU work. Queued tasks still need an available worker or participating caller.

- Imagine three workers available for queued work, each waiting on a remote
  call. An unrelated future needs only a millisecond of computation, but no
  worker is immediately available to run it. Work-stealing helps a free worker
  find work; the waiting calls do not create a free worker.

- If many common-pool workers block, unrelated work using that pool may make
  little progress. This is a starvation risk; blocking I/O alone does not
  automatically mean deadlock.

- **Starvation** means work is delayed for lack of execution opportunities.
  **Deadlock** means tasks cannot proceed because their dependencies cannot be
  satisfied, such as workers waiting for child tasks queued behind them.
  These need different diagnoses even when both appear as a stalled request.

- Fork/join can compensate for some supported waits, but **replacement workers
  are not guaranteed for arbitrary blocking I/O or unmanaged synchronization**.
  See [ForkJoinPool](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html).

- A JDBC connection pool can make the problem worse: fork/join workers may
  wait just to obtain a connection before the database call even starts.

- **The design question:** how many calls may run, how many may queue, how
  long may they wait, and what happens when capacity is exhausted?

#### Fix blocking I/O in Java 8: use an explicit executor

- **What would a fix need to achieve?** — Give the remote calls their own
  execution capacity, limit how many may run or wait, and leave common-pool
  workers available for other work. Moving the same calls to another API
  without choosing its executor would not establish that separation.

- An **executor** accepts tasks for execution. `ExecutorService` adds futures
  and lifecycle operations such as shutdown. A `CompletableFuture` is a handle
  to a result that can become available later; its callbacks can describe
  follow-up work. The handle itself does not supply execution capacity.

- A dedicated executor keeps blocking calls off the common pool and gives the
  application control over worker count, queue capacity, and overload behavior.

- For a small, finite batch, submit independent calls first, then collect their
  results. The sequential stream arranges submissions; the executor runs them
  concurrently.

- Assume `fetchRemote(id)` is an application method with configured connection
  and response timeouts. The values `20` and `100` illustrate capacity choices;
  they are not universal defaults.

- The constructor chooses 20 workers, a queue holding at most 100 waiting
  tasks, and a rejection policy. `supplyAsync` submits a supplier to that
  executor and returns a future. `join()` waits for the future's result or
  throws if it completed exceptionally.

```java
ExecutorService ioExecutor = new ThreadPoolExecutor(
    20, 20,
    0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<Runnable>(100),
    new ThreadPoolExecutor.AbortPolicy());

try {
    List<CompletableFuture<String>> pending = ids.stream()
        .map(id -> CompletableFuture.supplyAsync(
            () -> fetchRemote(id), ioExecutor))
        .collect(Collectors.toList());

    // Wait at the caller boundary, after submitting the batch.
    List<String> responses = pending.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
} finally {
    ioExecutor.shutdown();
}
```

- **Why two traversals?** — The first creates the pending calls; the second
  reads their results. Calling `supplyAsync(...).join()` inside the first
  stream's `map` would wait for each call before submitting the next, losing
  the intended concurrency.

- The response list follows input order because the futures were collected
  and then read in that order. Remote calls may still finish in another order.
  Concurrent execution and the order of returned results are separate choices.

- This executor permits up to 20 active tasks and 100 queued tasks. When both
  are full, `AbortPolicy` throws `RejectedExecutionException` during submission.
  The application must handle overload, for example by rejecting a request
  or admitting work in bounded batches.

- A dedicated worker still blocks during I/O. The benefit is **controlled
  capacity and workload isolation**, rather than making the call nonblocking.

- `newFixedThreadPool(20)` also isolates workers, but its unbounded queue does
  not provide the same overload limit.

- `CallerRunsPolicy` can slow submission by running work on the submitting
  thread. Use it deliberately: if the submitter is a common-pool worker or an
  event-loop thread, the blocking call would run there.
  See [ThreadPoolExecutor queue and rejection policies](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadPoolExecutor.html).

- Do not use silent discard policies for tasks whose futures callers will
  await; a discarded task can leave its future incomplete indefinitely.

- The example owns its executor for one batch. A service should usually reuse
  an application-owned I/O executor and shut it down at service shutdown.
  `shutdown()` lets accepted work finish; it does not wait or cancel that work.

- Failed submission or a failed future does not automatically cancel other
  accepted calls. Choose the batch's failure and cleanup policy explicitly.

- Supply the executor to every `*Async` stage that needs it. A following
  `thenApplyAsync(...)` without an executor does not inherit `ioExecutor`.
  Merely replacing `parallelStream()` with default `supplyAsync()` moves the
  blocking work back onto the usual common-pool path.

- Avoid submitting child tasks to a bounded executor and then waiting for
  them from all of that executor's workers. The children can be queued behind
  the waiting parents. Compose dependent futures or wait outside those workers.

#### How many types of pools are there?

- **Which choices solve which problems?** — A report may need CPU workers;
  blocking calls need controlled waiting capacity; a periodic refresh needs
  scheduling. Pool names describe different execution policies. Choose the
  policy from the work and its capacity constraints.

- There is no single official count. Distinguish executor interfaces,
  implementation classes, and factory configurations.

- `Executor` accepts tasks. `ExecutorService` adds futures and lifecycle
  operations. `ScheduledExecutorService` adds delayed and periodic execution.
  These interfaces are not three pool implementations.
  See [ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html).

- The main Java 8 implementations to recognize are `ThreadPoolExecutor`,
  `ScheduledThreadPoolExecutor` (a subclass of `ThreadPoolExecutor`), and
  `ForkJoinPool`. Factories expose common configurations:

|Configuration|Creation API|Behavior and main caveat|
|---|---|---|
|Single worker|`Executors.newSingleThreadExecutor()`|one task at a time; unbounded queue|
|Fixed workers|`Executors.newFixedThreadPool(n)`|at most `n` active workers; unbounded queue|
|Cached workers|`Executors.newCachedThreadPool()`|reuses idle workers and creates more as needed; effectively unbounded thread growth|
|Scheduled workers|`Executors.newScheduledThreadPool(n)` / `newSingleThreadScheduledExecutor()`|delayed or periodic work; long tasks can delay other scheduled work|
|Work-stealing / fork/join|`new ForkJoinPool(n)` / `Executors.newWorkStealingPool(n)`|tasks can create subtasks; workers steal work; suited to fork/join computation|
|Virtual thread per task — **Java 21+**|`Executors.newVirtualThreadPerTaskExecutor()`|new virtual thread per task; does not pool virtual threads or bound downstream concurrency|

- Java 8 factory reference: [Executors](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html).
  Scheduling reference: [ScheduledThreadPoolExecutor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html).
  Java 21+ factory reference: [Executors](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html).

- `ForkJoinPool.commonPool()` returns a shared instance of `ForkJoinPool`.
  It is not another implementation class.

- The OpenJDK `newWorkStealingPool` factory creates a separate `ForkJoinPool`;
  it does not return the common pool. Work-stealing alone does not make
  blocking I/O safe for that pool's capacity.

- **Fixed thread count does not mean bounded memory use.** An unbounded task
  queue can keep growing when arrivals outpace completion.

#### Size and protect the I/O workload

- **Why not just keep increasing the worker count?** — More simultaneous
  calls consume more connections and memory and put more load on the remote
  service. The limit should reflect what the dependency can sustain and what
  the application promises under overload.

- Start CPU worker sizing near the available processor count, then measure.
  More CPU workers do not create more processor capacity.

- Blocking I/O can benefit from more workers because calls spend time waiting.
  Measure throughput, wait time, queue growth, and downstream capacity rather
  than copying a fixed multiplier of CPU count.

- Match admission to the actual dependency: a database with 10 usable
  connections cannot execute 100 queries at once because 100 threads exist.

- Set connection, read/query, and overall request deadlines as appropriate.
  A timeout on `Future.get(timeout, unit)` bounds the caller's wait; it does
  not itself stop the underlying call. See [§I](#get-versus-join).

- Separate executors isolate worker availability; they still share CPU,
  memory, network, and downstream services.

#### A custom ForkJoinPool: isolate CPU work

- **What if the work really is CPU computation, but the shared pool is busy?**
  — A separate fork/join pool can give that workload its own workers. This
  addresses competition for pool workers; the computations still compete for
  the machine's CPU capacity.

- OpenJDK normally runs a parallel stream's forked tasks in the `ForkJoinPool`
  of the worker executing its terminal operation. A common isolation pattern
  submits the entire terminal computation to a dedicated pool:

```java
ForkJoinPool cpuPool = new ForkJoinPool(4);
try {
    long total = cpuPool.submit(() -> numbers.parallelStream()
        .mapToLong(number -> expensiveCpuCalculation(number))
        .sum()).get();
} finally {
    cpuPool.shutdown();
}
```

- The enclosing method must handle or declare `InterruptedException` and
  `ExecutionException` from `get()`. `expensiveCpuCalculation` represents
  independent CPU work.

- Keep the terminal operation inside the submitted lambda. Streams are lazy;
  creating a stream inside a pool and collecting it outside does not attach
  that stream to the pool.

- **Portability boundary:** the Stream API has no executor parameter. This
  pattern relies on OpenJDK execution behavior; verify it on the deployed JDK
  when pool selection is a requirement. See [ForkJoinTask.fork](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html#fork--).

- Wrapping `parallelStream()` in a normal `ThreadPoolExecutor` task does not
  redirect its forked work into that executor's workers.

- A custom fork/join pool isolates workers from the common pool. It does not
  fix blocking calls occupying its own workers. Reuse owned pools in a service
  rather than creating one per incoming request.

- Increasing `java.util.concurrent.ForkJoinPool.common.parallelism` is a global
  tuning choice, not workload isolation or downstream admission control.

#### ManagedBlocker: an advanced fork/join option

<details>
<summary>Why can telling fork/join about a wait help?</summary>

- A pool cannot reliably infer the capacity impact of every blocking API.
  Explicit cooperation gives it an opportunity to compensate while a worker
  waits. This is useful when an existing fork/join algorithm must include a
  supported blocking step.

- When blocking must occur inside fork/join work, code can cooperate with the
  pool through `ForkJoinPool.managedBlock(ManagedBlocker)`.

- `isReleasable()` checks whether blocking is unnecessary; `block()` performs
  the wait if needed. The pool can attempt to compensate with additional
  workers within its limits.

- Ordinary HTTP/JDBC lambdas are not automatically wrapped in a
  `ManagedBlocker`. It also does not provide remote-call timeouts, database
  connections, or protection from excess downstream requests.

- Prefer an explicit I/O executor for application blocking work unless there
  is a specific reason to integrate that work with fork/join scheduling.
  See [ManagedBlocker](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.ManagedBlocker.html).

</details>

#### Java 21+: virtual threads for blocking work

- **Why consider another execution model?** — A platform thread is a costly
  resource to occupy during every remote wait. Virtual threads let many tasks
  use a smaller set of platform threads when their blocking operations support
  suspension, while application code can retain a straightforward blocking
  style. Remote-service capacity still needs its own limit.

- `Executors.newVirtualThreadPerTaskExecutor()` creates one virtual thread per
  submitted task. It is a per-task executor, not a pool of reusable virtual
  threads.

- Supported blocking I/O can suspend a virtual thread and free its carrier
  platform thread. This helps scale waiting tasks; it does not make CPU work
  or the remote service run faster.

- Limit concurrent calls with a semaphore or the dependency's own capacity
  controls. Virtual threads do not make connection limits, deadlines, or
  admission limits unnecessary.

- Running `parallelStream()` from a virtual thread does not turn the stream's
  fork/join tasks into virtual threads.

- Virtual-thread executors are **not available in Java 8**. Their lifecycle can
  use try-with-resources on Java 21+. See the [virtual-thread guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

#### Other parallel-stream interview traps

- **What can still defeat a correct pool choice?** — The work may be too cheap,
  difficult to divide, order-sensitive, or dependent on shared state. These
  affect whether the computation benefits from parallel execution at all.

- **Cheap work:** `String::length` is inexpensive. Scheduling and combining
  costs may exceed the work saved, even when the pipeline is correct.

- **Source splitting:** arrays and array-backed lists generally split more
  easily than sources that require traversal or produce uneven partitions.

- **Shared mutation:** `parallelStream().forEach(results::add)` is unsafe when
  `results` is an ordinary shared `ArrayList`. Prefer a collector.

- **Ordering:** `forEach` does not preserve encounter order; `forEachOrdered`
  does, with coordination costs. Ordered `limit`, `distinct`, and sorting can
  require coordination or buffering.

- **Nested parallel streams:** inner streams usually share the current pool;
  nesting adds tasks and coordination without adding CPU capacity. Prefer one
  useful level of parallelism unless measurement supports more.

- **Thread-bound context:** ordinary `ThreadLocal` values belong to the current
  thread. Do not assume request context is available on stream workers; pass
  needed values explicitly. See [ThreadLocal](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html).

- **Measurement:** compare sequential and parallel execution with realistic
  input and competing requests. A single-request timing does not establish
  better server throughput. See [stream parallelism and side effects](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html).

#### Choose the execution model

|Workload|Reasonable starting point|
|---|---|
|Small in-memory transformation or grouping|sequential stream or loop|
|Large, independent CPU computation|measured parallel stream; custom fork/join pool if isolation matters|
|Blocking calls on Java 8|dedicated executor with bounded workers, queue, and overload handling|
|Many blocking calls on Java 21+|virtual thread per task with downstream concurrency limits|
|Client already exposes genuinely asynchronous I/O|compose its completion stages; keep blocking work off its completion/event-loop threads|

#### Interview answer to practise aloud

- **Why be careful?** Parallel streams usually share the common fork/join pool
  with other streams and default async `CompletableFuture` work. Blocking I/O
  can occupy its workers and delay unrelated tasks.

- **How would you fix it?** Use a dedicated, capacity-controlled executor for
  blocking work in Java 8, or consider virtual threads in Java 21+ while
  limiting downstream concurrency. Set timeouts and define overload behavior.

- **Would a custom ForkJoinPool solve everything?** It isolates worker usage
  but does not remove blocking or create more CPU capacity. Pool selection,
  workload type, and measured performance are separate decisions.

### Close resource-backed streams

- Collection, array, and generated streams usually need no explicit close.

- I/O-backed streams can own resources and should use try-with-resources.

```java
try (Stream<String> lines =
         Files.lines(path, StandardCharsets.UTF_8)) {
    long errors = lines
        .filter(line -> line.contains("ERROR"))
        .count();
}
```

### Later conveniences: do not attribute these to Java 8

|API|Release|Java 8 alternative|
|---|---:|---|
|`takeWhile`, `dropWhile`|9|express the bound another way or use a loop/custom spliterator|
|three-argument `Stream.iterate`|9|two-argument `iterate` plus a safe bound|
|`Collectors.toUnmodifiableList()`|10|collect, then wrap or copy deliberately|
|`Stream.toList()`|16|`collect(Collectors.toList())`, with different mutability guarantees|
|`mapMulti`|16|`flatMap`|

### Mastery check

- Can you identify source, intermediate operations, and terminal operation?

- Can you predict which elements a short-circuiting pipeline will inspect?

- Can you distinguish changing an ArrayList before traversal from interference
  during traversal?

- Can you explain why swapping `sorted` and `limit` changes results and may
  change termination?

- Can you explain why stream reuse is invalid even if no exception appears?

- Can you choose between `map`, `flatMap`, and a primitive mapping operation?

- Can you explain encounter order and the danger of shared mutation?

- Why can a lazy pipeline avoid intermediate collections without promising
  that every callback runs?

- Can you explain how a blocking parallel stream can delay unrelated futures?

- Can you distinguish pool implementations from factory configurations?

- Can you choose an executor, queue bound, and overload policy for blocking I/O?

- Can you name which table entries arrived after Java 8?

- **Practice:** [LazyFirstMatch.java](lab/09-stream-api/LazyFirstMatch.java).

---

## §E Collectors — what `collect` uses

Official reference: [Java 8 `Collectors` API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html).

### Mutable reduction

- **Why `collect` instead of adding to an outside list in `forEach`?** — The
  library needs to own how result containers are created and combined. A
  collector describes that process, allowing ordinary parallel collection to
  use separate containers rather than racing on one shared `ArrayList`.

- `collect` reduces elements into a mutable result container such as a list,
  set, map, or `StringBuilder`.

- A `Collector<T,A,R>` describes how values of type `T` accumulate through a
  mutable container of type `A` into a final result of type `R`.

```java
List<String> list = words.stream()
    .collect(Collectors.toList());

Set<String> set = words.stream()
    .collect(Collectors.toSet());

String joined = words.stream()
    .collect(Collectors.joining(", "));
```

### Group and partition

```java
Map<Integer, List<String>> byLength = words.stream()
    .collect(Collectors.groupingBy(String::length));

Map<Integer, Long> countByLength = words.stream()
    .collect(Collectors.groupingBy(
        String::length,
        Collectors.counting()));

Map<Boolean, List<String>> longAndShort = words.stream()
    .collect(Collectors.partitioningBy(word -> word.length() > 3));
```

- `groupingBy` produces one entry per classifier result that occurs.

- `partitioningBy` produces `false` and `true` partitions, including an empty
  partition when no element belongs to one side.

- A downstream collector reduces the values inside each group.

```java
Map<Integer, List<String>> upperByLength = words.stream().collect(
    Collectors.groupingBy(String::length,
        Collectors.mapping(word -> word.toUpperCase(Locale.ROOT), Collectors.toList())));
// For ["a", "bb", "c"]: length 1 -> ["A", "C"], length 2 -> ["BB"].
```

### `groupingBy` with parallel streams

- The word-count example using `words.stream()` is sequential. Switching to
  `parallelStream()` is valid without replacing `groupingBy`:

```java
Map<Integer, Long> countByLength = words.parallelStream()
    .collect(Collectors.groupingBy(
        String::length,
        Collectors.counting()));
```

- **Why is parallel `groupingBy` safe without a concurrent map?** —
  `groupingBy` is a **non-concurrent collector**, but parallel collection is
  safe. Each partition accumulates into an isolated result container, then the
  combiner merges partial results. A container is not mutated concurrently by
  multiple workers during accumulation.
  See the [Collector isolation contract](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html).

```text
partition ["a", "bb"] -> {1=1, 2=1}
partition ["c", "dd"] -> {1=1, 2=1}
combine counts       -> {1=2, 2=2}
```

- Safe collection does not make the returned map thread-safe for subsequent
  shared mutation. Its concrete map type and iteration order are not promised.

- Map merging can be expensive. If encounter order is unnecessary,
  `groupingByConcurrent` permits concurrent accumulation into a shared map:

```java
ConcurrentMap<Integer, Long> countByLength = words.parallelStream()
    .collect(Collectors.groupingByConcurrent(
        String::length,
        Collectors.counting()));
```

- This collector is concurrent and unordered. Counting does not depend on
  encounter order. Using it with `words.stream()` alone does not request
  parallel execution.

- **Concurrent does not guarantee faster.** With a non-concurrent downstream
  collector such as `counting()`, OpenJDK's implementation synchronizes updates
  to each group's accumulator. Many words mapping to the same lengths can
  contend on those groups.
  See the [OpenJDK groupingByConcurrent implementation](https://github.com/openjdk/jdk8u/blob/master/jdk/src/share/classes/java/util/stream/Collectors.java).

- Supplying `ConcurrentHashMap::new` to ordinary `groupingBy` changes the map
  implementation; it does not give that collector the `CONCURRENT`
  characteristic.

- For cheap `String::length` plus counting, start sequentially and measure
  before changing the execution mode or collector.
  See [Collectors grouping API notes](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html).

### Build maps and choose a duplicate policy

- **Why does `toMap` reject duplicates by default?** — A map can keep only one
  value for a key. Silently keeping or combining values would impose a business
  rule; supply a merge function when duplicates are valid.

- The two-argument `toMap` throws `IllegalStateException` when two elements map
  to equal keys.

```java
Map<String, Integer> lengthByWord = words.stream()
    .collect(Collectors.toMap(
        Function.identity(),
        String::length));
```

- Use the third argument when duplicates are possible. State the policy in code.

```java
Map<String, Integer> votesByCandidate = votes.stream()
    .collect(Collectors.toMap(
        Vote::getCandidate,
        Vote::getCount,
        Integer::sum));
```

- A fourth argument chooses the map implementation. Supply a suitable map
  factory when encounter or sorted iteration order is required.

```java
Map<String, Integer> sortedVotes = votes.stream()
    .collect(Collectors.toMap(
        Vote::getCandidate,
        Vote::getCount,
        Integer::sum,
        TreeMap::new));
```

- **Nulls:** define an input/null policy; collector behavior is not simply the
  backing map's null policy.

### Do not assume collector result mutability

- Java 8's `Collectors.toList()` promises a `List` in encounter order.

- It does **not** promise `ArrayList`, mutability, serializability, or thread
  safety.

- Request a concrete collection when those properties are part of the contract.

```java
ArrayList<String> mutable = words.stream()
    .collect(Collectors.toCollection(ArrayList::new));
```

- For protected structural ownership, choose an appropriate copy and wrapper;
  mutable elements need their own ownership policy.

### `reduce` versus `collect`

- **Why does `collect` take a supplier?** — Parallel work may need a fresh
  mutable container for each partition. A supplier can create those containers;
  a reduction identity is one neutral value, not a factory. Mutating the same
  seed can make partial results alias each other and break combination.

- Use `reduce` to combine elements into an immutable value.

- Use `collect` to accumulate into a mutable container.

```java
int sum = numbers.stream()
    .reduce(0, Integer::sum);

List<Integer> positives = numbers.stream()
    .filter(number -> number > 0)
    .collect(Collectors.toList());
```

- The identity must be neutral: `0` for addition. A non-neutral identity can
  contaminate every partial result in a parallel reduction.

- The operation must be associative. Subtraction fails: `(10 - 3) - 2` is `5`,
  while `10 - (3 - 2)` is `9`; regrouping work changes the answer.

- With the three-argument `reduce`, the accumulator and combiner must agree:
  `combiner.apply(u, accumulator.apply(identity, t))` must equal
  `accumulator.apply(u, t)`.

- Sequential execution may hide a broken reduction; parallel execution often
  exposes it.

- Do not use `reduce` by mutating and returning the same external list.

### Numeric reductions: overflow and rounding

- **Does a correct reduction guarantee the intended mathematical result?** —
  The reduction still uses Java's arithmetic types. Integer addition can
  overflow, and floating-point addition can lose information through rounding.

```java
int wrapped = IntStream.of(Integer.MAX_VALUE, 1).sum();
long widenedTooLate = (long) IntStream.of(Integer.MAX_VALUE, 1).sum();
long widenedBeforeAddition = IntStream.of(Integer.MAX_VALUE, 1)
    .asLongStream().sum();

System.out.println(wrapped);               // -2147483648
System.out.println(widenedTooLate);        // -2147483648
System.out.println(widenedBeforeAddition); // 2147483648
```

- Widen before addition when `long` covers the domain. Casting an already
  overflowed result preserves the wrong value. A `long` can also overflow;
  choose checked or arbitrary-precision arithmetic when the domain needs it.
  See [IntStream.sum](https://docs.oracle.com/javase/8/docs/api/java/util/stream/IntStream.html#sum--).

- Floating-point rounding makes addition non-associative for some inputs:

```java
double a = 1.0e16;
double b = -1.0e16;
double c = 1.0;

System.out.println((a + b) + c); // 1.0
System.out.println(a + (b + c)); // 0.0
```

- Regrouping work in parallel can round a floating-point total differently.
  Sequential and parallel calculations need not produce identical bit patterns.

- `DoubleStream.sum()` may use a summation technique that improves numerical
  accuracy; it is not specified as exactly the same calculation as
  `reduce(0.0, Double::sum)`. Choose numerical error tolerances or exact decimal
  arithmetic from the domain's requirements, rather than expecting a stream
  execution mode to solve rounding. See [DoubleStream.sum](https://docs.oracle.com/javase/8/docs/api/java/util/stream/DoubleStream.html#sum--).

<details>
<summary>Predict: does casting IntStream.sum() to long prevent integer overflow?</summary>

- No. The `int` additions have already occurred. Convert the elements to a
  sufficiently wide type before adding, then validate the result's range when
  the domain requires a narrower type.

</details>

### The collector lifecycle

|Part|Shape|Responsibility|
|---|---|---|
|supplier|`() → A`|create an empty accumulator|
|accumulator|`(A,T) → void`|add one stream element|
|combiner|`(A,A) → A`|merge partial accumulators|
|finisher|`A → R`|turn accumulator into final result|
|characteristics|metadata|describe concurrency, order, or identity finish|

```java
Collector<String, StringJoiner, String> bracketed = Collector.of(
    () -> new StringJoiner(", ", "[", "]"), // supplier
    StringJoiner::add,                        // accumulator
    StringJoiner::merge,                      // combiner
    StringJoiner::toString);                  // finisher

String result = words.stream().collect(bracketed);
```

### Collector characteristics

- `IDENTITY_FINISH` means the accumulator already has the final result type and
  the finisher can be treated as identity.

- `UNORDERED` means the result does not depend on encounter order.

- `CONCURRENT` means multiple threads may accumulate into the same result
  container under the conditions defined by the collector contract.

- **Why must characteristics be truthful?** — They permit different execution:
  shared accumulation, relaxed ordering, or skipping the finisher. A false
  declaration can change results or introduce races. Do not declare a
  characteristic unless the implementation truly satisfies it.

### Mastery check

- Can you explain why `Collectors.toList()` does not promise mutability?

- Can you write a duplicate-key merge policy for `toMap`?

- Can you choose between `reduce` and `collect`?

- Can you explain why widening after `sum` is too late and why floating-point
  regrouping can change a result?

- Why does a supplier solve an ownership problem that one mutable reduction
  seed does not?

- Can you explain why parallel `groupingBy` works without a concurrent map?

- Can you compare merging partial maps with concurrent grouping and key contention?

- Can you name the supplier, accumulator, combiner, and finisher in a custom
  collector?

- Can you explain why the combiner matters even when a sequential run appears
  correct?

- **Practice:** [GroupingSales.java](lab/09-stream-api/GroupingSales.java) ·
  [MergingVotes.java](lab/09-stream-api/MergingVotes.java) ·
  [ReduceTransactions.java](lab/09-stream-api/ReduceTransactions.java).

---

## §F Optional — "maybe absent," made explicit

Official reference: [Java 8 `Optional` API](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).

### Purpose

- `Optional<T>` represents either one non-null `T` or no value.

- **Why not just return null?** — It makes possible absence visible in a
  method's return type, so callers can compose the present case and choose an
  absent-case outcome explicitly.

- It does not make `null` impossible everywhere, and the compiler does not force
  callers to inspect it correctly.

```java
Optional<Account> findAccount(String id) {
    Account account = repository.lookup(id); // legacy API may return null
    return Optional.ofNullable(account);
}
```

### Create an Optional

```java
Optional<Account> known = Optional.of(account);          // rejects null
Optional<Account> maybe = Optional.ofNullable(account);  // null -> empty
Optional<Account> none = Optional.empty();
```

- **Why both `of` and `ofNullable`?** — `of` asserts that a value must exist
  and detects a violated assumption immediately. `ofNullable` adapts a boundary
  where null legitimately means absence.

### Transform without opening it

```java
Optional<String> activeBranch = findAccount(id)
    .filter(Account::isActive)
    .map(Account::getBranch)
    .map(branch -> branch.toUpperCase(Locale.ROOT));
```

- `filter` keeps the present value only when the predicate passes.

- `map` transforms a present value. A null mapper result becomes empty.

- `flatMap` is for a mapping function that already returns an `Optional`.
  Unlike `map`, a null result from its mapper throws `NullPointerException`.

```java
Optional<Card> card = findAccount(id)
    .flatMap(Account::findPrimaryCard);

// map(Account::findPrimaryCard) would produce Optional<Optional<Card>>.
```

### Exit deliberately

```java
Account first = findAccount(id)
    .orElse(Account.GUEST);

Account second = findAccount(id)
    .orElseGet(this::loadGuest);

Account required = findAccount(id)
    .orElseThrow(() -> new AccountNotFoundException(id));

findAccount(id).ifPresent(this::audit);
```

### `orElse` versus `orElseGet`

- **Why can't `orElse` skip an expensive argument?** — Java evaluates method
  arguments before entering the method. `orElseGet` receives a supplier and
  invokes it only when empty. Use `orElse` for a cheap available value, and
  `orElseGet` for work that should be lazy.

```java
Optional<String> found = Optional.of("Ada");
Supplier<String> fallback = () -> { System.out.println("loading"); return "guest"; };

found.orElse(fallback.get());                 // prints loading; returns Ada
found.orElseGet(fallback);                    // no print; returns Ada
Optional.<String>empty().orElseGet(fallback); // prints loading; returns guest
```

### `isPresent` and `get`

- `get()` throws `NoSuchElementException` when empty.

- Repeated `isPresent()` followed by `get()` often recreates a verbose null
  check and misses fluent operations such as `map`, `flatMap`, and `orElse`.

- It can still be reasonable at an imperative boundary where separate control
  flow is clearer. Treat it as a design smell to examine, not a forbidden pair.

### Design rules

- Prefer `Optional` as a return type when absence is a normal outcome.
  Return `Optional.empty()` for absence, never a null Optional reference.

- Avoid using it mechanically for every field or parameter; ordinary fields and
  explicit validation are usually clearer.

- Java 8 `Optional` is not `Serializable`, which makes it unsuitable for many
  entity and DTO fields.

- `Optional` is value based. Avoid reference identity checks, identity hash
  codes, and synchronization on Optional instances.

- Use `OptionalInt`, `OptionalLong`, and `OptionalDouble` when absence and a
  primitive result occur together.

### Later Optional conveniences

|API|Release|
|---|---:|
|`ifPresentOrElse`, `or`, `stream`|9|
|`orElseThrow()` without a supplier|10|
|`isEmpty`|11|

### Mastery check

- Can you choose between `of`, `ofNullable`, and `empty`?

- Can you explain when `map` creates an empty Optional?

- Can you prevent `Optional<Optional<T>>` with `flatMap`?

- Can you predict when an `orElse` argument executes?

- Can you justify whether a method should return `Optional<T>`?

- **Practice:** [OptionalAccountWorkflow.java](lab/08-advanced/OptionalAccountWorkflow.java).

---

## §G default / static methods in interfaces

Official reference: [JLS §9.4.3, Interface Method Body](https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.4.3).

### Default methods

- A `default` method has an implementation in an interface.

- **Why add method bodies to interfaces?** — It lets an interface evolve by
  adding useful behavior without requiring every existing implementation to
  provide a new method body. Adding an abstract operation could instead break
  implementors on recompilation. Java 8 needed operations such as `stream()`
  on existing collection contracts.

```java
interface Greeter {
    String name();

    default String greeting() {
        return "Hello, " + name();
    }
}
```

- Java 8 used default methods for APIs such as `Iterable.forEach`,
  `Collection.stream`, and `List.sort`.

### Static interface methods

- A static method belongs to the interface itself.

- It is called with the interface name and is not inherited as an instance
  method.

```java
interface Greeter {
    static String normalize(String name) {
        return name.trim();
    }
}

String name = Greeter.normalize("  Ada  ");
```

### Default-method resolution rules

- **Why can unrelated defaults require an override?** — If no concrete class
  method or more-specific interface resolves the conflict, there is no
  unambiguous inherited body. An override makes the selected or replacement
  behavior explicit.

- **Class wins:** an inherited or declared class instance method takes priority
  over an interface default.

- **More specific interface wins:** a child interface's override takes priority
  over the same default inherited from its parent.

- **Unrelated conflict requires an override:** independently defined defaults
  with the same signature must be resolved explicitly.

```java
interface ParentLabel { default String label() { return "parent"; } }
interface ChildLabel extends ParentLabel {
    default String label() { return "child"; }
}
class Base { public String label() { return "class"; } }
class ClassWins extends Base implements ChildLabel { }
class SpecificWins implements ParentLabel, ChildLabel { }

interface OtherLabel { default String label() { return "other"; } }
class Resolved implements ParentLabel, OtherLabel {
    public String label() {
        return ParentLabel.super.label() + "+" + OtherLabel.super.label();
    }
}

new ClassWins().label();    // class — inherited class method wins
new SpecificWins().label(); // child — more specific interface wins
new Resolved().label();     // parent+other — conflict resolved explicitly
```

### An abstract superclass method can suppress a default

- **Can an interface default fill an abstract superclass requirement?** — An
  inherited abstract class declaration takes precedence in method resolution
  too. A concrete subclass must supply an implementation even when an
  interface offers a matching default.

```java
interface DefaultLabel {
    default String label() { return "default"; }
}

abstract class RequiredLabel {
    public abstract String label();
}

// Does not compile: the concrete class has not implemented label().
// class MissingLabel extends RequiredLabel implements DefaultLabel { }

class SuppliedLabel extends RequiredLabel implements DefaultLabel {
    @Override
    public String label() { return "supplied"; }
}

System.out.println(new SuppliedLabel().label()); // supplied
```

- The superclass has required an implementation; the subclass cannot satisfy
  that requirement merely by listing an interface with a convenient default.
  It must implement the method or remain abstract. See [JLS inheritance of
  override-equivalent methods](https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.8.4).

<details>
<summary>Predict: if RequiredLabel instead provides a concrete label(), which body is inherited?</summary>

- The superclass body. With an abstract superclass declaration the concrete
  subclass must implement the method; with a concrete superclass declaration
  that inherited body supplies it. The interface default wins in neither case.

</details>

### Mastery check

- Can you explain why default methods were needed for API evolution?

- Can you apply all three resolution rules without compiling the example?

- Can you explain why an abstract superclass declaration can prevent use of an
  interface default?

- Can you explain why a static interface method is called through the interface
  name?

- **Practice:** [DefaultMethodResolution.java](lab/08-advanced/DefaultMethodResolution.java).

---

## §H java.time — the new date/time API

Official reference: [Java 8 `java.time` package](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html).

### Why it replaced common legacy usage

- `Date` and `Calendar` are mutable; sharing them across threads requires
  suitable coordination.

- `Calendar` exposes confusing conventions such as zero-based month constants.

- `SimpleDateFormat` is mutable and not thread-safe.

- The main `java.time` classes are immutable and thread-safe.

- Each type represents a specific date-time concept instead of placing unrelated
  concepts into one mutable object.

### Choose the type by meaning

- **Why separate date, local time, and instant types?** — A birthday, an audit
  timestamp, and an appointment in a region mean different things. Separate
  types make the intended meaning explicit before arithmetic or conversion.

|Type|Use it for|Example|
|---|---|---|
|`LocalDate`|calendar date without time or zone|birthday|
|`LocalTime`|wall-clock time without date or zone|shop opening time|
|`LocalDateTime`|local date and time without zone|appointment before zone is known|
|`Instant`|one point on the UTC timeline|audit timestamp|
|`ZonedDateTime`|local date/time plus region time-zone rules|scheduled event in Kolkata|
|`OffsetDateTime`|date/time plus a fixed UTC offset|serialized timestamp with offset|
|`Duration`|time-based amount in seconds/nanos|90-minute timeout|
|`Period`|date-based amount in years/months/days|one calendar month|
|`DateTimeFormatter`|thread-safe parsing and formatting|input/output boundary|

### Basic operations and immutability

```java
LocalDate today = LocalDate.now();
LocalDate expiry = today.plusDays(30);
Period age = Period.between(dateOfBirth, today);

DateTimeFormatter formatter =
    DateTimeFormatter.ofPattern("dd-MM-uuuu")
        .withResolverStyle(ResolverStyle.STRICT);

LocalDate parsed = LocalDate.parse("16-09-2025", formatter);
String rendered = parsed.format(formatter); // 16-09-2025
// LocalDate.parse("31-02-2025", formatter); // DateTimeParseException
```

- Strict parsing rejects impossible dates. `uuuu` is year; `yyyy` is year-of-era.

- Every apparent change returns a new value.

```java
LocalDate date = LocalDate.of(2025, 9, 16);

date.plusDays(30);        // discarded; date is unchanged
date = date.plusDays(30); // retained
```

### Date boundaries: patterns, parsing, and month ends

- **Why test a date formatter around New Year?** — Similar-looking pattern
  letters can describe different calendar fields. Ordinary dates may hide a
  wrong pattern until a boundary changes the displayed year.

|Pattern|Meaning|Decision|
|---|---|---|
|`uuuu`|year, including the proleptic year zero|use for an ISO calendar year without a separate era|
|`yyyy`|year-of-era|era matters, especially for strict parsing|
|`YYYY`|week-based year|use with a week-date model, not as a substitute for calendar year|
|`MM`|month-of-year|calendar month|
|`mm`|minute-of-hour|time field; a LocalDate alone has no minute|

```java
LocalDate boundary = LocalDate.of(2019, 12, 29);

System.out.println(boundary.format(
    DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US))); // 2019-12-29

System.out.println(boundary.format(
    DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.US))); // 2020-12-29
```

- Under this locale's week rules, the date belongs to week-year 2020. Mixing
  that year with calendar month/day creates misleading text. Week rules can
  depend on locale, so the example fixes `Locale.US` explicitly.

- Choose `uuuu-MM-dd` for this calendar-date representation, or an actual
  week-date representation such as `DateTimeFormatter.ISO_WEEK_DATE` when
  weeks are the intended model.

#### SMART parsing can adjust an invalid day

- `DateTimeFormatter.ofPattern(...)` uses `ResolverStyle.SMART` by default.
  SMART resolution can adjust a day such as February 30 to the month's last
  valid day. Choose STRICT when invalid calendar input must be rejected.

```java
DateTimeFormatter smart = DateTimeFormatter.ofPattern("uuuu-MM-dd");
System.out.println(LocalDate.parse("2025-02-30", smart)); // 2025-02-28

DateTimeFormatter strict = smart.withResolverStyle(ResolverStyle.STRICT);
try {
    LocalDate.parse("2025-02-30", strict);
} catch (DateTimeParseException expected) {
    System.out.println("rejected");
}
```

- SMART does not accept every invalid date. Select a resolver policy from the
  input contract rather than relying on the formatter's default. See
  [DateTimeFormatter patterns and resolution](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html).

#### Month arithmetic can lose the original day

```java
LocalDate january = LocalDate.of(2025, 1, 31);
LocalDate february = january.plusMonths(1);
LocalDate back = february.minusMonths(1);

System.out.println(january);  // 2025-01-31
System.out.println(february); // 2025-02-28
System.out.println(back);     // 2025-01-28
```

- February has no day 31, so `plusMonths` chooses the last valid day. The
  resulting date contains day 28; subtracting one month has no record of the
  original day-31 intention.

- For monthly billing, retain the intended day or an explicit end-of-month
  rule and calculate from that rule. Repeatedly advancing an adjusted date can
  drift from the intended schedule. See [LocalDate.plusMonths](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html#plusMonths-long-).

<details>
<summary>Predict: does January 31 becoming January 28 after a month round trip mean LocalDate was mutated?</summary>

- No. Every operation returned a new immutable value. Calendar adjustment
  discarded an invalid day-of-month; subtraction cannot reconstruct that
  discarded intention. The original `january` value is still January 31.

</details>

### Timeline versus local view

- **Why isn't `LocalDateTime` a global timestamp?** — It has no offset or
  zone, so it cannot identify one moment by itself. Supply the missing
  interpretation and resolve any gap or overlap when converting.

- Use `Instant` for a machine timestamp or one globally unambiguous moment.

- Attach a `ZoneId` when presenting or calculating that moment in a region.

- A `ZoneId` contains historical and daylight-saving rules.

- A `ZoneOffset` is only a fixed offset such as `+05:30`.

- **DST:** a local time can be invalid or ambiguous in a region; define the
  gap/overlap policy.

```java
Instant event = Instant.parse("2025-09-16T12:00:00Z");

ZonedDateTime inKolkata =
    event.atZone(ZoneId.of("Asia/Kolkata"));

Instant sameMoment = inKolkata.toInstant();
```

### `Duration` versus `Period`

- **Which promise are you making?** — A timeout promises elapsed time; a daily
  appointment promises a calendar relationship. Daylight-saving changes can
  make those promises differ.

- `Duration.ofHours(24)` means exactly 24 elapsed hours.

- `Period.ofDays(1)` means one calendar day.

- Across a daylight-saving transition, those operations can produce different
  local times in regions that change their offset.

```java
ZonedDateTime start = ZonedDateTime.of(
    2025, 3, 29, 12, 0, 0, 0,
    ZoneId.of("Europe/Paris"));

ZonedDateTime calendarDayLater = start.plus(Period.ofDays(1));
// 2025-03-30T12:00+02:00 — same local time

ZonedDateTime elapsedDayLater = start.plus(Duration.ofHours(24));
// 2025-03-30T13:00+02:00 — exactly 24 elapsed hours
```

### Inject a clock for testable time

- Calling `now()` directly reads the system clock.

- **Why inject a `Clock`?** — Current time and the interpreting zone are
  dependencies. Accepting a `Clock` lets tests control them; a fixed or otherwise
  controlled clock makes the result deterministic without waiting or changing
  machine settings.

```java
LocalDate billingDate(Clock clock) {
    return LocalDate.now(clock);
}

Clock fixed = Clock.fixed(
    Instant.parse("2025-09-16T00:00:00Z"),
    ZoneOffset.UTC);

LocalDate known = billingDate(fixed); // 2025-09-16
```

### Legacy interoperation

```java
Instant instant = legacyDate.toInstant();
Date legacyAgain = Date.from(instant);
```

### Mastery check

- Can you choose between `Instant`, `LocalDateTime`, and `ZonedDateTime`?

- Can you explain why an offset is not a time zone?

- Why is a local date/time insufficient for an audit timestamp that must
  identify one moment?

- Can you distinguish `Duration` from `Period` across daylight saving?

- Can you explain why ignored return values are a common `java.time` bug?

- Can you distinguish calendar year from week-year and choose SMART versus
  STRICT parsing deliberately?

- Can you explain why adding and subtracting one month need not be inverses?

- Can you make `LocalDate.now()` deterministic with a `Clock`?

- **Practice:** [ClockDrivenTime.java](lab/11-io-time/ClockDrivenTime.java),
  then [ZonedDstScheduling.java](lab/11-io-time/ZonedDstScheduling.java).

---

## §I CompletableFuture — compose asynchronous stages

Official reference: [Java 8 `CompletableFuture` API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

### Why it matters

- **Why `CompletableFuture` if `Future` already exists?** — `Future.get()`
  obtains one eventual value but encourages the caller to block before
  describing the next step. Completion stages let you describe dependencies
  without parking the caller between stages; blocking callbacks or an early
  `join()` can still lose that benefit.

- `CompletableFuture` implements both `Future` and `CompletionStage`.

- A pipeline can transform, flatten, combine, consume, and recover from
  asynchronous stages.

```java
// Future: submit, then block before continuing.
Future<User> future = executor.submit(() -> loadUser(id));
String firstEmail = future.get().getEmail();

// CompletableFuture: describe the continuation.
CompletableFuture<String> emailFuture = CompletableFuture
    .supplyAsync(() -> loadUser(id), ioExecutor)
    .thenApply(User::getEmail)
    .exceptionally(error -> "support@example.com");

String secondEmail = emailFuture.join();
```

### The core stage shapes

|Method|Use it when|Shape|
|---|---|---|
|`thenApply`|transform one completed value|T → U|
|`thenCompose`|the next function already returns a stage|T → stage of U|
|`thenCombine`|combine two independent results|(T,U) → V|
|`thenAccept`|consume a result|T → void|
|`thenRun`|run an action that needs no result|() → void|

```java
userFuture.thenApply(User::getEmail);

userFuture.thenCompose(this::loadOrdersAsync);

userFuture.thenCombine(limitFuture, this::createQuote);

userFuture.thenAccept(this::display);

userFuture.thenRun(() -> System.out.println("user stage finished"));
```

These `then*` callbacks run after normal completion; upstream failures propagate
past them until handled.

### `thenApply` versus `thenCompose`

- Use `thenApply` when the mapping function returns a plain value.

- **Why `thenCompose` here?** — The mapping function already returns another
  stage. Compose exposes its eventual value through one stage instead of
  making the caller manage a future inside a future.

- Composing stages on already-completed values does not itself launch
  background work; non-`Async` continuations can run immediately.

```java
CompletableFuture<CompletableFuture<List<Order>>> nested =
    userFuture.thenApply(this::loadOrdersAsync);

CompletableFuture<List<Order>> flat =
    userFuture.thenCompose(this::loadOrdersAsync);
```

### Failure handling

|Method|Runs on success?|Runs on failure?|Can replace result?|
|---|:---:|:---:|:---:|
|`exceptionally`|No|Yes|Yes, with a fallback value|
|`handle`|Yes|Yes|Yes|
|`whenComplete`|Yes|Yes|No recovery; observes the existing outcome|

```java
CompletableFuture<String> recovered = emailFuture
    .exceptionally(error -> "support@example.com");

CompletableFuture<String> inspected = emailFuture
    .handle((email, error) ->
        error == null ? email : "support@example.com");

CompletableFuture<String> observed = emailFuture
    .whenComplete((email, error) -> audit(email, error));
```

- `whenComplete` normally preserves the prior value or failure. It is suitable
  for logging and cleanup rather than recovery. If its callback throws, a prior
  success becomes a failure; a prior failure retains priority.

- Exceptions in a stage complete dependent stages exceptionally until a later
  stage handles the failure.

### Recovery position defines the failure boundary

- **Why can the same recovery callback produce a different outcome after
  moving it?** — Each continuation returns a dependent future. A recovery stage
  sees failures of its upstream computation, not failures thrown by stages
  attached after it.

```java
CompletableFuture<String> source = CompletableFuture.completedFuture("oops");

CompletableFuture<Integer> recoverBefore = source
    .exceptionally(error -> "0")
    .thenApply(Integer::parseInt);

CompletableFuture<Integer> recoverAfter = source
    .thenApply(Integer::parseInt)
    .exceptionally(error -> 0);

try {
    recoverBefore.join();
} catch (CompletionException expected) {
    System.out.println(expected.getCause().getClass().getSimpleName());
    // NumberFormatException
}
System.out.println(recoverAfter.join()); // 0
System.out.println(source.join());       // oops
```

- In `recoverBefore`, the source succeeds, so its recovery is skipped. Parsing
  then fails downstream of that recovery. In `recoverAfter`, the recovery sees
  the parse failure and replaces it with zero.

- A failure in a dependent stage does not retroactively fail its already
  successful source. Ignoring the returned future can hide the failed result:

```java
CompletableFuture<String> original = CompletableFuture.completedFuture("oops");
original.thenApply(Integer::parseInt); // failed dependent future is discarded
System.out.println(original.join());  // oops; this is not the parse result
```

- Retain, return, or attach outcome handling to the stage representing the
  work whose success matters. Inspecting only the initial future misses
  failures introduced by later transformations.

- Returning `null` from a recovery callback can complete its stage normally
  with a null value. A later callback that dereferences that value can fail
  again. Choose a meaningful fallback or propagate the failure rather than
  silently losing the error. See [CompletableFuture dependent stages and recovery](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

<details>
<summary>Predict: does exceptionally(error -&gt; null) guarantee that later stages succeed?</summary>

- No. It can replace one failure with a successful null result; later code
  still has its own contract and can throw, including when dereferencing null.
  Recovery placement determines which of those later failures are handled.

</details>

### Thread execution

- **Why choose an executor explicitly?** — Dependencies say when work becomes
  eligible; the executor decides where it runs and which capacity it competes
  for. An asynchronous dependency graph alone does not isolate workloads.

- A non-`Async` continuation may run on the thread that completes the previous
  stage or another caller involved in completion.

- An `*Async` overload without an explicit executor normally uses the common
  `ForkJoinPool`.

- Pass an explicit executor for blocking I/O, workload isolation, or controlled
  capacity. See [parallel streams and shared pools](#parallel-streams) for pool
  types, bounded queues, and the blocking-I/O example.

```java
CompletableFuture<User> userFuture = CompletableFuture
    .supplyAsync(() -> loadUser(id), ioExecutor);

CompletableFuture<View> viewFuture = userFuture
    .thenApplyAsync(this::render, cpuExecutor);
```

### `get` versus `join`

- `get()` follows the `Future` contract and throws checked
  `InterruptedException` and `ExecutionException`.

- `join()` throws unchecked `CompletionException` for exceptional completion.

- Both wait if the result is not ready, so join only at a deliberate boundary.
  `get(timeout, unit)` bounds the wait; a timeout does not cancel the task.

### Combining many stages

- `allOf` waits for all supplied futures, including when one fails early. If any
  fails, the combined future fails and a following `thenApply` does not run.

- Its result is `CompletableFuture<Void>`; read the typed results from the
  original futures after it completes.

```java
CompletableFuture<Void> all =
    CompletableFuture.allOf(userFuture, limitFuture);

CompletableFuture<Quote> quote = all.thenApply(ignored ->
    createQuote(userFuture.join(), limitFuture.join()));
```

- `anyOf` races completion, including exceptional completion; it does not select
  only successful results. Neither method automatically cancels other inputs.

```java
CompletableFuture<Object> firstFinished =
    CompletableFuture.anyOf(primaryFuture, backupFuture);
```

### Cancellation caveat

- **Why can work continue after `cancel()`?** — `CompletableFuture` has no
  direct control over the computation that eventually completes it. The result
  can be cancelled without owning the worker or external operation; stopping
  that work needs its own cooperative cancellation contract.

- `cancel(...)` records exceptional completion with `CancellationException`.

- The `mayInterruptIfRunning` argument has no effect in `CompletableFuture`;
  interrupts are not used to control its underlying processing.

### Mastery check

- Can you choose between `thenApply`, `thenCompose`, and `thenCombine`?

- Why describe a dependent stage instead of calling `join()` inside the
  previous callback?

- Can you explain the difference between `exceptionally`, `handle`, and
  `whenComplete`?

- Can you place recovery after the failures it should handle and identify which
  returned future represents the final outcome?

- Can you predict which executor an asynchronous stage uses?

- Can you explain the exception difference between `get` and `join`?

- Can you explain why cancellation may not stop the underlying task?

- **Practice:** [CompletableFutureComposition.java](lab/12-concurrency-async/CompletableFutureComposition.java).

---

## §J Other Java 8 additions worth knowing

### Collection and map operations

- **Why `compute` and `merge` instead of get–test–put code?** — They express
  absent/present update policies in one operation. Atomicity still depends on
  the map's contract; these helpers do not make an ordinary `HashMap`
  thread-safe.

```java
names.removeIf(String::isEmpty);
names.replaceAll(String::trim);
names.sort(String.CASE_INSENSITIVE_ORDER);

Map<String, Integer> counts = new HashMap<>();
counts.merge("java", 1, Integer::sum);          // 1 — absent: insert
counts.merge("java", 1, Integer::sum);          // 2 — present: combine
counts.merge("java", 1, (old, added) -> null);  // null — remove the mapping
usersById.computeIfAbsent(id, this::loadUser);
usersById.computeIfPresent(id, (key, user) -> refresh(user));
usersById.forEach((id, user) -> audit(id, user));
```

- `computeIfAbsent` also computes when a key currently maps to `null`.

- A null result from its mapping function inserts nothing; an existing null
  mapping remains. `computeIfPresent` invokes its callback only for a non-null
  existing value; returning null from that callback removes the mapping.

- `merge` requires a non-null supplied value. It inserts that value when no
  non-null value exists; otherwise it invokes the remapping function.

- A null result from the merge function removes the mapping.

### Concurrent map callbacks: atomic installation has limits

- **Why can a thread-safe map still cause a stalled request or a race?** —
  Map atomicity covers the operation the map promises. It does not make an
  arbitrary callback cheap, and it does not control every subsequent operation
  on an object stored as a value.

#### Keep computation callbacks short

- `ConcurrentHashMap.computeIfAbsent` atomically installs an absent mapping.
  Some attempted updates can block while its computation runs, so the callback
  should be short and simple. A slow HTTP/JDBC lookup inside it can extend that
  coordination; this does not mean the entire map is globally locked.

- Avoid recursive updates or modifying other mappings of the same map from
  the computation. Detectable recursive updates can throw
  `IllegalStateException`; detection is not a design strategy.

- A computation returning null or throwing does not establish a mapping.
  Another invocation may compute again; removal also permits another load.
  Atomic computation is not a promise of one load per key for the map's
  entire lifetime. See [ConcurrentHashMap.computeIfAbsent](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html#computeIfAbsent-K-java.util.function.Function-).

- If duplicate remote loads are acceptable, one option is to load outside the
  computation and atomically publish with `putIfAbsent`. Assume `cache` is a
  `ConcurrentMap<String, String>` and `fetchRemote(id)` returns a non-null value
  or throws:

```java
String value = cache.get(id);
if (value == null) {
    String loaded = fetchRemote(id); // outside a map computation callback
    String existing = cache.putIfAbsent(id, loaded);
    value = existing == null ? loaded : existing;
}
```

- Two callers can both observe absence and perform that load; only one value
  is installed while the mapping remains present. This policy allows duplicate
  work. If loads must be coalesced, design a cache/loading contract with shared
  in-progress results, failure handling, capacity, and expiry. The map helper
  alone does not supply those policies.

- Running the load outside the map callback does not make it nonblocking.
  Choose its execution capacity using the
  [blocking-I/O executor rules](#fix-blocking-io-in-java-8-use-an-explicit-executor).

#### A concurrent map does not make its values concurrent

```java
ConcurrentMap<String, List<String>> unsafeGroups = new ConcurrentHashMap<>();
unsafeGroups.computeIfAbsent("java", key -> new ArrayList<>()).add("A");
// The map operation is atomic; concurrent calls to this ArrayList.add are unsafe.
```

- The `add` occurs on the returned list after the map operation. Other threads
  can receive the same list and race on it. Atomic publication of a reference
  does not make that list's methods thread-safe.

- If the requirement is concurrent individual additions, choose a concurrent
  value container as well:

```java
ConcurrentMap<String, Queue<String>> groups = new ConcurrentHashMap<>();
groups.computeIfAbsent("java", key -> new ConcurrentLinkedQueue<>()).add("A");
System.out.println(groups.get("java")); // [A]
```

- `ConcurrentLinkedQueue` supports concurrent individual additions. A larger
  invariant such as checking a per-group limit and then adding still needs its
  own atomic design. See [ConcurrentLinkedQueue](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html).

<details>
<summary>Predict: two computeIfAbsent calls use a callback that returns null; does the first call prevent another computation?</summary>

- No. Null installs no mapping in `ConcurrentHashMap`, so the second call can
  run the function again. Callbacks with irreversible side effects require a
  separate correctness contract; map atomicity does not make them exactly-once.

</details>

### Standard Base64 API

- **Why encode binary data as Base64?** — Some formats and transports need
  text rather than arbitrary bytes. Base64 supplies a reversible representation
  for that boundary; the basic, URL-safe, and MIME forms serve different
  formatting rules.

```java
String token = Base64.getUrlEncoder()
    .withoutPadding()
    .encodeToString(bytes);

byte[] decoded = Base64.getUrlDecoder().decode(token);
```

- Java 8 provides basic, URL-safe, and MIME encoders and decoders.

- Base64 is an encoding. It is not encryption and is not password hashing.

### Repeatable and type-use annotations

- `@Repeatable` permits the same annotation type more than once on one element.

- Type-use annotations can appear anywhere a type is used, such as
  `List<@NonNull String>`, for frameworks and static analysis tools.
  The annotation alone does not add runtime null checks.

### Parameter-name reflection

- **Why does reflection sometimes report `arg0`?** — Source parameter names
  are optional class-file metadata. Types alone cannot recover the original
  names; the compiler must retain them for reflection.

- Compile with `javac -parameters` to retain source parameter names for
  reflection.

- Without that flag, reflection generally exposes synthetic names such as
  `arg0`.

### Arrays and utility types

- `Arrays.parallelSort(values)` performs a parallel sort.

- `Arrays.parallelPrefix(values, operation)` computes cumulative values.

- `StringJoiner` builds delimited text with optional prefix and suffix.

- `Spliterator` supports partitioned traversal and underpins streams.

### Concurrency additions

- **Why `LongAdder` instead of one `AtomicLong`?** — `LongAdder` and
  `DoubleAdder` scale counters under high contention by using multiple internal
  cells. Distributing updates can improve throughput at extra memory cost.

- Their sums are not atomic snapshots while updates are in progress, so use
  them for statistics rather than synchronization decisions.
  See the [LongAdder contract](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/LongAdder.html).

- `StampedLock` adds optimistic reads but is not reentrant and requires careful
  stamp validation and release.

  - **Why optimistic reads?** Short reads can avoid acquiring a read lock when
    validation succeeds. The price is validation plus retry/fallback logic;
    skipping validation loses the intended guarantee.

- `CompletableFuture` provides asynchronous stage composition, as covered in
  §I.

### JVM and tooling changes

- In HotSpot Java 8, Permanent Generation was removed. Class metadata moved to
  native Metaspace, which can still be exhausted.

- `-XX:MaxMetaspaceSize` can place an upper bound on Metaspace when required.

- Nashorn was introduced as a JavaScript engine in Java 8, deprecated in Java
  11, and removed in Java 15. Treat it as historical for new development.

### Mastery check

- Can you explain the null behavior of `computeIfAbsent` and `merge`?

- Can you distinguish atomic map installation, duplicate loading, and safe
  concurrent mutation of the stored value?

- Can you distinguish Base64 encoding from security mechanisms?

- Can you state what `-parameters` changes?

- Can you explain why `LongAdder` helps under contention?

---

## §K The LTS list — cheap to memorize, certain to be asked

- **Why track feature releases as well as LTS?** — A support choice does not
  change when an API became available. A newer IDE JDK can accept code that
  fails on an older deployment target; choose features and compilation targets
  deliberately.

- Java LTS releases through Java 25 are **8, 11, 17, 21, and 25**.

- Their release years are **2014, 2018, 2021, 2023, and 2025**.

- Since Java 17, the LTS cadence has been two years: 17 → 21 → 25.

### Nearby version traps

| Convenience or feature | First permanent release |
|---|---:|
| String `switch` | 7 |
| `List.of`, `Map.of`, private interface helpers | 9 |
| `List.copyOf`, local-variable `var` | 10 |
| `Optional.isEmpty` | 11 |
| Switch expressions | 14 |
| Records (shallowly immutable data carriers), `Stream.toList` | 16 |
| Sealed classes | 17 |
| Virtual threads, record patterns, pattern switch, sequenced collections | 21 |
| Flexible constructor bodies, compact files/instance main | 25 |

- **`Stream.toList()` — Java 16+:** returns an unmodifiable list; its contract
  differs from `Collectors.toList()`.

- Keep release chronology in [java-versions.md](java-versions.md); it does not
  change the Java 8 API contracts taught here.

---

## Precision card

|Prompt|Precise answer|
|---|---|
|What is a lambda?|An implementation of a target functional interface|
|What makes an interface functional?|One effective abstract method|
|When does a stream run?|When a terminal operation requests a result|
|Can a stream be reused?|No|
|`map` or `flatMap`?|Plain result → `map`; stream/Optional result → `flatMap`|
|`reduce` or `collect`?|Immutable value → `reduce`; mutable container → `collect`|
|Why avoid blocking I/O in parallel streams?|It can occupy shared common-pool workers and delay unrelated work|
|Does fixed pool size bound queued work?|No; `newFixedThreadPool` uses an unbounded queue|
|Is parallel `groupingBy` safe?|Yes; isolated partial accumulators are combined|
|Does `Collectors.toList()` guarantee mutability?|No|
|`orElse` or `orElseGet`?|Eager value → `orElse`; lazy supplier → `orElseGet`|
|`thenApply` or `thenCompose`?|Plain result → apply; future result → compose|
|`Duration` or `Period`?|Elapsed timeline amount → duration; calendar amount → period|

## Final mastery checklist

You have solid Java 8 knowledge when you can do all of these without copying a
prepared answer:

- Define a functional interface and choose the correct built-in interface by
  shape.

- Convert between anonymous classes, lambdas, and all four method-reference
  forms.

- Explain target typing, effectively-final capture, and lambda `this`.

- Trace a lazy, short-circuiting stream pipeline element by element.

- Choose among `map`, `flatMap`, primitive streams, `reduce`, and `collect`.

- Build grouped, partitioned, and duplicate-safe map results.

- Explain shared-pool contention and choose a capacity-controlled executor for
  blocking I/O.

- Explain why parallel collection can be safe without a concurrent result map.

- Explain every function in a custom collector and why its combiner is correct.

- Use `Optional` without eager fallback work or unnecessary nesting.

- Resolve all default-method inheritance cases.

- Choose a date-time type from the meaning of the data.

- Compose dependent and independent asynchronous operations and handle failure.

- Identify whether a convenient API belongs to Java 8 or a later release.
