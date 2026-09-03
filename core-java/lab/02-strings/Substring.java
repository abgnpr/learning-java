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
        if (failures > 0) {
            throw new AssertionError("Challenge 15: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 15 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(String expected, String actual, String label) {
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
