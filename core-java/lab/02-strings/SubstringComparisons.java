/*
 * Challenge 16: Java Substring Comparisons
 * Difficulty: Easy
 *
 * Task: Among all contiguous substrings of the requested length, return the
 * lexicographically smallest and largest according to String.compareTo. The
 * requested length is between 1 and the text length, inclusive.
 * Complete: static Extremes extremes(String text, int length)
 * Run: java SubstringComparisons.java
 */
public final class SubstringComparisons {
    record Extremes(String smallest, String largest) {
    }

    private SubstringComparisons() {
    }

    static Extremes extremes(String text, int length) {
        throw new UnsupportedOperationException("TODO: scan and compare each fixed-length substring");
    }

    public static void main(String[] args) {
        checkEquals(new Extremes("ava", "wel"), extremes("welcometojava", 3), "mixed substrings");
        checkEquals(new Extremes("aa", "aa"), extremes("aaaa", 2), "identical substrings");
        checkEquals(new Extremes("a", "z"), extremes("zab", 1), "single characters");
        System.out.println("Challenge 16 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
