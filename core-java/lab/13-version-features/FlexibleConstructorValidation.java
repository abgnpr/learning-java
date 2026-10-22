/*
 * Challenge 118: Flexible Constructor Validation (Medium)
 * Java feature: Java 25 flexible constructor bodies (JEP 513).
 *
 * Task: Validate balance and owner, normalize the owner, then call super with
 * the validated balance. Put the validation and local computation directly
 * before super(...); do not move them into a static helper.
 *
 * Complete: SavingsAccount(...).
 * Run on JDK 25+: java FlexibleConstructorValidation.java
 */
public final class FlexibleConstructorValidation {
    static class Account {
        private final int balance;

        Account(int balance) {
            this.balance = balance;
        }

        int balance() {
            return balance;
        }
    }

    static final class SavingsAccount extends Account {
        private final String owner;

        SavingsAccount(String owner, int balance) {
            // TODO: Validate and normalize before super(balance), then assign owner.
            super(balance);
            throw new UnsupportedOperationException("TODO: use a constructor prologue");
        }

        String owner() {
            return owner;
        }
    }

    private FlexibleConstructorValidation() {
    }

    public static void main(String[] args) {
        SavingsAccount account = new SavingsAccount("  Ada Lovelace  ", 500);
        check("owner is normalized before construction", "  Ada Lovelace  ",
                "Ada Lovelace", account.owner());
        check("validated balance reaches superclass", "balance=500",
                500, account.balance());
        check("negative balance is rejected before superclass construction", "balance=-1",
                "IllegalArgumentException", thrownType(() -> new SavingsAccount("Ada", -1)));
        check("blank owner is rejected", "owner=spaces",
                "IllegalArgumentException", thrownType(() -> new SavingsAccount("   ", 1)));
        report("Challenge 118");
    }

    @FunctionalInterface
    interface Action {
        void run();
    }

    private static String thrownType(Action action) {
        try {
            action.run();
            return "nothing thrown";
        } catch (RuntimeException exception) {
            return exception.getClass().getSimpleName();
        }
    }

    private static int passes;
    private static int failures;

    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) throw new AssertionError(challenge + " failed");
        System.out.println(challenge + " passed.");
    }
}
