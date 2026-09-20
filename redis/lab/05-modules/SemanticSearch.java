/*
 * Redis Lab 15 — nearest-neighbour search over vectors (Hard)
 *
 * Task: Index documents by meaning rather than by the words they contain. Each
 * document carries an embedding — a fixed-length array of floats — stored as a
 * FLOAT32 blob in a hash. A vector index lets the server rank documents by
 * distance from a query vector, so a query shares no words with the document
 * it finds.
 *
 * The embedding here is a toy: three dimensions counting topic keywords, given
 * by the harness. Only the Redis half is yours.
 * Complete: createIndex, addDoc and nearest
 * Needs: Redis Stack on 127.0.0.1:6379 — see ../README.md
 * Run: java -cp "lib/*" 05-modules/SemanticSearch.java
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.RediSearchUtil;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;

public final class SemanticSearch {
    private SemanticSearch() {
    }

    /**
     * Declares an index over hashes under keyPrefix, holding a text field and
     * a flat FLOAT32 vector field of the given width, compared by cosine.
     */
    static void createIndex(JedisPooled client, String index, String keyPrefix, int dimensions) {
        // TODO: create an index over hashes with that prefix, carrying a TEXT field and a FLAT FLOAT32 VECTOR field compared by COSINE.
        throw new UnsupportedOperationException("TODO: declare the vector index");
    }

    /** Stores one document: its text, and its embedding as a FLOAT32 blob. */
    static void addDoc(JedisPooled client, String key, String text, float[] embedding) {
        // TODO: write both fields into the hash, the vector as raw bytes rather than a string.
        throw new UnsupportedOperationException("TODO: index one document");
    }

    /** The keys of the k documents closest to the query vector, nearest first. */
    static List<String> nearest(JedisPooled client, String index, float[] query, int k) {
        // TODO: run a KNN query with the vector as a parameter, sorted by the distance it scores.
        throw new UnsupportedOperationException("TODO: search by nearest neighbour");
    }

    public static void main(String[] args) throws Exception {
        try (Jedis jedis = connect(); JedisPooled client = new JedisPooled(host(), port())) {
            String index = PREFIX + "idx";
            String keyPrefix = PREFIX + "doc:";
            dropIndex(client, index);

            createIndex(client, index, keyPrefix, 3);
            checkThat("the index exists", index, indexExists(client, index));

            String payments = keyPrefix + "1";
            String streaming = keyPrefix + "2";
            String garden = keyPrefix + "3";
            addDoc(client, payments, "payment settled through the switch ledger", embed("payment settled through the switch ledger"));
            addDoc(client, streaming, "kafka consumer group rebalanced across partitions", embed("kafka consumer group rebalanced across partitions"));
            addDoc(client, garden, "roses bloom in the spring garden soil", embed("roses bloom in the spring garden soil"));
            awaitIndexed(client, index, 3);

            check("the document text was stored", payments, "payment settled through the switch ledger",
                    jedis.hget(payments, "text"));

            // None of these queries share their decisive words with the answer.
            check("a debit question finds the payments note", "a debit was never credited back",
                    payments, nearest(client, index, embed("a debit was never credited back"), 1).get(0));
            check("an offset question finds the streaming note", "the offset never committed",
                    streaming, nearest(client, index, embed("the offset never committed"), 1).get(0));
            check("a soil question finds the garden note", "the soil needs turning before spring",
                    garden, nearest(client, index, embed("the soil needs turning before spring"), 1).get(0));

            List<String> topTwo = nearest(client, index, embed("a debit was never credited back"), 2);
            check("asking for two neighbours returns two", "k=2", 2, topTwo.size());
            check("the nearest is still first", "k=2", payments, topTwo.get(0));
            checkThat("the second is a different document", topTwo.toString(), !topTwo.get(0).equals(topTwo.get(1)));

            check("asking for more than exists returns what exists", "k=10", 3,
                    nearest(client, index, embed("payment"), 10).size());

            report("Redis Lab 15");
        }
    }

    // ---- the toy embedding: given, so the exercise stays about Redis ----

    /** Keyword buckets standing in for a real embedding model. */
    private static final List<List<String>> TOPICS = List.of(
            List.of("payment", "payments", "settle", "settled", "switch", "debit", "credit", "credited", "ledger"),
            List.of("kafka", "consumer", "consumers", "partition", "partitions", "offset", "topic", "stream", "committed"),
            List.of("rose", "roses", "bloom", "garden", "spring", "soil", "turning"));

    /** Counts topic keywords, then scales to unit length so cosine behaves. */
    static float[] embed(String text) {
        float[] vector = new float[TOPICS.size()];
        for (String word : text.toLowerCase().split("\\W+")) {
            for (int topic = 0; topic < TOPICS.size(); topic++) {
                if (TOPICS.get(topic).contains(word)) {
                    vector[topic]++;
                }
            }
        }
        double length = 0;
        for (float component : vector) {
            length += component * component;
        }
        if (length == 0) {
            java.util.Arrays.fill(vector, 1f);
            length = vector.length;
        }
        float norm = (float) Math.sqrt(length);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
        return vector;
    }

    /** Drops the index if a previous run left one, keeping the lab rerunnable. */
    private static void dropIndex(JedisPooled client, String index) {
        try {
            client.ftDropIndex(index);
        } catch (RuntimeException ignored) {
            // No index yet; nothing to drop.
        }
    }

    private static boolean indexExists(JedisPooled client, String index) {
        try {
            client.ftInfo(index);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Indexing is asynchronous; waits for the documents to become searchable. */
    private static void awaitIndexed(JedisPooled client, String index, int expected) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            Object docs = client.ftInfo(index).get("num_docs");
            if (docs != null && Long.parseLong(String.valueOf(docs)) >= expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException("index never reached " + expected + " documents");
    }

    // ---- lab harness (identical in every Redis lab; not part of the exercise) ----

    /** Every key this lab writes starts here, so cleanup never touches other data. */
    static final String PREFIX = "lab:15:";

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
