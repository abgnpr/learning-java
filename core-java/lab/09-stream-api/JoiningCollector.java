/*
 * Challenge 75: Joining Collector
 * Difficulty: Easy
 *
 * Task: Trim tags, discard blank tags, lowercase them with Locale.ROOT, and
 * join them with ", " inside square brackets. Preserve encounter order and
 * duplicates. Empty input must produce "[]".
 * Complete: static String formatTags(List<String> tags)
 * Required focus: Collectors.joining(delimiter, prefix, suffix).
 * Run: java JoiningCollector.java
 */
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class JoiningCollector {
    private JoiningCollector() {
    }

    static String formatTags(List<String> tags) {
        throw new UnsupportedOperationException("TODO: normalize and join tags");
    }

    public static void main(String[] args) {
        checkEquals("[java, streams, java]", formatTags(List.of(" Java ", "", "STREAMS", "java")),
                "normalized tags");
        checkEquals("[one]", formatTags(List.of("one")), "single tag");
        checkEquals("[]", formatTags(List.of(" ", "\t")), "no usable tags");
        System.out.println("Challenge 75 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
