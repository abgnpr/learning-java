/*
 * Challenge 34 — Java Map (Easy)
 *
 * Task: Look up names in a phone book and return one result String per query,
 * in query order: "name=number" when present or "Not found" when absent.
 * Complete: lookup(Map<String, String> phoneBook, List<String> queries)
 * Run: java PhoneBookLookup.java
 */
import java.util.List;
import java.util.Map;

public class PhoneBookLookup {
    static List<String> lookup(Map<String, String> phoneBook, List<String> queries) {
        // TODO: Resolve queries in their original order with Map lookups.
        throw new UnsupportedOperationException("TODO: query the phone book");
    }

    public static void main(String[] args) {
        Map<String, String> phoneBook = Map.of(
            "sam", "99912222",
            "tom", "11122222",
            "harry", "12299933"
        );

        checkEquals(List.of("sam=99912222"), lookup(phoneBook, List.of("sam")), "known name");
        checkEquals(List.of("Not found"), lookup(phoneBook, List.of("edward")), "unknown name");
        checkEquals(
            List.of("harry=12299933", "sam=99912222", "Not found"),
            lookup(phoneBook, List.of("harry", "sam", "nobody")),
            "queries preserve order"
        );
        System.out.println("Challenge 34 passed");
    }

    static void checkEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
