/*
 * Redis Lab 1 — OTP cache on strings and TTL (Easy)
 *
 * Task: Back a one-time-passcode flow with a Redis string that expires by
 * itself, so nothing has to sweep old codes away. Issuing stores the code
 * under the key with a time-to-live in seconds. Verifying reports EXPIRED when
 * the key is gone, INVALID when the stored code differs — leaving the key
 * alone so the caller may retry — and OK when it matches, consuming the key so
 * the same code never verifies twice.
 * Complete: issue and verify
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 01-data-types/OtpCache.java
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public final class OtpCache {
    private OtpCache() {
    }

    /** Stores code under key so that Redis drops it after ttlSeconds. */
    static void issue(Jedis jedis, String key, String code, int ttlSeconds) {
        // TODO: write the code and its expiry in the same command, not as a write then an expire.
        throw new UnsupportedOperationException("TODO: issue an expiring passcode");
    }

    /** Returns "OK", "INVALID" or "EXPIRED"; consumes the key only on "OK". */
    static String verify(Jedis jedis, String key, String code) {
        // TODO: read the stored code, then decide between the three outcomes.
        throw new UnsupportedOperationException("TODO: verify a passcode");
    }

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = connect()) {
            String key = PREFIX + "otp:asha";

            issue(jedis, key, "482913", 60);
            check("the issued code is stored", key, "482913", jedis.get(key));
            checkThat("the issued code carries a TTL", key, jedis.ttl(key) > 0 && jedis.ttl(key) <= 60);

            check("a wrong code is rejected", "code 000000", "INVALID", verify(jedis, key, "000000"));
            checkThat("a rejected attempt leaves the key so the caller may retry", key, jedis.exists(key));

            check("the matching code is accepted", "code 482913", "OK", verify(jedis, key, "482913"));
            checkThat("acceptance consumes the key", key, !jedis.exists(key));
            check("the same code cannot be replayed", "code 482913 again", "EXPIRED", verify(jedis, key, "482913"));

            // A lapsed key is indistinguishable from one that never existed.
            String lapsing = PREFIX + "otp:bala";
            issue(jedis, lapsing, "111111", 60);
            jedis.pexpire(lapsing, 120);
            Thread.sleep(300);
            check("a lapsed code reads as expired", "key past its TTL", "EXPIRED", verify(jedis, lapsing, "111111"));

            check("a code never issued reads as expired too", PREFIX + "otp:nobody",
                    "EXPIRED", verify(jedis, PREFIX + "otp:nobody", "123456"));

            report("Redis Lab 1");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:1:";

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
