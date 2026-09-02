/*
 * Challenge 79: Teeing Collector
 * Difficulty: Medium
 *
 * Task: Find the minimum and maximum values in one collection pass using two
 * downstream collectors. Return Optional.empty() for empty input.
 * Complete: static Optional<Range> range(List<Integer> values)
 * Required focus: Collectors.teeing and minBy/maxBy.
 * Run: java TeeingRange.java
 */
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TeeingRange {
    record Range(int minimum, int maximum) {
    }

    private TeeingRange() {
    }

    static Optional<Range> range(List<Integer> values) {
        throw new UnsupportedOperationException("TODO: combine min and max collectors with teeing");
    }

    public static void main(String[] args) {
        checkEquals(Optional.of(new Range(-4, 12)), range(List.of(3, 12, -4, 7)), "mixed values");
        checkEquals(Optional.of(new Range(5, 5)), range(List.of(5)), "single value");
        checkEquals(Optional.empty(), range(List.of()), "empty input");
        System.out.println("Challenge 79 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
