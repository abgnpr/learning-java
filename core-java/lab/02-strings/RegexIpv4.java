
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
    private static final Pattern PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");

    private RegexIpv4() {
    }

    static boolean isValidIpv4(String candidate) {
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
