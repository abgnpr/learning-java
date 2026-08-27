/*
 * Challenge 01: Welcome to Java!
 * Difficulty: Easy
 *
 * Task: Return the exact greeting requested by this small stdout warm-up.
 * Complete: static String greeting()
 * Run: java WelcomeToJava.java
 */
public final class WelcomeToJava {
    private WelcomeToJava() {
    }

    static String greeting() {
        throw new UnsupportedOperationException("TODO: return the Java greeting");
    }

    public static void main(String[] args) {
        checkEquals("Hello, Java!", greeting(), "greeting text");
        checkEquals(12, greeting().length(), "greeting length");
        checkEquals(false, greeting().endsWith("\n"), "no embedded newline");
        System.out.println("Challenge 01 passed.");
    }

    private static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
