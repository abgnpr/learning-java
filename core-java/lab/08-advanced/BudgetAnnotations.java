/*
 * Challenge 60: Java Annotations (Medium)
 *
 * Task: Inspect runtime budget annotations, find the method for a requested
 * family role, and invoke it only when spend is at or below its limit. Return
 * "Budget Limit Over" when the role is unknown or spend exceeds the limit.
 * Complete: messageFor(FamilyMember, String, int).
 * Run: java BudgetAnnotations.java
 */
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

public class BudgetAnnotations {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface BudgetRule {
        String role();

        int limit();
    }

    static final class FamilyMember {
        @BudgetRule(role = "SENIOR", limit = 100)
        public String seniorMember(int spend) {
            return "Senior member approved: " + spend;
        }

        @BudgetRule(role = "JUNIOR", limit = 50)
        public String juniorMember(int spend) {
            return "Junior member approved: " + spend;
        }
    }

    static String messageFor(FamilyMember member, String role, int spend)
            throws ReflectiveOperationException {
        // TODO: Inspect BudgetRule annotations and invoke the eligible method.
        throw new UnsupportedOperationException("TODO: implement messageFor");
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        FamilyMember member = new FamilyMember();
        check("senior within limit", "messageFor(member, \"SENIOR\", 75)", "Senior member approved: 75", messageFor(member, "SENIOR", 75));
        check("junior at limit", "messageFor(member, \"JUNIOR\", 50)", "Junior member approved: 50", messageFor(member, "JUNIOR", 50));
        check("senior over limit", "messageFor(member, \"SENIOR\", 101)", "Budget Limit Over", messageFor(member, "SENIOR", 101));
        check("unknown role", "messageFor(member, \"GUEST\", 1)", "Budget Limit Over", messageFor(member, "GUEST", 1));
        report("Challenge 60");
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
