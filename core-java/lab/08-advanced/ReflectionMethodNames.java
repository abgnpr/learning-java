/*
 * Challenge 54: Java Reflection - Attributes (Easy)
 *
 * Task: Inspect a class at runtime and return the name of every method it
 * declares, sorted in ascending order. Exclude inherited methods; include one
 * entry per declared overload, so a name may appear more than once.
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
        if (failures > 0) {
            throw new AssertionError("Challenge 54: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 54 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(List<String> expected, List<String> actual, String message) {
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
