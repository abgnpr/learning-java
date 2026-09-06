/*
 * Challenge 93 — Cooperative Cancellation (Hard)
 *
 * Task: Run interruptible steps in order. Check for cancellation between
 * steps, propagate InterruptedException from a running step, and never begin a
 * later step after cancellation has been observed.
 * Complete: runSteps(List<InterruptibleStep>)
 * Run: java CooperativeCancellation.java
 */
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CooperativeCancellation {
    @FunctionalInterface
    interface InterruptibleStep {
        void run() throws InterruptedException;
    }

    static int runSteps(List<InterruptibleStep> steps) throws InterruptedException {
        // TODO: Cooperate with interruption and return the completed-step count.
        throw new UnsupportedOperationException("TODO: implement cooperative cancellation");
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        AtomicBoolean laterStepRan = new AtomicBoolean();
        AtomicBoolean interruptionPropagated = new AtomicBoolean();

        Thread worker = Thread.ofVirtual().start(() -> {
            try {
                runSteps(List.of(
                        () -> {
                            entered.countDown();
                            new CountDownLatch(1).await();
                        },
                        () -> laterStepRan.set(true)));
            } catch (InterruptedException expected) {
                interruptionPropagated.set(true);
            }
        });

        if (!entered.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("worker did not enter the first step");
        }
        worker.interrupt();
        worker.join();

        check("InterruptedException propagates", "interrupt blocked first step",
                true, interruptionPropagated.get());
        check("later work never starts", "second step", false, laterStepRan.get());

        Thread.currentThread().interrupt();
        try {
            check("pre-existing interruption stops before first step", "interrupted caller",
                    "InterruptedException",
                    thrownSimpleName(() -> runSteps(List.of(() -> laterStepRan.set(true)))));
        } finally {
            Thread.interrupted();
        }
        report("Challenge 93");
    }

    @FunctionalInterface
    interface InterruptibleAction {
        void run() throws InterruptedException;
    }

    private static String thrownSimpleName(InterruptibleAction action) {
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
