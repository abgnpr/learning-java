
/*
 * Challenge 05: Java Output Formatting
 * Difficulty: Easy
 *
 * Task: Format each row as a left-aligned 12-character label, a colon, and a
 * four-digit zero-padded quantity; join multiple rows with newlines. Inputs
 * have labels of at most 12 characters and quantities from 0 through 9999.
 * Complete: static String formatRows(List<Row> rows)
 * Run: java OutputFormatting.java
 */
import java.util.List;
import java.util.stream.Collectors;

public final class OutputFormatting {
    record Row(String label, int quantity) {
    }

    private OutputFormatting() {
    }

    static String formatRows(List<Row> rows) {
        // Keep the row in one format string: "%-12s" is a left-aligned width,
        // while "%04d" uses 0 as a flag (not integer precision). Width pads but
        // never truncates; the contract's bounds make clipping unnecessary.
        return rows.stream()
                .map(row -> String.format("%-12s:%04d", row.label, row.quantity))
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        check("no rows", "List.of()", "", formatRows(List.of()));
        check("empty label and zero quantity", "\"\", 0", "            :0000", formatRows(List.of(new Row("", 0))));
        check("short label", "\"apple\", 7", "apple       :0007", formatRows(List.of(new Row("apple", 7))));
        check("full-width label", "\"twelve-chars\", 42", "twelve-chars:0042", formatRows(List.of(new Row("twelve-chars", 42))));
        check("maximum quantity", "\"limit\", 9999", "limit       :9999", formatRows(List.of(new Row("limit", 9999))));
        check("multiple rows", "\"tea\", 3", "tea         :0003\ncoffee      :0120", formatRows(List.of(new Row("tea", 3), new Row("coffee", 120))));
        report("Challenge 05");
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
