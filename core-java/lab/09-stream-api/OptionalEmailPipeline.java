/*
 * Challenge 72: Optional in a Stream Pipeline
 * Difficulty: Medium
 *
 * Task: Find the youngest adult who has an email address. Break age ties by
 * name, ignore adults whose email is empty, lowercase the chosen email with
 * Locale.ROOT, and return Optional.empty() if no candidate exists.
 * Complete: static Optional<String> youngestAdultEmail(List<Person> people)
 * Required focus: sorting, Optional.stream, flatMap, and findFirst.
 * Run: java OptionalEmailPipeline.java
 */
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class OptionalEmailPipeline {
    record Person(String name, int age, Optional<String> email) {
    }

    private OptionalEmailPipeline() {
    }

    static Optional<String> youngestAdultEmail(List<Person> people) {
        throw new UnsupportedOperationException("TODO: compose Stream and Optional operations");
    }

    public static void main(String[] args) {
        List<Person> people = List.of(
                new Person("Zoe", 17, Optional.of("ZOE@EXAMPLE.COM")),
                new Person("Ben", 18, Optional.empty()),
                new Person("Ana", 20, Optional.of("ANA@EXAMPLE.COM")),
                new Person("Cal", 20, Optional.of("CAL@EXAMPLE.COM")));

        check("skip minors and missing emails", "people", Optional.of("ana@example.com"), youngestAdultEmail(people));
        check("name breaks age tie", "\"Zed\", 18, Optional.of(\"zed@example.com\")", Optional.of("amy@example.com"), youngestAdultEmail(List.of(
                new Person("Zed", 18, Optional.of("zed@example.com")),
                new Person("Amy", 18, Optional.of("AMY@EXAMPLE.COM")))));
        check("no eligible email", "\"Minor\", 12, Optional.of(\"minor@example.com\")", Optional.empty(), youngestAdultEmail(List.of(
                new Person("Minor", 12, Optional.of("minor@example.com")),
                new Person("Adult", 30, Optional.empty()))));
        report("Challenge 72");
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
