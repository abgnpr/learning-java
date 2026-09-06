/*
 * Challenge 97 — Condition-based Bounded Buffer (Hard)
 *
 * Task: Implement a bounded FIFO buffer with one ReentrantLock and distinct
 * not-empty/not-full Conditions. Wait in while loops and let interruption
 * cancel blocked put/take operations.
 * Complete: Buffer constructor, put(T), take()
 * Run: java ConditionBoundedBuffer.java
 */
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionBoundedBuffer {
    static final class Buffer<T> {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();
        private final Deque<T> items = new ArrayDeque<>();
        private final int capacity;

        Buffer(int capacity) {
            // TODO: Reject non-positive capacity and store the bound.
            throw new UnsupportedOperationException("TODO: initialize bounded buffer");
        }

        void put(T value) throws InterruptedException {
            // TODO: Await room, append the value, and signal a waiting consumer.
            throw new UnsupportedOperationException("TODO: put into buffer");
        }

        T take() throws InterruptedException {
            // TODO: Await an item, remove FIFO head, and signal a waiting producer.
            throw new UnsupportedOperationException("TODO: take from buffer");
        }
    }

    public static void main(String[] args) throws Exception {
        Buffer<String> buffer = new Buffer<>(1);
        buffer.put("first");

        CountDownLatch producerAttempted = new CountDownLatch(1);
        Thread producer = Thread.ofVirtual().start(() -> {
            try {
                producerAttempted.countDown();
                buffer.put("second");
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });

        producerAttempted.await();
        awaitWaiting(producer);
        checkThat("producer waits while buffer is full", producer, producer.isAlive());
        check("FIFO first item", "take from full buffer", "first", buffer.take());
        producer.join();
        check("waiting producer continues after signal", "take after producer resumes",
                "second", buffer.take());

        AtomicReference<String> interruption = new AtomicReference<>("none");
        Thread consumer = Thread.ofVirtual().start(() -> {
            try {
                buffer.take();
            } catch (InterruptedException expected) {
                interruption.set("InterruptedException");
            }
        });
        awaitWaiting(consumer);
        consumer.interrupt();
        consumer.join();
        check("blocked take is interruptible", "interrupt empty-buffer consumer",
                "InterruptedException", interruption.get());
        report("Challenge 97");
    }

    private static void awaitWaiting(Thread thread) {
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (thread.getState() != Thread.State.WAITING
                && thread.getState() != Thread.State.TIMED_WAITING) {
            if (System.nanoTime() >= deadline) {
                throw new AssertionError("thread did not enter a waiting state");
            }
            Thread.onSpinWait();
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
