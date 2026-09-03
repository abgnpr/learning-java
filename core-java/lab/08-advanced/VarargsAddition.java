/*
 * Challenge 53: Java Varargs - Simple Addition (Easy)
 *
 * Task: Accept one or more integers through one varargs method and format both
 * the addends and their sum as an equation.
 * Complete: add(int...).
 * Run: java VarargsAddition.java
 */
public class VarargsAddition {
    static String add(int... numbers) {
        // TODO: Build an expression such as 1+2+3=6 for all supplied numbers.
        throw new UnsupportedOperationException("TODO: implement add");
    }

    public static void main(String[] args) {
        checkEquals("7=7", add(7), "one addend");
        checkEquals("1+2=3", add(1, 2), "two addends");
        checkEquals("1+2+3+4+5=15", add(1, 2, 3, 4, 5), "five addends");
        checkEquals("10+-4+0=6", add(10, -4, 0), "mixed-sign addends");
        if (failures > 0) {
            throw new AssertionError("Challenge 53: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 53 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(String expected, String actual, String message) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + message + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
