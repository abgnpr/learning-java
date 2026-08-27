/*
 * Challenge 44: Java Inheritance II (Easy)
 *
 * Task: Create an adder by inheriting the reusable addition operation from an
 * arithmetic base type instead of duplicating it.
 * Complete: Change the Adder type so it extends Arithmetic.
 * Run: java ArithmeticInheritance.java
 */
public class ArithmeticInheritance {
    static class Arithmetic {
        public int add(int left, int right) {
            return left + right;
        }
    }

    // TODO: Make Adder inherit from Arithmetic.
    static class Adder {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        check(Arithmetic.class.isAssignableFrom(Adder.class),
                "Adder must be a subtype of Arithmetic");
        checkEquals(7, invokeAdd(new Adder(), 2, 5), "positive operands");
        checkEquals(-3, invokeAdd(new Adder(), -8, 5), "mixed-sign operands");
        checkEquals(0, invokeAdd(new Adder(), 0, 0), "zero operands");
        System.out.println("Challenge 44 passed!");
    }

    private static int invokeAdd(Object target, int left, int right)
            throws ReflectiveOperationException {
        return (int) target.getClass().getMethod("add", int.class, int.class)
                .invoke(target, left, right);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
