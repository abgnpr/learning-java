/*
 * Challenge 11: Java Int to String
 * Difficulty: Easy
 *
 * Task: Convert an int to its ordinary base-10 String representation.
 * Complete: static String toDecimalString(int value)
 * Run: java IntToString.java
 */
public final class IntToString {
    private IntToString() {
    }

    static String toDecimalString(int value) {
        throw new UnsupportedOperationException("TODO: convert the integer to text");
    }

    public static void main(String[] args) {
        checkEquals("0", toDecimalString(0), "zero");
        checkEquals("-2048", toDecimalString(-2048), "negative value");
        checkEquals("2147483647", toDecimalString(Integer.MAX_VALUE), "maximum int");
        System.out.println("Challenge 11 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
