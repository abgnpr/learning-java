/*
 * Challenge 56: Prime Checker (Medium)
 *
 * Task: Provide overloaded checkPrime methods for several fixed arities; each
 * call returns its prime arguments in their original order, separated by one
 * space with no surrounding whitespace. Return an empty String when none are
 * prime.
 * Complete: All PrimeChecker.checkPrime overloads.
 * Run: java OverloadedPrimeChecker.java
 */
public class OverloadedPrimeChecker {
    static final class PrimeChecker {
        public String checkPrime(int first) {
            // TODO: Return the prime values among the arguments.
            throw new UnsupportedOperationException("TODO: implement one-argument checkPrime");
        }

        public String checkPrime(int first, int second, int third) {
            // TODO: Implement this overload without replacing it with varargs.
            throw new UnsupportedOperationException("TODO: implement three-argument checkPrime");
        }

        public String checkPrime(int first, int second, int third, int fourth, int fifth) {
            // TODO: Implement this overload without replacing it with varargs.
            throw new UnsupportedOperationException("TODO: implement five-argument checkPrime");
        }
    }

    public static void main(String[] args) {
        PrimeChecker checker = new PrimeChecker();
        checkEquals("2", checker.checkPrime(2), "one prime");
        checkEquals("2", checker.checkPrime(1, 2, 4), "one prime among three values");
        checkEquals("5 7 11", checker.checkPrime(5, 7, 8, 9, 11),
                "three primes among five values");
        System.out.println("Challenge 56 passed!");
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
