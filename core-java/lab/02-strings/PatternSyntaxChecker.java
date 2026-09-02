/*
 * Challenge 20: Pattern Syntax Checker
 * Difficulty: Easy
 *
 * Task: Return whether a string is a syntactically valid Java regular
 * expression; invalid patterns must not escape as exceptions.
 * Complete: static boolean isValidRegex(String candidate)
 * Run: java PatternSyntaxChecker.java
 */
public final class PatternSyntaxChecker {
    private PatternSyntaxChecker() {
    }

    static boolean isValidRegex(String candidate) {
        throw new UnsupportedOperationException("TODO: compile the pattern and handle syntax failures");
    }

    public static void main(String[] args) {
        checkEquals(true, isValidRegex("[a-z]+"), "character class");
        checkEquals(true, isValidRegex("(cat|dog){2}"), "group and quantifier");
        checkEquals(false, isValidRegex("[unterminated"), "open character class");
        checkEquals(false, isValidRegex("*bad"), "orphan quantifier");
        System.out.println("Challenge 20 passed.");
    }

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
