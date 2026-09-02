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

        checkEquals(Optional.of("ana@example.com"), youngestAdultEmail(people),
                "skip minors and missing emails");
        checkEquals(Optional.of("amy@example.com"), youngestAdultEmail(List.of(
                new Person("Zed", 18, Optional.of("zed@example.com")),
                new Person("Amy", 18, Optional.of("AMY@EXAMPLE.COM")))), "name breaks age tie");
        checkEquals(Optional.empty(), youngestAdultEmail(List.of(
                new Person("Minor", 12, Optional.of("minor@example.com")),
                new Person("Adult", 30, Optional.empty()))), "no eligible email");
        System.out.println("Challenge 72 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
