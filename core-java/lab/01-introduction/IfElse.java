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

        // standard
        // if (value < 0) {
        // return "negative";
        // } else if (value == 0) {
        // return "zero";
        // } else if (value % 2 == 0) {
        // return "positive-even";
        // } else {
        // return "positive-odd";
        // }

        // preview in java 21
        // return switch (value) {
        // case int v when v < 0 -> "negative";
        // case 0 -> "zero";
        // case int v when v % 2 == 0 -> "positive-even";
        // default -> "positive-odd";
        // };

        // standard java 21 using switch
        return switch (Integer.signum(value)) {
            case -1 -> "negative";
            case 0 -> "zero";
            default -> value % 2 == 0 ? "positive-even" : "positive-odd";
        };

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
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
