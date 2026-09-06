
/*
 * Challenge 76: Grouping with a Downstream Collector
 * Difficulty: Medium
 *
 * Task: Group sales by department and sum each department's BigDecimal
 * amounts. Return a TreeMap so department keys are ordered. Do not mutate the
 * supplied list or perform a second pass over it.
 * Complete: static Map<String, BigDecimal> totalsByDepartment(List<Sale> sales)
 * Required focus: groupingBy with map supplier and reducing downstream collector.
 * Run: java GroupingSales.java
 */
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class GroupingSales {
    record Sale(String department, BigDecimal amount) {
    }

    private GroupingSales() {
    }

    /*
     * groupingBy has three overloads, and the map supplier only exists on the
     * three-arg one: groupingBy(classifier, mapFactory, downstream). The supplier
     * is a bare Supplier<M extends Map<K,D>> in the middle slot — TreeMap::new goes
     * there directly, NOT wrapped in a collector:
     *
     *     .collect(Collectors.groupingBy(Sale::department),      // two collectors is
     *              Collectors.toCollection(TreeMap::new))        // not a collect() shape
     *
     * collect takes either one Collector or the three-arg (supplier, accumulator,
     * combiner) — never two collectors. And toCollection cannot build a Map at all:
     * its supplier is bounded by Collection, so TreeMap fails to compile there.
     *
     * The downstream slot wants a Collector, so a method reference can never stand
     * in for it — Collector has five abstract methods, which is what javac means by
     * "not a functional interface; multiple non-overriding abstract methods":
     *
     *     Collectors.groupingBy(Sale::department, TreeMap::new, BigDecimal::add)
     *
     * reducing's one-arg overload does not fit either. reducing(BinaryOperator)
     * returns Optional<T>, which would make the map a Map<String, Optional<BigDecimal>>,
     * and its T is pinned to BigDecimal while the stream elements are Sale — nothing
     * extracts the amount:
     *
     *     Collectors.reducing(BigDecimal::add)
     *
     * The three-arg reducing(identity, mapper, op) fixes both: identity makes the
     * result a plain BigDecimal, and the mapper — in the MIDDLE, easy to write last —
     * is the Sale -> BigDecimal step. Collectors.mapping(Sale::amount,
     * Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)) is the same thing, longer.
     *
     * ZERO is safe as the identity even though it has scale 0 and the expected totals
     * have scale 2: add() gives the result the LARGER of the two scales, so
     * ZERO.add(12.50) is 12.50, not 12.5. That matters because BigDecimal.equals
     * compares scale as well as value — 0.00 does not equal ZERO — so an identity
     * that truncated would fail the checks. Use compareTo when only value matters.
     * There is no Collectors.summingBigDecimal in the JDK (the summing* helpers are
     * primitive-only), which is exactly why this reaches for reducing.
     */
    static Map<String, BigDecimal> totalsByDepartment(List<Sale> sales) {
        return sales.stream().collect(Collectors.groupingBy(
                Sale::department,
                TreeMap::new,
                Collectors.reducing(
                        BigDecimal.ZERO,
                        Sale::amount,
                        BigDecimal::add)));
    }

    public static void main(String[] args) {
        List<Sale> sales = List.of(
                new Sale("books", new BigDecimal("12.50")),
                new Sale("games", new BigDecimal("20.00")),
                new Sale("books", new BigDecimal("7.50")),
                new Sale("games", new BigDecimal("-5.00")));

        Map<String, BigDecimal> expected = new TreeMap<>();
        expected.put("books", new BigDecimal("20.00"));
        expected.put("games", new BigDecimal("15.00"));
        check("department totals", "sales", expected, totalsByDepartment(sales));
        check("empty input", "List.of()", new TreeMap<>(), totalsByDepartment(List.of()));
        checkThat("result type is TreeMap", "sales", totalsByDepartment(sales) instanceof TreeMap);

        // A group of one still reduces: the value is the sum, not the element.
        Map<String, BigDecimal> one = new TreeMap<>();
        one.put("books", new BigDecimal("12.50"));
        check("single sale", "one books sale", one,
                totalsByDepartment(List.of(new Sale("books", new BigDecimal("12.50")))));

        // Every element in one group.
        Map<String, BigDecimal> single = new TreeMap<>();
        single.put("books", new BigDecimal("30.00"));
        check("all in one department", "three books sales", single,
                totalsByDepartment(List.of(
                        new Sale("books", new BigDecimal("12.50")),
                        new Sale("books", new BigDecimal("7.50")),
                        new Sale("books", new BigDecimal("10.00")))));

        // Many keys, every group of size one.
        Map<String, BigDecimal> spread = new TreeMap<>();
        spread.put("books", new BigDecimal("1.00"));
        spread.put("games", new BigDecimal("2.00"));
        spread.put("music", new BigDecimal("3.00"));
        check("one sale per department", "three departments", spread,
                totalsByDepartment(List.of(
                        new Sale("music", new BigDecimal("3.00")),
                        new Sale("books", new BigDecimal("1.00")),
                        new Sale("games", new BigDecimal("2.00")))));

        // BigDecimal.equals compares scale as well as value, so the zero sum of two
        // scale-2 amounts is 0.00 and NOT BigDecimal.ZERO (scale 0).
        Map<String, BigDecimal> zeroSum = new TreeMap<>();
        zeroSum.put("books", new BigDecimal("0.00"));
        check("amounts cancel to zero", "-5.00 and 5.00", zeroSum,
                totalsByDepartment(List.of(
                        new Sale("books", new BigDecimal("-5.00")),
                        new Sale("books", new BigDecimal("5.00")))));

        // add() gives the result the larger of the two scales, so mixed scales widen
        // rather than truncate — and the ZERO identity never drags the scale down.
        Map<String, BigDecimal> scales = new TreeMap<>();
        scales.put("books", new BigDecimal("3.75"));
        check("mixed scales widen", "1.5 and 2.25", scales,
                totalsByDepartment(List.of(
                        new Sale("books", new BigDecimal("1.5")),
                        new Sale("books", new BigDecimal("2.25")))));

        // The TreeMap supplier is what sorts the keys; the instanceof check above
        // would still hold if the order were wrong.
        check("keys are sorted", "unsorted input",
                List.of("apples", "books", "games"),
                List.copyOf(totalsByDepartment(List.of(
                        new Sale("games", new BigDecimal("1.00")),
                        new Sale("apples", new BigDecimal("2.00")),
                        new Sale("books", new BigDecimal("3.00")))).keySet()));

        // The stated contract forbids mutating the input.
        List<Sale> before = List.copyOf(sales);
        totalsByDepartment(sales);
        check("input is not mutated", "sales", before, sales);

        report("Challenge 76");
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
