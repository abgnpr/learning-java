/*
 * Challenge 31 — Java Arraylist (Easy)
 *
 * Task: Store rows of varying lengths and answer one-based row/column
 * queries. Return "ERROR!" when a requested element does not exist.
 * Complete: answerQueries(List<List<Integer>> rows, List<Query> queries)
 * Run: java ArrayListQueries.java
 */
import java.util.List;

public class ArrayListQueries {
    record Query(int row, int column) { }

    static List<String> answerQueries(List<List<Integer>> rows, List<Query> queries) {
        // TODO: Resolve each query safely against the ragged rows.
        throw new UnsupportedOperationException("TODO: answer array-list queries");
    }

    public static void main(String[] args) {
        List<List<Integer>> rows = List.of(
            List.of(5, 8),
            List.of(),
            List.of(7, 9, 11)
        );

        checkEquals(List.of("8"), answerQueries(rows, List.of(new Query(1, 2))), "existing element");
        checkEquals(List.of("ERROR!"), answerQueries(rows, List.of(new Query(2, 1))), "empty row");
        checkEquals(
            List.of("7", "ERROR!"),
            answerQueries(rows, List.of(new Query(3, 1), new Query(3, 4))),
            "mixed queries"
        );
        System.out.println("Challenge 31 passed");
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
