/*
 * Challenge 24: Tag Content Extractor
 * Difficulty: Medium
 *
 * Task: From flat, non-nested markup, collect non-empty plain text enclosed by
 * matching tags from left to right. Tag names contain ASCII letters, tags have
 * no attributes, and text contains no angle brackets. Ignore mismatches and
 * empty content.
 * Complete: static List<String> extractText(String markup)
 * Run: java TagContentExtractor.java
 */
import java.util.List;

public final class TagContentExtractor {
    private TagContentExtractor() {
    }

    static List<String> extractText(String markup) {
        throw new UnsupportedOperationException("TODO: capture text between matching tag names");
    }

    public static void main(String[] args) {
        checkEquals(List.of("alpha", "beta"), extractText("<p>alpha</p><b>beta</b>"), "two tags");
        checkEquals(List.of("two"), extractText("<x>one</y><z>two</z>"), "mismatched tag ignored");
        checkEquals(List.of(), extractText("plain text"), "no tags");
        checkEquals(List.of("ok"), extractText("<a></a><a>ok</a>"), "empty content ignored");
        if (failures > 0) {
            throw new AssertionError("Challenge 24: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 24 passed.");
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
