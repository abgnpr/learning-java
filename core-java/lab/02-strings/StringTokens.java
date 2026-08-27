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
        System.out.println("Challenge 19 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
