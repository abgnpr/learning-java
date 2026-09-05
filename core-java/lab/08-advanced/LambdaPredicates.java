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

        checkThat("odd operation", "odd", odd != null && odd.getClass().isSynthetic());
        check("7 is odd", "odd.test(7)", true, odd.test(7));
        check("12 is not odd", "odd.test(12)", false, odd.test(12));
        check("negative 3 is odd", "odd.test(-3)", true, odd.test(-3));

        checkThat("prime operation", "prime", prime != null && prime.getClass().isSynthetic());
        check("17 is prime", "prime.test(17)", true, prime.test(17));
        check("1 is not prime", "prime.test(1)", false, prime.test(1));
        check("21 is composite", "prime.test(21)", false, prime.test(21));

        checkThat("palindrome operation", "palindrome", palindrome != null && palindrome.getClass().isSynthetic());
        check("898 is a palindrome", "palindrome.test(898)", true, palindrome.test(898));
        check("123 is not a palindrome", "palindrome.test(123)", false, palindrome.test(123));
        report("Challenge 62");
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
