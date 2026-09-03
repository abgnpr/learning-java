/*
 * Challenge 01: Welcome to Java!
 * Difficulty: Easy
 *
 * Task: Return the exact greeting requested by this small stdout warm-up.
 * Complete: static String greeting()
 * Run: java WelcomeToJava.java
 */
public final class WelcomeToJava {
    private WelcomeToJava() {
    }

    static String greeting() {
        return "Hello, Java!";
    }

    public static void main(String[] args) {
        checkEquals("Hello, Java!", greeting(), "greeting text");
        checkEquals(12, greeting().length(), "greeting length");
        checkEquals(false, greeting().endsWith("\n"), "no embedded newline");
        if (failures > 0) {
            throw new AssertionError("Challenge 01: " + failures + " check(s) failed.");
        }
        System.out.println("Challenge 01 passed.");
    }

    private static int failures = 0;

    private static void checkEquals(Object expected, Object actual, String label) {
        if (java.util.Objects.equals(expected, actual)) {
            System.out.println("PASS " + label + ": " + show(actual));
            return;
        }
        failures++;
        System.out.println("FAIL " + label
                + "\n  expected: " + show(expected)
                + "\n    actual: " + show(actual));
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
}
