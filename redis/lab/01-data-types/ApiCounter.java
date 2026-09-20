/*
 * Redis Lab 5 — fixed-window API counter (Medium)
 *
 * Task: Count calls per API key inside a fixed time window. The counter is
 * incremented server-side, and the window's expiry is attached exactly once —
 * on the increment that created the key. Re-attaching it on every hit is the
 * classic bug: the window slides forward with traffic and never closes, so a
 * busy caller is never throttled.
 * Complete: hit and remaining
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 01-data-types/ApiCounter.java
 */
import redis.clients.jedis.Jedis;

public final class ApiCounter {
    private ApiCounter() {
    }

    /**
     * Counts one call and returns the running total for the current window,
     * attaching the window's expiry only when this call opened the window.
     */
    static long hit(Jedis jedis, String key, int windowSeconds) {
        // TODO: increment, then set the expiry only when the reply says the counter is 1.
        throw new UnsupportedOperationException("TODO: count one call");
    }

    /** Calls still allowed in this window; 0 once the limit is reached. */
    static long remaining(Jedis jedis, String key, int limit) {
        // TODO: read the counter, treat an absent key as zero, and never go below 0.
        throw new UnsupportedOperationException("TODO: report the remaining budget");
    }

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = connect()) {
            String key = PREFIX + "quota:key-abc";

            check("an untouched window has the full budget", "limit 5", 5L, remaining(jedis, key, 5));

            check("the first call counts 1", key, 1L, hit(jedis, key, 60));
            check("the second counts 2", key, 2L, hit(jedis, key, 60));
            check("the budget falls as calls land", "limit 5", 3L, remaining(jedis, key, 5));

            long ttlAfterFirstHits = jedis.ttl(key);
            checkThat("the window carries an expiry", key, ttlAfterFirstHits > 0 && ttlAfterFirstHits <= 60);

            // The trap: a later hit must not push the window's end further out.
            jedis.expire(key, 30);
            hit(jedis, key, 60);
            long ttlAfterLaterHit = jedis.ttl(key);
            checkThat("a later hit does not extend the window", "ttl was 30", ttlAfterLaterHit <= 30);

            hit(jedis, key, 60);
            hit(jedis, key, 60);
            check("the budget bottoms out at zero", "limit 5", 0L, remaining(jedis, key, 5));
            check("calls past the limit still count", key, 6L, hit(jedis, key, 60));
            check("the budget never goes negative", "limit 5", 0L, remaining(jedis, key, 5));

            // When the window lapses the key vanishes, and counting restarts at 1.
            String rolling = PREFIX + "quota:key-xyz";
            hit(jedis, rolling, 60);
            jedis.pexpire(rolling, 120);
            Thread.sleep(300);
            check("a lapsed window restarts at 1", rolling, 1L, hit(jedis, rolling, 60));

            report("Redis Lab 5");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:5:";

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
