/*
 * Challenge 42 — Java Priority Queue (Medium)
 *
 * Task: Process ENTER and SERVED events. Service the student with highest
 * CGPA, then alphabetically earliest name, then lowest ID; return the students
 * still waiting in that same priority order.
 * Complete: remainingStudents(List<Event> events)
 * Run: java StudentPriorityQueue.java
 */
import java.util.List;

public class StudentPriorityQueue {
    record Student(String name, double cgpa, int id) { }

    record Event(String command, String name, double cgpa, int id) {
        static Event enter(String name, double cgpa, int id) {
            return new Event("ENTER", name, cgpa, id);
        }

        static Event served() {
            return new Event("SERVED", null, 0.0, 0);
        }
    }

    static List<Student> remainingStudents(List<Event> events) {
        // TODO: Model the waiting line with the required priority ordering.
        throw new UnsupportedOperationException("TODO: process student events");
    }

    public static void main(String[] args) {
        check("serve highest CGPA", "List.of( Event.enter(\"John\", 3.75, 50), Event.enter(\"Mark\", 3.8, 24), Event.enter(\"Shafaet\", 3.7, 35), Event.served() )", List.of(
                new Student("John", 3.75, 50),
                new Student("Shafaet", 3.7, 35)
            ), remainingStudents(List.of(
                Event.enter("John", 3.75, 50),
                Event.enter("Mark", 3.8, 24),
                Event.enter("Shafaet", 3.7, 35),
                Event.served()
            )));
        check("name and ID tie-breakers", "List.of( Event.enter(\"Zoe\", 4.0, 2), Event.enter(\"Amy\", 4.0, 7), Event.enter(\"Amy\", 4.0, 3), Event.served() )", List.of(
                new Student("Amy", 4.0, 7),
                new Student("Zoe", 4.0, 2)
            ), remainingStudents(List.of(
                Event.enter("Zoe", 4.0, 2),
                Event.enter("Amy", 4.0, 7),
                Event.enter("Amy", 4.0, 3),
                Event.served()
            )));
        check("empty after service", "List.of(Event.enter(\"Eve\", 3.2, 1), Event.served())", List.of(), remainingStudents(List.of(Event.enter("Eve", 3.2, 1), Event.served())));
        report("Challenge 42");
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
