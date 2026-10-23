/*
 * Challenge 80: Build a Custom Collector (Hard)
 *
 * Task: Implement a Collector<String, StringJoiner, String> that joins values
 * with " | " inside "<" and ">". It must preserve encounter order and merge
 * partial results correctly for sequential and parallel streams.
 *
 * Complete: bracketedJoining().
 * Required focus: Collector.of supplier, accumulator, combiner, and finisher.
 * Java baseline: Java 8.
 * Run: javac CustomBracketCollector.java && java CustomBracketCollector
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collector;

public final class CustomBracketCollector {
    private CustomBracketCollector() {
    }

    static Collector<String, StringJoiner, String> bracketedJoining() {
        throw new UnsupportedOperationException(
                "TODO: construct a collector with Collector.of");
    }

    public static void main(String[] args) {
        check("sequential collection", "a, b, c",
                "<a | b | c>",
                Arrays.asList("a", "b", "c")
                        .stream()
                        .collect(bracketedJoining()));

        check("parallel combination", "a, b, c, d, e",
                "<a | b | c | d | e>",
                Arrays.asList("a", "b", "c", "d", "e")
                        .parallelStream()
                        .collect(bracketedJoining()));

        check("empty stream", "empty list",
                "<>",
                Collections.<String>emptyList()
                        .stream()
                        .collect(bracketedJoining()));

        check("single value", "only",
                "<only>",
                Collections.singletonList("only")
                        .stream()
                        .collect(bracketedJoining()));

        Collector<String, StringJoiner, String> collector = bracketedJoining();
        check("characteristics match ordered non-concurrent finishing", "collector metadata",
                Collections.emptySet(), collector.characteristics());
        StringJoiner left = collector.supplier().get();
        StringJoiner right = collector.supplier().get();
        collector.accumulator().accept(left, "a");
        collector.accumulator().accept(right, "b");
        check("combiner preserves left then right encounter order", "two partial accumulators",
                "<a | b>", collector.finisher().apply(collector.combiner().apply(left, right)));
        StringJoiner empty = collector.supplier().get();
        StringJoiner single = collector.supplier().get();
        collector.accumulator().accept(single, "x");
        check("empty partial accumulator is a valid identity", "empty left plus x",
                "<x>", collector.finisher().apply(collector.combiner().apply(empty, single)));

        report("Challenge 80");
    }

    private static int passes;
    private static int failures;

    private static void check(
            String label,
            Object input,
            Object expected,
            Object actual) {
        boolean ok = Objects.deepEquals(expected, actual);

        if (ok) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, "
                + failures + " failed.");

        if (failures > 0) {
            throw new AssertionError(
                    challenge + ": " + failures + " check(s) failed.");
        }

        System.out.println(challenge + " passed.");
    }
}
