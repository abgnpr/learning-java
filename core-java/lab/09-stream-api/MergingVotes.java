
/*
 * Challenge 78: Building Maps with Duplicate Keys
 * Difficulty: Medium
 *
 * Task: Collect votes into a candidate-to-points map. Sum points when a
 * candidate appears more than once and return a TreeMap for deterministic key
 * order. Zero and negative point adjustments are valid.
 * Complete: static Map<String, Integer> totals(List<Vote> votes)
 * Required focus: Collectors.toMap key mapper, value mapper, merge function,
 * and map supplier.
 * Run: java MergingVotes.java
 */
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class MergingVotes {
    record Vote(String candidate, int points) {
    }

    private MergingVotes() {
    }

    static Map<String, Integer> totals(List<Vote> votes) {
        // approach 1 — groupingBy with reducing. Strictly worse than approach 3:
        // three arguments to say what summingInt says in one, and it reduces in
        // Integer, so every element unboxes to add and reboxes. summingInt
        // accumulates in a primitive int and boxes once, at the end.
        // return votes.stream().collect(
        //         Collectors.groupingBy(
        //                 Vote::candidate,
        //                 TreeMap::new,
        //                 Collectors.reducing(0, Vote::points, Integer::sum)));

        // approach 2 — toMap. The intended solution: the challenge's required
        // focus is toMap's key mapper, value mapper, merge function and map
        // supplier, and approach 3 exercises none of the four.
        // The merge function is the whole point: toMap builds
        // entries one at a time, so a repeated candidate is a collision it must be
        // told how to resolve. Drop Integer::sum and the two-arg overload throws
        // IllegalStateException: Duplicate key (asserted in main).
        // Only this four-arg overload takes a map supplier; the three-arg one
        // cannot produce a TreeMap.
        // Its other sharp edge: toMap merges via Map::merge, which NPEs on a null
        // value. Unreachable here — points is an int — but it is the standard
        // follow-up, and groupingBy has no such rule.
        // return votes.stream().collect(
        //         Collectors.toMap(
        //                 Vote::candidate,
        //                 Vote::points,
        //                 Integer::sum,
        //                 TreeMap::new));

        // approach 3 — the better code, though not what the challenge drills.
        // groupingBy has no duplicate-key concept at all: it
        // classifies elements into groups and the downstream collector reduces
        // each one, so merging is structural rather than a case to handle. That
        // is why this reads as though the collision problem never existed, and
        // summingInt names the operation instead of spelling it as arithmetic on
        // collision. Prefer toMap when the merge is a *choice* rather than an
        // aggregation — last-wins (a, b) -> b, first-wins, keep-the-larger; those
        // are natural there and awkward here.
        return votes.stream().collect(
                Collectors.groupingBy(
                        Vote::candidate,
                        TreeMap::new,
                        Collectors.summingInt(Vote::points)));
    }

    public static void main(String[] args) {
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("Ada", 8);
        expected.put("Grace", 6);
        expected.put("Linus", 0);
        check("merged totals", "\"Grace\", 7", expected, totals(List.of(
                new Vote("Grace", 7), new Vote("Ada", 5), new Vote("Ada", 3),
                new Vote("Grace", -1), new Vote("Linus", 0))));
        check("empty input", "List.of()", new TreeMap<>(), totals(List.of()));
        checkThat("result type is TreeMap", "\"Ada\", 1", totals(List.of(new Vote("Ada", 1))) instanceof TreeMap);

        // Map.of(...) is a MapN and the actual is a TreeMap, so the printed types
        // differ on every line below. Not a mismatch: deepEquals routes to
        // AbstractMap.equals, which compares entries and ignores implementation.
        check("single vote, merge never invoked", "\"Ada\", 4",
                Map.of("Ada", 4), totals(List.of(new Vote("Ada", 4))));
        check("one candidate, merge invoked n-1 times", "\"Ada\" x4",
                Map.of("Ada", 10), totals(List.of(
                        new Vote("Ada", 1), new Vote("Ada", 2),
                        new Vote("Ada", 3), new Vote("Ada", 4))));
        check("total falls below zero", "\"Ada\", -5, 2",
                Map.of("Ada", -3), totals(List.of(new Vote("Ada", -5), new Vote("Ada", 2))));

        // The TreeMap check above proves the type; only reading the keys back
        // in order proves the contract the type was chosen for.
        check("keys come back sorted, not in encounter order", "Zoe, Ada, Mia",
                List.of("Ada", "Mia", "Zoe"),
                List.copyOf(totals(List.of(
                        new Vote("Zoe", 1), new Vote("Ada", 1), new Vote("Mia", 1))).keySet()));

        // Pins the failure approach 3 sidesteps by construction: the two-arg toMap
        // has no way to combine values, so a repeated key is an
        // IllegalStateException. Built inline rather than through totals(), since
        // totals() is by definition the version that cannot throw it.
        boolean threw = false;
        try {
            List.of(new Vote("Ada", 1), new Vote("Ada", 2)).stream()
                    .collect(Collectors.toMap(Vote::candidate, Vote::points));
        } catch (IllegalStateException e) {
            threw = true;
        }
        checkThat("toMap without a merge function rejects the duplicate key", "\"Ada\" x2", threw);

        report("Challenge 78");
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
