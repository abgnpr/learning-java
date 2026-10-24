/*
 * Challenge 132 — Employee Stream Queries (Medium)
 *
 * Task: Answer four report queries without changing the source list:
 * - highestPaidByDepartment: select one employee per department; greater salary
 *   wins, then smaller id breaks a salary tie. Do not use name as a tie-breaker.
 * - averageSalaryByDepartment: average every salary, including duplicates and
 *   zeroes, as a double. Do not perform integer division or sum into an int.
 * - namesByDepartmentAndRole: group by department, then role, retaining names
 *   in encounter order, including repeated names.
 * - secondHighestDistinctSalary: return the second greatest DISTINCT salary
 *   across all departments, or OptionalInt.empty() when fewer than two exist.
 * All report maps, including nested maps, iterate keys in natural String order.
 * Results are detached from later source-list changes; mutability is unspecified.
 * Inputs and fields are non-null; ids are unique; salaries are non-negative ints.
 * Complete: the four query methods. Baseline: Java 21.
 * Required focus: groupingBy, downstream maxBy/collectingAndThen, averagingInt,
 * nested groupingBy/mapping, and distinct/sorted/skip/findFirst.
 * Run: java 09-stream-api/EmployeeStreamQueries.java
 * Explain: second employee versus second distinct salary; comparator direction
 * inside maxBy; encounter order versus map-key order; average overflow/precision.
 */
import java.util.*;
import java.util.stream.Collectors;

public final class EmployeeStreamQueries {
    record Employee(int id, String name, String department, String role, int salary) { }

    static Map<String, Employee> highestPaidByDepartment(List<Employee> employees) {
        throw new UnsupportedOperationException("TODO: select a deterministic maximum per department");
    }

    static Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        throw new UnsupportedOperationException("TODO: average salaries per department");
    }

    static Map<String, Map<String, List<String>>> namesByDepartmentAndRole(List<Employee> employees) {
        throw new UnsupportedOperationException("TODO: group twice and map employees to names");
    }

    static OptionalInt secondHighestDistinctSalary(List<Employee> employees) {
        throw new UnsupportedOperationException("TODO: select the second distinct salary");
    }

    public static void main(String[] args) {
        Employee zoe = new Employee(9, "Zoe", "Tech", "Engineer", 120);
        Employee ana = new Employee(3, "Ana", "Tech", "Engineer", 120);
        Employee ben = new Employee(5, "Ben", "Sales", "Manager", 90);
        Employee sam = new Employee(7, "Sam", "Tech", "Manager", 60);
        Employee samAgain = new Employee(8, "Sam", "Tech", "Engineer", 100);
        Employee mia = new Employee(2, "Mia", "Sales", "Agent", 0);
        List<Employee> source = new ArrayList<>(List.of(zoe, ana, ben, sam, samAgain, mia));
        List<Employee> before = List.copyOf(source);

        Map<String, Employee> highest = highestPaidByDepartment(source);
        Map<String, Double> averages = averageSalaryByDepartment(source);
        Map<String, Map<String, List<String>>> names = namesByDepartmentAndRole(source);
        check("maximum per department with smaller-id tie-breaker",
                Map.of("Sales", ben, "Tech", ana), highest);
        check("average counts tied salaries and zeroes",
                Map.of("Sales", 45.0, "Tech", 100.0), averages);
        check("nested grouping preserves names in encounter order", Map.of(
                "Sales", Map.of("Agent", List.of("Mia"), "Manager", List.of("Ben")),
                "Tech", Map.of("Engineer", List.of("Zoe", "Ana", "Sam"),
                        "Manager", List.of("Sam"))), names);
        check("second distinct salary skips both highest earners", OptionalInt.of(100),
                secondHighestDistinctSalary(source));
        check("highest map orders department keys", List.of("Sales", "Tech"),
                new ArrayList<>(highest.keySet()));
        check("average map orders department keys", List.of("Sales", "Tech"),
                new ArrayList<>(averages.keySet()));
        check("nested map orders department keys", List.of("Sales", "Tech"),
                new ArrayList<>(names.keySet()));
        check("nested map orders Sales roles", List.of("Agent", "Manager"),
                new ArrayList<>(names.get("Sales").keySet()));
        check("nested map orders Tech roles", List.of("Engineer", "Manager"),
                new ArrayList<>(names.get("Tech").keySet()));
        check("all queries leave the source unchanged", before, source);

        Employee smallerId = new Employee(1, "Zed", "Tie", "Same", 10);
        Employee largerId = new Employee(2, "Amy", "Tie", "Same", 10);
        check("id wins a salary tie even when names and encounter order disagree",
                Map.of("Tie", smallerId), highestPaidByDepartment(List.of(largerId, smallerId)));
        Employee lowestId = new Employee(Integer.MIN_VALUE, "Low id", "Tie", "Same", 10);
        Employee highestId = new Employee(Integer.MAX_VALUE, "High id", "Tie", "Same", 10);
        check("id comparison does not overflow across the int range", Map.of("Tie", lowestId),
                highestPaidByDepartment(List.of(highestId, lowestId)));
        check("salary outranks the id tie-breaker", Map.of("Tech", zoe),
                highestPaidByDepartment(List.of(sam, zoe)));
        check("all-equal salaries have no second distinct value", OptionalInt.empty(),
                secondHighestDistinctSalary(List.of(smallerId, largerId)));
        Employee zero = new Employee(4, "Zero", "Tie", "Same", 0);
        check("zero is a real second salary", OptionalInt.of(0),
                secondHighestDistinctSalary(List.of(smallerId, largerId, zero)));
        check("fractional average is retained", Map.of("Tie", 5.5),
                averageSalaryByDepartment(List.of(smallerId,
                        new Employee(6, "One", "Tie", "Same", 1))));
        List<Employee> extremes = List.of(
                new Employee(10, "High", "Large", "Same", Integer.MAX_VALUE),
                new Employee(11, "Also high", "Large", "Same", Integer.MAX_VALUE),
                new Employee(12, "Low", "Large", "Same", 1));
        check("average does not overflow an int sum", Map.of("Large", 1431655765.0),
                averageSalaryByDepartment(extremes));
        check("wide salary range still selects the second distinct value", OptionalInt.of(1),
                secondHighestDistinctSalary(extremes));
        check("equal names within one group are retained", Map.of("Tech", Map.of(
                "Engineer", List.of("Sam", "Sam"))), namesByDepartmentAndRole(List.of(
                        samAgain, new Employee(13, "Sam", "Tech", "Engineer", 80))));

        check("empty maxima", Map.of(), highestPaidByDepartment(List.of()));
        check("empty averages", Map.of(), averageSalaryByDepartment(List.of()));
        check("empty nested grouping", Map.of(), namesByDepartmentAndRole(List.of()));
        check("empty salary selection", OptionalInt.empty(), secondHighestDistinctSalary(List.of()));
        check("singleton maximum", Map.of("Tech", zoe), highestPaidByDepartment(List.of(zoe)));
        check("singleton average", Map.of("Tech", 120.0), averageSalaryByDepartment(List.of(zoe)));
        check("singleton has no second salary", OptionalInt.empty(),
                secondHighestDistinctSalary(List.of(zoe)));
        source.clear();
        check("selected employees remain detached", Map.of("Sales", ben, "Tech", ana), highest);
        check("averages remain detached", Map.of("Sales", 45.0, "Tech", 100.0), averages);
        check("nested names remain detached", List.of("Zoe", "Ana", "Sam"),
                names.get("Tech").get("Engineer"));
        report("Challenge 132");
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
