/*
 * Redis Lab 12 — surviving a failover with Sentinel (Hard)
 *
 * Task: A client that hard-codes a master's address loses the database the
 * moment that node dies. Sentinel exists so the client asks "who is the master
 * right now?" instead. Connect through a sentinel-aware pool, force a
 * failover, and watch the same pool object start serving a different node
 * without being reconfigured or restarted.
 *
 * This lab talks to the sentinel tier, not the everyday Redis on 6379.
 * Complete: openPool, currentMasterPort, put and get
 * Needs: podman compose --profile sentinel up -d
 * Run: java -cp "lib/*" 04-operations/FailoverExperiment.java
 */
import java.util.List;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

public final class FailoverExperiment {
    private FailoverExperiment() {
    }

    static final String MASTER = "mymaster";
    static final Set<String> SENTINELS = Set.of("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381");

    /** A pool that resolves the master through the sentinels and follows it. */
    static JedisSentinelPool openPool(String masterName, Set<String> sentinels) {
        // TODO: build a sentinel-backed pool for this master name.
        throw new UnsupportedOperationException("TODO: open a sentinel pool");
    }

    /** Asks a sentinel which port currently holds the master role. */
    static String currentMasterPort(Set<String> sentinels, String masterName) {
        // TODO: ask any sentinel for the master's address and return its port.
        throw new UnsupportedOperationException("TODO: ask a sentinel for the master");
    }

    /** Writes through the pool, borrowing and returning a connection. */
    static void put(JedisSentinelPool pool, String key, String value) {
        // TODO: borrow a connection from the pool, write, and return it.
        throw new UnsupportedOperationException("TODO: write through the pool");
    }

    /** Reads through the pool. */
    static String get(JedisSentinelPool pool, String key) {
        // TODO: borrow a connection from the pool and read.
        throw new UnsupportedOperationException("TODO: read through the pool");
    }

    public static void main(String[] args) throws Exception {
        requireSentinels();

        String before = currentMasterPort(SENTINELS, MASTER);
        checkThat("a sentinel names the current master", "master port " + before, before != null);

        try (JedisSentinelPool pool = openPool(MASTER, SENTINELS)) {
            String survivor = PREFIX + "written:before";
            put(pool, survivor, "before-failover");
            check("a write through the pool reads back", survivor, "before-failover", get(pool, survivor));

            // Give the replica a moment to receive the write before promoting it.
            Thread.sleep(500);
            forceFailover();
            String after = awaitMasterChange(before);

            checkThat("the master role moved to another node", before + " -> " + after, !before.equals(after));
            check("the pool followed without being reconfigured", survivor, "before-failover",
                    retry(() -> get(pool, survivor)));

            String fresh = PREFIX + "written:after";
            retry(() -> {
                put(pool, fresh, "after-failover");
                return null;
            });
            check("the pool writes to the promoted master", fresh, "after-failover", retry(() -> get(pool, fresh)));
            check("the earlier write is still there too", survivor, "before-failover", retry(() -> get(pool, survivor)));

            String nowServing = currentMasterPort(SENTINELS, MASTER);
            check("the sentinels agree on who is master now", "master port", after, nowServing);
        }

        report("Redis Lab 12");
    }

    /** Fails the run with the command that starts the tier this lab needs. */
    private static void requireSentinels() {
        if (currentMasterPort(SENTINELS, MASTER) == null) {
            throw new IllegalStateException(
                    "No sentinel quorum on 26379-26381. Start it from redis/lab with: "
                    + "podman compose --profile sentinel up -d");
        }
    }

    /** Asks a sentinel to promote the replica, the way a real outage would. */
    private static void forceFailover() {
        String[] parts = SENTINELS.iterator().next().split(":");
        try (Jedis sentinel = new Jedis(parts[0], Integer.parseInt(parts[1]))) {
            sentinel.sentinelFailover(MASTER);
        }
    }

    /** Waits for the master port to differ from the one held before. */
    private static String awaitMasterChange(String before) throws InterruptedException {
        for (int attempt = 0; attempt < 120; attempt++) {
            String now = currentMasterPort(SENTINELS, MASTER);
            if (now != null && !now.equals(before)) {
                return now;
            }
            Thread.sleep(250);
        }
        throw new IllegalStateException("no failover within 30s; master still " + before);
    }

    /** Retries briefly while the pool rediscovers the promoted master. */
    private static <T> T retry(java.util.concurrent.Callable<T> work) throws Exception {
        RuntimeException last = null;
        for (int attempt = 0; attempt < 60; attempt++) {
            try {
                return work.call();
            } catch (RuntimeException e) {
                last = e;
                Thread.sleep(250);
            }
        }
        throw last;
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:12:";

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
