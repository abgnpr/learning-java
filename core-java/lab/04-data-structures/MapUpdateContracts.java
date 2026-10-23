/*
 * Challenge 125 — Map Update Contracts (Medium)
 *
 * Task: Implement the exact Map callback contracts below using containsKey,
 * computeIfAbsent, merge, computeIfPresent and putIfAbsent. These are ordinary
 * HashMap contracts; do not claim they make arbitrary maps thread-safe.
 * Complete: inspect, cachedLookup, adjust, refresh, insertIfAbsent.
 * Run: java 04-data-structures/MapUpdateContracts.java
 * Explain: when a callback runs, what null means on input/output, and why a
 * ConcurrentHashMap rejects null keys and values.
 */
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public final class MapUpdateContracts {
    record Presence(boolean present, String value) { }

    static Presence inspect(Map<String, String> values, String key) {
        throw new UnsupportedOperationException("TODO: distinguish absent from mapped to null");
    }

    static String cachedLookup(Map<String, String> cache, String key, Function<String, String> load) {
        throw new UnsupportedOperationException("TODO: compute only absent or null mappings");
    }

    // Insert delta for an absent/null mapping, even if delta is zero. Otherwise
    // add to the old value; remove the entry when that combined value is zero.
    static Integer adjust(Map<String, Integer> counts, String key, int delta) {
        throw new UnsupportedOperationException("TODO: merge with a zero-removal policy");
    }

    // Invoke update only for an existing NON-NULL value; a null result removes it.
    static String refresh(Map<String, String> cache, String key, BiFunction<String, String, String> update) {
        throw new UnsupportedOperationException("TODO: compute only present non-null mappings");
    }

    // Return the previous non-null value, or null when value was installed.
    // A mapped null is treated as absent. value is non-null.
    static String insertIfAbsent(Map<String, String> cache, String key, String value) {
        throw new UnsupportedOperationException("TODO: insert without replacing a non-null value");
    }

    public static void main(String[] args) {
        Map<String, String> cache = new HashMap<>();
        cache.put("null", null); cache.put("present", "old");
        check("missing key", new Presence(false, null), inspect(cache, "missing"));
        check("explicit null", new Presence(true, null), inspect(cache, "null"));
        check("ordinary entry", new Presence(true, "old"), inspect(cache, "present"));
        AtomicInteger loads = new AtomicInteger();
        Function<String, String> loader = key -> { loads.incrementAndGet(); return "loaded:" + key; };
        check("present value bypasses loader", "old", cachedLookup(cache, "present", loader));
        check("absent key loads", "loaded:new", cachedLookup(cache, "new", loader));
        check("mapped null loads too", "loaded:null", cachedLookup(cache, "null", loader));
        check("loaded value is reused", "loaded:new", cachedLookup(cache, "new", loader));
        check("loader ran only twice", 2, loads.get());
        check("null loader result is returned", null, cachedLookup(cache, "none", k -> null));
        check("null result creates no entry", false, cache.containsKey("none"));
        cache.put("still-null", null);
        cachedLookup(cache, "still-null", k -> null);
        check("existing null mapping is retained", new Presence(true, null), inspect(cache, "still-null"));
        IllegalStateException failure = new IllegalStateException("backend");
        check("loader failure escapes unchanged", failure,
                expect(IllegalStateException.class, () -> cachedLookup(cache, "broken", k -> { throw failure; })));
        check("failed load is not cached", false, cache.containsKey("broken"));

        Map<String, Integer> counts = new HashMap<>();
        check("absent merge inserts", 4, adjust(counts, "a", 4));
        check("present merge adds", 7, adjust(counts, "a", 3));
        check("zero sum removes mapping", null, adjust(counts, "a", -7));
        check("removed means absent", false, counts.containsKey("a"));
        counts.put("nullable", null);
        check("null mapping takes supplied value", 2, adjust(counts, "nullable", 2));
        check("absent zero is inserted without remapping", 0, adjust(counts, "zero", 0));
        check("inserted zero is a real mapping", true, counts.containsKey("zero"));

        AtomicInteger updates = new AtomicInteger();
        BiFunction<String, String, String> updater = (key, old) -> {
            updates.incrementAndGet(); return key + ":" + old;
        };
        check("absent refresh does nothing", null, refresh(cache, "absent", updater));
        check("null refresh does nothing", null, refresh(cache, "still-null", updater));
        check("present refresh sees key and value", "present:old", refresh(cache, "present", updater));
        check("only one remapping", 1, updates.get());
        refresh(cache, "present", (key, old) -> null);
        check("null remapping result removes", false, cache.containsKey("present"));
        cache.put("keep", "old"); cache.put("replace-null", null);
        check("putIfAbsent returns prior value", "old", insertIfAbsent(cache, "keep", "new"));
        check("existing non-null value survives", "old", cache.get("keep"));
        check("mapped null accepts insertion", null, insertIfAbsent(cache, "replace-null", "new"));
        check("null mapping was replaced", "new", cache.get("replace-null"));
        check("absent key accepts insertion", null, insertIfAbsent(cache, "fresh", "new"));
        check("inserted mapping exists", "new", cache.get("fresh"));
        report("Challenge 125");
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
