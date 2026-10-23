/*
 * Challenge 122 — Payment Routing Contracts (Medium)
 *
 * Task: Use composition to charge through any PaymentMethod with any FeePolicy.
 * Checkout holds those two dependencies, rejects null dependencies at construction,
 * and validates a strictly positive amount BEFORE invoking either dependency.
 * Charge amount + fee; the fee must be non-negative. Never select concrete types
 * with instanceof. Propagate dependency failures unchanged.
 * Wallet encapsulates a non-negative balance: reject overdrafts without changing
 * it and return "wallet" for successful authorization. Its constructor rejects
 * null/negative balances. It is intentionally a single-threaded exercise.
 * Complete: Checkout constructor/charge and Wallet constructor/authorize.
 * Run: java 05-oop/PaymentRoutingContracts.java
 * Explain: substitution, encapsulation, interface versus abstract class, and
 * why replacing a fee policy should not require modifying Checkout.
 */
import java.math.BigDecimal;
import java.util.*;

public final class PaymentRoutingContracts {
    interface PaymentMethod { String authorize(BigDecimal amount); }
    interface FeePolicy { BigDecimal fee(BigDecimal amount); }
    record Receipt(String authorization, BigDecimal charged) { }

    static final class Wallet implements PaymentMethod {
        private BigDecimal balance;
        Wallet(BigDecimal openingBalance) {
            throw new UnsupportedOperationException("TODO: establish the wallet invariant");
        }
        BigDecimal balance() { return balance; }
        @Override public String authorize(BigDecimal amount) {
            throw new UnsupportedOperationException("TODO: debit only a valid affordable amount");
        }
    }

    static final class Checkout {
        private final PaymentMethod method;
        private final FeePolicy policy;
        Checkout(PaymentMethod method, FeePolicy policy) {
            throw new UnsupportedOperationException("TODO: retain validated interface dependencies");
        }
        Receipt charge(BigDecimal amount) {
            throw new UnsupportedOperationException("TODO: validate, calculate fee, and delegate once");
        }
    }

    public static void main(String[] args) {
        Wallet wallet = new Wallet(new BigDecimal("100.00"));
        PaymentMethod polymorphic = wallet;
        Checkout checkout = new Checkout(polymorphic, amount -> new BigDecimal("2.00"));
        check("fee is included in delegated charge", new Receipt("wallet", new BigDecimal("22.00")), checkout.charge(new BigDecimal("20.00")));
        check("wallet encapsulates its balance", new BigDecimal("78.00"), wallet.balance());
        expect(IllegalStateException.class, () -> checkout.charge(new BigDecimal("80.00")));
        check("overdraft leaves state intact", new BigDecimal("78.00"), wallet.balance());
        expect(IllegalArgumentException.class, () -> wallet.authorize(BigDecimal.ZERO));
        expect(IllegalArgumentException.class, () -> wallet.authorize(new BigDecimal("-1.00")));
        expect(NullPointerException.class, () -> wallet.authorize(null));
        check("invalid direct calls preserve the invariant", new BigDecimal("78.00"), wallet.balance());
        Wallet exact = new Wallet(new BigDecimal("5.00"));
        check("entire balance is spendable", "wallet", exact.authorize(new BigDecimal("5.00")));
        check("exact debit reaches zero", new BigDecimal("0.00"), exact.balance());
        expect(IllegalArgumentException.class, () -> new Wallet(new BigDecimal("-1")));
        expect(NullPointerException.class, () -> new Wallet(null));
        expect(NullPointerException.class, () -> new Checkout(null, amount -> BigDecimal.ZERO));
        expect(NullPointerException.class, () -> new Checkout(polymorphic, null));

        List<String> trace = new ArrayList<>();
        PaymentMethod external = amount -> { trace.add("authorize:" + amount); return "external-42"; };
        FeePolicy percent = amount -> {
            trace.add("fee:" + amount);
            return amount.movePointLeft(1).setScale(2, java.math.RoundingMode.UNNECESSARY);
        };
        Checkout alternate = new Checkout(external, percent);
        check("unrelated implementation substitutes", new Receipt("external-42", new BigDecimal("11.00")), alternate.charge(new BigDecimal("10.00")));
        check("exactly one fee and authorization in order", List.of("fee:10.00", "authorize:11.00"), trace);
        trace.clear();
        expect(IllegalArgumentException.class, () -> alternate.charge(BigDecimal.ZERO));
        expect(IllegalArgumentException.class, () -> alternate.charge(new BigDecimal("-1")));
        expect(NullPointerException.class, () -> alternate.charge(null));
        check("invalid amount calls no dependency", List.of(), trace);
        expect(IllegalArgumentException.class, () -> new Checkout(external, amount -> new BigDecimal("-1")).charge(BigDecimal.ONE));
        check("negative fee never reaches payment method", List.of(), trace);
        IllegalStateException declined = new IllegalStateException("declined");
        Checkout failing = new Checkout(amount -> { throw declined; }, amount -> BigDecimal.ZERO);
        check("gateway failure identity survives", true,
                expect(IllegalStateException.class, () -> failing.charge(BigDecimal.ONE)) == declined);
        report("Challenge 122");
    }

    // ---- test harness (not part of the exercise) ----
    private static int passes;
    private static int failures;

    private static void check(String label, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    private static String show(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return "<" + text.replace("\n", "\\n") + ">";
        if (value.getClass().isArray()) return java.util.Arrays.deepToString(new Object[]{value});
        return value + " (" + value.getClass().getSimpleName() + ")";
    }

    @FunctionalInterface
    interface CheckedAction { void run() throws Exception; }

    private static <E extends Throwable> E expect(Class<E> type, CheckedAction action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("expected " + type.getName()
                    + ", got " + actual.getClass().getName(), actual);
            passes++;
            System.out.println("PASS  throws " + type.getSimpleName());
            return type.cast(actual);
        }
        throw new AssertionError("expected " + type.getName() + ", nothing was thrown");
    }

    private static void report(String name) {
        System.out.println(name + ": " + passes + " passed, " + failures + " failed.");
        if (failures != 0) throw new AssertionError(name + " failed");
        System.out.println(name + " passed.");
    }
}
