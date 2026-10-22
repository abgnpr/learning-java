/*
 * Challenge 113: Optional Account Workflow (Medium)
 *
 * Task: Find an active customer, flatten the optional email returned by the
 * customer, normalize it, reject blank text, and invoke the fallback lazily.
 *
 * Complete: activeEmail(...).
 * Java baseline: Java 8.
 * Run: javac OptionalAccountWorkflow.java && java OptionalAccountWorkflow
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class OptionalAccountWorkflow {
    static final class Customer {
        private final String id;
        private final boolean active;
        private final String email;

        Customer(String id, boolean active, String email) {
            this.id = id;
            this.active = active;
            this.email = email;
        }

        String id() {
            return id;
        }

        boolean isActive() {
            return active;
        }

        Optional<String> email() {
            return Optional.ofNullable(email);
        }
    }

    private OptionalAccountWorkflow() {
    }

    static String activeEmail(
            Map<String, Customer> customers,
            String id,
            Supplier<String> fallback) {
        // TODO: Use Optional.ofNullable, filter, flatMap, map, and orElseGet.
        throw new UnsupportedOperationException("TODO: build the Optional workflow");
    }

    public static void main(String[] args) {
        Map<String, Customer> customers = new HashMap<String, Customer>();
        customers.put("a", new Customer("a", true, "  ADA@EXAMPLE.COM  "));
        customers.put("b", new Customer("b", false, "bob@example.com"));
        customers.put("c", new Customer("c", true, null));
        customers.put("d", new Customer("d", true, "   "));

        AtomicInteger fallbackCalls = new AtomicInteger();
        Supplier<String> fallback = () -> {
            fallbackCalls.incrementAndGet();
            return "missing@example.com";
        };

        check("present active email is normalized", "id=a",
                "ada@example.com", activeEmail(customers, "a", fallback));
        check("fallback stays lazy for a present value", "after id=a",
                0, fallbackCalls.get());

        check("inactive customer uses fallback", "id=b",
                "missing@example.com", activeEmail(customers, "b", fallback));
        check("missing email uses fallback", "id=c",
                "missing@example.com", activeEmail(customers, "c", fallback));
        check("blank email uses fallback", "id=d",
                "missing@example.com", activeEmail(customers, "d", fallback));
        check("unknown customer uses fallback", "id=unknown",
                "missing@example.com", activeEmail(customers, "unknown", fallback));
        check("fallback called exactly for absent outcomes", "b, c, d, unknown",
                4, fallbackCalls.get());

        report("Challenge 113");
    }

    private static int passes;
    private static int failures;

    private static void check(
            String label,
            Object input,
            Object expected,
            Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);

        if (ok) {
            passes++;
        } else {
            failures++;
        }

        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, "
                + failures + " failed.");

        if (failures > 0) {
            throw new AssertionError(
                    challenge + ": " + failures + " check(s) failed.");
        }

        System.out.println(challenge + " passed.");
    }
}
