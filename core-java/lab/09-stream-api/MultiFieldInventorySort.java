/*
 * Challenge 111: Multi-Field Comparator Sorting
 * Difficulty: Medium
 *
 * Task: Produce an inventory report ordered by warehouse ascending, then by
 * quantity descending within each warehouse, and finally by item name in
 * ascending natural (lexicographical) order when both earlier fields tie.
 * Do not modify the supplied list.
 * Complete: static List<InventoryItem> sortInventory(List<InventoryItem> items)
 * Required focus: sorted, Comparator.comparing, thenComparing, reversed, and
 * Collectors.toList.
 * Run: java MultiFieldInventorySort.java
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class MultiFieldInventorySort {
    record InventoryItem(String warehouse, int quantity, String name) {
    }

    private MultiFieldInventorySort() {
    }

    static List<InventoryItem> sortInventory(List<InventoryItem> items) {
        throw new UnsupportedOperationException("TODO: sort with three comparator keys");
    }

    public static void main(String[] args) {
        List<InventoryItem> items = new ArrayList<>(List.of(
                new InventoryItem("warehouse-b", 12, "mouse"),
                new InventoryItem("warehouse-a", 5, "monitor"),
                new InventoryItem("warehouse-a", 20, "mouse"),
                new InventoryItem("warehouse-b", 2, "cable"),
                new InventoryItem("warehouse-a", 20, "keyboard"),
                new InventoryItem("warehouse-b", 12, "adapter")));
        List<InventoryItem> originalOrder = List.copyOf(items);

        check("all three keys", "mixed inventory", List.of(
                new InventoryItem("warehouse-a", 20, "keyboard"),
                new InventoryItem("warehouse-a", 20, "mouse"),
                new InventoryItem("warehouse-a", 5, "monitor"),
                new InventoryItem("warehouse-b", 12, "adapter"),
                new InventoryItem("warehouse-b", 12, "mouse"),
                new InventoryItem("warehouse-b", 2, "cable")), sortInventory(items));

        check("first key is ascending", "warehouses c, a, b", List.of(
                new InventoryItem("a", 1, "item"),
                new InventoryItem("b", 1, "item"),
                new InventoryItem("c", 1, "item")), sortInventory(List.of(
                new InventoryItem("c", 1, "item"),
                new InventoryItem("a", 1, "item"),
                new InventoryItem("b", 1, "item"))));

        check("second key alone is descending", "one warehouse, quantities 3, 9, 1", List.of(
                new InventoryItem("a", 9, "item"),
                new InventoryItem("a", 3, "item"),
                new InventoryItem("a", 1, "item")), sortInventory(List.of(
                new InventoryItem("a", 3, "item"),
                new InventoryItem("a", 9, "item"),
                new InventoryItem("a", 1, "item"))));

        check("third key breaks complete ties", "same warehouse and quantity", List.of(
                new InventoryItem("a", 5, "alpha"),
                new InventoryItem("a", 5, "beta"),
                new InventoryItem("a", 5, "gamma")), sortInventory(List.of(
                new InventoryItem("a", 5, "gamma"),
                new InventoryItem("a", 5, "alpha"),
                new InventoryItem("a", 5, "beta"))));

        check("source list is unchanged", "items after sorting", originalOrder, items);
        check("empty input", "List.of()", List.of(), sortInventory(List.of()));
        check("single item", "one item", List.of(new InventoryItem("a", 7, "pen")),
                sortInventory(List.of(new InventoryItem("a", 7, "pen"))));
        report("Challenge 111");
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

    /** Records one case whose contract is a condition rather than a value. */
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
