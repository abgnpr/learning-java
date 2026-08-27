/*
 * Challenge 33 — Java List (Easy)
 *
 * Task: Apply zero-based Insert and Delete commands to an integer list and
 * return the list after every command has been processed in order.
 * Complete: applyOperations(List<Integer> initial, List<Operation> operations)
 * Run: java ListOperations.java
 */
import java.util.List;

public class ListOperations {
    record Operation(String command, int index, Integer value) {
        static Operation insert(int index, int value) {
            return new Operation("Insert", index, value);
        }

        static Operation delete(int index) {
            return new Operation("Delete", index, null);
        }
    }

    static List<Integer> applyOperations(List<Integer> initial, List<Operation> operations) {
        // TODO: Use a mutable List and honor command order.
        throw new UnsupportedOperationException("TODO: apply list operations");
    }

    public static void main(String[] args) {
        checkEquals(
            List.of(0, 1, 78, 12, 23),
            applyOperations(
                List.of(12, 0, 1, 78, 12),
                List.of(Operation.insert(5, 23), Operation.delete(0))
            ),
            "insert then delete"
        );
        checkEquals(
            List.of(9, 1),
            applyOperations(List.of(1), List.of(Operation.insert(0, 9))),
            "insert at front"
        );
        checkEquals(
            List.of(1, 3),
            applyOperations(List.of(1, 2, 3), List.of(Operation.delete(1))),
            "delete from middle"
        );
        System.out.println("Challenge 33 passed");
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
