/*
 * Challenge 66: Laziness and Short-Circuiting
 * Difficulty: Easy
 *
 * Task: Find the first word whose length is at least minLength and return it
 * in uppercase. Also return, in encounter order, the words inspected before
 * findFirst short-circuited. Use peek only to make evaluation visible in this
 * learning exercise; do not use peek for production business logic.
 * Complete: static SearchTrace findFirstLongWord(List<String>, int)
 * Required focus: peek, filter, map, findFirst, and lazy evaluation.
 * Run: java LazyFirstMatch.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class LazyFirstMatch {
    record SearchTrace(Optional<String> match, List<String> inspected) {
    }

    private LazyFirstMatch() {
    }

    static SearchTrace findFirstLongWord(List<String> words, int minLength) {
        throw new UnsupportedOperationException("TODO: observe a lazy, short-circuiting pipeline");
    }

    public static void main(String[] args) {
        checkEquals(new SearchTrace(Optional.of("STREAM"), List.of("a", "tool", "stream")),
                findFirstLongWord(List.of("a", "tool", "stream", "pipeline"), 5),
                "evaluation stops after the first match");
        checkEquals(new SearchTrace(Optional.empty(), List.of("a", "bb")),
                findFirstLongWord(List.of("a", "bb"), 3), "all elements inspected without a match");
        checkEquals(new SearchTrace(Optional.empty(), List.of()),
                findFirstLongWord(List.of(), 1), "empty stream");
        System.out.println("Challenge 66 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
