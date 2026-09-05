/*
 * Challenge 39 — Java Sort (Easy)
 *
 * Task: Sort students by decreasing CGPA, then increasing first name, then
 * increasing ID when both earlier fields tie.
 * Complete: sortStudents(List<Student> students)
 * Run: java StudentSort.java
 */
import java.util.List;

public class StudentSort {
    record Student(int id, String firstName, double cgpa) { }

    static List<Student> sortStudents(List<Student> students) {
        // TODO: Build the chained ordering and return the sorted students.
        throw new UnsupportedOperationException("TODO: sort students");
    }

    public static void main(String[] args) {
        Student tina = new Student(33, "Tina", 3.68);
        Student louis = new Student(85, "Louis", 3.85);
        Student sam = new Student(56, "Sam", 3.75);
        Student samantha = new Student(19, "Samantha", 3.75);
        Student lorry = new Student(22, "Lorry", 3.76);

        check("all ordering levels", "List.of(tina, louis, sam, samantha, lorry)", List.of(louis, lorry, sam, samantha, tina), sortStudents(List.of(tina, louis, sam, samantha, lorry)));
        check("ID tie-breaker", "7, \"Amy\", 4.0", List.of(new Student(3, "Amy", 4.0), new Student(7, "Amy", 4.0)), sortStudents(List.of(new Student(7, "Amy", 4.0), new Student(3, "Amy", 4.0))));
        check("single student", "1, \"Only\", 2.5", List.of(new Student(1, "Only", 2.5)), sortStudents(List.of(new Student(1, "Only", 2.5))));
        report("Challenge 39");
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
