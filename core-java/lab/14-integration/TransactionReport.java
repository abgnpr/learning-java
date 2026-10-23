/*
 * Challenge 129 — Transaction Report (Hard)
 *
 * Task: Read UTF-8 CSV without a header or quoting. Each non-blank row has six
 * fields: tenant,id,account,kind,amount,instant. Trim every field. Required text
 * fields are non-blank; kind is CREDIT or DEBIT (case-sensitive); amount is
 * non-negative, exactly representable at scale 2; instant is ISO-8601 Instant.
 * parse rejects malformed rows with InvalidRowException whose message contains
 * the one-based physical line number and whose cause preserves parse failures.
 *
 * summarize deduplicates by (tenant,id), preserving first-seen order. An identical
 * repeated transaction increments duplicates; conflicting reuse of a key throws
 * IllegalArgumentException. IDs may repeat across tenants. Signed per-account
 * totals use CREDIT positive / DEBIT negative and retain zero-balance accounts.
 * Rank accounts by net descending, then tenant ascending, then account ascending.
 * Return unmodifiable accepted, netByAccount, and ranking collections. Leave
 * caller input unchanged. Records contain immutable values.
 *
 * read owns/closes its file stream, skips blank lines, and delegates to the other
 * methods. Missing files propagate IOException. Do not read the whole file into
 * one String. No database, Spring, networking, or other labs are dependencies.
 * Complete: parse, summarize, read. Run: java 14-integration/TransactionReport.java
 * Explain: deduplication policy; logical keys; exact arithmetic; grouping and
 * sorting choices; resource ownership; error boundary; time representation.
 */
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class TransactionReport {
    enum Kind { CREDIT, DEBIT }
    record AccountKey(String tenant, String account) { }
    record Transaction(String tenant, String id, String account, Kind kind,
                       BigDecimal amount, Instant at) { }
    record AccountTotal(AccountKey account, BigDecimal net) { }
    record Report(List<Transaction> accepted, Map<AccountKey, BigDecimal> netByAccount,
                  List<AccountTotal> ranking, int duplicates) { }

    static final class InvalidRowException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        InvalidRowException(int line, Throwable cause) { super("invalid transaction at line " + line, cause); }
    }

    static Transaction parse(String row, int lineNumber) {
        throw new UnsupportedOperationException("TODO: parse and validate with line-aware failures");
    }

    // Input transactions already satisfy parse's invariants. Do not reparse them.
    static Report summarize(List<Transaction> transactions) {
        throw new UnsupportedOperationException("TODO: deduplicate, aggregate, rank, and snapshot");
    }

    static Report read(Path input) throws IOException {
        throw new UnsupportedOperationException("TODO: stream owned UTF-8 input and preserve physical line numbers");
    }

    public static void main(String[] args) throws Exception {
        String instant = "2026-01-02T03:04:05Z";
        Transaction credit = parse(" in, t1, alpha, CREDIT, 10.000, " + instant, 1);
        check("trim, normalize exact scale, parse timestamp",
                new Transaction("in", "t1", "alpha", Kind.CREDIT, new BigDecimal("10.00"), Instant.parse(instant)), credit);
        for (String bad : List.of(
                "in,t1,alpha,CREDIT,1.001," + instant,
                "in,t1,alpha,CREDIT,-1," + instant,
                "in,t1,alpha,credit,1," + instant,
                "in,t1,alpha,CREDIT,1,not-an-instant",
                "in,t1,,CREDIT,1," + instant,
                "in,t1,alpha,CREDIT,1,",
                "in,t1,alpha,CREDIT,1," + instant + ",extra")) {
            InvalidRowException failure = expect(InvalidRowException.class, () -> parse(bad, 7));
            check("malformed row reports physical line", true, failure.getMessage().contains("line 7"));
            check("malformed row retains cause", true, failure.getCause() != null);
        }
        InvalidRowException numeric = expect(InvalidRowException.class,
                () -> parse("in,t1,alpha,CREDIT,not-money," + instant, 9));
        check("numeric cause type is retained", true, numeric.getCause() instanceof NumberFormatException);

        Transaction debit = parse("in,t2,alpha,DEBIT,3.00," + instant, 2);
        Transaction tie = parse("in,t3,beta,CREDIT,7," + instant, 3);
        Transaction otherTenant = parse("uk,t1,alpha,CREDIT,7," + instant, 4);
        Transaction zero = parse("in,t4,zero,CREDIT,0," + instant, 5);
        Transaction negative = parse("in,t5,negative,DEBIT,2," + instant, 6);
        List<Transaction> input = new ArrayList<>(List.of(otherTenant, credit, debit, credit, tie, zero, negative));
        List<Transaction> before = List.copyOf(input);
        Report report = summarize(input);
        AccountKey alpha = new AccountKey("in", "alpha"), beta = new AccountKey("in", "beta");
        AccountKey uk = new AccountKey("uk", "alpha"), zeroKey = new AccountKey("in", "zero");
        AccountKey minus = new AccountKey("in", "negative");
        check("deduplication preserves first encounter", List.of(otherTenant, credit, debit, tie, zero, negative), report.accepted());
        check("identical repetition counted", 1, report.duplicates());
        check("tenant namespaces transaction ids", 6, report.accepted().size());
        check("signed exact totals including zero", Map.of(alpha, new BigDecimal("7.00"), beta, new BigDecimal("7.00"),
                uk, new BigDecimal("7.00"), zeroKey, new BigDecimal("0.00"), minus, new BigDecimal("-2.00")), report.netByAccount());
        check("ranking has deterministic tie breakers", List.of(
                new AccountTotal(alpha, new BigDecimal("7.00")), new AccountTotal(beta, new BigDecimal("7.00")),
                new AccountTotal(uk, new BigDecimal("7.00")), new AccountTotal(zeroKey, new BigDecimal("0.00")),
                new AccountTotal(minus, new BigDecimal("-2.00"))), report.ranking());
        check("source order and contents unchanged", before, input);
        input.clear();
        check("returned accepted list is detached", 6, report.accepted().size());
        expect(UnsupportedOperationException.class, () -> report.accepted().clear());
        expect(UnsupportedOperationException.class, () -> report.netByAccount().put(alpha, BigDecimal.ZERO));
        expect(UnsupportedOperationException.class, () -> report.ranking().clear());
        Transaction conflict = parse("in,t1,alpha,CREDIT,11," + instant, 10);
        expect(IllegalArgumentException.class, () -> summarize(List.of(credit, conflict)));
        check("empty report", new Report(List.of(), Map.of(), List.of(), 0), summarize(List.of()));
        Transaction cancellation = parse("in,t6,alpha,DEBIT,10," + instant, 11);
        check("net-zero account remains in result", new BigDecimal("0.00"),
                summarize(List.of(credit, cancellation)).netByAccount().get(alpha));

        Path directory = Files.createTempDirectory("transaction-report-");
        Path file = directory.resolve("transactions.csv");
        try {
            Files.writeString(file, "in,t1,alpha,CREDIT,10," + instant + "\n\n"
                    + "in,t2,alpha,DEBIT,3," + instant + "\n"
                    + "in,t1,alpha,CREDIT,10.00," + instant + "\n", StandardCharsets.UTF_8);
            check("file workflow matches in-memory report", summarize(List.of(credit, debit, credit)), read(file));
            Files.writeString(file, "\n\nin,t1,alpha,CREDIT,bad," + instant, StandardCharsets.UTF_8);
            InvalidRowException rowFailure = expect(InvalidRowException.class, () -> read(file));
            check("blank lines still count physically", true, rowFailure.getMessage().contains("line 3"));
            Files.writeString(file, "in,u1,किराना,CREDIT,1," + instant, StandardCharsets.UTF_8);
            check("UTF-8 account survives", new BigDecimal("1.00"), read(file).netByAccount().get(new AccountKey("in", "किराना")));
            Files.writeString(file, " \n\t\n", StandardCharsets.UTF_8);
            check("blank-only file", new Report(List.of(), Map.of(), List.of(), 0), read(file));
            expect(IOException.class, () -> read(directory.resolve("missing.csv")));
        } finally {
            Files.deleteIfExists(file); Files.deleteIfExists(directory);
        }
        report("Challenge 129");
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
