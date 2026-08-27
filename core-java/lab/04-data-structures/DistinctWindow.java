/*
 * Challenge 40 — Java Dequeue (Medium)
 *
 * Task: For every contiguous window of a fixed size, count its distinct
 * integers and return the largest count seen.
 * Complete: maxDistinctInWindow(int[] values, int windowSize)
 * Run: java DistinctWindow.java
 */
public class DistinctWindow {
    static int maxDistinctInWindow(int[] values, int windowSize) {
        // TODO: Maintain the current window efficiently as it slides.
        throw new UnsupportedOperationException("TODO: find the best distinct window");
    }

    public static void main(String[] args) {
        checkEquals(3, maxDistinctInWindow(new int[] { 5, 3, 5, 2, 3, 2 }, 3), "mixed windows");
        checkEquals(1, maxDistinctInWindow(new int[] { 7, 7, 7, 7 }, 2), "all duplicates");
        checkEquals(3, maxDistinctInWindow(new int[] { 1, 2, 3 }, 3), "whole array window");
        System.out.println("Challenge 40 passed");
    }

    static void checkEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
