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

    static Map<Boolean, List<String>> partitionNames(List<Person> people) {
        throw new UnsupportedOperationException("TODO: partition and map people");
    }

    public static void main(String[] args) {
        Map<Boolean, List<String>> expected = Map.of(
                true, List.of("Ada", "Grace"),
                false, List.of("Linus"));
        check("adult partition", "\"Ada\", 18", expected, partitionNames(List.of(
                new Person("Ada", 18), new Person("Linus", 17), new Person("Grace", 30))));
        check("both keys exist for empty input", "List.of()", Map.of(true, List.of(), false, List.of()), partitionNames(List.of()));
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
