/*
 * Challenge 06: Java Loops I
 * Difficulty: Easy
 *
 * Task: Build a multiplication table from multiplier 1 through the supplied
 * limit, using lines shaped like "value x multiplier = product".
 * Complete: static String multiplicationTable(int value, int limit)
 * Run: java LoopsOne.java
 */
public final class LoopsOne {
    private LoopsOne() {
    }

    static String multiplicationTable(int value, int limit) {
        throw new UnsupportedOperationException("TODO: generate the table with a loop");
    }

    public static void main(String[] args) {
        checkEquals("5 x 1 = 5\n5 x 2 = 10\n5 x 3 = 15",
                multiplicationTable(5, 3), "three rows");
        checkEquals("-2 x 1 = -2\n-2 x 2 = -4", multiplicationTable(-2, 2), "negative value");
        checkEquals("", multiplicationTable(9, 0), "zero rows");
        System.out.println("Challenge 06 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
