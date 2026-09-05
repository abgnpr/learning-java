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
        check("department totals", "sales", expected, totalsByDepartment(sales));
        check("empty input", "List.of()", new TreeMap<>(), totalsByDepartment(List.of()));
        checkThat("result type is TreeMap", "sales", totalsByDepartment(sales) instanceof TreeMap);
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
