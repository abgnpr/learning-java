/*
 * Challenge 64: Java SHA-256 (Medium)
 *
 * Task: Hash the UTF-8 bytes of a string with SHA-256 and return all digest
 * bytes as a zero-padded, lowercase hexadecimal string.
 * Complete: sha256Hex(String).
 * Run: java Sha256Digest.java
 */
import java.security.NoSuchAlgorithmException;

public class Sha256Digest {
    static String sha256Hex(String text) throws NoSuchAlgorithmException {
        // TODO: Create a SHA-256 MessageDigest and format every byte as two hex digits.
        throw new UnsupportedOperationException("TODO: implement sha256Hex");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        checkEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                sha256Hex(""), "empty input");
        checkEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                sha256Hex("abc"), "short input");
        checkEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
                sha256Hex("The quick brown fox jumps over the lazy dog"), "sentence input");
        checkEquals("850f7dc43910ff890f8879c0ed26fe697c93a067ad93a7d50f466a7028a9bf4e",
                sha256Hex("café"), "UTF-8 input");
        System.out.println("Challenge 64 passed!");
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
