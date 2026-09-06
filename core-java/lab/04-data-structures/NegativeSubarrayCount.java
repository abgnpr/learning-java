/*
 * Challenge 30 — Java Subarray (Easy)
 *
 * Task: Count the contiguous, non-empty slices of an integer array whose
 * element sum is negative.
 * Complete: countNegativeSubarrays(int[] values)
 * Run: java NegativeSubarrayCount.java
 */

public class NegativeSubarrayCount {
    // ---- Approach 1 — enumerate every slice, O(n^3) ----
    //
    // Outer loop picks a window length, inner loop slides that window across
    // the array; `values.length - n + 1` is how many windows of size n fit,
    // because a window starting at i needs i + n - 1 in bounds, so i <= length - n.
    //
    // A running sum cannot collapse this to O(n^2) as written: stepping i at a
    // fixed n moves the window sideways, dropping values[i] and adding
    // values[i + n] — two changes, so a plain accumulator has nothing to carry.
    // The start-major shape (fix the start, extend the end) is the one where
    // `sum += values[j]` does all the work, and it is also the shape prefix
    // sums and Kadane's algorithm build on.
    //
    // private static long sumInclusive(int[] values, int start, int end) {
    //     long sum = 0;
    //     for (int i = start; i <= end; i++) {
    //         sum += values[i];
    //     }
    //     return sum;
    // }
    //
    // static long countNegativeSubarrays(int[] values) {
    //     long count = 0;
    //     for (int n = 1; n <= values.length; n++) {
    //         for (int i = 0; i < values.length - n + 1; i++) {
    //             if (sumInclusive(values, i, i + n - 1) < 0) {
    //                 count++;
    //             }
    //         }
    //     }
    //     return count;
    // }

    // ---- Approach 2 — count inversions in the prefix array, O(n log n) ----
    //
    // With P[0] = 0 and P[k] = values[0] + ... + values[k-1], the slice [i..j]
    // sums to P[j+1] - P[i]. So the slice is negative exactly when
    // P[j+1] < P[i] — an earlier prefix exceeding a later one. Every non-empty
    // slice is one such index pair, so counting negative slices IS counting
    // inversions in P.
    //
    // The general move: "count pairs satisfying a comparison" -> prefix
    // transform -> inversion count. "Subarrays with sum < K" is the same
    // skeleton with a different merge-step query.
    static long countNegativeSubarrays(int[] values) {
        // P is long, not int: with n elements at Integer.MIN_VALUE the true
        // sum reaches -2^31 * n, which wraps to a wrong sign in int.
        long[] prefix = new long[values.length + 1];
        for (int i = 0; i < values.length; i++) {
            prefix[i + 1] = prefix[i] + values[i];
        }
        return sortAndCountInversions(
                prefix,
                new long[prefix.length],
                0,
                prefix.length);
    }

    /** Counts inversions in p[lo, hi) and leaves that range sorted. */
    private static long sortAndCountInversions(long[] p, long[] buffer, int lo, int hi) {
        if (hi - lo < 2) {
            return 0;
        }
        // >>> not / 2: an unsigned shift cannot overflow to a negative midpoint
        // the way (lo + hi) / 2 does once the sum exceeds Integer.MAX_VALUE.
        int mid = (lo + hi) >>> 1;
        long count = sortAndCountInversions(p, buffer, lo, mid)
                + sortAndCountInversions(p, buffer, mid, hi);
        int left = lo, right = mid, out = lo;
        while (left < mid && right < hi) {
            if (p[left] <= p[right]) {
                // <= and not <: equal prefixes mean a slice summing to zero,
                // which is not negative. Taking from the left on ties leaves
                // those pairs uncounted; < would count them as inversions.
                buffer[out++] = p[left++];
            } else {
                // p is sorted within each half, so p[left] > p[right] implies
                // every remaining left element also exceeds p[right]. Retiring
                // the whole block at once is what buys the log factor — this
                // line is the algorithm; `count++` here would just be O(n^2).
                count += mid - left;
                buffer[out++] = p[right++];
            }
        }
        while (left < mid) {
            buffer[out++] = p[left++];
        }
        while (right < hi) {
            buffer[out++] = p[right++];
        }
        System.arraycopy(buffer, lo, p, lo, hi - lo);
        return count;
    }

    public static void main(String[] args) {
        check("mixed sample", "new int[] { 1, -2, 4, -5, 1 }", 9L,
                countNegativeSubarrays(new int[] { 1, -2, 4, -5, 1 }));
        check("no negative sum", "new int[] { 1, 2, 3 }", 0L, countNegativeSubarrays(new int[] { 1, 2, 3 }));
        check("all slices are negative", "new int[] { -1, -1 }", 3L, countNegativeSubarrays(new int[] { -1, -1 }));

        // Degenerate sizes: nothing to slice, and the two one-element verdicts.
        check("empty array", "new int[] { }", 0L, countNegativeSubarrays(new int[] {}));
        check("single negative", "new int[] { -5 }", 1L, countNegativeSubarrays(new int[] { -5 }));
        check("single positive", "new int[] { 5 }", 0L, countNegativeSubarrays(new int[] { 5 }));

        // Zero is not negative, so no slice of zeros counts.
        check("all zeros", "new int[] { 0, 0, 0 }", 0L, countNegativeSubarrays(new int[] { 0, 0, 0 }));
        check("slice cancels to zero", "new int[] { 3, -3 }", 1L, countNegativeSubarrays(new int[] { 3, -3 }));

        // Every negative slice here is strictly interior: it touches neither end
        // of the array, so counting only prefixes and suffixes scores 0.
        check("only interior slices are negative", "new int[] { 5, -1, -1, 5 }", 3L,
                countNegativeSubarrays(new int[] { 5, -1, -1, 5 }));

        // n(n+1)/2 = 10 slices, all negative — the upper bound for n = 4.
        check("every slice negative", "new int[] { -1, -2, -3, -4 }", 10L,
                countNegativeSubarrays(new int[] { -1, -2, -3, -4 }));
        check("alternating signs", "new int[] { 2, -3, 2, -3, 2 }", 9L,
                countNegativeSubarrays(new int[] { 2, -3, 2, -3, 2 }));
        check("interior slices sum to zero", "new int[] { 1, -1, 1, -1 }", 3L,
                countNegativeSubarrays(new int[] { 1, -1, 1, -1 }));

        // Sums must accumulate in long: -2^31 + -2^31 overflows int to 0, which
        // would read as non-negative and lose the whole-array slice.
        check("sum overflows int", "new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE }", 3L,
                countNegativeSubarrays(new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE }));
        check("positive sum overflows int", "new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, -1 }", 1L,
                countNegativeSubarrays(new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, -1 }));
        report("Challenge 30");
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
