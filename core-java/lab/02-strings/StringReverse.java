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
        check("odd-length palindrome", "level", true, isPalindrome("level"));
        check("even-length palindrome", "abba", true, isPalindrome("abba"));
        check("empty string", "", true, isPalindrome(""));
        check("case-sensitive mismatch", "Level", false, isPalindrome("Level"));
        check("punctuation is not ignored", "a!ba", false, isPalindrome("a!ba"));
        check("non-palindrome", "abca", false, isPalindrome("abca"));
        report("Challenge 17");
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
