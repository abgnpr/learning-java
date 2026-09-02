
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
        System.out.println("Challenge 06 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
