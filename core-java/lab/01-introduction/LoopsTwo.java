/*
 * Challenge 07: Java Loops II
 * Difficulty: Easy
 *
 * Task: Starting at start, repeatedly add increment multiplied by successive
 * powers of two and return each resulting term.
 * Complete: static List<Long> buildSeries(long start, long increment, int terms)
 * Run: java LoopsTwo.java
 */
import java.util.List;

public final class LoopsTwo {
    private LoopsTwo() {
    }

    static List<Long> buildSeries(long start, long increment, int terms) {
        throw new UnsupportedOperationException("TODO: generate the doubling-increment series");
    }

    public static void main(String[] args) {
        checkEquals(List.of(3L, 5L, 9L, 17L), buildSeries(2, 1, 4), "basic series");
        checkEquals(List.of(0L, -12L, -36L), buildSeries(6, -6, 3), "negative increment");
        checkEquals(List.of(), buildSeries(99, 4, 0), "no terms");
        System.out.println("Challenge 07 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
