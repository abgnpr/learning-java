/*
 * Challenge 61: Covariant Return Types (Easy)
 *
 * Task: Specialize a region's flower method so its override returns the more
 * precise Jasmine subtype while remaining usable through a Region reference.
 * Complete: WestBengal.nationalFlower().
 * Run: java CovariantFlowerReturn.java
 */
public class CovariantFlowerReturn {
    static class Flower {
        public String name() {
            return "Flower";
        }
    }

    static final class Jasmine extends Flower {
        @Override
        public String name() {
            return "Jasmine";
        }
    }

    static class Region {
        public Flower nationalFlower() {
            return new Flower();
        }
    }

    static final class WestBengal extends Region {
        @Override
        public Jasmine nationalFlower() {
            // TODO: Return the region's flower using the covariant subtype.
            throw new UnsupportedOperationException("TODO: implement nationalFlower");
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        checkEquals(Jasmine.class,
                WestBengal.class.getDeclaredMethod("nationalFlower").getReturnType(),
                "override return type");
        Flower direct = new WestBengal().nationalFlower();
        check(direct instanceof Jasmine, "direct call must return Jasmine");
        Region polymorphic = new WestBengal();
        checkEquals("Jasmine", polymorphic.nationalFlower().name(),
                "covariant result through base reference");
        System.out.println("Challenge 61 passed!");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
