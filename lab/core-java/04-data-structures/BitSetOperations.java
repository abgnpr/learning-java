/*
 * Challenge 41 — Java BitSet (Easy)
 *
 * Task: Maintain two zero-filled fixed-size bit sets. Apply AND, OR, XOR,
 * FLIP, and SET commands, reporting both cardinalities after every command.
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
        System.out.println("Challenge 41 passed");
    }

    static Operation op(String command, int left, int right) {
        return new Operation(command, left, right);
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
