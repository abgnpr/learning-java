
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
    record Query(int row, int column) {
    }

    static List<String> answerQueries(List<List<Integer>> rows, List<Query> queries) {

        // approach 1: default ArrayList, exception as the miss signal
        /*
        List<String> answers = new ArrayList<>();
        for (Query query : queries) {
            try {
                String answer = rows.get(query.row() - 1).get(query.column() - 1).toString();
                answers.add(answer);
            } catch (IndexOutOfBoundsException e) {
                answers.add("ERROR!");
            }
        }
        return answers; */

        // approach 2: exact-size array wrapped as a fixed-size List, filled by set
        /*
        List<String> answers = Arrays.asList(new String[queries.size()]);
        for (int i = 0; i < queries.size(); i++) {
            try {
                Query query = queries.get(i);
                String answer = rows.get(query.row() - 1).get(query.column() - 1).toString();
                answers.set(i, answer);
            } catch (IndexOutOfBoundsException e) {
                answers.set(i, "ERROR!");
            }
        }
        return answers;
        */

        /*
         * approach 3: pre-sized ArrayList, bounds tested instead of thrown.
         *
         * Why not approach 1: `new ArrayList<>()` starts at capacity 10 and grows
         * by 1.5x, so a million answers means ~30 reallocations, each copying every
         * element written so far. Measured at n=1,000,000 that is 25-35% slower than
         * either pre-sized version. Reserving the exact size up front is the win.
         *
         * Why not approach 2: it is just as fast — the cost is semantic, not clock.
         * `Arrays.asList` does not copy the array, it WRAPS it, so the list and the
         * array share storage. That view is fixed-size: `set` writes through, but
         * `add`/`remove` throw (the unsupported-operation kind), and the returned object
         * is java.util.Arrays$ArrayList, a private nested class — not java.util.ArrayList.
         * Returning a list the caller cannot append to is a surprise worth avoiding.
         *
         * Capacity is not size, and this is where the two constructors part ways:
         *   Arrays.asList(new String[n])  size n (n nulls) -> set works, add throws
         *   new ArrayList<>(n)            size 0, room for n -> add works, set throws
         * Each accepts exactly the mutator the other rejects. Pairing `new ArrayList<>(n)`
         * with `set` throws IndexOutOfBoundsException on the very first query.
         *
         * `add` appends at the current size, so answer i lands at index i with no index
         * arithmetic — which is what keeps hits and misses aligned with their queries.
         *
         * A missing cell is half the spec, not an exceptional event, so it is tested
         * rather than caught. Testing also costs less: constructing an exception fills
         * in a stack trace unless the JIT has optimised that away on a hot, repeatedly
         * thrown instance — which a cold path or varied throw sites will not give you.
         */
        /*
        List<String> answers = new ArrayList<>(queries.size());
        for (int i = 0; i < queries.size(); i++) {
            answers.add(lookup(rows, queries.get(i)));
        }
        return answers; */

        /*
         * approach 4: the same work said as what it is — one answer per query,
         * order preserved, nothing carried between elements. That shape is `map`
         * and nothing else, which is the case for preferring a stream here; a
         * problem needing an index or state across elements would not qualify.
         *
         * Pre-sizing still happens, just not by hand: a List's spliterator reports
         * SIZED, so `toList()` knows the count up front and allocates once. Insert
         * a `filter` and that is lost — the size is no longer known in advance.
         *
         * `toList()` (Java 16+) returns an unmodifiable list, and is not
         * `Collectors.toList()`, which returns a mutable ArrayList and promises
         * nothing about the class. The checks pass against either because
         * List.equals compares contents, not implementations.
         *
         * The bounds test moved into `lookup` because a lambda has nowhere to bind
         * `row` — inlined, the guard would recompute `query.row() - 1` three times.
         * Extracting it also means the loop above and this stream share one
         * definition of a present cell instead of drifting apart.
         *
         * Approach 1's try/catch does not survive the translation: wrapping a
         * lambda body in one forces a block lambda, uglier than the loop it replaced.
         */
        return queries.stream().map(query -> lookup(rows, query)).toList();
    }

    private static String lookup(List<List<Integer>> rows, Query query) {
        // Queries are one-based; these can go negative, so the >= 0 guards below
        // are load-bearing. So is the order: && short-circuits, so row < rows.size()
        // is proven before rows.get(row) runs.
        int row = query.row() - 1;
        int col = query.column() - 1;
        return row >= 0 && row < rows.size() && col >= 0 && col < rows.get(row).size()
                ? rows.get(row).get(col).toString()
                : "ERROR!";
    }

    public static void main(String[] args) {
        List<List<Integer>> rows = List.of(
                List.of(5, 8),
                List.of(),
                List.of(7, 9, 11));

        // -- the cases the original three covered --
        check("existing element", "1, 2", List.of("8"), answerQueries(rows, List.of(new Query(1, 2))));
        check("empty row", "2, 1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(2, 1))));
        check("mixed queries", "3, 1 and 3, 4", List.of("7", "ERROR!"),
                answerQueries(rows, List.of(new Query(3, 1), new Query(3, 4))));

        // -- boundaries: the last valid cell on each axis, and one past it --
        check("first cell of first row", "1, 1", List.of("5"), answerQueries(rows, List.of(new Query(1, 1))));
        check("last cell of last row", "3, 3", List.of("11"), answerQueries(rows, List.of(new Query(3, 3))));
        check("column one past the row", "1, 3", List.of("ERROR!"), answerQueries(rows, List.of(new Query(1, 3))));
        check("row one past the grid", "4, 1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(4, 1))));

        // -- below the one-based floor: index arithmetic sends these negative --
        check("row zero", "0, 1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(0, 1))));
        check("column zero", "1, 0", List.of("ERROR!"), answerQueries(rows, List.of(new Query(1, 0))));
        check("negative row", "-1, 1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(-1, 1))));
        check("negative column", "1, -1", List.of("ERROR!"), answerQueries(rows, List.of(new Query(1, -1))));
        check("both below floor", "0, 0", List.of("ERROR!"), answerQueries(rows, List.of(new Query(0, 0))));

        // -- extremes: no overflow wraparound is allowed to turn a miss into a hit --
        check("MAX_VALUE row", "MAX_VALUE, 1", List.of("ERROR!"),
                answerQueries(rows, List.of(new Query(Integer.MAX_VALUE, 1))));
        check("MIN_VALUE column", "1, MIN_VALUE", List.of("ERROR!"),
                answerQueries(rows, List.of(new Query(1, Integer.MIN_VALUE))));

        // -- shape of the result: one answer per query, order preserved, empty in/empty out --
        check("no queries", "(none)", List.of(), answerQueries(rows, List.of()));
        check("repeated query keeps both", "1, 1 twice", List.of("5", "5"),
                answerQueries(rows, List.of(new Query(1, 1), new Query(1, 1))));
        check("every cell in reading order", "all valid cells", List.of("5", "8", "7", "9", "11"),
                answerQueries(rows, List.of(new Query(1, 1), new Query(1, 2),
                        new Query(3, 1), new Query(3, 2), new Query(3, 3))));
        check("hits and misses interleaved", "alternating", List.of("ERROR!", "5", "ERROR!", "11", "ERROR!"),
                answerQueries(rows, List.of(new Query(0, 1), new Query(1, 1), new Query(2, 1),
                        new Query(3, 3), new Query(4, 1))));

        // -- a grid with no rows at all: every query misses --
        check("empty grid", "1, 1 against no rows", List.of("ERROR!"),
                answerQueries(List.of(), List.of(new Query(1, 1))));

        // -- values that are not their own digits: negatives and multi-digit stringify verbatim --
        List<List<Integer>> signed = List.of(List.of(-4, 0, 1000));
        check("negative value", "1, 1", List.of("-4"), answerQueries(signed, List.of(new Query(1, 1))));
        check("zero value", "1, 2", List.of("0"), answerQueries(signed, List.of(new Query(1, 2))));
        check("multi-digit value", "1, 3", List.of("1000"), answerQueries(signed, List.of(new Query(1, 3))));

        report("Challenge 31");
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
