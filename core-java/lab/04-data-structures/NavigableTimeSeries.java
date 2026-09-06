/*
 * Challenge DS-A2 — Navigable Time-series Lookup (Hard)
 *
 * Task: Index readings by timestamp. For a query timestamp, return the reading
 * closest in time. Prefer the earlier reading when distances tie. Return empty
 * when there are no readings. Duplicate timestamps keep the last reading.
 * Complete: indexReadings(List<Reading>), closestReading(NavigableMap, long)
 * Run: java NavigableTimeSeries.java
 */
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public class NavigableTimeSeries {
    record Reading(long timestamp, int value) { }

    static NavigableMap<Long, Reading> indexReadings(List<Reading> readings) {
        // TODO: Build a timestamp-ordered index; later duplicates replace earlier ones.
        throw new UnsupportedOperationException("TODO: build the time-series index");
    }

    static Optional<Reading> closestReading(
            NavigableMap<Long, Reading> readingsByTimestamp, long queryTimestamp) {
        // TODO: Compare the floor and ceiling entries without scanning the map.
        throw new UnsupportedOperationException("TODO: query the time-series index");
    }

    public static void main(String[] args) {
        List<Reading> readings = List.of(
                new Reading(40, 400),
                new Reading(10, 100),
                new Reading(30, 300),
                new Reading(20, 200));
        NavigableMap<Long, Reading> index = indexReadings(readings);

        check("timestamps are sorted", "readings supplied out of order",
                List.of(10L, 20L, 30L, 40L), List.copyOf(index.keySet()));
        checkThat("index is a TreeMap", "indexReadings(readings)", index instanceof TreeMap);
        check("exact timestamp", "query 20", Optional.of(new Reading(20, 200)),
                closestReading(index, 20));
        check("closer ceiling", "query 28", Optional.of(new Reading(30, 300)),
                closestReading(index, 28));
        check("closer floor", "query 12", Optional.of(new Reading(10, 100)),
                closestReading(index, 12));
        check("tie prefers earlier", "query 25", Optional.of(new Reading(20, 200)),
                closestReading(index, 25));
        check("below first timestamp", "query 1", Optional.of(new Reading(10, 100)),
                closestReading(index, 1));
        check("above last timestamp", "query 99", Optional.of(new Reading(40, 400)),
                closestReading(index, 99));
        check("empty input", "empty readings, query 50", Optional.empty(),
                closestReading(new TreeMap<>(), 50));
        check("last duplicate wins", "readings at 10 twice",
                Optional.of(new Reading(10, 999)),
                closestReading(indexReadings(List.of(
                        new Reading(10, 100), new Reading(10, 999))), 10));
        report("Challenge DS-A2");
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
