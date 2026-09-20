/*
 * Redis Lab 13 — sharding across a cluster (Hard)
 *
 * Task: A cluster splits 16384 hash slots across its masters, and a key's slot
 * is decided by hashing the key. That makes single-key work transparent and
 * multi-key work conditional: commands touching several keys only run when
 * every key lives in the same slot. A hash tag — the part of the key inside
 * braces — is the lever, because only that part is hashed, so related keys can
 * be pinned together on purpose.
 *
 * This lab talks to the cluster tier, not the everyday Redis on 6379.
 * Complete: openCluster, put, get, slotOf and msetAll
 * Needs: podman compose --profile cluster up -d
 * Run: java -cp "lib/*" 04-operations/ShardedCache.java
 */
import java.util.HashSet;
import java.util.Set;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.JedisClusterCRC16;

public final class ShardedCache {
    private ShardedCache() {
    }

    static final Set<HostAndPort> NODES = Set.of(
            new HostAndPort("127.0.0.1", 7001),
            new HostAndPort("127.0.0.1", 7002),
            new HostAndPort("127.0.0.1", 7003));

    /** A client that discovers the whole cluster from any seed node. */
    static JedisCluster openCluster(Set<HostAndPort> seeds) {
        // TODO: build a cluster client from the seed nodes.
        throw new UnsupportedOperationException("TODO: open a cluster client");
    }

    /** Writes a key, wherever its slot happens to live. */
    static void put(JedisCluster cluster, String key, String value) {
        // TODO: write the key; the client routes it to the owning node for you.
        throw new UnsupportedOperationException("TODO: write to the cluster");
    }

    /** Reads a key, wherever its slot happens to live. */
    static String get(JedisCluster cluster, String key) {
        // TODO: read the key.
        throw new UnsupportedOperationException("TODO: read from the cluster");
    }

    /** The slot number this key hashes to, in 0..16383. */
    static int slotOf(String key) {
        // TODO: compute the key's slot the same way the server does.
        throw new UnsupportedOperationException("TODO: compute a key's slot");
    }

    /** Writes two keys in one command, which the cluster allows only within a slot. */
    static void msetAll(JedisCluster cluster, String keyA, String valueA, String keyB, String valueB) {
        // TODO: set both keys in a single multi-key command.
        throw new UnsupportedOperationException("TODO: write two keys at once");
    }

    public static void main(String[] args) {
        requireCluster();

        try (JedisCluster cluster = openCluster(NODES)) {
            // Single-key work does not care where the slot lives.
            var slots = new HashSet<Integer>();
            for (int i = 0; i < 40; i++) {
                String key = PREFIX + "item:" + i;
                put(cluster, key, "value-" + i);
                slots.add(slotOf(key));
            }
            check("a key written anywhere reads back", PREFIX + "item:7", "value-7", get(cluster, PREFIX + "item:7"));
            check("and so does another", PREFIX + "item:39", "value-39", get(cluster, PREFIX + "item:39"));
            checkThat("40 keys scatter across many slots", slots.size() + " distinct slots", slots.size() > 20);

            // The hash tag is the whole lesson.
            String taggedProfile = PREFIX + "{user:1}:profile";
            String taggedSessions = PREFIX + "{user:1}:sessions";
            check("keys sharing a hash tag share a slot", "{user:1}",
                    slotOf(taggedProfile), slotOf(taggedSessions));

            String plainProfile = PREFIX + "user:1:profile";
            String plainSessions = PREFIX + "user:1:sessions";
            checkThat("without a tag, related keys scatter", "user:1:*",
                    slotOf(plainProfile) != slotOf(plainSessions));

            checkThat("only the braced part decides the slot", "{user:1} vs a longer tail",
                    slotOf(PREFIX + "{user:1}:anything-at-all") == slotOf(taggedProfile));

            // Multi-key commands follow from that.
            msetAll(cluster, taggedProfile, "Asha", taggedSessions, "2");
            check("a multi-key write within one slot succeeds", taggedProfile, "Asha", get(cluster, taggedProfile));
            check("both halves of it landed", taggedSessions, "2", get(cluster, taggedSessions));

            boolean rejected = false;
            String reason = "";
            try {
                msetAll(cluster, plainProfile, "Asha", plainSessions, "2");
            } catch (RuntimeException e) {
                rejected = true;
                reason = e.getClass().getSimpleName();
            }
            checkThat("the same write across two slots is refused", reason, rejected);
            check("and neither key was written", plainProfile, null, get(cluster, plainProfile));

            report("Redis Lab 13");
        }
    }

    /** Fails the run with the command that starts the tier this lab needs. */
    private static void requireCluster() {
        try (Jedis probe = new Jedis("127.0.0.1", 7001)) {
            if (!probe.clusterInfo().contains("cluster_state:ok")) {
                throw new IllegalStateException("cluster not formed");
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "No healthy cluster on 7001-7006. Start it from redis/lab with: "
                    + "podman compose --profile cluster up -d", e);
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:13:";

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
