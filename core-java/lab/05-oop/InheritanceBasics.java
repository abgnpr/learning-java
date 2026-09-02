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
        System.out.println("Challenge 43 passed!");
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
