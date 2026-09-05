
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * Challenge 06: Java Loops I
 * Difficulty: Easy
 *
 * Task: Build a multiplication table from multiplier 1 through the supplied
 * limit, using lines shaped like "value x multiplier = product".
 * Complete: static String multiplicationTable(int value, int limit)
 * Run: java LoopsOne.java
 */
public final class LoopsOne {
    private LoopsOne() {
    }

    static String multiplicationTable(int value, int limit) {
        // mapToObj, not map: IntStream.map is int -> int, whereas formatting
        // produces String values and crosses into Stream<T> for joining.
        //     .map(i -> String.format(...))   // int -> String: won't compile
        // rangeClosed includes limit; for limit 0 it is empty, and joining()
        // naturally returns "" without a branch.
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> String.format("%d x %d = %d", value, i, value * i))
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        check("three rows", "5, 3", "5 x 1 = 5\n5 x 2 = 10\n5 x 3 = 15", multiplicationTable(5, 3));
        check("negative value", "-2, 2", "-2 x 1 = -2\n-2 x 2 = -4", multiplicationTable(-2, 2));
        check("zero rows", "9, 0", "", multiplicationTable(9, 0));
        report("Challenge 06");
    }



    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */

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
