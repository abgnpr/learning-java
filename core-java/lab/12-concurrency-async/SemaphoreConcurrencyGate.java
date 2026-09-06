/*
 * Challenge 98 — Semaphore Concurrency Gate (Hard)
 *
 * Task: Run a Callable only while holding a semaphore permit. Acquisition must
 * be interruptible and every acquired permit must be released exactly once,
 * including when the action throws.
 * Complete: withPermit(Semaphore, Callable<T>)
 * Run: java SemaphoreConcurrencyGate.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SemaphoreConcurrencyGate {
    static <T> T withPermit(Semaphore semaphore, Callable<T> action) throws Exception {
        // TODO: Acquire interruptibly and release exactly once in a finally block.
        throw new UnsupportedOperationException("TODO: guard action with a permit");
    }

    public static void main(String[] args) throws Exception {
        Semaphore gate = new Semaphore(2);
        CountDownLatch twoEntered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maximum = new AtomicInteger();
        List<Thread> workers = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            workers.add(Thread.ofVirtual().start(() -> {
                try {
                    withPermit(gate, () -> {
                        int now = active.incrementAndGet();
                        maximum.accumulateAndGet(now, Math::max);
                        twoEntered.countDown();
                        release.await();
                        active.decrementAndGet();
                        return null;
                    });
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }));
        }

        if (!twoEntered.await(2, TimeUnit.SECONDS)) {
            release.countDown();
            throw new AssertionError("two workers did not acquire permits");
        }
        check("permit count limits active actions", "6 workers, 2 permits",
                2, maximum.get());
        release.countDown();
        for (Thread worker : workers) {
            worker.join();
        }
        check("all permits returned after success", gate, 2, gate.availablePermits());

        check("action failure propagates", "callable throws",
                "IllegalStateException",
                thrownSimpleName(() -> withPermit(gate, () -> {
                    throw new IllegalStateException("boom");
                })));
        check("permit returned after failure", gate, 2, gate.availablePermits());
        report("Challenge 98");
    }

    @FunctionalInterface
    interface ThrowingAction {
        void run() throws Exception;
    }

    private static String thrownSimpleName(ThrowingAction action) {
        try {
            action.run();
            return "nothing thrown";
        } catch (Exception exception) {
            return exception.getClass().getSimpleName();
        }
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
