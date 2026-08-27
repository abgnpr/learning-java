/*
 * Challenge 60: Java Annotations (Medium)
 *
 * Task: Inspect runtime budget annotations, find the method for a requested
 * family role, and invoke it only when the spending limit allows it.
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
        checkEquals("Senior member approved: 75", messageFor(member, "SENIOR", 75),
                "senior within limit");
        checkEquals("Junior member approved: 50", messageFor(member, "JUNIOR", 50),
                "junior at limit");
        checkEquals("Budget Limit Over", messageFor(member, "SENIOR", 101),
                "senior over limit");
        checkEquals("Budget Limit Over", messageFor(member, "GUEST", 1),
                "unknown role");
        System.out.println("Challenge 60 passed!");
    }

    private static void checkEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
