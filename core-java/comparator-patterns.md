# Comparator patterns for application code

A sort requirement is a sequence of keys, each with its own direction and null
policy. Write that sequence before writing the comparator. A later key breaks a
tie only when every earlier comparison returned zero.

The examples use Java 8 APIs and syntax. Start with [Java 8 comparator composition](java-8.md#comparator-composition);
use this guide when implementing a particular ordering. The [Comparator API](https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html)
defines the composition and comparison contracts.

## Pattern map

| Requirement | Typical use | Pattern |
|---|---|---|
| Natural ascending / descending | Codes, amounts, timestamps | `naturalOrder()` / `reverseOrder()` |
| One field ascending / descending | Cheapest price, latest update | `comparing(key)` / `comparing(key).reversed()` |
| All fields ascending | Department, then employee name | `a.thenComparing(b).thenComparing(c)` |
| Mixed directions | Department ASC, salary DESC, name ASC | Reverse each descending key before chaining |
| Reverse an entire order | Invert a complete report | Reverse the completed comparator |
| Deterministic final tie-breaker | Repeatable exports and page boundaries | Append a unique immutable ID |
| Nullable fields and objects | Optional owner, missing row | Wrap field and object comparators separately |
| Case-insensitive ordering | Usernames and technical labels | Ignore case, then natural spelling, then ID |
| Locale-aware text | Names displayed in a user's language | `Collator` with an explicit locale |
| Derived keys | Shortest name, normalized identifier | Compare a computed key, then break ties |
| Business-defined rank | Status/severity workflows | Map each value to an explicit rank |
| Preferred group first | Pinned, urgent, in-stock, VIP | Compare a 0/1 group key, then ordinary keys |
| User-selected sort | Sort menus and report parameters | Select a permitted primary comparator, then append tie-breakers |
| Ranked map entries | Leaderboards, frequency reports | Value DESC, key ASC, collect in encounter order |
| Best result / top N / next task | Selection and scheduling | Reuse the ordering with `min`, `limit`, or a priority queue |

## A shared dataset

Assume `java.util.*` and `java.util.stream.Collectors` imports. Fields below are
non-null unless a null example says otherwise. IDs uniquely identify rows.

```java
class Employee {
    final long id;
    final String department, name;
    final int salary;
    Employee(long id, String department, String name, int salary) {
        this.id = id; this.department = department;
        this.name = name; this.salary = salary;
    }
    long getId() { return id; }
    String getDepartment() { return department; }
    String getName() { return name; }
    int getSalary() { return salary; }
}

List<Employee> employees = new ArrayList<>(Arrays.asList(
    new Employee(5, "Tech", "Zoe", 100), new Employee(2, "HR", "Mia", 90),
    new Employee(4, "Tech", "Ben", 120), new Employee(3, "Tech", "Ana", 120),
    new Employee(1, "Tech", "Ana", 120)));

Comparator<Employee> byDepartment = Comparator.comparing(Employee::getDepartment);
Comparator<Employee> bySalary = Comparator.comparingInt(Employee::getSalary);
Comparator<Employee> byName = Comparator.comparing(Employee::getName);
Comparator<Employee> byId = Comparator.comparingLong(Employee::getId);
```

`comparingInt`, `comparingLong`, and `comparingDouble` avoid boxing their
extracted primitive keys. Objects such as `BigDecimal` and `Instant` use
`comparing`; their natural ordering compares numeric value and timeline position
respectively. [String natural order](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareTo-java.lang.String-)
is case-sensitive lexicographical order:
`"10"` comes before `"2"`. Parse numeric text when numeric order is intended.

## Ascending, descending, and multiple fields

Keep primitive and object key comparators as reusable values:

```java
employees.sort(bySalary);            // salaries: 90, 100, 120, 120, 120
employees.sort(bySalary.reversed()); // salaries: 120, 120, 120, 100, 90

Comparator<Employee> allAscending = byDepartment
    .thenComparing(bySalary).thenComparing(byName).thenComparing(byId);

Comparator<Employee> reportOrder = byDepartment
    .thenComparing(bySalary.reversed()).thenComparing(byName).thenComparing(byId);
employees.sort(reportOrder);
// IDs: [2, 1, 3, 4, 5]
// HR/Mia/90, Tech/Ana/120, Tech/Ana/120, Tech/Ben/120, Tech/Zoe/100
```

This is the same ordering specification as
`department ASC, salary DESC, name ASC, id ASC`. Salary is compared only within
one department; names are compared only when department and salary both tie.
The two Ana rows are finally ordered by ID.

Every direction mix follows the same rule. In this table, name and ID remain
ascending:

| Department | Salary | Comparator prefix |
|---|---|---|
| ASC | ASC | `byDepartment.thenComparing(bySalary)` |
| ASC | DESC | `byDepartment.thenComparing(bySalary.reversed())` |
| DESC | ASC | `byDepartment.reversed().thenComparing(bySalary)` |
| DESC | DESC | `byDepartment.reversed().thenComparing(bySalary.reversed())` |

Append `.thenComparing(byName).thenComparing(byId)` to any prefix. Reverse
`byName` as well if the third key should descend. There is no need to memorize
eight separate three-field recipes.

## Where reversed is attached matters

```java
Comparator<Employee> oneKeyReversed = byDepartment
    .thenComparing(bySalary.reversed()).thenComparing(byName).thenComparing(byId);
// department ASC, salary DESC, name ASC, id ASC

Comparator<Employee> wholeOrderReversed = oneKeyReversed.reversed();
// department DESC, salary ASC, name DESC, id DESC

Comparator<Employee> prefixReversed = byDepartment.thenComparing(bySalary)
    .reversed().thenComparing(byName).thenComparing(byId);
// department DESC, salary DESC, name ASC, id ASC
```

Read the expression to the left of `.reversed()` as one unit. Reversing a
completed chain reverses all its tie-breakers too.

## Stable ties versus explicit IDs

For a fresh copy of the shared dataset:

```java
Comparator<Employee> businessKeys = byDepartment
    .thenComparing(bySalary.reversed()).thenComparing(byName);
// IDs after sorting: [2, 3, 1, 4, 5] — tied Ana rows retain input order

Comparator<Employee> repeatable = businessKeys.thenComparing(byId);
// IDs after sorting: [2, 1, 3, 4, 5] — ID resolves the remaining tie
```

[List.sort is stable](https://docs.oracle.com/javase/8/docs/api/java/util/List.html#sort-java.util.Comparator-):
rows that compare as zero keep their relative input order. That is useful when
ties should preserve arrival order. Use an ID when different input arrangements
must still yield the same order. An ID orders a fixed dataset consistently; it
does not freeze data that changes between page requests.

## Null fields, null objects, and descending order

Null handling belongs at the level where null can occur:

```java
Comparator<Employee> nameAscending = Comparator.comparing(
    Employee::getName, Comparator.nullsLast(Comparator.naturalOrder()));

Comparator<Employee> nameDescending = Comparator.comparing(
    Employee::getName, Comparator.nullsLast(Comparator.reverseOrder()));

Comparator<Employee> nullableRows = Comparator.nullsLast(
    nameDescending.thenComparing(byId));
```

The field comparators allow null names; the outer wrapper also allows null
Employee objects. For descending names with nulls still last, reverse the
non-null key order **inside** `nullsLast`. `nameAscending.reversed()` would put
null names first. `nullsFirst` is the corresponding policy when missing values
belong first. Nested properties need the same care: an outer row guard does
not protect a null manager inside an otherwise non-null employee.

## Case, natural text order, and derived keys

```java
Comparator<Employee> namesIgnoringCase = Comparator
    .comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER)
    .thenComparing(Employee::getName) // exact spelling breaks case-insensitive ties
    .thenComparing(byId);
// For the same group: Ana, ana, Ben, zoe

Comparator<Employee> shortestNameFirst = Comparator
    .comparingInt((Employee e) -> e.getName().length())
    .thenComparing(byName).thenComparing(byId);
```

Case-insensitive order can make distinct spellings compare as zero. Append
natural spelling or an ID if those rows must remain distinct in the ordering.
For user-facing language order, use [Collator](https://docs.oracle.com/javase/8/docs/api/java/text/Collator.html),
for example `Comparator.comparing(Employee::getName,
Collator.getInstance(Locale.FRENCH))` with a `java.text.Collator` import.
Case folding and locale collation express different requirements.

Derived keys also include normalized codes, parsed dates, or computed scores.
Extraction can run repeatedly during sorting; calculate expensive keys once
before sorting rather than reparsing them on every comparison.

## Business ranks and preferred groups

Alphabetical order rarely expresses workflow priority. Assign ranks explicitly
and decide where unknown values belong:

```java
Map<String, Integer> ranks = new HashMap<>();
ranks.put("CRITICAL", 0); ranks.put("HIGH", 1); ranks.put("NORMAL", 2);
Comparator<String> priorityOrder = Comparator
    .comparingInt((String status) -> ranks.getOrDefault(status, Integer.MAX_VALUE))
    .thenComparing(Comparator.nullsLast(Comparator.naturalOrder()));
// CRITICAL, HIGH, NORMAL, then unknown values alphabetically, then null

Comparator<Employee> techFirst = Comparator
    .comparingInt((Employee e) -> "Tech".equals(e.getDepartment()) ? 0 : 1)
    .thenComparing(byName).thenComparing(byId);
```

The 0/1 key generalizes to urgent tickets, pinned rows, VIP customers, or in-stock
products first. Remaining keys determine order inside each group. Enum natural
order follows declaration order; use an explicit rank when business priority
must be independent of that declaration. Keep the rank map stable during a sort.

## User-selected sort policies

Select a permitted primary comparator, reverse that key if requested, and append
an ascending ID. Here `sortBy` and `descending` are input parameters:

```java
Comparator<Employee> primary;
switch (sortBy) {
    case "name": primary = byName; break;
    case "salary": primary = bySalary; break;
    case "department": primary = byDepartment; break;
    default: throw new IllegalArgumentException("unknown sort: " + sortBy);
}
if (descending) primary = primary.reversed();
Comparator<Employee> selected = primary.thenComparing(byId);
```

If nullable values are allowed, apply their chosen null policy after deciding
the non-null key direction. Reversing the entire completed comparator would
also reverse the ID tie-breaker.

## Rank map entries and preserve report order

```java
Comparator<Map.Entry<String, Long>> leaderboardOrder =
    Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
        .thenComparing(Map.Entry.comparingByKey());

Map<String, Long> ranked = counts.entrySet().stream()
    .sorted(leaderboardOrder)
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
        (left, right) -> left, LinkedHashMap::new));
// With {Zoe=8, Ada=8, Ben=3}, iteration is Ada=8, Zoe=8, Ben=3.
```

The value orders the ranking; the key breaks score ties. The merge function is
required by this `toMap` overload, although source map keys are already unique.
Preserve sorted encounter order with [LinkedHashMap](https://docs.oracle.com/javase/8/docs/api/java/util/LinkedHashMap.html).
A default `toMap` result
or a `TreeMap` using natural key order does not express this ranking contract.

## Best, top N, and queue order

```java
Optional<Employee> firstInReport = employees.stream().min(reportOrder);
List<Employee> firstThree = employees.stream()
    .sorted(reportOrder).limit(3).collect(Collectors.toList());

PriorityQueue<Employee> queue = new PriorityQueue<>(reportOrder);
queue.addAll(employees);
Employee next = queue.poll(); // first under reportOrder, or null if empty
```

For a comparator written **best first**, `min` selects the best and `max` the
last. Empty stream selection returns an empty Optional. A
[PriorityQueue](https://docs.oracle.com/javase/8/docs/api/java/util/PriorityQueue.html)
exposes the least element under its comparator at the head; repeatedly poll to
obtain priority order. Its iterator is not sorted and ties need an explicit
tie-breaker if a particular service order matters. `sorted().limit(k)` is a
clear report implementation; a bounded heap is a separate optimization for
large inputs and small k.

## Contracts that prevent subtle bugs

- Return a negative/zero/positive result, not necessarily -1/0/1. Use
  `Integer.compare`/`Long.compare` rather than subtraction; overflow can reverse
  comparisons. Use `Double.compare` for floating-point values, including NaN.
- Preserve sign symmetry and transitivity. Random results, changing external
  state, or special cases that disagree across three rows break the contract.
- `compare(a, b) == 0` means tied under this ordering. [Sorted sets](https://docs.oracle.com/javase/8/docs/api/java/util/SortedSet.html)
  and sorted maps use
  that result to decide uniqueness; an employee report comparator is not
  automatically a suitable identity comparator. Adding an ID helps distinguish
  different rows but does not by itself prove consistency with `equals`.
- A stable list sort retains duplicates. Using a TreeSet merely to sort can
  silently remove tied rows.
- Keep compared keys stable while sorting or while elements are stored in an
  ordered set/map or priority queue. Change a queued priority by removing and
  reinserting the item under the new key.
- `list.sort(order)` mutates the list. Use a copy or `stream().sorted(order)`
  when source order must remain unchanged. Sorting reorders references; it does
  not deep-copy the elements.

## Practice route

| Step | Lab | Main rep |
|---:|---|---|
| 1 | [Multi-field inventory](lab/09-stream-api/MultiFieldInventorySort.java) | Warehouse ASC, quantity DESC, natural name ASC; source unchanged |
| 2 | [Comparator policy catalog](lab/09-stream-api/ComparatorPolicyCatalog.java) | Mixed keys, case and null policies, urgent/business rank, oldest/newest |
| 3 | [Ranked results](lab/09-stream-api/RankedResults.java) | Value DESC/key ASC, top N, best selection, ordered map |
| 4 | [Equality and ordering](lab/10-core-contracts/EqualityOrderingContract.java) | Natural order, equals/hashCode, TreeSet uniqueness |
| 5 | [Student priority queue](lab/04-data-structures/StudentPriorityQueue.java) | The same ordering controls service order |

Predict the row IDs before running. Change one direction without changing the
others, add a complete tie, then add nulls at the field and object levels.
Explain exactly which comparison decides each adjacent pair in the result.
