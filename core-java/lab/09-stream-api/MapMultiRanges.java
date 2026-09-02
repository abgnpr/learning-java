/*
 * Challenge 69: One-to-Many Mapping with mapMulti
 * Difficulty: Medium
 *
 * Task: Expand each inclusive integer range into its values, preserving range
 * and value order. A range whose start is greater than its end emits nothing.
 * Complete: static List<Integer> expand(List<IntRange> ranges)
 * Required focus: Stream.mapMulti.
 * Run: java MapMultiRanges.java
 */
import java.util.List;

public final class MapMultiRanges {
    record IntRange(int start, int end) {
    }

    private MapMultiRanges() {
    }

    static List<Integer> expand(List<IntRange> ranges) {
        throw new UnsupportedOperationException("TODO: emit every value with mapMulti");
    }

    public static void main(String[] args) {
        checkEquals(List.of(1, 2, 3, 7, 9, 10),
                expand(List.of(new IntRange(1, 3), new IntRange(7, 7),
                        new IntRange(5, 2), new IntRange(9, 10))),
                "mixed ranges");
        checkEquals(List.of(-2, -1, 0), expand(List.of(new IntRange(-2, 0))), "negative range");
        checkEquals(List.of(), expand(List.of()), "empty input");
        System.out.println("Challenge 69 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
