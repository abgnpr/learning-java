/*
 * Challenge 32 — Java 1D Array (Part 2) (Medium)
 *
 * Task: Starting at index zero in a non-empty 0/1 game array, decide whether
 * moves of one step backward, one step forward, or a positive fixed leap can
 * reach past the end. The first element is zero, and a move within the array
 * may land only on a zero.
 * Complete: canEscape(int leap, int[] game)
 * Run: java LeapGame.java
 */
public class LeapGame {
    static boolean canEscape(int leap, int[] game) {
        // TODO: Explore reachable positions without cycling forever.
        throw new UnsupportedOperationException("TODO: solve the leap game");
    }

    public static void main(String[] args) {
        check(true, canEscape(3, new int[] { 0, 0, 0, 0, 0 }), "open path");
        check(true, canEscape(5, new int[] { 0, 0, 0, 1, 1, 1 }), "leap beyond the end");
        check(false, canEscape(3, new int[] { 0, 0, 0, 1, 1, 1 }), "blocked path");
        check(false, canEscape(1, new int[] { 0, 1, 0 }), "blocked immediately");
        check(true, canEscape(3, new int[] { 0, 1, 0, 0, 1, 0, 1 }),
            "escape route requires a backward move");
        System.out.println("Challenge 32 passed");
    }

    static void check(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
