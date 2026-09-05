
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
import java.util.regex.Pattern;

public final class TagContentExtractor {
    private TagContentExtractor() {
    }

    // `?` means "0 or 1" after a character (colou?r), but after a quantifier it
    // flips that quantifier from greedy to lazy: `.*` takes everything and hands
    // characters back until the rest of the pattern fits, `.*?` takes nothing and
    // accepts one more at a time. On "<a>x</a><a>y</a>", `<(\w+)>(.*)</\1>` spans
    // both elements; `.*?` stops at the first `</a>`.
    //
    // That makes `(.*?)` the tempting content group here, and it is wrong twice:
    //
    //     <a></a><a>ok</a>        ->  ["", "ok"]  -- `*` matches zero characters,
    //                                 so an empty element is still a match
    //     <p>skip</q><p>keep</p>  ->  ["skip</q><p>keep"]
    //
    // The second is the one worth remembering. Lazy stops at the first place the
    // REST OF THE PATTERN can match, not at the first `<`. Here `</q>` is not the
    // `</\1>` being sought, so `.` -- which may cross `<` -- keeps extending
    // through the malformed tag until a real `</p>` turns up, welding two elements
    // into one match. The contract says ignore mismatches; lazy quietly cannot.
    //
    // `[^<>]+` fixes both without a `?`, and the fix is structural rather than a
    // matter of stopping early: the class cannot step over the `<` that opens
    // `</q>`, so that match dies there and the scan resumes at `<p>keep</p>`. `+`
    // then rules out the empty element, so no `.filter(s -> !s.isEmpty())` is
    // needed downstream. The general lesson -- when a match runs too long, narrow
    // what the content may contain before reaching for laziness.
    //
    // Bounding content by "anything but angle brackets" is only legal because the
    // contract promises flat markup with no `<`/`>` in text (real markup escapes
    // them as &lt; / &gt;). Allow raw brackets or nesting and this stops being a
    // regex problem -- Java patterns cannot count depth -- and wants a parser.
    // Group numbers count OPENING parens left to right, so group 1 is the tag name
    // and group 2 the content; `\1` is a backreference demanding the closing tag
    // repeat whatever group 1 actually captured. `</\w+>` would accept any name and
    // let `<x>one</y>` through. Note `$1` is replacement syntax (replaceAll) -- in a
    // pattern it is the literal characters `$` and `1`, matching nothing here.
    //
    // `\w` suits the NAME even though it was too narrow for the content: names are
    // letters and digits, so `<h1>` works, and the surrounding `<` `>` pin both ends
    // -- which is what makes `<bb>x</b>` fail rather than matching a `b` prefix.
    private final static Pattern EXTRACTOR_PATTERN = Pattern.compile(
            "<(\\w+)>([^<>]+)</\\1>");

    static List<String> extractText(String markup) {
        // results() (Java 9+) is the stream form of the `while (m.find())` loop: one
        // MatchResult per match, left to right, non-overlapping. Each carries the
        // match POSITIONS, so its toString() is an object dump -- group(n) is what
        // returns text. Scanning resumes after each match, which is what lets a
        // failed attempt at `<x>one</y>` be abandoned and `<z>two</z>` still be found.
        return EXTRACTOR_PATTERN.matcher(markup)
                .results()
                .map(r -> r.group(2))
                .toList();
    }

    public static void main(String[] args) {
        check("two tags", "<p>alpha</p><b>beta</b>", List.of("alpha", "beta"), extractText("<p>alpha</p><b>beta</b>"));
        check("mismatched tag ignored", "<x>one</y><z>two</z>", List.of("two"), extractText("<x>one</y><z>two</z>"));
        check("no tags", "plain text", List.of(), extractText("plain text"));
        check("empty content ignored", "<a></a><a>ok</a>", List.of("ok"), extractText("<a></a><a>ok</a>"));

        // Content class: the contract says text is anything but angle brackets,
        // so \w (letters, digits, _) is too narrow on every case below.
        check("content with a space", "<p>hello world</p>", List.of("hello world"),
                extractText("<p>hello world</p>"));
        check("content with punctuation", "<p>a-b, c.</p>", List.of("a-b, c."),
                extractText("<p>a-b, c.</p>"));
        check("content is only spaces", "<p>   </p>", List.of("   "), extractText("<p>   </p>"));

        // Empty content skipped mid-run, not just at the head.
        check("empty tag between two full ones", "<p>a</p><p></p><p>b</p>", List.of("a", "b"),
                extractText("<p>a</p><p></p><p>b</p>"));

        // Tag-name matching: the backreference must compare the whole name,
        // not a prefix, and tag names are case-sensitive.
        check("prefix name is not a match", "<b>x</bb>", List.of(), extractText("<b>x</bb>"));
        check("longer name closed by prefix", "<bb>x</b>", List.of(), extractText("<bb>x</b>"));
        check("case-sensitive names", "<P>x</p>", List.of(), extractText("<P>x</p>"));
        check("digits in tag name", "<h1>title</h1>", List.of("title"), extractText("<h1>title</h1>"));

        // Text outside tags is not collected, and tags may be separated by it.
        check("text around and between tags", "pre<p>in</p>mid<b>on</b>post", List.of("in", "on"),
                extractText("pre<p>in</p>mid<b>on</b>post"));

        // Repeated names, and the same name reused after a mismatch.
        check("same name twice", "<p>one</p><p>two</p>", List.of("one", "two"),
                extractText("<p>one</p><p>two</p>"));
        check("mismatch then valid same name", "<p>skip</q><p>keep</p>", List.of("keep"),
                extractText("<p>skip</q><p>keep</p>"));

        // Degenerate inputs.
        check("empty markup", "", List.of(), extractText(""));
        check("unclosed tag", "<p>dangling", List.of(), extractText("<p>dangling"));
        check("closing tag only", "</p>", List.of(), extractText("</p>"));

        report("Challenge 24");
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
