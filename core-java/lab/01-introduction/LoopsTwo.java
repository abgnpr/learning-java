
/*
 * Challenge 07: Java Loops II
 * Difficulty: Easy
 *
 * Task: Starting at start, repeatedly add increment multiplied by successive
 * powers of two and return each resulting term.
 * Complete: static List<Long> buildSeries(long start, long increment, int terms)
 * Run: java LoopsTwo.java
 */
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public final class LoopsTwo {
    private LoopsTwo() {
    }

    static List<Long> buildSeries(long start, long increment, int terms) {
        // A stream cannot carry the previous term as mutable state. The closed
        // form replaces that history: start + increment * (2^i - 1).
        return LongStream.iterate(1, i -> i + 1)
                .limit(terms)
                // This source starts at 1, so the exponent is i. Use 1L: an int
                // shift overflows at 31, and Math.pow would introduce doubles.
                .mapToObj(i -> start + increment * ((1L << i) - 1))
                // mapToObj crosses to Stream<Long>, where Collectors.toList()
                // is available; LongStream's collect has a different API.
                .collect(Collectors.toList());
        // terms must stay at most 63: Java masks larger shift counts modulo 64.
    }

    public static void main(String[] args) {
        check("one term", "2, 1, 1", List.of(3L), buildSeries(2, 1, 1));
        check("basic series", "2, 1, 4", List.of(3L, 5L, 9L, 17L), buildSeries(2, 1, 4));
        check("negative increment", "6, -6, 3", List.of(0L, -12L, -36L), buildSeries(6, -6, 3));
        check("largest representable term", "0, 1, 63", Long.MAX_VALUE, buildSeries(0, 1, 63).get(62));
        check("no terms", "99, 4, 0", List.of(), buildSeries(99, 4, 0));
        report("Challenge 07");
    }



    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */

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
