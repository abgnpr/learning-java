/*
 * Challenge 36 — Java Hashset (Easy)
 *
 * Task: Insert ordered pairs of strings into a set and return the running
 * number of distinct pairs after each insertion.
 * Complete: runningDistinctCounts(List<Pair> pairs)
 * Run: java DistinctPairs.java
 */
import java.util.List;

public class DistinctPairs {
    record Pair(String first, String second) { }

    static List<Integer> runningDistinctCounts(List<Pair> pairs) {
        // TODO: Rely on value equality and hashing for each complete pair.
        throw new UnsupportedOperationException("TODO: count distinct pairs");
    }

    public static void main(String[] args) {
        checkEquals(
            List.of(1, 2, 2),
            runningDistinctCounts(List.of(
                new Pair("john", "tom"),
                new Pair("john", "mary"),
                new Pair("john", "tom")
            )),
            "duplicate pair"
        );
        checkEquals(
            List.of(1, 2),
            runningDistinctCounts(List.of(new Pair("a", "b"), new Pair("b", "a"))),
            "pair order matters"
        );
        checkEquals(List.of(), runningDistinctCounts(List.of()), "empty input");
        if (failures > 0) {
            throw new AssertionError("Challenge 36: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 36 passed.");
    }

    private static int failures = 0;

    static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
