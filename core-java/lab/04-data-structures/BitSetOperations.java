/*
 * Challenge 41 — Java BitSet (Easy)
 *
 * Task: Maintain two zero-filled bit sets of the supplied positive size and
 * return both cardinalities after every command. For AND, OR, and XOR, left
 * identifies the target set and right identifies the other set (1 or 2). For
 * FLIP and SET, left identifies the set and right is a valid zero-based bit
 * index.
 * Complete: execute(int size, List<Operation> operations)
 * Run: java BitSetOperations.java
 */
import java.util.List;

public class BitSetOperations {
    record Operation(String command, int left, int right) { }
    record Cardinality(int first, int second) { }

    static List<Cardinality> execute(int size, List<Operation> operations) {
        // TODO: Apply commands to two BitSet instances and capture each count.
        throw new UnsupportedOperationException("TODO: execute bit-set commands");
    }

    public static void main(String[] args) {
        checkEquals(
            List.of(
                new Cardinality(1, 0),
                new Cardinality(1, 1),
                new Cardinality(2, 1),
                new Cardinality(2, 1)
            ),
            execute(5, List.of(
                op("SET", 1, 0),
                op("SET", 2, 1),
                op("OR", 1, 2),
                op("XOR", 2, 1)
            )),
            "set and binary commands"
        );
        checkEquals(
            List.of(new Cardinality(1, 0), new Cardinality(1, 1), new Cardinality(0, 1)),
            execute(4, List.of(op("SET", 1, 2), op("FLIP", 2, 1), op("AND", 1, 2))),
            "flip and intersection"
        );
        checkEquals(
            List.of(new Cardinality(1, 0), new Cardinality(0, 0), new Cardinality(0, 0)),
            execute(3, List.of(op("FLIP", 1, 0), op("FLIP", 1, 0), op("OR", 2, 1))),
            "flip twice"
        );
        if (failures > 0) {
            throw new AssertionError("Challenge 41: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 41 passed.");
    }

    static Operation op(String command, int left, int right) {
        return new Operation(command, left, right);
    }

    private static int failures = 0;

    static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
