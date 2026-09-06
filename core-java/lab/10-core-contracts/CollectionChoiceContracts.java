/*
 * Challenge 87 — Collection Choice Contracts (Medium)
 *
 * Task: Select concrete collection implementations whose behavior is part of
 * the result contract: first-seen uniqueness, sorted uniqueness, and FIFO
 * removal. Do not sort after the fact.
 * Complete: firstSeen(...), sortedUnique(...), fifo(...)
 * Run: java CollectionChoiceContracts.java
 */
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class CollectionChoiceContracts {
    static Set<String> firstSeen(List<String> values) {
        // TODO: Preserve encounter order while removing duplicates.
        throw new UnsupportedOperationException("TODO: choose an ordered set");
    }

    static NavigableSet<Integer> sortedUnique(List<Integer> values) {
        // TODO: Maintain elements in sorted order with navigation operations.
        throw new UnsupportedOperationException("TODO: choose a navigable set");
    }

    static Deque<String> fifo(List<String> values) {
        // TODO: Build a queue with head removal and no null-element support.
        throw new UnsupportedOperationException("TODO: choose a FIFO deque");
    }

    public static void main(String[] args) {
        Set<String> encountered = firstSeen(List.of("b", "a", "b", "c", "a"));
        check("first occurrence order", "[b,a,b,c,a]",
                List.of("b", "a", "c"), List.copyOf(encountered));
        checkThat("first-seen type advertises ordering", encountered,
                encountered instanceof LinkedHashSet);

        NavigableSet<Integer> sorted = sortedUnique(List.of(7, 2, 7, 4, 1));
        check("sorted unique values", "[7,2,7,4,1]",
                List.of(1, 2, 4, 7), List.copyOf(sorted));
        check("nearest lower value", "lower(4)", 2, sorted.lower(4));
        checkThat("sorted type is TreeSet", sorted, sorted instanceof TreeSet);

        Deque<String> queue = fifo(List.of("first", "second", "third"));
        check("FIFO first removal", queue, "first", queue.removeFirst());
        check("FIFO remaining order", queue, List.of("second", "third"), List.copyOf(queue));
        checkThat("queue type is ArrayDeque", queue, queue instanceof ArrayDeque);
        report("Challenge 87");
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
