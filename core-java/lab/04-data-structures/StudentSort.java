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

        checkEquals(
            List.of(louis, lorry, sam, samantha, tina),
            sortStudents(List.of(tina, louis, sam, samantha, lorry)),
            "all ordering levels"
        );
        checkEquals(
            List.of(new Student(3, "Amy", 4.0), new Student(7, "Amy", 4.0)),
            sortStudents(List.of(new Student(7, "Amy", 4.0), new Student(3, "Amy", 4.0))),
            "ID tie-breaker"
        );
        checkEquals(
            List.of(new Student(1, "Only", 2.5)),
            sortStudents(List.of(new Student(1, "Only", 2.5))),
            "single student"
        );
        System.out.println("Challenge 39 passed");
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
