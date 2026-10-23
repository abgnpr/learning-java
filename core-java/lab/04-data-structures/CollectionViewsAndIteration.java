/*
 * Challenge 124 — Collection Views and Iteration (Medium)
 *
 * Task: Distinguish a live view, a detached snapshot, and a deeply detached
 * nested snapshot. Remove negatives in place using Iterator.remove (no index
 * loop). Observe a CopyOnWriteArrayList iterator created BEFORE an append.
 * Inputs and their elements are non-null. Do not change caller data except
 * where the method explicitly requests it.
 * Complete: the six marked methods. Run: java 04-data-structures/CollectionViewsAndIteration.java
 * Explain: why an unmodifiable collection may contain mutable elements;
 * why ConcurrentModificationException is not a synchronization guarantee;
 * snapshot iteration versus ConcurrentHashMap's weak consistency.
 */
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CollectionViewsAndIteration {
    record Views(List<String> live, List<String> snapshot) { }

    static List<String> arrayView(String[] values) {
        throw new UnsupportedOperationException("TODO: expose a fixed-size array-backed view");
    }

    static Views views(List<String> source) {
        throw new UnsupportedOperationException("TODO: return an unmodifiable live view and snapshot");
    }

    static List<List<String>> deepSnapshot(List<List<String>> rows) {
        throw new UnsupportedOperationException("TODO: snapshot both nesting levels");
    }

    static void removeNegatives(List<Integer> values) {
        throw new UnsupportedOperationException("TODO: remove through an iterator");
    }

    static List<Integer> snapshotWhileAppending(CopyOnWriteArrayList<Integer> values, int extra) {
        throw new UnsupportedOperationException("TODO: create iterator, append, then traverse iterator");
    }

    static Set<String> liveKeys(Map<String, Integer> values) {
        throw new UnsupportedOperationException("TODO: return the map's key view");
    }

    public static void main(String[] args) {
        String[] array = {"a", "b"};
        List<String> backed = arrayView(array);
        backed.set(0, "changed");
        check("set writes through to array", "changed", array[0]);
        array[1] = "external";
        check("array writes reach view", List.of("changed", "external"), backed);
        expect(UnsupportedOperationException.class, () -> backed.add("x"));
        expect(UnsupportedOperationException.class, () -> backed.remove(0));

        List<String> source = new ArrayList<>(List.of("original"));
        Views result = views(source);
        source.set(0, "replacement"); source.add("new");
        check("wrapper sees backing changes", source, result.live());
        check("snapshot is detached", List.of("original"), result.snapshot());
        expect(UnsupportedOperationException.class, () -> result.live().set(0, "x"));
        expect(UnsupportedOperationException.class, () -> result.snapshot().add("x"));

        List<String> inner = new ArrayList<>(List.of("reader"));
        List<List<String>> rows = new ArrayList<>(List.of(inner));
        List<List<String>> deep = deepSnapshot(rows);
        inner.add("admin"); rows.clear();
        check("both levels are detached", List.of(List.of("reader")), deep);
        expect(UnsupportedOperationException.class, () -> deep.add(List.of("other")));
        expect(UnsupportedOperationException.class, () -> deep.get(0).add("owner"));

        List<Integer> numbers = new LinkedList<>(List.of(-1, -2, 0, 3, -4, -5));
        removeNegatives(numbers);
        check("adjacent negatives and the tail are removed", List.of(0, 3), numbers);
        List<Integer> allNegative = new ArrayList<>(List.of(-1, -2));
        removeNegatives(allNegative);
        check("all removed", List.of(), allNegative);
        removeNegatives(new ArrayList<>());

        CopyOnWriteArrayList<Integer> copyOnWrite = new CopyOnWriteArrayList<>(List.of(1, 2));
        check("iterator sees its creation snapshot", List.of(1, 2), snapshotWhileAppending(copyOnWrite, 3));
        check("append still changes the collection", List.of(1, 2, 3), copyOnWrite);
        Map<String, Integer> map = new HashMap<>(Map.of("a", 1, "b", 2));
        Set<String> keys = liveKeys(map);
        keys.remove("a");
        check("removing a view key removes the entry", Map.of("b", 2), map);
        map.put("c", 3);
        check("map additions appear in the key view", Set.of("b", "c"), keys);
        expect(UnsupportedOperationException.class, () -> keys.add("d"));
        report("Challenge 124");
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
