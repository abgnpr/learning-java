
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
        checkEquals("5 x 1 = 5\n5 x 2 = 10\n5 x 3 = 15",
                multiplicationTable(5, 3), "three rows");
        checkEquals("-2 x 1 = -2\n-2 x 2 = -4", multiplicationTable(-2, 2), "negative value");
        checkEquals("", multiplicationTable(9, 0), "zero rows");
        if (failures > 0) {
            throw new AssertionError("Challenge 06: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 06 passed.");
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
