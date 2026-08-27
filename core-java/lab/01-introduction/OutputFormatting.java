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

public final class OutputFormatting {
    record Row(String label, int quantity) {
    }

    private OutputFormatting() {
    }

    static String formatRows(List<Row> rows) {
        throw new UnsupportedOperationException("TODO: apply fixed-width formatting to every row");
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
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
