
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
        // mapToObj, not map: IntStream.map is an IntUnaryOperator (int -> int),
        // so returning a String there does not compile. mapToObj is the single
        // crossing point from a primitive stream back to Stream<T>.
        //     .map(i -> String.format(...))   // int -> String: won't compile
        //
        // Collectors exist on Stream<T> only. IntStream has NO collect(Collector)
        // overload -- its 3-arg collect is mutable reduction, a different shape
        // entirely (supplier, accumulator, combiner):
        //     collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        // So Collectors.joining() is unreachable until after mapToObj.
        //
        // rangeClosed(1, limit) -- closed, so limit is included; range() would
        // stop one short. Starting at 1 also makes the empty case free:
        // rangeClosed(1, 0) is an empty stream and joining() returns "" for
        // one, so "zero rows" needs no branch of its own.
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
