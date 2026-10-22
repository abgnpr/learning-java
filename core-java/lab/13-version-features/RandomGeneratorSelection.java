/*
 * Challenge 114: Random Generator Selection (Medium)
 * Java feature: Java 17 enhanced PRNG API (JEP 356).
 *
 * Task: Select a named RandomGenerator algorithm, seed it, and return exactly
 * count die rolls in the inclusive range 1..6. Reject a negative count.
 * This is simulation code; security-sensitive values require SecureRandom.
 *
 * Complete: dieRolls(...).
 * Run on JDK 17+: java RandomGeneratorSelection.java
 */
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class RandomGeneratorSelection {
    private RandomGeneratorSelection() {
    }

    static List<Integer> dieRolls(String algorithm, long seed, int count) {
        // TODO: Select the named factory, create(seed), then use ints(count, 1, 7).
        throw new UnsupportedOperationException("TODO: select and use a RandomGenerator");
    }

    public static void main(String[] args) {
        List<Integer> first = dieRolls("L64X128MixRandom", 42L, 8);
        List<Integer> second = dieRolls("L64X128MixRandom", 42L, 8);

        check("same algorithm and seed are reproducible", first, first, second);
        check("requested number of rolls", "count=8", 8, first.size());
        checkThat("all rolls are in 1..6", first,
                first.stream().allMatch(value -> value >= 1 && value <= 6));
        check("zero count produces an empty list", "count=0",
                List.of(), dieRolls("L64X128MixRandom", 42L, 0));
        check("negative count is rejected", "count=-1",
                "IllegalArgumentException",
                thrownType(() -> dieRolls("L64X128MixRandom", 42L, -1)));

        report("Challenge 114");
    }

    @FunctionalInterface
    interface Action {
        void run();
    }

    private static String thrownType(Action action) {
        try {
            action.run();
            return "nothing thrown";
        } catch (RuntimeException exception) {
            return exception.getClass().getSimpleName();
        }
    }

    private static int passes;
    private static int failures;

    private static void check(String label, Object input, Object expected, Object actual) {
        record(java.util.Objects.deepEquals(expected, actual), label, input, expected, actual);
    }

    private static void checkThat(String label, Object input, boolean condition) {
        record(condition, label, input, true, condition);
    }

    private static void record(
            boolean ok, String label, Object input, Object expected, Object actual) {
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) throw new AssertionError(challenge + " failed");
        System.out.println(challenge + " passed.");
    }
}
