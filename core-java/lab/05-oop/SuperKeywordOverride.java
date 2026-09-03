/*
 * Challenge 48: Java Method Overriding 2 (Super Keyword) (Easy)
 *
 * Task: Override a vehicle description while preserving and including the
 * parent description through a super call.
 * Complete: Motorcycle.description().
 * Run: java SuperKeywordOverride.java
 */
public class SuperKeywordOverride {
    static class Cycle {
        public String description() {
            return "a vehicle with pedals";
        }
    }

    static final class Motorcycle extends Cycle {
        @Override
        public String description() {
            // TODO: Combine the motorcycle text with super.description().
            throw new UnsupportedOperationException("TODO: implement description");
        }
    }

    public static void main(String[] args) {
        checkEquals("a vehicle with pedals", new Cycle().description(),
                "base description remains available");
        checkEquals("a cycle with an engine; ancestor: a vehicle with pedals",
                new Motorcycle().description(), "subclass description");
        Cycle polymorphic = new Motorcycle();
        checkEquals("a cycle with an engine; ancestor: a vehicle with pedals",
                polymorphic.description(), "polymorphic description");
        if (failures > 0) {
            throw new AssertionError("Challenge 48: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 48 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(String expected, String actual, String message) {
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
