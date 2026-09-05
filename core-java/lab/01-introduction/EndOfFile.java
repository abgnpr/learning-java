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
        check("two lines", "alpha\nbeta", "1 alpha\n2 beta", numberLines("alpha\nbeta"));
        check("blank middle line", "one\n\nthree", "1 one\n2 \n3 three", numberLines("one\n\nthree"));
        check("single blank line", "\n", "1 ", numberLines("\n"));
        check("trailing blank line", "alpha\n\n", "1 alpha\n2 ", numberLines("alpha\n\n"));
        check("CRLF input", "alpha\r\nbeta\r\n", "1 alpha\n2 beta", numberLines("alpha\r\nbeta\r\n"));
        check("empty stream", "", "", numberLines(""));
        report("Challenge 09");
    }



    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look
     * alike.
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
