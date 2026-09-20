/*
 * Redis Lab 4 — leaderboard on a sorted set (Medium)
 *
 * Task: Rank players by score using a sorted set, which keeps its members
 * ordered by score at write time so reading the top N never sorts anything.
 * Ranks are reported to humans from 1, while Redis counts from 0, and a
 * player's score is adjusted server-side so two concurrent awards cannot
 * overwrite one another.
 * Complete: submit, top, rankOf and award
 * Needs: Redis on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 01-data-types/Leaderboard.java
 */
import java.util.List;
import redis.clients.jedis.Jedis;

public final class Leaderboard {
    private Leaderboard() {
    }

    /** Sets a player's score outright. */
    static void submit(Jedis jedis, String board, String player, double score) {
        // TODO: add the member with its score to the sorted set.
        throw new UnsupportedOperationException("TODO: submit a score");
    }

    /** The n highest scorers, best first. */
    static List<String> top(Jedis jedis, String board, int n) {
        // TODO: read a range from the high-score end, remembering ranges are inclusive.
        throw new UnsupportedOperationException("TODO: read the top N");
    }

    /** The player's position counting from 1, or -1 when they are not ranked. */
    static long rankOf(Jedis jedis, String board, String player) {
        // TODO: ask for the rank from the high-score end and shift it to 1-based.
        throw new UnsupportedOperationException("TODO: rank a player");
    }

    /** Adds delta to the player's score server-side and returns the new score. */
    static double award(Jedis jedis, String board, String player, double delta) {
        // TODO: increment the member's score in one command, without reading it first.
        throw new UnsupportedOperationException("TODO: award points");
    }

    public static void main(String[] args) {
        try (Jedis jedis = connect()) {
            String board = PREFIX + "board:weekly";

            submit(jedis, board, "asha", 120);
            submit(jedis, board, "bala", 340);
            submit(jedis, board, "chen", 90);
            submit(jedis, board, "dara", 275);

            check("the top three come back best first", board, List.of("bala", "dara", "asha"), top(jedis, board, 3));
            check("asking for more than exists returns what exists", board,
                    List.of("bala", "dara", "asha", "chen"), top(jedis, board, 10));

            check("the leader ranks 1, not 0", "bala", 1L, rankOf(jedis, board, "bala"));
            check("the last player ranks 4", "chen", 4L, rankOf(jedis, board, "chen"));
            check("an unranked player reports -1", "elif", -1L, rankOf(jedis, board, "elif"));

            check("an award returns the new score", "chen +500", 590.0, award(jedis, board, "chen", 500));
            check("the award reorders the board", board, List.of("chen", "bala", "dara"), top(jedis, board, 3));
            check("the promoted player now leads", "chen", 1L, rankOf(jedis, board, "chen"));

            check("awarding an unknown player enrols them", "elif +10", 10.0, award(jedis, board, "elif", 10));
            check("a negative award subtracts", "elif -4", 6.0, award(jedis, board, "elif", -4));

            report("Redis Lab 4");
        }
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:4:";

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
