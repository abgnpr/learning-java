/*
 * Challenge 25 — Java BigDecimal (Medium)
 *
 * Task: Sort decimal strings by numeric value from greatest to least while
 * preserving each string's spelling and the input order of equal values.
 * Complete: sortNumericallyDescending(List<String> values)
 * Run: java BigDecimalOrdering.java
 */
import java.util.List;

public class BigDecimalOrdering {
    static List<String> sortNumericallyDescending(List<String> values) {
        // TODO: Compare with arbitrary precision; do not convert through double.
        throw new UnsupportedOperationException("TODO: sort decimal strings");
    }

    public static void main(String[] args) {
        checkEquals(
            List.of("90", "56.6", "50", "9", "02.34", "0.12", ".12", "0", "000.000", "-100"),
            sortNumericallyDescending(List.of(
                "9", "-100", "50", "0", "56.6", "90", "0.12", ".12", "02.34", "000.000")),
            "mixed values"
        );
        checkEquals(
            List.of("1.0", "1", "1.00"),
            sortNumericallyDescending(List.of("1.0", "1", "1.00")),
            "equal values stay stable"
        );
        checkEquals(
            List.of("-0.5", "-1", "-10"),
            sortNumericallyDescending(List.of("-1", "-0.5", "-10")),
            "negative values"
        );
        checkEquals(
            List.of("9007199254740993", "9007199254740992"),
            sortNumericallyDescending(List.of("9007199254740992", "9007199254740993")),
            "values that double cannot distinguish"
        );
        if (failures > 0) {
            throw new AssertionError("Challenge 25: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 25 passed.");
    }

    private static int failures = 0;

    static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
