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
        check("iterate powers", "5", List.of(1L, 2L, 4L, 8L, 16L), powersOfTwo(5));
        check("zero powers", "0", List.of(), powersOfTwo(0));
        check("generated copies", "\"go\", 3", List.of("go", "go", "go"), repeat("go", 3));
        check("zero copies", "\"go\", 0", List.of(), repeat("go", 0));
        expectIllegalArgument(() -> powersOfTwo(-1), "negative iterate count");
        expectIllegalArgument(() -> repeat("go", -1), "negative generate count");
        report("Challenge 81");
    }

    private static void expectIllegalArgument(Runnable action, String label) {
        try {
            action.run();
            throw new AssertionError(label + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
