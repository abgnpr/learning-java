/*
 * Challenge 30 — Java Subarray (Easy)
 *
 * Task: Count the contiguous, non-empty slices of an integer array whose
 * element sum is negative.
 * Complete: countNegativeSubarrays(int[] values)
 * Run: java NegativeSubarrayCount.java
 */
public class NegativeSubarrayCount {
    static long countNegativeSubarrays(int[] values) {
        // TODO: Consider every valid start/end pair and its sum.
        throw new UnsupportedOperationException("TODO: count negative subarrays");
    }

    public static void main(String[] args) {
        checkEquals(9L, countNegativeSubarrays(new int[] { 1, -2, 4, -5, 1 }), "mixed sample");
        checkEquals(0L, countNegativeSubarrays(new int[] { 1, 2, 3 }), "no negative sum");
        checkEquals(3L, countNegativeSubarrays(new int[] { -1, -1 }), "all slices are negative");
        if (failures > 0) {
            throw new AssertionError("Challenge 30: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 30 passed.");
    }

    private static int failures = 0;

    static void checkEquals(long expected, long actual, String label) {
        if (expected == actual) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
