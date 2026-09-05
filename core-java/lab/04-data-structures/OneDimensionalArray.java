
/*
 * Challenge 28 — Java 1D Array (Easy)
 *
 * Task: Store a sequence of integers in a primitive one-dimensional array,
 * preserving both its length and element order.
 * Complete: copyIntoArray(List<Integer> values)
 * Run: java OneDimensionalArray.java
 */
import java.util.List;

public class OneDimensionalArray {
    static int[] copyIntoArray(List<Integer> values) {

        // approach 1 — pre-Java 8
        // Size the array up front: the length is known, so there is no growth
        // step and no boxing beyond what the list already holds. get(i) reads
        // an Integer and the assignment to int[] unboxes it — the same
        // intValue() call approach 2 makes explicit, just implicit here.
        // Indexed access assumes RandomAccess; on a LinkedList this degrades
        // to O(n^2), where a for-each stays O(n).
        /*
        int[] copy = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            copy[i] = values.get(i);
        }
        return copy;
        */

        // approach 2 — Java 8+
        // mapToInt crosses from Stream<Integer> to IntStream, so toArray()
        // returns int[] rather than Integer[]; without it there is no
        // primitive array at the end. Integer::intValue names the unboxing
        // instead of leaving it to an implicit conversion, and both forms
        // throw NullPointerException on a null element.
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {
        checkArray(new int[] {}, copyIntoArray(List.of()), "empty input", "List.of()");
        checkArray(new int[] { 7 }, copyIntoArray(List.of(7)), "one value", "List.of(7)");
        checkArray(new int[] { 1, -2, 3, 0 }, copyIntoArray(List.of(1, -2, 3, 0)), "ordered values", "List.of(1, -2, 3, 0)");

        // Order is the contract, so an already-sorted case cannot distinguish a
        // faithful copy from one that sorts on the way through.
        checkArray(new int[] { 5, 1, 4, 1, 5 }, copyIntoArray(List.of(5, 1, 4, 1, 5)), "unsorted order preserved", "List.of(5, 1, 4, 1, 5)");
        checkArray(new int[] { 9, 9, 9 }, copyIntoArray(List.of(9, 9, 9)), "duplicates kept, not deduplicated", "List.of(9, 9, 9)");
        checkArray(new int[] { 3, 2, 1 }, copyIntoArray(List.of(3, 2, 1)), "descending order preserved", "List.of(3, 2, 1)");

        // Unboxing edges: the sentinel values and the caching boundary at 127.
        checkArray(new int[] { 0 }, copyIntoArray(List.of(0)), "zero survives as a value", "List.of(0)");
        checkArray(new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE },
                copyIntoArray(List.of(Integer.MIN_VALUE, Integer.MAX_VALUE)),
                "int bounds unbox without overflow", "List.of(Integer.MIN_VALUE, Integer.MAX_VALUE)");
        checkArray(new int[] { 127, 128, -128, -129 }, copyIntoArray(List.of(127, 128, -128, -129)),
                "values either side of the Integer cache", "List.of(127, 128, -128, -129)");

        // A fresh array each call: mutating the result must not affect a later one.
        List<Integer> shared = List.of(1, 2, 3);
        int[] first = copyIntoArray(shared);
        first[0] = 99;
        checkArray(new int[] { 1, 2, 3 }, copyIntoArray(shared), "result is independent of an earlier one", "List.of(1, 2, 3) copied twice");

        // The source list is an input, not a workspace.
        List<Integer> source = new java.util.ArrayList<>(List.of(4, 5, 6));
        copyIntoArray(source);
        check("source list left unmodified", "new ArrayList<>(List.of(4, 5, 6))", List.of(4, 5, 6), source);

        // Length is carried over independently of content, past any small-input special case.
        List<Integer> long100 = java.util.stream.IntStream.range(0, 100).boxed().toList();
        int[] copied100 = copyIntoArray(long100);
        checkThat("100 elements keep length and order",
                "IntStream.range(0, 100)",
                copied100.length == 100 && java.util.Arrays.equals(copied100, java.util.stream.IntStream.range(0, 100).toArray()));

        report("Challenge 28");
    }

    static void checkArray(int[] expected, int[] actual, String label, Object input) {
        check(label, input, expected, actual);
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
