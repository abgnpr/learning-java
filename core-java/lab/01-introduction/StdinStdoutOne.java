
import java.util.Arrays;
import java.util.stream.Collectors;

/*
 * Challenge 02: Java Stdin and Stdout I
 * Difficulty: Easy
 *
 * Task: Parse every whitespace-separated integer and return one normalized
 * base-10 integer per line in a single String. Do not print inside this method.
 * Complete: static String echoIntegers(String input)
 * Run: java StdinStdoutOne.java
 */
public final class StdinStdoutOne {
    private StdinStdoutOne() {
    }

    static String echoIntegers(String input) {

        // classic way
        /*         
        if (input.isBlank()) {
            return "";
        }
        String[] result = input.strip().split("\\s+");
        for (int i = 0; i < result.length; i++) {
            result[i] = String.valueOf(Integer.parseInt(result[i]));
        }
        return String.join("\n", result);
        */

        // streams way
        /*   
        return Arrays.stream(input.strip().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .map(String::valueOf)
                .collect(Collectors.joining("\n")); 
        */

        // best way
        if (input.isBlank()) {
            return "";
        }
        return Arrays.stream(input.strip().split("\\s+"))
                .map(Integer::parseInt)
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        check("space-separated input", "7 11 -3", "7\n11\n-3", echoIntegers("7 11 -3"));
        check("mixed whitespace", " 4\n5\t6 ", "4\n5\n6", echoIntegers("  4\n5\t6  "));
        check("integer normalization", "007 +4 -0", "7\n4\n0", echoIntegers("007 +4 -0"));
        check("empty input", " \n\t ", "", echoIntegers(" \n\t "));
        report("Challenge 02");
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
