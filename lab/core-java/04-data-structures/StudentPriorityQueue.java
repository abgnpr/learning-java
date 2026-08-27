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
        checkEquals(
            List.of(
                new Student("John", 3.75, 50),
                new Student("Shafaet", 3.7, 35)
            ),
            remainingStudents(List.of(
                Event.enter("John", 3.75, 50),
                Event.enter("Mark", 3.8, 24),
                Event.enter("Shafaet", 3.7, 35),
                Event.served()
            )),
            "serve highest CGPA"
        );
        checkEquals(
            List.of(
                new Student("Amy", 4.0, 7),
                new Student("Zoe", 4.0, 2)
            ),
            remainingStudents(List.of(
                Event.enter("Zoe", 4.0, 2),
                Event.enter("Amy", 4.0, 7),
                Event.enter("Amy", 4.0, 3),
                Event.served()
            )),
            "name and ID tie-breakers"
        );
        checkEquals(
            List.of(),
            remainingStudents(List.of(Event.enter("Eve", 3.2, 1), Event.served())),
            "empty after service"
        );
        System.out.println("Challenge 42 passed");
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
