/*
 * Challenge 94 — Bounded Executor Lifecycle (Hard)
 *
 * Task: Create a fixed-size ThreadPoolExecutor with a bounded ArrayBlockingQueue
 * and abort rejection. Shut it down orderly, wait only for the supplied
 * timeout, then cancel queued/running work if necessary.
 * Complete: newExecutor(...), shutdownAndAwait(...)
 * Run: java BoundedExecutorLifecycle.java
 */
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BoundedExecutorLifecycle {
    static ExecutorService newExecutor(int workers, int queueCapacity) {
        // TODO: Build a bounded fixed-size executor with abort rejection.
        throw new UnsupportedOperationException("TODO: create bounded executor");
    }

    static boolean shutdownAndAwait(ExecutorService executor, Duration timeout) {
        // TODO: Orderly shutdown, bounded await, then shutdownNow; preserve interruption.
        throw new UnsupportedOperationException("TODO: manage executor shutdown");
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = newExecutor(1, 1);
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        executor.execute(() -> {
            running.countDown();
            try {
                release.await();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        if (!running.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("executor did not start the first task");
        }
        executor.execute(() -> { });

        check("third task is rejected when worker and queue are full", "1 worker, queue 1",
                "RejectedExecutionException",
                thrownSimpleName(() -> executor.execute(() -> { })));

        release.countDown();
        check("orderly completion before timeout", "release running task",
                true, shutdownAndAwait(executor, Duration.ofSeconds(2)));
        checkThat("executor is terminated", executor, executor.isTerminated());

        ExecutorService slow = newExecutor(1, 1);
        CountDownLatch slowStarted = new CountDownLatch(1);
        slow.execute(() -> {
            slowStarted.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        if (!slowStarted.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("executor did not start the slow task");
        }
        check("timeout escalates to cancellation", "10 ms shutdown timeout",
                true, shutdownAndAwait(slow, Duration.ofMillis(10)));
        report("Challenge 94");
    }

    private static String thrownSimpleName(Runnable action) {
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
