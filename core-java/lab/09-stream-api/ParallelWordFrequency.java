/*
 * Challenge 82: Parallel Collection Without Shared Mutation
 * Difficulty: Hard
 *
 * Task: Count case-insensitive words across lines using a parallel stream.
 * Split on whitespace, ignore blank lines, and return a ConcurrentMap. Build
 * the result through a concurrent collector; do not mutate a shared HashMap
 * from forEach.
 * Complete: static ConcurrentMap<String, Long> frequencies(List<String> lines)
 * Required focus: parallelStream, flatMap, groupingByConcurrent, and counting.
 * Run: java ParallelWordFrequency.java
 */
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ParallelWordFrequency {
    private ParallelWordFrequency() {
    }

    static ConcurrentMap<String, Long> frequencies(List<String> lines) {
        throw new UnsupportedOperationException("TODO: collect frequencies safely in parallel");
    }

    public static void main(String[] args) {
        ConcurrentMap<String, Long> actual = frequencies(List.of(
                "Java streams streams", "parallel JAVA", "  ", "safe parallel streams"));
        check("java count", "actual.get(\"java\")", 2L, actual.get("java"));
        check("streams count", "actual.get(\"streams\")", 3L, actual.get("streams"));
        check("parallel count", "actual.get(\"parallel\")", 2L, actual.get("parallel"));
        check("safe count", "actual.get(\"safe\")", 1L, actual.get("safe"));
        check("distinct word count", "actual.size()", 4, actual.size());
        checkThat("result implements ConcurrentMap", "actual instanceof ConcurrentMap", actual instanceof ConcurrentMap);
        check("empty input", "List.of()", 0, frequencies(List.of()).size());
        report("Challenge 82");
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
