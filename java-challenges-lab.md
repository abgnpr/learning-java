# Java challenge lab ☕🧪

Sixty-four small Java exercises covering the full supplied track. Each
challenge owns one source file under `lab/core-java/` and uses only
the Java 21 standard library. The task descriptions are self-contained,
original practice contracts; the challenge names and ordering form the
curriculum map.

## How to run

Open a source file, read its task and acceptance checks, then complete the
marked `TODO`. Run that file directly from `lab/core-java/`:

```bash
cd lab/core-java
java 01-introduction/WelcomeToJava.java
```

An untouched starter throws `UnsupportedOperationException` or fails a
structural check. A completed challenge prints its completion line after every
embedded check succeeds.

Compile the whole track without writing class files into the repository:

```bash
cd lab/core-java
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

The order moves from syntax and standard input through collections and
object design to reflection, annotations, lambdas and cryptographic hashes.

### Introduction

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 01 | Welcome to Java! | Easy | [WelcomeToJava.java](lab/core-java/01-introduction/WelcomeToJava.java) |
| 02 | Java Stdin and Stdout I | Easy | [StdinStdoutOne.java](lab/core-java/01-introduction/StdinStdoutOne.java) |
| 03 | Java If-Else | Easy | [IfElse.java](lab/core-java/01-introduction/IfElse.java) |
| 04 | Java Stdin and Stdout II | Easy | [StdinStdoutTwo.java](lab/core-java/01-introduction/StdinStdoutTwo.java) |
| 05 | Java Output Formatting | Easy | [OutputFormatting.java](lab/core-java/01-introduction/OutputFormatting.java) |
| 06 | Java Loops I | Easy | [LoopsOne.java](lab/core-java/01-introduction/LoopsOne.java) |
| 07 | Java Loops II | Easy | [LoopsTwo.java](lab/core-java/01-introduction/LoopsTwo.java) |
| 08 | Java Datatypes | Easy | [Datatypes.java](lab/core-java/01-introduction/Datatypes.java) |
| 09 | Java End-of-file | Easy | [EndOfFile.java](lab/core-java/01-introduction/EndOfFile.java) |
| 10 | Java Static Initializer Block | Easy | [StaticInitializerBlock.java](lab/core-java/01-introduction/StaticInitializerBlock.java) |
| 11 | Java Int to String | Easy | [IntToString.java](lab/core-java/01-introduction/IntToString.java) |
| 12 | Java Date and Time | Easy | [DateAndTime.java](lab/core-java/01-introduction/DateAndTime.java) |
| 13 | Java Currency Formatter | Easy | [CurrencyFormatter.java](lab/core-java/01-introduction/CurrencyFormatter.java) |

### Strings

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 14 | Java Strings Introduction | Easy | [StringsIntroduction.java](lab/core-java/02-strings/StringsIntroduction.java) |
| 15 | Java Substring | Easy | [Substring.java](lab/core-java/02-strings/Substring.java) |
| 16 | Java Substring Comparisons | Easy | [SubstringComparisons.java](lab/core-java/02-strings/SubstringComparisons.java) |
| 17 | Java String Reverse | Easy | [StringReverse.java](lab/core-java/02-strings/StringReverse.java) |
| 18 | Java Anagrams | Easy | [Anagrams.java](lab/core-java/02-strings/Anagrams.java) |
| 19 | Java String Tokens | Easy | [StringTokens.java](lab/core-java/02-strings/StringTokens.java) |
| 20 | Pattern Syntax Checker | Easy | [PatternSyntaxChecker.java](lab/core-java/02-strings/PatternSyntaxChecker.java) |
| 21 | Java Regex | Medium | [RegexIpv4.java](lab/core-java/02-strings/RegexIpv4.java) |
| 22 | Java Regex 2 - Duplicate Words | Medium | [DuplicateWords.java](lab/core-java/02-strings/DuplicateWords.java) |
| 23 | Valid Username Regular Expression | Easy | [ValidUsername.java](lab/core-java/02-strings/ValidUsername.java) |
| 24 | Tag Content Extractor | Medium | [TagContentExtractor.java](lab/core-java/02-strings/TagContentExtractor.java) |

### BigNumber

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 25 | Java BigDecimal | Medium | [BigDecimalOrdering.java](lab/core-java/03-big-numbers/BigDecimalOrdering.java) |
| 26 | Java Primality Test | Easy | [LargeNumberPrimality.java](lab/core-java/03-big-numbers/LargeNumberPrimality.java) |
| 27 | Java BigInteger | Easy | [BigIntegerArithmetic.java](lab/core-java/03-big-numbers/BigIntegerArithmetic.java) |

### Data Structures

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 28 | Java 1D Array | Easy | [OneDimensionalArray.java](lab/core-java/04-data-structures/OneDimensionalArray.java) |
| 29 | Java 2D Array | Easy | [HourglassSum.java](lab/core-java/04-data-structures/HourglassSum.java) |
| 30 | Java Subarray | Easy | [NegativeSubarrayCount.java](lab/core-java/04-data-structures/NegativeSubarrayCount.java) |
| 31 | Java Arraylist | Easy | [ArrayListQueries.java](lab/core-java/04-data-structures/ArrayListQueries.java) |
| 32 | Java 1D Array (Part 2) | Medium | [LeapGame.java](lab/core-java/04-data-structures/LeapGame.java) |
| 33 | Java List | Easy | [ListOperations.java](lab/core-java/04-data-structures/ListOperations.java) |
| 34 | Java Map | Easy | [PhoneBookLookup.java](lab/core-java/04-data-structures/PhoneBookLookup.java) |
| 35 | Java Stack | Medium | [BalancedBrackets.java](lab/core-java/04-data-structures/BalancedBrackets.java) |
| 36 | Java Hashset | Easy | [DistinctPairs.java](lab/core-java/04-data-structures/DistinctPairs.java) |
| 37 | Java Generics | Easy | [GenericArrayPrinter.java](lab/core-java/04-data-structures/GenericArrayPrinter.java) |
| 38 | Java Comparator | Medium | [PlayerRankingComparator.java](lab/core-java/04-data-structures/PlayerRankingComparator.java) |
| 39 | Java Sort | Easy | [StudentSort.java](lab/core-java/04-data-structures/StudentSort.java) |
| 40 | Java Dequeue | Medium | [DistinctWindow.java](lab/core-java/04-data-structures/DistinctWindow.java) |
| 41 | Java BitSet | Easy | [BitSetOperations.java](lab/core-java/04-data-structures/BitSetOperations.java) |
| 42 | Java Priority Queue | Medium | [StudentPriorityQueue.java](lab/core-java/04-data-structures/StudentPriorityQueue.java) |

### Object-Oriented Programming

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 43 | Java Inheritance I | Easy | [InheritanceBasics.java](lab/core-java/05-oop/InheritanceBasics.java) |
| 44 | Java Inheritance II | Easy | [ArithmeticInheritance.java](lab/core-java/05-oop/ArithmeticInheritance.java) |
| 45 | Java Abstract Class | Easy | [AbstractBook.java](lab/core-java/05-oop/AbstractBook.java) |
| 46 | Java Interface | Easy | [DivisorSumInterface.java](lab/core-java/05-oop/DivisorSumInterface.java) |
| 47 | Java Method Overriding | Easy | [SportsMethodOverriding.java](lab/core-java/05-oop/SportsMethodOverriding.java) |
| 48 | Java Method Overriding 2 (Super Keyword) | Easy | [SuperKeywordOverride.java](lab/core-java/05-oop/SuperKeywordOverride.java) |
| 49 | Java Instanceof keyword | Easy | [InstanceofTypeCounter.java](lab/core-java/05-oop/InstanceofTypeCounter.java) |
| 50 | Java Iterator | Easy | [IteratorAfterMarker.java](lab/core-java/05-oop/IteratorAfterMarker.java) |

### Exception Handling

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 51 | Java Exception Handling (Try-catch) | Easy | [TryCatchDivision.java](lab/core-java/06-exceptions/TryCatchDivision.java) |
| 52 | Java Exception Handling | Easy | [PowerExceptionRules.java](lab/core-java/06-exceptions/PowerExceptionRules.java) |

### Advanced

| # | Challenge | Difficulty | Starter |
|---:|---|---|---|
| 53 | Java Varargs - Simple Addition | Easy | [VarargsAddition.java](lab/core-java/08-advanced/VarargsAddition.java) |
| 54 | Java Reflection - Attributes | Easy | [ReflectionMethodNames.java](lab/core-java/08-advanced/ReflectionMethodNames.java) |
| 55 | Can You Access? | Medium | [PrivateMemberAccess.java](lab/core-java/08-advanced/PrivateMemberAccess.java) |
| 56 | Prime Checker | Medium | [OverloadedPrimeChecker.java](lab/core-java/08-advanced/OverloadedPrimeChecker.java) |
| 57 | Java Factory Pattern | Easy | [FoodFactoryPattern.java](lab/core-java/08-advanced/FoodFactoryPattern.java) |
| 58 | Java Singleton Pattern | Easy | [SingletonPattern.java](lab/core-java/08-advanced/SingletonPattern.java) |
| 59 | Java Visitor Pattern | Medium | [TreeVisitorPattern.java](lab/core-java/08-advanced/TreeVisitorPattern.java) |
| 60 | Java Annotations | Medium | [BudgetAnnotations.java](lab/core-java/08-advanced/BudgetAnnotations.java) |
| 61 | Covariant Return Types | Easy | [CovariantFlowerReturn.java](lab/core-java/08-advanced/CovariantFlowerReturn.java) |
| 62 | Java Lambda Expressions | Medium | [LambdaPredicates.java](lab/core-java/08-advanced/LambdaPredicates.java) |
| 63 | Java MD5 | Medium | [Md5Digest.java](lab/core-java/08-advanced/Md5Digest.java) |
| 64 | Java SHA-256 | Medium | [Sha256Digest.java](lab/core-java/08-advanced/Sha256Digest.java) |

## Scorecard

Mark a challenge only after its checks pass and you can explain the core
concept without reading the implementation.

| Subdomain | Challenges | Complete |
|---|---:|---:|
| Introduction | 13 | 0/13 |
| Strings | 11 | 0/11 |
| BigNumber | 3 | 0/3 |
| Data Structures | 15 | 0/15 |
| Object-Oriented Programming | 8 | 0/8 |
| Exception Handling | 2 | 0/2 |
| Advanced | 12 | 0/12 |
| **Total** | **64** | **0/64** |
