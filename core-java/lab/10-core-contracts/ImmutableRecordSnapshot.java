/*
 * Challenge 86 — Immutable Record Snapshot (Hard)
 *
 * Task: Keep Profile deeply immutable even when constructed from a mutable
 * roles list. Apply sealed change values by returning new profiles. A Profile
 * used as a hash key must remain findable after the caller mutates its source.
 * Complete: Profile compact constructor, Profile.apply(Change)
 * Run: java ImmutableRecordSnapshot.java
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImmutableRecordSnapshot {
    sealed interface Change permits NameChanged, RolesChanged { }
    record NameChanged(String name) implements Change { }
    record RolesChanged(List<String> roles) implements Change { }

    record Profile(String id, String name, List<String> roles) {
        Profile {
            // TODO: Validate required values and defensively snapshot roles.
            throw new UnsupportedOperationException("TODO: make Profile immutable");
        }

        Profile apply(Change change) {
            // TODO: Exhaustively create the changed Profile without mutating this one.
            throw new UnsupportedOperationException("TODO: apply the sealed change");
        }
    }

    public static void main(String[] args) {
        List<String> sourceRoles = new ArrayList<>(List.of("reader"));
        Profile original = new Profile("u-1", "Asha", sourceRoles);
        Map<Profile, String> index = new HashMap<>();
        index.put(original, "stored");

        sourceRoles.add("admin");
        check("constructor takes a defensive snapshot", sourceRoles,
                List.of("reader"), original.roles());
        checkThat("roles cannot be mutated through accessor", original.roles(),
                isUnmodifiable(original.roles()));
        check("hash key remains stable after source mutation", "lookup equal snapshot",
                "stored", index.get(new Profile("u-1", "Asha", List.of("reader"))));
        Profile equalValue = new Profile("u-1", "Asha", List.of("reader"));
        checkThat("value equality is independent of identity", "two equal profiles",
                original != equalValue && original.equals(equalValue));

        Profile renamed = original.apply(new NameChanged("Mira"));
        check("name change returns new value", renamed,
                new Profile("u-1", "Mira", List.of("reader")), renamed);
        check("original is unchanged", original,
                new Profile("u-1", "Asha", List.of("reader")), original);

        List<String> replacement = new ArrayList<>(List.of("writer"));
        Profile rerolled = original.apply(new RolesChanged(replacement));
        replacement.add("owner");
        check("change payload is also snapshotted", replacement,
                List.of("writer"), rerolled.roles());
        report("Challenge 86");
    }

    private static boolean isUnmodifiable(List<?> values) {
        try {
            values.clear();
            return false;
        } catch (UnsupportedOperationException expected) {
            return true;
        }
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
