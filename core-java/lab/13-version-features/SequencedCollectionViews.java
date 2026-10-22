/*
 * Challenge 116: Sequenced Collection Views (Medium)
 * Java feature: Java 21 sequenced collections (JEP 431).
 *
 * Task: Move the last queue item to the front and return the live reversed
 * view. Do not copy or call Collections.reverse.
 *
 * Complete: promoteLastAndReverse(...).
 * Run on JDK 21+: java SequencedCollectionViews.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

public final class SequencedCollectionViews {
    private SequencedCollectionViews() {
    }

    static <T> SequencedCollection<T> promoteLastAndReverse(
            SequencedCollection<T> queue) {
        // TODO: removeLast, addFirst, and return reversed(). Reject an empty queue.
        throw new UnsupportedOperationException("TODO: use both ends and a reversed view");
    }

    public static void main(String[] args) {
        SequencedCollection<String> queue =
                new ArrayList<>(List.of("alpha", "beta", "gamma"));

        SequencedCollection<String> backwards = promoteLastAndReverse(queue);
        check("last item moves to front", "original collection",
                List.of("gamma", "alpha", "beta"), queue);
        check("result is reverse encounter order", "returned view",
                List.of("beta", "alpha", "gamma"), backwards);

        backwards.addFirst("tail");
        check("mutation through reversed view reaches the original", "addFirst on view",
                List.of("gamma", "alpha", "beta", "tail"), queue);

        check("empty collection is rejected", "[]", "NoSuchElementException",
                thrownType(() -> promoteLastAndReverse(new ArrayList<>())));
        report("Challenge 116");
    }

    @FunctionalInterface
    interface Action {
        void run();
    }

    private static String thrownType(Action action) {
        try {
            action.run();
            return "nothing thrown";
        } catch (RuntimeException exception) {
            return exception.getClass().getSimpleName();
        }
    }

    private static int passes;
    private static int failures;

    private static void check(String label, Object input, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      input:    " + input);
        System.out.println("      expected: " + expected);
        System.out.println("      actual:   " + actual);
    }

    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) throw new AssertionError(challenge + " failed");
        System.out.println(challenge + " passed.");
    }
}
