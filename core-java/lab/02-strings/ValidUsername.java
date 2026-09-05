/*
 * Challenge 23: Valid Username Regular Expression
 * Difficulty: Easy
 *
 * Task: Validate an ASCII username of 6 to 18 characters. It must start with a
 * letter; every remaining character must be a letter, digit, or underscore.
 * Complete: static boolean isValidUsername(String candidate)
 * Run: java ValidUsername.java
 */

import java.util.regex.Pattern;

public final class ValidUsername {
    private ValidUsername() {
    }

    /*
     * Compiled once as a constant: Pattern.compile parses the syntax into a
     * matcher program, and repeating that per call is the usual regex cost sink.
     * A Pattern is immutable and thread-safe; the Matcher it hands out is not,
     * which is why the Matcher is created per call inside the method.
     *
     * [a-zA-Z] and not \p{L}: the contract says ASCII. \p{L} is the Unicode
     * letter category, so it admits "e-acute" and CJK, while \w without the
     * UNICODE_CHARACTER_CLASS flag stays ASCII [a-zA-Z0-9_]. Pairing the two
     * makes the first position Unicode and the rest ASCII, so the same letter
     * is accepted in "emile_1" but rejected in "cafe_7x".
     *
     * A-Z and not A-z: a class range spans code points, not alphabets. A is 65
     * and z is 122, so A-z also covers 91-96 -- [ \ ] ^ _ ` -- which silently
     * readmits the leading underscore this rule exists to reject.
     *
     * {5,17} and not {6,18}: the leading [a-zA-Z] already consumed one
     * character, so the quantifier governs the remainder. 1 + 5..17 = 6..18.
     *
     * To accept every alphabet instead, make BOTH halves Unicode -- the flag
     * widens \w to letters, digits and connector punctuation of any script:
     *
     * Pattern.compile("\\p{L}\\w{5,17}", Pattern.UNICODE_CHARACTER_CLASS);
     *
     * or spell the class out, which needs no flag and is explicit about what
     * "word character" was meant to include:
     *
     * Pattern.compile("\\p{L}[\\p{L}\\p{N}_]{5,17}");
     *
     * Both still reject a leading digit or underscore, since \p{L} anchors the
     * first position to a letter in any script.
     */
    private static final Pattern VALID_USERNAME = Pattern.compile("[a-zA-Z]\\w{5,17}");

    static boolean isValidUsername(String candidate) {
        // matches() requires the whole input to match, so the pattern needs no
        // ^ and $. find() would accept "!!!coder_7!!!" by matching a substring.
        // Anchoring with $ alone would still admit a trailing newline, since $
        // matches before a final line terminator; matches() does not.
        return candidate != null && VALID_USERNAME.matcher(candidate).matches();
    }

    public static void main(String[] args) {
        check("letters underscore and digit", "coder_7", true, isValidUsername("coder_7"));
        check("minimum length", "A12345", true, isValidUsername("A12345"));
        check("must start with a letter", "_coder", false, isValidUsername("_coder"));
        check("too short", "ab12", false, isValidUsername("ab12"));
        check("invalid punctuation", "name-with-dash", false, isValidUsername("name-with-dash"));
        check("too long", "abcdefghijklmnopqrs", false, isValidUsername("abcdefghijklmnopqrs"));
        check("maximum length", "abcdefghijklmnopqr", true, isValidUsername("abcdefghijklmnopqr"));
        check("one below minimum", "A1234", false, isValidUsername("A1234"));
        check("underscore at the end", "coder_", true, isValidUsername("coder_"));
        check("all letters", "abcdef", true, isValidUsername("abcdef"));
        check("digit immediately after the first letter", "a123456", true, isValidUsername("a123456"));
        check("digit first", "1coder", false, isValidUsername("1coder"));
        check("empty", "", false, isValidUsername(""));
        check("single letter", "a", false, isValidUsername("a"));
        check("internal space", "coder 7x", false, isValidUsername("coder 7x"));
        check("leading space", " coder7", false, isValidUsername(" coder7"));
        check("trailing newline", "coder_7\n", false, isValidUsername("coder_7\n"));
        check("non-ASCII start letter", "\u00e9mile_1", false, isValidUsername("\u00e9mile_1"));
        check("non-ASCII letter inside", "caf\u00e9_7x", false, isValidUsername("caf\u00e9_7x"));
        check("null", null, false, isValidUsername(null));
        report("Challenge 23");
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
