/*
 * Challenge 81: Infinite Stream Sources
 * Difficulty: Medium
 *
 * Task: Produce the first count powers of two with Stream.iterate and produce
 * count copies of a value with Stream.generate. Reject a negative count with
 * IllegalArgumentException before creating either pipeline.
 * Complete: powersOfTwo(int) and repeat(String, int)
 * Required focus: iterate, generate, and limit.
 * Run: java InfiniteStreamBounds.java
 */
import java.util.List;
import java.util.stream.Stream;

public final class InfiniteStreamBounds {
    private InfiniteStreamBounds() {
    }

    static List<Long> powersOfTwo(int count) {
        throw new UnsupportedOperationException("TODO: bound Stream.iterate");
    }

    static List<String> repeat(String value, int count) {
        throw new UnsupportedOperationException("TODO: bound Stream.generate");
    }

    public static void main(String[] args) {
        checkEquals(List.of(1L, 2L, 4L, 8L, 16L), powersOfTwo(5), "iterate powers");
        checkEquals(List.of(), powersOfTwo(0), "zero powers");
        checkEquals(List.of("go", "go", "go"), repeat("go", 3), "generated copies");
        checkEquals(List.of(), repeat("go", 0), "zero copies");
        expectIllegalArgument(() -> powersOfTwo(-1), "negative iterate count");
        expectIllegalArgument(() -> repeat("go", -1), "negative generate count");
        if (failures > 0) {
            throw new AssertionError("Challenge 81: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 81 passed.");
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
