/*
 * Challenge 68: Flatten Nested Data
 * Difficulty: Easy
 *
 * Task: Split every nonblank line on one or more whitespace characters,
 * flatten all words into one stream, lowercase with Locale.ROOT, remove
 * duplicates, sort, and return a list.
 * Complete: static List<String> uniqueWords(List<String> lines)
 * Required focus: flatMap.
 * Run: java FlatMapWords.java
 */
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class FlatMapWords {
    private FlatMapWords() {
    }

    static List<String> uniqueWords(List<String> lines) {
        throw new UnsupportedOperationException("TODO: flatten lines into normalized words");
    }

    public static void main(String[] args) {
        checkEquals(List.of("api", "java", "stream"),
                uniqueWords(List.of(" Java stream ", "STREAM   API", "", "java")),
                "flatten, normalize, and deduplicate");
        checkEquals(List.of("one"), uniqueWords(List.of("one")), "single word");
        checkEquals(List.of(), uniqueWords(List.of("  ", "\t")), "blank lines");
        if (failures > 0) {
            throw new AssertionError("Challenge 68: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 68 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
