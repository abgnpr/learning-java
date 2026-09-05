# Core Java labs ☕🧪

This directory contains 90 runnable Java labs arranged as a learning path:
82 self-testing challenge starters and eight concurrency stations. Each lab
uses only the Java 21 standard library. The task descriptions are
self-contained, original practice contracts; the challenge names and ordering
form the curriculum map.

## Directory map

| Section | Topic | Labs |
|---:|---|---:|
| 01 | Introduction | 13 |
| 02 | Strings and regular expressions | 11 |
| 03 | Big numbers | 3 |
| 04 | Data structures | 15 |
| 05 | Object-oriented programming | 8 |
| 06 | Exception handling | 2 |
| 07 | Threads and concurrency | 8 |
| 08 | Advanced Java | 12 |
| 09 | Stream API | 18 |
| | **Total** | **90** |

`Scratchpad.java` is available for experiments and is not counted as a lab.

## How to run

Open a source file, read its task and acceptance checks, then complete the
marked `TODO`. Run that file directly from `core-java/lab/`:

```bash
cd core-java/lab
java 01-introduction/WelcomeToJava.java
```

An untouched starter throws `UnsupportedOperationException` or fails a
structural check. A completed challenge prints its completion line after every
embedded check succeeds.

Compile all 82 challenge starters without writing class files into the
repository:

```bash
cd core-java/lab
challenge_build=$(mktemp -d)
find . -name '*.java' ! -path './07-threads/*' ! -name 'Scratchpad.java' \
  -print0 | xargs -0 javac -d "$challenge_build"
```

## Drill protocol

1. **READ** — state the contract and edge cases in your own words.
2. **PREDICT** — inspect the checks before touching the implementation.
3. **BUILD** — replace only the marked starter seam or seams.
4. **RUN** — earn the completion line; do not weaken an acceptance check.
5. **EXTEND** — add one edge case that the supplied checks do not cover.
6. **EXPLAIN** — name the Java API or language rule that made the solution
   work.

The source files contain no answer key. Keep each exercise independent so
it remains runnable through Java's single-file source launcher.

### Assisted review

Steps 4–6 have an optional assisted form: the `/lab-review` skill, defined in
[.claude/skills/lab-review/SKILL.md](../../.claude/skills/lab-review/SKILL.md).
Invoke it after your own BUILD to run the challenge, critique the solution,
annotate it with why-comments, and update this file's progress and revision
tables.

The skill never writes the implementation — that is the rep, and it belongs to
the reader. A better approach gets described and left for the reader to accept
or reject. Before the attempt, it gives the smallest hint that unblocks rather
than the answer.

Planned lab extensions and the payment-ledger capstone are scoped in the
[Core Java lab roadmap](ROADMAP.md).

## Track

The order moves from syntax and standard input through collections and object
design to concurrency, reflection, annotations, lambdas, cryptographic hashes,
and the Stream API. Challenge numbers 01–82 cover sections 01–06 and 08–09;
section 07 uses station numbers S1–S8 and has a separate
[predict-run-explain guide](07-threads/README.md).

### Introduction

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 01 | Welcome to Java! | Easy | [WelcomeToJava.java](01-introduction/WelcomeToJava.java) |
| 02 | Stdin and Stdout I | Easy | [StdinStdoutOne.java](01-introduction/StdinStdoutOne.java) |
| 03 | If-Else | Easy | [IfElse.java](01-introduction/IfElse.java) |
| 04 | Stdin and Stdout II | Easy | [StdinStdoutTwo.java](01-introduction/StdinStdoutTwo.java) |
| 05 | Output Formatting | Easy | [OutputFormatting.java](01-introduction/OutputFormatting.java) |
| 06 | Loops I | Easy | [LoopsOne.java](01-introduction/LoopsOne.java) |
| 07 | Loops II | Easy | [LoopsTwo.java](01-introduction/LoopsTwo.java) |
| 08 | Datatypes | Easy | [Datatypes.java](01-introduction/Datatypes.java) |
| 09 | End-of-file | Easy | [EndOfFile.java](01-introduction/EndOfFile.java) |
| 10 | Static Initializer Block | Easy | [StaticInitializerBlock.java](01-introduction/StaticInitializerBlock.java) |
| 11 | Int to String | Easy | [IntToString.java](01-introduction/IntToString.java) |
| 12 | Date and Time | Easy | [DateAndTime.java](01-introduction/DateAndTime.java) |
| 13 | Currency Formatter | Easy | [CurrencyFormatter.java](01-introduction/CurrencyFormatter.java) |

### Strings

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 14 | Strings Introduction | Easy | [StringsIntroduction.java](02-strings/StringsIntroduction.java) |
| 15 | Substring | Easy | [Substring.java](02-strings/Substring.java) |
| 16 | Substring Comparisons | Easy | [SubstringComparisons.java](02-strings/SubstringComparisons.java) |
| 17 | String Reverse | Easy | [StringReverse.java](02-strings/StringReverse.java) |
| 18 | Anagrams | Easy | [Anagrams.java](02-strings/Anagrams.java) |
| 19 | String Tokens | Easy | [StringTokens.java](02-strings/StringTokens.java) |
| 20 | Pattern Syntax Checker | Easy | [PatternSyntaxChecker.java](02-strings/PatternSyntaxChecker.java) |
| 21 | Regex | Medium | [RegexIpv4.java](02-strings/RegexIpv4.java) |
| 22 | Regex 2 - Duplicate Words | Medium | [DuplicateWords.java](02-strings/DuplicateWords.java) |
| 23 | Valid Username Regular Expression | Easy | [ValidUsername.java](02-strings/ValidUsername.java) |
| 24 | Tag Content Extractor | Medium | [TagContentExtractor.java](02-strings/TagContentExtractor.java) |

### BigNumber

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 25 | BigDecimal | Medium | [BigDecimalOrdering.java](03-big-numbers/BigDecimalOrdering.java) |
| 26 | Primality Test | Easy | [LargeNumberPrimality.java](03-big-numbers/LargeNumberPrimality.java) |
| 27 | BigInteger | Easy | [BigIntegerArithmetic.java](03-big-numbers/BigIntegerArithmetic.java) |

### Data Structures

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 28 | 1D Array | Easy | [OneDimensionalArray.java](04-data-structures/OneDimensionalArray.java) |
| 29 | 2D Array | Easy | [HourglassSum.java](04-data-structures/HourglassSum.java) |
| 30 | Subarray | Easy | [NegativeSubarrayCount.java](04-data-structures/NegativeSubarrayCount.java) |
| 31 | Arraylist | Easy | [ArrayListQueries.java](04-data-structures/ArrayListQueries.java) |
| 32 | 1D Array (Part 2) | Medium | [LeapGame.java](04-data-structures/LeapGame.java) |
| 33 | List | Easy | [ListOperations.java](04-data-structures/ListOperations.java) |
| 34 | Map | Easy | [PhoneBookLookup.java](04-data-structures/PhoneBookLookup.java) |
| 35 | Stack | Medium | [BalancedBrackets.java](04-data-structures/BalancedBrackets.java) |
| 36 | Hashset | Easy | [DistinctPairs.java](04-data-structures/DistinctPairs.java) |
| 37 | Generics | Easy | [GenericArrayPrinter.java](04-data-structures/GenericArrayPrinter.java) |
| 38 | Comparator | Medium | [PlayerRankingComparator.java](04-data-structures/PlayerRankingComparator.java) |
| 39 | Sort | Easy | [StudentSort.java](04-data-structures/StudentSort.java) |
| 40 | Dequeue | Medium | [DistinctWindow.java](04-data-structures/DistinctWindow.java) |
| 41 | BitSet | Easy | [BitSetOperations.java](04-data-structures/BitSetOperations.java) |
| 42 | Priority Queue | Medium | [StudentPriorityQueue.java](04-data-structures/StudentPriorityQueue.java) |

### Object-Oriented Programming

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 43 | Inheritance I | Easy | [InheritanceBasics.java](05-oop/InheritanceBasics.java) |
| 44 | Inheritance II | Easy | [ArithmeticInheritance.java](05-oop/ArithmeticInheritance.java) |
| 45 | Abstract Class | Easy | [AbstractBook.java](05-oop/AbstractBook.java) |
| 46 | Interface | Easy | [DivisorSumInterface.java](05-oop/DivisorSumInterface.java) |
| 47 | Method Overriding | Easy | [SportsMethodOverriding.java](05-oop/SportsMethodOverriding.java) |
| 48 | Method Overriding 2 (Super Keyword) | Easy | [SuperKeywordOverride.java](05-oop/SuperKeywordOverride.java) |
| 49 | Instanceof keyword | Easy | [InstanceofTypeCounter.java](05-oop/InstanceofTypeCounter.java) |
| 50 | Iterator | Easy | [IteratorAfterMarker.java](05-oop/IteratorAfterMarker.java) |

### Exception Handling

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 51 | Exception Handling (Try-catch) | Easy | [TryCatchDivision.java](06-exceptions/TryCatchDivision.java) |
| 52 | Exception Handling | Easy | [PowerExceptionRules.java](06-exceptions/PowerExceptionRules.java) |

### Threads and Concurrency

These stations are demonstrations rather than TODO-based challenges. Predict
the behavior, run the source, then explain the result. S3 and S5 intentionally
hang; guard them with `timeout 6` or stop them with `Ctrl-C`.

| Station | Topic | Source |
|---:|---|---|
| S1 | `start()` vs `run()` | [StartVsRun.java](07-threads/StartVsRun.java) |
| S2 | Lost update | [LostUpdate.java](07-threads/LostUpdate.java) |
| S3 | Visibility and `volatile` | [Visibility.java](07-threads/Visibility.java) |
| S4 | `wait()` vs `sleep()` | [WaitVsSleep.java](07-threads/WaitVsSleep.java) |
| S5 | Deadlock | [Deadlock.java](07-threads/Deadlock.java) |
| S6 | Thread-pool reuse | [PoolReuse.java](07-threads/PoolReuse.java) |
| S7 | Producer/consumer | [ProducerConsumer.java](07-threads/ProducerConsumer.java) |
| S8 | Virtual threads | [VirtualThreads.java](07-threads/VirtualThreads.java) |

### Advanced

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 53 | Varargs - Simple Addition | Easy | [VarargsAddition.java](08-advanced/VarargsAddition.java) |
| 54 | Reflection - Attributes | Easy | [ReflectionMethodNames.java](08-advanced/ReflectionMethodNames.java) |
| 55 | Can You Access? | Medium | [PrivateMemberAccess.java](08-advanced/PrivateMemberAccess.java) |
| 56 | Prime Checker | Medium | [OverloadedPrimeChecker.java](08-advanced/OverloadedPrimeChecker.java) |
| 57 | Factory Pattern | Easy | [FoodFactoryPattern.java](08-advanced/FoodFactoryPattern.java) |
| 58 | Singleton Pattern | Easy | [SingletonPattern.java](08-advanced/SingletonPattern.java) |
| 59 | Visitor Pattern | Medium | [TreeVisitorPattern.java](08-advanced/TreeVisitorPattern.java) |
| 60 | Annotations | Medium | [BudgetAnnotations.java](08-advanced/BudgetAnnotations.java) |
| 61 | Covariant Return Types | Easy | [CovariantFlowerReturn.java](08-advanced/CovariantFlowerReturn.java) |
| 62 | Lambda Expressions | Medium | [LambdaPredicates.java](08-advanced/LambdaPredicates.java) |
| 63 | MD5 | Medium | [Md5Digest.java](08-advanced/Md5Digest.java) |
| 64 | SHA-256 | Medium | [Sha256Digest.java](08-advanced/Sha256Digest.java) |

### Stream API

The [section guide](09-stream-api/README.md) explains the stream mental model,
learning order, and review checkpoints.

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 65 | Filter and Map Pipeline | Easy | [FilterMapPipeline.java](09-stream-api/FilterMapPipeline.java) |
| 66 | Laziness and Short-Circuiting | Easy | [LazyFirstMatch.java](09-stream-api/LazyFirstMatch.java) |
| 67 | Distinct, Sorted, Skip, and Limit | Easy | [DistinctScorePage.java](09-stream-api/DistinctScorePage.java) |
| 68 | Flatten Nested Data | Easy | [FlatMapWords.java](09-stream-api/FlatMapWords.java) |
| 69 | One-to-Many Mapping with `mapMulti` | Medium | [MapMultiRanges.java](09-stream-api/MapMultiRanges.java) |
| 70 | Prefix Operations | Easy | [TakeDropWhileReadings.java](09-stream-api/TakeDropWhileReadings.java) |
| 71 | Matching and Finding | Easy | [MatchAndFindInventory.java](09-stream-api/MatchAndFindInventory.java) |
| 72 | Optional in a Stream Pipeline | Medium | [OptionalEmailPipeline.java](09-stream-api/OptionalEmailPipeline.java) |
| 73 | Three-Argument Reduction | Medium | [ReduceTransactions.java](09-stream-api/ReduceTransactions.java) |
| 74 | Primitive Streams and Statistics | Easy | [PrimitiveStreamStatistics.java](09-stream-api/PrimitiveStreamStatistics.java) |
| 75 | Joining Collector | Easy | [JoiningCollector.java](09-stream-api/JoiningCollector.java) |
| 76 | Grouping with a Downstream Collector | Medium | [GroupingSales.java](09-stream-api/GroupingSales.java) |
| 77 | Partitioning with Downstream Mapping | Medium | [PartitionPeople.java](09-stream-api/PartitionPeople.java) |
| 78 | Building Maps with Duplicate Keys | Medium | [MergingVotes.java](09-stream-api/MergingVotes.java) |
| 79 | Teeing Collector | Medium | [TeeingRange.java](09-stream-api/TeeingRange.java) |
| 80 | Build a Custom Collector | Hard | [CustomBracketCollector.java](09-stream-api/CustomBracketCollector.java) |
| 81 | Infinite Stream Sources | Medium | [InfiniteStreamBounds.java](09-stream-api/InfiniteStreamBounds.java) |
| 82 | Parallel Collection Without Shared Mutation | Hard | [ParallelWordFrequency.java](09-stream-api/ParallelWordFrequency.java) |

## Scorecard

Mark a challenge only after its checks pass and you can explain the core
concept without reading the implementation. Mark a concurrency station after
you can predict and explain its behavior.

| Subdomain | Labs | Complete |
|---|---:|---:|
| Introduction | 13 | 13/13 |
| Strings | 11 | 6/11 |
| BigNumber | 3 | 0/3 |
| Data Structures | 15 | 0/15 |
| Object-Oriented Programming | 8 | 0/8 |
| Exception Handling | 2 | 0/2 |
| Threads and Concurrency | 8 | 0/8 |
| Advanced | 12 | 0/12 |
| Stream API | 18 | 0/18 |
| **Total** | **90** | **19/90** |

## Revision references

Save useful follow-up material here when a challenge exposes a construct worth
revisiting.

| Challenge | Construct | Resource | Studied |
|---|---|---|---|
| 02 — Java Stdin and Stdout I | Stream collectors, especially `Collectors.joining()` | [Baeldung — Guide to Java Collectors](https://www.baeldung.com/java-collectors) | 2026-09-02 |
| 03 — Java If-Else | Traditional and expression forms of `switch` | [Baeldung — The `switch` Statement in Java](https://www.baeldung.com/java-switch) | 2026-09-02 |
| 03 — Java If-Else | Type patterns, guarded cases, and exhaustiveness in `switch` | [Baeldung — Pattern Matching for `switch`](https://www.baeldung.com/java-switch-pattern-matching) | 2026-09-02 |
| 04 — Java Stdin and Stdout II | Console input and output | [Baeldung — Java Console I/O](https://www.baeldung.com/java-console-input-output) | 2026-09-02 |
| 05 — Java Output Formatting | `Formatter` conversions: width vs precision, the `-` and `0` flags | [Java 21 API — `java.util.Formatter`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Formatter.html) | 2026-09-02 |
| 06 — Java Loops I | Primitive streams: `mapToObj` as the crossing to `Stream<T>`, and why `Collector`s need it | [Baeldung — Primitive Type Streams in Java](https://www.baeldung.com/java-8-primitive-streams) | 2026-09-02 |
| 07 — Java Loops II | `iterate()` + `limit()`: laziness and bounding an infinite source | [Baeldung — Java and Infinite Streams](https://www.baeldung.com/java-inifinite-streams) | 2026-09-02 |
| 08 — Java Datatypes | `parseByte` and the `MIN_VALUE`/`MAX_VALUE` constants: what a wrapper's range contract guarantees, and that a failed parse throws rather than saturating | [Java 21 API — `java.lang.Byte`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Byte.html) | 2026-09-03 |
| 07 — Java Loops II | Stateless lambdas: why a captured accumulator is the wrong way to carry state | [Baeldung — Lambda Expressions and Functional Interfaces: Tips and Best Practices](https://www.baeldung.com/java-8-lambda-expressions-tips) | 2026-09-02 |
| 09 — Java End-of-file | `readLine()` returning `null` as the EOF signal, and the assign-inside-the-condition loop it forces | [Java 21 API — `java.io.BufferedReader`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedReader.html) | 2026-09-03 |
| 09 — Java End-of-file | Choosing between `Scanner` and `BufferedReader`: lookahead vs raw reads, buffer size, checked vs unchecked exceptions | [Baeldung — BufferedReader vs Console vs Scanner in Java](https://www.baeldung.com/bufferedreader-vs-console-vs-scanner-in-java) | 2026-09-03 |
| 12 — Java Date and Time | Java 8 date and time API | [Baeldung — Introduction to the Java 8 Date/Time API](https://www.baeldung.com/java-8-date-time-intro) | 2026-09-04 |
| 12 — Java Date and Time | History of Java date and time APIs | [Baeldung — Java Date and Time History](https://www.baeldung.com/java-date-time-history) | 2026-09-04 |
| 14 — Java Strings Introduction | `String.substring` bounds and the sign returned by `compareTo` | [Java 21 API — `java.lang.String`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html#substring(int)) | 2026-09-04 |
| 17 — String Reverse | Two-pointer palindrome scan vs reverse-and-compare | [Java 21 API — `StringBuilder.reverse`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html#reverse()) | 2026-09-04 |
| 18 — Anagrams | Frequency maps: `Map.merge` for accumulation and `Map.equals` for matching keys and multiplicities | [Java 21 API — `Map`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html) | 2026-09-04 |
| 18 — Anagrams | Sorting characters before comparison | [Baeldung — Sorting in Java](https://www.baeldung.com/java-sorting) | 2026-09-04 |
| 20 — Pattern Syntax Checker | `PatternSyntaxException` as `Pattern.compile`'s only syntax failure, and why it is unchecked | [Java 21 API — `java.util.regex.PatternSyntaxException`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/PatternSyntaxException.html) | 2026-09-05 |
| 20 — Pattern Syntax Checker | Catching narrowly: which `Throwable`s a predicate should absorb and which must escape | [Baeldung — Exception Handling in Java](https://www.baeldung.com/java-exceptions) | 2026-09-05 |
| 18 — Anagrams | Whitespace removal with `replaceAll` | [Baeldung — Removing Whitespace from a String in Java](https://www.baeldung.com/java-string-remove-whitespace) | 2026-09-04 |
