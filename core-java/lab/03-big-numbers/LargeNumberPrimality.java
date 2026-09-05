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
        check("smallest prime", "new BigInteger(\"2\")", true, isProbablyPrime(new BigInteger("2")));
        check("one is not prime", "new BigInteger(\"1\")", false, isProbablyPrime(new BigInteger("1")));
        check("prime beyond long", "new BigInteger(\"170141183460469231731687303715884105727\")", true, isProbablyPrime(
            new BigInteger("170141183460469231731687303715884105727")));
        check("composite beyond long", "new BigInteger(\"510423550381407695195061911147652317181\")", false, isProbablyPrime(
            new BigInteger("510423550381407695195061911147652317181")));
        report("Challenge 26");
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
