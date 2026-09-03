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
        checkEquals("d41d8cd98f00b204e9800998ecf8427e", md5Hex(""), "empty input");
        checkEquals("900150983cd24fb0d6963f7d28e17f72", md5Hex("abc"), "short input");
        checkEquals("9e107d9d372bb6826bd81d3542a419d6",
                md5Hex("The quick brown fox jumps over the lazy dog"), "sentence input");
        checkEquals("07117fe4a1ebd544965dc19573183da2", md5Hex("café"), "UTF-8 input");
        if (failures > 0) {
            throw new AssertionError("Challenge 63: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 63 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(String expected, String actual, String message) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + message + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
