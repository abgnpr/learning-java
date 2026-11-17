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

    private static final Pattern VALID_USERNAME = Pattern.compile("[a-zA-Z]\\w{5,17}");

    static boolean isValidUsername(String candidate) {
        if (candidate == null) {
            return false;
        }

        // --- Approach 1: Procedural character scan (O(n), zero regex overhead) ---
        /*
        int len = candidate.length();
        if (len < 6 || len > 18 || !isAsciiLetter(candidate.charAt(0))) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            char c = candidate.charAt(i);
            if (!isAsciiLetter(c) && !(c >= '0' && c <= '9') && c != '_') {
                return false;
            }
        }
        return true;
        */

        // --- Approach 2: Inline String.matches (recompiles Pattern on every call) ---
        // return candidate.matches("[a-zA-Z]\\w{5,17}");

        // --- Approach 3: Hoisted Pattern + Matcher.matches (compiles once) ---
        return VALID_USERNAME.matcher(candidate).matches();
    }

    // private static boolean isAsciiLetter(char c) {
    //     return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    // }

    /*
     * =========================================================================
     * STUDY NOTES: CHARACTER RANGES, ANCHORING & UNICODE FLAGS
     * =========================================================================
     *
     * 1. ANATOMY OF THE PATTERN: [a-zA-Z]\w{5,17}
     *    - [a-zA-Z]:  First character must be an ASCII letter (consumes 1 char).
     *    - \w{5,17}:  Remaining 5 to 17 characters must be word characters
     *                 ([a-zA-Z0-9_]). Total length: 1 + (5..17) = 6..18.
     *
     * 2. THE [A-z] ASCII RANGE TRAP
     *    Use [a-zA-Z], never [A-z]. Character class ranges span numeric code
     *    points, not alphabets:
     *    - 'A' is 65, 'Z' is 90, 'a' is 97, and 'z' is 122.
     *    - [A-z] includes code points 91..96: [ \ ] ^ _ `
     *    - Using [A-z] on the first character silently admits "_coder" (95) and
     *      "[coder" (91). Using [A-z0-9_] on the tail silently admits "coder^7"
     *      (94) and "coder`7" (96).
     *
     * 3. MATCHER.MATCHES() VS. FIND() AND ANCHORS (^ / $ VS. \A / \z)
     *    - matches() requires the ENTIRE input string to match the pattern
     *      (implicitly anchored with \A at the start and \z at the absolute end),
     *      so ^ and $ are unnecessary.
     *    - find() searches for a matching substring anywhere in the input, so it
     *      would accept "!!!coder_7!!!".
     *    - Even with ^ and $, find() on "^[a-zA-Z]\\w{5,17}$" would still accept
     *      "coder_7\n" because $ matches before a trailing line terminator (\Z).
     *      matches() rejects trailing newlines automatically.
     *
     * 4. ASCII VS. UNICODE (\p{L}, \w, AND UNICODE_CHARACTER_CLASS)
     *    - Why [a-zA-Z] instead of \p{L}: the contract specifies ASCII. \p{L}
     *      matches any Unicode letter (e.g., 'é' or CJK characters), whereas \w
     *      without flags stays strict ASCII [a-zA-Z0-9_].
     *    - Mixing them (\p{L}\w{5,17}) creates an inconsistent rule where the
     *      first character allows Unicode ("émile_1" passes) but the rest is
     *      restricted to ASCII ("café_7x" fails).
     *    - To support Unicode usernames consistently across all positions, either
     *      enable UNICODE_CHARACTER_CLASS (or inline (?U)) so \w widens to
     *      Unicode letters, digits, and connector punctuation:
     *          Pattern.compile("\\p{L}\\w{5,17}", Pattern.UNICODE_CHARACTER_CLASS)
     *      or spell out the Unicode categories explicitly without flags:
     *          Pattern.compile("\\p{L}[\\p{L}\\p{N}_]{5,17}")
     *
     * 5. HOISTED PATTERN VS. PER-CALL MATCHER (THREAD SAFETY)
     *    - Pattern.compile() parses the regex into an immutable, thread-safe
     *      automaton once at class load. Calling String.matches() recompiles
     *      that automaton on every invocation.
     *    - Matcher holds mutable cursor state (start/end indices, groups) and is
     *      NOT thread-safe, which is why VALID_USERNAME.matcher(candidate) is
     *      instantiated per call inside the method (a lightweight allocation).
     */

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

        // [A-z] ASCII range trap (code points 91..96: [ \ ] ^ _ ` lie between 'Z' and 'a').
        // Catches [A-z] at index 0 and [A-z0-9_] in positions 1..17:
        check("[A-z] trap: leading bracket (91)", "[coder", false, isValidUsername("[coder"));
        check("[A-z] trap: leading caret (94)", "^coder", false, isValidUsername("^coder"));
        check("[A-z] trap: leading backtick (96)", "`coder", false, isValidUsername("`coder"));
        check("[A-z] trap: internal bracket (91)", "coder[7", false, isValidUsername("coder[7"));
        check("[A-z] trap: internal backslash (92)", "coder\\7", false, isValidUsername("coder\\7"));
        check("[A-z] trap: internal caret (94)", "coder^7", false, isValidUsername("coder^7"));
        check("[A-z] trap: internal backtick (96)", "coder`7", false, isValidUsername("coder`7"));

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