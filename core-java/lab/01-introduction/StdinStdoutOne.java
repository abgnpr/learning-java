
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
        checkEquals("7\n11\n-3", echoIntegers("7 11 -3"), "space-separated input");
        checkEquals("4\n5\n6", echoIntegers("  4\n5\t6  "), "mixed whitespace");
        checkEquals("7\n4\n0", echoIntegers("007 +4 -0"), "integer normalization");
        checkEquals("", echoIntegers(" \n\t "), "empty input");
        if (failures > 0) {
            throw new AssertionError("Challenge 02: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 02 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + show(actual));
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + "\n  expected: " + show(expected)
                + "\n    actual: " + show(actual));
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
}
