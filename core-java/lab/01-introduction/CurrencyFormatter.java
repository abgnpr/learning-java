
/*
 * Challenge 13: Java Currency Formatter
 * Difficulty: Easy
 *
 * Task: Format a decimal amount as currency using the supplied Locale.
 * Complete: static String formatCurrency(BigDecimal amount, Locale locale)
 * Run: java CurrencyFormatter.java
 */
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {
    private CurrencyFormatter() {
    }

    static String formatCurrency(BigDecimal amount, Locale locale) {
        var numberFormat = NumberFormat.getCurrencyInstance(locale);
        return numberFormat.format(amount);
    }

    public static void main(String[] args) {
        checkEquals("$0.00", formatCurrency(BigDecimal.ZERO, Locale.US), "US zero amount");
        checkEquals("$1,234.50", formatCurrency(new BigDecimal("1234.50"), Locale.US), "US amount");
        checkEquals("£1,234.50", formatCurrency(new BigDecimal("1234.50"), Locale.UK), "UK amount");
        checkEquals("1.234,50 €", formatCurrency(new BigDecimal("1234.50"), Locale.GERMANY), "German amount");
        checkEquals("-$42.00", formatCurrency(new BigDecimal("-42"), Locale.US), "negative amount");
        if (failures > 0) {
            throw new AssertionError("Challenge 13: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 13 passed.");
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
