/*
 * Challenge 37 — Java Generics (Easy)
 *
 * Task: Write one type-safe method that prints every element of any reference
 * type array on its own line, without overloading it for individual types.
 * Complete: <T> printArray(T[] values, PrintStream out)
 * Run: java GenericArrayPrinter.java
 */
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class GenericArrayPrinter {
    static <T> void printArray(T[] values, PrintStream out) {
        // TODO: Implement this once using a method type parameter.
        throw new UnsupportedOperationException("TODO: print a generic array");
    }

    public static void main(String[] args) {
        checkEquals(lines("1", "2", "3"), render(new Integer[] { 1, 2, 3 }), "integer array");
        checkEquals(lines("Hello", "World"), render(new String[] { "Hello", "World" }), "string array");
        checkEquals(lines("A", "z"), render(new Character[] { 'A', 'z' }), "character array");
        System.out.println("Challenge 37 passed");
    }

    static <T> String render(T[] values) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            printArray(values, out);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    static String lines(String... values) {
        return String.join(System.lineSeparator(), values) + System.lineSeparator();
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
