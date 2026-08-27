/*
 * Challenge 29 — Java 2D Array (Easy)
 *
 * Task: Find the greatest hourglass sum in a rectangular integer grid. An
 * hourglass uses three cells, then the center cell, then three cells below.
 * Complete: maxHourglassSum(int[][] grid)
 * Run: java HourglassSum.java
 */
public class HourglassSum {
    static int maxHourglassSum(int[][] grid) {
        // TODO: Examine every 3-by-3 window without assuming sums are positive.
        throw new UnsupportedOperationException("TODO: find the maximum hourglass");
    }

    public static void main(String[] args) {
        checkEquals(35, maxHourglassSum(new int[][] {
            { 1, 2, 3 },
            { 4, 5, 6 },
            { 7, 8, 9 }
        }), "single hourglass");

        checkEquals(-7, maxHourglassSum(new int[][] {
            { -1, -1, -1 },
            { -1, -1, -1 },
            { -1, -1, -1 }
        }), "all-negative grid");

        checkEquals(19, maxHourglassSum(new int[][] {
            { 1, 1, 1, 0, 0, 0 },
            { 0, 1, 0, 0, 0, 0 },
            { 1, 1, 1, 0, 0, 0 },
            { 0, 0, 2, 4, 4, 0 },
            { 0, 0, 0, 2, 0, 0 },
            { 0, 0, 1, 2, 4, 0 }
        }), "multiple candidates");
        System.out.println("Challenge 29 passed");
    }

    static void checkEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
