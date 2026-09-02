/*
 * Challenge 13: Java Currency Formatter
 * Difficulty: Easy
 *
 * Task: Format a decimal amount as currency using the supplied Locale.
 * Complete: static String formatCurrency(BigDecimal amount, Locale locale)
 * Run: java CurrencyFormatter.java
 */
import java.math.BigDecimal;
import java.util.Locale;

public final class CurrencyFormatter {
    private CurrencyFormatter() {
    }

    static String formatCurrency(BigDecimal amount, Locale locale) {
        throw new UnsupportedOperationException("TODO: use locale-aware currency formatting");
    }

    public static void main(String[] args) {
        checkEquals("$1,234.50", formatCurrency(new BigDecimal("1234.50"), Locale.US), "US amount");
        checkEquals("£1,234.50", formatCurrency(new BigDecimal("1234.50"), Locale.UK), "UK amount");
        checkEquals("-$42.00", formatCurrency(new BigDecimal("-42"), Locale.US), "negative amount");
        System.out.println("Challenge 13 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
