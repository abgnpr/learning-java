/*
 * Challenge 117: Scoped Value Context (Medium)
 * Java feature: Java 25 scoped values (JEP 506).
 *
 * Task: Bind REQUEST_ID only for the dynamic extent of an action. Nested calls
 * must rebind temporarily, and normal or exceptional completion must restore
 * the previous binding automatically.
 *
 * Complete: withRequest(...).
 * Run on JDK 25+: java ScopedValueContext.java
 */
import java.util.function.Supplier;

public final class ScopedValueContext {
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private ScopedValueContext() {
    }

    static <T> T withRequest(String requestId, Supplier<T> action) {
        // TODO: Bind REQUEST_ID with ScopedValue.where(...).call(...).
        throw new UnsupportedOperationException("TODO: establish a scoped binding");
    }

    static String currentRequest() {
        return REQUEST_ID.orElse("unbound");
    }

    public static void main(String[] args) {
        check("unbound before call", "outside", "unbound", currentRequest());
        check("binding is visible down the call chain", "request-1", "request-1",
                withRequest("request-1", ScopedValueContext::currentRequest));

        String nested = withRequest("outer", () -> {
            String before = currentRequest();
            String inner = withRequest("inner", ScopedValueContext::currentRequest);
            String after = currentRequest();
            return before + "/" + inner + "/" + after;
        });
        check("nested binding restores outer value", "outer -> inner -> outer",
                "outer/inner/outer", nested);

        check("unbound after normal completion", "outside", "unbound", currentRequest());
        check("exception propagates", "throw inside scope", "IllegalStateException",
                thrownType(() -> withRequest("failing", () -> {
                    throw new IllegalStateException("boom");
                })));
        check("unbound after exceptional completion", "outside", "unbound", currentRequest());
        report("Challenge 117");
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
