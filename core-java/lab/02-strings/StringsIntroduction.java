
import java.util.Locale;

/*
 * Challenge 14: Java Strings Introduction
 * Difficulty: Easy
 *
 * Task: For two non-empty ASCII strings, return a Summary containing their
 * combined length, whether the first sorts after the second using
 * case-sensitive String.compareTo, and the pair with both first letters
 * capitalized.
 * Complete: static Summary analyze(String first, String second)
 * Run: java StringsIntroduction.java
 */
public final class StringsIntroduction {
    record Summary(int combinedLength, boolean firstAfterSecond, String capitalizedPair) {
    }

    private StringsIntroduction() {
    }

    static Summary analyze(String first, String second) {
        var combinedLength = first.length() + second.length();

        // compareTo() is lexicographic and case-sensitive; its exact positive
        // value is unspecified, so test its sign rather than comparing to 1.
        var firstAfterSecond = first.compareTo(second) > 0;

        // Locale.ROOT keeps case mapping independent of the machine (for
        // example, a Turkish default locale changes ASCII "i"). substring(0, 1)
        // is safe because the contract excludes empty strings; each remainder
        // must come from its own string, or unequal lengths truncate or overread.
        var capitalizedPair = first.substring(0, 1).toUpperCase(Locale.ROOT)
                + first.substring(1)
                + " "
                + second.substring(0, 1).toUpperCase(Locale.ROOT)
                + second.substring(1);
        return new Summary(combinedLength, firstAfterSecond, capitalizedPair);
    }

    public static void main(String[] args) {
        checkEquals(new Summary(8, true, "Java Code"), analyze("java", "code"), "first sorts after");
        checkEquals(new Summary(10, false, "Hello World"), analyze("hello", "world"), "first sorts before");
        checkEquals(new Summary(2, true, "A B"), analyze("a", "B"), "case-sensitive ordering");
        checkEquals(new Summary(7, false, "Hi World"), analyze("hi", "world"), "second string is longer");
        checkEquals(new Summary(7, true, "Hello Go"), analyze("hello", "go"), "second string is shorter");
        if (failures > 0) {
            throw new AssertionError("Challenge 14: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 14 passed.");
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
