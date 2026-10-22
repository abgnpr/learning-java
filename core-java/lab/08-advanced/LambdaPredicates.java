/*
 * Challenge 62: Java Lambda Expressions (Medium)
 *
 * Task: Return lambda expressions that classify an integer as odd, prime, or
 * palindromic through one functional interface. Negative values may be odd,
 * but they are neither prime nor decimal palindromes.
 *
 * Complete: isOdd(), isPrime(), and isPalindrome().
 * Java baseline: Java 8.
 * Run: javac LambdaPredicates.java && java LambdaPredicates
 */
public final class LambdaPredicates {
    @FunctionalInterface
    interface IntOperation {
        boolean test(int value);
    }

    private LambdaPredicates() {
    }

    static IntOperation isOdd() {
        // TODO: Return an odd-number predicate as a lambda expression.
        throw new UnsupportedOperationException("TODO: implement isOdd");
    }

    static IntOperation isPrime() {
        // TODO: Return a prime-number predicate as a lambda expression.
        throw new UnsupportedOperationException("TODO: implement isPrime");
    }

    static IntOperation isPalindrome() {
        // TODO: Return a decimal-palindrome predicate as a lambda expression.
        throw new UnsupportedOperationException("TODO: implement isPalindrome");
    }

    public static void main(String[] args) {
        IntOperation odd = isOdd();
        IntOperation prime = isPrime();
        IntOperation palindrome = isPalindrome();

        check("7 is odd", "odd.test(7)", true, odd.test(7));
        check("12 is not odd", "odd.test(12)", false, odd.test(12));
        check("negative 3 is odd", "odd.test(-3)", true, odd.test(-3));

        check("17 is prime", "prime.test(17)", true, prime.test(17));
        check("1 is not prime", "prime.test(1)", false, prime.test(1));
        check("21 is composite", "prime.test(21)", false, prime.test(21));

        check("898 is a palindrome", "palindrome.test(898)", true,
                palindrome.test(898));
        check("123 is not a palindrome", "palindrome.test(123)", false,
                palindrome.test(123));
        check("negative is not a palindrome", "palindrome.test(-121)", false,
                palindrome.test(-121));

        report("Challenge 62");
    }

    // The harness checks behavior. Whether a lambda class is synthetic is an
    // implementation detail and is deliberately not part of the contract.

    private static int passes;
    private static int failures;

    private static void check(
            String label,
            Object input,
            Object expected,
            Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);

        if (ok) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, "
                + failures + " failed.");

        if (failures > 0) {
            throw new AssertionError(
                    challenge + ": " + failures + " check(s) failed.");
        }

        System.out.println(challenge + " passed.");
    }
}
