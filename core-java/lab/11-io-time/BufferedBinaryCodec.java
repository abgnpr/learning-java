/*
 * Challenge 89 — Buffered Binary Codec (Hard)
 *
 * Task: Write and read a small binary long-list format using buffered data
 * streams: 32-bit magic 0x4C414231, 32-bit element count, then signed 64-bit
 * values. Reject a wrong magic or negative count with IOException.
 * Complete: writeLongs(Path, List<Long>), readLongs(Path)
 * Run: java BufferedBinaryCodec.java
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BufferedBinaryCodec {
    private static final int MAGIC = 0x4C414231;

    static void writeLongs(Path path, List<Long> values) throws IOException {
        // TODO: Own and close a buffered DataOutputStream; write the exact format.
        throw new UnsupportedOperationException("TODO: write binary values");
    }

    static List<Long> readLongs(Path path) throws IOException {
        // TODO: Validate the header and read exactly count long values.
        throw new UnsupportedOperationException("TODO: read binary values");
    }

    public static void main(String[] args) throws Exception {
        Path valid = Files.createTempFile("java-lab-binary-", ".bin");
        Path invalid = Files.createTempFile("java-lab-binary-bad-", ".bin");
        try {
            List<Long> values = List.of(Long.MIN_VALUE, -1L, 0L, 42L, Long.MAX_VALUE);
            writeLongs(valid, values);
            check("signed long round trip", values, values, readLongs(valid));

            writeLongs(valid, List.of());
            check("empty round trip", "empty values", List.of(), readLongs(valid));

            Files.write(invalid, new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
            check("wrong magic is rejected", invalid.getFileName(), "IOException",
                    thrownSimpleName(() -> readLongs(invalid)));
        } finally {
            Files.deleteIfExists(valid);
            Files.deleteIfExists(invalid);
        }
        report("Challenge 89");
    }

    @FunctionalInterface
    interface IoAction {
        void run() throws IOException;
    }

    private static String thrownSimpleName(IoAction action) {
        try {
            action.run();
            return "nothing thrown";
        } catch (Exception exception) {
            return exception.getClass().getSimpleName();
        }
    }

    // ---- test harness (identical in every challenge; not part of the exercise) ----

    private static int passes = 0;
    private static int failures = 0;

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
        System.out.println("      expected: " + "condition holds");
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
            return java.util.Arrays.deepToString(new Object[] { value })
                    .replaceAll("^\\[|\\]$", "");
        }
        if (!(value instanceof String s)) {
            return value + " (" + value.getClass().getSimpleName() + ")";
        }
        if (s.isEmpty()) {
            return "<> (empty)";
        }
        // -1 keeps the trailing empty field, so a value ending in \n still shows it.
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
    private static void report(String challenge) {
        System.out.println("----");
        System.out.println(challenge + ": " + passes + " passed, " + failures + " failed.");
        if (failures > 0) {
            throw new AssertionError(challenge + ": " + failures + " check(s) failed.");
        }
        System.out.println(challenge + " passed.");
    }
}
