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

        check("filter, normalize, and sort", "users", List.of("ADA", "GRACE"), activeUserNames(users));
        check("empty input", "List.of()", List.of(), activeUserNames(List.of()));
        checkThat("result must be unmodifiable", "users", isUnmodifiable(activeUserNames(users)));
        report("Challenge 65");
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

    /** True when the list rejects mutation, as an unmodifiable view must. */
    private static boolean isUnmodifiable(java.util.List<?> list) {
        try {
            list.clear();
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }
}
