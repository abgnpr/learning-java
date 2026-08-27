/*
 * Challenge 03: Java If-Else
 * Difficulty: Easy
 *
 * Task: Classify an integer as negative, zero, positive-even, or positive-odd.
 * Complete: static String classify(int value)
 * Run: java IfElse.java
 */
public final class IfElse {
    private IfElse() {
    }

    static String classify(int value) {
        throw new UnsupportedOperationException("TODO: implement the conditional classification");
    }

    public static void main(String[] args) {
        checkEquals("negative", classify(-8), "negative value");
        checkEquals("zero", classify(0), "zero");
        checkEquals("positive-even", classify(42), "positive even value");
        checkEquals("positive-odd", classify(17), "positive odd value");
        System.out.println("Challenge 03 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
