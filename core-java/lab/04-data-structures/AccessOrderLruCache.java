/*
 * Challenge DS-A1 — Access-order LRU Cache (Hard)
 *
 * Task: Create a fixed-capacity Map backed by LinkedHashMap. Iteration must run
 * from least recently used to most recently used; a successful get and an
 * update both make an entry most-recently used. Inserting beyond capacity must
 * evict exactly the least-recently used entry.
 * Complete: newCache(int capacity)
 * Run: java AccessOrderLruCache.java
 */
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccessOrderLruCache {
    static <K, V> Map<K, V> newCache(int capacity) {
        // TODO: Configure access order and implement bounded eldest-entry eviction.
        throw new UnsupportedOperationException("TODO: build the LRU cache");
    }

    public static void main(String[] args) {
        Map<String, Integer> cache = newCache(3);
        cache.put("alpha", 1);
        cache.put("beta", 2);
        cache.put("gamma", 3);

        check("insertion order starts least-to-most recent", "put alpha, beta, gamma",
                List.of("alpha", "beta", "gamma"), keys(cache));

        cache.get("alpha");
        check("get refreshes recency", "get alpha",
                List.of("beta", "gamma", "alpha"), keys(cache));

        cache.put("delta", 4);
        check("overflow evicts one LRU entry", "put delta at capacity 3",
                List.of("gamma", "alpha", "delta"), keys(cache));
        check("evicted key is absent", "get beta", null, cache.get("beta"));

        cache.put("gamma", 30);
        check("update refreshes without growing", "put existing gamma",
                List.of("alpha", "delta", "gamma"), keys(cache));
        check("update replaces the value", "get gamma", 30, cache.get("gamma"));

        Map<String, Integer> one = newCache(1);
        one.put("first", 1);
        one.put("second", 2);
        check("capacity one keeps only newest", "put first, then second",
                List.of("second"), keys(one));
        checkThat("implementation is a LinkedHashMap", "newCache(1)",
                one instanceof LinkedHashMap);
        report("Challenge DS-A1");
    }

    private static List<String> keys(Map<String, Integer> map) {
        return new ArrayList<>(map.keySet());
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
