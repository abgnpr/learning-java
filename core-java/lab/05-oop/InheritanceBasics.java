/*
 * Challenge 43: Java Inheritance I (Easy)
 *
 * Task: Model a bird that inherits the walking behavior of an animal while
 * adding its own flying and singing behaviors.
 * Complete: Change the Bird type so it has the required inheritance relation.
 * Run: java InheritanceBasics.java
 */
public class InheritanceBasics {
    static class Animal {
        public String walk() {
            return "I am walking";
        }
    }

    // TODO: Make Bird inherit from Animal.
    static class Bird {
        public String fly() {
            return "I am flying";
        }

        public String sing() {
            return "I am singing";
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        check(Animal.class.isAssignableFrom(Bird.class),
                "Bird must be a subtype of Animal");
        Object bird = Bird.class.getDeclaredConstructor().newInstance();
        checkEquals("I am walking", Bird.class.getMethod("walk").invoke(bird),
                "Bird must inherit walk()");
        checkEquals("I am flying", Bird.class.getMethod("fly").invoke(bird),
                "Bird must retain fly()");
        checkEquals("I am singing", Bird.class.getMethod("sing").invoke(bird),
                "Bird must retain sing()");
        if (failures > 0) {
            throw new AssertionError("Challenge 43: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 43 passed.");
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
