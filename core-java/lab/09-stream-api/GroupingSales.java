/*
 * Challenge 76: Grouping with a Downstream Collector (Medium)
 *
 * Task: Group sales by department and sum each department's BigDecimal
 * amounts. Return a TreeMap so department keys are ordered. Do not mutate the
 * supplied list or perform a second pass over it.
 *
 * Required focus: groupingBy with a map supplier and a downstream reducing
 * collector.
 * Java baseline: Java 8.
 * Run: javac GroupingSales.java && java GroupingSales
 */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class GroupingSales {
    static final class Sale {
        private final String department;
        private final BigDecimal amount;

        Sale(String department, BigDecimal amount) {
            this.department = department;
            this.amount = amount;
        }

        String getDepartment() {
            return department;
        }

        BigDecimal getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Sale)) {
                return false;
            }

            Sale that = (Sale) other;
            return Objects.equals(department, that.department)
                    && Objects.equals(amount, that.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(department, amount);
        }

        @Override
        public String toString() {
            return "Sale{department='" + department + "', amount=" + amount + "}";
        }
    }

    private GroupingSales() {
    }

    /*
     * groupingBy(classifier, mapFactory, downstream) has three distinct jobs:
     *
     * - Sale::getDepartment chooses the key.
     * - TreeMap::new chooses the result map implementation.
     * - reducing(identity, mapper, operator) reduces the sales in each group.
     *
     * BigDecimal.ZERO is neutral for addition. BigDecimal.add preserves the
     * larger operand scale, while BigDecimal.equals compares both value and
     * scale. There is no Collectors.summingBigDecimal in Java 8.
     */
    static Map<String, BigDecimal> totalsByDepartment(List<Sale> sales) {
        return sales.stream().collect(Collectors.groupingBy(
                Sale::getDepartment,
                TreeMap::new,
                Collectors.reducing(
                        BigDecimal.ZERO,
                        Sale::getAmount,
                        BigDecimal::add)));
    }

    public static void main(String[] args) {
        List<Sale> sales = list(
                sale("books", "12.50"),
                sale("games", "20.00"),
                sale("books", "7.50"),
                sale("games", "-5.00"));

        Map<String, BigDecimal> expected = map(
                "books", "20.00",
                "games", "15.00");

        check("department totals", "sales",
                expected, totalsByDepartment(sales));
        check("empty input", "empty list",
                Collections.emptyMap(),
                totalsByDepartment(Collections.<Sale>emptyList()));
        checkThat("result type is TreeMap", "sales",
                totalsByDepartment(sales) instanceof TreeMap);

        check("single sale", "one books sale",
                map("books", "12.50"),
                totalsByDepartment(list(sale("books", "12.50"))));

        check("all sales in one group", "three books sales",
                map("books", "30.00"),
                totalsByDepartment(list(
                        sale("books", "12.50"),
                        sale("books", "7.50"),
                        sale("books", "10.00"))));

        Map<String, BigDecimal> spread = map(
                "apples", "2.00",
                "books", "1.00",
                "games", "3.00");

        Map<String, BigDecimal> spreadActual = totalsByDepartment(list(
                sale("games", "3.00"),
                sale("apples", "2.00"),
                sale("books", "1.00")));

        check("one sale per department", "unsorted input",
                spread, spreadActual);
        check("keys are sorted", "spread keys",
                list("apples", "books", "games"),
                new ArrayList<String>(spreadActual.keySet()));

        check("amounts cancel at scale two", "-5.00 and 5.00",
                map("books", "0.00"),
                totalsByDepartment(list(
                        sale("books", "-5.00"),
                        sale("books", "5.00"))));

        check("mixed scales widen", "1.5 and 2.25",
                map("books", "3.75"),
                totalsByDepartment(list(
                        sale("books", "1.5"),
                        sale("books", "2.25"))));

        List<Sale> before = new ArrayList<Sale>(sales);
        totalsByDepartment(sales);
        check("input is not mutated", "sales", before, sales);

        report("Challenge 76");
    }

    private static Sale sale(String department, String amount) {
        return new Sale(department, new BigDecimal(amount));
    }

    private static Map<String, BigDecimal> map(String... keyValuePairs) {
        Map<String, BigDecimal> result = new TreeMap<String, BigDecimal>();

        for (int index = 0; index < keyValuePairs.length; index += 2) {
            result.put(
                    keyValuePairs[index],
                    new BigDecimal(keyValuePairs[index + 1]));
        }

        return result;
    }

    @SafeVarargs
    private static <T> List<T> list(T... values) {
        return Arrays.asList(values);
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

    private static void checkThat(
            String label,
            Object input,
            boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: condition holds");
        System.out.println("      actual:   "
                + (condition ? "holds" : "does not hold"));
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
