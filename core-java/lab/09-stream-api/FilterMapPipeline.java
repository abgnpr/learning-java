
/*
 * Challenge 65: Filter and Map Pipeline
 * Difficulty: Easy
 *
 * Task: From the supplied users, keep active users whose trimmed name is not
 * blank, normalize their names to uppercase with Locale.ROOT, sort them in
 * natural order, and return an unmodifiable list.
 * Complete: static List<String> activeUserNames(List<User> users)
 * Required focus: filter, map, sorted, and Stream.toList().
 * Run: java FilterMapPipeline.java
 */
import java.util.List;
import java.util.Locale;

public final class FilterMapPipeline {
    record User(String name, boolean active) {
    }

    private FilterMapPipeline() {
    }

    /*
     * Filter order is load-bearing, not stylistic. Each guard has to clear the
     * ground for the one after it: name() != null must precede the strip() on the
     * next line, or a User holding a null name NPEs instead of being skipped.
     *
     * strip().isBlank() reads redundant -- isBlank() already scans for
     * Character.isWhitespace, the same notion strip() trims by, so the two agree on
     * every input. It stays because the task says "trimmed name is not blank", and
     * code that mirrors the spec beats code that needs an equivalence argument to
     * defend. Note what neither call touches: U+00A0 NO-BREAK SPACE is not
     * Character.isWhitespace, so a NBSP-only name survives as a one-character name.
     *
     * Locale.ROOT is the whole reason this is locale-safe. Bare toUpperCase() maps
     * "i" to the dotted "I" under a Turkish default locale, so the same build would
     * produce different output on different machines.
     *
     * sorted() sits after map(), so it orders the uppercased forms. Moving it above
     * map() sorts the raw names instead, and the two disagree whenever case is
     * mixed: 'a' is 0x61 and 'Z' is 0x5A, so "ada" would follow "Zoe" pre-map but
     * "ADA" precedes "ZOE" after it.
     *
     * No distinct(): the contract never asked for one, so two names that collide
     * after normalizing are both kept.
     *
     * toList() returns an unmodifiable list directly -- collect(Collectors.toList())
     * would give a mutable ArrayList and break the stated contract:
     *
     *     .collect(Collectors.toList())   // mutable, wrong here
     *     .collect(Collectors.toUnmodifiableList())   // right, but toList() says it
     */
    static List<String> activeUserNames(List<User> users) {
        return users.stream()
                // Only reachable for a list built with Arrays.asList or ArrayList:
                // List.of rejects null elements at construction.
                .filter(user -> user != null)
                .filter(user -> user.name() != null)
                .filter(user -> user.active())
                .filter(user -> !user.name().strip().isBlank())
                .map(user -> user.name().strip().toUpperCase(Locale.ROOT))
                .sorted()
                .toList();
    }

    public static void main(String[] args) {
        List<User> users = List.of(
                new User("  grace ", true),
                new User("Ada", true),
                new User("Linus", false),
                new User("   ", true));

        check("filter, normalize, and sort", "users", List.of("ADA", "GRACE"), activeUserNames(users));
        check("empty input", "List.of()", List.of(), activeUserNames(List.of()));
        checkThat("result must be unmodifiable", "users", isUnmodifiable(activeUserNames(users)));

        // -- inactive users --
        check("all inactive", "\"Ada\" false, \"Linus\" false", List.of(),
                activeUserNames(List.of(new User("Ada", false), new User("Linus", false))));
        check("inactive user with a blank name is dropped once", "\"  \" false",
                List.of(), activeUserNames(List.of(new User("   ", false))));

        // -- blankness: every shape of "no usable name" --
        check("empty string name", "\"\" true", List.of(), activeUserNames(List.of(new User("", true))));
        check("tab and newline are whitespace", "\"\\t\\n\" true", List.of(),
                activeUserNames(List.of(new User("\t\n", true))));
        // U+00A0 NO-BREAK SPACE: isWhitespace() is false for it, so this name survives
        // and strip() keeps it. Character.isWhitespace excludes non-breaking spaces by
        // definition; String.isBlank() delegates to it.
        check("non-breaking space is not blank", "\"\\u00A0\" true", List.of("\u00A0"),
                activeUserNames(List.of(new User("\u00A0", true))));

        // -- normalization --
        check("interior spaces are kept; only the ends are stripped", "\"  ada  lovelace  \" true",
                List.of("ADA  LOVELACE"), activeUserNames(List.of(new User("  ada  lovelace  ", true))));
        check("already uppercase is unchanged", "\"ADA\" true", List.of("ADA"),
                activeUserNames(List.of(new User("ADA", true))));
        // Locale.ROOT is why this is "I" and not the dotted "İ" a Turkish default locale
        // would produce. The check is locale-independent only because the code says ROOT.
        check("uppercase is locale-independent", "\"i\" true", List.of("I"),
                activeUserNames(List.of(new User("i", true))));

        // -- sorting --
        // sorted() runs after map(), so it orders the uppercased forms by code point.
        // Sorting before the map would have put "ada" (0x61) after "Zoe" (0x5A).
        check("sort is applied to the normalized form", "\"ada\", \"Zoe\"", List.of("ADA", "ZOE"),
                activeUserNames(List.of(new User("ada", true), new User("Zoe", true))));
        check("input order does not leak through", "\"Zoe\", \"Ada\", \"Mia\"",
                List.of("ADA", "MIA", "ZOE"), activeUserNames(List.of(
                        new User("Zoe", true), new User("Ada", true), new User("Mia", true))));
        // No distinct() in the pipeline, and the contract never asked for one: names that
        // collide after normalizing are both kept.
        check("duplicates after normalizing are kept", "\" ada \", \"ADA\"", List.of("ADA", "ADA"),
                activeUserNames(List.of(new User(" ada ", true), new User("ADA", true))));

        // -- null guards --
        // Arrays.asList, not List.of: List.of throws NullPointerException on a null
        // element, so a null-tolerance case cannot even be built with it.
        check("null element is skipped", "Arrays.asList(null, \"Ada\" true)", List.of("ADA"),
                activeUserNames(java.util.Arrays.asList(null, new User("Ada", true))));
        // A non-null User holding a null name: the name() != null filter has to run
        // before strip(), or this NPEs instead of returning a result.
        check("null name is skipped", "Arrays.asList(User(null, true), \"Ada\" true)", List.of("ADA"),
                activeUserNames(java.util.Arrays.asList(new User(null, true), new User("Ada", true))));
        check("only nulls", "Arrays.asList(null, User(null, true))", List.of(),
                activeUserNames(java.util.Arrays.asList(null, new User(null, true))));

        // -- the source list is only read --
        List<User> source = List.of(new User(" bob ", true), new User("Amy", false));
        activeUserNames(source);
        check("input list is not mutated", "source",
                List.of(new User(" bob ", true), new User("Amy", false)), source);

        checkThat("empty result is unmodifiable too", "List.of()", isUnmodifiable(activeUserNames(List.of())));
        report("Challenge 65");
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

    /** True when the list rejects mutation, as an unmodifiable view must. */
    private static boolean isUnmodifiable(java.util.List<?> list) {
        try {
            list.clear();
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }
}
