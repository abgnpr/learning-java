/*
 * Challenge 71: Matching and Finding
 * Difficulty: Easy
 *
 * Task: Inspect an inventory using stream terminal operations. Report whether
 * every stock count is nonnegative, whether any product is out of stock, and
 * the name of the first out-of-stock product in encounter order.
 * Complete: static InventoryCheck inspect(List<Product> products)
 * Required focus: allMatch, anyMatch, findFirst, and empty-stream semantics.
 * Run: java MatchAndFindInventory.java
 */
import java.util.List;
import java.util.Optional;

public final class MatchAndFindInventory {
    record Product(String name, int stock) {
    }

    record InventoryCheck(boolean allNonNegative, boolean anyOutOfStock,
            Optional<String> firstOutOfStock) {
    }

    private MatchAndFindInventory() {
    }

    static InventoryCheck inspect(List<Product> products) {
        throw new UnsupportedOperationException("TODO: query the inventory with stream terminals");
    }

    public static void main(String[] args) {
        check("valid inventory with missing stock", "\"keyboard\", 2", new InventoryCheck(true, true, Optional.of("mouse")), inspect(List.of(new Product("keyboard", 2), new Product("mouse", 0),
                        new Product("monitor", 0))));
        check("negative stock is invalid but not out of stock", "\"invalid\", -1", new InventoryCheck(false, false, Optional.empty()), inspect(List.of(new Product("invalid", -1), new Product("cable", 4))));
        check("match identities on an empty stream", "List.of()", new InventoryCheck(true, false, Optional.empty()), inspect(List.of()));
        report("Challenge 71");
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
