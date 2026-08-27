/*
 * Challenge 18: Java Anagrams
 * Difficulty: Easy
 *
 * Task: Decide whether two strings contain the same non-whitespace characters
 * with the same multiplicities, ignoring character case. Punctuation counts.
 * Complete: static boolean areAnagrams(String first, String second)
 * Run: java Anagrams.java
 */
public final class Anagrams {
    private Anagrams() {
    }

    static boolean areAnagrams(String first, String second) {
        throw new UnsupportedOperationException("TODO: normalize and count the characters");
    }

    public static void main(String[] args) {
        checkEquals(true, areAnagrams("Dormitory", "Dirty room"), "spaces and case ignored");
        checkEquals(true, areAnagrams("Java", "avaJ"), "same multiplicities");
        checkEquals(false, areAnagrams("abc!", "cab"), "punctuation is significant");
        checkEquals(false, areAnagrams("aab", "abb"), "different multiplicities");
        System.out.println("Challenge 18 passed.");
    }

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
