/*
 * Challenge 23: Valid Username Regular Expression
 * Difficulty: Easy
 *
 * Task: Validate an ASCII username of 6 to 18 characters. It must start with a
 * letter; every remaining character must be a letter, digit, or underscore.
 * Complete: static boolean isValidUsername(String candidate)
 * Run: java ValidUsername.java
 */
public final class ValidUsername {
    private ValidUsername() {
    }

    static boolean isValidUsername(String candidate) {
        throw new UnsupportedOperationException("TODO: express and apply the complete username rule");
    }

    public static void main(String[] args) {
        checkEquals(true, isValidUsername("coder_7"), "letters underscore and digit");
        checkEquals(true, isValidUsername("A12345"), "minimum length");
        checkEquals(false, isValidUsername("_coder"), "must start with a letter");
        checkEquals(false, isValidUsername("ab12"), "too short");
        checkEquals(false, isValidUsername("name-with-dash"), "invalid punctuation");
        checkEquals(false, isValidUsername("abcdefghijklmnopqrs"), "too long");
        System.out.println("Challenge 23 passed.");
    }

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
