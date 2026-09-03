
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
        checkEquals("", formatRows(List.of()), "no rows");
        checkEquals("            :0000", formatRows(List.of(new Row("", 0))), "empty label and zero quantity");
        checkEquals("apple       :0007", formatRows(List.of(new Row("apple", 7))), "short label");
        checkEquals("twelve-chars:0042", formatRows(List.of(new Row("twelve-chars", 42))), "full-width label");
        checkEquals("limit       :9999", formatRows(List.of(new Row("limit", 9999))), "maximum quantity");
        checkEquals("tea         :0003\ncoffee      :0120",
                formatRows(List.of(new Row("tea", 3), new Row("coffee", 120))), "multiple rows");
        if (failures > 0) {
            throw new AssertionError("Challenge 05: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 05 passed.");
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
