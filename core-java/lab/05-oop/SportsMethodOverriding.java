/*
 * Challenge 47: Java Method Overriding (Easy)
 *
 * Task: Specialize a general sport so dynamic dispatch reports the number of
 * players on a soccer team.
 * Complete: Soccer.teamSize().
 * Run: java SportsMethodOverriding.java
 */
public class SportsMethodOverriding {
    static class Sport {
        public String name() {
            return "Generic Sports";
        }

        public int teamSize() {
            return 0;
        }
    }

    static final class Soccer extends Sport {
        @Override
        public String name() {
            return "Soccer";
        }

        @Override
        public int teamSize() {
            // TODO: Override the general rule with soccer's team size.
            throw new UnsupportedOperationException("TODO: implement teamSize");
        }
    }

    public static void main(String[] args) {
        Sport sport = new Soccer();
        checkEquals("Soccer", sport.name(), "overridden name via base reference");
        checkEquals(11, sport.teamSize(), "soccer team size via dynamic dispatch");
        checkEquals(11, new Soccer().teamSize(), "soccer team size via subtype reference");
        System.out.println("Challenge 47 passed!");
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
