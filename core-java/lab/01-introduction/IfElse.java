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
        checkEquals("negative", classify(-8), "negative value");
        checkEquals("zero", classify(0), "zero");
        checkEquals("positive-even", classify(42), "positive even value");
        checkEquals("positive-odd", classify(17), "positive odd value");
        if (failures > 0) {
            throw new AssertionError("Challenge 03: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 03 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + show(actual));
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + "\n  expected: " + show(expected)
                + "\n    actual: " + show(actual));
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
}
