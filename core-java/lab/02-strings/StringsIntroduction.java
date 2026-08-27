/*
 * Challenge 14: Java Strings Introduction
 * Difficulty: Easy
 *
 * Task: For two non-empty ASCII strings, report their combined length, whether
 * the first sorts after the second using case-sensitive String.compareTo, and
 * a version of the pair with each first letter capitalized.
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
        System.out.println("Challenge 14 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
