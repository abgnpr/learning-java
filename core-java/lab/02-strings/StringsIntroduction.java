
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
        check("first sorts after", "\"java\", \"code\"", new Summary(8, true, "Java Code"), analyze("java", "code"));
        check("first sorts before", "\"hello\", \"world\"", new Summary(10, false, "Hello World"), analyze("hello", "world"));
        check("case-sensitive ordering", "\"a\", \"B\"", new Summary(2, true, "A B"), analyze("a", "B"));
        check("second string is longer", "\"hi\", \"world\"", new Summary(7, false, "Hi World"), analyze("hi", "world"));
        check("second string is shorter", "\"hello\", \"go\"", new Summary(7, true, "Hello Go"), analyze("hello", "go"));
        report("Challenge 14");
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

    /** Records one case. Prints input, expected and actual so a failure is diagnosable. */
    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    /** Records a case whose contract is a condition rather than a value. */
    private static void checkThat(String label, Object input, boolean condition) {
        if (condition) {
            passes++;
        } else {
            failures++;
        }
        System.out.println((condition ? "PASS  " : "FAIL  ") + label);
        if (input != null) {
            System.out.println("      input:    " + show(input));
        }
        System.out.println("      expected: " + "condition holds");
        System.out.println("      actual:   " + (condition ? "holds" : "does not hold"));
    }

    /**
     * Renders a value on one line so line breaks and trailing spaces stay visible:
     * every line is wrapped in <> and the breaks between them are shown as \n.
     * Non-strings carry their type, so <12> the text and 12 the int never look alike.
     */
    private static String show(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Object[] array) {
            return java.util.Arrays.deepToString(array);
        }
        if (value.getClass().isArray()) {
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
        String[] lines = s.split("\n", -1);
        var sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\\n");
            }
            sb.append('<').append(lines[i].replace("\r", "\\r")).append('>');
        }
        return sb.toString();
    }

    /** Prints the tally and fails the run if any case failed. */
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
