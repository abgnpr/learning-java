/*
 * Redis Lab 6 — transfer under WATCH/MULTI/EXEC (Hard)
 *
 * Task: Move points between two accounts so the pair can never be seen
 * half-moved, and so a balance read before the write cannot be acted on after
 * someone else has changed it. WATCH the source, read it, decide, then queue
 * both writes in a transaction: if the watched key changed in the meantime the
 * server discards the queued commands and EXEC reports nothing ran.
 *
 * betweenReadAndWrite is a test seam. The harness passes a lambda that lets
 * another connection interfere at exactly the moment that matters; production
 * code would pass a no-op.
 * Complete: transfer
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 02-atomicity/AtomicWorkflow.java
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public final class AtomicWorkflow {
    private AtomicWorkflow() {
    }

    /**
     * Returns true when the transfer committed; false when the source lacked
     * the funds, or when a concurrent write to the source aborted the attempt.
     */
    static boolean transfer(Jedis jedis, String fromKey, String toKey, long amount,
                            Runnable betweenReadAndWrite) {
        // TODO: watch the source, read it, let the seam run, then commit both writes in one transaction and report whether EXEC ran.
        throw new UnsupportedOperationException("TODO: transfer points atomically");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect(); Jedis other = new Jedis(host(), port())) {
            String from = PREFIX + "acct:asha";
            String to = PREFIX + "acct:bala";
            Runnable noop = () -> { };

            jedis.set(from, "100");
            jedis.set(to, "20");

            checkThat("a funded transfer commits", "60 of 100", transfer(jedis, from, to, 60, noop));
            check("the source is debited", from, "40", jedis.get(from));
            check("the destination is credited", to, "80", jedis.get(to));

            checkThat("an unaffordable transfer is refused", "90 of 40", !transfer(jedis, from, to, 90, noop));
            check("a refusal leaves the source untouched", from, "40", jedis.get(from));
            check("a refusal leaves the destination untouched", to, "80", jedis.get(to));

            // Another client drains the source after this transfer read it.
            // The balance the decision rested on is stale, so EXEC must not run.
            Runnable interfere = () -> other.set(from, "5");
            checkThat("a concurrent write aborts the transaction", "source changed mid-flight",
                    !transfer(jedis, from, to, 40, interfere));
            check("the interfering write stands", from, "5", jedis.get(from));
            check("no points were conjured for the destination", to, "80", jedis.get(to));

            // The same attempt succeeds once nothing else is racing it.
            jedis.set(from, "40");
            checkThat("the retry commits when nothing interferes", "40 of 40", transfer(jedis, from, to, 40, noop));
            check("the source is drained exactly once", from, "0", jedis.get(from));
            check("the destination gained exactly the amount", to, "120", jedis.get(to));

            report("Redis Lab 6");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:6:";

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
