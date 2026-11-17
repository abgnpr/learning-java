import java.util.regex.Pattern;

/*
 * Challenge 22: Java Regex 2 - Duplicate Words
 * Difficulty: Medium
 *
 * Task: Collapse each run of adjacent duplicate ASCII-letter words
 * case-insensitively, preserving the spelling of the run's first word. Input
 * words are separated by single spaces, as is the returned text.
 * Complete: static String removeAdjacentDuplicates(String text)
 * Run: java DuplicateWords.java
 */
public final class DuplicateWords {
    private DuplicateWords() {
    }

    private static final Pattern DUPLICATE_RUN = Pattern.compile(
            "\\b(\\w+)(\\s+\\1\\b)+", Pattern.CASE_INSENSITIVE);

    static String removeAdjacentDuplicates(String text) {
        if (text == null) {
            return "";
        }
        // strip() prevents leading spaces from making split() emit an empty first token.
        text = text.strip();

        // --- Approach 1: Split and walk (O(n), no backtracking) ---
        /*
        String[] words = text.split("\\s+"); // Returns [""] on empty string; loop simply skips.
        String prev = words[0];
        StringBuilder result = new StringBuilder(prev);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            // Compare against the last word KEPT (`prev` only updates on mismatch),
            // preserving the exact spelling/casing of the run's opening word.
            if (!prev.equalsIgnoreCase(word)) {
                result.append(" ").append(word);
                prev = word;
            }
        }
        return result.toString();
        */

        // --- Approach 2: Inline regex (recompiles Pattern on every call) ---
        // return text.replaceAll("(?i)\\b(\\w+)(\\s+\\1\\b)+", "$1");

        // --- Approach 3: Hoisted Pattern (compiles once at class load) ---
        return DUPLICATE_RUN.matcher(text).replaceAll("$1");
    }

    /*
     * =========================================================================
     * STUDY NOTES: REGEX BACK-REFERENCES & ENGINE BEHAVIOR
     * =========================================================================
     *
     * 1. ANATOMY OF THE PATTERN: (?i)\b(\w+)(\s+\1\b)+  -->  $1
     *
     * - Back-references (\1 vs $1):
     *   \1 is NOT "match \w+ again". A capturing group remembers the exact text
     *   it matched, and \1 demands those literal characters back. (\w+)\s+\1
     *   matches "go go" and fails on "go home".
     *   \1 and $1 name the same group in two different languages: \1 in the
     *   PATTERN, $1 in the REPLACEMENT template. Swapping them breaks both ($1
     *   in a pattern is end-of-line $ + "1"; \1 in a replacement is literal \1,
     *   and a bare $ throws IllegalArgumentException: "group index is missing"
     *   instead of matching a literal dollar, which is spelled \$).
     *
     * - Quantifying the repeating unit ((\s+\1\b)+):
     *   The outer + collapses runs of 3+ words. Without it, the pattern matches
     *   only a pair, and replaceAll resumes scanning AFTER the match without
     *   backing up: "go go go" matches "go go" -> writes "go" -> restarts at
     *   trailing " go" with nothing left to pair -> returns "go go". Grouping
     *   whitespace + \1 and quantifying the group is the same move as
     *   (octet\.){3} in RegexIpv4.
     *   Inside the group, \s+ matches one or more whitespace characters (spaces,
     *   tabs, newlines). Using a literal space ( \1\b)+ would only collapse
     *   duplicates separated by a single space and ignore "go  go".
     *
     * - Word boundaries (\b):
     *   \b is zero-width (like ^ and \z), asserting a boundary between \w and \W.
     *   Both \b anchors in \b(\w+)(\s+\1\b)+ are load-bearing:
     *   1) Leading \b on (\w+): stops (\w+) from starting mid-word on a suffix
     *      (without it, "the he" matches "he he" starting inside "the" -> "the").
     *   2) Trailing \b on \1: stops \1 from matching a prefix of a longer word
     *      (without it, "go gone" matches "go go" inside "gone" -> "gone").
     *   3) No \b is needed between \s+ and \1: because \1 matched \w+, its
     *      first character is already a word character following whitespace.
     *
     * - Case-insensitivity ((?i) vs Pattern.CASE_INSENSITIVE):
     *   (?i) is an inline flag, not a group (the ? marks it non-capturing, leaving
     *   (\w+) as group 1). Placed at index 0, it covers the whole pattern including
     *   \1, making "Go"/"go"/"GO" one run. (Scoped forms: (?i:X) or (?-i)).
     *   String.replaceAll takes no flags argument, which is why (?i) is needed
     *   inline; Pattern.compile takes Pattern.CASE_INSENSITIVE as a second arg,
     *   and hides the chain Pattern -> matcher(input) -> replaceAll(replacement).
     *
     * 2. WHICH APPROACH TO SHIP (AND WHITESPACE NUANCE)
     *   Ship Approach 1 (the loop) when requirements allow: it is guaranteed O(n)
     *   with zero backtracking.
     *   Even with \s+ in the regex, the two approaches differ when NON-duplicate
     *   words are separated by multiple spaces:
     *   - Regex ((\s+\1\b)+): Only replaces matched duplicate runs, so
     *     "go   go   home" becomes "go   home" (the three spaces before "home"
     *     are untouched).
     *   - Loop (split("\\s+")): Splits across all whitespace and joins with " ",
     *     normalizing "go   go   home" to "go home".
     *   Also note \w is [a-zA-Z0-9_], which is wider than "ASCII-letter words"
     *   (so "9 9" collapses too).
     *
     * 3. CATASTROPHIC BACKTRACKING & WHY THIS PATTERN IS SAFE
     *   A backtracking engine tries every alternative before failing. When a
     *   quantified group wraps another quantifier — (X|Y+)+, (a+)+, (a*)*b —
     *   input can be partitioned between inner and outer quantifiers in
     *   exponentially many ways. Matching input returns early, while non-matching
     *   input explores all paths, creating a ReDoS (denial-of-service) risk.
     *
     *   Rule of thumb: never nest quantifiers unless each repetition has exactly
     *   ONE way to be divided. For untrusted patterns/inputs, cap input length
     *   and enforce a timeout.
     *
     *   Why \b(\w+)(\s+\1\b)+ is safe: despite nesting \w+ and \s+ inside (...)+,
     *   \w and \s are completely disjoint character sets, and \1 is locked to
     *   already-captured text bounded by word boundaries. Neither \s+ nor \1 can
     *   trade characters across repetitions, leaving zero ambiguity to explore.
     *   On Java 21 it runs linearly (~12 ms for 40,000 identical words). Note
     *   that Java 21 also optimizes away textbook traps like (a+)+b via
     *   pre-checks, but other engines (JS, older JVMs) do not.
     */

    public static void main(String[] args) {
        check("three-word duplicate run", "Go go GO home", "Go home", removeAdjacentDuplicates("Go go GO home"));
        check("separate runs", "red blue blue RED red", "red blue RED",
                removeAdjacentDuplicates("red blue blue RED red"));
        check("no duplicates", "one two three", "one two three", removeAdjacentDuplicates("one two three"));
        check("whole input is one run", "Echo ECHO echo", "Echo", removeAdjacentDuplicates("Echo ECHO echo"));

        check("leading whitespace", "  a a", "a", removeAdjacentDuplicates("  a a"));
        check("trailing whitespace", "a a  ", "a", removeAdjacentDuplicates("a a  "));
        check("multiple spaces in duplicate run", "go   go home", "go home",
                removeAdjacentDuplicates("go   go home"));

        check("empty input", "", "", removeAdjacentDuplicates(""));
        check("all whitespace", "   ", "", removeAdjacentDuplicates("   "));
        check("null input", "null (not printed above)", "", removeAdjacentDuplicates(null));

        check("single word", "hi", "hi", removeAdjacentDuplicates("hi"));
        check("prefix is not a duplicate", "go gone", "go gone", removeAdjacentDuplicates("go gone"));
        check("suffix is not a duplicate", "the he", "the he", removeAdjacentDuplicates("the he"));
        report("Challenge 22");
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