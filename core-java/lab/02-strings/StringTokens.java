
/*
 * Challenge 19: Java String Tokens
 * Difficulty: Easy
 *
 * Task: Split text into its non-empty runs of Unicode letters, preserving the
 * original spelling and encounter order.
 * Complete: static List<String> tokenize(String text)
 * Run: java StringTokens.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class StringTokens {
    private StringTokens() {
    }

    private static final Pattern NON_LETTERS = Pattern.compile("\\P{L}+");
    private static final Pattern LETTERS = Pattern.compile("\\p{L}+");

    static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        // --- Approach 1: Code-point loop (O(n), zero regex overhead) ---
        /*
        var tokens = new ArrayList<String>();
        var token = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            if (Character.isLetter(cp)) {
                token.appendCodePoint(cp);
            } else if (!token.isEmpty()) {
                tokens.add(token.toString());
                token.setLength(0);
            }
            i += Character.charCount(cp);
        }
        // Flush the final token if the string ended with a letter rather than a delimiter.
        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }
        return tokens;
        */

        // --- Approach 2: Split on non-letters (\P{L}+) via stream ---
        // return NON_LETTERS.splitAsStream(text)
        //         .filter(s -> !s.isEmpty())
        //         .toList();

        // --- Approach 3: Match letters directly (\p{L}+) via Matcher.results() ---
        return LETTERS.matcher(text)
                .results()
                .map(MatchResult::group)
                .toList();

    }

    /*
     * =========================================================================
     * STUDY NOTES: UNICODE CODE POINTS, NEGATED CATEGORIES & SPLIT ASYMMETRY
     * =========================================================================
     *
     * 1. UNICODE CODE POINTS VS. UTF-16 CHARS (ASTRAL LETTERS)
     *    A "letter" is a Unicode code point, not a 16-bit Java `char`. Java
     *    strings use UTF-16, so supplementary (astral) letters like Deseret '𐐀'
     *    (U+10400) occupy two chars (surrogate pair \uD801\uDC00).
     *    - `Character.isLetter(char)` inspects a single 16-bit unit and returns
     *      `false` for each surrogate half on its own.
     *    - Approach 1 handles astral letters properly by reading full code points
     *      via `codePointAt(i)`, appending via `appendCodePoint(cp)`, and
     *      advancing by `Character.charCount(cp)` (1 or 2 chars).
     *    - Approaches 2 & 3 get this for free because `java.util.regex` matches
     *      `\p{L}` and `\P{L}` per full Unicode code point.
     *
     * 2. LOOP MECHANICS (APPROACH 1)
     *    - Trailing flush: a token still sitting in `StringBuilder` when the loop
     *      reaches the end of the string has no trailing delimiter to trigger the
     *      `else if` branch, so it must be flushed after the `while` loop.
     *    - No `strip()` needed: leading and trailing non-letters are naturally
     *      ignored by the `Character.isLetter(cp)` check.
     *
     * 3. NEGATED UNICODE CATEGORY (\P{L}+) & SPLIT ASYMMETRY (APPROACH 2)
     *    - `\p{L}` matches any Unicode letter; capital `\P{L}` is its negation
     *      (any non-letter code point).
     *    - Why `+` matters: `\P{L}+` collapses runs of adjacent non-letters into
     *      a single delimiter. Without `+`, `"coffee... and"` splits on each dot
     *      individually and emits empty strings between them.
     *    - Why `.filter(s -> !s.isEmpty())` is required: `split` / `splitAsStream`
     *      discards trailing empty fields by default, but keeps a LEADING empty
     *      field when the string starts with a delimiter:
     *        * `"Tea, "`      -> `["Tea"]`      (trailing empty discarded)
     *        * `", Tea"`      -> `["", "Tea"]`  (leading empty kept -> needs filter)
     *        * `"--- 123 ---"` -> `[]`          (all delimiter -> empty stream)
     *
     * 4. SPLITTING ON \P{L}+ (APPROACH 2) VS. MATCHING \p{L}+ (APPROACH 3)
     *    Instead of defining delimiters with `\P{L}+` and filtering out a leading
     *    empty string, Approach 3 matches token runs directly with `\p{L}+` and
     *    streams them via `Matcher.results()`. Because every match of `\p{L}+`
     *    has at least one letter, empty strings can never be produced.
     */

    public static void main(String[] args) {
        check("punctuation delimiters", "Tea, coffee... and cocoa!",
                List.of("Tea", "coffee", "and", "cocoa"), tokenize("Tea, coffee... and cocoa!"));
        check("digits and spaces", "  42 cats + 7 dogs  ",
                List.of("cats", "dogs"), tokenize("  42 cats + 7 dogs  "));
        check("no words", "--- 123 ---",
                List.of(), tokenize("--- 123 ---"));
        check("Unicode letters (BMP)", "naïve/café",
                List.of("naïve", "café"), tokenize("naïve/café"));

        // Supplementary (astral) Unicode letters: U+10400 (\uD801\uDC00) takes 2 UTF-16 chars.
        check("astral Unicode letters (surrogate pairs)", "\uD801\uDC00-\uD801\uDC28",
                List.of("\uD801\uDC00", "\uD801\uDC28"), tokenize("\uD801\uDC00-\uD801\uDC28"));

        // Split asymmetry: leading vs. trailing delimiters.
        check("leading delimiter only", ", Tea",
                List.of("Tea"), tokenize(", Tea"));
        check("trailing delimiter only", "Tea, ",
                List.of("Tea"), tokenize("Tea, "));
        check("single word no delimiters", "hello",
                List.of("hello"), tokenize("hello"));

        // Degenerate inputs.
        check("empty input", "", List.of(), tokenize(""));
        check("null input", null, List.of(), tokenize(null));

        report("Challenge 19");
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