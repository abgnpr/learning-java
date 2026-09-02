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
        checkEquals(
            List.of(
                new Player("aleksa", 150),
                new Player("amy", 100),
                new Player("david", 100),
                new Player("aakansha", 75),
                new Player("heraldo", 50)
            ),
            sorted(List.of(
                new Player("amy", 100),
                new Player("david", 100),
                new Player("heraldo", 50),
                new Player("aakansha", 75),
                new Player("aleksa", 150)
            )),
            "score then name"
        );
        checkEquals(
            List.of(new Player("bob", 11), new Player("anna", 10), new Player("zoe", 10)),
            sorted(List.of(new Player("zoe", 10), new Player("anna", 10), new Player("bob", 11))),
            "name tie-breaker"
        );
        checkEquals(
            List.of(new Player("solo", 0)),
            sorted(List.of(new Player("solo", 0))),
            "single player"
        );
        System.out.println("Challenge 38 passed");
    }

    static List<Player> sorted(List<Player> players) {
        List<Player> copy = new ArrayList<>(players);
        copy.sort(new Ranking());
        return copy;
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label
                    + ":\n  expected: <" + expected + ">"
                    + "\n    actual: <" + actual + ">");
        }
    }
}
