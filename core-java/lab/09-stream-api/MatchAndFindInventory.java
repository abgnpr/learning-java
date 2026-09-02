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
        checkEquals(new InventoryCheck(true, true, Optional.of("mouse")),
                inspect(List.of(new Product("keyboard", 2), new Product("mouse", 0),
                        new Product("monitor", 0))),
                "valid inventory with missing stock");
        checkEquals(new InventoryCheck(false, false, Optional.empty()),
                inspect(List.of(new Product("invalid", -1), new Product("cable", 4))),
                "negative stock is invalid but not out of stock");
        checkEquals(new InventoryCheck(true, false, Optional.empty()), inspect(List.of()),
                "match identities on an empty stream");
        System.out.println("Challenge 71 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
