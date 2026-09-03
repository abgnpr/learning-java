/*
 * Challenge 67: Distinct, Sorted, Skip, and Limit
 * Difficulty: Easy
 *
 * Task: Remove duplicate scores, sort highest first, skip pageOffset scores,
 * and return at most pageSize scores. Reject negative paging arguments with
 * IllegalArgumentException.
 * Complete: static List<Integer> page(List<Integer>, long, long)
 * Required focus: distinct, sorted, skip, and limit.
 * Run: java DistinctScorePage.java
 */
import java.util.Comparator;
import java.util.List;

public final class DistinctScorePage {
    private DistinctScorePage() {
    }

    static List<Integer> page(List<Integer> scores, long pageOffset, long pageSize) {
        throw new UnsupportedOperationException("TODO: create a distinct descending page");
    }

    public static void main(String[] args) {
        List<Integer> scores = List.of(50, 90, 70, 90, 40, 70, 100);
        checkEquals(List.of(90, 70), page(scores, 1, 2), "middle page");
        checkEquals(List.of(100, 90, 70, 50, 40), page(scores, 0, 20), "oversized page");
        checkEquals(List.of(), page(scores, 20, 2), "offset beyond result");
        expectIllegalArgument(() -> page(scores, -1, 2), "negative offset");
        expectIllegalArgument(() -> page(scores, 0, -1), "negative size");
        if (failures > 0) {
            throw new AssertionError("Challenge 67: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 67 passed.");
    }

    private static void expectIllegalArgument(Runnable action, String label) {
        try {
            action.run();
            throw new AssertionError(label + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
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
