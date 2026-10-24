/*
 * Challenge 133 — Functional Behavior Predictions (Medium)
 *
 * Task: Read every fixed scenario, then fill predictions() with its exact
 * returned String, keyed by scenario name. Predict BEFORE running and explain
 * the governing rule. Change only predictions(), not the scenarios or harness.
 * Baseline: Java 21. These scenarios exercise Java 8 functional APIs.
 * Run: java 10-core-contracts/FunctionalBehaviorPredictions.java
 * Focus: bound receiver timing, null failure timing, late source binding versus
 * copied membership, stream reuse, reduction laws, and future recovery position.
 * Partition examples explicitly fix the split: never infer a particular task
 * count or subtraction result from a real parallel stream's scheduling.
 */
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FunctionalBehaviorPredictions {
    static Map<String, String> predictions() {
        throw new UnsupportedOperationException("TODO: predict every functional scenario without running it");
    }

    static Map<String, Supplier<String>> scenarios() {
        Map<String, Supplier<String>> cases = new LinkedHashMap<>();
        cases.put("bound-versus-deferred-receiver", () -> {
            Holder holder = new Holder(" first ");
            Supplier<String> bound = holder.value::trim;
            Supplier<String> deferred = () -> holder.value.trim();
            holder.value = " second ";
            return bound.get() + "," + deferred.get();
        });
        cases.put("bound-receiver-survives-field-null", () -> {
            Holder holder = new Holder(" kept ");
            Supplier<String> bound = holder.value::trim;
            holder.value = null;
            return bound.get();
        });
        cases.put("null-receiver-failure-timing", () -> {
            Holder holder = new Holder(null);
            List<String> events = new ArrayList<>();
            try {
                Supplier<String> bound = holder.value::trim;
                events.add("bound-created");
                bound.get();
            } catch (NullPointerException expected) {
                events.add("bound-null");
            }
            Supplier<String> deferred = () -> holder.value.trim();
            events.add("lambda-created");
            holder.value = " ready ";
            events.add(deferred.get());
            holder.value = null;
            try { deferred.get(); }
            catch (NullPointerException expected) { events.add("lambda-null"); }
            return events.toString();
        });
        cases.put("late-bound-array-list", () -> {
            List<String> names = new ArrayList<>(List.of("Ada"));
            Stream<String> pending = names.stream();
            names.add("Lin");
            return pending.collect(Collectors.toList()).toString();
        });
        cases.put("copied-source-membership", () -> {
            List<String> names = new ArrayList<>(List.of("Ada"));
            Stream<String> pending = new ArrayList<>(names).stream();
            names.add("Lin");
            return pending.collect(Collectors.toList()).toString();
        });
        cases.put("stream-reuse", () -> {
            Stream<String> words = Stream.of("a", "bb");
            long first = words.count();
            try { return first + "," + words.count(); }
            catch (IllegalStateException expected) {
                return first + "," + expected.getClass().getSimpleName();
            }
        });
        cases.put("parallel-valid-identity", () ->
                Integer.toString(Stream.of(1, 2, 3, 4).parallel().reduce(0, Integer::sum)));
        cases.put("wrong-identity-fixed-partitions", () -> {
            int whole = Stream.of(1, 2, 3, 4).reduce(10, Integer::sum);
            int left = Stream.of(1, 2).reduce(10, Integer::sum);
            int right = Stream.of(3, 4).reduce(10, Integer::sum);
            return whole + "," + Integer.sum(left, right);
        });
        cases.put("subtraction-fixed-partitions", () -> {
            int whole = Stream.of(1, 2, 3, 4).reduce(0, (a, b) -> a - b);
            int left = Stream.of(1, 2).reduce(0, (a, b) -> a - b);
            int right = Stream.of(3, 4).reduce(0, (a, b) -> a - b);
            return whole + "," + (left - right);
        });
        cases.put("recovery-before-parse", () -> {
            CompletableFuture<String> source = CompletableFuture.completedFuture("oops");
            CompletableFuture<Integer> parsed = source.exceptionally(error -> "0")
                    .thenApply(Integer::parseInt);
            try { return parsed.join().toString(); }
            catch (CompletionException failure) {
                return failure.getCause().getClass().getSimpleName() + "," + source.join();
            }
        });
        cases.put("recovery-after-parse", () ->
                CompletableFuture.completedFuture("oops").thenApply(Integer::parseInt)
                        .exceptionally(error -> 0).join().toString());
        cases.put("discarded-dependent-failure", () -> {
            CompletableFuture<String> source = CompletableFuture.completedFuture("oops");
            source.thenApply(Integer::parseInt);
            return source.join() + "," + source.isCompletedExceptionally();
        });
        cases.put("recovered-null-downstream", () -> {
            CompletableFuture<String> source = new CompletableFuture<>();
            source.completeExceptionally(new IllegalStateException("backend"));
            CompletableFuture<Integer> length = source.exceptionally(error -> null)
                    .thenApply(String::length);
            try { return length.join().toString(); }
            catch (CompletionException failure) {
                return failure.getCause().getClass().getSimpleName();
            }
        });
        return cases;
    }

    static final class Holder {
        String value;
        Holder(String value) { this.value = value; }
    }

    public static void main(String[] args) {
        Map<String, String> guesses = predictions();
        Map<String, Supplier<String>> cases = scenarios();
        check("every scenario has exactly one prediction", cases.keySet(), guesses.keySet());
        cases.forEach((name, scenario) -> check(name, guesses.get(name), scenario.get()));
        report("Challenge 133");
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
