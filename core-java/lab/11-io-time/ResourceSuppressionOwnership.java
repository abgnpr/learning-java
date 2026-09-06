/*
 * Challenge 90 — Resource Suppression and Ownership (Hard)
 *
 * Task: Use two caller-supplied resources in one try-with-resources statement.
 * Record the primary failure, suppressed failures, and event order. The body
 * failure wins; otherwise the first close failure encountered wins.
 * Complete: execute(Probe first, Probe second, boolean failBody)
 * Run: java ResourceSuppressionOwnership.java
 */
import java.util.ArrayList;
import java.util.List;

public class ResourceSuppressionOwnership {
    record Outcome(String primary, List<String> suppressed, List<String> events) { }

    static final class Probe implements AutoCloseable {
        private final String name;
        private final boolean failOnClose;
        private final List<String> events;

        Probe(String name, boolean failOnClose, List<String> events) {
            this.name = name;
            this.failOnClose = failOnClose;
            this.events = events;
        }

        void use() {
            events.add("use-" + name);
        }

        @Override
        public void close() {
            events.add("close-" + name);
            if (failOnClose) {
                throw new IllegalStateException("close-" + name);
            }
        }
    }

    static Outcome execute(Probe first, Probe second, boolean failBody) {
        // TODO: Take ownership with try-with-resources and capture failure metadata.
        throw new UnsupportedOperationException("TODO: manage resource ownership");
    }

    public static void main(String[] args) {
        List<String> bodyEvents = new ArrayList<>();
        Outcome bodyFailure = execute(
                new Probe("first", true, bodyEvents),
                new Probe("second", true, bodyEvents),
                true);
        check("body remains primary", bodyEvents, "body", bodyFailure.primary());
        check("close failures are suppressed in close order", bodyEvents,
                List.of("close-second", "close-first"), bodyFailure.suppressed());
        check("resources close in reverse declaration order", bodyEvents,
                List.of("use-first", "use-second", "close-second", "close-first"),
                bodyFailure.events());

        List<String> closeEvents = new ArrayList<>();
        Outcome closeFailure = execute(
                new Probe("first", true, closeEvents),
                new Probe("second", true, closeEvents),
                false);
        check("second close is primary without body failure", closeEvents,
                "close-second", closeFailure.primary());
        check("first close becomes suppressed", closeEvents,
                List.of("close-first"), closeFailure.suppressed());
        report("Challenge 90");
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
