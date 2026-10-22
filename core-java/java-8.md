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
  chronological view and [core-java.md §4](core-java.md#4-java-8-know-it-cold)
  for rapid interview review.

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

- **Practice:** complete each TODO or rewrite each solved exercise from memory in
  this order:

  1. [LambdaPredicates.java](lab/08-advanced/LambdaPredicates.java)
  2. [FilterMapPipeline.java](lab/09-stream-api/FilterMapPipeline.java)
  3. [GroupingSales.java](lab/09-stream-api/GroupingSales.java)
  4. [CustomBracketCollector.java](lab/09-stream-api/CustomBracketCollector.java)
  5. [OptionalAccountWorkflow.java](lab/08-advanced/OptionalAccountWorkflow.java)
  6. [DefaultMethodResolution.java](lab/08-advanced/DefaultMethodResolution.java)

- **Then reinforce the remaining concepts:** these existing labs use a JDK 21
  harness, while the APIs being practiced originated in Java 8:

  1. [MultiFieldInventorySort.java](lab/09-stream-api/MultiFieldInventorySort.java)
  2. [ClockDrivenTime.java](lab/11-io-time/ClockDrivenTime.java)
  3. [ZonedDstScheduling.java](lab/11-io-time/ZonedDstScheduling.java)
  4. [CompletableFutureComposition.java](lab/12-concurrency-async/CompletableFutureComposition.java)

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

### Definition

- A **functional interface** has one effective abstract method, commonly called
  its **SAM**: Single Abstract Method.

- Abstract methods matching public methods of `Object` do not count toward the
  SAM.

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

- `Predicate.and` and `Predicate.or` short-circuit from left to right.

- `Predicate.negate` reverses the result.

- `Function.andThen` runs the current function first.

- `Function.compose` runs its argument first.

```java
Predicate<String> notNull = Objects::nonNull;
Predicate<String> notEmpty = text -> !text.isEmpty();
Predicate<String> usable = notNull.and(notEmpty);

Function<String, String> trim = String::trim;
Function<String, Integer> cleanLength = trim.andThen(String::length);

cleanLength.apply("  Java  "); // 4
```

### Comparator composition

- Java 8 made multi-field sorting declarative through static and default methods
  on `Comparator`.

```java
Comparator<Person> byDepartmentThenName =
    Comparator.comparing(Person::getDepartment)
              .thenComparing(Person::getName);

people.sort(byDepartmentThenName);
people.sort(byDepartmentThenName.reversed());
```

- Reverse only one key by reversing that key's comparator:

```java
Comparator<Person> byDepartmentThenSalaryDesc =
    Comparator.comparing(Person::getDepartment)
              .thenComparing(
                  Comparator.comparingInt(Person::getSalary).reversed());
```

### Mastery check

- Can you choose the correct interface from only the input and output shapes?

- Can you explain why `@FunctionalInterface` is useful but optional?

- Can you distinguish `Function<T,T>` from `UnaryOperator<T>`?

- Can you predict the order of `compose` and `andThen`?

---

## §B Lambda — the compact syntax

Official reference: [JLS §15.27, Lambda Expressions](https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.27).

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

- A lambda may capture a local variable or parameter only when it is `final` or
  **effectively final**: it is not assigned again after initialization or its
  one definite assignment.

- The captured value may be an object reference. The object itself can still be
  mutable, although shared mutation is usually a poor stream design.

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

### The four forms

|Kind|Method reference|Equivalent lambda|
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

### When to use one

- Use a method reference when the lambda only forwards its arguments to one
  existing method or constructor.

- Keep the lambda when it makes argument order, adaptation, or extra behavior
  clearer.

```java
Function<String, Integer> clear = text -> parse(text.trim());

// This cannot become only parse::... because it performs an extra trim step.
```

- Overloaded methods still rely on the target functional interface to choose a
  compatible signature.

### Mastery check

- Can you expand each of the four method-reference forms into a lambda?

- Can you identify the receiver in `String::compareTo`?

- Can you explain why some one-line lambdas are clearer when left as lambdas?

---

## §D Stream API — the payoff

Official reference: [Java 8 Stream package](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html).

### Exact mental model

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

  ```java
  Stream<String> stream = names.stream();
  long count = stream.count();

  // Invalid: the stream has already been consumed.
  // Optional<String> first = stream.findFirst();

  // Ask the reusable source for a new stream instead.
  Optional<String> first = names.stream().findFirst();
  ```

- **Non-interfering and usually stateless:** behavioral parameters must not
  modify the source, and their results should not depend on changing shared
  state.

  ```java
  // Avoid this shared mutation.
  List<String> output = new ArrayList<>();
  names.stream().filter(name -> {
      output.add(name);
      return true;
  }).count();
  ```

### `map` versus `flatMap`

- `map` transforms each input into exactly one output value.

- `flatMap` transforms each input into a stream of zero or more values, then
  flattens those streams into one stream.

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4));

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

- `limit`, matching operations, and finding operations can stop traversal early.

- An infinite stream needs a short-circuiting path before it can produce a
  finite result.

### Primitive streams

- `IntStream`, `LongStream`, and `DoubleStream` avoid boxing every numeric
  element.

- They provide numeric terminals such as `sum`, `average`, `min`, `max`, and
  `summaryStatistics`.

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

### Side effects and `peek`

- Prefer pipelines that compute and return values.

- Use `forEach` when a side effect is the actual terminal purpose.

- Use `peek` mainly to observe a pipeline while debugging.

- Do not make program correctness depend on `peek` executing for every source
  element; short-circuiting may stop before later elements are requested.

### Parallel streams

- Parallel execution is useful only when the work is large enough, CPU-bound,
  splittable, and expressed with associative operations.

- It often performs worse for small inputs, blocking I/O, ordered work, or
  pipelines with shared mutable state.

- The common `ForkJoinPool` is used by default.

- Benchmark the real workload before keeping `parallelStream()`.

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

- Can you explain why stream reuse is invalid even if no exception appears?

- Can you choose between `map`, `flatMap`, and a primitive mapping operation?

- Can you explain encounter order and the danger of shared mutation?

- Can you name which table entries arrived after Java 8?

---

## §E Collectors — what `collect` uses

Official reference: [Java 8 `Collectors` API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html).

### Mutable reduction

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

### Build maps and choose a duplicate policy

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

- A fourth argument chooses the map implementation.

```java
Map<String, Integer> sortedVotes = votes.stream()
    .collect(Collectors.toMap(
        Vote::getCandidate,
        Vote::getCount,
        Integer::sum,
        TreeMap::new));
```

### Do not assume collector result mutability

- Java 8's `Collectors.toList()` promises a `List` in encounter order.

- It does **not** promise `ArrayList`, mutability, serializability, or thread
  safety.

- Request a concrete collection when those properties are part of the contract.

```java
ArrayList<String> mutable = words.stream()
    .collect(Collectors.toCollection(ArrayList::new));
```

### `reduce` versus `collect`

- Use `reduce` to combine elements into an immutable value.

- Use `collect` to accumulate into a mutable container.

```java
int sum = numbers.stream()
    .reduce(0, Integer::sum);

List<Integer> positives = numbers.stream()
    .filter(number -> number > 0)
    .collect(Collectors.toList());
```

- The identity must be neutral.

- The reduction operation must be associative by contract.

- Sequential execution may hide a broken reduction; parallel execution often
  exposes it.

- Do not use `reduce` by mutating and returning the same external list.

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

- Do not declare a characteristic unless the implementation truly satisfies it.

### Mastery check

- Can you explain why `Collectors.toList()` does not promise mutability?

- Can you write a duplicate-key merge policy for `toMap`?

- Can you choose between `reduce` and `collect`?

- Can you name the supplier, accumulator, combiner, and finisher in a custom
  collector?

- Can you explain why the combiner matters even when a sequential run appears
  correct?

---

## §F Optional — "maybe absent," made explicit

Official reference: [Java 8 `Optional` API](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).

### Purpose

- `Optional<T>` represents either one non-null `T` or no value.

- It makes possible absence visible in a method's return type.

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

### Transform without opening it

```java
Optional<String> activeBranch = findAccount(id)
    .filter(Account::isActive)
    .map(Account::getBranch)
    .map(String::toUpperCase);
```

- `filter` keeps the present value only when the predicate passes.

- `map` transforms a present value. A null mapper result becomes empty.

- `flatMap` is for a mapping function that already returns an `Optional`.

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

- Java evaluates method arguments before the method call.

- `orElse(loadGuest())` therefore calls `loadGuest()` even when the Optional is
  present.

- `orElseGet(this::loadGuest)` invokes the supplier only when empty.

- Use `orElse` for an already available cheap value.

- Use `orElseGet` for work that should be lazy.

### `isPresent` and `get`

- `get()` throws `NoSuchElementException` when empty.

- Repeated `isPresent()` followed by `get()` often recreates a verbose null
  check and misses fluent operations such as `map`, `flatMap`, and `orElse`.

- It can still be reasonable at an imperative boundary where separate control
  flow is clearer. Treat it as a design smell to examine, not a forbidden pair.

### Design rules

- Prefer `Optional` as a return type when absence is a normal outcome.

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

- It lets an interface evolve by adding useful behavior without requiring every
  existing implementation to provide a new method body.

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

- **Class wins:** an inherited or declared class instance method takes priority
  over an interface default.

- **More specific interface wins:** a child interface's override takes priority
  over the same default inherited from its parent.

- **Unrelated conflict requires an override:** independently defined defaults
  with the same signature must be resolved explicitly.

```java
interface Left {
    default String label() {
        return "left";
    }
}

interface Right {
    default String label() {
        return "right";
    }
}

class Both implements Left, Right {
    @Override
    public String label() {
        return Left.super.label();
    }
}
```

### Mastery check

- Can you explain why default methods were needed for API evolution?

- Can you apply all three resolution rules without compiling the example?

- Can you explain why a static interface method is called through the interface
  name?

- **Practice:** [DefaultMethodResolution.java](lab/08-advanced/DefaultMethodResolution.java).

---

## §H java.time — the new date/time API

Official reference: [Java 8 `java.time` package](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html).

### Why it replaced common legacy usage

- `Date` and `Calendar` are mutable.

- `Calendar` exposes confusing conventions such as zero-based month constants.

- `SimpleDateFormat` is mutable and not thread-safe.

- The main `java.time` classes are immutable and thread-safe.

- Each type represents a specific date-time concept instead of placing unrelated
  concepts into one mutable object.

### Choose the type by meaning

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
    DateTimeFormatter.ofPattern("dd-MM-yyyy");

String rendered = LocalDate.of(2025, 9, 16)
    .format(formatter); // 16-09-2025
```

- Every apparent change returns a new value.

```java
LocalDate date = LocalDate.of(2025, 9, 16);

date.plusDays(30);        // discarded; date is unchanged
date = date.plusDays(30); // retained
```

### Timeline versus local view

- Use `Instant` for a machine timestamp or one globally unambiguous moment.

- Attach a `ZoneId` when presenting or calculating that moment in a region.

- A `ZoneId` contains historical and daylight-saving rules.

- A `ZoneOffset` is only a fixed offset such as `+05:30`.

```java
Instant event = Instant.parse("2025-09-16T12:00:00Z");

ZonedDateTime inKolkata =
    event.atZone(ZoneId.of("Asia/Kolkata"));

Instant sameMoment = inKolkata.toInstant();
```

### `Duration` versus `Period`

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

- Accepting a `Clock` makes the result deterministic in tests and simulations.

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

- Can you distinguish `Duration` from `Period` across daylight saving?

- Can you explain why ignored return values are a common `java.time` bug?

- Can you make `LocalDate.now()` deterministic with a `Clock`?

- **Practice:** [ClockDrivenTime.java](lab/11-io-time/ClockDrivenTime.java),
  then [ZonedDstScheduling.java](lab/11-io-time/ZonedDstScheduling.java).

---

## §I CompletableFuture — compose asynchronous stages

Official reference: [Java 8 `CompletableFuture` API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

### Why it matters

- `Future.get()` obtains one eventual value but encourages the caller to block
  before describing the next step.

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
```

### `thenApply` versus `thenCompose`

- Use `thenApply` when the mapping function returns a plain value.

- Use `thenCompose` when the mapping function returns another stage.

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
  for logging and cleanup rather than recovery.

- Exceptions in a stage complete dependent stages exceptionally until a later
  stage handles the failure.

### Thread execution

- A non-`Async` continuation may run on the thread that completes the previous
  stage or another caller involved in completion.

- An `*Async` overload without an explicit executor normally uses the common
  `ForkJoinPool`.

- Pass an explicit executor for blocking I/O, workload isolation, or controlled
  capacity.

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

### Combining many stages

- `allOf` completes when all supplied futures complete.

- Its result is `CompletableFuture<Void>`; read the typed results from the
  original futures after it completes.

```java
CompletableFuture<Void> all =
    CompletableFuture.allOf(userFuture, limitFuture);

CompletableFuture<Quote> quote = all.thenApply(ignored ->
    createQuote(userFuture.join(), limitFuture.join()));
```

### Cancellation caveat

- `CompletableFuture` has no direct control over the computation that eventually
  completes it.

- `cancel(...)` records exceptional completion with `CancellationException`.

- The `mayInterruptIfRunning` argument has no effect in `CompletableFuture`;
  interrupts are not used to control its underlying processing.

### Mastery check

- Can you choose between `thenApply`, `thenCompose`, and `thenCombine`?

- Can you explain the difference between `exceptionally`, `handle`, and
  `whenComplete`?

- Can you predict which executor an asynchronous stage uses?

- Can you explain the exception difference between `get` and `join`?

- Can you explain why cancellation may not stop the underlying task?

- **Practice:** [CompletableFutureComposition.java](lab/12-concurrency-async/CompletableFutureComposition.java).

---

## §J Other Java 8 additions worth knowing

### Collection and map operations

```java
names.removeIf(String::isEmpty);
names.replaceAll(String::trim);
names.sort(String.CASE_INSENSITIVE_ORDER);

counts.merge(word, 1, Integer::sum);
usersById.computeIfAbsent(id, this::loadUser);
usersById.computeIfPresent(id, (key, user) -> refresh(user));
usersById.forEach((id, user) -> audit(id, user));
```

- `computeIfAbsent` also computes when a key currently maps to `null`.

- A null result from its mapping function creates no mapping.

- `merge` requires a non-null supplied value. It inserts that value when no
  non-null value exists; otherwise it invokes the remapping function.

- A null result from the merge function removes the mapping.

### Standard Base64 API

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

### Parameter-name reflection

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

- `LongAdder` and `DoubleAdder` scale counters under high contention by using
  multiple internal cells.

- Their sums are snapshots rather than atomic coordination points, so use them
  for statistics rather than synchronization decisions.

- `StampedLock` adds optimistic reads but is not reentrant and requires careful
  stamp validation and release.

- `CompletableFuture` provides asynchronous stage composition, as covered in
  §I.

### JVM and tooling changes

- Permanent Generation was removed. Class metadata moved to native Metaspace.

- `-XX:MaxMetaspaceSize` can place an upper bound on Metaspace when required.

- Nashorn was introduced as a JavaScript engine in Java 8, deprecated in Java
  11, and removed in Java 15. Treat it as historical for new development.

### Mastery check

- Can you explain the null behavior of `computeIfAbsent` and `merge`?

- Can you distinguish Base64 encoding from security mechanisms?

- Can you state what `-parameters` changes?

- Can you explain why `LongAdder` helps under contention?

---

## §K The LTS list — cheap to memorize, certain to be asked

- Java LTS releases through Java 25 are **8, 11, 17, 21, and 25**.

- Their release years are **2014, 2018, 2021, 2023, and 2025**.

- Since Java 17, the LTS cadence has been two years: 17 → 21 → 25.

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

- Explain every function in a custom collector and why its combiner is correct.

- Use `Optional` without eager fallback work or unnecessary nesting.

- Resolve all default-method inheritance cases.

- Choose a date-time type from the meaning of the data.

- Compose dependent and independent asynchronous operations and handle failure.

- Identify whether a convenient API belongs to Java 8 or a later release.
