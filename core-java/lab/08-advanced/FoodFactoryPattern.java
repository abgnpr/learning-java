/*
 * Challenge 57: Java Factory Pattern (Easy)
 *
 * Task: Centralize food creation in a factory that maps trimmed,
 * case-insensitive orders to the matching Pizza or Cake product. Every
 * supplied order normalizes to "pizza" or "cake".
 * Complete: FoodFactory.createFood(String).
 * Run: java FoodFactoryPattern.java
 */
public class FoodFactoryPattern {
    interface Food {
        String type();
    }

    static final class Pizza implements Food {
        @Override
        public String type() {
            return "Pizza";
        }
    }

    static final class Cake implements Food {
        @Override
        public String type() {
            return "Cake";
        }
    }

    static final class FoodFactory {
        public Food createFood(String order) {
            // TODO: Return the product selected by the normalized order.
            throw new UnsupportedOperationException("TODO: implement createFood");
        }
    }

    public static void main(String[] args) {
        FoodFactory factory = new FoodFactory();
        check("pizza order", "factory.createFood(\"pizza\")", Pizza.class.getSimpleName() + " with type " + "Pizza", describe(factory.createFood("pizza")));
        check("case-insensitive order", "factory.createFood(\"CAKE\")", Cake.class.getSimpleName() + " with type " + "Cake", describe(factory.createFood("CAKE")));
        check("trimmed order", "factory.createFood(\" Pizza \")", Pizza.class.getSimpleName() + " with type " + "Pizza", describe(factory.createFood("  Pizza  ")));
        report("Challenge 57");
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

    /** Names an object by its concrete class and declared type. */
    private static String describe(Food value) {
        return value == null
                ? "null"
                : value.getClass().getSimpleName() + " with type " + value.type();
    }
}
