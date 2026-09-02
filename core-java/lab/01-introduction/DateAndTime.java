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
        System.out.println("Challenge 12 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
