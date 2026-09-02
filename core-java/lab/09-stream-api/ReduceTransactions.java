/*
 * Challenge 73: Three-Argument Reduction
 * Difficulty: Medium
 *
 * Task: Reduce transactions into an immutable total containing transaction
 * count and net amount. The accumulator and combiner must be associative so
 * the same method works correctly for sequential and parallel streams.
 * Complete: static Totals total(Stream<Transaction> transactions)
 * Required focus: reduce(identity, accumulator, combiner).
 * Run: java ReduceTransactions.java
 */
import java.math.BigDecimal;
import java.util.stream.Stream;

public final class ReduceTransactions {
    record Transaction(BigDecimal amount) {
    }

    record Totals(long count, BigDecimal net) {
    }

    private ReduceTransactions() {
    }

    static Totals total(Stream<Transaction> transactions) {
        throw new UnsupportedOperationException("TODO: implement an associative reduction");
    }

    public static void main(String[] args) {
        Transaction[] values = {
                new Transaction(new BigDecimal("10.25")),
                new Transaction(new BigDecimal("-2.00")),
                new Transaction(new BigDecimal("3.75"))
        };

        checkEquals(new Totals(3, new BigDecimal("12.00")), total(Stream.of(values)),
                "sequential total");
        checkEquals(new Totals(3, new BigDecimal("12.00")), total(Stream.of(values).parallel()),
                "parallel total");
        checkEquals(new Totals(0, BigDecimal.ZERO), total(Stream.empty()), "identity for empty stream");
        System.out.println("Challenge 73 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
