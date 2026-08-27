# Core Java labs ☕🧪

This directory contains 72 runnable Java labs arranged as a learning path:
64 self-testing challenge starters and eight concurrency stations. Each lab
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
| | **Total** | **72** |

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

Compile all 64 challenge starters without writing class files into the
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

## Track

The order moves from syntax and standard input through collections and object
design to concurrency, reflection, annotations, lambdas and cryptographic
hashes. Challenge numbers 01–64 cover sections 01–06 and 08; section 07 uses
station numbers S1–S8 and has a separate
[predict-run-explain guide](07-threads/README.md).

### Introduction

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 01 | Welcome to Java! | Easy | [WelcomeToJava.java](01-introduction/WelcomeToJava.java) |
| 02 | Java Stdin and Stdout I | Easy | [StdinStdoutOne.java](01-introduction/StdinStdoutOne.java) |
| 03 | Java If-Else | Easy | [IfElse.java](01-introduction/IfElse.java) |
| 04 | Java Stdin and Stdout II | Easy | [StdinStdoutTwo.java](01-introduction/StdinStdoutTwo.java) |
| 05 | Java Output Formatting | Easy | [OutputFormatting.java](01-introduction/OutputFormatting.java) |
| 06 | Java Loops I | Easy | [LoopsOne.java](01-introduction/LoopsOne.java) |
| 07 | Java Loops II | Easy | [LoopsTwo.java](01-introduction/LoopsTwo.java) |
| 08 | Java Datatypes | Easy | [Datatypes.java](01-introduction/Datatypes.java) |
| 09 | Java End-of-file | Easy | [EndOfFile.java](01-introduction/EndOfFile.java) |
| 10 | Java Static Initializer Block | Easy | [StaticInitializerBlock.java](01-introduction/StaticInitializerBlock.java) |
| 11 | Java Int to String | Easy | [IntToString.java](01-introduction/IntToString.java) |
| 12 | Java Date and Time | Easy | [DateAndTime.java](01-introduction/DateAndTime.java) |
| 13 | Java Currency Formatter | Easy | [CurrencyFormatter.java](01-introduction/CurrencyFormatter.java) |

### Strings

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 14 | Java Strings Introduction | Easy | [StringsIntroduction.java](02-strings/StringsIntroduction.java) |
| 15 | Java Substring | Easy | [Substring.java](02-strings/Substring.java) |
| 16 | Java Substring Comparisons | Easy | [SubstringComparisons.java](02-strings/SubstringComparisons.java) |
| 17 | Java String Reverse | Easy | [StringReverse.java](02-strings/StringReverse.java) |
| 18 | Java Anagrams | Easy | [Anagrams.java](02-strings/Anagrams.java) |
| 19 | Java String Tokens | Easy | [StringTokens.java](02-strings/StringTokens.java) |
| 20 | Pattern Syntax Checker | Easy | [PatternSyntaxChecker.java](02-strings/PatternSyntaxChecker.java) |
| 21 | Java Regex | Medium | [RegexIpv4.java](02-strings/RegexIpv4.java) |
| 22 | Java Regex 2 - Duplicate Words | Medium | [DuplicateWords.java](02-strings/DuplicateWords.java) |
| 23 | Valid Username Regular Expression | Easy | [ValidUsername.java](02-strings/ValidUsername.java) |
| 24 | Tag Content Extractor | Medium | [TagContentExtractor.java](02-strings/TagContentExtractor.java) |

### BigNumber

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 25 | Java BigDecimal | Medium | [BigDecimalOrdering.java](03-big-numbers/BigDecimalOrdering.java) |
| 26 | Java Primality Test | Easy | [LargeNumberPrimality.java](03-big-numbers/LargeNumberPrimality.java) |
| 27 | Java BigInteger | Easy | [BigIntegerArithmetic.java](03-big-numbers/BigIntegerArithmetic.java) |

### Data Structures

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 28 | Java 1D Array | Easy | [OneDimensionalArray.java](04-data-structures/OneDimensionalArray.java) |
| 29 | Java 2D Array | Easy | [HourglassSum.java](04-data-structures/HourglassSum.java) |
| 30 | Java Subarray | Easy | [NegativeSubarrayCount.java](04-data-structures/NegativeSubarrayCount.java) |
| 31 | Java Arraylist | Easy | [ArrayListQueries.java](04-data-structures/ArrayListQueries.java) |
| 32 | Java 1D Array (Part 2) | Medium | [LeapGame.java](04-data-structures/LeapGame.java) |
| 33 | Java List | Easy | [ListOperations.java](04-data-structures/ListOperations.java) |
| 34 | Java Map | Easy | [PhoneBookLookup.java](04-data-structures/PhoneBookLookup.java) |
| 35 | Java Stack | Medium | [BalancedBrackets.java](04-data-structures/BalancedBrackets.java) |
| 36 | Java Hashset | Easy | [DistinctPairs.java](04-data-structures/DistinctPairs.java) |
| 37 | Java Generics | Easy | [GenericArrayPrinter.java](04-data-structures/GenericArrayPrinter.java) |
| 38 | Java Comparator | Medium | [PlayerRankingComparator.java](04-data-structures/PlayerRankingComparator.java) |
| 39 | Java Sort | Easy | [StudentSort.java](04-data-structures/StudentSort.java) |
| 40 | Java Dequeue | Medium | [DistinctWindow.java](04-data-structures/DistinctWindow.java) |
| 41 | Java BitSet | Easy | [BitSetOperations.java](04-data-structures/BitSetOperations.java) |
| 42 | Java Priority Queue | Medium | [StudentPriorityQueue.java](04-data-structures/StudentPriorityQueue.java) |

### Object-Oriented Programming

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 43 | Java Inheritance I | Easy | [InheritanceBasics.java](05-oop/InheritanceBasics.java) |
| 44 | Java Inheritance II | Easy | [ArithmeticInheritance.java](05-oop/ArithmeticInheritance.java) |
| 45 | Java Abstract Class | Easy | [AbstractBook.java](05-oop/AbstractBook.java) |
| 46 | Java Interface | Easy | [DivisorSumInterface.java](05-oop/DivisorSumInterface.java) |
| 47 | Java Method Overriding | Easy | [SportsMethodOverriding.java](05-oop/SportsMethodOverriding.java) |
| 48 | Java Method Overriding 2 (Super Keyword) | Easy | [SuperKeywordOverride.java](05-oop/SuperKeywordOverride.java) |
| 49 | Java Instanceof keyword | Easy | [InstanceofTypeCounter.java](05-oop/InstanceofTypeCounter.java) |
| 50 | Java Iterator | Easy | [IteratorAfterMarker.java](05-oop/IteratorAfterMarker.java) |

### Exception Handling

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 51 | Java Exception Handling (Try-catch) | Easy | [TryCatchDivision.java](06-exceptions/TryCatchDivision.java) |
| 52 | Java Exception Handling | Easy | [PowerExceptionRules.java](06-exceptions/PowerExceptionRules.java) |

### Threads and Concurrency

These stations are demonstrations rather than TODO-based challenges. Predict
the behavior, run the source, then explain the result. S3 and S5 intentionally
hang; guard them with `timeout 6` or stop them with `Ctrl-C`.

| Station | Topic | Source |
|---:|---|---|
| S1 | `start()` vs `run()` | [StartVsRun.java](07-threads/01-start-vs-run/StartVsRun.java) |
| S2 | Lost update | [LostUpdate.java](07-threads/02-lost-update/LostUpdate.java) |
| S3 | Visibility and `volatile` | [Visibility.java](07-threads/03-visibility/Visibility.java) |
| S4 | `wait()` vs `sleep()` | [WaitVsSleep.java](07-threads/04-wait-vs-sleep/WaitVsSleep.java) |
| S5 | Deadlock | [Deadlock.java](07-threads/05-deadlock/Deadlock.java) |
| S6 | Thread-pool reuse | [PoolReuse.java](07-threads/06-pool-reuse/PoolReuse.java) |
| S7 | Producer/consumer | [ProducerConsumer.java](07-threads/07-producer-consumer/ProducerConsumer.java) |
| S8 | Virtual threads | [VirtualThreads.java](07-threads/08-virtual-threads/VirtualThreads.java) |

### Advanced

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 53 | Java Varargs - Simple Addition | Easy | [VarargsAddition.java](08-advanced/VarargsAddition.java) |
| 54 | Java Reflection - Attributes | Easy | [ReflectionMethodNames.java](08-advanced/ReflectionMethodNames.java) |
| 55 | Can You Access? | Medium | [PrivateMemberAccess.java](08-advanced/PrivateMemberAccess.java) |
| 56 | Prime Checker | Medium | [OverloadedPrimeChecker.java](08-advanced/OverloadedPrimeChecker.java) |
| 57 | Java Factory Pattern | Easy | [FoodFactoryPattern.java](08-advanced/FoodFactoryPattern.java) |
| 58 | Java Singleton Pattern | Easy | [SingletonPattern.java](08-advanced/SingletonPattern.java) |
| 59 | Java Visitor Pattern | Medium | [TreeVisitorPattern.java](08-advanced/TreeVisitorPattern.java) |
| 60 | Java Annotations | Medium | [BudgetAnnotations.java](08-advanced/BudgetAnnotations.java) |
| 61 | Covariant Return Types | Easy | [CovariantFlowerReturn.java](08-advanced/CovariantFlowerReturn.java) |
| 62 | Java Lambda Expressions | Medium | [LambdaPredicates.java](08-advanced/LambdaPredicates.java) |
| 63 | Java MD5 | Medium | [Md5Digest.java](08-advanced/Md5Digest.java) |
| 64 | Java SHA-256 | Medium | [Sha256Digest.java](08-advanced/Sha256Digest.java) |

## Scorecard

Mark a challenge only after its checks pass and you can explain the core
concept without reading the implementation. Mark a concurrency station after
you can predict and explain its behavior.

| Subdomain | Labs | Complete |
|---|---:|---:|
| Introduction | 13 | 0/13 |
| Strings | 11 | 0/11 |
| BigNumber | 3 | 0/3 |
| Data Structures | 15 | 0/15 |
| Object-Oriented Programming | 8 | 0/8 |
| Exception Handling | 2 | 0/2 |
| Threads and Concurrency | 8 | 0/8 |
| Advanced | 12 | 0/12 |
| **Total** | **72** | **0/72** |
