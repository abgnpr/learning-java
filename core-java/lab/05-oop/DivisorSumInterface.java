/*
 * Challenge 46: Java Interface (Easy)
 *
 * Task: Implement an interface operation that returns the sum of every
 * positive divisor of a positive integer.
 * Complete: Calculator.divisorSum(int).
 * Run: java DivisorSumInterface.java
 */
public class DivisorSumInterface {
    interface AdvancedArithmetic {
        int divisorSum(int number);
    }

    static final class Calculator implements AdvancedArithmetic {
        @Override
        public int divisorSum(int number) {
            // TODO: Return the sum of all positive divisors of number.
            throw new UnsupportedOperationException("TODO: implement divisorSum");
        }
    }

    public static void main(String[] args) {
        AdvancedArithmetic calculator = new Calculator();
        checkEquals(1, calculator.divisorSum(1), "divisors of 1");
        checkEquals(12, calculator.divisorSum(6), "divisors of 6");
        checkEquals(56, calculator.divisorSum(28), "divisors of 28");
        System.out.println("Challenge 46 passed!");
    }

    private static void checkEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
