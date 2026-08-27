/*
 * Challenge 28 — Java 1D Array (Easy)
 *
 * Task: Store a sequence of integers in a primitive one-dimensional array,
 * preserving both its length and element order.
 * Complete: copyIntoArray(List<Integer> values)
 * Run: java OneDimensionalArray.java
 */
import java.util.List;

public class OneDimensionalArray {
    static int[] copyIntoArray(List<Integer> values) {
        // TODO: Allocate and populate the primitive array.
        throw new UnsupportedOperationException("TODO: build the array");
    }

    public static void main(String[] args) {
        checkArray(new int[] { }, copyIntoArray(List.of()), "empty input");
        checkArray(new int[] { 7 }, copyIntoArray(List.of(7)), "one value");
        checkArray(new int[] { 1, -2, 3, 0 }, copyIntoArray(List.of(1, -2, 3, 0)), "ordered values");
        System.out.println("Challenge 28 passed");
    }

    static void checkArray(int[] expected, int[] actual, String label) {
        if (!java.util.Arrays.equals(expected, actual)) {
            throw new AssertionError(label + ": expected "
                + java.util.Arrays.toString(expected) + ", got " + java.util.Arrays.toString(actual));
        }
    }
}
