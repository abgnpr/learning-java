/*
 * Challenge 92 — Zoned Scheduling Across DST (Hard)
 *
 * Task: Parse an English textual local date-time and resolve it in a named
 * zone. Use ZonedDateTime's gap/overlap rules. Measure elapsed minutes on the
 * instant timeline rather than subtracting wall-clock fields.
 * Complete: englishFormatter(), schedule(...), elapsedMinutes(...)
 * Run: java ZonedDstScheduling.java
 */
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ZonedDstScheduling {
    static DateTimeFormatter englishFormatter() {
        // TODO: Build a strict, locale-stable formatter for "dd MMM uuuu HH:mm".
        throw new UnsupportedOperationException("TODO: build the formatter");
    }

    static ZonedDateTime schedule(String text, ZoneId zone) {
        // TODO: Parse the local value and resolve it with the zone's transition rules.
        throw new UnsupportedOperationException("TODO: resolve the zoned schedule");
    }

    static long elapsedMinutes(ZonedDateTime start, ZonedDateTime end) {
        // TODO: Measure elapsed timeline duration, not local field difference.
        throw new UnsupportedOperationException("TODO: calculate elapsed minutes");
    }

    public static void main(String[] args) {
        ZoneId newYork = ZoneId.of("America/New_York");

        ZonedDateTime gap = schedule("10 Mar 2024 02:30", newYork);
        check("DST gap moves forward", "10 Mar 2024 02:30 America/New_York",
                LocalDateTime.of(2024, 3, 10, 3, 30), gap.toLocalDateTime());
        check("gap uses daylight offset", gap, "-04:00", gap.getOffset().toString());

        ZonedDateTime overlap = schedule("03 Nov 2024 01:30", newYork);
        check("overlap chooses earlier offset by default", overlap,
                "-04:00", overlap.getOffset().toString());

        ZonedDateTime beforeGap = schedule("10 Mar 2024 01:30", newYork);
        ZonedDateTime afterGap = schedule("10 Mar 2024 03:30", newYork);
        check("two wall-clock hours are one elapsed hour", "01:30 to 03:30 at spring DST",
                60L, elapsedMinutes(beforeGap, afterGap));

        check("formatter is explicitly English", "05 Dec 2024 09:15",
                12, schedule("05 Dec 2024 09:15", ZoneId.of("UTC")).getMonthValue());
        report("Challenge 92");
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
