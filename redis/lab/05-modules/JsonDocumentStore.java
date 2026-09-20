/*
 * Redis Lab 14 — documents with RedisJSON (Medium)
 *
 * Task: A hash is flat, so storing a nested object in one means flattening it
 * and rewriting the whole thing to change one field. The JSON type stores the
 * document as a tree and addresses it by path, so a nested value can be read
 * or replaced on the server without the document ever crossing the network.
 * Complete: save, valueAt, patch and appendTo
 * Needs: Redis Stack on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 05-modules/JsonDocumentStore.java
 */
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;

public final class JsonDocumentStore {
    private JsonDocumentStore() {
    }

    /** Stores the document at the root of the key. */
    static void save(JedisPooled client, String key, Map<String, Object> document) {
        // TODO: write the map at the document's root path, escaping it as JSON.
        throw new UnsupportedOperationException("TODO: save a document");
    }

    /** First match at the path rendered as a string, or null when nothing matches. */
    static String valueAt(JedisPooled client, String key, String path) {
        // TODO: read the path and take the first element of the array of matches.
        throw new UnsupportedOperationException("TODO: read one path");
    }

    /** Replaces the value at one path, leaving the rest of the document alone. */
    static void patch(JedisPooled client, String key, String path, Object value) {
        // TODO: write the value at that path only.
        throw new UnsupportedOperationException("TODO: patch one path");
    }

    /** Appends to the array at the path and returns the array's new length. */
    static long appendTo(JedisPooled client, String key, String path, Object value) {
        // TODO: append to the array at that path and take the new length from the reply.
        throw new UnsupportedOperationException("TODO: append to an array");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect(); JedisPooled client = new JedisPooled(host(), port())) {
            String key = PREFIX + "customer:1";

            save(jedis, client, key);

            check("a top-level field reads by path", "$.name", "Asha", valueAt(client, key, "$.name"));
            check("a nested field reads by path", "$.address.city", "Pune", valueAt(client, key, "$.address.city"));
            check("a number comes back too", "$.limit", "50000", valueAt(client, key, "$.limit"));
            check("an array element reads by index", "$.tags[0]", "retail", valueAt(client, key, "$.tags[0]"));
            check("a path that matches nothing reads as null", "$.nonsense", null, valueAt(client, key, "$.nonsense"));

            // The point of the type: change one leaf, leave the tree standing.
            patch(client, key, "$.address.city", "Mumbai");
            check("the patched field changed", "$.address.city", "Mumbai", valueAt(client, key, "$.address.city"));
            check("its sibling was untouched", "$.address.pin", "411001", valueAt(client, key, "$.address.pin"));
            check("an unrelated branch was untouched", "$.name", "Asha", valueAt(client, key, "$.name"));

            patch(client, key, "$.limit", 75000);
            check("a number patches as a number", "$.limit", "75000", valueAt(client, key, "$.limit"));

            check("appending returns the new length", "$.tags", 3L, appendTo(client, key, "$.tags", "vip"));
            check("the appended element is there", "$.tags[2]", "vip", valueAt(client, key, "$.tags[2]"));
            check("appending again keeps counting", "$.tags", 4L, appendTo(client, key, "$.tags", "priority"));

            checkThat("the whole document still parses", key, client.jsonGet(key) != null);

            report("Redis Lab 14");
        }
    }

    /** Writes the fixture document this lab reads from. */
    private static void save(Jedis jedis, JedisPooled client, String key) {
        jedis.del(key);
        save(client, key, Map.of(
                "name", "Asha",
                "limit", 50000,
                "address", Map.of("city", "Pune", "pin", "411001"),
                "tags", List.of("retail", "kyc-done")));
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:14:";

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
