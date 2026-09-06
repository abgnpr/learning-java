/*
 * Challenge 99 — Virtual-thread Blocking Fan-out (Hard)
 *
 * Task: Start one virtual thread per input for a blocking function, wait for
 * every result, preserve input order, and cancel unfinished tasks when one
 * fails. Do not use a shared mutable result list from worker threads.
 * Complete: mapBlocking(List<T>, BlockingFunction<T,R>)
 * Run: java VirtualThreadFanout.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VirtualThreadFanout {
    @FunctionalInterface
    interface BlockingFunction<T, R> {
        R apply(T value) throws Exception;
    }

    static <T, R> List<R> mapBlocking(
            List<T> inputs, BlockingFunction<? super T, ? extends R> function)
            throws Exception {
        // TODO: Fan out with virtual FutureTasks, collect in order, cancel on failure.
        throw new UnsupportedOperationException("TODO: map blocking work on virtual threads");
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch allStarted = new CountDownLatch(3);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger virtualMappers = new AtomicInteger();
        AtomicReference<List<Integer>> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread coordinator = Thread.ofVirtual().start(() -> {
            try {
                result.set(mapBlocking(List.of(1, 2, 3), value -> {
                    if (Thread.currentThread().isVirtual()) {
                        virtualMappers.incrementAndGet();
                    }
                    allStarted.countDown();
                    release.await();
                    return value * value;
                }));
            } catch (Throwable thrown) {
                failure.set(thrown);
            }
        });

        if (!allStarted.await(2, TimeUnit.SECONDS)) {
            release.countDown();
            coordinator.interrupt();
            coordinator.join();
            throw new AssertionError("blocking calls did not fan out");
        }
        check("all blocking calls fan out before release", "three inputs",
                0L, allStarted.getCount());
        release.countDown();
        coordinator.join();
        check("successful fan-out has no failure", "coordinator", null, failure.get());
        check("every mapper uses a virtual thread", "three mapper invocations",
                3, virtualMappers.get());
        check("results preserve input order", "[1,2,3]",
                List.of(1, 4, 9), result.get());

        check("worker failure propagates", "second input throws",
                "IllegalArgumentException",
                thrownSimpleName(() -> mapBlocking(List.of(1, 2, 3), value -> {
                    if (value == 2) {
                        throw new IllegalArgumentException("bad input");
                    }
                    return value;
                })));
        report("Challenge 99");
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
