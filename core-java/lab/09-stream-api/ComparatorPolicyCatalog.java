/*
 * Challenge 130 — Comparator Policy Catalog (Medium)
 *
 * Task: Return reusable comparators for different application requirements.
 * - reportOrder: team ASC, severity DESC, owner natural ASC (null owner last), id ASC.
 * - ownerOrder: owner case-insensitive, then exact spelling, both in the chosen
 *   direction; id always ASC. Null owners AND null Ticket objects stay last in
 *   BOTH directions. No other method accepts null Ticket objects.
 * - triageOrder: urgent first, then status OPEN / IN_PROGRESS / RESOLVED /
 *   unknown-or-null, then oldest created timestamp first, then id ASC. Unknown
 *   statuses share one rank; do not alphabetize them before the timestamp.
 * - newestFirst: created timestamp DESC, then id ASC.
 * Every non-null Ticket has a non-null team and created timestamp. Owner and
 * status may be null. Numeric fields span the complete int range.
 * Complete: reportOrder, ownerOrder, triageOrder, newestFirst. Baseline: Java 21.
 * Required focus: comparing/comparingInt, thenComparing, key-local reversal,
 * nullsLast, String.CASE_INSENSITIVE_ORDER, and explicit business ranks.
 * Run: java 09-stream-api/ComparatorPolicyCatalog.java
 * Explain: why reversing a completed comparator changes null/ID policies;
 * why a report comparator's zero result is not automatically object equality.
 */
import java.time.Instant;
import java.util.*;

public final class ComparatorPolicyCatalog {
    record Ticket(int id, String team, String owner, String status,
                  boolean urgent, int severity, Instant created) { }

    static Comparator<Ticket> reportOrder() {
        throw new UnsupportedOperationException("TODO: compose mixed report keys");
    }

    static Comparator<Ticket> ownerOrder(boolean descending) {
        throw new UnsupportedOperationException("TODO: apply direction without reversing nulls or ids");
    }

    static Comparator<Ticket> triageOrder() {
        throw new UnsupportedOperationException("TODO: compose urgency, business status, age, and id");
    }

    static Comparator<Ticket> newestFirst() {
        throw new UnsupportedOperationException("TODO: sort latest timestamps first with ascending ids");
    }

    public static void main(String[] args) {
        List<Ticket> tickets = List.of(
                ticket(1, "Tech", "zoe", "OPEN", false, 2, 4),
                ticket(2, "HR", "Mia", "IN_PROGRESS", true, 9, 3),
                ticket(3, "Tech", "ana", "OPEN", false, 5, 2),
                ticket(4, "Tech", "Ben", "RESOLVED", true, 5, 1),
                ticket(5, "Tech", "Ana", "OPEN", false, 5, 2),
                ticket(6, "Tech", null, null, false, 5, 0));
        check("report mixes directions and natural case order", List.of(2, 5, 4, 3, 6, 1),
                ids(tickets, reportOrder()));
        check("newest first keeps id ties ascending", List.of(1, 2, 3, 5, 4, 6),
                ids(tickets, newestFirst()));
        check("urgent then business status then oldest", List.of(2, 4, 3, 5, 1, 6),
                ids(tickets, triageOrder()));

        List<Ticket> nullable = new ArrayList<>(tickets);
        nullable.add(null);
        check("case-insensitive ASC with spelling tie and both null levels", Arrays.asList(5, 3, 4, 2, 1, 6, null),
                ids(nullable, ownerOrder(false)));
        check("DESC reverses text keys, keeps nulls last", Arrays.asList(1, 2, 4, 3, 5, 6, null),
                ids(nullable, ownerOrder(true)));
        List<Ticket> ownerTie = List.of(
                ticket(10, "A", "same", "OPEN", false, 1, 0),
                ticket(7, "A", "same", "OPEN", false, 1, 0));
        check("id remains ASC for descending owners", List.of(7, 10), ids(ownerTie, ownerOrder(true)));
        List<Ticket> nullOwnerTie = List.of(
                ticket(10, "A", null, "OPEN", false, 1, 0),
                ticket(7, "A", null, "OPEN", false, 1, 0));
        check("null owners still use id tie-breaker", List.of(7, 10), ids(nullOwnerTie, ownerOrder(true)));

        Ticket unknownEarly = ticket(7, "A", "a", "ZZZ", false, 0, -1);
        Ticket unknownLate = ticket(8, "A", "a", "AAA", false, 0, 1);
        check("unknown and null statuses share rank and use age", List.of(7, 6, 8),
                ids(List.of(unknownLate, tickets.get(5), unknownEarly), triageOrder()));
        check("urgency outranks even a known high-priority status", List.of(9, 10), ids(List.of(
                ticket(10, "A", "a", "OPEN", false, 0, 0),
                ticket(9, "A", "a", "UNKNOWN", true, 0, 1)), triageOrder()));
        check("severity comparison does not subtract and overflow", List.of(9, 10), ids(List.of(
                ticket(10, "A", "same", "OPEN", false, Integer.MIN_VALUE, 0),
                ticket(9, "A", "same", "OPEN", false, Integer.MAX_VALUE, 0)), reportOrder()));
        check("id comparison handles extreme values", List.of(Integer.MIN_VALUE, Integer.MAX_VALUE), ids(List.of(
                ticket(Integer.MAX_VALUE, "A", "same", "OPEN", false, 0, 0),
                ticket(Integer.MIN_VALUE, "A", "same", "OPEN", false, 0, 0)), reportOrder()));
        check("empty report", List.of(), ids(List.of(), reportOrder()));
        check("single null object is supported by owner order", Collections.singletonList(null),
                ids(Collections.singletonList(null), ownerOrder(false)));

        check("report comparison contract on sample", true, obeysContract(tickets, reportOrder()));
        check("triage comparison contract on sample", true, obeysContract(tickets, triageOrder()));
        check("owner comparison contract including nulls", true, obeysContract(nullable, ownerOrder(true)));
        report("Challenge 130");
    }

    // ---- test harness (not part of the exercise) ----
    private static Ticket ticket(int id, String team, String owner, String status,
                                 boolean urgent, int severity, long hours) {
        return new Ticket(id, team, owner, status, urgent, severity,
                Instant.parse("2026-01-01T00:00:00Z").plusSeconds(hours * 3600));
    }

    private static List<Integer> ids(List<Ticket> input, Comparator<Ticket> order) {
        List<Ticket> sorted = new ArrayList<>(input);
        sorted.sort(order);
        List<Integer> ids = new ArrayList<>();
        for (Ticket ticket : sorted) ids.add(ticket == null ? null : ticket.id());
        return ids;
    }

    // Diagnostic checks over a finite sample, not a proof for all possible inputs.
    private static <T> boolean obeysContract(List<T> sample, Comparator<T> order) {
        for (T a : sample) for (T b : sample) {
            int ab = Integer.signum(order.compare(a, b));
            if (ab != -Integer.signum(order.compare(b, a))) return false;
            for (T c : sample) {
                int bc = Integer.signum(order.compare(b, c));
                int ac = Integer.signum(order.compare(a, c));
                if (ab <= 0 && bc <= 0 && ac > 0) return false;
                if (ab == 0 && ac != bc) return false;
            }
        }
        return true;
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
