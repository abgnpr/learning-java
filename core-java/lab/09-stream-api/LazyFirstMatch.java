/*
 * Challenge 66: Laziness and Short-Circuiting
 * Difficulty: Easy
 *
 * Task: Find the first word whose length is at least minLength and return it
 * in uppercase. Also return, in encounter order, the words inspected before
 * findFirst short-circuited. Use peek only to make evaluation visible in this
 * learning exercise; do not use peek for production business logic.
 * Complete: static SearchTrace findFirstLongWord(List<String>, int)
 * Required focus: peek, filter, map, findFirst, and lazy evaluation.
 * Run: java LazyFirstMatch.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LazyFirstMatch {
    record SearchTrace(Optional<String> match, List<String> inspected) {
    }

    private LazyFirstMatch() {
    }

    static SearchTrace findFirstLongWord(List<String> words, int minLength) {
        throw new UnsupportedOperationException("TODO: observe a lazy, short-circuiting pipeline");
    }

    public static void main(String[] args) {
        check("evaluation stops after the first match", "List.of(\"a\", \"tool\", \"stream\", \"pipeline\"), 5", new SearchTrace(Optional.of("STREAM"), List.of("a", "tool", "stream")), findFirstLongWord(List.of("a", "tool", "stream", "pipeline"), 5));
        check("all elements inspected without a match", "List.of(\"a\", \"bb\"), 3", new SearchTrace(Optional.empty(), List.of("a", "bb")), findFirstLongWord(List.of("a", "bb"), 3));
        check("empty stream", "List.of(), 1", new SearchTrace(Optional.empty(), List.of()), findFirstLongWord(List.of(), 1));
        report("Challenge 66");
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
