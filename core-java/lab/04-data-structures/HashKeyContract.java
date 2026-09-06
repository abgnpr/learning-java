/*
 * Challenge DS-A3 — Hash Key Contract (Hard)
 *
 * Task: Make CustomerKey a logical HashMap key. Keys with the same tenant and
 * customer ID must compare equal and produce the same hash code; either field
 * being different must make keys unequal. Keep the key immutable.
 * Complete: CustomerKey.equals(Object other), CustomerKey.hashCode()
 * Run: java HashKeyContract.java
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashKeyContract {
    static final class CustomerKey {
        private final String tenant;
        private final long customerId;

        CustomerKey(String tenant, long customerId) {
            this.tenant = tenant;
            this.customerId = customerId;
        }

        @Override
        public boolean equals(Object other) {
            // TODO: Implement value equality for both fields.
            throw new UnsupportedOperationException("TODO: compare customer keys");
        }

        @Override
        public int hashCode() {
            // TODO: Hash the same fields used by equals.
            throw new UnsupportedOperationException("TODO: hash customer keys");
        }

        @Override
        public String toString() {
            return tenant + ":" + customerId;
        }
    }

    public static void main(String[] args) {
        CustomerKey original = new CustomerKey("india", 42);
        CustomerKey sameValue = new CustomerKey("india", 42);
        CustomerKey otherTenant = new CustomerKey("uk", 42);
        CustomerKey otherCustomer = new CustomerKey("india", 43);

        check("reflexive equality", original, true, original.equals(original));
        check("equal independent instances", "india:42 and india:42", true,
                original.equals(sameValue));
        check("symmetric equality", "both comparison directions", true,
                original.equals(sameValue) && sameValue.equals(original));
        check("different tenant", "india:42 and uk:42", false,
                original.equals(otherTenant));
        check("different customer", "india:42 and india:43", false,
                original.equals(otherCustomer));
        check("not equal to null", original, false, original.equals(null));
        check("not equal to another type", "india:42 String", false,
                original.equals("india:42"));
        check("equal keys share hash", "two india:42 keys", original.hashCode(),
                sameValue.hashCode());

        Map<CustomerKey, String> names = new HashMap<>();
        names.put(original, "Asha");
        check("HashMap finds a logical key", sameValue, "Asha", names.get(sameValue));

        Set<CustomerKey> unique = new HashSet<>();
        unique.add(original);
        unique.add(sameValue);
        unique.add(otherTenant);
        check("HashSet deduplicates equal keys", "india:42, india:42, uk:42", 2,
                unique.size());
        report("Challenge DS-A3");
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
