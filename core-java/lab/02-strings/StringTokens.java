
/*
 * Challenge 19: Java String Tokens
 * Difficulty: Easy
 *
 * Task: Split text into its non-empty runs of Unicode letters, preserving the
 * original spelling and encounter order.
 * Complete: static List<String> tokenize(String text)
 * Run: java StringTokens.java
 */
import java.util.List;
import java.util.regex.Pattern;

public final class StringTokens {
    private StringTokens() {
    }

    /*
     * Both approaches hinge on one rule: a "letter" here is a Unicode code
     * point, not a char. Java strings are UTF-16, so an astral letter like
     * 𐐀 (U+10400) is two chars, and Character.isLetter(char) says false for
     * each surrogate half on its own. Approach 1 survives that only because
     * it reads codePointAt/charCount and never touches charAt; approach 2
     * gets it free, since \P{L} in java.util.regex matches per code point.
     */
    static List<String> tokenize(String text) {
        // appraoch 1: using a loop and codepoints
        /* 
        var tokens = new ArrayList<String>();
        text = text.strip();
        int i = 0;
        var token = new StringBuilder();
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            if (Character.isLetter(cp)) {
                token.appendCodePoint(cp);
            } else if (token.length() > 0) {
                tokens.add(token.toString());
                token.setLength(0);
            }
            i += Character.charCount(cp);
        }
        if (token.length() > 0) {
            tokens.add(token.toString());
        }
        return tokens;
        */

        // The loop's cost is the trailing flush after the while: a token still
        // in the builder when the text ends has no delimiter to close it. The
        // strip() is decoration — a leading or trailing run of non-letters is
        // already skipped by the isLetter test.

        // appraoch 2: regex and streams
        // \P{L} is the negated category: one or more non-letters, so the
        // delimiter is "everything a token isn't" and no letter needs listing.
        // The + matters — without it "coffee... and" would split on each dot
        // and hand back empty strings between them.
        //
        // The filter exists for one asymmetric case: split discards trailing
        // empty fields but keeps a leading one, so ", Tea" arrives as
        // ["", "Tea"] while "Tea, " is just ["Tea"]. "--- 123 ---" needs no
        // guard of its own; it is all delimiter, so the stream is empty and
        // toList() returns [].
        return Pattern.compile("\\P{L}+")
                .splitAsStream(text)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static void main(String[] args) {
        check("punctuation delimiters", "Tea, coffee... and cocoa!",
                List.of("Tea", "coffee", "and", "cocoa"), tokenize("Tea, coffee... and cocoa!"));
        check("digits and spaces", "  42 cats + 7 dogs  ",
                List.of("cats", "dogs"), tokenize("  42 cats + 7 dogs  "));
        check("no words", "--- 123 ---",
                List.of(), tokenize("--- 123 ---"));
        check("Unicode letters", "naïve/café",
                List.of("naïve", "café"), tokenize("naïve/café"));
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
