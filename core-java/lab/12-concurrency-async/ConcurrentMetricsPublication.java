/*
 * Challenge 96 — Concurrent Metrics Publication (Hard)
 *
 * Task: Accumulate named counters concurrently without a global lock. Publish
 * an immutable point-in-time snapshot through an atomic reference so readers
 * see either the previous complete snapshot or the next complete snapshot.
 * Complete: Metrics.increment(...), publish(), snapshot()
 * Run: java ConcurrentMetricsPublication.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public class ConcurrentMetricsPublication {
    static final class Metrics {
        private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
        private final AtomicReference<Map<String, Long>> published =
                new AtomicReference<>(Map.of());

        void increment(String name) {
            // TODO: Atomically initialize and increment the named counter.
            throw new UnsupportedOperationException("TODO: increment concurrent metric");
        }

        void publish() {
            // TODO: Build an immutable complete snapshot, then publish it atomically.
            throw new UnsupportedOperationException("TODO: publish metrics snapshot");
        }

        Map<String, Long> snapshot() {
            // TODO: Return the most recently published immutable snapshot.
            throw new UnsupportedOperationException("TODO: read published snapshot");
        }
    }

    public static void main(String[] args) throws Exception {
        Metrics metrics = new Metrics();
        List<Thread> workers = new ArrayList<>();
        for (int worker = 0; worker < 4; worker++) {
            workers.add(Thread.ofVirtual().start(() -> {
                for (int i = 0; i < 1_000; i++) {
                    metrics.increment("requests");
                }
                metrics.increment("workers");
            }));
        }
        for (Thread worker : workers) {
            worker.join();
        }

        check("unpublished state remains previous snapshot", "before publish",
                Map.of(), metrics.snapshot());
        metrics.publish();
        check("concurrent increments are retained", "4 workers x 1000",
                4_000L, metrics.snapshot().get("requests"));
        check("multiple keys publish together", "workers counter",
                4L, metrics.snapshot().get("workers"));
        checkThat("published snapshot is immutable", metrics.snapshot(),
                isUnmodifiable(metrics.snapshot()));
        report("Challenge 96");
    }

    private static boolean isUnmodifiable(Map<?, ?> values) {
        try {
            values.clear();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
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
