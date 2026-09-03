/*
 * Challenge 62: Java Lambda Expressions (Medium)
 *
 * Task: Return lambda expressions that classify an integer as odd, prime, or
 * palindromic through one functional interface. Negative values may be odd,
 * but they are neither prime nor decimal palindromes.
 * Complete: isOdd(), isPrime(), and isPalindrome().
 * Run: java LambdaPredicates.java
 */
public class LambdaPredicates {
    @FunctionalInterface
    interface IntOperation {
        boolean test(int value);
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

        checkLambda(odd, "odd operation");
        checkEquals(true, odd.test(7), "7 is odd");
        checkEquals(false, odd.test(12), "12 is not odd");
        checkEquals(true, odd.test(-3), "negative 3 is odd");

        checkLambda(prime, "prime operation");
        checkEquals(true, prime.test(17), "17 is prime");
        checkEquals(false, prime.test(1), "1 is not prime");
        checkEquals(false, prime.test(21), "21 is composite");

        checkLambda(palindrome, "palindrome operation");
        checkEquals(true, palindrome.test(898), "898 is a palindrome");
        checkEquals(false, palindrome.test(123), "123 is not a palindrome");
        if (failures > 0) {
            throw new AssertionError("Challenge 62: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 62 passed.");
    }

    private static void checkLambda(IntOperation operation, String message) {
        check(operation != null && operation.getClass().isSynthetic(),
                message + " must be implemented by a lambda");
    }

    private static int failures = 0;

    private static void check(boolean condition, String message) {
        if (condition) {
            System.out.println("PASS " + message);
            return;
        }
        failures++;
        System.out.println("FAIL " + message);
    }

    private static void checkEquals(boolean expected, boolean actual, String message) {
        if (expected == actual) {
            System.out.println("PASS " + message + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + message
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
    }
}
