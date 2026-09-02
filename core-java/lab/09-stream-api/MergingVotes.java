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
        checkEquals(expected, totals(List.of(
                new Vote("Grace", 7), new Vote("Ada", 5), new Vote("Ada", 3),
                new Vote("Grace", -1), new Vote("Linus", 0))), "merged totals");
        checkEquals(new TreeMap<>(), totals(List.of()), "empty input");
        checkTrue(totals(List.of(new Vote("Ada", 1))) instanceof TreeMap, "result type is TreeMap");
        System.out.println("Challenge 78 passed.");
    }

    private static void checkTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
