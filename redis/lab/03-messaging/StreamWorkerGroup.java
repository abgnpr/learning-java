/*
 * Redis Lab 11 — splitting a stream across workers (Hard)
 *
 * Task: Lab 10 let every reader see every entry. A consumer group instead
 * hands each entry to exactly one member, so several JVMs can share the work
 * without coordinating. A delivered entry is not finished: it sits in the
 * group's pending list until the worker acknowledges it, which is what lets a
 * crashed worker's messages be recovered rather than lost.
 * Complete: ensureGroup, claim, ack and pending
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 03-messaging/StreamWorkerGroup.java
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;

public final class StreamWorkerGroup {
    private StreamWorkerGroup() {
    }

    /**
     * Creates the group reading from the start of the stream, creating the
     * stream too if it does not exist. Calling it again must be harmless —
     * every worker runs this on boot.
     */
    static void ensureGroup(Jedis jedis, String stream, String group) {
        // TODO: create the group from id 0-0 with MKSTREAM, and swallow the error that says it already exists.
        throw new UnsupportedOperationException("TODO: create the consumer group");
    }

    /** Takes up to count entries nobody in the group has been given yet. */
    static List<StreamEntry> claim(Jedis jedis, String stream, String group, String consumer, int count) {
        // TODO: read as this consumer, asking only for entries never delivered to the group.
        throw new UnsupportedOperationException("TODO: claim a batch of work");
    }

    /** Marks the entries finished so they leave the pending list. */
    static long ack(Jedis jedis, String stream, String group, List<StreamEntry> entries) {
        // TODO: acknowledge every entry's id in one call.
        throw new UnsupportedOperationException("TODO: acknowledge finished work");
    }

    /** How many entries the group has delivered but not yet had acknowledged. */
    static long pending(Jedis jedis, String stream, String group) {
        // TODO: read the group's pending summary and return its total.
        throw new UnsupportedOperationException("TODO: count pending work");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            String stream = PREFIX + "payments";
            String group = "settlers";

            ensureGroup(jedis, stream, group);
            ensureGroup(jedis, stream, group);
            checkThat("creating the group twice is harmless", group, true);

            for (int i = 1; i <= 6; i++) {
                jedis.xadd(stream, XAddParams.xAddParams(), Map.of("payment", "P-" + i));
            }
            check("the stream holds the work", stream, 6L, jedis.xlen(stream));
            check("nothing is pending before anyone reads", group, 0L, pending(jedis, stream, group));

            List<StreamEntry> byWorkerA = claim(jedis, stream, group, "worker-a", 3);
            List<StreamEntry> byWorkerB = claim(jedis, stream, group, "worker-b", 3);

            check("worker A took three", "worker-a", 3, byWorkerA.size());
            check("worker B took three", "worker-b", 3, byWorkerB.size());

            var idsA = byWorkerA.stream().map(e -> e.getFields().get("payment")).toList();
            var idsB = byWorkerB.stream().map(e -> e.getFields().get("payment")).toList();
            var overlap = new ArrayList<>(idsA);
            overlap.retainAll(idsB);
            check("no payment went to both workers", idsA + " vs " + idsB, List.of(), overlap);

            var union = new java.util.TreeSet<String>();
            union.addAll(idsA);
            union.addAll(idsB);
            check("between them the workers saw everything", "union",
                    java.util.Set.of("P-1", "P-2", "P-3", "P-4", "P-5", "P-6"), union);

            check("delivered work is pending until acknowledged", group, 6L, pending(jedis, stream, group));

            check("acknowledging A's batch clears three", "worker-a", 3L, ack(jedis, stream, group, byWorkerA));
            check("three remain pending", group, 3L, pending(jedis, stream, group));

            check("acknowledging B's batch clears the rest", "worker-b", 3L, ack(jedis, stream, group, byWorkerB));
            check("nothing is left pending", group, 0L, pending(jedis, stream, group));

            check("a late worker finds no undelivered work", "worker-c", 0,
                    claim(jedis, stream, group, "worker-c", 3).size());

            // The entries are still on the stream; the group only tracks who got what.
            check("the stream itself is untouched by all this", stream, 6L, jedis.xlen(stream));

            report("Redis Lab 11");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:11:";

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
