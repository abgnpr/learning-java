/*
 * Challenge 134 — Date Boundary Contracts (Medium)
 *
 * Task: Build a strict formatter for calendar dates "uuuu-MM-dd", then generate
 * count monthly due dates beginning with anchor. Each due date uses the ORIGINAL
 * anchor's day, clamped only when its target month is shorter. A February clamp
 * must not change March's intended day. Return a detached unmodifiable list;
 * count == 0 gives empty and count < 0 throws IllegalArgumentException.
 * Inputs are non-null and target months stay within LocalDate's supported range.
 * Also predict the exact String returned by each fixed pattern scenario.
 * Complete: strictCalendarFormatter, monthlyDueDates, predictions. Baseline: Java 21.
 * Required focus: DateTimeFormatter/ResolverStyle, LocalDate/YearMonth, calendar
 * versus week-based years, year-of-era, and month-end adjustment/immutability.
 * Week-year and era examples explicitly fix Locale.US; no system date is used.
 * Run: java 11-io-time/DateBoundaryContracts.java
 * Explain: why SMART is insufficient for validation; why adding/subtracting a
 * month need not round-trip; anchor-day scheduling versus an end-of-month policy.
 */
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.function.Supplier;

public final class DateBoundaryContracts {
    static DateTimeFormatter strictCalendarFormatter() {
        throw new UnsupportedOperationException("TODO: build a strict calendar-date formatter");
    }

    static List<LocalDate> monthlyDueDates(LocalDate anchor, int count) {
        throw new UnsupportedOperationException("TODO: preserve the original intended day across months");
    }

    static Map<String, String> predictions() {
        throw new UnsupportedOperationException("TODO: predict every date-boundary scenario before running it");
    }

    static Map<String, Supplier<String>> scenarios() {
        Map<String, Supplier<String>> cases = new LinkedHashMap<>();
        cases.put("calendar-versus-week-year", () -> {
            LocalDate date = LocalDate.of(2019, 12, 29);
            return date.format(DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US)) + ","
                    + date.format(DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.US));
        });
        cases.put("proleptic-versus-year-of-era", () ->
                LocalDate.of(0, 1, 2).format(DateTimeFormatter.ofPattern("uuuu|yyyy G", Locale.US)));
        cases.put("smart-invalid-day", () ->
                LocalDate.parse("2025-02-30", DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ROOT))
                        .toString());
        cases.put("strict-year-of-era-without-era", () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);
            try { return LocalDate.parse("2024-02-29", formatter).toString(); }
            catch (DateTimeParseException expected) { return expected.getClass().getSimpleName(); }
        });
        cases.put("month-round-trip", () -> {
            LocalDate original = LocalDate.of(2025, 1, 31);
            LocalDate next = original.plusMonths(1);
            return original + "," + next + "," + next.minusMonths(1);
        });
        return cases;
    }

    public static void main(String[] args) {
        DateTimeFormatter formatter = strictCalendarFormatter();
        check("valid leap day", LocalDate.of(2024, 2, 29), LocalDate.parse("2024-02-29", formatter));
        check("proleptic year zero needs no era", LocalDate.of(0, 1, 2),
                LocalDate.parse("0000-01-02", formatter));
        check("formatting retains calendar year at a week-year boundary", "2019-12-29",
                LocalDate.of(2019, 12, 29).format(formatter));
        check("formatter uses proleptic rather than year-of-era output", "0000-01-02",
                LocalDate.of(0, 1, 2).format(formatter));
        for (String invalid : List.of("2025-02-29", "2025-02-30", "2024-04-31",
                "2024-00-10", "2024-13-10", "2024-01-00", "2024-02-29extra")) {
            expect(DateTimeParseException.class, () -> LocalDate.parse(invalid, formatter));
        }

        LocalDate january = LocalDate.of(2025, 1, 31);
        List<LocalDate> due = monthlyDueDates(january, 4);
        check("short months do not permanently move the anchor day", List.of(
                january, LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 4, 30)), due);
        check("anchor is unchanged", LocalDate.of(2025, 1, 31), january);
        check("leap February is clamped to day 29", List.of(LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31)),
                monthlyDueDates(LocalDate.of(2024, 1, 31), 3));
        check("year boundary preserves the original day", List.of(LocalDate.of(2024, 12, 30),
                LocalDate.of(2025, 1, 30), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 30)),
                monthlyDueDates(LocalDate.of(2024, 12, 30), 4));
        check("February's last day is an anchor day, not an end-of-month flag", List.of(
                LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 28)),
                monthlyDueDates(LocalDate.of(2025, 2, 28), 2));
        check("ordinary day survives every month", List.of(LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 2, 15), LocalDate.of(2025, 3, 15)),
                monthlyDueDates(LocalDate.of(2025, 1, 15), 3));
        check("singleton schedule includes anchor", List.of(january), monthlyDueDates(january, 1));
        check("zero count", List.of(), monthlyDueDates(january, 0));
        expect(IllegalArgumentException.class, () -> monthlyDueDates(january, -1));
        expect(UnsupportedOperationException.class, () -> due.add(january));
        expect(UnsupportedOperationException.class, () -> due.set(0, january.plusDays(1)));
        expect(UnsupportedOperationException.class, () -> monthlyDueDates(january, 0).add(january));
        check("separate invocation does not change an earlier schedule", List.of(
                january, LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 4, 30)), due);

        Map<String, String> guesses = predictions();
        Map<String, Supplier<String>> cases = scenarios();
        check("every scenario has exactly one prediction", cases.keySet(), guesses.keySet());
        cases.forEach((name, scenario) -> check(name, guesses.get(name), scenario.get()));
        report("Challenge 134");
    }

    // ---- test harness (not part of the exercise) ----
    private static int passes;
    private static int failures;

    private static void check(String label, Object expected, Object actual) {
        boolean ok = java.util.Objects.deepEquals(expected, actual);
        if (ok) passes++; else failures++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + label);
        System.out.println("      expected: " + show(expected));
        System.out.println("      actual:   " + show(actual));
    }

    private static String show(Object value) {
        if (value == null) return "null";
        if (value instanceof String text) return "<" + text.replace("\n", "\\n") + ">";
        if (value.getClass().isArray()) return java.util.Arrays.deepToString(new Object[]{value});
        return value + " (" + value.getClass().getSimpleName() + ")";
    }

    @FunctionalInterface
    interface CheckedAction { void run() throws Exception; }

    private static <E extends Throwable> E expect(Class<E> type, CheckedAction action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (!type.isInstance(actual)) throw new AssertionError("expected " + type.getName()
                    + ", got " + actual.getClass().getName(), actual);
            passes++;
            System.out.println("PASS  throws " + type.getSimpleName());
            return type.cast(actual);
        }
        throw new AssertionError("expected " + type.getName() + ", nothing was thrown");
    }

    private static void report(String name) {
        System.out.println(name + ": " + passes + " passed, " + failures + " failed.");
        if (failures != 0) throw new AssertionError(name + " failed");
        System.out.println(name + " passed.");
    }
}
