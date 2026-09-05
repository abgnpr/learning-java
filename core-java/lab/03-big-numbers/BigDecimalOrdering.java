/*
 * Challenge 25 — Java BigDecimal (Medium)
 *
 * Task: Sort decimal strings by numeric value from greatest to least while
 * preserving each string's spelling and the input order of equal values.
 * Complete: sortNumericallyDescending(List<String> values)
 * Run: java BigDecimalOrdering.java
 */
import java.util.List;

public class BigDecimalOrdering {
    static List<String> sortNumericallyDescending(List<String> values) {
        // TODO: Compare with arbitrary precision; do not convert through double.
        throw new UnsupportedOperationException("TODO: sort decimal strings");
    }

    public static void main(String[] args) {
        check("mixed values", "List.of( \"9\", \"-100\", \"50\", \"0\", \"56.6\", \"90\", \"0.12\", \".12\", \"02.34\", \"000.000\")", List.of("90", "56.6", "50", "9", "02.34", "0.12", ".12", "0", "000.000", "-100"), sortNumericallyDescending(List.of(
                "9", "-100", "50", "0", "56.6", "90", "0.12", ".12", "02.34", "000.000")));
        check("equal values stay stable", "List.of(\"1.0\", \"1\", \"1.00\")", List.of("1.0", "1", "1.00"), sortNumericallyDescending(List.of("1.0", "1", "1.00")));
        check("negative values", "List.of(\"-1\", \"-0.5\", \"-10\")", List.of("-0.5", "-1", "-10"), sortNumericallyDescending(List.of("-1", "-0.5", "-10")));
        check("values that double cannot distinguish", "List.of(\"9007199254740992\", \"9007199254740993\")", List.of("9007199254740993", "9007199254740992"), sortNumericallyDescending(List.of("9007199254740992", "9007199254740993")));
        report("Challenge 25");
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
