/*
 * Challenge 70: Prefix Operations
 * Difficulty: Easy
 *
 * Task: Drop leading negative warm-up readings, then take readings while they
 * are at most the inclusive safety limit. Once a reading exceeds the limit,
 * ignore it and everything after it—even values that would otherwise qualify.
 * Complete: static List<Integer> stableWindow(List<Integer>, int)
 * Required focus: dropWhile and takeWhile; understand how they differ from filter.
 * Run: java TakeDropWhileReadings.java
 */
import java.util.List;

public final class TakeDropWhileReadings {
    private TakeDropWhileReadings() {
    }

    static List<Integer> stableWindow(List<Integer> readings, int safetyLimit) {
        throw new UnsupportedOperationException("TODO: select the bounded encounter-order prefix");
    }

    public static void main(String[] args) {
        checkEquals(List.of(3, 8, 10), stableWindow(List.of(-2, -1, 3, 8, 10, 11, 4), 10),
                "drop warm-up values and stop at breach");
        checkEquals(List.of(), stableWindow(List.of(-3, -1, 20, 2), 10), "breach immediately after warm-up");
        checkEquals(List.of(1, 2), stableWindow(List.of(1, 2), 10), "no warm-up or breach");
        System.out.println("Challenge 70 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
