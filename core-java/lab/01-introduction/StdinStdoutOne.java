
import java.util.Arrays;
import java.util.stream.Collectors;

/*
 * Challenge 02: Java Stdin and Stdout I
 * Difficulty: Easy
 *
 * Task: Parse every whitespace-separated integer and return one normalized
 * base-10 integer per line in a single String. Do not print inside this method.
 * Complete: static String echoIntegers(String input)
 * Run: java StdinStdoutOne.java
 */
public final class StdinStdoutOne {
    private StdinStdoutOne() {
    }

    static String echoIntegers(String input) {

        // classic way
        // if (input.isBlank()) {
        // return "";
        // }
        // String[] result = input.strip().split("\\s+");
        // for (int i = 0; i < result.length; i++) {
        // result[i] = String.valueOf(Integer.parseInt(result[i]));
        // }
        // return String.join("\n", result);

        // streams way
        // return Arrays.stream(input.strip().split("\\s+"))
        // .filter(s -> !s.isEmpty())
        // .map(Integer::parseInt)
        // .map(String::valueOf)
        // .collect(Collectors.joining("\n"));

        // best way
        if (input.isBlank()) {
            return "";
        }
        return Arrays.stream(input.strip().split("\\s+"))
                .map(Integer::parseInt)
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        checkEquals("7\n11\n-3", echoIntegers("7 11 -3"), "space-separated input");
        checkEquals("4\n5\n6", echoIntegers("  4\n5\t6  "), "mixed whitespace");
        checkEquals("7\n4\n0", echoIntegers("007 +4 -0"), "integer normalization");
        checkEquals("", echoIntegers(" \n\t "), "empty input");
        System.out.println("Challenge passed.");
    }

    private static void checkEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
