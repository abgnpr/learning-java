/*
 * Challenge 10: Java Static Initializer Block
 * Difficulty: Easy
 *
 * Task: Complete the nested Defaults type's static initialization so its
 * width, height, and area fields hold 7, 5, and 35 respectively.
 * Complete: static void Defaults.initialize()
 * Run: java StaticInitializerBlock.java
 */
public final class StaticInitializerBlock {
    static final class Defaults {
        static int width;
        static int height;
        static int area;

        static {
            initialize();
        }

        private static void initialize() {
            throw new UnsupportedOperationException("TODO: initialize all static fields");
        }
    }

    private StaticInitializerBlock() {
    }

    public static void main(String[] args) {
        checkEquals(7, Defaults.width, "default width");
        checkEquals(5, Defaults.height, "default height");
        checkEquals(35, Defaults.area, "computed area");
        System.out.println("Challenge 10 passed.");
    }

    private static void checkEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
