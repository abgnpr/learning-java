
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
        check("character class", "[a-z]+", true, isValidRegex("[a-z]+"));
        check("group and quantifier", "(cat|dog){2}", true, isValidRegex("(cat|dog){2}"));
        check("open character class", "[unterminated", false, isValidRegex("[unterminated"));
        check("orphan quantifier", "*bad", false, isValidRegex("*bad"));
        // "" is VALID — it compiles and matches the empty string at every
        // position. "Valid syntax" and "useful pattern" are different questions.
        check("empty pattern", "", true, isValidRegex(""));
        // A lone backslash starts an escape that never arrives; {2,1} and [z-a]
        // parse as quantifier and range, then fail their own bounds check. All
        // three are syntax errors found after tokenizing, not while scanning.
        check("trailing escape", "\\", false, isValidRegex("\\"));
        check("duplicate group name", "(?<name>x)(?<name>y)", false, isValidRegex("(?<name>x)(?<name>y)"));
        check("inverted quantifier bounds", "a{2,1}", false, isValidRegex("a{2,1}"));
        check("reversed character range", "[z-a]", false, isValidRegex("[z-a]"));
        check("null candidate", "null", false, isValidRegex(null));
        report("Challenge 20");
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
