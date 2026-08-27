/*
 * Challenge 26 — Java Primality Test (Easy)
 *
 * Task: Classify an arbitrarily large non-negative integer with
 * BigInteger.isProbablePrime at certainty 100. At that certainty, the chance
 * of a composite being reported as probably prime is at most 2^-100.
 * Complete: isProbablyPrime(BigInteger value)
 * Run: java LargeNumberPrimality.java
 */
import java.math.BigInteger;

public class LargeNumberPrimality {
    static boolean isProbablyPrime(BigInteger value) {
        // TODO: Use BigInteger's primality test with the required certainty.
        throw new UnsupportedOperationException("TODO: test probable primality");
    }

    public static void main(String[] args) {
        check(true, isProbablyPrime(new BigInteger("2")), "smallest prime");
        check(false, isProbablyPrime(new BigInteger("1")), "one is not prime");
        check(true, isProbablyPrime(
            new BigInteger("170141183460469231731687303715884105727")),
            "prime beyond long"
        );
        check(false, isProbablyPrime(
            new BigInteger("510423550381407695195061911147652317181")),
            "composite beyond long"
        );
        System.out.println("Challenge 26 passed");
    }

    static void check(boolean expected, boolean actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
