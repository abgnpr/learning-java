/*
 * Challenge 08: Java Datatypes
 * Difficulty: Easy
 *
 * Task: Given a base-10 whole-number literal, name the smallest signed Java
 * primitive among byte, short, int, and long that can hold it, or return
 * "out-of-range".
 * Complete: static String smallestIntegralType(String literal)
 * Run: java Datatypes.java
 */
public final class Datatypes {
    private Datatypes() {
    }

    static String smallestIntegralType(String literal) {
        throw new UnsupportedOperationException("TODO: compare the value with primitive type ranges");
    }

    public static void main(String[] args) {
        checkEquals("byte", smallestIntegralType("127"), "byte upper boundary");
        checkEquals("short", smallestIntegralType("128"), "first value above byte");
        checkEquals("int", smallestIntegralType("-40000"), "negative int value");
        checkEquals("long", smallestIntegralType("2147483648"), "first value above int");
        checkEquals("out-of-range", smallestIntegralType("9223372036854775808"), "above long");
        System.out.println("Challenge 08 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
