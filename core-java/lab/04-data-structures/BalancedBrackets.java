/*
 * Challenge 35 — Java Stack (Medium)
 *
 * Task: Decide whether every (), [], and {} delimiter in a string closes in
 * the correct order. Input contains only those delimiters; an empty string is
 * balanced.
 * Complete: isBalanced(String text)
 * Run: java BalancedBrackets.java
 */
public class BalancedBrackets {
    static boolean isBalanced(String text) {
        // TODO: Track opening delimiters with a stack.
        throw new UnsupportedOperationException("TODO: validate bracket order");
    }

    public static void main(String[] args) {
        check(true, isBalanced("{}()[]"), "separate pairs");
        check(true, isBalanced("({[]})"), "nested pairs");
        check(false, isBalanced("([)]"), "crossed pairs");
        check(false, isBalanced("(("), "unclosed pair");
        check(true, isBalanced(""), "empty text");
        if (failures > 0) {
            throw new AssertionError("Challenge 35: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 35 passed.");
    }

    private static int failures = 0;

    static void check(boolean expected, boolean actual, String label) {
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
