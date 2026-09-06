
/*
 * Challenge 77: Partitioning with Downstream Mapping
 * Difficulty: Medium
 *
 * Task: Partition people into adults (true) and minors (false), collecting
 * only their names in encounter order. The result must contain both Boolean
 * keys even when one or both groups are empty.
 * Complete: static Map<Boolean, List<String>> partitionNames(List<Person>)
 * Required focus: partitioningBy with mapping as a downstream collector.
 * Run: java PartitionPeople.java
 */
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PartitionPeople {
    record Person(String name, int age) {
    }

    private PartitionPeople() {
    }

    /*
     * Both Boolean keys come for free, and that is the whole reason to reach for
     * partitioningBy instead of groupingBy(p -> p.age() >= 18, ...). groupingBy
     * builds its map from the keys it actually encounters, so an all-adult input
     * yields a one-entry map and get(false) returns null. partitioningBy is not a
     * map keyed by what it saw: it fills a fixed two-slot structure and runs the
     * downstream's supplier for BOTH halves before consuming anything, so each
     * side already holds an empty list when the stream turns out to be empty.
     * With groupingBy the same guarantee costs a collectingAndThen that injects
     * the missing key afterwards.
     *
     * The downstream mapping() is what keeps the predicate and the projection
     * apart. Mapping first collapses the element too early:
     *
     * // people.stream().map(Person::name).collect(partitioningBy(??? >= 18))
     *
     * — by then only the name survives and the age the predicate needs is gone.
     * mapping(Person::name, toList()) applies the projection AFTER the element
     * has been routed to a side, which is the general shape: classify on the
     * whole object, reduce on a piece of it.
     *
     * Order is inherited, not arranged. toList() accumulates into an ArrayList
     * by appending, and a sequential stream over a List has a defined encounter
     * order, so each side comes out in source order without a sort.
     *
     * The result is not immutable. The two inner lists are ArrayLists and accept
     * add(); only the key set is fixed — put() with anything but true/false
     * throws. Wrap it before handing it out if callers must not mutate it.
     */
    static Map<Boolean, List<String>> partitionNames(List<Person> people) {
        return people.stream().collect(Collectors.partitioningBy(
                person -> person.age() >= 18,
                Collectors.mapping(Person::name, Collectors.toList())));
    }

    public static void main(String[] args) {
        Map<Boolean, List<String>> expected = Map.of(
                true, List.of("Ada", "Grace"),
                false, List.of("Linus"));
        check("adult partition", "\"Ada\", 18", expected, partitionNames(List.of(
                new Person("Ada", 18), new Person("Linus", 17), new Person("Grace", 30))));
        check("both keys exist for empty input", "List.of()", Map.of(true, List.of(), false, List.of()),
                partitionNames(List.of()));

        // Boundary: 18 is an adult, 17 is not — the predicate is >=, not >.
        check("age 18 is the first adult year", "18",
                Map.of(true, List.of("Ada"), false, List.of()),
                partitionNames(List.of(new Person("Ada", 18))));
        check("age 17 is still a minor", "17",
                Map.of(true, List.of(), false, List.of("Linus")),
                partitionNames(List.of(new Person("Linus", 17))));
        check("age 0 partitions false", "0",
                Map.of(true, List.of(), false, List.of("Newborn")),
                partitionNames(List.of(new Person("Newborn", 0))));

        // One-sided inputs still produce both keys with an empty list on the other side.
        check("all adults leave false empty", "all >= 18",
                Map.of(true, List.of("Ada", "Grace"), false, List.of()),
                partitionNames(List.of(new Person("Ada", 40), new Person("Grace", 30))));
        check("all minors leave true empty", "all < 18",
                Map.of(true, List.of(), false, List.of("Linus", "Kid")),
                partitionNames(List.of(new Person("Linus", 17), new Person("Kid", 3))));

        // Encounter order is preserved inside each partition, not merged or sorted.
        check("encounter order kept per partition", "interleaved ages",
                Map.of(true, List.of("A1", "A2", "A3"), false, List.of("M1", "M2")),
                partitionNames(List.of(
                        new Person("A1", 60), new Person("M1", 10), new Person("A2", 18),
                        new Person("M2", 17), new Person("A3", 21))));

        // mapping keeps duplicates: toList is not a set, so repeated names both survive.
        check("duplicate names are not collapsed", "\"Ada\" twice",
                Map.of(true, List.of("Ada", "Ada"), false, List.of()),
                partitionNames(List.of(new Person("Ada", 18), new Person("Ada", 44))));

        // The mapper takes the name verbatim — empty and null names are passed through.
        check("empty name survives mapping", "\"\"",
                Map.of(true, List.of(""), false, List.of()),
                partitionNames(List.of(new Person("", 18))));
        checkThat("null name survives mapping", "null name",
                partitionNames(java.util.List.of(new Person(null, 18)))
                        .equals(Map.of(true, java.util.Collections.singletonList(null), false, List.of())));

        // Extreme ages: no overflow or sign trickery in the predicate.
        check("Integer.MAX_VALUE is an adult", "Integer.MAX_VALUE",
                Map.of(true, List.of("Old"), false, List.of()),
                partitionNames(List.of(new Person("Old", Integer.MAX_VALUE))));
        check("negative age is a minor", "-1",
                Map.of(true, List.of(), false, List.of("Bad")),
                partitionNames(List.of(new Person("Bad", -1))));

        // partitioningBy's map answers both keys by contract, and only those two.
        Map<Boolean, List<String>> empty = partitionNames(List.of());
        checkThat("map has exactly two keys", "List.of()", empty.size() == 2);
        checkThat("get(true) is never null", "List.of()", empty.get(true) != null);
        checkThat("get(false) is never null", "List.of()", empty.get(false) != null);
        checkThat("containsKey(true) on empty input", "List.of()", empty.containsKey(true));
        checkThat("containsKey(false) on empty input", "List.of()", empty.containsKey(false));

        // A stream with many elements partitions without losing any.
        List<Person> many = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Person("P" + i, i))
                .toList();
        Map<Boolean, List<String>> split = partitionNames(many);
        checkThat("100 people split 83 adults / 17 minors", "ages 1..100",
                split.get(true).size() == 83 && split.get(false).size() == 17);
        checkThat("no person is dropped or duplicated", "ages 1..100",
                split.get(true).size() + split.get(false).size() == many.size());

        report("Challenge 77");
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
