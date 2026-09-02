/*
 * Challenge 17: Java String Reverse
 * Difficulty: Easy
 *
 * Task: Decide whether a string reads identically from left to right and right
 * to left; comparison is case-sensitive and includes every character.
 * Complete: static boolean isPalindrome(String text)
 * Run: java StringReverse.java
 */
public final class StringReverse {
    private StringReverse() {
    }

    static boolean isPalindrome(String text) {
        throw new UnsupportedOperationException("TODO: compare the text with its reverse");
    }

    public static void main(String[] args) {
        checkEquals(true, isPalindrome("level"), "odd-length palindrome");
        checkEquals(true, isPalindrome(""), "empty string");
        checkEquals(false, isPalindrome("Level"), "case-sensitive mismatch");
        checkEquals(false, isPalindrome("abca"), "non-palindrome");
        System.out.println("Challenge 17 passed.");
    }

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
