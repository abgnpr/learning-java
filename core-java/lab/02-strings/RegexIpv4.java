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
    private RegexIpv4() {
    }

    static boolean isValidIpv4(String candidate) {
        throw new UnsupportedOperationException("TODO: validate the complete address with a regex");
    }

    public static void main(String[] args) {
        checkEquals(true, isValidIpv4("192.168.1.10"), "ordinary address");
        checkEquals(true, isValidIpv4("0.0.0.0"), "zero address");
        checkEquals(true, isValidIpv4("255.255.255.255"), "upper boundaries");
        checkEquals(false, isValidIpv4("256.1.1.1"), "octet above 255");
        checkEquals(false, isValidIpv4("1.2.3"), "too few octets");
        checkEquals(false, isValidIpv4("01.2.3.4"), "leading zero");
        System.out.println("Challenge 21 passed.");
    }

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
