/*
 * Redis Lab 10 — order events on a stream (Medium)
 *
 * Task: Record order events on a stream, which keeps what it is given. Unlike
 * pub/sub, reading a stream does not consume it: the same range can be read
 * again, and a reader that arrives late still sees everything from the
 * beginning. Entries carry server-assigned, monotonically increasing ids, and
 * the log is bounded by trimming it rather than by expiring it.
 * Complete: append, range, length and trimTo
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 03-messaging/OrderEventStream.java
 */
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.resps.StreamEntry;

public final class OrderEventStream {
    private OrderEventStream() {
    }

    /** Appends one event, letting the server assign the id, and returns it. */
    static StreamEntryID append(Jedis jedis, String stream, Map<String, String> fields) {
        // TODO: add the entry with a server-generated id.
        throw new UnsupportedOperationException("TODO: append an event");
    }

    /** Reads up to count entries from the oldest end, oldest first. */
    static List<StreamEntry> range(Jedis jedis, String stream, int count) {
        // TODO: read a bounded range spanning the whole stream.
        throw new UnsupportedOperationException("TODO: read a range of events");
    }

    /** How many entries the stream currently holds. */
    static long length(Jedis jedis, String stream) {
        // TODO: ask the server for the stream's length.
        throw new UnsupportedOperationException("TODO: measure the stream");
    }

    /** Trims the stream to roughly maxLen entries, dropping the oldest. */
    static long trimTo(Jedis jedis, String stream, long maxLen) {
        // TODO: trim to a maximum length, asking for an exact rather than approximate cut.
        throw new UnsupportedOperationException("TODO: trim the stream");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            String stream = PREFIX + "orders";

            StreamEntryID first = append(jedis, stream, Map.of("order", "A-1", "state", "RECEIVED"));
            StreamEntryID second = append(jedis, stream, Map.of("order", "A-1", "state", "AUTHORISED"));
            StreamEntryID third = append(jedis, stream, Map.of("order", "A-2", "state", "RECEIVED"));

            checkThat("the server hands back an id", "first append", first != null);
            checkThat("ids increase along the stream", first + " < " + second, first.compareTo(second) < 0);
            checkThat("and keep increasing", second + " < " + third, second.compareTo(third) < 0);
            check("the stream holds what it was given", stream, 3L, length(jedis, stream));

            List<StreamEntry> events = range(jedis, stream, 10);
            check("reading returns every entry", stream, 3, events.size());
            check("entries come back oldest first", "first entry", "RECEIVED", events.get(0).getFields().get("state"));
            check("the second entry is the authorisation", "second entry", "AUTHORISED", events.get(1).getFields().get("state"));
            check("the entry keeps its id", "first entry", first, events.get(0).getID());

            // A stream is not a queue: reading it again yields the same entries.
            check("reading does not consume the stream", stream, 3, range(jedis, stream, 10).size());
            check("a second read is identical", "first entry again", "RECEIVED",
                    range(jedis, stream, 10).get(0).getFields().get("state"));

            check("a bounded read stops at the count", "count 2", 2, range(jedis, stream, 2).size());

            long removed = trimTo(jedis, stream, 2);
            check("trimming drops the overflow", "3 down to 2", 1L, removed);
            check("the stream is bounded afterwards", stream, 2L, length(jedis, stream));
            check("trimming drops the oldest first", "surviving head", "AUTHORISED",
                    range(jedis, stream, 10).get(0).getFields().get("state"));

            check("an unknown stream measures zero", PREFIX + "nothing", 0L, length(jedis, PREFIX + "nothing"));

            report("Redis Lab 10");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:10:";

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
