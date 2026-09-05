/*
 * Challenge 16: Java Substring Comparisons
 * Difficulty: Easy
 *
 * Task: Among all contiguous substrings of the requested length, return the
 * lexicographically smallest and largest according to String.compareTo. The
 * requested length is between 1 and the text length, inclusive.
 * Complete: static Extremes extremes(String text, int length)
 * Run: java SubstringComparisons.java
 */
public final class SubstringComparisons {
    record Extremes(String smallest, String largest) {
    }

    private SubstringComparisons() {
    }

    static Extremes extremes(String text, int length) {
        // Seed from window 0 to avoid nullable extrema. The last valid start is
        // text.length() - length, so the exclusive loop bound includes it.
        var smallest = text.substring(0, length);
        var largest = smallest;
        for (int i = 1; i < text.length() - length + 1; i++) {
            var s = text.substring(i, i + length);
            if (s.compareTo(smallest) < 0) {
                smallest = s;
            }
            if (s.compareTo(largest) > 0) {
                largest = s;
            }
        }
        return new Extremes(smallest, largest);
    }

    /*
     * Stream alternative: materialize the windows because a stream can only be
     * consumed once; min() and max() are separate terminal operations. The loop
     * above is preferable here because it finds both extrema in one pass and
     * uses constant extra space.
     *
     * static Extremes extremesWithStreams(String text, int length) {
     *     var windows = IntStream.rangeClosed(0, text.length() - length)
     *             .mapToObj(i -> text.substring(i, i + length))
     *             .toList();
     *     return new Extremes(Collections.min(windows), Collections.max(windows));
     * }
     */

    public static void main(String[] args) {
        check("mixed substrings", "\"welcometojava\", 3", new Extremes("ava", "wel"), extremes("welcometojava", 3));
        check("identical substrings", "\"aaaa\", 2", new Extremes("aa", "aa"), extremes("aaaa", 2));
        check("whole string is one window", "\"java\", 4", new Extremes("java", "java"), extremes("java", 4));
        check("single characters", "\"zab\", 1", new Extremes("a", "z"), extremes("zab", 1));
        report("Challenge 16");
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
