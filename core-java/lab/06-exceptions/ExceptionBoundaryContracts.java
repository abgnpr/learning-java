/*
 * Challenge 126 — Exception Boundary Contracts (Medium)
 *
 * Task: Read and trim one reference from Source. Reject null/blank data with
 * IllegalArgumentException. Translate only IOException into ReferenceLoadException
 * with message "reference lookup failed" and the original cause. Unrelated
 * runtime failures must escape unchanged. Do not turn failures into null/empty.
 * Complete: exception constructor and loadReference.
 * Run: java 06-exceptions/ExceptionBoundaryContracts.java
 * Explain: cause versus suppressed failure; checked versus unchecked contract;
 * why catching Exception here would hide programming errors.
 */
import java.io.IOException;

public final class ExceptionBoundaryContracts {
    @FunctionalInterface
    interface Source { String read() throws IOException; }

    static final class ReferenceLoadException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        ReferenceLoadException(String message, Throwable cause) {
            // TODO: Initialize the superclass with both message and cause.
            throw new UnsupportedOperationException("TODO: preserve the exception cause");
        }
    }

    static String loadReference(Source source) {
        throw new UnsupportedOperationException("TODO: validate data and translate only I/O failure");
    }

    public static void main(String[] args) {
        check("successful normalization", "RRN-42", loadReference(() -> "  RRN-42  "));
        java.util.concurrent.atomic.AtomicInteger reads = new java.util.concurrent.atomic.AtomicInteger();
        check("a source is read once", "RRN-43", loadReference(() -> {
            reads.incrementAndGet(); return "RRN-43";
        }));
        check("no repeated I/O for validation", 1, reads.get());
        expect(IllegalArgumentException.class, () -> loadReference(() -> " \t "));
        expect(IllegalArgumentException.class, () -> loadReference(() -> null));
        IOException disk = new IOException("disk unavailable");
        ReferenceLoadException wrapped = expect(ReferenceLoadException.class,
                () -> loadReference(() -> { throw disk; }));
        check("domain message", "reference lookup failed", wrapped.getMessage());
        check("original cause identity", true, wrapped.getCause() == disk);
        check("cause is not a suppressed exception", 0, wrapped.getSuppressed().length);
        IllegalStateException bug = new IllegalStateException("unexpected state");
        check("unrelated failure retains identity", true,
                expect(IllegalStateException.class, () -> loadReference(() -> { throw bug; })) == bug);
        ReferenceLoadException alreadyDomain = new ReferenceLoadException("existing", disk);
        check("domain failures are not double-wrapped", true,
                expect(ReferenceLoadException.class,
                        () -> loadReference(() -> { throw alreadyDomain; })) == alreadyDomain);
        report("Challenge 126");
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
