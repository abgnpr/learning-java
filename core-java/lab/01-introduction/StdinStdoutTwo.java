
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
            // Add the input: argument evaluation can throw before checkEquals()
            // gets a chance to print its label.
            throw new IllegalStateException("failed parsing input:\n<" + input + ">", e);
        }
    }

    public static void main(String[] args) {
        checkEquals("Ada Lovelace | 12 | 19.50",
                summarizeRecord("12\n19.5\nAda Lovelace\n"), "ordinary record");
        checkEquals("Timeothy Tim | 12 | 3.13",
                summarizeRecord("12\n3.13\nTimeothy Tim\n"), "ordinary record");
        checkEquals("tea | -4 | 0.13",
                summarizeRecord("-4\n0.125\ntea\n"), "rounding and negative integer");
        checkEquals("negative half | -1 | -0.13",
                summarizeRecord("-1\n-0.125\nnegative half\n"), "negative half-up rounding");
        checkEquals("two words | 0 | -8.00",
                summarizeRecord("0\n-8\ntwo words"), "label containing a space");
        checkEquals("precision | 7 | 2.67",
                summarizeRecord("7\n2.6749999999999999999\nprecision\n"),
                "decimal precision before rounding");

        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            checkEquals("locale | 5 | 19.50",
                    summarizeRecord("5\n19.5\nlocale\n"),
                    "dot-decimal input under a comma-decimal locale");
        } finally {
            Locale.setDefault(originalLocale);
        }

        if (failures > 0) {

            throw new AssertionError("Challenge 04: " + failures + " check(s) failed.");

        }

        System.out.println("Challenge 04 passed.");
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
