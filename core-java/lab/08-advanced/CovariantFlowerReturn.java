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
        if (failures > 0) {
            throw new AssertionError("Challenge 61: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 61 passed.");
    }

    private static int failures = 0;

    private static void check(boolean condition, String message) {
        if (condition) {
            System.out.println("PASS " + message);
            return;
        }
        failures++;
        System.out.println("FAIL " + message);
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + message + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
