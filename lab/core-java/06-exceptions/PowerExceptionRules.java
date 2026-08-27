/*
 * Challenge 52: Java Exception Handling (Easy)
 *
 * Task: Raise n to p while rejecting negative inputs and the special case in
 * which both arguments are zero with precise messages on plain checked
 * Exception instances.
 * Complete: Calculator.power(int, int).
 * Run: java PowerExceptionRules.java
 */
public class PowerExceptionRules {
    static final class Calculator {
        public long power(int n, int p) throws Exception {
            // TODO: Validate the arguments, then calculate n raised to p.
            throw new UnsupportedOperationException("TODO: implement power");
        }
    }

    public static void main(String[] args) throws Exception {
        Calculator calculator = new Calculator();
        checkEquals(243L, calculator.power(3, 5), "positive exponent");
        checkEquals(1L, calculator.power(7, 0), "zero exponent");
        expectCheckedMessage("n and p should not be zero.", () -> calculator.power(0, 0),
                "both zero");
        expectCheckedMessage("n or p should not be negative.", () -> calculator.power(-2, 3),
                "negative base");
        expectCheckedMessage("n or p should not be negative.", () -> calculator.power(2, -3),
                "negative exponent");
        System.out.println("Challenge 52 passed!");
    }

    @FunctionalInterface
    interface CheckedAction {
        void run() throws Exception;
    }

    private static void expectCheckedMessage(
            String expected, CheckedAction action, String message) {
        try {
            action.run();
            throw new AssertionError(message + ": expected an exception");
        } catch (Exception exception) {
            if (exception.getClass() != Exception.class) {
                throw new AssertionError(message + ": expected checked java.lang.Exception, got "
                        + exception.getClass().getName());
            }
            if (!expected.equals(exception.getMessage())) {
                throw new AssertionError(message + ": expected message=" + expected
                        + ", actual=" + exception.getMessage());
            }
        }
    }

    private static void checkEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
