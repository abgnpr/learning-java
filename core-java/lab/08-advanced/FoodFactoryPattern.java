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
        checkType(Pizza.class, "Pizza", factory.createFood("pizza"), "pizza order");
        checkType(Cake.class, "Cake", factory.createFood("CAKE"), "case-insensitive order");
        checkType(Pizza.class, "Pizza", factory.createFood("  Pizza  "), "trimmed order");
        System.out.println("Challenge 57 passed!");
    }

    private static void checkType(Class<?> expectedClass, String expectedType,
            Food actual, String message) {
        if (actual == null || actual.getClass() != expectedClass
                || !expectedType.equals(actual.type())) {
            throw new AssertionError(message + ": expected " + expectedClass.getSimpleName());
        }
    }
}
