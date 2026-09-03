/*
 * Challenge 14: Java Strings Introduction
 * Difficulty: Easy
 *
 * Task: For two non-empty ASCII strings, return a Summary containing their
 * combined length, whether the first sorts after the second using
 * case-sensitive String.compareTo, and the pair with both first letters
 * capitalized.
 * Complete: static Summary analyze(String first, String second)
 * Run: java StringsIntroduction.java
 */
public final class StringsIntroduction {
    record Summary(int combinedLength, boolean firstAfterSecond, String capitalizedPair) {
    }

    private StringsIntroduction() {
    }

    static Summary analyze(String first, String second) {
        throw new UnsupportedOperationException("TODO: compute all three string facts");
    }

    public static void main(String[] args) {
        checkEquals(new Summary(8, true, "Java Code"), analyze("java", "code"), "first sorts after");
        checkEquals(new Summary(10, false, "Hello World"), analyze("hello", "world"), "first sorts before");
        checkEquals(new Summary(2, true, "A B"), analyze("a", "B"), "case-sensitive ordering");
        if (failures > 0) {
            throw new AssertionError("Challenge 14: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 14 passed.");
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
