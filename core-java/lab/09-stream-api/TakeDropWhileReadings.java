/*
 * Challenge 70: Prefix Operations
 * Difficulty: Easy
 *
 * Task: Drop leading negative warm-up readings, then take readings while they
 * are at most the inclusive safety limit. Once a reading exceeds the limit,
 * ignore it and everything after it—even values that would otherwise qualify.
 * Complete: static List<Integer> stableWindow(List<Integer>, int)
 * Required focus: dropWhile and takeWhile; understand how they differ from filter.
 * Run: java TakeDropWhileReadings.java
 */
import java.util.List;

public final class TakeDropWhileReadings {
    private TakeDropWhileReadings() {
    }

    static List<Integer> stableWindow(List<Integer> readings, int safetyLimit) {
        throw new UnsupportedOperationException("TODO: select the bounded encounter-order prefix");
    }

    public static void main(String[] args) {
        check("drop warm-up values and stop at breach", "List.of(-2, -1, 3, 8, 10, 11, 4), 10", List.of(3, 8, 10), stableWindow(List.of(-2, -1, 3, 8, 10, 11, 4), 10));
        check("breach immediately after warm-up", "List.of(-3, -1, 20, 2), 10", List.of(), stableWindow(List.of(-3, -1, 20, 2), 10));
        check("no warm-up or breach", "List.of(1, 2), 10", List.of(1, 2), stableWindow(List.of(1, 2), 10));
        report("Challenge 70");
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
