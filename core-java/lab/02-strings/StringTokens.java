/*
 * Challenge 19: Java String Tokens
 * Difficulty: Easy
 *
 * Task: Split text into its non-empty runs of Unicode letters, preserving the
 * original spelling and encounter order.
 * Complete: static List<String> tokenize(String text)
 * Run: java StringTokens.java
 */
import java.util.List;

public final class StringTokens {
    private StringTokens() {
    }

    static List<String> tokenize(String text) {
        throw new UnsupportedOperationException("TODO: split on non-letter delimiters");
    }

    public static void main(String[] args) {
        checkEquals(List.of("Tea", "coffee", "and", "cocoa"),
                tokenize("Tea, coffee... and cocoa!"), "punctuation delimiters");
        checkEquals(List.of("cats", "dogs"), tokenize("  42 cats + 7 dogs  "), "digits and spaces");
        checkEquals(List.of(), tokenize("--- 123 ---"), "no words");
        checkEquals(List.of("naïve", "café"), tokenize("naïve/café"), "Unicode letters");
        if (failures > 0) {
            throw new AssertionError("Challenge 19: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 19 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
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
