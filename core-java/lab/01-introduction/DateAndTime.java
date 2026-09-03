/*
 * Challenge 12: Java Date and Time
 * Difficulty: Easy
 *
 * Task: Return the English weekday name in uppercase for a valid ISO date.
 * Complete: static String dayOfWeek(int year, int month, int day)
 * Run: java DateAndTime.java
 */
public final class DateAndTime {
    private DateAndTime() {
    }

    static String dayOfWeek(int year, int month, int day) {
        throw new UnsupportedOperationException("TODO: calculate the weekday with java.time");
    }

    public static void main(String[] args) {
        checkEquals("SATURDAY", dayOfWeek(2000, 1, 1), "millennium date");
        checkEquals("THURSDAY", dayOfWeek(2024, 2, 29), "leap day");
        checkEquals("WEDNESDAY", dayOfWeek(2030, 12, 25), "future date");
        if (failures > 0) {
            throw new AssertionError("Challenge 12: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 12 passed.");
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
