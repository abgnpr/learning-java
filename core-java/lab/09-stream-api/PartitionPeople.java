/*
 * Challenge 77: Partitioning with Downstream Mapping
 * Difficulty: Medium
 *
 * Task: Partition people into adults (true) and minors (false), collecting
 * only their names in encounter order. The result must contain both Boolean
 * keys even when one or both groups are empty.
 * Complete: static Map<Boolean, List<String>> partitionNames(List<Person>)
 * Required focus: partitioningBy with mapping as a downstream collector.
 * Run: java PartitionPeople.java
 */
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PartitionPeople {
    record Person(String name, int age) {
    }

    private PartitionPeople() {
    }

    static Map<Boolean, List<String>> partitionNames(List<Person> people) {
        throw new UnsupportedOperationException("TODO: partition and map people");
    }

    public static void main(String[] args) {
        Map<Boolean, List<String>> expected = Map.of(
                true, List.of("Ada", "Grace"),
                false, List.of("Linus"));
        checkEquals(expected, partitionNames(List.of(
                new Person("Ada", 18), new Person("Linus", 17), new Person("Grace", 30))),
                "adult partition");
        checkEquals(Map.of(true, List.of(), false, List.of()), partitionNames(List.of()),
                "both keys exist for empty input");
        System.out.println("Challenge 77 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
