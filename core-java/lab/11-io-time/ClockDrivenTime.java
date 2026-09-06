/*
 * Challenge 91 — Clock-driven Temporal Snapshot (Medium)
 *
 * Task: Calculate machine elapsed time and calendar membership using an
 * injected Clock. Duration measures between Instants; Period measures between
 * LocalDates in the clock's zone.
 * Complete: snapshot(Instant, LocalDate, Clock)
 * Run: java ClockDrivenTime.java
 */
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

public class ClockDrivenTime {
    record Snapshot(Duration elapsed, Period membership) { }

    static Snapshot snapshot(Instant startedAt, LocalDate joinedOn, Clock clock) {
        // TODO: Use the injected clock for both timeline and calendar calculations.
        throw new UnsupportedOperationException("TODO: calculate temporal snapshot");
    }

    public static void main(String[] args) {
        Instant now = Instant.parse("2024-03-01T12:00:00Z");
        Clock utc = Clock.fixed(now, ZoneId.of("UTC"));
        check("fixed UTC snapshot",
                "start 2024-03-01T10Z, joined 2020-01-15",
                new Snapshot(Duration.ofHours(2), Period.of(4, 1, 15)),
                snapshot(
                        Instant.parse("2024-03-01T10:00:00Z"),
                        LocalDate.of(2020, 1, 15),
                        utc));

        Clock kolkata = Clock.fixed(
                Instant.parse("2024-02-29T20:00:00Z"),
                ZoneId.of("Asia/Kolkata"));
        check("calendar date comes from clock zone",
                "2024-02-29T20Z is March 1 in Kolkata",
                Period.ofDays(1),
                snapshot(
                        Instant.parse("2024-02-29T19:30:00Z"),
                        LocalDate.of(2024, 2, 29),
                        kolkata).membership());
        check("duration stays timeline-based",
                "30 minutes across zones",
                Duration.ofMinutes(30),
                snapshot(
                        Instant.parse("2024-02-29T19:30:00Z"),
                        LocalDate.of(2024, 2, 29),
                        kolkata).elapsed());
        report("Challenge 91");
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
