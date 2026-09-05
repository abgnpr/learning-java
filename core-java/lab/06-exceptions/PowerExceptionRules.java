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
        check("positive exponent", "calculator.power(3, 5)", 243L, calculator.power(3, 5));
        check("zero exponent", "calculator.power(7, 0)", 1L, calculator.power(7, 0));
        expectCheckedMessage("n and p should not be zero.", () -> calculator.power(0, 0),
                "both zero");
        expectCheckedMessage("n or p should not be negative.", () -> calculator.power(-2, 3),
                "negative base");
        expectCheckedMessage("n or p should not be negative.", () -> calculator.power(2, -3),
                "negative exponent");
        report("Challenge 52");
    }

    @FunctionalInterface
    interface CheckedAction {
        void run() throws Exception;
    }

    private static void expectCheckedMessage(
            String expected, CheckedAction action, String message) {
        try {
            action.run();
            throw new AssertionError(message
                    + ":\n  expected: <java.lang.Exception>"
                    + "\n    actual: <no exception thrown>");
        } catch (Exception exception) {
            if (exception.getClass() != Exception.class) {
                throw new AssertionError(message
                        + ":\n  expected: <java.lang.Exception>"
                        + "\n    actual: <" + exception.getClass().getName() + ">");
            }
            if (!expected.equals(exception.getMessage())) {
                throw new AssertionError(message
                        + ":\n  expected: <" + expected + ">"
                        + "\n    actual: <" + exception.getMessage() + ">");
            }
        }
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
