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
        // TODO: Allocate and populate the primitive array.
        throw new UnsupportedOperationException("TODO: build the array");
    }

    public static void main(String[] args) {
        checkArray(new int[] { }, copyIntoArray(List.of()), "empty input");
        checkArray(new int[] { 7 }, copyIntoArray(List.of(7)), "one value");
        checkArray(new int[] { 1, -2, 3, 0 }, copyIntoArray(List.of(1, -2, 3, 0)), "ordered values");
        report("Challenge 28");
    }

    static void checkArray(int[] expected, int[] actual, String label) {
        if (!java.util.Arrays.equals(expected, actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + java.util.Arrays.toString(expected) + ">"
                    + "\n    actual: <" + java.util.Arrays.toString(actual) + ">");
        }
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
