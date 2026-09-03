/*
 * Challenge 80: Build a Custom Collector
 * Difficulty: Hard
 *
 * Task: Implement a Collector<String, StringJoiner, String> that joins values
 * with " | " inside "<" and ">". It must preserve encounter order and combine
 * partial results correctly so both sequential and parallel streams work.
 * Complete: static Collector<String, StringJoiner, String> bracketedJoining()
 * Required focus: Collector.of supplier, accumulator, combiner, and finisher.
 * Run: java CustomBracketCollector.java
 */
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

public final class CustomBracketCollector {
    private CustomBracketCollector() {
    }

    static Collector<String, StringJoiner, String> bracketedJoining() {
        throw new UnsupportedOperationException("TODO: construct a collector with Collector.of");
    }

    public static void main(String[] args) {
        checkEquals("<a | b | c>", List.of("a", "b", "c").stream().collect(bracketedJoining()),
                "sequential collection");
        checkEquals("<a | b | c | d | e>",
                List.of("a", "b", "c", "d", "e").parallelStream().collect(bracketedJoining()),
                "parallel combination");
        checkEquals("<>", List.<String>of().stream().collect(bracketedJoining()), "empty stream");
        if (failures > 0) {
            throw new AssertionError("Challenge 80: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 80 passed.");
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
