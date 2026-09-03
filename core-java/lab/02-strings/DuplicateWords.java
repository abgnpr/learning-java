/*
 * Challenge 22: Java Regex 2 - Duplicate Words
 * Difficulty: Medium
 *
 * Task: Collapse each run of adjacent duplicate ASCII-letter words
 * case-insensitively, preserving the spelling of the run's first word. Input
 * words are separated by single spaces, as is the returned text.
 * Complete: static String removeAdjacentDuplicates(String text)
 * Run: java DuplicateWords.java
 */
public final class DuplicateWords {
    private DuplicateWords() {
    }

    static String removeAdjacentDuplicates(String text) {
        throw new UnsupportedOperationException("TODO: replace adjacent duplicate-word runs");
    }

    public static void main(String[] args) {
        checkEquals("Go home", removeAdjacentDuplicates("Go go GO home"), "three-word duplicate run");
        checkEquals("red blue RED", removeAdjacentDuplicates("red blue blue RED red"), "separate runs");
        checkEquals("one two three", removeAdjacentDuplicates("one two three"), "no duplicates");
        checkEquals("Echo", removeAdjacentDuplicates("Echo ECHO echo"), "whole input is one run");
        if (failures > 0) {
            throw new AssertionError("Challenge 22: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 22 passed.");
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
