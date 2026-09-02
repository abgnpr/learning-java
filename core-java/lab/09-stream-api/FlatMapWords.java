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
        System.out.println("Challenge 68 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
