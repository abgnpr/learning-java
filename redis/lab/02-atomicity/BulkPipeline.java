/*
 * Redis Lab 7 — bulk reads and writes through a pipeline (Medium)
 *
 * Task: Send many commands without paying a network round trip for each one.
 * A pipeline queues commands locally, flushes them in one write, and hands
 * back a Response per command that only holds a value once the batch has been
 * synchronised. Pipelining is not a transaction: the commands are not atomic
 * and other clients interleave freely — it buys latency, nothing else.
 * Complete: writeAll and readAll
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 02-atomicity/BulkPipeline.java
 */
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public final class BulkPipeline {
    private BulkPipeline() {
    }

    /** Writes every entry in one batch rather than one round trip per key. */
    static void writeAll(Jedis jedis, Map<String, String> entries) {
        // TODO: queue a write per entry on a pipeline, then flush the batch once.
        throw new UnsupportedOperationException("TODO: write a batch");
    }

    /** Reads the keys in one batch, answering in the order they were asked for. */
    static List<String> readAll(Jedis jedis, List<String> keys) {
        // TODO: queue a read per key, keep the Response handles in order, flush, then collect.
        throw new UnsupportedOperationException("TODO: read a batch");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            var entries = new LinkedHashMap<String, String>();
            var keys = new ArrayList<String>();
            for (int i = 0; i < 500; i++) {
                String key = PREFIX + "row:" + i;
                entries.put(key, "value-" + i);
                keys.add(key);
            }

            writeAll(jedis, entries);
            check("every key in the batch landed", "500 keys", 500L,
                    jedis.keys(PREFIX + "row:*").size() * 1L);
            check("a spot-checked value is right", PREFIX + "row:42", "value-42", jedis.get(PREFIX + "row:42"));

            List<String> read = readAll(jedis, keys);
            check("the batch reads back the same count", "500 keys", 500, read.size());
            check("answers keep the order they were asked in", "first three",
                    List.of("value-0", "value-1", "value-2"), read.subList(0, 3));
            check("the last answer lines up too", "row:499", "value-499", read.get(499));

            // Reading in a different order proves the answers track the request,
            // not the keyspace.
            List<String> reversed = readAll(jedis, List.of(PREFIX + "row:2", PREFIX + "row:1", PREFIX + "row:0"));
            check("a reordered request comes back reordered", "rows 2,1,0",
                    List.of("value-2", "value-1", "value-0"), reversed);

            List<String> withGap = readAll(jedis, List.of(PREFIX + "row:0", PREFIX + "absent", PREFIX + "row:1"));
            check("a missing key holds its slot as null", "row:0, absent, row:1",
                    java.util.Arrays.asList("value-0", null, "value-1"), withGap);

            // Not a check — the point of the exercise, shown rather than asserted.
            long pipelined = time(() -> readAll(jedis, keys));
            long oneByOne = time(() -> {
                for (String key : keys) {
                    jedis.get(key);
                }
            });
            System.out.println();
            System.out.println("      500 reads pipelined:   " + pipelined + " ms");
            System.out.println("      500 reads one by one:  " + oneByOne + " ms");
            System.out.println();

            report("Redis Lab 7");
        }
    }

    private static long time(Runnable work) {
        long start = System.nanoTime();
        work.run();
        return (System.nanoTime() - start) / 1_000_000;
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:7:";

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
