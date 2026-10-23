/*
 * Challenge 127 — BigDecimal Money Contracts (Medium)
 *
 * Task: Parse non-negative decimal text exactly into scale 2. Extra trailing
 * zeroes are legal; non-zero sub-cent digits must raise ArithmeticException.
 * Never construct money via double. Calculate amount * rate rounded once to
 * two places with HALF_EVEN (rate is a fraction: 0.025 means 2.5 percent).
 * Allocate a scale-2 total among positive parts without losing a cent: earlier
 * parts receive one extra cent until the remainder is exhausted.
 * Complete: amount, fee, allocate, sameNumericValue.
 * Run: java 03-big-numbers/BigDecimalMoneyContracts.java
 * Explain: new BigDecimal(double) versus string/valueOf; rounding policy at a
 * boundary; equals/hashCode versus compareTo and HashSet versus TreeSet.
 */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class BigDecimalMoneyContracts {
    static BigDecimal amount(String text) {
        throw new UnsupportedOperationException("TODO: parse exact non-negative scale-2 money");
    }

    // Inputs are non-null, non-negative. Reject a negative input with IllegalArgumentException.
    static BigDecimal fee(BigDecimal amount, BigDecimal rate) {
        throw new UnsupportedOperationException("TODO: apply the rate and HALF_EVEN rounding");
    }

    // total is non-null, non-negative, and exactly representable at scale 2.
    // Return an unmodifiable list, including for zero total; reject parts <= 0.
    static List<BigDecimal> allocate(BigDecimal total, int parts) {
        throw new UnsupportedOperationException("TODO: divide exact cents and distribute remainder");
    }

    static boolean sameNumericValue(BigDecimal left, BigDecimal right) {
        throw new UnsupportedOperationException("TODO: compare numerical value independently of scale");
    }

    public static void main(String[] args) {
        check("scale is part of the result", new BigDecimal("12.00"), amount("12"));
        check("trailing zeroes are exact", new BigDecimal("1.23"), amount("1.2300"));
        check("beyond double precision", new BigDecimal("9007199254740993.01"), amount("9007199254740993.01"));
        expect(ArithmeticException.class, () -> amount("1.001"));
        expect(IllegalArgumentException.class, () -> amount("-0.01"));
        expect(NumberFormatException.class, () -> amount("twelve"));
        check("ties round to an even cent", new BigDecimal("0.02"), fee(new BigDecimal("1.00"), new BigDecimal("0.025")));
        check("odd cent tie rounds up", new BigDecimal("0.04"), fee(new BigDecimal("1.00"), new BigDecimal("0.035")));
        check("round only the final product", new BigDecimal("3.09"), fee(new BigDecimal("123.45"), new BigDecimal("0.025")));
        expect(IllegalArgumentException.class, () -> fee(BigDecimal.ONE, new BigDecimal("-0.1")));
        expect(IllegalArgumentException.class, () -> fee(new BigDecimal("-1"), BigDecimal.ONE));

        List<BigDecimal> thirds = allocate(new BigDecimal("10.00"), 3);
        check("remainder goes to earlier parts", List.of(new BigDecimal("3.34"), new BigDecimal("3.33"), new BigDecimal("3.33")), thirds);
        check("no cent is lost", new BigDecimal("10.00"), thirds.stream().reduce(new BigDecimal("0.00"), BigDecimal::add));
        check("more recipients than cents", List.of(new BigDecimal("0.01"), new BigDecimal("0.01"), new BigDecimal("0.00")), allocate(new BigDecimal("0.02"), 3));
        check("zero total still has every part", List.of(new BigDecimal("0.00"), new BigDecimal("0.00")), allocate(BigDecimal.ZERO, 2));
        check("large totals do not narrow to int cents", List.of(new BigDecimal("5000000000.01"), new BigDecimal("5000000000.00")), allocate(new BigDecimal("10000000000.01"), 2));
        expect(UnsupportedOperationException.class, () -> thirds.add(BigDecimal.ZERO));
        expect(IllegalArgumentException.class, () -> allocate(BigDecimal.ONE, 0));
        expect(IllegalArgumentException.class, () -> allocate(BigDecimal.ONE, -1));
        expect(IllegalArgumentException.class, () -> allocate(new BigDecimal("-1.00"), 2));
        check("numerical equality ignores scale", true, sameNumericValue(new BigDecimal("1.0"), new BigDecimal("1.00")));
        check("different numerical values differ", false, sameNumericValue(new BigDecimal("1.0"), new BigDecimal("1.01")));
        List<BigDecimal> scales = List.of(new BigDecimal("1.0"), new BigDecimal("1.00"));
        check("HashSet uses equals", 2, new HashSet<>(scales).size());
        check("TreeSet uses comparison", 1, new TreeSet<>(scales).size());
        report("Challenge 127");
    }

    // ---- test harness (not part of the exercise) ----
    private static int passes;
    private static int failures;

    private static void check(String label, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    private static String show(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return "<" + text.replace("\n", "\\n") + ">";
        if (value.getClass().isArray()) return java.util.Arrays.deepToString(new Object[]{value});
        return value + " (" + value.getClass().getSimpleName() + ")";
    }

    @FunctionalInterface
    interface CheckedAction { void run() throws Exception; }

    private static <E extends Throwable> E expect(Class<E> type, CheckedAction action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("expected " + type.getName()
                    + ", got " + actual.getClass().getName(), actual);
            passes++;
            System.out.println("PASS  throws " + type.getSimpleName());
            return type.cast(actual);
        }
        throw new AssertionError("expected " + type.getName() + ", nothing was thrown");
    }

    private static void report(String name) {
        System.out.println(name + ": " + passes + " passed, " + failures + " failed.");
        if (failures != 0) throw new AssertionError(name + " failed");
        System.out.println(name + " passed.");
    }
}
