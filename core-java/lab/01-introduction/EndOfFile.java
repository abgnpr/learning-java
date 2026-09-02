/*
 * Challenge 09: Java End-of-file
 * Difficulty: Easy
 *
 * Task: Return the supplied text with every input line, including blank lines,
 * prefixed by its one-based line number and a space. Treat the end of the
 * String as EOF. Accept LF or CRLF terminators; a final line terminator does
 * not create another blank input line.
 * Complete: static String numberLines(String input)
 * Run: java EndOfFile.java
 */
public final class EndOfFile {
    private EndOfFile() {
    }

    static String numberLines(String input) {
        throw new UnsupportedOperationException("TODO: consume and number all lines until EOF");
    }

    public static void main(String[] args) {
        checkEquals("1 alpha\n2 beta", numberLines("alpha\nbeta"), "two lines");
        checkEquals("1 one\n2 \n3 three", numberLines("one\n\nthree"), "blank middle line");
        checkEquals("1 alpha\n2 beta", numberLines("alpha\r\nbeta\r\n"), "CRLF input");
        checkEquals("", numberLines(""), "empty stream");
        System.out.println("Challenge 09 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
