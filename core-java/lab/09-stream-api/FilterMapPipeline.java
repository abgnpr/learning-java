/*
 * Challenge 65: Filter and Map Pipeline (Easy)
 *
 * Task: Keep active users whose name is non-null and non-empty after trim,
 * normalize their names to uppercase with Locale.ROOT, sort the normalized
 * names, and return an unmodifiable list. Ignore null users.
 *
 * Complete: activeUserNames(List<User> users).
 * Required focus: filter, map, sorted, collect, and collectingAndThen.
 * Java baseline: Java 8.
 * Run: javac FilterMapPipeline.java && java FilterMapPipeline
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FilterMapPipeline {
    static final class User {
        private final String name;
        private final boolean active;

        User(String name, boolean active) {
            this.name = name;
            this.active = active;
        }

        String getName() {
            return name;
        }

        boolean isActive() {
            return active;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof User)) {
                return false;
            }

            User that = (User) other;
            return active == that.active && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, active);
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', active=" + active + "}";
        }
    }

    private FilterMapPipeline() {
    }

    static List<String> activeUserNames(List<User> users) {
        return users.stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getName() != null)
                .filter(User::isActive)
                .map(User::getName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> name.toUpperCase(Locale.ROOT))
                .sorted()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList));
    }

    public static void main(String[] args) {
        List<User> users = list(
                new User("  grace ", true),
                new User("Ada", true),
                new User("Linus", false),
                new User("   ", true));

        check("filter, normalize, and sort", "users",
                list("ADA", "GRACE"), activeUserNames(users));
        check("empty input", "empty list",
                Collections.emptyList(),
                activeUserNames(Collections.<User>emptyList()));
        checkThat("result is unmodifiable", "users",
                isUnmodifiable(activeUserNames(users)));

        check("all inactive", "Ada false, Linus false",
                Collections.emptyList(),
                activeUserNames(list(
                        new User("Ada", false),
                        new User("Linus", false))));

        check("empty after trim", "spaces and control whitespace",
                Collections.emptyList(),
                activeUserNames(list(
                        new User("", true),
                        new User(" \t\n ", true))));

        // String.trim() is the Java 8 operation. It does not remove U+00A0.
        check("non-breaking space survives Java 8 trim", "U+00A0",
                list("\u00A0"),
                activeUserNames(list(new User("\u00A0", true))));

        check("normalization keeps interior spaces", "  ada  lovelace  ",
                list("ADA  LOVELACE"),
                activeUserNames(list(new User("  ada  lovelace  ", true))));

        check("uppercase uses Locale.ROOT", "i",
                list("I"),
                activeUserNames(list(new User("i", true))));

        check("sorting uses normalized values", "ada, Zoe",
                list("ADA", "ZOE"),
                activeUserNames(list(
                        new User("ada", true),
                        new User("Zoe", true))));

        check("duplicates remain", " ada , ADA",
                list("ADA", "ADA"),
                activeUserNames(list(
                        new User(" ada ", true),
                        new User("ADA", true))));

        check("null user and name are ignored", "null entries",
                list("ADA"),
                activeUserNames(Arrays.asList(
                        null,
                        new User(null, true),
                        new User("Ada", true))));

        List<User> source = list(
                new User(" bob ", true),
                new User("Amy", false));
        List<User> before = list(
                new User(" bob ", true),
                new User("Amy", false));

        activeUserNames(source);
        check("source is not mutated", "source", before, source);

        report("Challenge 65");
    }

    // ---- test harness (not part of the exercise) ----

    @SafeVarargs
    private static <T> List<T> list(T... values) {
        return Arrays.asList(values);
    }

    private static int passes;
    private static int failures;

    private static void check(
            String label,
            Object input,
            Object expected,
            Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);

        if (ok) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void checkThat(
            String label,
            Object input,
            boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: condition holds");
        System.out.println("      actual:   "
                + (condition ? "holds" : "does not hold"));
    }

    private static boolean isUnmodifiable(List<?> list) {
        try {
            list.clear();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
        }
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, "
                + failures + " failed.");

        if (failures > 0) {
            throw new AssertionError(
                    challenge + ": " + failures + " check(s) failed.");
        }

        System.out.println(challenge + " passed.");
    }
}
