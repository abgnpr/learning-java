/*
 * Challenge 58: Java Singleton Pattern (Easy)
 *
 * Task: Finish a lazily created singleton so sequential callers receive one
 * shared object and state written through one reference is visible through
 * another. Thread-safe initialization is outside this exercise's scope.
 * Complete: Singleton.getInstance().
 * Run: java SingletonPattern.java
 */
import java.lang.reflect.Modifier;

public class SingletonPattern {
    static final class Singleton {
        private static Singleton instance;
        private String message;

        private Singleton() {
        }

        public static Singleton getInstance() {
            // TODO: Lazily create and return the single shared instance.
            throw new UnsupportedOperationException("TODO: implement getInstance");
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Singleton first = Singleton.getInstance();
        Singleton second = Singleton.getInstance();
        check(first != null, "getInstance must not return null");
        check(first == second, "both calls must return the same object");
        first.setMessage("shared");
        checkEquals("shared", second.getMessage(), "singleton state must be shared");
        check(Modifier.isPrivate(Singleton.class.getDeclaredConstructor().getModifiers()),
                "constructor must remain private");
        if (failures > 0) {
            throw new AssertionError("Challenge 58: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 58 passed.");
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
