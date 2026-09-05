/*
 * Challenge 67: Distinct, Sorted, Skip, and Limit
 * Difficulty: Easy
 *
 * Task: Remove duplicate scores, sort highest first, skip pageOffset scores,
 * and return at most pageSize scores. Reject negative paging arguments with
 * IllegalArgumentException.
 * Complete: static List<Integer> page(List<Integer>, long, long)
 * Required focus: distinct, sorted, skip, and limit.
 * Run: java DistinctScorePage.java
 */
import java.util.Comparator;
import java.util.List;

public final class DistinctScorePage {
    private DistinctScorePage() {
    }

    static List<Integer> page(List<Integer> scores, long pageOffset, long pageSize) {
        throw new UnsupportedOperationException("TODO: create a distinct descending page");
    }

    public static void main(String[] args) {
        List<Integer> scores = List.of(50, 90, 70, 90, 40, 70, 100);
        check("middle page", "scores, 1, 2", List.of(90, 70), page(scores, 1, 2));
        check("oversized page", "scores, 0, 20", List.of(100, 90, 70, 50, 40), page(scores, 0, 20));
        check("offset beyond result", "scores, 20, 2", List.of(), page(scores, 20, 2));
        expectIllegalArgument(() -> page(scores, -1, 2), "negative offset");
        expectIllegalArgument(() -> page(scores, 0, -1), "negative size");
        report("Challenge 67");
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
