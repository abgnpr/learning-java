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

    static Map<String, BigDecimal> totalsByDepartment(List<Sale> sales) {
        throw new UnsupportedOperationException("TODO: group and reduce sales");
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
        checkEquals(expected, totalsByDepartment(sales), "department totals");
        checkEquals(new TreeMap<>(), totalsByDepartment(List.of()), "empty input");
        checkTrue(totalsByDepartment(sales) instanceof TreeMap, "result type is TreeMap");
        System.out.println("Challenge 76 passed.");
    }

    private static void checkTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
