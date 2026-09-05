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
        /* 
        if (value < 0) {
            return "negative";
        } else if (value == 0) {
            return "zero";
        } else if (value % 2 == 0) {
            return "positive-even";
        } else {
            return "positive-odd";
        } 
        */

        // preview in java 21
        /* 
        return switch (value) {
            case int v when v < 0 -> "negative";
            case 0 -> "zero";
            case int v when v % 2 == 0 -> "positive-even";
            default -> "positive-odd";
        }; 
        */

        // standard java 21 using switch
        return switch (Integer.signum(value)) {
            case -1 -> "negative";
            case 0 -> "zero";
            default -> value % 2 == 0 ? "positive-even" : "positive-odd";
        };

    }

    public static void main(String[] args) {
        check("negative value", "-8", "negative", classify(-8));
        check("zero", "0", "zero", classify(0));
        check("positive even value", "42", "positive-even", classify(42));
        check("positive odd value", "17", "positive-odd", classify(17));
        report("Challenge 03");
    }



    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */

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
