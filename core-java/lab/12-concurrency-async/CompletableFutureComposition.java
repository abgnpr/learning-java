/*
 * Challenge 95 — CompletableFuture Composition (Hard)
 *
 * Task: Compose dependent asynchronous stages, combine independent futures
 * without blocking worker stages, recover individual fan-out failures with
 * zero, and cancel all unfinished operations on request.
 * Complete: lookupThenPrice(...), sumSuccessful(...), cancelUnfinished(...)
 * Run: java CompletableFutureComposition.java
 */
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    static long cancelUnfinished(List<? extends CompletableFuture<?>> futures) {
        // TODO: Cancel unfinished operations and return how many accepted cancellation.
        throw new UnsupportedOperationException("TODO: cancel outstanding futures");
    }

    public static void main(String[] args) {
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
        report("Challenge 95");
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
