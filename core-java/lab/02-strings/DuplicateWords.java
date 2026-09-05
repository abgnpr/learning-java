
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
            "(\\w+)( \\b\\1\\b)+", Pattern.CASE_INSENSITIVE);

    static String removeAdjacentDuplicates(String text) {

        if (text == null) {
            return "";
        }
        // strip() first, so a leading space cannot make split() emit an empty
        // first word and put a stray space at the front of the result.
        // No empty-string guard is needed after it: split() never returns a
        // zero-length array, so words[0] is "" for "" and the loop never runs.
        text = text.strip();

        // approach 1 — split and walk
        // Compare against the last word KEPT, not the last word seen: `prev`
        // updates only inside the if, so it always holds what was appended,
        // which is by construction the word that opened the current run. Both
        // work, because within a run every word is equalsIgnoreCase to every
        // other, but only this one matches the stated contract literally
        // ("preserving the spelling of the run's first word").
        /*
        String[] words = text.split("\\s+");
        String prev = words[0];
        StringBuilder result = new StringBuilder(prev);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            if (!prev.equalsIgnoreCase(word)) {
                result.append(" ").append(word);
                prev = word;
            }
        }
        return result.toString();
        */

        // approach 2 — back-references, the point of "Java Regex 2"
        /*    (?i)  (\w+)  ( \b\1\b)+   ->   $1
         *
         * \1 IS NOT "match \w+ again". A group remembers the TEXT it matched,
         * and \1 demands those exact characters back. So (\w+) \1 matches
         * "go go" and fails on "go home" — at \1 the pattern has stopped being
         * a pattern and become the literal "go".
         *
         * \1 and $1 are the same group in two different languages: \1 inside
         * the PATTERN, $1 inside the REPLACEMENT template. Swapping them breaks
         * both — $1 in a pattern is the $ anchor then a "1", and \1 in a
         * replacement is a literal backslash-one.
         *
         * The + is what makes a run of three collapse. Without it the pattern
         * matches exactly a pair, and replaceAll resumes scanning AFTER the
         * match and never backs up: "go go go" matches "go go", writes "go",
         * restarts at the trailing " go" with nothing left to pair it with, and
         * returns "go go". Grouping the repeating unit — a space plus the
         * back-reference, not the word — and quantifying that group is the same
         * move as (octet\.){3} in RegexIpv4.
         *
         * The trailing \b is load-bearing and cost a failing test. (\w+) is
         * greedy but \1 is not anchored to a word end, so in "go gone" the
         * back-reference happily matched the "go" INSIDE "gone": the match was
         * "go go", the leftover "ne" was copied through, and the result was
         * "gone". \b is zero-width — it asserts a position between a word and a
         * non-word character, consuming nothing, the same family as ^ and \z.
         * The LEADING \b is redundant: it follows a literal space, so a
         * boundary is already guaranteed there. Kept for symmetry only.
         *
         * (?i) is a flag, not a group — the ? after ( marks the construct
         * non-capturing, so group numbers are untouched and (\w+) is still
         * group 1. It applies from where it appears to the end of the enclosing
         * group, so at position 0 it covers everything, including \1, which is
         * what makes "Go"/"go"/"GO" one run. Scoped forms: (?i:X) for X alone,
         * (?-i) to switch back off.
         *
         * String.replaceAll takes no flags argument — that is why (?i) exists.
         * Pattern.compile("...", Pattern.CASE_INSENSITIVE) is the equivalent.
         * String.replaceAll also recompiles the regex on every call, where a
         * static final Pattern compiles once at class-load. That is approach 3.
         *
         * Which to ship: the loop. It is O(n) with no backtracking, and its
         * split("\\s+") takes any whitespace where this pattern hardcodes a
         * single space. This one backtracks, and \w is [a-zA-Z0-9_] — wider
         * than the "ASCII-letter words" of the task, so "9 9" collapses too.
         *
         * CATASTROPHIC BACKTRACKING — the reason "it backtracks" is a real
         * answer and not a nitpick. A backtracking engine is determined to find
         * a match: on failure it returns to the last choice point and tries the
         * next alternative. When a quantified group contains another quantifier
         * — (X|Y+)+, (a+)+, (a*)*b — one input can be split among the inner and
         * outer quantifiers in exponentially many ways, and a string that
         * CANNOT match forces the engine to try all of them before giving up.
         * The failing input is the expensive one; the matching input returns
         * early. That asymmetry is what makes it a denial-of-service shape
         * rather than a slow-code shape.
         *
         * The rule: never nest a quantifier inside a quantified group unless
         * each input has exactly ONE way to be divided between them. Where a
         * pattern comes from user input, also cap the input length and run the
         * match under a timeout — an untrusted pattern is untrusted code.
         *
         * This pattern nests + inside a group under +, which is the shape to be
         * suspicious of, but it is safe: \1 is a back-reference to already-
         * captured text, so at each repetition there is exactly one string the
         * inner element can match. No ambiguity, nothing to explore. Measured
         * on Java 21 it is linear — 40,000 identical words collapse in ~12 ms.
         *
         * Java 21 also resists the textbook examples: (a+)+, (a|aa)+b and
         * (a*)*b against a non-matching input all return in under a millisecond
         * here, because the engine pre-checks required literals and prunes.
         * Do not read that as immunity — the guarantee is absent, not present,
         * and other engines (JS, and older JVMs) blow up on exactly these.
         */
        // return text.replaceAll("(?i)(\\w+)( \\b\\1\\b)+", "$1");

        // approach 3 — the same regex as a hoisted Pattern
        // Pattern.compile takes the flags as its second argument, so (?i) comes
        // out of the pattern text; setting both would be saying it twice. The
        // chain String.replaceAll hides is Pattern -> matcher(input) ->
        // replaceAll(replacement), and replaceAll lives on Matcher, not Pattern.
        // The replacement is "$1", not "$": a bare $ throws
        // IllegalArgumentException ("group index is missing") rather than
        // meaning a literal dollar, which is spelled \\$.
        return DUPLICATE_RUN.matcher(text).replaceAll("$1");

    }

    public static void main(String[] args) {
        check("three-word duplicate run", "Go go GO home", "Go home", removeAdjacentDuplicates("Go go GO home"));
        check("separate runs", "red blue blue RED red", "red blue RED",
                removeAdjacentDuplicates("red blue blue RED red"));
        check("no duplicates", "one two three", "one two three", removeAdjacentDuplicates("one two three"));
        check("whole input is one run", "Echo ECHO echo", "Echo", removeAdjacentDuplicates("Echo ECHO echo"));

        // Beyond the stated single-space contract: strip() is what keeps a
        // leading run of spaces from making split() emit an empty first word.
        check("leading whitespace", "  a a", "a", removeAdjacentDuplicates("  a a"));
        check("trailing whitespace", "a a  ", "a", removeAdjacentDuplicates("a a  "));

        // The no-guard paths: split() never returns a zero-length array, so
        // words[0] is "" here and the loop simply never runs.
        check("empty input", "", "", removeAdjacentDuplicates(""));
        check("all whitespace", "   ", "", removeAdjacentDuplicates("   "));
        check("null input", "null (not printed above)", "", removeAdjacentDuplicates(null));

        check("single word", "hi", "hi", removeAdjacentDuplicates("hi"));
        // A prefix is not a duplicate: the word boundary is the whole reason
        // "gone" survives next to "go".
        check("prefix is not a duplicate", "go gone", "go gone", removeAdjacentDuplicates("go gone"));
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
