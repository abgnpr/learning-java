/*
 * Challenge 65: Filter and Map Pipeline
 * Difficulty: Easy
 *
 * Task: From the supplied users, keep active users whose trimmed name is not
 * blank, normalize their names to uppercase with Locale.ROOT, sort them in
 * natural order, and return an unmodifiable list.
 * Complete: static List<String> activeUserNames(List<User> users)
 * Required focus: filter, map, sorted, and Stream.toList().
 * Run: java FilterMapPipeline.java
 */
import java.util.List;
import java.util.Locale;

public final class FilterMapPipeline {
    record User(String name, boolean active) {
    }

    private FilterMapPipeline() {
    }

    static List<String> activeUserNames(List<User> users) {
        throw new UnsupportedOperationException("TODO: build the filter/map pipeline");
    }

    public static void main(String[] args) {
        List<User> users = List.of(
                new User("  grace ", true),
                new User("Ada", true),
                new User("Linus", false),
                new User("   ", true));

        checkEquals(List.of("ADA", "GRACE"), activeUserNames(users), "filter, normalize, and sort");
        checkEquals(List.of(), activeUserNames(List.of()), "empty input");
        checkUnmodifiable(activeUserNames(users), "result must be unmodifiable");
        if (failures > 0) {
            throw new AssertionError("Challenge 65: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 65 passed.");
    }

    private static void checkUnmodifiable(List<String> actual, String label) {
        try {
            actual.add("MUTATION");
            throw new AssertionError(label + ": expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected: Stream.toList() returns an unmodifiable list.
        }
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
