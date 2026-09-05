
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/*
 * Challenge 20: Pattern Syntax Checker
 * Difficulty: Easy
 *
 * Task: Return whether a string is a syntactically valid Java regular
 * expression; invalid patterns must not escape as exceptions.
 * Complete: static boolean isValidRegex(String candidate)
 * Run: java PatternSyntaxChecker.java
 */
public final class PatternSyntaxChecker {
    private PatternSyntaxChecker() {
    }

    /*
     * Exception-as-predicate is the intended design, not a smell: the regex
     * grammar has no "would this parse?" API, so compiling and catching is the
     * only way to ask. Pattern.compile and not Pattern.matches — matches
     * compiles AND runs, costing a scan and needing an input string we do not
     * have.
     *
     * catch (PatternSyntaxException) and not catch (Exception): compile has
     * exactly two failure modes, and they mean opposite things. A syntax error
     * is the answer "false"; a null argument is a caller bug. The wide catch
     * collapses both into false, so the null policy becomes an accident of
     * which exceptions happen to be subclasses rather than a decision. The
     * explicit guard states it instead — null is "not a valid regex" here.
     *
     * PatternSyntaxException is unchecked (it extends IllegalArgumentException)
     * because patterns are usually programmer-supplied constants, so the API
     * declines to force a catch on every call site.
     *
     * Nothing here catches Error, and that is deliberate: a pathological
     * pattern (thousands of nested groups) can exhaust the stack during
     * parsing, and a StackOverflowError should escape rather than be reported
     * as a syntax verdict.
     */
    static boolean isValidRegex(String candidate) {
        if (candidate == null) {
            return false;
        }
        try {
            Pattern.compile(candidate);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        checkEquals(true, isValidRegex("[a-z]+"), "character class");
        checkEquals(true, isValidRegex("(cat|dog){2}"), "group and quantifier");
        checkEquals(false, isValidRegex("[unterminated"), "open character class");
        checkEquals(false, isValidRegex("*bad"), "orphan quantifier");
        // "" is VALID — it compiles and matches the empty string at every
        // position. "Valid syntax" and "useful pattern" are different questions.
        checkEquals(true, isValidRegex(""), "empty pattern");
        // A lone backslash starts an escape that never arrives; {2,1} and [z-a]
        // parse as quantifier and range, then fail their own bounds check. All
        // three are syntax errors found after tokenizing, not while scanning.
        checkEquals(false, isValidRegex("\\"), "trailing escape");
        checkEquals(false, isValidRegex("(?<name>x)(?<name>y)"), "duplicate group name");
        checkEquals(false, isValidRegex("a{2,1}"), "inverted quantifier bounds");
        checkEquals(false, isValidRegex("[z-a]"), "reversed character range");
        checkEquals(false, isValidRegex(null), "null candidate");
        if (failures > 0) {
            throw new AssertionError("Challenge 20: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 20 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(boolean expected, boolean actual, String label) {
        if (expected == actual) {
            System.out.println("PASS " + label + ": " + "<" + actual + ">");
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + ":\n  expected: <" + expected + ">"
                + "\n    actual: <" + actual + ">");
    }
}
