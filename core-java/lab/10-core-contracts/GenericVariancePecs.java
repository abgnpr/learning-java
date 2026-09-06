/*
 * Challenge 83 — Generic Variance and PECS (Hard)
 *
 * Task: Implement reusable generic algorithms without casts: copy values from
 * a producer to a consumer, sum any Number subtype, and find a maximum whose
 * Comparable may be declared on a supertype.
 * Complete: copy(...), sum(...), max(...)
 * Run: java GenericVariancePecs.java
 */
import java.util.ArrayList;
import java.util.List;

public class GenericVariancePecs {
    static <T> void copy(List<? extends T> source, List<? super T> destination) {
        // TODO: Copy every source element into the destination.
        throw new UnsupportedOperationException("TODO: apply PECS");
    }

    static double sum(List<? extends Number> values) {
        // TODO: Sum values without narrowing the accepted Number subtype.
        throw new UnsupportedOperationException("TODO: sum bounded values");
    }

    static <T extends Comparable<? super T>> T max(List<? extends T> values) {
        // TODO: Return the greatest non-empty-list element using its natural order.
        throw new UnsupportedOperationException("TODO: find the generic maximum");
    }

    public static void main(String[] args) {
        List<Number> numbers = new ArrayList<>(List.of(0L));
        copy(List.of(1, 2, 3), numbers);
        check("Integer producer to Number consumer", "copy [1,2,3]",
                List.of(0L, 1, 2, 3), numbers);
        check("sum mixed Number values", "[1, 2.5, 3L]", 6.5,
                sum(List.of(1, 2.5, 3L)));
        check("maximum integers", "[3, 1, 7, 2]", 7,
                max(List.of(3, 1, 7, 2)));
        check("maximum strings", "[beta, alpha, gamma]", "gamma",
                max(List.of("beta", "alpha", "gamma")));
        report("Challenge 83");
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
