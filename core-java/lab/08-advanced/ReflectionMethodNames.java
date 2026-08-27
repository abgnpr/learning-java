/*
 * Challenge 54: Java Reflection - Attributes (Easy)
 *
 * Task: Inspect a class at runtime and return the names of only the methods it
 * declares, sorted in ascending order.
 * Complete: declaredMethodNames(Class<?>).
 * Run: java ReflectionMethodNames.java
 */
import java.util.List;

public class ReflectionMethodNames {
    static final class Student {
        public String getName() {
            return "Ada";
        }

        public int getId() {
            return 7;
        }

        public String getEmail() {
            return "ada@example.test";
        }
    }

    static class ParentFixture {
        public void inheritedMethod() {
        }
    }

    static final class ChildFixture extends ParentFixture {
        public void ownMethod() {
        }
    }

    static final class EmptyFixture {
    }

    static List<String> declaredMethodNames(Class<?> type) {
        // TODO: Use reflection to collect and sort the declared method names.
        throw new UnsupportedOperationException("TODO: implement declaredMethodNames");
    }

    public static void main(String[] args) {
        checkEquals(List.of("getEmail", "getId", "getName"),
                declaredMethodNames(Student.class), "student methods");
        checkEquals(List.of("ownMethod"), declaredMethodNames(ChildFixture.class),
                "declared methods exclude inherited methods");
        checkEquals(List.of(), declaredMethodNames(EmptyFixture.class), "class with no methods");
        System.out.println("Challenge 54 passed!");
    }

    private static void checkEquals(List<String> expected, List<String> actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
