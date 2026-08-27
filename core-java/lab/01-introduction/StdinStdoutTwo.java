/*
 * Challenge 04: Java Stdin and Stdout II
 * Difficulty: Easy
 *
 * Task: Parse three lines containing an integer, a decimal, and a label, then
 * return "label | integer | decimal". Round the decimal to two places with
 * HALF_UP and always use a locale-independent decimal point.
 * Complete: static String summarizeRecord(String input)
 * Run: java StdinStdoutTwo.java
 */
public final class StdinStdoutTwo {
    private StdinStdoutTwo() {
    }

    static String summarizeRecord(String input) {
        throw new UnsupportedOperationException("TODO: read all three lines without skipping the label");
    }

    public static void main(String[] args) {
        checkEquals("Ada Lovelace | 12 | 19.50",
                summarizeRecord("12\n19.5\nAda Lovelace\n"), "ordinary record");
        checkEquals("tea | -4 | 0.13",
                summarizeRecord("-4\n0.125\ntea\n"), "rounding and negative integer");
        checkEquals("two words | 0 | -8.00",
                summarizeRecord("0\n-8\ntwo words"), "label containing a space");
        System.out.println("Challenge 04 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
