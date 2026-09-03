
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
        // useLocale(ROOT): Scanner's number parsers read separators from the
        // DEFAULT locale. Under de-DE, '.' is the grouping separator, so "19.5"
        // is a malformed group (groups must be 3 digits) -> InputMismatchException.
        // ROOT pins '.' as the decimal point, so the parse depends on the input
        // only, not on the machine. Machine data formats -> ROOT; text shown to a
        // person -> that person's locale.
        try (Scanner in = new Scanner(input).useLocale(Locale.ROOT)) {
            int i = in.nextInt();

            // Trap: a double holds ~17 significant digits, so nextDouble() on
            // "2.6749999999999999999" (20 digits) already rounds it to 2.675 --
            // then HALF_UP correctly gives 2.68 instead of the wanted 2.67. The
            // precision is gone before setScale ever runs.
            //     double d = in.nextDouble();
            //     BigDecimal bd = new BigDecimal(Double.toString(d));
            // BigDecimal keeps every digit of the text, so rounding sees the real
            // value. (new BigDecimal(double) is also wrong -- it captures the
            // exact binary value; build from a String.)
            BigDecimal bd = in.nextBigDecimal();

            // HALF_UP because .5 must round away from zero. String.format("%.2f")
            // would use HALF_EVEN and turn 0.125 into 0.12, not 0.13.
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            // Token methods (nextInt/nextBigDecimal/next) stop at the end of the
            // token, leaving the rest of that line -- including '\n' -- unread.
            // nextLine() does not skip leading whitespace, so without this flush
            // it returns "" instead of the label. Back-to-back token calls hide
            // the problem because they skip leading whitespace themselves.
            in.nextLine();

            // nextLine(), not next(): the label may contain spaces.
            String l = in.nextLine();

            // Output order is label | integer | decimal -- not the read order.
            String s = l + " | " + String.valueOf(i) + " | " + String.valueOf(bd);
            return s;
        } catch (Exception e) {
            // Wrap rather than rethrow: a throw from here escapes before
            // checkEquals is entered (Java evaluates arguments first), so the
            // test's label never prints. Attaching the input names the culprit.
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
