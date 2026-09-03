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
        // Compare mirrored pairs once: the right index is text.length() - 1 - i.
        // length() / 2 skips the unpaired middle character and makes empty input
        // true without a special case. Unlike reversing, this uses no extra copy.
        for (int i = 0; i < text.length() / 2; i++) {
            if (text.charAt(i) != text.charAt(text.length() - 1 - i)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Reverse-and-compare alternatives: both allocate a complete reversed copy,
     * so the mirrored scan above is the better fit when only a boolean is needed.
     *
     * static String reverser(String text) {
     *     return new StringBuilder(text).reverse().toString();
     * }
     *
     * static String twoPointerReverser(String text) {
     *     char[] chars = text.toCharArray();
     *     for (int left = 0, right = chars.length - 1; left < right; left++, right--) {
     *         char temporary = chars[left];
     *         chars[left] = chars[right];
     *         chars[right] = temporary;
     *     }
     *     return new String(chars);
     * }
     *
     * return text.equals(reverser(text));
     */

    public static void main(String[] args) {
        checkEquals(true, isPalindrome("level"), "odd-length palindrome");
        checkEquals(true, isPalindrome("abba"), "even-length palindrome");
        checkEquals(true, isPalindrome(""), "empty string");
        checkEquals(false, isPalindrome("Level"), "case-sensitive mismatch");
        checkEquals(false, isPalindrome("a!ba"), "punctuation is not ignored");
        checkEquals(false, isPalindrome("abca"), "non-palindrome");
        if (failures > 0) {
            throw new AssertionError("Challenge 17: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 17 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected == actual) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + ":\n  expected: <" + expected + ">"
                + "\n    actual: <" + actual + ">");
    }
}
