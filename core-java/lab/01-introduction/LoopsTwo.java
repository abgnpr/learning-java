
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
        checkEquals(List.of(3L), buildSeries(2, 1, 1), "one term");
        checkEquals(List.of(3L, 5L, 9L, 17L), buildSeries(2, 1, 4), "basic series");
        checkEquals(List.of(0L, -12L, -36L), buildSeries(6, -6, 3), "negative increment");
        checkEquals(Long.MAX_VALUE, buildSeries(0, 1, 63).get(62), "largest representable term");
        checkEquals(List.of(), buildSeries(99, 4, 0), "no terms");
        if (failures > 0) {
            throw new AssertionError("Challenge 07: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 07 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + show(actual));
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + "\n  expected: " + show(expected)
                + "\n    actual: " + show(actual));
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
}
