/*
 * Redis Lab 8 — rate limiter in a Lua script (Hard)
 *
 * Task: Lab 5 counted calls with INCR then EXPIRE — two round trips, and the
 * decision to allow or reject was made in the JVM from a value that could
 * already be stale. Move the whole decision into a script. Redis runs a script
 * to completion without interleaving another client's commands, so read,
 * increment, expire and verdict happen as one indivisible step.
 *
 * Contract: allow returns the budget left after this call, or -1 when the call
 * is over the limit. The window's expiry is attached only when the script
 * itself created the counter.
 * Complete: script and allow
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 02-atomicity/LuaRateLimiter.java
 */
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class LuaRateLimiter {
    private LuaRateLimiter() {
    }

    /**
     * The script body. KEYS[1] is the counter, ARGV[1] the limit and ARGV[2]
     * the window in seconds. Returns the remaining budget, or -1 if over.
     */
    static String script() {
        // TODO: return a Lua body that increments, expires on first use, and returns the budget or -1.
        throw new UnsupportedOperationException("TODO: supply the limiter script");
    }

    /** Runs the script for one call. */
    static long allow(Jedis jedis, String key, int limit, int windowSeconds) {
        // TODO: evaluate the script with one key and two arguments, and return its number.
        throw new UnsupportedOperationException("TODO: evaluate the limiter");
    }

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = connect()) {
            String key = PREFIX + "rate:key-abc";

            check("the first call leaves 2 of 3", "limit 3", 2L, allow(jedis, key, 3, 60));
            check("the second leaves 1", "limit 3", 1L, allow(jedis, key, 3, 60));
            check("the third exhausts the budget", "limit 3", 0L, allow(jedis, key, 3, 60));
            check("the fourth is rejected", "limit 3", -1L, allow(jedis, key, 3, 60));
            check("rejection is not a one-off", "limit 3", -1L, allow(jedis, key, 3, 60));

            checkThat("the window carries an expiry", key, jedis.ttl(key) > 0 && jedis.ttl(key) <= 60);
            jedis.expire(key, 30);
            allow(jedis, key, 3, 60);
            checkThat("a later call does not extend the window", "ttl was 30", jedis.ttl(key) <= 30);

            String lapsing = PREFIX + "rate:key-xyz";
            allow(jedis, lapsing, 3, 60);
            jedis.pexpire(lapsing, 120);
            Thread.sleep(300);
            check("a lapsed window restores the full budget", lapsing, 2L, allow(jedis, lapsing, 3, 60));

            // The reason the script exists: 60 threads, a limit of 20, and the
            // count has to come out at exactly 20 admissions. INCR-then-decide
            // in Java loses this test.
            String contended = PREFIX + "rate:contended";
            var admitted = new AtomicInteger();
            try (JedisPool pool = new JedisPool(host(), port())) {
                var threads = new java.util.ArrayList<Thread>();
                for (int i = 0; i < 60; i++) {
                    Thread t = new Thread(() -> {
                        try (Jedis conn = pool.getResource()) {
                            if (allow(conn, contended, 20, 60) >= 0) {
                                admitted.incrementAndGet();
                            }
                        }
                    });
                    threads.add(t);
                    t.start();
                }
                for (Thread t : threads) {
                    t.join();
                }
            }
            check("60 racing callers admit exactly the limit", "60 threads, limit 20", 20, admitted.get());
            check("the counter saw every attempt", contended, "60", jedis.get(contended));

            report("Redis Lab 8");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:8:";

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
