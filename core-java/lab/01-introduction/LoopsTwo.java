
/*
 * Challenge 07: Java Loops II
 * Difficulty: Easy
 *
 * Task: Starting at start, repeatedly add increment multiplied by successive
 * powers of two and return each resulting term.
 * Complete: static List<Long> buildSeries(long start, long increment, int terms)
 * Run: java LoopsTwo.java
 */
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public final class LoopsTwo {
    private LoopsTwo() {
    }

    static List<Long> buildSeries(long start, long increment, int terms) {
        // The loop carries state: each term is built from the previous one.
        //     long p = 1, v = start;                  // int p overflows at 32 terms
        //     for (int i = 0; i < terms; i++) { v += increment * p; l.add(v); p *= 2; }
        // map() cannot do that -- it is a pure per-element function with no memory
        // of earlier elements (that is what lets streams split and parallelize).
        // Faking it with a captured long[] accumulator breaks under .parallel().
        //
        // So the stream is only legal because the recurrence has a closed form.
        // Unrolling the additions leaves a sum of powers of two, and a run of
        // those is always one less than the next power (1+2+4+8 = 1111b = 2^4 - 1):
        //     term i = start + increment * (2^i - 1)
        // The whole loop history collapses into one shift.
        return LongStream.iterate(1, i -> i + 1)
                .limit(terms)
                // The exponent is tied to where the source starts counting. This
                // source emits 1,2,3..., so it is (1L << i) - 1. Off an
                // IntStream.range(0, terms) -- which emits 0,1,2... and needs no
                // limit() -- the same series needs (1L << (i + 1)) - 1. Copying a
                // closed form without checking the first index is an off-by-one.
                // 1L, not 1: 1 << 31 overflows an int. Math.pow is worse -- it
                // returns a double, which is inexact past 2^53.
                .mapToObj(i -> start + increment * ((1L << i) - 1))
                // LongStream has no collect(Collector); that is Stream<T>'s. Its
                // own collect() is the 3-arg supplier/accumulator/combiner form.
                // mapToObj crosses to the object world and maps in one step;
                // boxed() would be the crossing alone.
                .collect(Collectors.toList());
        // Untested edge: 1L << i is meaningful only for i < 63. Java masks the
        // shift count mod 64, so 1L << 64 is silently 1 -- wrong values, no throw.
        // terms = 0 needs no guard: limit(0) is empty and collects to [].
    }

    public static void main(String[] args) {
        checkEquals(List.of(3L, 5L, 9L, 17L), buildSeries(2, 1, 4), "basic series");
        checkEquals(List.of(0L, -12L, -36L), buildSeries(6, -6, 3), "negative increment");
        checkEquals(List.of(), buildSeries(99, 4, 0), "no terms");
        if (failures > 0) {
            throw new AssertionError("Challenge 07: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 07 passed.");
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
