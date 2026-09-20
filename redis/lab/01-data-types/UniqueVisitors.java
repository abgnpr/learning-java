/*
 * Redis Lab 3 — unique visitors in a set (Easy)
 *
 * Task: Count distinct visitors per day without storing a row per hit. A set
 * answers "have I seen this one before?" in the write itself, so recording a
 * visit tells you whether the visitor was new. Comparing two days is then set
 * arithmetic the server performs: who came back, and who was new on day two.
 * Complete: record, uniqueCount, returning and newOn
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 01-data-types/UniqueVisitors.java
 */
import java.util.Set;
import redis.clients.jedis.Jedis;

public final class UniqueVisitors {
    private UniqueVisitors() {
    }

    /** Records a visit; returns true only the first time this visitor is seen. */
    static boolean record(Jedis jedis, String dayKey, String visitorId) {
        // TODO: add to the set and let the reply tell you whether it was new.
        throw new UnsupportedOperationException("TODO: record a visit");
    }

    /** Number of distinct visitors recorded for that day. */
    static long uniqueCount(Jedis jedis, String dayKey) {
        // TODO: ask the server for the set's size rather than fetching members.
        throw new UnsupportedOperationException("TODO: count unique visitors");
    }

    /** Visitors present on both days. */
    static Set<String> returning(Jedis jedis, String dayOne, String dayTwo) {
        // TODO: intersect the two day sets on the server.
        throw new UnsupportedOperationException("TODO: find returning visitors");
    }

    /** Visitors on the second day who were absent on the first. */
    static Set<String> newOn(Jedis jedis, String dayOne, String dayTwo) {
        // TODO: subtract the first day from the second, minding the argument order.
        throw new UnsupportedOperationException("TODO: find first-time visitors");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            String mon = PREFIX + "visits:mon";
            String tue = PREFIX + "visits:tue";

            checkThat("a first sighting is new", "asha on Monday", record(jedis, mon, "asha"));
            checkThat("the same visitor again is not new", "asha on Monday again", !record(jedis, mon, "asha"));
            record(jedis, mon, "bala");
            record(jedis, mon, "chen");
            check("repeat hits do not inflate the count", mon, 3L, uniqueCount(jedis, mon));

            record(jedis, tue, "bala");
            record(jedis, tue, "chen");
            record(jedis, tue, "dara");
            check("Tuesday counts its own visitors", tue, 3L, uniqueCount(jedis, tue));

            check("returning visitors are the overlap", "mon ∩ tue", Set.of("bala", "chen"), returning(jedis, mon, tue));
            check("new visitors are Tuesday minus Monday", "tue - mon", Set.of("dara"), newOn(jedis, mon, tue));
            check("the subtraction is not symmetric", "mon - tue", Set.of("asha"), newOn(jedis, tue, mon));

            check("an unseen day counts zero", PREFIX + "visits:sun", 0L, uniqueCount(jedis, PREFIX + "visits:sun"));

            report("Redis Lab 3");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:3:";

    private static int passes = 0;
    private static int failures = 0;

    /** Host the lab Redis answers on; override with REDIS_HOST / REDIS_PORT. */
    static String host() {
        return System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
    }

    static int port() {
        return Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
    }

    /**
     * Opens a connection to the lab Redis and clears this lab's own keys, so a
     * rerun starts from a known state without flushing anybody else's keyspace.
     */
    private static Jedis connect() {
        Jedis jedis = new Jedis(host(), port());
        try {
            jedis.ping();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "No Redis at " + host() + ":" + port()
                    + ". Start one from redis/lab with: podman compose up -d", e);
        }
        for (String key : jedis.keys(PREFIX + "*")) {
            jedis.del(key);
        }
        return jedis;
    }

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
        System.out.println("      expected: condition holds");
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
            return java.util.Arrays.deepToString(new Object[] { value }).replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
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
    private static void report(String lab) {
        System.out.println("----");
        System.out.println(lab + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(lab + ": " + failures + " check(s) failed.");
        }
        System.out.println(lab + " passed.");
    }
}
