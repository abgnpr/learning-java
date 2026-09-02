/*
 * Challenge 51: Java Exception Handling (Try-catch) (Easy)
 *
 * Task: Parse two integer tokens and return their integer quotient. Return
 * ArithmeticException.toString() for division by zero, or the fully qualified
 * InputMismatchException class name when either token is not an integer.
 * Complete: evaluate(String).
 * Run: java TryCatchDivision.java
 */
import java.util.InputMismatchException;
import java.util.Scanner;

public class TryCatchDivision {
    static String evaluate(String input) {
        // TODO: Use Scanner and try-catch for InputMismatchException and ArithmeticException.
        throw new UnsupportedOperationException("TODO: implement evaluate");
    }

    public static void main(String[] args) {
        checkEquals("3", evaluate("10 3"), "integer division");
        checkEquals("java.lang.ArithmeticException: / by zero", evaluate("10 0"),
                "division by zero");
        checkEquals(InputMismatchException.class.getName(), evaluate("ten 3"),
                "non-integer input");
        System.out.println("Challenge 51 passed!");
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
