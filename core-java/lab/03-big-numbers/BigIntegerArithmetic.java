/*
 * Challenge 27 — Java BigInteger (Easy)
 *
 * Task: For two non-negative integers too large for primitive types, compute
 * their sum and product without losing precision.
 * Complete: calculate(BigInteger left, BigInteger right)
 * Run: java BigIntegerArithmetic.java
 */
import java.math.BigInteger;

public class BigIntegerArithmetic {
    record ArithmeticResult(BigInteger sum, BigInteger product) { }

    static ArithmeticResult calculate(BigInteger left, BigInteger right) {
        // TODO: Perform both arbitrary-precision operations.
        throw new UnsupportedOperationException("TODO: add and multiply");
    }

    public static void main(String[] args) {
        checkEquals(
            result("42", "360"),
            calculate(new BigInteger("12"), new BigInteger("30")),
            "ordinary values"
        );
        checkEquals(
            result("12345678901234567900", "123456789012345678900"),
            calculate(new BigInteger("12345678901234567890"), BigInteger.TEN),
            "beyond long range"
        );
        checkEquals(
            result("999999999999999999999999", "0"),
            calculate(BigInteger.ZERO, new BigInteger("999999999999999999999999")),
            "zero operand"
        );
        System.out.println("Challenge 27 passed");
    }

    static ArithmeticResult result(String sum, String product) {
        return new ArithmeticResult(new BigInteger(sum), new BigInteger(product));
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
