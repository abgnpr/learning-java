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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

public final class EndOfFile {
    private EndOfFile() {
    }

    /* Put separators before later lines, so empty input needs no trailing-newline
     * cleanup. readLine() drops a final terminator but preserves a trailing blank line. */
    static String numberLines(String input) {
        int i = 0;
        var sb = new StringBuilder();

        // Scanner's hasNextLine() does regex lookahead; BufferedReader reads a
        // line directly, which matters for large stdin.
        /*
        try (Scanner in = new Scanner(input)) {
            while (in.hasNextLine()) {
                if (i > 0) {
                    sb.append('\n');
                }
                ++i;
                sb.append(i).append(' ').append(in.nextLine());
            }
        }
        */

        // readLine() signals EOF with null, so assignment belongs in the
        // condition. StringReader fits already-decoded test input; System.in
        // would instead need an InputStreamReader.
        try (var reader = new BufferedReader(new StringReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(++i).append(' ').append(line);
            }
        } catch (IOException ioe) {
            // StringReader cannot fail here, but readLine() declares IOException;
            // wrapping preserves failure rather than returning partial output.
            throw new UncheckedIOException(ioe);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        checkEquals("1 alpha\n2 beta", numberLines("alpha\nbeta"), "two lines");
        checkEquals("1 one\n2 \n3 three", numberLines("one\n\nthree"), "blank middle line");
        checkEquals("1 ", numberLines("\n"), "single blank line");
        checkEquals("1 alpha\n2 ", numberLines("alpha\n\n"), "trailing blank line");
        checkEquals("1 alpha\n2 beta", numberLines("alpha\r\nbeta\r\n"), "CRLF input");
        checkEquals("", numberLines(""), "empty stream");
        if (failures > 0) {
            throw new AssertionError("Challenge 09: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 09 passed.");
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
     * Non-strings carry their type, so <12> the text and 12 the int never look
     * alike.
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
