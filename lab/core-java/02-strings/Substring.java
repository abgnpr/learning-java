/*
 * Challenge 15: Java Substring
 * Difficulty: Easy
 *
 * Task: Return the characters from startInclusive up to, but not including,
 * endExclusive, using Java String index rules.
 * Complete: static String slice(String text, int startInclusive, int endExclusive)
 * Run: java Substring.java
 */
public final class Substring {
    private Substring() {
    }

    static String slice(String text, int startInclusive, int endExclusive) {
        throw new UnsupportedOperationException("TODO: return the requested substring");
    }

    public static void main(String[] args) {
        checkEquals("cyclo", slice("encyclopedia", 2, 7), "middle slice");
        checkEquals("Java", slice("Java", 0, 4), "whole string");
        checkEquals("", slice("abc", 2, 2), "empty slice");
        System.out.println("Challenge 15 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
