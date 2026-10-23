/*
 * Challenge 95 — CompletableFuture Composition (Hard)
 *
 * Task: Compose dependent asynchronous stages, combine independent futures
 * without blocking worker stages, recover individual fan-out failures with
 * zero, and cancel all unfinished futures on request. Cancelling a future does
 * not establish that its underlying operation stopped.
 * All composition methods must return before pending inputs complete. Blocking
 * inside these methods fails the contract. sumSuccessful waits for every input,
 * treating individual failures as zero; an empty input produces zero.
 * Complete: lookupThenPrice, combineIndependent, sumSuccessful, cancelUnfinished.
 * Run: java CompletableFutureComposition.java
 */
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureComposition {
    static CompletableFuture<Integer> lookupThenPrice(
            String sku,
            Function<String, CompletableFuture<String>> lookupProduct,
            Function<String, CompletableFuture<Integer>> fetchPrice) {
        // TODO: Compose the dependent futures without nested CompletableFuture values.
        throw new UnsupportedOperationException("TODO: compose dependent stages");
    }

    static CompletableFuture<Integer> sumSuccessful(
            List<CompletableFuture<Integer>> futures) {
        // TODO: Recover each failure as zero, then fan in after every future completes.
        throw new UnsupportedOperationException("TODO: implement fan-out/fan-in");
    }

    static CompletableFuture<Integer> combineIndependent(
            CompletableFuture<Integer> subtotal, CompletableFuture<Integer> delivery) {
        // TODO: Combine independent results without blocking or serializing their work.
        throw new UnsupportedOperationException("TODO: combine independent stages");
    }

    static long cancelUnfinished(List<? extends CompletableFuture<?>> futures) {
        // TODO: Cancel unfinished operations and return how many accepted cancellation.
        throw new UnsupportedOperationException("TODO: cancel outstanding futures");
    }

    public static void main(String[] args) throws Exception {
        CompletableFuture<Integer> price = lookupThenPrice(
                "sku-7",
                sku -> CompletableFuture.completedFuture("product-for-" + sku),
                product -> CompletableFuture.completedFuture(product.length()));
        check("thenCompose flattens dependent stages", "sku-7", 17, price.join());

        CompletableFuture<Integer> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("provider down"));
        check("fan-in sums successes and recovers failure", "[10, failure, 7]",
                17, sumSuccessful(List.of(
                        CompletableFuture.completedFuture(10),
                        failed,
                        CompletableFuture.completedFuture(7))).join());

        CompletableFuture<Integer> first = new CompletableFuture<>();
        CompletableFuture<Integer> alreadyDone = CompletableFuture.completedFuture(2);
        CompletableFuture<Integer> second = new CompletableFuture<>();
        check("only unfinished futures accept cancellation", "pending, done, pending",
                2L, cancelUnfinished(List.of(first, alreadyDone, second)));
        check("pending futures are cancelled", "first and second",
                true, first.isCancelled() && second.isCancelled());

        ExecutorService caller = Executors.newSingleThreadExecutor(
                Thread.ofPlatform().daemon(true).name("composition-caller").factory());
        CompletableFuture<String> product = new CompletableFuture<>();
        CompletableFuture<Integer> pendingPrice = new CompletableFuture<>();
        CompletableFuture<Integer> left = new CompletableFuture<>();
        CompletableFuture<Integer> right = new CompletableFuture<>();
        CompletableFuture<Integer> goodA = new CompletableFuture<>();
        CompletableFuture<Integer> goodB = new CompletableFuture<>();
        CompletableFuture<Integer> bad = new CompletableFuture<>();
        try {
            AtomicInteger priceCalls = new AtomicInteger();
            CompletableFuture<String> requestedProduct = new CompletableFuture<>();
            CompletableFuture<Integer> dependent = returnsPromptly(caller, () -> lookupThenPrice(
                    "sku-delayed", sku -> product, name -> {
                        priceCalls.incrementAndGet(); requestedProduct.complete(name); return pendingPrice;
                    }));
            check("dependent call returns with lookup still pending", "incomplete lookup", false, dependent.isDone());
            check("price stage has not started early", "before lookup completion", 0, priceCalls.get());
            product.complete("resolved-product");
            check("resolved value reaches next stage", "lookup completes", "resolved-product",
                    requestedProduct.get(2, TimeUnit.SECONDS));
            check("dependent output waits for price", "price still pending", false, dependent.isDone());
            pendingPrice.complete(23);
            check("delayed dependent result", "price completes", 23, dependent.get(2, TimeUnit.SECONDS));
            check("dependent stage ran once", "completed chain", 1, priceCalls.get());

            CompletableFuture<Integer> combined = returnsPromptly(caller, () -> combineIndependent(left, right));
            right.complete(3);
            check("independent combination waits for both", "right completed first", false, combined.isDone());
            left.complete(20);
            check("independent combination", "both complete", 23, combined.get(2, TimeUnit.SECONDS));

            CompletableFuture<Integer> total = returnsPromptly(caller,
                    () -> sumSuccessful(List.of(goodA, bad, goodB)));
            goodB.complete(7); bad.completeExceptionally(new IllegalStateException("down"));
            check("fan-in waits for unfinished successes", "last-listed input finished first", false, total.isDone());
            goodA.complete(10);
            check("out-of-order completion retains all successes", "10 + failure + 7", 17, total.get(2, TimeUnit.SECONDS));
            check("empty fan-in identity", "[]", 0,
                    returnsPromptly(caller, () -> sumSuccessful(List.of())).get(2, TimeUnit.SECONDS));

            IllegalArgumentException lookupFailure = new IllegalArgumentException("unknown SKU");
            CompletableFuture<String> failedLookup = new CompletableFuture<>();
            failedLookup.completeExceptionally(lookupFailure);
            AtomicInteger forbiddenCalls = new AtomicInteger();
            CompletableFuture<Integer> failedChain = returnsPromptly(caller, () -> lookupThenPrice(
                    "missing", sku -> failedLookup, name -> {
                        forbiddenCalls.incrementAndGet(); return CompletableFuture.completedFuture(99);
                    }));
            check("lookup failure keeps original cause", "failed upstream", lookupFailure, failureOf(failedChain));
            check("failed lookup never invokes pricing", "failed upstream", 0, forbiddenCalls.get());
            CompletableFuture<Integer> failedCombined = returnsPromptly(caller,
                    () -> combineIndependent(CompletableFuture.failedFuture(lookupFailure),
                            CompletableFuture.completedFuture(1)));
            check("independent failure propagates", "one input failed", lookupFailure, failureOf(failedCombined));
        } finally {
            // Unblock even an incorrect implementation before stopping the harness.
            product.complete("cleanup"); pendingPrice.complete(0); left.complete(0); right.complete(0);
            goodA.complete(0); goodB.complete(0); bad.complete(0);
            caller.shutdownNow();
            if (!caller.awaitTermination(2, TimeUnit.SECONDS)) {
                throw new AssertionError("composition caller did not terminate");
            }
        }
        report("Challenge 95");
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static <T> CompletableFuture<T> returnsPromptly(
            ExecutorService caller, Supplier<CompletableFuture<T>> build) throws Exception {
        try {
            return caller.submit(build::get).get(2, TimeUnit.SECONDS);
        } catch (TimeoutException blocked) {
            throw new AssertionError("Composition blocked on an incomplete input; return a stage instead", blocked);
        }
    }

    private static Throwable failureOf(CompletableFuture<?> stage) throws Exception {
        try {
            stage.get(2, TimeUnit.SECONDS);
            throw new AssertionError("expected exceptional completion");
        } catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            while (cause instanceof CompletionException && cause.getCause() != null) cause = cause.getCause();
            return cause;
        }
    }

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
