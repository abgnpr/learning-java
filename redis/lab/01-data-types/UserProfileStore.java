/*
 * Redis Lab 2 — user profile in a hash (Easy)
 *
 * Task: Keep a user profile in a single Redis hash instead of one key per
 * field. Saving merges the supplied fields without disturbing fields already
 * stored. Reading returns one field or the whole map. A login counter lives in
 * the same hash and is bumped server-side. The whole profile expires as one
 * unit, because a TTL belongs to the key, never to a field inside it.
 * Complete: save, field, load, recordLogin and expireIn
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 01-data-types/UserProfileStore.java
 */
import java.util.LinkedHashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;

public final class UserProfileStore {
    private UserProfileStore() {
    }

    /** Merges fields into the hash at key, leaving untouched fields in place. */
    static void save(Jedis jedis, String key, Map<String, String> fields) {
        // TODO: write every supplied field into the hash in one command.
        throw new UnsupportedOperationException("TODO: save a profile hash");
    }

    /** Returns one field, or null when the field or the hash is absent. */
    static String field(Jedis jedis, String key, String name) {
        // TODO: read a single field out of the hash.
        throw new UnsupportedOperationException("TODO: read one profile field");
    }

    /** Returns every field of the hash; an absent hash reads as an empty map. */
    static Map<String, String> load(Jedis jedis, String key) {
        // TODO: read the whole hash back as a map.
        throw new UnsupportedOperationException("TODO: load a whole profile");
    }

    /** Bumps the "logins" field server-side and returns its new value. */
    static long recordLogin(Jedis jedis, String key) {
        // TODO: increment a numeric field inside the hash without reading it first.
        throw new UnsupportedOperationException("TODO: count a login");
    }

    /** Puts a time-to-live on the profile as a whole. */
    static void expireIn(Jedis jedis, String key, int seconds) {
        // TODO: attach an expiry to the key that holds the hash.
        throw new UnsupportedOperationException("TODO: expire a profile");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            String key = PREFIX + "user:7";

            save(jedis, key, Map.of("name", "Asha", "city", "Pune"));
            check("a stored field reads back", "name", "Asha", field(jedis, key, "name"));
            check("an absent field reads as null", "phone", null, field(jedis, key, "phone"));

            var expected = new LinkedHashMap<String, String>();
            expected.put("name", "Asha");
            expected.put("city", "Pune");
            check("the whole profile reads back", key, expected, load(jedis, key));

            save(jedis, key, Map.of("city", "Mumbai"));
            check("a partial save overwrites only what it names", "city", "Mumbai", field(jedis, key, "city"));
            check("a partial save leaves other fields alone", "name", "Asha", field(jedis, key, "name"));

            check("first login counts as 1", key, 1L, recordLogin(jedis, key));
            check("the counter keeps climbing", key, 2L, recordLogin(jedis, key));
            check("the counter lives in the same hash", "logins", "2", field(jedis, key, "logins"));

            checkThat("a fresh profile has no expiry", key, jedis.ttl(key) == -1);
            expireIn(jedis, key, 120);
            checkThat("the expiry covers the whole key", key, jedis.ttl(key) > 0 && jedis.ttl(key) <= 120);

            check("an absent profile loads as an empty map", PREFIX + "user:nobody",
                    Map.of(), load(jedis, PREFIX + "user:nobody"));

            report("Redis Lab 2");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:2:";

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
