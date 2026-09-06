
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

        // The trap: one predicate cannot answer both questions. Validity asks
        // "is this data sane" (>= 0); availability asks "is the shelf empty"
        // (== 0). A negative count fails the first and is not an answer to the
        // second, so neither of these works for out-of-stock:
        //
        //     .anyMatch(product -> product.stock() < 0)   // asks validity twice
        //     .anyMatch(product -> product.stock() <= 0)  // swallows negatives
        //
        // Empty-stream identities make the empty case need no branch: allMatch
        // is "no counterexample exists" (vacuously true), anyMatch is "a witness
        // exists" (false), and findFirst is "no element" (Optional.empty).
        //
        // findFirst throws NPE on a null element rather than returning empty --
        // Optional cannot hold null, and a null element is not the same as no
        // element. A null product name therefore blows up here by design.
        return new InventoryCheck(
                products.stream().allMatch(product -> product.stock() >= 0),
                products.stream().anyMatch(product -> product.stock() == 0),
                products.stream()
                        .filter(product -> product.stock() == 0)
                        .map(Product::name)
                        .findFirst());
    }

    public static void main(String[] args) {
        check("valid inventory with missing stock", "\"keyboard\", 2",
                new InventoryCheck(true, true, Optional.of("mouse")),
                inspect(List.of(new Product("keyboard", 2), new Product("mouse", 0),
                        new Product("monitor", 0))));
        check("negative stock is invalid but not out of stock", "\"invalid\", -1",
                new InventoryCheck(false, false, Optional.empty()),
                inspect(List.of(new Product("invalid", -1), new Product("cable", 4))));
        check("match identities on an empty stream", "List.of()", new InventoryCheck(true, false, Optional.empty()),
                inspect(List.of()));

        // The two predicates ask different questions: >= 0 is "is this data valid",
        // == 0 is "is the shelf empty". A negative count fails the first and is not
        // an answer to the second, so <= 0 and < 0 are both wrong for out-of-stock.
        check("negative before zero finds the zero, not the negative", "-1 then 0",
                new InventoryCheck(false, true, Optional.of("mouse")),
                inspect(List.of(new Product("bad", -1), new Product("mouse", 0))));
        check("negative alone is invalid with no out-of-stock", "-1",
                new InventoryCheck(false, false, Optional.empty()),
                inspect(List.of(new Product("bad", -1))));
        check("zero alone is valid and out of stock", "0",
                new InventoryCheck(true, true, Optional.of("mouse")),
                inspect(List.of(new Product("mouse", 0))));
        check("stock 1 is neither invalid nor out of stock", "1",
                new InventoryCheck(true, false, Optional.empty()),
                inspect(List.of(new Product("cable", 1))));

        // findFirst is encounter order, not "any zero" — the earliest zero wins even
        // when later ones exist, and a leading in-stock run does not shadow it.
        check("first of several zeros wins", "three zeros",
                new InventoryCheck(true, true, Optional.of("first")),
                inspect(List.of(new Product("first", 0), new Product("second", 0),
                        new Product("third", 0))));
        check("in-stock prefix does not hide a later zero", "2, 5, then 0",
                new InventoryCheck(true, true, Optional.of("last")),
                inspect(List.of(new Product("keyboard", 2), new Product("cable", 5),
                        new Product("last", 0))));

        // allMatch is "no counterexample": one bad row among good ones flips it.
        check("a single negative among valid rows fails validity", "one -1 of four",
                new InventoryCheck(false, true, Optional.of("mouse")),
                inspect(List.of(new Product("keyboard", 2), new Product("mouse", 0),
                        new Product("bad", -3), new Product("monitor", 7))));
        check("all negative is invalid with no zero", "all negative",
                new InventoryCheck(false, false, Optional.empty()),
                inspect(List.of(new Product("a", -1), new Product("b", -2))));

        // Names are taken verbatim by Product::name — no trimming, no null guard.
        check("empty name survives as the found name", "\"\"",
                new InventoryCheck(true, true, Optional.of("")),
                inspect(List.of(new Product("", 0))));
        // findFirst rejects a null element: Product::name maps a null name to null,
        // and findFirst throws rather than handing back an empty Optional.
        boolean threw;
        try {
            inspect(java.util.Collections.singletonList(new Product(null, 0)));
            threw = false;
        } catch (NullPointerException expectedNpe) {
            threw = true;
        }
        checkThat("findFirst throws NPE on a null name", "null name", threw);

        // Extreme counts: the predicates are plain comparisons, no overflow trickery.
        check("Integer.MAX_VALUE is stocked and valid", "Integer.MAX_VALUE",
                new InventoryCheck(true, false, Optional.empty()),
                inspect(List.of(new Product("many", Integer.MAX_VALUE))));
        check("Integer.MIN_VALUE is invalid, not out of stock", "Integer.MIN_VALUE",
                new InventoryCheck(false, false, Optional.empty()),
                inspect(List.of(new Product("broken", Integer.MIN_VALUE))));

        // The empty-stream identities, stated one field at a time.
        InventoryCheck empty = inspect(List.of());
        checkThat("allMatch on empty is vacuously true", "List.of()", empty.allNonNegative());
        checkThat("anyMatch on empty has no witness", "List.of()", !empty.anyOutOfStock());
        checkThat("findFirst on empty is empty, never null", "List.of()",
                empty.firstOutOfStock() != null && empty.firstOutOfStock().isEmpty());

        // anyOutOfStock and firstOutOfStock answer the same question two ways, so
        // they must never disagree, whatever the input.
        List<List<Product>> shapes = List.of(
                List.of(),
                List.of(new Product("a", 3)),
                List.of(new Product("a", 0)),
                List.of(new Product("a", -1)),
                List.of(new Product("a", -1), new Product("b", 0)),
                List.of(new Product("a", 0), new Product("b", -1)),
                List.of(new Product("a", 4), new Product("b", 0), new Product("c", -2)));
        boolean agree = shapes.stream()
                .map(MatchAndFindInventory::inspect)
                .allMatch(result -> result.anyOutOfStock() == result.firstOutOfStock().isPresent());
        checkThat("anyOutOfStock always agrees with firstOutOfStock.isPresent()", "7 shapes", agree);

        // A long stream still reports the earliest zero and does not lose the flag.
        List<Product> many = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Product("P" + i, i % 10))
                .toList();
        InventoryCheck manyResult = inspect(many);
        checkThat("100 products stay valid", "1..100 mod 10", manyResult.allNonNegative());
        checkThat("earliest zero in a long stream is P10", "1..100 mod 10",
                manyResult.firstOutOfStock().equals(Optional.of("P10")));

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
