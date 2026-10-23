/*
 * Challenge 128 — CompletableFuture Execution and Failure (Hard)
 *
 * Task: Schedule a transformation on the supplied Executor; observe an outcome
 * without recovering it; recover failures with a supplied value; and cancel a
 * future. Use thenApplyAsync, whenComplete, exceptionally and cancel respectively.
 * The cancellation checks deliberately prove that CompletableFuture.cancel(true)
 * does not interrupt its running supplier. Worker cancellation needs its own
 * cooperative mechanism (see CooperativeCancellation).
 * Handle both outcomes with a supplied BiFunction to transform success/failure
 * into a different result type. Complete: transform, observe, recover, outcome, cancel.
 * Run: java 12-concurrency-async/CompletableFutureExecution.java
 * Explain: non-Async stage execution, explicit versus default executors,
 * handle versus exceptionally versus whenComplete, get versus join.
 */
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public final class CompletableFutureExecution {
    static CompletableFuture<Integer> transform(CompletableFuture<Integer> source,
            IntUnaryOperator operation, Executor executor) {
        throw new UnsupportedOperationException("TODO: schedule on the supplied executor");
    }

    static CompletableFuture<Integer> observe(CompletableFuture<Integer> source,
            BiConsumer<Integer, Throwable> observer) {
        throw new UnsupportedOperationException("TODO: observe while preserving the outcome");
    }

    static CompletableFuture<Integer> recover(CompletableFuture<Integer> source,
            Function<Throwable, Integer> fallback) {
        throw new UnsupportedOperationException("TODO: recover exceptional outcomes only");
    }

    static CompletableFuture<String> outcome(CompletableFuture<Integer> source,
            BiFunction<Integer, Throwable, String> handler) {
        throw new UnsupportedOperationException("TODO: handle either outcome and transform its type");
    }

    static boolean cancel(CompletableFuture<?> source) {
        throw new UnsupportedOperationException("TODO: cancel the future without claiming worker interruption");
    }

    public static void main(String[] args) throws Exception {
        ManualExecutor executor = new ManualExecutor();
        AtomicInteger transformations = new AtomicInteger();
        CompletableFuture<Integer> transformed = transform(CompletableFuture.completedFuture(4), n -> {
            transformations.incrementAndGet(); return n * 3;
        }, executor);
        check("explicit executor received one action", 1, executor.tasks.size());
        check("callback has not run on construction", 0, transformations.get());
        check("result remains pending until executor runs", false, transformed.isDone());
        executor.runNext();
        check("executor produces the result", 12, transformed.get(2, TimeUnit.SECONDS));
        check("callback invoked once", 1, transformations.get());
        IllegalStateException failure = new IllegalStateException("provider");
        List<Object> observations = new ArrayList<>();
        CompletableFuture<Integer> observed = observe(CompletableFuture.failedFuture(failure), (value, error) -> {
            observations.add(value); observations.add(error);
        });
        check("observer receives no success value", null, observations.get(0));
        check("observer receives original failure", failure, observations.get(1));
        check("observation does not recover", failure, failureOf(observed));
        AtomicInteger recoveries = new AtomicInteger();
        Function<Throwable, Integer> fallback = error -> { recoveries.incrementAndGet(); return -1; };
        check("recovery supplies a replacement", -1, recover(observed, fallback).get(2, TimeUnit.SECONDS));
        check("success bypasses recovery", 9, recover(CompletableFuture.completedFuture(9), fallback).get(2, TimeUnit.SECONDS));
        check("fallback ran only for failure", 1, recoveries.get());
        AtomicInteger handled = new AtomicInteger();
        BiFunction<Integer, Throwable, String> handler = (value, error) -> {
            handled.incrementAndGet();
            return error == null ? "ok:" + value : "failed:" + error.getMessage();
        };
        check("handle transforms a success", "ok:7",
                outcome(CompletableFuture.completedFuture(7), handler).get(2, TimeUnit.SECONDS));
        check("handle may recover a failure into another type", "failed:provider",
                outcome(CompletableFuture.failedFuture(failure), handler).get(2, TimeUnit.SECONDS));
        check("handle ran for both outcomes", 2, handled.get());
        check("successful observation retains value", 5, observe(CompletableFuture.completedFuture(5), (v, e) -> {
            check("success observer has no failure", null, e);
        }).get(2, TimeUnit.SECONDS));
        IllegalArgumentException observerFailure = new IllegalArgumentException("observer");
        check("observer failure fails a successful stage", observerFailure,
                failureOf(observe(CompletableFuture.completedFuture(1), (v, e) -> { throw observerFailure; })));

        ExecutorService worker = Executors.newSingleThreadExecutor(
                Thread.ofPlatform().daemon(true).name("cancellation-supplier").factory());
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1), finished = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        try {
            CompletableFuture<Integer> running = CompletableFuture.supplyAsync(() -> {
                started.countDown();
                try { release.await(); return 42; }
                catch (InterruptedException cancelled) { interrupted.set(true); Thread.currentThread().interrupt(); return -1; }
                finally { finished.countDown(); }
            }, worker);
            if (!started.await(2, TimeUnit.SECONDS)) throw new AssertionError("supplier never started");
            check("pending future accepts cancellation", true, cancel(running));
            check("future records cancellation", true, running.isCancelled());
            expect(CancellationException.class, () -> running.get(2, TimeUnit.SECONDS));
            release.countDown();
            check("supplier still finishes its work", true, finished.await(2, TimeUnit.SECONDS));
            check("cancel(true) did not interrupt supplier", false, interrupted.get());
            check("completed future rejects cancellation", false, cancel(CompletableFuture.completedFuture(1)));
        } finally {
            release.countDown(); worker.shutdownNow();
            if (!worker.awaitTermination(2, TimeUnit.SECONDS)) throw new AssertionError("supplier did not terminate");
        }
        report("Challenge 128");
    }

    // ---- test harness (not part of the exercise) ----
    static final class ManualExecutor implements Executor {
        final Deque<Runnable> tasks = new ArrayDeque<>();
        @Override public void execute(Runnable action) { tasks.addLast(action); }
        void runNext() {
            if (tasks.isEmpty()) throw new AssertionError("nothing scheduled on supplied executor");
            tasks.removeFirst().run();
        }
    }
    private static Throwable failureOf(CompletableFuture<?> stage) throws Exception {
        try { stage.get(2, TimeUnit.SECONDS); throw new AssertionError("expected failure"); }
        catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            while (cause instanceof CompletionException && cause.getCause() != null) cause = cause.getCause();
            return cause;
        }
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
