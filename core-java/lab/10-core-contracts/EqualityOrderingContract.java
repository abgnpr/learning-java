/*
 * Challenge 85 — Equality and Ordering Contract (Hard)
 *
 * Task: Make Release a value suitable for HashSet and TreeSet. Equality,
 * hashing, and natural ordering must all use major then minor; compareTo must
 * return zero exactly when equals does.
 * Complete: Release.equals(...), Release.hashCode(), Release.compareTo(...)
 * Run: java EqualityOrderingContract.java
 */
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class EqualityOrderingContract {
    static final class Release implements Comparable<Release> {
        private final int major;
        private final int minor;

        Release(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        @Override
        public boolean equals(Object other) {
            // TODO: Implement logical equality using both version components.
            throw new UnsupportedOperationException("TODO: compare releases");
        }

        @Override
        public int hashCode() {
            // TODO: Hash exactly the fields used by equals.
            throw new UnsupportedOperationException("TODO: hash releases");
        }

        @Override
        public int compareTo(Release other) {
            // TODO: Order by major, then minor, without subtraction overflow.
            throw new UnsupportedOperationException("TODO: order releases");
        }

        @Override
        public String toString() {
            return major + "." + minor;
        }
    }

    static Comparator<Release> newestFirst() {
        // TODO: Reverse the full natural order while remaining consistent with equals.
        throw new UnsupportedOperationException("TODO: build consistent comparator");
    }

    public static void main(String[] args) {
        Release oneTwo = new Release(1, 2);
        Release same = new Release(1, 2);
        Release oneTen = new Release(1, 10);
        Release twoZero = new Release(2, 0);

        check("logical equality", "1.2 and a separate 1.2", true, oneTwo.equals(same));
        check("equal values share hash", "two 1.2 values", oneTwo.hashCode(), same.hashCode());
        check("compareTo agrees with equals", "two 1.2 values", 0, oneTwo.compareTo(same));

        Set<Release> hashed = new HashSet<>(List.of(oneTwo, same, oneTen, twoZero));
        check("HashSet value uniqueness", hashed, 3, hashed.size());

        Set<Release> sorted = new TreeSet<>(List.of(twoZero, oneTen, same, oneTwo));
        check("TreeSet order and uniqueness", sorted,
                List.of(oneTwo, oneTen, twoZero), List.copyOf(sorted));

        Set<Release> newest = new TreeSet<>(newestFirst());
        newest.addAll(List.of(oneTwo, same, oneTen, twoZero));
        check("custom comparator retains equality consistency", newest,
                List.of(twoZero, oneTen, oneTwo), List.copyOf(newest));

        Release min = new Release(Integer.MIN_VALUE, 0);
        Release max = new Release(Integer.MAX_VALUE, 0);
        checkThat("comparison avoids integer overflow", "MIN_VALUE versus MAX_VALUE",
                min.compareTo(max) < 0);
        report("Challenge 85");
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
