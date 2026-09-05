
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Scanner;

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
        // Scanner reads numbers in the default locale; ROOT makes '.' mean a
        // decimal point even on a machine such as de-DE. Input format, not host
        // settings, must decide whether "19.5" parses.
        try (Scanner in = new Scanner(input).useLocale(Locale.ROOT)) {
            int i = in.nextInt();

            // Do not parse through double: it rounds before HALF_UP sees the
            // input. BigDecimal keeps the decimal text exact.
            //     double d = in.nextDouble();
            //     BigDecimal bd = new BigDecimal(Double.toString(d));
            BigDecimal bd = in.nextBigDecimal();

            // HALF_UP because .5 must round away from zero. String.format("%.2f")
            // would use HALF_EVEN and turn 0.125 into 0.12, not 0.13.
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            // Token reads leave their line break behind; consume it before the
            // following nextLine(), which otherwise returns the empty remainder.
            in.nextLine();

            // nextLine(), not next(): the label may contain spaces.
            String l = in.nextLine();

            // Output order is label | integer | decimal -- not the read order.
            String s = l + " | " + String.valueOf(i) + " | " + String.valueOf(bd);
            return s;
        } catch (Exception e) {
            // Add the input: argument evaluation can throw before check()
            // gets a chance to print its label.
            throw new IllegalStateException("failed parsing input:\n<" + input + ">", e);
        }
    }

    public static void main(String[] args) {
        check("ordinary record", "12\n19.5\nAda Lovelace\n", "Ada Lovelace | 12 | 19.50", summarizeRecord("12\n19.5\nAda Lovelace\n"));
        check("ordinary record", "12\n3.13\nTimeothy Tim\n", "Timeothy Tim | 12 | 3.13", summarizeRecord("12\n3.13\nTimeothy Tim\n"));
        check("rounding and negative integer", "-4\n0.125\ntea\n", "tea | -4 | 0.13", summarizeRecord("-4\n0.125\ntea\n"));
        check("negative half-up rounding", "-1\n-0.125\nnegative half\n", "negative half | -1 | -0.13", summarizeRecord("-1\n-0.125\nnegative half\n"));
        check("label containing a space", "0\n-8\ntwo words", "two words | 0 | -8.00", summarizeRecord("0\n-8\ntwo words"));
        check("decimal precision before rounding", "7\n2.6749999999999999999\nprecision\n", "precision | 7 | 2.67", summarizeRecord("7\n2.6749999999999999999\nprecision\n"));

        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            check("dot-decimal input under a comma-decimal locale", "5\n19.5\nlocale\n", "locale | 5 | 19.50", summarizeRecord("5\n19.5\nlocale\n"));
        } finally {
            Locale.setDefault(originalLocale);
        }
        report("Challenge 04");
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
