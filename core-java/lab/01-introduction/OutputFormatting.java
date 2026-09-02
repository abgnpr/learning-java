
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
        // One format string, not two formats glued with '+': the literal ':'
        // sits between the conversions exactly where it appears in the output,
        // so the whole row shape is readable at a glance.
        //
        // "%-12s" -- width 12, '-' left-aligns (right is the default).
        // "%04d"  -- width 4 padded with zeros; '0' is a FLAG, not a precision.
        // ("%.4d" is illegal: precision does not apply to integers.)
        //
        // Width pads but never truncates: a 20-char label emits all 20 chars
        // and pushes the column out. The contract caps labels at 12, so no
        // clipping is needed -- but on unbounded input the fix is the
        // precision "%-12.12s" (max chars), never a substring() guard.
        //
        // Quantities over 9999 overflow the column the same way; the contract
        // caps them at 9999.
        return rows.stream()
                .map(row -> String.format("%-12s:%04d", row.label, row.quantity))
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        checkEquals("apple       :0007", formatRows(List.of(new Row("apple", 7))), "short label");
        checkEquals("twelve-chars:0042", formatRows(List.of(new Row("twelve-chars", 42))), "full-width label");
        checkEquals("tea         :0003\ncoffee      :0120",
                formatRows(List.of(new Row("tea", 3), new Row("coffee", 120))), "multiple rows");
        System.out.println("Challenge 05 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
