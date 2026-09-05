/*
 * Challenge 38 — Java Comparator (Medium)
 *
 * Task: Rank players by decreasing score, using alphabetical name order when
 * scores tie.
 * Complete: Ranking.compare(Player left, Player right)
 * Run: java PlayerRankingComparator.java
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerRankingComparator {
    record Player(String name, int score) { }

    static class Ranking implements Comparator<Player> {
        @Override
        public int compare(Player left, Player right) {
            // TODO: Implement the two-level ordering contract.
            throw new UnsupportedOperationException("TODO: compare players");
        }
    }

    public static void main(String[] args) {
        check("score then name", "\"amy\", 100", List.of(
                new Player("aleksa", 150),
                new Player("amy", 100),
                new Player("david", 100),
                new Player("aakansha", 75),
                new Player("heraldo", 50)
            ), sorted(List.of(
                new Player("amy", 100),
                new Player("david", 100),
                new Player("heraldo", 50),
                new Player("aakansha", 75),
                new Player("aleksa", 150)
            )));
        check("name tie-breaker", "\"zoe\", 10", List.of(new Player("bob", 11), new Player("anna", 10), new Player("zoe", 10)), sorted(List.of(new Player("zoe", 10), new Player("anna", 10), new Player("bob", 11))));
        check("single player", "\"solo\", 0", List.of(new Player("solo", 0)), sorted(List.of(new Player("solo", 0))));
        report("Challenge 38");
    }

    static List<Player> sorted(List<Player> players) {
        List<Player> copy = new ArrayList<>(players);
        copy.sort(new Ranking());
        return copy;
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
