/*
 * Challenge 50: Java Iterator (Easy)
 *
 * Task: Walk a heterogeneous list with an Iterator and collect every element
 * that appears after a marker value.
 * Complete: elementsAfterMarker(List<Object>, Object).
 * Run: java IteratorAfterMarker.java
 */
import java.util.List;

public class IteratorAfterMarker {
    static List<Object> elementsAfterMarker(List<Object> values, Object marker) {
        // TODO: Traverse with Iterator (not indexes) and return elements after marker.
        throw new UnsupportedOperationException("TODO: implement elementsAfterMarker");
    }

    public static void main(String[] args) {
        checkEquals(List.of("hello", "world"),
                elementsAfterMarker(List.of(12, 0, "###", "hello", "world"), "###"),
                "strings after marker");
        checkEquals(List.of(2, 3),
                elementsAfterMarker(List.of(1, "stop", 2, 3), "stop"),
                "numbers after marker");
        checkEquals(List.of(),
                elementsAfterMarker(List.of("only", "marker"), "marker"),
                "marker at end");
        System.out.println("Challenge 50 passed!");
    }

    private static void checkEquals(List<?> expected, List<?> actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
