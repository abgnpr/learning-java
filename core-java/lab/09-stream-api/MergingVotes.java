/*
 * Challenge 78: Building Maps with Duplicate Keys
 * Difficulty: Medium
 *
 * Task: Collect votes into a candidate-to-points map. Sum points when a
 * candidate appears more than once and return a TreeMap for deterministic key
 * order. Zero and negative point adjustments are valid.
 * Complete: static Map<String, Integer> totals(List<Vote> votes)
 * Required focus: Collectors.toMap key mapper, value mapper, merge function,
 * and map supplier.
 * Run: java MergingVotes.java
 */
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class MergingVotes {
    record Vote(String candidate, int points) {
    }

    private MergingVotes() {
    }

    static Map<String, Integer> totals(List<Vote> votes) {
        throw new UnsupportedOperationException("TODO: collect votes and merge duplicate keys");
    }

    public static void main(String[] args) {
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("Ada", 8);
        expected.put("Grace", 6);
        expected.put("Linus", 0);
        check("merged totals", "\"Grace\", 7", expected, totals(List.of(
                new Vote("Grace", 7), new Vote("Ada", 5), new Vote("Ada", 3),
                new Vote("Grace", -1), new Vote("Linus", 0))));
        check("empty input", "List.of()", new TreeMap<>(), totals(List.of()));
        checkThat("result type is TreeMap", "\"Ada\", 1", totals(List.of(new Vote("Ada", 1))) instanceof TreeMap);
        report("Challenge 78");
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
