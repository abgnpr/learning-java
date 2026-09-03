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
        // Seed from window 0 to avoid nullable extrema. The last valid start is
        // text.length() - length, so the exclusive loop bound includes it.
        var smallest = text.substring(0, length);
        var largest = smallest;
        for (int i = 1; i < text.length() - length + 1; i++) {
            var s = text.substring(i, i + length);
            if (s.compareTo(smallest) < 0) {
                smallest = s;
            }
            if (s.compareTo(largest) > 0) {
                largest = s;
            }
        }
        return new Extremes(smallest, largest);
    }

    /*
     * Stream alternative: materialize the windows because a stream can only be
     * consumed once; min() and max() are separate terminal operations. The loop
     * above is preferable here because it finds both extrema in one pass and
     * uses constant extra space.
     *
     * static Extremes extremesWithStreams(String text, int length) {
     *     var windows = IntStream.rangeClosed(0, text.length() - length)
     *             .mapToObj(i -> text.substring(i, i + length))
     *             .toList();
     *     return new Extremes(Collections.min(windows), Collections.max(windows));
     * }
     */

    public static void main(String[] args) {
        checkEquals(new Extremes("ava", "wel"), extremes("welcometojava", 3), "mixed substrings");
        checkEquals(new Extremes("aa", "aa"), extremes("aaaa", 2), "identical substrings");
        checkEquals(new Extremes("java", "java"), extremes("java", 4), "whole string is one window");
        checkEquals(new Extremes("a", "z"), extremes("zab", 1), "single characters");
        if (failures > 0) {
            throw new AssertionError("Challenge 16: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 16 passed.");
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
