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

        check("known name", "phoneBook, List.of(\"sam\")", List.of("sam=99912222"), lookup(phoneBook, List.of("sam")));
        check("unknown name", "phoneBook, List.of(\"edward\")", List.of("Not found"), lookup(phoneBook, List.of("edward")));
        check("queries preserve order", "phoneBook, List.of(\"harry\", \"sam\", \"nobody\")", List.of("harry=12299933", "sam=99912222", "Not found"), lookup(phoneBook, List.of("harry", "sam", "nobody")));
        report("Challenge 34");
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
