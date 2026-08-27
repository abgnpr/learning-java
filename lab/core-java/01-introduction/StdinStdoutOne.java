/*
 * Challenge 02: Java Stdin and Stdout I
 * Difficulty: Easy
 *
 * Task: Read every whitespace-separated integer and echo one integer per line.
 * Complete: static String echoIntegers(String input)
 * Run: java StdinStdoutOne.java
 */
public final class StdinStdoutOne {
    private StdinStdoutOne() {
    }

    static String echoIntegers(String input) {
        throw new UnsupportedOperationException("TODO: parse and echo the integers");
    }

    public static void main(String[] args) {
        checkEquals("7\n11\n-3", echoIntegers("7 11 -3"), "space-separated input");
        checkEquals("4\n5\n6", echoIntegers("  4\n5\t6  "), "mixed whitespace");
        checkEquals("7\n4\n0", echoIntegers("007 +4 -0"), "integer normalization");
        checkEquals("", echoIntegers(" \n\t "), "empty input");
        System.out.println("Challenge 02 passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
