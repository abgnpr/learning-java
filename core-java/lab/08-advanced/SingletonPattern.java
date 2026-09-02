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
        System.out.println("Challenge 58 passed!");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
