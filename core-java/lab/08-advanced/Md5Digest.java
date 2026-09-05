/*
 * Challenge 63: Java MD5 (Medium)
 *
 * Task: Hash the UTF-8 bytes of a string with MD5 and return all digest bytes
 * as a zero-padded, lowercase hexadecimal string.
 * Complete: md5Hex(String).
 * Run: java Md5Digest.java
 */
import java.security.NoSuchAlgorithmException;

public class Md5Digest {
    static String md5Hex(String text) throws NoSuchAlgorithmException {
        // TODO: Create an MD5 MessageDigest and format every byte as two hex digits.
        throw new UnsupportedOperationException("TODO: implement md5Hex");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        check("empty input", "md5Hex(\"\")", "d41d8cd98f00b204e9800998ecf8427e", md5Hex(""));
        check("short input", "md5Hex(\"abc\")", "900150983cd24fb0d6963f7d28e17f72", md5Hex("abc"));
        check("sentence input", "md5Hex(\"The quick brown fox jumps over the lazy dog\")", "9e107d9d372bb6826bd81d3542a419d6", md5Hex("The quick brown fox jumps over the lazy dog"));
        check("UTF-8 input", "md5Hex(\"café\")", "07117fe4a1ebd544965dc19573183da2", md5Hex("café"));
        report("Challenge 63");
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
