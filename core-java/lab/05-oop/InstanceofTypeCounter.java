/*
 * Challenge 49: Java Instanceof keyword (Easy)
 *
 * Task: Count students, rockstars, and hackers in a mixed list of people by
 * testing each object's runtime type.
 * Complete: countTypes(List<Person>).
 * Run: java InstanceofTypeCounter.java
 */
import java.util.List;

public class InstanceofTypeCounter {
    static class Person {
    }

    static final class Student extends Person {
    }

    static final class Rockstar extends Person {
    }

    static final class Hacker extends Person {
    }

    record Counts(int students, int rockstars, int hackers) {
    }

    static Counts countTypes(List<Person> people) {
        // TODO: Use instanceof to count each supported subtype.
        throw new UnsupportedOperationException("TODO: implement countTypes");
    }

    public static void main(String[] args) {
        checkEquals(new Counts(2, 1, 1),
                countTypes(List.of(new Student(), new Rockstar(), new Student(), new Hacker())),
                "mixed group");
        checkEquals(new Counts(0, 0, 0), countTypes(List.of()), "empty group");
        checkEquals(new Counts(0, 2, 1),
                countTypes(List.of(new Rockstar(), new Hacker(), new Rockstar())),
                "group without students");
        checkEquals(new Counts(1, 0, 0),
                countTypes(List.of(new Person(), new Student())),
                "plain people are not counted as a subtype");
        System.out.println("Challenge 49 passed!");
    }

    private static void checkEquals(Counts expected, Counts actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
