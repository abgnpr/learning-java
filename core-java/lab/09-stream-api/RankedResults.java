/*
 * Challenge 131 — Ranked Results and Ordered Maps (Medium)
 *
 * Task: Rank named counts by count DESC, then name in ascending natural
 * lexicographical order. Counts span the entire long range, including negatives.
 * Names and values are non-null. Do not change the source map.
 * - ranking: return every Metric in that order as an unmodifiable detached list.
 * - top: return the first k metrics in the same order, unmodifiable. Reject k < 0;
 *   k == 0 returns empty and k > size returns all.
 * - best: return the first-ranked metric, or Optional.empty() for empty input.
 * - orderedCounts: return an unmodifiable detached map whose ITERATION order
 *   is the ranking order, including the name tie-breaker.
 * Complete: ranking, top, best, orderedCounts. Baseline: Java 21.
 * Required focus: comparator composition, sorted/limit, min/max direction,
 * Map.Entry ordering, LinkedHashMap, and explicit result mutability.
 * Run: java 09-stream-api/RankedResults.java
 * Explain: why max with a best-first comparator chooses the wrong end; why a
 * sorted stream alone does not make an arbitrary collected map iterate in order.
 */
import java.util.*;

public final class RankedResults {
    record Metric(String name, long count) { }

    static List<Metric> ranking(Map<String, Long> counts) {
        throw new UnsupportedOperationException("TODO: rank counts descending and names ascending");
    }

    static List<Metric> top(Map<String, Long> counts, int k) {
        throw new UnsupportedOperationException("TODO: apply a validated top-N boundary");
    }

    static Optional<Metric> best(Map<String, Long> counts) {
        throw new UnsupportedOperationException("TODO: select the first element under ranking order");
    }

    static Map<String, Long> orderedCounts(Map<String, Long> counts) {
        throw new UnsupportedOperationException("TODO: retain ranking order in a detached unmodifiable map");
    }

    public static void main(String[] args) {
        Map<String, Long> source = new LinkedHashMap<>();
        source.put("Ben", 3L); source.put("Zoe", 8L); source.put("Ada", 8L); source.put("Debt", -1L);
        List<String> sourceOrder = new ArrayList<>(source.keySet());
        List<Metric> expected = List.of(new Metric("Ada", 8), new Metric("Zoe", 8),
                new Metric("Ben", 3), new Metric("Debt", -1));
        List<Metric> ranked = ranking(source);
        check("value DESC then natural key ASC", expected, ranked);
        check("top two uses deterministic ties", expected.subList(0, 2), top(source, 2));
        check("zero requested", List.of(), top(source, 0));
        check("oversized request returns all", expected, top(source, 100));
        expect(IllegalArgumentException.class, () -> top(source, -1));
        check("best uses first-ranked end", Optional.of(new Metric("Ada", 8)), best(source));
        Map<String, Long> ordered = orderedCounts(source);
        check("ordered map iteration expresses ranking", List.of("Ada", "Zoe", "Ben", "Debt"),
                new ArrayList<>(ordered.keySet()));
        check("ordered map retains all values", source, ordered);
        check("input iteration remains untouched", sourceOrder, new ArrayList<>(source.keySet()));
        check("input values remain untouched", Map.of("Ben", 3L, "Zoe", 8L, "Ada", 8L, "Debt", -1L), source);
        expect(UnsupportedOperationException.class, () -> ranked.clear());
        expect(UnsupportedOperationException.class, () -> top(source, 2).add(new Metric("x", 1)));
        expect(UnsupportedOperationException.class, () -> ordered.put("x", 1L));
        expect(UnsupportedOperationException.class, () -> ordered.entrySet().iterator().next().setValue(99L));
        source.put("Ada", 999L); source.put("New", 1000L);
        check("list is detached from source changes", expected, ranked);
        check("ordered map values are detached", 8L, ordered.get("Ada"));
        check("ordered map keys are detached", false, ordered.containsKey("New"));

        Map<String, Long> extremes = Map.of("low", Long.MIN_VALUE, "high", Long.MAX_VALUE, "zero", 0L);
        check("long comparison never narrows a difference", List.of(new Metric("high", Long.MAX_VALUE),
                new Metric("zero", 0), new Metric("low", Long.MIN_VALUE)), ranking(extremes));
        check("all-equal scores use natural case-sensitive names", List.of(new Metric("Ada", 1),
                new Metric("Zoe", 1), new Metric("ada", 1)), ranking(Map.of("ada", 1L, "Zoe", 1L, "Ada", 1L)));
        check("empty ranking", List.of(), ranking(Map.of()));
        check("empty top", List.of(), top(Map.of(), 3));
        check("empty best", Optional.empty(), best(Map.of()));
        check("empty ordered map", Map.of(), orderedCounts(Map.of()));
        check("one candidate", Optional.of(new Metric("only", -9)), best(Map.of("only", -9L)));
        report("Challenge 131");
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
