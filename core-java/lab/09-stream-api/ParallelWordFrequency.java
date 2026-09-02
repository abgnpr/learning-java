/*
 * Challenge 82: Parallel Collection Without Shared Mutation
 * Difficulty: Hard
 *
 * Task: Count case-insensitive words across lines using a parallel stream.
 * Split on whitespace, ignore blank lines, and return a ConcurrentMap. Build
 * the result through a concurrent collector; do not mutate a shared HashMap
 * from forEach.
 * Complete: static ConcurrentMap<String, Long> frequencies(List<String> lines)
 * Required focus: parallelStream, flatMap, groupingByConcurrent, and counting.
 * Run: java ParallelWordFrequency.java
 */
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ParallelWordFrequency {
    private ParallelWordFrequency() {
    }

    static ConcurrentMap<String, Long> frequencies(List<String> lines) {
        throw new UnsupportedOperationException("TODO: collect frequencies safely in parallel");
    }

    public static void main(String[] args) {
        ConcurrentMap<String, Long> actual = frequencies(List.of(
                "Java streams streams", "parallel JAVA", "  ", "safe parallel streams"));
        checkEquals(2L, actual.get("java"), "java count");
        checkEquals(3L, actual.get("streams"), "streams count");
        checkEquals(2L, actual.get("parallel"), "parallel count");
        checkEquals(1L, actual.get("safe"), "safe count");
        checkEquals(4, actual.size(), "distinct word count");
        checkTrue(actual instanceof ConcurrentMap, "result implements ConcurrentMap");
        checkEquals(0, frequencies(List.of()).size(), "empty input");
        System.out.println("Challenge 82 passed.");
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
