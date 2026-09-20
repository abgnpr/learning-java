/*
 * Redis Lab 9 — notifications over pub/sub (Medium)
 *
 * Task: Fan a message out to whoever is listening right now. Pub/sub keeps no
 * history: a publish is delivered to the subscribers connected at that instant
 * and is then gone, so a subscriber that connects a moment later has no way to
 * learn what it missed. PUBLISH answers with the number of subscribers it
 * reached, which is the only delivery report the protocol offers.
 * Complete: collector and broadcast
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 03-messaging/NotificationBus.java
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public final class NotificationBus {
    private NotificationBus() {
    }

    /**
     * A subscriber that appends each message to sink and stops listening once
     * it has taken `expected` of them, so the subscribing thread can finish.
     */
    static JedisPubSub collector(List<String> sink, int expected) {
        // TODO: return a JedisPubSub whose onMessage records the payload and unsubscribes at the expected count.
        throw new UnsupportedOperationException("TODO: build a collecting subscriber");
    }

    /** Publishes every message and returns how many deliveries were made in total. */
    static long broadcast(Jedis jedis, String channel, List<String> messages) {
        // TODO: publish each message and add up the subscriber counts the server reports.
        throw new UnsupportedOperationException("TODO: broadcast to a channel");
    }

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = connect()) {
            String channel = PREFIX + "alerts";

            check("a publish with nobody listening reaches nobody", channel,
                    0L, broadcast(jedis, channel, List.of("this one is lost")));

            List<String> received = Collections.synchronizedList(new ArrayList<>());
            JedisPubSub subscriber = collector(received, 3);
            Jedis subscriberConnection = new Jedis(host(), port());
            Thread listening = new Thread(() -> subscriberConnection.subscribe(subscriber, channel));
            listening.setDaemon(true);
            listening.start();
            awaitSubscribers(jedis, channel, 1);

            long delivered = broadcast(jedis, channel, List.of("first", "second", "third"));
            listening.join(5000);

            check("every message reached the one subscriber", "3 publishes, 1 subscriber", 3L, delivered);
            check("the subscriber took all three", channel, List.of("first", "second", "third"), received);
            checkThat("what was published before it connected was never delivered",
                    "this one is lost", !received.contains("this one is lost"));
            checkThat("the subscriber stopped once it had its three", channel, !listening.isAlive());

            subscriberConnection.close();
            awaitSubscribers(jedis, channel, 0);
            check("with the listener gone, publishing reaches nobody again", channel,
                    0L, broadcast(jedis, channel, List.of("also lost")));

            report("Redis Lab 9");
        }
    }

    /** Waits until the server reports the expected subscriber count on the channel. */
    private static void awaitSubscribers(Jedis jedis, String channel, long expected) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (jedis.pubsubNumSub(channel).getOrDefault(channel, 0L) == expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException("channel never reached " + expected + " subscriber(s)");
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:9:";

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
