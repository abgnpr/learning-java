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
        return String.valueOf(value);
    }

    public static void main(String[] args) {
        checkEquals("0", toDecimalString(0), "zero");
        checkEquals("-2147483648", toDecimalString(Integer.MIN_VALUE), "minimum int");
        checkEquals("-2048", toDecimalString(-2048), "negative value");
        checkEquals("2147483647", toDecimalString(Integer.MAX_VALUE), "maximum int");
        if (failures > 0) {
            throw new AssertionError("Challenge 11: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 11 passed.");
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
