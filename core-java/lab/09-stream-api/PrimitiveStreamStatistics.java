/*
 * Challenge 74: Primitive Streams and Statistics
 * Difficulty: Easy
 *
 * Task: Map words to their lengths with an IntStream and summarize them in one
 * pass. Represent missing minimum, maximum, and average values with OptionalInt
 * and OptionalDouble when the input is empty.
 * Complete: static LengthStats summarizeLengths(List<String> words)
 * Required focus: mapToInt and summaryStatistics.
 * Run: java PrimitiveStreamStatistics.java
 */
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class PrimitiveStreamStatistics {
    record LengthStats(long count, long sum, OptionalInt min, OptionalInt max,
            OptionalDouble average) {
    }

    private PrimitiveStreamStatistics() {
    }

    static LengthStats summarizeLengths(List<String> words) {
        throw new UnsupportedOperationException("TODO: summarize a primitive stream");
    }

    public static void main(String[] args) {
        checkEquals(new LengthStats(3, 11, OptionalInt.of(1), OptionalInt.of(6),
                        OptionalDouble.of(11.0 / 3)),
                summarizeLengths(List.of("a", "java", "stream")), "mixed lengths");
        checkEquals(new LengthStats(1, 0, OptionalInt.of(0), OptionalInt.of(0),
                        OptionalDouble.of(0.0)),
                summarizeLengths(List.of("")), "empty string is still an element");
        checkEquals(new LengthStats(0, 0, OptionalInt.empty(), OptionalInt.empty(),
                        OptionalDouble.empty()),
                summarizeLengths(List.of()), "empty input");
        System.out.println("Challenge 74 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
