/*
 * Challenge 31 — Java Arraylist (Easy)
 *
 * Task: Store rows of varying lengths and answer one-based row/column
 * queries. Return "ERROR!" when a requested element does not exist.
 * Complete: answerQueries(List<List<Integer>> rows, List<Query> queries)
 * Run: java ArrayListQueries.java
 */
import java.util.List;

public class ArrayListQueries {
    record Query(int row, int column) { }

    static List<String> answerQueries(List<List<Integer>> rows, List<Query> queries) {
        // TODO: Resolve each query safely against the ragged rows.
        throw new UnsupportedOperationException("TODO: answer array-list queries");
    }

    public static void main(String[] args) {
        List<List<Integer>> rows = List.of(
            List.of(5, 8),
            List.of(),
            List.of(7, 9, 11)
        );

        check("existing element", "1, 2", List.of("8"), answerQueries(rows, List.of(new Query(1, 2))));
        check("empty row", "2, 1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(2, 1))));
        check("mixed queries", "3, 1", List.of("7", "ERROR!"), answerQueries(rows, List.of(new Query(3, 1), new Query(3, 4))));
        report("Challenge 31");
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
