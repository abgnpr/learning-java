/*
 * Challenge 115: Pattern Switch Routing (Medium)
 * Java features: Java 21 record patterns and pattern switch (JEPs 440, 441).
 *
 * Task: Route a sealed payment event with one exhaustive switch expression.
 * Destructure records, use a guard for large withdrawals, and handle null.
 *
 * Complete: route(...).
 * Run on JDK 21+: java PatternSwitchRouting.java
 */
public final class PatternSwitchRouting {
    sealed interface PaymentEvent permits Deposit, Withdrawal, Transfer {
    }

    record Deposit(String account, int amount) implements PaymentEvent {
    }

    record Withdrawal(String account, int amount) implements PaymentEvent {
    }

    record Transfer(String from, String to, int amount) implements PaymentEvent {
    }

    private PatternSwitchRouting() {
    }

    static String route(PaymentEvent event) {
        // TODO: Return one exhaustive pattern switch. Use a when guard at 10_000.
        throw new UnsupportedOperationException("TODO: route with record patterns");
    }

    public static void main(String[] args) {
        check("null event", null, "ignore", route(null));
        check("deposit destructures its record", "Deposit(A1, 500)",
                "credit A1 by 500", route(new Deposit("A1", 500)));
        check("ordinary withdrawal", "Withdrawal(A1, 9000)",
                "debit A1 by 9000", route(new Withdrawal("A1", 9_000)));
        check("guarded withdrawal", "Withdrawal(A1, 10000)",
                "review A1", route(new Withdrawal("A1", 10_000)));
        check("transfer destructures all components", "Transfer(A1, B2, 75)",
                "move 75 from A1 to B2", route(new Transfer("A1", "B2", 75)));
        report("Challenge 115");
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
