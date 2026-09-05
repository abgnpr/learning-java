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
        check("mixed lengths", "List.of(\"a\", \"java\", \"stream\")", new LengthStats(3, 11, OptionalInt.of(1), OptionalInt.of(6),
                        OptionalDouble.of(11.0 / 3)), summarizeLengths(List.of("a", "java", "stream")));
        check("empty string is still an element", "List.of(\"\")", new LengthStats(1, 0, OptionalInt.of(0), OptionalInt.of(0),
                        OptionalDouble.of(0.0)), summarizeLengths(List.of("")));
        check("empty input", "List.of()", new LengthStats(0, 0, OptionalInt.empty(), OptionalInt.empty(),
                        OptionalDouble.empty()), summarizeLengths(List.of()));
        report("Challenge 74");
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
