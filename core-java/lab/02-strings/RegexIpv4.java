
import java.util.regex.Pattern;

/*
 * Challenge 21: Java Regex
 * Difficulty: Medium
 *
 * Task: Validate canonical IPv4 text: exactly four decimal octets from 0 to
 * 255, with no leading zero on a multi-digit octet.
 * Complete: static boolean isValidIpv4(String candidate)
 * Run: java RegexIpv4.java
 */
public final class RegexIpv4 {
    /*
     * One octet is 25[0-5] | 2[0-4][0-9] | 1[0-9][0-9] | [1-9]?[0-9], and the
     * branch order is load-bearing: alternation is first-match-wins, not
     * longest-match-wins, so the widest branch has to come last. Put
     * [1-9]?[0-9] first and "255" matches on its "25" prefix, leaving a stray
     * "5" for the next \. to choke on.
     *
     * [1-9]?[0-9] is also what rejects leading zeros, and it is NOT the same as
     * [0-9]{1,2} — both accept 0-99, but only this one refuses "01", because a
     * two-digit match must start with 1-9. That is the entire no-leading-zero
     * rule; there is no separate guard for it.
     *
     * The 4th octet is spelled out instead of folding the group into {4}: the
     * group is "octet + dot" and repeats three times, because the last octet
     * has no dot after it.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])");

    private RegexIpv4() {
    }

    static boolean isValidIpv4(String candidate) {

        // the anchored-find way — the bug this started as
        /*
        // with ^...$ wrapped around the pattern:
        var matcher = PATTERN.matcher(candidate);
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        return matches == 1;
        */
        // Two faults. Counting hits from an anchored pattern is a yes/no
        // question asked as arithmetic — anchored at ^, it can only ever hit
        // once. And Java's $ matches before a FINAL line terminator, so
        // "1.2.3.4\n" matched its first line and the method returned true.

        // matches() requires the whole region to match, so that trailing \n is
        // left unconsumed and the match fails. It also makes ^ and $ redundant,
        // which is why they are gone: the anchors were not merely noise, they
        // were what hid the newline hole.
        //
        // The three Matcher entry points, since picking the wrong one is how
        // validators leak:
        //   matches()     whole region must match. The validator's default.
        //   lookingAt()   must match from the start, trailing junk allowed.
        //   find()        matches anywhere, and repeat calls walk every hit.
        //                 A validator using it needs its own anchors.
        //
        // And the anchors themselves, which are NOT interchangeable:
        //   ^  \A   start — but ^ also follows every \n under MULTILINE.
        //   $       end, EXCEPT it also matches before one final terminator.
        //   \z      true end of input. No exemption. What ^...$ should have
        //           been here.
        //   \Z      end, but before a final terminator — the $ trap, spelled.
        // So "1.2.3.4\n" satisfies ...$ and fails ...\z. That one exemption
        // is the whole bug above.
        return PATTERN.matcher(candidate).matches();
    }

    public static void main(String[] args) {
        check("ordinary address", "192.168.1.10", true, isValidIpv4("192.168.1.10"));
        check("zero address", "0.0.0.0", true, isValidIpv4("0.0.0.0"));
        check("upper boundaries", "255.255.255.255", true, isValidIpv4("255.255.255.255"));
        check("octet above 255", "256.1.1.1", false, isValidIpv4("256.1.1.1"));
        check("too few octets", "1.2.3", false, isValidIpv4("1.2.3"));
        check("leading zero", "01.2.3.4", false, isValidIpv4("01.2.3.4"));
        check("leading zero on zero", "00.1.1.1", false, isValidIpv4("00.1.1.1"));
        check("padded three-digit octet", "192.168.001.1", false, isValidIpv4("192.168.001.1"));
        check("too many octets", "1.2.3.4.5", false, isValidIpv4("1.2.3.4.5"));
        check("trailing dot", "1.2.3.", false, isValidIpv4("1.2.3."));
        check("empty string", "", false, isValidIpv4(""));
        check("trailing newline", "1.2.3.4\n", false, isValidIpv4("1.2.3.4\n"));
        report("Challenge 21");
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
